package com.outdu.camconnect.ui.components.settings.ota

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.R
import com.outdu.camconnect.communication.MotocamAPIAndroidHelper
import com.outdu.camconnect.communication.MotocamSocketClient
import com.outdu.camconnect.communication.CameraApiManager
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import io.ktor.http.ContentType
import java.io.InputStream
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import android.provider.OpenableColumns
import com.outdu.camconnect.profiler.*

@Composable
fun OtaLayout() {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var firmwareVersion by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatus by remember { mutableStateOf("") }
    var uploadComplete by remember { mutableStateOf(false) }
    var showOtaDialog by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(120) }
    var otaStatus by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedFile = uri
        }
    )

    LaunchedEffect(Unit) {

        val specs = getDeviceSpecs(context)
        val score = estimatePerformanceScore(specs)
        val tier = classifyPerformance(score)
        val message = getPerformanceMessage(tier)

        Log.d("Hardware Profiler", specs.toString())
        Log.d("Hardware Profiler", "Score: $score, Tier: ${tier.label}")
        Log.w("Hardware Profiler", message)

        // Api call to get firmware version
        fetchFirmwareVersion(
            scope = scope,
            onSuccess = {
                firmwareVersion = it
            },
            onError = {
                Log.e("OTALayout", "Firmware fetch failed: $it")
            }
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            FirmwareCard(firmwareVersion)

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(StravionBlue)
                    .clickable {
                        // Always open file picker
                        launcher.launch("*/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Select Firmware",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight(500),
                        color = Color.White,
                        fontFamily = FontFamily(Font(R.font.onest_regular))
                    )
                )
            }

// Show selected file name
            selectedFile?.let { uri ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Selected file: ${uri.lastPathSegment}")

                    // Update firmware button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isUploading) Color.Gray else StravionBlue)
                            .clickable(enabled = !isUploading) {
                                uploadFirmwareFile(
                                    context = context,
                                    scope = scope,
                                    onSuccess = {
                                        Log.d("OTALayout", "Firmware upload success: $it")
                                        uploadStatus = "✅ Upload complete: ${selectedFile?.lastPathSegment}"
                                        uploadComplete = true
                                    },
                                    onError = {
                                        Log.e("OTALayout", "Firmware upload failed: $it")
                                        uploadStatus = "❌ Upload failed: $it"
                                        uploadComplete = false
                                    },
                                    selectedFile = selectedFile,
                                    onUploadStart = {
                                        isUploading = true
                                        uploadStatus = "Preparing upload..."
                                    },
                                    onUploadComplete = {
                                        isUploading = false
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isUploading) "Uploading..." else "Upload Firmware",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight(500),
                                color = Color.White,
                                fontFamily = FontFamily(Font(R.font.onest_regular))
                            )
                        )
                    }
                    
                    // Show upload status
                    if (uploadStatus.isNotEmpty()) {
                        Text(
                            text = uploadStatus,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight(400),
                                color = if (uploadStatus.contains("✅", ignoreCase = true)) Color.Green else Color.Red,
                                fontFamily = FontFamily(Font(R.font.onest_regular))
                            )
                        )
                    }
                    
                    // Show "Update Firmware" button after successful upload
                    if (uploadComplete) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(StravionBlue)
                                .clickable {
                                    handleFirmwareUpdate(
                                        scope = scope,
                                        onSuccess = {
                                            Log.d("OTALayout", "OTA update success: $it")
                                            otaStatus = "✅ Firmware update successful. Please reboot the camera manually."
                                        },
                                        onError = {
                                            Log.e("OTALayout", "OTA update failed: $it")
                                            otaStatus = "❌ Firmware update failed."
                                        },
                                        onStart = {
                                            showOtaDialog = true
                                            countdown = 120
                                            otaStatus = null
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Update Firmware",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight(500),
                                    color = Color.White,
                                    fontFamily = FontFamily(Font(R.font.onest_regular))
                                )
                            )
                        }
                    }
                }
            }
        }
        
        // OTA Update Dialog
        if (showOtaDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showOtaDialog = false },
                title = {
                    Text(
                        text = if (otaStatus == null) "Flashing firmware..." else "Firmware Update Result",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight(600),
                            fontFamily = FontFamily(Font(R.font.onest_regular))
                        )
                    )
                },
                text = {
                    if (otaStatus == null) {
                        Text(
                            text = "Checking update status in $countdown second${if (countdown != 1) "s" else ""}...",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.onest_regular))
                            )
                        )
                    } else {
                        Text(
                            text = otaStatus!!,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.onest_regular))
                            )
                        )
                    }
                },
                confirmButton = {
                    if (otaStatus != null) {
                        androidx.compose.material3.TextButton(
                            onClick = { showOtaDialog = false }
                        ) {
                            Text("Close")
                        }
                    }
                }
            )
        }
    }
}


@Composable
fun FirmwareCard(firmwareVersion: String) {
    Text(
        text = "Firmware Version: $firmwareVersion",
        style = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight(500),
            color = StravionBlue,
            fontFamily = FontFamily(Font(R.font.onest_regular))
        )
    )
}

