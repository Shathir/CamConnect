package com.outdu.camconnect.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.components.buttons.CustomizableButton
import com.outdu.camconnect.ui.components.camera.*
import com.outdu.camconnect.ui.components.controls.*
import com.outdu.camconnect.ui.components.indicators.*
import com.outdu.camconnect.ui.models.CameraState
import com.outdu.camconnect.ui.models.SystemStatus

/**
 * Layout 2: Expanded Control Panel (Interactive Controls)
 * Left Pane: 60% - Live camera stream display
 * Right Pane: 40% - Dynamic and scrollable control options
 */
@Composable
fun ExpandedControlLayout(
    cameraState: CameraState,
    systemStatus: SystemStatus,
    customButtons: List<ButtonConfig>,
    toggleableIcons: List<ToggleableIcon>,
    onSettingsClick: () -> Unit,
    onCameraSwitch: () -> Unit,
    onRecordingToggle: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onCustomButtonClick: (String) -> Unit,
    onIconToggle: (String) -> Unit,
    onCollapseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // Left Pane - Camera Stream (60%)
        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
        ) {
            CameraStreamView(
                modifier = Modifier.fillMaxSize(),
                isConnected = systemStatus.isOnline,
                cameraName = "Camera ${cameraState.currentCamera + 1}"
            )
        }
        
        // Right Pane - Expanded Controls (40%)
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .background(Color(0xFFF5F5F5)) // Light gray background
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
                            config = buttonConfig,
                            modifier = Modifier.weight(1f),
                            isCompact = true,
                            showText = true
                        )
                    }
                }
                
                // Row 2: Recording toggle and Zoom selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Recording toggle
                    RecordingToggle(
                        isRecording = cameraState.isRecording,
                        onToggle = onRecordingToggle,
                        modifier = Modifier.size(56.dp)
                    )
                    
                    // Scrollable zoom selector
                    ZoomSelector(
                        currentZoom = cameraState.zoomLevel,
                        onZoomSelected = onZoomChange,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Row 3: 6 toggleable icons
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0))
                        .padding(8.dp)
                ) {
                    ToggleableIconRow(
                        icons = toggleableIcons,
                        onToggle = onIconToggle
                    )
                }
                
                // Row 4: Compass component
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CompassIndicator(
                        direction = systemStatus.compassDirection,
                        size = 100.dp
                    )
                }
                
                // Row 5: Video feed and snapshot slots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VideoFeedSlot(
                        modifier = Modifier.weight(1f),
                        label = "Rear Camera"
                    )
                    SnapshotSlot(
                        modifier = Modifier.weight(1f),
                        hasSnapshot = false
                    )
                }
                
                // Row 6: Status information
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0))
                        .padding(12.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Speed indicator
                        SpeedIndicator(
                            speed = systemStatus.currentSpeed,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Status indicators in a row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            BatteryIndicator(
                                batteryLevel = systemStatus.batteryLevel,
                                showPercentage = true
                            )
                            WifiIndicator(isConnected = systemStatus.isWifiConnected)
                            OnlineIndicator(isOnline = systemStatus.isOnline)
                            AiStatusIndicator(isEnabled = systemStatus.isAiEnabled)
                        }
                    }
                }
            }
            
            // Bottom bar with settings and collapse button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp)
                    .background(Color(0xFFE0E0E0))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Settings button
                    Row(
                        modifier = Modifier
                            .clickable { onSettingsClick() }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Settings icon placeholder
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Gray)
                        )
                        // "Settings" text placeholder
                        Box(
                            modifier = Modifier
                                .height(14.dp)
                                .width(50.dp)
                                .background(Color.Black.copy(alpha = 0.2f))
                        )
                    }
                    
                    // Collapse button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { onCollapseClick() }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Chevron right placeholder
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.Gray, RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }
    }
} 