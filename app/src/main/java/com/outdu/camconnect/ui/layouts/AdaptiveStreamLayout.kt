package com.outdu.camconnect.ui.layouts

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.components.buttons.CustomizableButton
import com.outdu.camconnect.ui.components.camera.*
import com.outdu.camconnect.ui.components.controls.*
import com.outdu.camconnect.ui.components.indicators.*
import com.outdu.camconnect.ui.components.settings.*
import com.outdu.camconnect.ui.models.*

/**
 * Main adaptive layout container with animated individual components
 * Maintains consistent structure while animating individual elements
 */
@Composable
fun AdaptiveStreamLayout(
    modifier: Modifier = Modifier,
    context: Context
) {
    // Layout state
    var layoutMode by remember { mutableStateOf(LayoutMode.MINIMAL_CONTROL) }
    
    // Camera state
    var cameraState by remember { mutableStateOf(CameraState()) }
    
    // System status
    var systemStatus by remember { 
        mutableStateOf(
            SystemStatus(
                batteryLevel = 75,
                isWifiConnected = true,
                isLteConnected = false,
                isOnline = true,
                isAiEnabled = true,
                currentSpeed = 45.5f,
                compassDirection = 127f
            )
        )
    }
    
    // Detection settings
    var detectionSettings by remember { mutableStateOf(DetectionSettings()) }
    
    // Settings tab state
    var selectedTab by remember { mutableStateOf(ControlTab.CAMERA_CONTROL) }
    
    // Animated weights for the two panes
    val leftPaneWeight by animateFloatAsState(
        targetValue = when (layoutMode) {
            LayoutMode.MINIMAL_CONTROL -> 0.9f
            LayoutMode.EXPANDED_CONTROL -> 0.6f
            LayoutMode.FULL_CONTROL -> 0.45f
        },
        animationSpec = tween(durationMillis = 300),
        label = "left_pane_weight"
    )
    
    val rightPaneWeight by animateFloatAsState(
        targetValue = when (layoutMode) {
            LayoutMode.MINIMAL_CONTROL -> 0.1f
            LayoutMode.EXPANDED_CONTROL -> 0.4f
            LayoutMode.FULL_CONTROL -> 0.55f
        },
        animationSpec = tween(durationMillis = 300),
        label = "right_pane_weight"
    )

    // Custom buttons configuration
    val customButtons = remember {
        listOf(
            ButtonConfig(
                id = "picture-in-picture",
                iconPlaceholder = R.drawable.picture_in_picture_line.toString(),
                text = "Picture-in-Picture",
                backgroundColor = Color(0xFF333333),
                onClick = { /* Handle snapshot */ }
            ),
            ButtonConfig(
                id = "collapse-screen",
                iconPlaceholder = R.drawable.expand_line.toString(),
                text = "Collapse Screen",
                backgroundColor = Color(0xFF333333),
                onClick = { /* Handle gallery */ }
            ),
            ButtonConfig(
                id = "ir",
                iconPlaceholder = R.drawable.ir_line.toString(),
                text = "IR",
                backgroundColor = Color(0xFF333333),
                onClick = { /* Handle share */ }
            ),
            ButtonConfig(
                id = "ir-cut-filter",
                iconPlaceholder = R.drawable.ircut_filter_line.toString(),
                text = "IR-Cut-Filter",
                backgroundColor = Color(0xFF333333),
                onClick = { /* Handle night mode */ }
            ),
            ButtonConfig(
                id = "Settings",
                iconPlaceholder = R.drawable.settings_line.toString(),
                text = "Settings",
                backgroundColor = Color(0xFF333333),
                onClick = { /* Handle flash */ }
            )
        )
    }
    
    // Toggleable icons for Layout 2
    val toggleableIcons = remember {
        mutableStateListOf(
            ToggleableIcon("hdr", R.drawable.hd_line.toString(), "Hdr", true, colorOnSelect = Color.White),
            ToggleableIcon("stabilize", R.drawable.git_commit_line.toString(), "Stabilize", false, colorOnSelect = Color.White),
            ToggleableIcon("timer", R.drawable.spy_line.toString(), "Timer", true, colorOnSelect = Color(0xFF0C59E0)),
            ToggleableIcon("dayNight", R.drawable.eye_2_line.toString(), "DayNight", false, colorOnSelect = Color.White),
            ToggleableIcon("visible", R.drawable.eye_line.toString(), "Visible", true, colorOnSelect = Color.White),
            ToggleableIcon("ir", R.drawable.router_line.toString(), "Ir", true, colorOnSelect = Color(0xFFF43823))
        )
    }
    
    // Main consistent layout structure
    Row(
        modifier = modifier.fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        // Left Pane - Camera Stream (animated width)
        Box(
            modifier = Modifier
                .weight(leftPaneWeight)
                .fillMaxHeight()
        ) {
            CameraStreamView(
                modifier = Modifier.fillMaxSize(),
                isConnected = systemStatus.isOnline,
                cameraName = "Camera ${cameraState.currentCamera + 1}",
                context = context
            )
        }
        
        HorizontalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .width(8.dp)
                .background(Color(0xFF0D0D0D))
        )

        // Right Pane - Controls (animated width and content)
        AnimatedRightPane(
            layoutMode = layoutMode,
            paneWeight = rightPaneWeight,
            cameraState = cameraState,
            systemStatus = systemStatus,
            detectionSettings = detectionSettings,
            customButtons = customButtons,
            toggleableIcons = toggleableIcons,
            selectedTab = selectedTab,
            onLayoutModeChange = { layoutMode = it },
            onCameraSwitch = { 
                cameraState = cameraState.copy(
                    currentCamera = (cameraState.currentCamera + 1) % 3
                )
            },
            onRecordingToggle = {
                cameraState = cameraState.copy(
                    isRecording = !cameraState.isRecording
                )
            },
            onZoomChange = { zoom ->
                cameraState = cameraState.copy(zoomLevel = zoom)
            },
            onTabSelected = { selectedTab = it },
            onAutoDayNightToggle = { enabled ->
                cameraState = cameraState.copy(isAutoDayNightEnabled = enabled)
            },
            onVisionModeSelected = { mode ->
                cameraState = cameraState.copy(visionMode = mode)
            },
            onObjectDetectionToggle = { enabled ->
                detectionSettings = detectionSettings.copy(
                    isObjectDetectionEnabled = enabled
                )
            },
            onFarObjectDetectionToggle = { enabled ->
                detectionSettings = detectionSettings.copy(
                    isFarObjectDetectionEnabled = enabled
                )
            },
            onMotionDetectionToggle = { enabled ->
                detectionSettings = detectionSettings.copy(
                    isMotionDetectionEnabled = enabled
                )
            },
            onCameraModeSelected = { mode ->
                cameraState = cameraState.copy(cameraMode = mode)
            },
            onOrientationModeSelected = { mode ->
                cameraState = cameraState.copy(orientationMode = mode)
            },
            onIconToggle = { iconId ->
                val index = toggleableIcons.indexOfFirst { it.id == iconId }
                if (index != -1) {
                    toggleableIcons[index] = toggleableIcons[index].copy(
                        isSelected = !toggleableIcons[index].isSelected
                    )
                }
            }
        )
    }
}