private fun fetchFirmwareVersion(
    scope: kotlinx.coroutines.CoroutineScope,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    MotocamAPIAndroidHelper.getFirmwareVersionAsync(scope) { status, error ->

        if (error != null) {
            Log.e("OTALayout", "Firmware fetch failed: $error")
            onError(error)
            return@getFirmwareVersionAsync
        }

        status?.let {
            Log.d("OTALayout", " Firmware Version is  $it .")
            onSuccess(it)
        } ?: onError("No health status received")

    }
}

private fun uploadFirmwareFile(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    selectedFile: Uri?,
    onUploadStart: () -> Unit,
    onUploadComplete: () -> Unit
) {
    if (selectedFile == null) {
        onError("No file selected")
        return
    }

    scope.launch {
        try {
            onUploadStart()
            
            // Read file content from URI
            val inputStream: InputStream? = context.contentResolver.openInputStream(selectedFile)
            if (inputStream == null) {
                onError("Could not read selected file")
                return@launch
            }
            
            val fileBytes = inputStream.readBytes()
            inputStream.close()
            
            // Resolve filename from URI using ContentResolver (falls back to lastPathSegment)
            val displayName = getDisplayName(context, selectedFile)
            val originalFileName = selectedFile.lastPathSegment ?: "firmware.bin"
            val fileName = displayName ?: originalFileName
            Log.d("OTALayout", "Original filename: $originalFileName, Resolved displayName: $displayName, Using: $fileName")
            Log.d("OTALayout", "FileName is : ${selectedFile}")
            
            // Get current camera IP from CameraApiManager
            val cameraApiManager = CameraApiManager.getInstance()
            val currentCameraIp = cameraApiManager.getCurrentDeviceIP()
            Log.d("OTALayout", "Using camera IP: $currentCameraIp")
            
            // Initialize MotocamSocketClient with current camera IP
            val client = MotocamSocketClient()
            client.init(currentCameraIp)// Use the current camera IP
            
            // Upload the firmware file to the web UI server (like the web interface does)
            val uploadSuccess = client.uploadFile(
                fileName = fileName,
                fileBytes = fileBytes,
                port = 80, // Use web UI server port (matching web interface)
                fieldName = "file",
                contentType = ContentType.Application.OctetStream
            )
            
            if (uploadSuccess) {
                Log.d("OTALayout", "Firmware file uploaded successfully")
                onSuccess("Firmware uploaded successfully")
            } else {
                onError("Upload failed - server returned error")
            }
            
        } catch (e: Exception) {
            Log.e("OTALayout", "Firmware upload exception", e)
            onError("Upload failed: ${e.message}")
        } finally {
            onUploadComplete()
        }
    }
}

private fun handleFirmwareUpdate(
    scope: kotlinx.coroutines.CoroutineScope,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onStart: () -> Unit
) {
    onStart()
    
    scope.launch {
        try {
            // Start countdown timer (120 seconds = 2 minutes)
            val timer = scope.launch {
                repeat(10) { i ->
                    kotlinx.coroutines.delay(1000)
                    // Countdown is handled in UI state
                }
                // After countdown, check OTA status
                checkOTAStatus(scope, onSuccess, onError)
            }
            
            // Trigger OTA update in the background
            MotocamAPIAndroidHelper.setOtaUpdateAsync(scope) { status, error ->
                if (error != null) {
                    Log.e("OTALayout", "OTA update failed: $error")
                    onError("OTA update failed: $error")
                } else {
                    status?.let {
                        Log.d("OTALayout", "OTA update status: $it")
                        // Status will be checked after countdown
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e("OTALayout", "OTA update exception", e)
            onError("OTA update failed: ${e.message}")
        }
    }
}

private fun checkOTAStatus(
    scope: kotlinx.coroutines.CoroutineScope,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    scope.launch {
        try {
            // Call the existing getOtaUpdateAsync function
            MotocamAPIAndroidHelper.getOtaUpdateAsync(scope) { status, error ->
                if (error != null) {
                    Log.e("OTALayout", "OTA status check failed: $error")
                    onError("Failed to check OTA status: $error")
                } else {
                    status?.let {
                        Log.d("OTALayout", "OTA status response: $it")
                        // Parse the response and show appropriate message
                        when {
                            it.toString().contains("success", ignoreCase = true) -> {
                                onSuccess("✅ Firmware update successful. Please reboot the camera manually.")
                            }
                            it.toString().contains("fail", ignoreCase = true) -> {
                                onError("❌ Firmware update failed.")
                            }
                            else -> {
                                onError("⚠️ Unable to read update status.")
                            }
                        }
                    } ?: onError("⚠️ No OTA status received.")
                }
            }
        } catch (e: Exception) {
            Log.e("OTALayout", "OTA status check exception", e)
            onError("Failed to check OTA status: ${e.message}")
        }
    }
}

private fun getDisplayName(context: Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
    }
}