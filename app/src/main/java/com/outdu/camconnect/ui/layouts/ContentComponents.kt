package com.outdu.camconnect.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.components.buttons.CustomizableButton
import com.outdu.camconnect.ui.components.camera.*
import com.outdu.camconnect.ui.components.controls.*
import com.outdu.camconnect.ui.components.indicators.*
import com.outdu.camconnect.ui.components.settings.*
import com.outdu.camconnect.ui.models.*

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
                    iconPlaceholder = R.drawable.settings_line.toString(),
                    text = "Settings",
                    backgroundColor = Color(0xFF333333),
                    onClick = onSettingsClick
                ),
                isCompact = true,
                showText = false
            )

            CustomizableButton(
                config = ButtonConfig(
                    id = "camera_switch",
                    iconPlaceholder = R.drawable.expand_line.toString(),
                    text = "Camera",
                    backgroundColor = Color(0xFF333333),
                    onClick = onExpandClick
                ),
                isCompact = true,
                showText = false
            )

            CustomizableButton(
                config = ButtonConfig(
                    id = "RecordingToggle",
                    iconPlaceholder = R.drawable.record_circle_line.toString(),
                    text = "Recording",
                    backgroundColor = Color(0xFF333333),
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

/**
 * Expanded control content - scrollable with multiple control sections
 */
@Composable
fun ExpandedControlContent(
    cameraState: CameraState,
    systemStatus: SystemStatus,
    customButtons: List<ButtonConfig>,
    toggleableIcons: List<ToggleableIcon>,
    onSettingsClick: () -> Unit,
    onCameraSwitch: () -> Unit,
    onRecordingToggle: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onIconToggle: (String) -> Unit,
    onCollapseClick: () -> Unit
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
            // Row 1: Customizable 5-button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                customButtons.take(5).forEach { buttonConfig ->
                    CustomizableButton(
                        config = if (buttonConfig.id == "Settings") {
                            buttonConfig.copy(onClick = onSettingsClick)

                        } else if (buttonConfig.id == "collapse-screen") {

                            buttonConfig.copy(onClick = onCollapseClick)
                        } else {
                            buttonConfig
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
                            if (cameraState.isRecording) Color(0xFFF43823) else Color(0xFF333333)
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
                                    if (cameraState.isRecording) Color.White else Color(0xFFDC2626)
                                )
                        )
                        // Record text
                        Text(
                            text = "RECORD",
                            color = if (cameraState.isRecording) Color.White else Color(0xFF9CA3AF),
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
                        .background(Color(0xFF374151))
                        .clickable { /* Handle zoom click */ }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${cameraState.zoomLevel.toInt()}X",
                        color = Color.White,
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
                    .background(Color(0xFF222222))
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
//                CompassIndicator(
//                    direction = systemStatus.compassDirection,
//                    size = 100.dp
//                )
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
                    hasSnapshot = false
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

        // Bottom bar with settings and collapse button
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .shadow(4.dp)
//                .background(Color(0xFFE0E0E0))
//                .padding(horizontal = 16.dp, vertical = 8.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Settings button
//                Row(
//                    modifier = Modifier
//                        .clickable { onSettingsClick() }
//                        .padding(8.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    // Settings icon placeholder
//                    Box(
//                        modifier = Modifier
//                            .size(20.dp)
//                            .clip(RoundedCornerShape(4.dp))
//                            .background(Color.Gray)
//                    )
//                    // "Settings" text placeholder
//                    Box(
//                        modifier = Modifier
//                            .height(14.dp)
//                            .width(50.dp)
//                            .background(Color.Black.copy(alpha = 0.2f))
//                    )
//                }
//
//                // Collapse button
//                Box(
//                    modifier = Modifier
//                        .size(48.dp)
//                        .clip(RoundedCornerShape(24.dp))
//                        .clickable { onCollapseClick() }
//                        .padding(12.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    // Chevron right placeholder
//                    Box(
//                        modifier = Modifier
//                            .size(24.dp)
//                            .background(Color.Gray, RoundedCornerShape(4.dp))
//                    )
//                }
//            }
//        }
    }
}

/**
 * Full control content - comprehensive settings interface
 */
@Composable
fun FullControlContent(
    cameraState: CameraState,
    systemStatus: SystemStatus,
    detectionSettings: DetectionSettings,
    customButtons: List<ButtonConfig>,
    selectedTab: ControlTab,
    onTabSelected: (ControlTab) -> Unit,
    onAutoDayNightToggle: (Boolean) -> Unit,
    onVisionModeSelected: (VisionMode) -> Unit,
    onObjectDetectionToggle: (Boolean) -> Unit,
    onFarObjectDetectionToggle: (Boolean) -> Unit,
    onMotionDetectionToggle: (Boolean) -> Unit,
    onCameraModeSelected: (CameraMode) -> Unit,
    onOrientationModeSelected: (OrientationMode) -> Unit,
    onCollapseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Row 1: Customizable 5-button row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            customButtons.take(5).forEach { buttonConfig ->
                if (buttonConfig.id != "Settings") {

                    CustomizableButton(
                        config =
                            if (buttonConfig.id == "collapse-screen") {
                                buttonConfig.copy(onClick = onCollapseClick)
                            } else {
                                buttonConfig
                            },
                        modifier = Modifier.weight(1f),
                        isCompact = false,
                        showText = false
                    )
                }

            }
        }

        // Row 2: Tab switcher
        ControlTabSwitcher(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.fillMaxWidth()
        )

        // Tab content
        when (selectedTab) {
            ControlTab.CAMERA_CONTROL -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Row 3: Display Settings
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0E0E0))
                            .padding(16.dp)
                    ) {
                        DisplaySettingsSection(
                            isAutoDayNightEnabled = cameraState.isAutoDayNightEnabled,
                            onAutoDayNightToggle = onAutoDayNightToggle,
                            selectedVisionMode = cameraState.visionMode,
                            onVisionModeSelected = onVisionModeSelected
                        )
                    }

                    // Row 4: Detection Settings
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0E0E0))
                            .padding(16.dp)
                    ) {
                        DetectionSettingsSection(
                            isObjectDetectionEnabled = detectionSettings.isObjectDetectionEnabled,
                            onObjectDetectionToggle = onObjectDetectionToggle,
                            isFarObjectDetectionEnabled = detectionSettings.isFarObjectDetectionEnabled,
                            onFarObjectDetectionToggle = onFarObjectDetectionToggle,
                            isMotionDetectionEnabled = detectionSettings.isMotionDetectionEnabled,
                            onMotionDetectionToggle = onMotionDetectionToggle
                        )
                    }

                    // Row 5: Image Settings
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0E0E0))
                            .padding(16.dp)
                    ) {
                        ImageSettingsSection(
                            selectedCameraMode = cameraState.cameraMode,
                            onCameraModeSelected = onCameraModeSelected,
                            selectedOrientationMode = cameraState.orientationMode,
                            onOrientationModeSelected = onOrientationModeSelected
                        )
                    }
                }
            }

            ControlTab.DEVICE_CONTROL -> {
                // Device control content placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Device icon placeholder
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // "Device Control" text placeholder
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .width(120.dp)
                                .background(Color.Black.copy(alpha = 0.3f))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Description text placeholder
                        Box(
                            modifier = Modifier
                                .height(16.dp)
                                .width(200.dp)
                                .background(Color.Gray.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }
    }
} 