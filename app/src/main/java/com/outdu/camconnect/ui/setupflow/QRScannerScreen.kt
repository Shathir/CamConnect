//package com.outdu.camconnect.ui.setupflow
//
//import android.Manifest
//import androidx.annotation.OptIn
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.FlashOff
//import androidx.compose.material.icons.filled.FlashOn
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.geometry.CornerRadius
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.BlendMode
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.drawscope.DrawScope
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.LifecycleOwner
//import com.google.mlkit.vision.barcode.BarcodeScanning
//import com.google.mlkit.vision.barcode.common.Barcode
//import com.google.mlkit.vision.common.InputImage
//import com.outdu.camconnect.R
//import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//@Composable
//fun QRScannerScreen(
//    onQRScanned: (String) -> Unit,
//    onBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    var flashEnabled by remember { mutableStateOf(false) }
//
//
//    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
//
//    DisposableEffect(Unit) {
//        onDispose {
//            cameraExecutor.shutdown()
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black)
//    ) {
//        // Camera Preview
//        AndroidView(
//            factory = { context ->
//                val previewView = PreviewView(context)
//                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
//
//                cameraProviderFuture.addListener({
//                    val cameraProvider = cameraProviderFuture.get()
//
//                    val preview = Preview.Builder().build()
//                    preview.setSurfaceProvider(previewView.surfaceProvider)
//
//                    val imageAnalysis = ImageAnalysis.Builder()
//                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                        .build()
//
//                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
//                        processImageProxy(imageProxy) { qrData ->
//                            if (qrData.isNotEmpty()) {
//                                onQRScanned(qrData)
//                            }
//                        }
//                    }
//
//                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//                    try {
//                        cameraProvider.unbindAll()
//                        val camera = cameraProvider.bindToLifecycle(
//                            lifecycleOwner,
//                            cameraSelector,
//                            preview,
//                            imageAnalysis
//                        )
//
//                        // Handle flash
//                        if (camera.cameraInfo.hasFlashUnit()) {
//                            camera.cameraControl.enableTorch(flashEnabled)
//                        }
//
//                    } catch (exc: Exception) {
//                        // Handle camera binding error
//                    }
//                }, ContextCompat.getMainExecutor(context))
//
//                previewView
//            },
//            modifier = Modifier.fillMaxSize()
//        )
//
//        // Overlay with scanning frame
//        Canvas(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            drawScanningOverlay()
//        }
//
//        // Top controls
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//                .align(Alignment.TopStart),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Back button
//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .clip(CircleShape)
//                    .background(Color.Black.copy(alpha = 0.5f))
//                    .clickable { onBack() },
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Default.ArrowBack,
//                    contentDescription = "Back",
//                    tint = Color.White,
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//
//            // Flash toggle
//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .clip(CircleShape)
//                    .background(Color.Black.copy(alpha = 0.5f))
//                    .clickable { flashEnabled = !flashEnabled },
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
//                    contentDescription = "Flash",
//                    tint = Color.White,
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//        }
//
//        // Instructions
//        Column(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(32.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = "Scan QR Code",
//                style = TextStyle(
//                    fontSize = 24.sp,
//                    fontFamily = FontFamily(Font(R.font.space_grotesk)),
//                    fontWeight = FontWeight(700),
//                    color = Color.White
//                ),
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = "Position the QR code within the frame to scan",
//                style = TextStyle(
//                    fontSize = 14.sp,
//                    fontFamily = FontFamily(Font(R.font.arial_regular)),
//                    fontWeight = FontWeight(400),
//                    color = Color.White.copy(alpha = 0.8f)
//                ),
//                textAlign = TextAlign.Center
//            )
//        }
//
//
//    }
//}
//
//private fun DrawScope.drawScanningOverlay() {
//    val overlayColor = Color.Black.copy(alpha = 0.5f)
//    val scanAreaSize = size.width * 0.7f
//    val scanAreaLeft = (size.width - scanAreaSize) / 2
//    val scanAreaTop = (size.height - scanAreaSize) / 2
//
//    // Draw overlay with transparent scanning area
//    drawRect(
//        color = overlayColor,
//        topLeft = Offset.Zero,
//        size = Size(size.width, scanAreaTop)
//    )
//
//    drawRect(
//        color = overlayColor,
//        topLeft = Offset(0f, scanAreaTop + scanAreaSize),
//        size = Size(size.width, size.height - scanAreaTop - scanAreaSize)
//    )
//
//    drawRect(
//        color = overlayColor,
//        topLeft = Offset(0f, scanAreaTop),
//        size = Size(scanAreaLeft, scanAreaSize)
//    )
//
//    drawRect(
//        color = overlayColor,
//        topLeft = Offset(scanAreaLeft + scanAreaSize, scanAreaTop),
//        size = Size(size.width - scanAreaLeft - scanAreaSize, scanAreaSize)
//    )
//
//    // Draw scanning frame corners
//    val cornerLength = 30f
//    val cornerWidth = 4f
//    val frameColor = Color.White
//
//    // Top-left corner
//    drawRect(
//        color = frameColor,
//        topLeft = Offset(scanAreaLeft, scanAreaTop),
//        size = Size(cornerLength, cornerWidth)
//    )
//    drawRect(
//        color = frameColor,
//        topLeft = Offset(scanAreaLeft, scanAreaTop),
//        size = Size(cornerWidth, cornerLength)
//    )
//
//    // Top-right corner
//    drawRect(
//        color = frameColor,
//        topLeft = Offset(scanAreaLeft + scanAreaSize - cornerLength, scanAreaTop),
//        size = Size(cornerLength, cornerWidth)
//    )
//    drawRect(
//        color = frameColor,
//        topLeft = Offset(scanAreaLeft + scanAreaSize - cornerWidth, scanAreaTop),
//        size = Size(cornerWidth, cornerLength)
//    )
//
//    // Bottom-left corner
//    drawRect(
//        color = frameColor,
//        topLeft = Offset(scanAreaLeft, scanAreaTop + scanAreaSize - cornerWidth),
//        size = Size(cornerLength, cornerWidth)
//    )
//    drawRect(
//        color = frameColor,
//        topLeft = Offset(scanAreaLeft, scanAreaTop + scanAreaSize - cornerLength),
//        size = Size(cornerWidth, cornerLength)
//    )
//
//    // Bottom-right corner
//    drawRect(
//        color = frameColor,
//        topLeft = Offset(scanAreaLeft + scanAreaSize - cornerLength, scanAreaTop + scanAreaSize - cornerWidth),
//        size = Size(cornerLength, cornerWidth)
//    )
//    drawRect(
//        color = frameColor,
//        topLeft = Offset(scanAreaLeft + scanAreaSize - cornerWidth, scanAreaTop + scanAreaSize - cornerLength),
//        size = Size(cornerWidth, cornerLength)
//    )
//}
//
//@OptIn(ExperimentalGetImage::class)
//private fun processImageProxy(
//    imageProxy: ImageProxy,
//    onQRScanned: (String) -> Unit
//) {
//    val mediaImage = imageProxy.image
//    if (mediaImage != null) {
//        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//        val scanner = BarcodeScanning.getClient()
//
//        scanner.process(image)
//            .addOnSuccessListener { barcodes ->
//                for (barcode in barcodes) {
//                    when (barcode.valueType) {
//                        Barcode.TYPE_TEXT,
//                        Barcode.TYPE_URL -> {
//                            barcode.rawValue?.let { qrData ->
//                                onQRScanned(qrData)
//                            }
//                        }
//                    }
//                }
//            }
//            .addOnFailureListener {
//                // Handle scanning failure
//            }
//            .addOnCompleteListener {
//                imageProxy.close()
//            }
//    } else {
//        imageProxy.close()
//    }
//}
