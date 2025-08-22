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
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import com.outdu.camconnect.ui.theme.AppColors.immersiveButtonBorderColor


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
                    BorderColor = immersiveButtonBorderColor,
                    backgroundColor = Color.White,
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
                    BorderColor = StravionBlue,
                    backgroundColor = StravionBlue,
                    onClick = onExpandClick
                ),
                isCompact = true,
                showText = false
            )

            // Screen recording button
            CustomizableButton(
                config = ButtonConfig(
                    id = "screen-record",
                    iconPlaceholder = if (isRecording) R.drawable.record_rectangle.toString() else R.drawable.record_icon.toString(),
                    color = if (isRecording) Color.White else RecordRed,
                    text = if (isRecording) "Stop Recording" else "Start Recording",
                    backgroundColor = if(isRecording) RecordRed  else Color.White,
                    BorderColor = if(isRecording) RecordRed else immersiveButtonBorderColor,
                    onClick = { recordingViewModel.toggleRecording(context) }
                ),
                isCompact = true,
                showText = false
            )

            CustomizableButton(
                config = customButtons.first { it.id == "picture-in-picture" }.copy(
                    id = "picture-in-picture",
                    iconPlaceholder = R.drawable.picture_in_picture_line.toString(),
                    color = Color(0xFFC5CBD4),
                    text = "Camera",
                    BorderColor = immersiveButtonBorderColor,
                    backgroundColor = immersiveButtonBorderColor,
                    enabled = false,
                    onClick = onExpandClick,
                ),
                isCompact = true,
                showText = false
            )

            CustomizableButton(
                config = ButtonConfig(
                    id = "ir",
                    iconPlaceholder = R.drawable.ir_line.toString(),
                    color = if (cameraLayoutViewModel.currentVisionMode.value != VisionMode.INFRARED) {
                        Color(0xFFC5CBD4)
                    } else {
                        when (cameraControlState.irIntensityLevel) {
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.OFF -> ButtonIconColor
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.LOW,
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.MEDIUM,
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.HIGH,
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.MAX,
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.ULTRA -> Color.White
                        }
                    },
                    text = "IR",
                    BorderColor = if (cameraLayoutViewModel.currentVisionMode.value != VisionMode.INFRARED) {
                        immersiveButtonBorderColor
                    } else {
                        when (cameraControlState.irIntensityLevel) {
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.OFF -> immersiveButtonBorderColor
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.LOW -> Color(0xFFFFA07D)
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.MEDIUM -> Color(0xFFF87646)
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.HIGH -> Color(0xFFF55114)
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.MAX -> Color(0xFFE63900)
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.ULTRA -> Color(0xFFE63900)
                        }
                    },
                    backgroundColor = if (cameraLayoutViewModel.currentVisionMode.value != VisionMode.INFRARED) {
                        immersiveButtonBorderColor
                    } else {
                        when (cameraControlState.irIntensityLevel) {
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.OFF -> Color.White
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.LOW -> Color(0xFFFFA07D)
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.MEDIUM -> Color(0xFFF87646)
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.HIGH -> Color(0xFFF55114)
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.MAX -> Color(0xFFE63900)
                            com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.ULTRA -> Color(0xFFE63900)
                        }
                    },
//                    enabled = cameraLayoutViewModel.currentVisionMode.value != VisionMode.VISION,
                    enabled = false,
                    onClick = { cameraControlViewModel.toggleIR() }
                ),
                isCompact = true,
                showText = false
            )

//            CustomizableButton(
//                config = ButtonConfig(
//                    id = "ir-cut-filter",
//                    iconPlaceholder = R.drawable.headlights.toString(),
//                    color = if (cameraControlState.isIrEnabled) { if(!cameraControlState.isLowIntensity) ButtonSelectedIconColor else ButtonIconColor} else Color(0xFF363636),
//                    text = "IR Intensity",
//                    BorderColor = ButtonBorderColor,
//                    backgroundColor = if(cameraControlState.isIrEnabled) {if (!cameraControlState.isLowIntensity) ButtonSelectedBgColor else ButtonBgColor} else {Color(0xFF272727)},
//                    enabled = cameraControlState.isIrEnabled,
//                    onClick = { cameraControlViewModel.toggleIrIntensity() }
//                ),
//                isCompact = true,
//                showText = false
//            )

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