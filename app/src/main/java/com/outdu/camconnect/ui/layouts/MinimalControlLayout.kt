package com.outdu.camconnect.ui.layouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.outdu.camconnect.ui.components.camera.CameraStreamView
import com.outdu.camconnect.ui.models.CameraState
import com.outdu.camconnect.ui.models.SystemStatus
import com.outdu.camconnect.R
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

/**
 * Minimal control content - vertical layout with essential controls
 */
@Composable
fun MinimalControlContent(
    cameraState: CameraState,
    systemStatus: SystemStatus,
    onSettingsClick: () -> Unit,
    onCameraSwitch: () -> Unit,
    onRecordingToggle: () -> Unit,
    onExpandClick: () -> Unit
) {
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
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top controls
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            CustomizableButton(
                config = ButtonConfig(
                    id = "RecordingToggle",
                    iconPlaceholder = R.drawable.record_icon.toString(),
                    color = RecordRed,
                    text = "Recording",
                    BorderColor = ButtonBorderColor,
                    backgroundColor = MediumDarkBackground,
                    onClick = onRecordingToggle
                ),
                isCompact = true,
                showText = false
            )

            // Compass
//            CompassIndicator(
//                direction = systemStatus.compassDirection,
//                size = 60.dp
//            )
        }

        // Bottom indicators
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Battery indicator
            BatteryIndicator(
                batteryLevel = systemStatus.batteryLevel,
                showPercentage = false
            )

            // Connectivity indicators
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WifiIndicator(isConnected = systemStatus.isWifiConnected)
//                LteIndicator(isConnected = systemStatus.isLteConnected)
            }

            // AI status
            AiStatusIndicator(
                isEnabled = systemStatus.isAiEnabled,
                modifier = Modifier.padding(vertical = 4.dp)
            )
//            CustomizableButton(
//                config = ButtonConfig(
//                    id = "AiStatusIndicator",
//                    iconPlaceholder = R.drawable.ai_line.toString(),
//                    text = "AiStatus",
//                    backgroundColor = Color(0xFF333333),
//                    onClick = {}
//                ),
//                isCompact = true,
//                showText = false
//            )

            // Speed indicator
            CompactSpeedIndicator(speed = systemStatus.currentSpeed)
        }

        // Expand button
//        ExpandButton(
//            isExpanded = false,
//            onClick = onExpandClick
//        )
    }
}