package com.outdu.camconnect.ui.setupflow

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import com.outdu.camconnect.security.MandatoryPermissionManager
import com.outdu.camconnect.utils.WifiCredentials
import java.util.concurrent.Executors
import org.json.JSONObject

private const val TAG = "QRScannerScreen"
private val QR_SCANNER: BarcodeScanner by lazy {
    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    BarcodeScanning.getClient(options)
}

/**
 * Result of scanning a QR code. If the QR is a WiFi QR, [wifiCredentials] will be populated.
 */
data class QrScanResult(
    val rawValue: String,
    val wifiCredentials: WifiCredentials? = null
)

@Composable
fun QRScannerScreen(
    onQRScanned: (QrScanResult) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionManager = remember { MandatoryPermissionManager.getInstance() }
    var flashEnabled by remember { mutableStateOf(false) }
    var cameraPermissionGranted by remember { mutableStateOf(false) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var hasScanned by remember { mutableStateOf(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Check camera permission
    LaunchedEffect(Unit) {
        cameraPermissionGranted = permissionManager.hasCameraPermission(context)
        if (!cameraPermissionGranted) {
            Log.w(TAG, "Camera permission not granted (scanner will not start)")
        } else {
            Log.d(TAG, "Camera permission granted")
        }
    }

    // Apply torch changes after the camera is bound (the old code only set torch once at bind time)
    LaunchedEffect(flashEnabled, boundCamera) {
        val camera = boundCamera ?: return@LaunchedEffect
        if (camera.cameraInfo.hasFlashUnit()) {
            Log.d(TAG, "Applying torch=$flashEnabled")
            camera.cameraControl.enableTorch(flashEnabled)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (cameraPermissionGranted) {
            // Camera Preview
            AndroidView(
            factory = { context ->
                val previewView = PreviewView(context)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    val cameraProvider = try {
                        cameraProviderFuture.get()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to get ProcessCameraProvider", e)
                        return@addListener
                    }

                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (hasScanned) {
                            imageProxy.close()
                            return@setAnalyzer
                        }
                        Log.v(
                            TAG,
                            "Analyzer frame received (w=${imageProxy.width}, h=${imageProxy.height}, format=${imageProxy.format}, rotation=${imageProxy.imageInfo.rotationDegrees})"
                        )
                        processImageProxy(
                            imageProxy,
                            onQRScanned = { qrData ->
                            if (qrData.isNotEmpty()) {
                                Log.i(TAG, "QR detected rawValue:\n$qrData")
                                hasScanned = true
                                onQRScanned(QrScanResult(rawValue = qrData))
                            }
                            },
                            onWifiScanned = { creds ->
                                Log.i(TAG, "WiFi QR detected: ssid='${creds.ssid}'")
                                hasScanned = true
                                // Normalize WiFi scan result into JSON so callers can parse consistently.
                                val payloadObj = JSONObject()
                                    .put("ssid", creds.ssid)
                                    .put("password", creds.password)
                                creds.ip?.let { payloadObj.put("ip", it) }
                                creds.macAddress?.let { payloadObj.put("mac", it) }
                                val payload = payloadObj.toString()
                                Log.i(TAG, "WiFi QR payload rawValue:\n$payload")
                                onQRScanned(QrScanResult(rawValue = payload, wifiCredentials = creds))
                            }
                        )
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        boundCamera = camera

                        // Handle flash
                        if (camera.cameraInfo.hasFlashUnit()) {
                            Log.d(TAG, "Camera bound; flash unit present")
                        } else {
                            Log.d(TAG, "Device has no flash unit")
                        }

                    } catch (exc: Exception) {
                        Log.e(TAG, "CameraX bindToLifecycle failed", exc)
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay with scanning frame
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawScanningOverlay()
        }
        } else {
            // Permission denied screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.camera_line),
                    contentDescription = "Camera Permission Required",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Camera Permission Required",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.space_grotesk)),
                        fontWeight = FontWeight(700),
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Camera access is required to scan QR codes. Please grant camera permission in app settings.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color.White.copy(alpha = 0.8f)
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        permissionManager.openAppSettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StravionBlue
                    )
                ) {
                    Text("Open Settings")
                }
            }
        }

        // Top controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Flash toggle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { flashEnabled = !flashEnabled },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flash",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Instructions
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Scan QR Code",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.space_grotesk)),
                    fontWeight = FontWeight(700),
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Position the QR code within the frame to scan",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.arial_regular)),
                    fontWeight = FontWeight(400),
                    color = Color.White.copy(alpha = 0.8f)
                ),
                textAlign = TextAlign.Center
            )
        }


    }
}

