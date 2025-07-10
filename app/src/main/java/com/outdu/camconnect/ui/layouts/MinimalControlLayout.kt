package com.outdu.camconnect.ui.layouts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.runtime.DisposableEffect
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType


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
    
    val isServiceRunning by ScreenRecorderService
        .isServiceRunning
        .collectAsStateWithLifecycle()

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
        if (hasNotificationPermission && !isServiceRunning) {
            screenRecordLauncher.launch(
                (context.getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE) as android.media.projection.MediaProjectionManager)
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
        verticalArrangement = if(deviceType == DeviceType.TABLET) Arrangement.SpaceAround else Arrangement.spacedBy(8.dp),
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
                    iconPlaceholder = if (isServiceRunning) R.drawable.record_circle_line.toString() else R.drawable.record_icon.toString(),
                    color = if (isServiceRunning) RecordRed else RecordRed,
                    text = if (isServiceRunning) "Stop Recording" else "Start Recording",
                    backgroundColor = MediumDarkBackground,
                    BorderColor = ButtonBorderColor,
                    onClick = {
                        if (!hasNotificationPermission &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            if (isServiceRunning) {
                                Intent(
                                    context,
                                    ScreenRecorderService::class.java
                                ).also {
                                    it.action = ScreenRecorderService.ACTION_STOP
                                    ContextCompat.startForegroundService(context, it)
                                }
                            } else {
                                screenRecordLauncher.launch(
                                    (context.getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE) as android.media.projection.MediaProjectionManager)
                                        .createScreenCaptureIntent()
                                )
                            }
                        }
                    }
                ),
                isCompact = true,
                showText = false
            )

            CustomizableButton(
                config = customButtons.first { it.id == "picture-in-picture" }.copy(
                    BorderColor = ButtonBorderColor,
                    enabled = false,
                    color = Color(0xFF363636),
                    backgroundColor = Color(0xFF272727)
                ),
                isCompact = true,
                showText = false
            )

            CustomizableButton(
                config = customButtons.first { it.id == "ir-cut-filter" }.copy(
                    BorderColor = ButtonBorderColor,
                    backgroundColor = MediumDarkBackground
                ),
                isCompact = true,
                showText = false
            )

            CustomizableButton(
                config = customButtons.first {it.id == "ir"}.copy(
                    BorderColor = ButtonBorderColor,
                    backgroundColor = MediumDarkBackground
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