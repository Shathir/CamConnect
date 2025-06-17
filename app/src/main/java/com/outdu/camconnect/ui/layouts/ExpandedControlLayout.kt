package com.outdu.camconnect.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.components.buttons.CustomizableButton
import com.outdu.camconnect.ui.components.camera.*
import com.outdu.camconnect.ui.components.controls.*
import com.outdu.camconnect.ui.components.indicators.*
import com.outdu.camconnect.ui.models.CameraState
import com.outdu.camconnect.ui.models.SystemStatus
import com.outdu.camconnect.ui.theme.*
import com.outdu.camconnect.ui.theme.AppColors.ButtonBgColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonBorderColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonIconColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonSelectedBgColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonSelectedIconColor

/**
 * Expanded control content - scrollable with multiple control sections
 */
@Composable
fun ExpandedControlContent(
    cameraState: CameraState,
    systemStatus: SystemStatus,
    customButtons: List<ButtonConfig>,
    toggleableIcons: List<ToggleableIcon>,
    buttonStates: MutableMap<String, Boolean>,
    onSettingsClick: () -> Unit,
    onCameraSwitch: () -> Unit,
    onRecordingToggle: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onIconToggle: (String) -> Unit,
    onCollapseClick: () -> Unit,
    onSpeedUpdate: (Float) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Row 1: Customizable 5-button row with state management
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                customButtons.take(5).forEach { buttonConfig ->
                    // Get current selection state for this button
                    val isSelected = buttonStates[buttonConfig.id] ?: false

                    CustomizableButton(
                        config = when (buttonConfig.id) {
                            "Settings" -> buttonConfig.copy(onClick = onSettingsClick,
                                BorderColor = ButtonBorderColor,
                                backgroundColor = if(isSelected) ButtonSelectedBgColor else ButtonBgColor,
                                color = if(isSelected) ButtonSelectedIconColor else ButtonIconColor
                            )
                            "collapse-screen" -> buttonConfig.copy(onClick = onCollapseClick,
                                BorderColor = ButtonBorderColor,
                                backgroundColor = if(isSelected) ButtonSelectedBgColor else ButtonBgColor,
                                color = if(isSelected) ButtonSelectedIconColor else ButtonIconColor
                            )
                            else -> {
                                // Create dynamic button config based on selection state
                                buttonConfig.copy(
                                    backgroundColor = if (isSelected) {
                                        // Active state - brighter background
                                        when (buttonConfig.id) {
                                            "ir" -> RecordRed
                                            else -> ButtonSelectedBgColor
                                        }
                                    } else {
                                        // Inactive state - default background
                                        ButtonBgColor
                                    },
                                    color = if (isSelected) ButtonSelectedIconColor else ButtonIconColor,
                                    BorderColor = if (isSelected) {
                                        // Active state - brighter background
                                        when (buttonConfig.id) {
                                            "ir" -> RecordRed
                                            else -> ButtonBorderColor
                                        }
                                    } else {
                                        // Inactive state - default background
                                        ButtonBorderColor
                                    },
                                    onClick = {
                                        // Toggle button state
                                        buttonStates[buttonConfig.id] = !isSelected
                                        // Call original onClick if needed
                                        buttonConfig.onClick()
                                    }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        isCompact = true,
                        showText = true,
                    )
                }
            }

            // Row 2: Recording toggle and Zoom selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recording button with dark theme
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .weight(2f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (cameraState.isRecording) RecordRed else MediumDarkBackground
                        )
                        .clickable { onRecordingToggle() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Record dot indicator
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (cameraState.isRecording) White else RedVariant
                                )
                        )
                        // Record text
                        Text(
                            text = "RECORD",
                            color = if (cameraState.isRecording) White else MediumLightGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Zoom selector with dark theme
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSlate)
                        .clickable { /* Handle zoom click */ }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${cameraState.zoomLevel.toInt()}X",
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Row 3: 6 toggleable icons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBackground2)
                    .padding(1.dp)

            ) {
                ToggleableIconRow(
                    icons = toggleableIcons,
                    onToggle = onIconToggle
                )
            }

            // Row 4: Compass component
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(Color(0xFFE0E0E0))
//                    .padding(16.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                )
//                {
//                toggleableIcons.forEachIndexed { index, icon ->
//
//                        DropInImage(
//                            imageRes = icon.iconPlaceholder,
//                            delayMillis = index * 300,
//                            imageSize = 40.dp
//                        )
//                    }
//                }
//            }

            // Row 5: Video feed and snapshot slots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
//                VideoFeedSlot(
//                    modifier = Modifier.weight(1f),
//                    label = "Rear Camera"
//                )
                SnapshotSlot(
                    modifier = Modifier.weight(1f),
                    hasSnapshot = false,
                    onSpeedUpdate = onSpeedUpdate
                )
            }

            // Row 6: Status information
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(Color(0xFFE0E0E0))
//                    .padding(12.dp)
//            ) {
////                Column(
////                    verticalArrangement = Arrangement.spacedBy(8.dp)
////                ) {
//                    // Speed indicator
////                    SpeedIndicator(
////                        speed = systemStatus.currentSpeed,
////                        modifier = Modifier.fillMaxWidth()
////                    )
//
//                    // Status indicators in a row
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceEvenly
//                    ) {
//                        SpeedIndicator(
//                            speed = systemStatus.currentSpeed,
////                            modifier = Modifier.fillMaxWidth()
//                        )
//                        BatteryIndicator(
//                            batteryLevel = systemStatus.batteryLevel,
//                            showPercentage = true
//                        )
//                        WifiIndicator(isConnected = systemStatus.isWifiConnected)
////                        OnlineIndicator(isOnline = systemStatus.isOnline)
//                        AiStatusIndicator(isEnabled = systemStatus.isAiEnabled)
//                    }
////                }
//            }

        }
    }
}