private fun DrawScope.drawScanningOverlay() {
    val overlayColor = Color.Black.copy(alpha = 0.5f)
    val scanAreaSize = size.width * 0.7f
    val scanAreaLeft = (size.width - scanAreaSize) / 2
    val scanAreaTop = (size.height - scanAreaSize) / 2

    // Draw overlay with transparent scanning area
    drawRect(
        color = overlayColor,
        topLeft = Offset.Zero,
        size = Size(size.width, scanAreaTop)
    )

    drawRect(
        color = overlayColor,
        topLeft = Offset(0f, scanAreaTop + scanAreaSize),
        size = Size(size.width, size.height - scanAreaTop - scanAreaSize)
    )

    drawRect(
        color = overlayColor,
        topLeft = Offset(0f, scanAreaTop),
        size = Size(scanAreaLeft, scanAreaSize)
    )

    drawRect(
        color = overlayColor,
        topLeft = Offset(scanAreaLeft + scanAreaSize, scanAreaTop),
        size = Size(size.width - scanAreaLeft - scanAreaSize, scanAreaSize)
    )

    // Draw scanning frame corners
    val cornerLength = 30f
    val cornerWidth = 4f
    val frameColor = Color.White

    // Top-left corner
    drawRect(
        color = frameColor,
        topLeft = Offset(scanAreaLeft, scanAreaTop),
        size = Size(cornerLength, cornerWidth)
    )
    drawRect(
        color = frameColor,
        topLeft = Offset(scanAreaLeft, scanAreaTop),
        size = Size(cornerWidth, cornerLength)
    )

    // Top-right corner
    drawRect(
        color = frameColor,
        topLeft = Offset(scanAreaLeft + scanAreaSize - cornerLength, scanAreaTop),
        size = Size(cornerLength, cornerWidth)
    )
    drawRect(
        color = frameColor,
        topLeft = Offset(scanAreaLeft + scanAreaSize - cornerWidth, scanAreaTop),
        size = Size(cornerWidth, cornerLength)
    )

    // Bottom-left corner
    drawRect(
        color = frameColor,
        topLeft = Offset(scanAreaLeft, scanAreaTop + scanAreaSize - cornerWidth),
        size = Size(cornerLength, cornerWidth)
    )
    drawRect(
        color = frameColor,
        topLeft = Offset(scanAreaLeft, scanAreaTop + scanAreaSize - cornerLength),
        size = Size(cornerWidth, cornerLength)
    )

    // Bottom-right corner
    drawRect(
        color = frameColor,
        topLeft = Offset(scanAreaLeft + scanAreaSize - cornerLength, scanAreaTop + scanAreaSize - cornerWidth),
        size = Size(cornerLength, cornerWidth)
    )
    drawRect(
        color = frameColor,
        topLeft = Offset(scanAreaLeft + scanAreaSize - cornerWidth, scanAreaTop + scanAreaSize - cornerLength),
        size = Size(cornerWidth, cornerLength)
    )
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onQRScanned: (String) -> Unit,
    onWifiScanned: (WifiCredentials) -> Unit = {}
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        QR_SCANNER.process(image)
            .addOnSuccessListener { barcodes ->
                Log.v(TAG, "MLKit success: barcodes=${barcodes.size}")
                for (barcode in barcodes) {
                    Log.v(TAG, "Barcode format=${barcode.format} valueType=${barcode.valueType}")
                    when (barcode.valueType) {
                        Barcode.TYPE_WIFI -> {
                            val wifi = barcode.wifi
                            val raw = barcode.rawValue.orEmpty()
                            Log.i(TAG, "WiFi QR rawValue:\n$raw")

                            val parsed = parseWifiCredentialsFromRawValue(raw)
                            val ssid = wifi?.ssid ?: parsed?.ssid
                            // ML Kit sometimes returns empty password even if QR contains it; fallback to rawValue parsing.
                            val password = wifi?.password?.takeIf { it.isNotBlank() } ?: parsed?.password.orEmpty()

                            if (!ssid.isNullOrBlank()) {
                                onWifiScanned(
                                    WifiCredentials(
                                        ssid = ssid,
                                        password = password,
                                        ip = parsed?.ip,
                                        macAddress = parsed?.macAddress
                                    )
                                )
                            } else {
                                Log.w(TAG, "TYPE_WIFI barcode but ssid was null/blank")
                            }
                        }
                        Barcode.TYPE_TEXT,
                        Barcode.TYPE_URL -> {
                            barcode.rawValue?.let { qrData ->
                                onQRScanned(qrData)
                            }
                        }
                        else -> {
                            Log.v(TAG, "Ignoring barcode valueType=${barcode.valueType}")
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "MLKit barcode scanning failed", it)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        Log.w(TAG, "Analyzer got null mediaImage")
        imageProxy.close()
    }
}

/**
 * Parses standard WiFi QR payloads like:
 * WIFI:T:WPA;S:MySSID;P:MyPassword;H:false;;
 *
 * Some scanners (including ML Kit's structured TYPE_WIFI) may omit the password; parsing rawValue
 * provides a fallback.
 */
private fun parseWifiQrRawValue(raw: String): WifiCredentials? {
    val trimmed = raw.trim()
    if (!trimmed.startsWith("WIFI:", ignoreCase = true)) return null

    // Remove leading "WIFI:" and trailing ";;" if present.
    var body = trimmed.removePrefix("WIFI:")
    if (body.endsWith(";;")) body = body.dropLast(2)

    // Split by unescaped ';'
    val fields = splitUnescaped(body, ';')
    var ssid: String? = null
    var password: String? = null

    for (field in fields) {
        val idx = field.indexOf(':')
        if (idx <= 0) continue
        val key = field.substring(0, idx)
        val value = unescapeWifiField(field.substring(idx + 1))
        when (key.uppercase()) {
            "S" -> ssid = value
            "P" -> password = value
        }
    }

    if (ssid.isNullOrBlank()) return null
    return WifiCredentials(ssid = ssid, password = password.orEmpty())
}

private fun parseWifiCredentialsFromRawValue(raw: String): WifiCredentials? {
    val trimmed = raw.trim()
    // Standard WiFi QR format
    parseWifiQrRawValue(trimmed)?.let { return it }

    // Some devices/QR generators embed WiFi creds as JSON text (e.g. {"ssid":"...","password":"...","ip":"...","mac":"..."})
    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
        return try {
            val json = JSONObject(trimmed)
            if (!json.has("ssid")) return null

            val macValue = json.optString("mac", "")
                .ifBlank { json.optString("macaddress", "") }
                .ifBlank { json.optString("macAddress", "") }
                .takeIf { it.isNotBlank() }

            WifiCredentials(
                ssid = json.getString("ssid"),
                password = json.optString("password", ""),
                ip = json.optString("ip", "").takeIf { it.isNotBlank() },
                macAddress = macValue
            )
        } catch (_: Exception) {
            null
        }
    }

    return null
}

private fun splitUnescaped(input: String, delimiter: Char): List<String> {
    val out = ArrayList<String>()
    val sb = StringBuilder()
    var escaped = false
    for (c in input) {
        when {
            escaped -> {
                sb.append(c)
                escaped = false
            }
            c == '\\' -> {
                // keep the backslash for unescape step
                sb.append(c)
                escaped = true
            }
            c == delimiter -> {
                out.add(sb.toString())
                sb.setLength(0)
            }
            else -> sb.append(c)
        }
    }
    out.add(sb.toString())
    return out
}

private fun unescapeWifiField(value: String): String {
    // Spec uses backslash escaping for characters like \; \: \, \\ and \"
    val sb = StringBuilder()
    var escaped = false
    for (c in value) {
        if (escaped) {
            sb.append(c)
            escaped = false
        } else if (c == '\\') {
            escaped = true
        } else {
            sb.append(c)
        }
    }
    return sb.toString()
}
