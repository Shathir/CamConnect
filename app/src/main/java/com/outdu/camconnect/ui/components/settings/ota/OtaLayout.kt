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
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue

@Composable
fun OtaLayout() {

    val scope = rememberCoroutineScope()
    var firmwareVersion by remember { mutableStateOf("") }
    var selectedFile by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedFile = uri
        }
    )

    LaunchedEffect(Unit) {
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
                            .background(StravionBlue)
                            .clickable {
                                updateFirmware(
                                    scope,
                                    onSuccess = {
                                        Log.d("OTALayout", "Firmware update success: $it")
                                        selectedFile = null
                                    },
                                    onError = {
                                        Log.e("OTALayout", "Firmware update failed: $it")
                                    },
                                    selectedFile
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

private fun updateFirmware(
    scope: kotlinx.coroutines.CoroutineScope,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    selectedFile: Uri?
)
{
    MotocamAPIAndroidHelper.setOtaUpdateAsync(scope) {
            status, error ->

        if (error != null) {
            Log.e("OTALayout", "Firmware update failed: $error")
            onError(error)
            return@setOtaUpdateAsync
        }

        status?.let {
            Log.d("OTALayout", " Firmware update is  $it .")
            onSuccess(it.toString())
        }
    }
}