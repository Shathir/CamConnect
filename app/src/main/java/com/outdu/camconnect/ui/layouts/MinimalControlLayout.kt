package com.outdu.camconnect.ui.layouts

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.outdu.camconnect.ui.components.camera.CameraStreamView
import com.outdu.camconnect.ui.models.CameraState
import com.outdu.camconnect.ui.models.SystemStatus
import com.outdu.camconnect.ui.models.VisionMode
import com.outdu.camconnect.R
import com.outdu.camconnect.services.RecordConfig
import com.outdu.camconnect.services.ScreenRecorderService
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.components.buttons.CustomizableButton
import com.outdu.camconnect.ui.components.indicators.AiStatusIndicator
import com.outdu.camconnect.ui.components.indicators.BatteryIndicator
import com.outdu.camconnect.ui.components.indicators.CompactSpeedIndicator
import com.outdu.camconnect.ui.components.indicators.WifiIndicator
import com.outdu.camconnect.ui.theme.*
import com.outdu.camconnect.ui.theme.AppColors.ButtonBorderColor
import com.outdu.camconnect.utils.MemoryManager
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType
import com.outdu.camconnect.ui.viewmodels.RecordingViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outdu.camconnect.ui.theme.AppColors.ButtonBgColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonIconColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonSelectedBgColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonSelectedIconColor
import com.outdu.camconnect.ui.viewmodels.CameraControlViewModel
import com.outdu.camconnect.ui.components.recording.RecordingTimer
import com.outdu.camconnect.Viewmodels.CameraLayoutViewModel


/**
 * Minimal control content - vertical layout with essential controls
 */
@Composable
fun MinimalControlContent(
    cameraState: CameraState,
    customButtons: List<ButtonConfig>,
    systemStatus: SystemStatus,
    onSettingsClick: () -> Unit,
    onCameraSwitch: () -> Unit,
    onRecordingToggle: () -> Unit,
    onExpandClick: () -> Unit
) {
    val context = LocalContext.current
    val deviceType = rememberDeviceType()
    val recordingViewModel: RecordingViewModel = viewModel()
    val cameraControlViewModel: CameraControlViewModel = viewModel()
    val cameraLayoutViewModel: CameraLayoutViewModel = viewModel()
    
    val isRecording by recordingViewModel.isRecording.collectAsStateWithLifecycle()
    val cameraControlState by cameraControlViewModel.cameraControlState.collectAsStateWithLifecycle()
    val isDarkTheme = isSystemInDarkTheme()

    var hasNotificationPermission by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        } else mutableStateOf(true)
    }

    val screenRecordLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intent = result.data ?: return@rememberLauncherForActivityResult
        val config = RecordConfig(
            resultCode = result.resultCode,
            data = intent
        )

        val serviceIntent = Intent(
            context,
            ScreenRecorderService::class.java
        ).apply {
            action = ScreenRecorderService.ACTION_START
            putExtra(ScreenRecorderService.RECORD_CONFIG, config)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (hasNotificationPermission && !isRecording) {
            screenRecordLauncher.launch(
                (context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
                    .createScreenCaptureIntent()
            )
        }
    }

    // Cleanup when component is disposed
    DisposableEffect(Unit) {
        Log.d("MinimalControlContent", "Component created")
        onDispose {
            Log.d("MinimalControlContent", "Component disposed - cleaning up")
            try {
                MemoryManager.cleanupWeakReferences()
            } catch (e: Exception) {
                Log.e("MinimalControlContent", "Error during cleanup", e)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
//        verticalArrangement = if(deviceType == DeviceType.TABLET) Arrangement.SpaceAround else Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top controls
            // Settings button
            CustomizableButton(
                config = ButtonConfig(
                    id = "settings",
                    iconPlaceholder = R.drawable.sliders_horizontal.toString(),
                    color = MediumGray,
                    text = "Settings",
                    BorderColor = ButtonBorderColor,
                    backgroundColor = MediumDarkBackground,
                    onClick = onSettingsClick
                ),
                isCompact = true,
                showText = false
            )

            CustomizableButton(
                config = ButtonConfig(
                    id = "camera_switch",
                    iconPlaceholder = R.drawable.expand_line.toString(),
                    color = Color.White,
                    text = "Camera",
                    BorderColor = DefaultColors.DarkGray,
                    backgroundColor = DefaultColors.DarkGray,
                    onClick = onExpandClick
                ),
                isCompact = true,
                showText = false
            )

            // Screen recording button
            CustomizableButton(
                config = ButtonConfig(
                    id = "screen-record",
                    iconPlaceholder = if (isRecording) R.drawable.record_circle_line.toString() else R.drawable.record_icon.toString(),
                    color = if (isRecording) RecordRed else RecordRed,
                    text = if (isRecording) "Stop Recording" else "Start Recording",
                    backgroundColor = MediumDarkBackground,
                    BorderColor = ButtonBorderColor,
                    onClick = { recordingViewModel.toggleRecording(context) }
                ),
                isCompact = true,
                showText = false
            )

//            CustomizableButton(
//                config = customButtons.first { it.id == "picture-in-picture" }.copy(
//                    BorderColor = ButtonBorderColor,
//                    enabled = false,
//                    color = Color(0xFF363636),
//                    backgroundColor = Color(0xFF272727)
//                ),
//                isCompact = true,
//                showText = false
//            )

            CustomizableButton(
                config = ButtonConfig(
                    id = "ir",
                    iconPlaceholder = R.drawable.ir_line.toString(),
                    color = if (cameraLayoutViewModel.currentVisionMode.value == VisionMode.VISION) Color(0xFF363636) 
                           else if (cameraControlState.isIrEnabled) {if(isDarkTheme) ButtonSelectedIconColor else Color.White} 
                           else ButtonIconColor,
                    text = "IR",
                    BorderColor = if (cameraControlState.isIrEnabled) RecordRed else ButtonBorderColor,
                    backgroundColor = if (cameraLayoutViewModel.currentVisionMode.value == VisionMode.VISION) Color(0xFF272727)
                                    else if (cameraControlState.isIrEnabled) RecordRed 
                                    else ButtonBgColor,
                    enabled = cameraLayoutViewModel.currentVisionMode.value != VisionMode.VISION,
                    onClick = { cameraControlViewModel.toggleIR() }
                ),
                isCompact = true,
                showText = false
            )

            CustomizableButton(
                config = ButtonConfig(
                    id = "ir-cut-filter",
                    iconPlaceholder = R.drawable.headlights.toString(),
                    color = if (cameraControlState.isIrEnabled) { if(!cameraControlState.isLowIntensity) ButtonSelectedIconColor else ButtonIconColor} else Color(0xFF363636),
                    text = "IR Intensity",
                    BorderColor = ButtonBorderColor,
                    backgroundColor = if(cameraControlState.isIrEnabled) {if (!cameraControlState.isLowIntensity) ButtonSelectedBgColor else ButtonBgColor} else {Color(0xFF272727)},
                    enabled = cameraControlState.isIrEnabled,
                    onClick = { cameraControlViewModel.toggleIrIntensity() }
                ),
                isCompact = true,
                showText = false
            )

            // WiFi indicator
            WifiIndicator(
                isConnected = systemStatus.isWifiConnected
            )

            // AI status
            AiStatusIndicator(
                isEnabled = systemStatus.isAiEnabled
            )

            // Battery indicator
            BatteryIndicator(
                batteryLevel = systemStatus.batteryLevel
            )
    }
}