/**
 * Animated right pane that changes content based on layout mode
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedRightPane(
    layoutMode: LayoutMode,
    paneWeight: Float,
    cameraState: CameraState,
    systemStatus: SystemStatus,
    detectionSettings: DetectionSettings,
    customButtons: List<ButtonConfig>,
    toggleableIcons: List<ToggleableIcon>,
    selectedTab: ControlTab,
    onLayoutModeChange: (LayoutMode) -> Unit,
    onCameraSwitch: () -> Unit,
    onRecordingToggle: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onTabSelected: (ControlTab) -> Unit,
    onAutoDayNightToggle: (Boolean) -> Unit,
    onVisionModeSelected: (VisionMode) -> Unit,
    onObjectDetectionToggle: (Boolean) -> Unit,
    onFarObjectDetectionToggle: (Boolean) -> Unit,
    onMotionDetectionToggle: (Boolean) -> Unit,
    onCameraModeSelected: (CameraMode) -> Unit,
    onOrientationModeSelected: (OrientationMode) -> Unit,
    onIconToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(paneWeight)
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF222222))
    ) {
        // Top bar with settings button (always visible)
        AnimatedVisibility(
            visible = layoutMode == LayoutMode.FULL_CONTROL,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp)
                    .background(Color(0xFF222222))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onLayoutModeChange(LayoutMode.EXPANDED_CONTROL) }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                    // "Settings" text placeholder
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .width(80.dp)
                            .padding(start = 8.dp)
                            .background(Color(0xFF222222).copy(alpha = 0.8f))
                    )
                    {
                        Text(
                            text = "Settings",
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
        
        // Main content area with animated content
        AnimatedContent(
            targetState = layoutMode,
            transitionSpec = {
                slideInVertically { height -> height } + fadeIn() with
                slideOutVertically { height -> -height } + fadeOut()
            },
            modifier = Modifier.weight(1f),
            label = "content_transition"
        ) { mode ->
            when (mode) {
                LayoutMode.MINIMAL_CONTROL -> {
                    MinimalControlContent(
                        cameraState = cameraState,
                        systemStatus = systemStatus,
                        onSettingsClick = { onLayoutModeChange(LayoutMode.FULL_CONTROL) },
                        onCameraSwitch = onCameraSwitch,
                        onRecordingToggle = onRecordingToggle,
                        onExpandClick = { onLayoutModeChange(LayoutMode.EXPANDED_CONTROL) }
                    )
                }
                
                LayoutMode.EXPANDED_CONTROL -> {
                    ExpandedControlContent(
                        cameraState = cameraState,
                        systemStatus = systemStatus,
                        customButtons = customButtons,
                        toggleableIcons = toggleableIcons,
                        onSettingsClick = { onLayoutModeChange(LayoutMode.FULL_CONTROL) },
                        onCameraSwitch = onCameraSwitch,
                        onRecordingToggle = onRecordingToggle,
                        onZoomChange = onZoomChange,
                        onIconToggle = onIconToggle,
                        onCollapseClick = { onLayoutModeChange(LayoutMode.MINIMAL_CONTROL) }
                    )
                }
                
                LayoutMode.FULL_CONTROL -> {
//                    FullControlContent(
//                        cameraState = cameraState,
//                        systemStatus = systemStatus,
//                        detectionSettings = detectionSettings,
//                        customButtons = customButtons,
//                        selectedTab = selectedTab,
//                        onTabSelected = onTabSelected,
//                        onAutoDayNightToggle = onAutoDayNightToggle,
//                        onVisionModeSelected = onVisionModeSelected,
//                        onObjectDetectionToggle = onObjectDetectionToggle,
//                        onFarObjectDetectionToggle = onFarObjectDetectionToggle,
//                        onMotionDetectionToggle = onMotionDetectionToggle,
//                        onCameraModeSelected = onCameraModeSelected,
//                        onOrientationModeSelected = onOrientationModeSelected,
//                        onCollapseClick = {onLayoutModeChange(LayoutMode.MINIMAL_CONTROL)}
//                    )
                    CameraControlScreen(viewModel = CameraControlViewModel())
                }
            }
        }
    }
} 