package com.outdu.camconnect.ui.layouts

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.outdu.camconnect.R
import com.outdu.camconnect.Viewmodels.AppViewModel
import com.outdu.camconnect.communication.CameraConfigurationManager
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.components.buttons.CustomizableButton
import com.outdu.camconnect.ui.components.camera.*
import com.outdu.camconnect.ui.components.controls.*
import com.outdu.camconnect.ui.components.indicators.*
import com.outdu.camconnect.ui.components.settings.*
import com.outdu.camconnect.ui.layouts.streamer.ZoomableVideoTextureView
import com.outdu.camconnect.ui.models.*
import com.outdu.camconnect.ui.theme.*
import com.outdu.camconnect.ui.theme.AppColors.IconOnSelected
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outdu.camconnect.ui.viewmodels.CameraControlViewModel
import com.outdu.camconnect.singleton.MainActivitySingleton
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import com.outdu.camconnect.Viewmodels.CameraLayoutViewModel
import com.outdu.camconnect.OverlayPoints
import kotlinx.coroutines.delay
import androidx.compose.ui.input.pointer.pointerInput

@Composable
private fun LoadingOverlay(
    modifier: Modifier = Modifier
) {
    val rotation = rememberInfiniteTransition(label = "loading_rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_animation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(64.dp)
        ) {
            rotate(rotation.value) {
                drawArc(
                    color = Color.White,
                    startAngle = 0f,
                    sweepAngle = 300f,
                    useCenter = false,
                    style = Stroke(
                        width = 8f,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}

/**
 * Main adaptive layout container with animated individual components
 * Maintains consistent structure while animating individual elements
 */
@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AdaptiveStreamLayout(
    modifier: Modifier = Modifier,
    context: Context,
    pointState: MutableState<OverlayPoints>,
    onLogout: () -> Unit = {}
) {
    // Layout state - persists across theme changes and recompositions
    var layoutMode by rememberSaveable { mutableStateOf(LayoutMode.MINIMAL_CONTROL) }

    // Get reference to the AppViewModel and CameraControlViewModel
    val appViewModel: AppViewModel = viewModel()
    val cameraControlViewModel: CameraControlViewModel = viewModel()
    val cameraLayoutViewModel: CameraLayoutViewModel = viewModel()
    val darkTheme = isSystemInDarkTheme()
    // Add LaunchedEffect to refresh settings when layout mode changes
    LaunchedEffect(layoutMode) {
        when (layoutMode) {
            LayoutMode.MINIMAL_CONTROL, LayoutMode.EXPANDED_CONTROL -> {
                // Refresh camera control state
                cameraControlViewModel.refreshCameraState()
            }
            else -> {} // No refresh needed for FULL_CONTROL
        }
    }

    // Camera state
    var cameraState by remember { mutableStateOf(CameraState()) }

    // GPS Speed state (updated from LiveTrackingMap)
    var currentSpeed by remember { mutableStateOf(0f) }

    // System status
    var systemStatus by remember {
        mutableStateOf(
            SystemStatus(
                batteryLevel = 75,
                isWifiConnected = true,
                isLteConnected = false,
                isOnline = true,
                isAiEnabled = CameraConfigurationManager.isObjectDetectionEnabled(),
                currentSpeed = 0f, // Will be updated by GPS
                compassDirection = 127f
            )
        )
    }

    // Update systemStatus when GPS speed changes
    LaunchedEffect(currentSpeed) {
        systemStatus = systemStatus.copy(currentSpeed = currentSpeed)
    }

    // Detection settings
    var detectionSettings by remember { mutableStateOf(DetectionSettings()) }

    // Settings tab state - persists across theme changes
    var selectedTab by rememberSaveable { mutableStateOf(ControlTab.CAMERA_CONTROL) }

    // Persistent button states for expanded control - survives layout mode changes
    val buttonStates = remember { mutableStateMapOf<String, Boolean>() }

    // Observe stream reloading state (needed for timer logic)
    val isStreamReloading = cameraLayoutViewModel.isStreamReloading.collectAsState()

    // Auto-hide controls state
    var isControlsHidden by remember { mutableStateOf(false) }
    var lastActivityTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Activity tracking callback - resets timer on any user interaction
    val onUserActivity = {
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastActivityTime
        Log.d("AutoHide", "Activity detected - LayoutMode: $layoutMode, Elapsed since last: ${elapsed}ms, ControlsHidden: $isControlsHidden")
        lastActivityTime = currentTime
        if (isControlsHidden) {
            Log.d("AutoHide", "Revealing controls due to activity")
            isControlsHidden = false
        }
    }

    // Function to handle system status changes
    val onSystemStatusChange: (SystemStatus) -> Unit = { newStatus ->
        systemStatus = newStatus
        onUserActivity() // Track activity when settings change
    }
    
    // Timer logic: Hide controls after inactivity
    // MINIMAL_CONTROL and EXPANDED_CONTROL: 10 seconds
    // FULL_CONTROL: 20 seconds
    val isStreamReloadingState = isStreamReloading.value
    LaunchedEffect(lastActivityTime, layoutMode, isStreamReloadingState) {
        Log.d("AutoHide", "Timer LaunchedEffect triggered - LayoutMode: $layoutMode, StreamReloading: $isStreamReloadingState")
        
        // Don't auto-hide during stream reloading
        if (isStreamReloadingState) {
            Log.d("AutoHide", "Stream reloading - disabling auto-hide")
            isControlsHidden = false
            return@LaunchedEffect
        }
        
        // Only run timer for MINIMAL_CONTROL, EXPANDED_CONTROL, and FULL_CONTROL modes
        val applicableModes = listOf(
            LayoutMode.MINIMAL_CONTROL,
            LayoutMode.EXPANDED_CONTROL,
            LayoutMode.FULL_CONTROL
        )
        if (layoutMode !in applicableModes) {
            Log.d("AutoHide", "Not in applicable mode ($layoutMode) - disabling auto-hide")
            isControlsHidden = false
            return@LaunchedEffect
        }
        
        // Get timeout duration based on layout mode
        val timeoutDuration = when (layoutMode) {
            LayoutMode.MINIMAL_CONTROL -> 10000L // 10 seconds
            LayoutMode.EXPANDED_CONTROL -> 10000L // 10 seconds
            LayoutMode.FULL_CONTROL -> 20000L // 20 seconds
            else -> 10000L // Default to 10 seconds
        }
        
        Log.d("AutoHide", "Starting timer for mode: $layoutMode (timeout: ${timeoutDuration}ms)")
        
        while (true) {
            delay(1000) // Check every second
            // Re-check conditions in case they changed
            val currentLayoutMode = layoutMode
            val currentStreamReloading = isStreamReloading.value
            
            if (currentStreamReloading || currentLayoutMode !in applicableModes) {
                Log.d("AutoHide", "Conditions changed - stopping timer. Mode: $currentLayoutMode, Reloading: $currentStreamReloading")
                isControlsHidden = false
                return@LaunchedEffect
            }
            
            // Get timeout duration for current mode
            val currentTimeoutDuration = when (currentLayoutMode) {
                LayoutMode.MINIMAL_CONTROL -> 10000L // 10 seconds
                LayoutMode.EXPANDED_CONTROL -> 10000L // 10 seconds
                LayoutMode.FULL_CONTROL -> 20000L // 20 seconds
                else -> 10000L // Default to 10 seconds
            }
            
            val elapsed = System.currentTimeMillis() - lastActivityTime
            if (elapsed >= currentTimeoutDuration) {
                if (!isControlsHidden && currentLayoutMode in applicableModes) {
                    Log.d("AutoHide", "${currentTimeoutDuration}ms elapsed - HIDING controls. Mode: $currentLayoutMode, Elapsed: ${elapsed}ms")
                    isControlsHidden = true
                }
            } else {
                // Log every 5 seconds for debugging
                if (elapsed % 5000 < 1000) {
                    Log.d("AutoHide", "Timer check - Mode: $currentLayoutMode, Elapsed: ${elapsed}ms/${currentTimeoutDuration}ms, ControlsHidden: $isControlsHidden")
                }
            }
        }
    }
    
    // Reset timer when layout mode changes
    LaunchedEffect(layoutMode) {
        Log.d("AutoHide", "Layout mode changed to: $layoutMode")
        lastActivityTime = System.currentTimeMillis()
        // Reset controls visibility when switching modes - timer will handle auto-hide
        isControlsHidden = false
    }

    // Animated weights for the two panes
    val leftPaneWeight by animateFloatAsState(
        targetValue = when (layoutMode) {
            LayoutMode.MINIMAL_CONTROL -> 0.9f
            LayoutMode.EXPANDED_CONTROL -> 0.6f
//            LayoutMode.FULL_CONTROL -> 0.3f
            LayoutMode.FULL_CONTROL -> 0.6f
        },
        animationSpec = tween(durationMillis = 300),
        label = "left_pane_weight"
    )

    val rightPaneWeight by animateFloatAsState(
        targetValue = when {
            isControlsHidden -> 0f // Hide controls when auto-hidden
            layoutMode == LayoutMode.MINIMAL_CONTROL -> 0.1f
            layoutMode == LayoutMode.EXPANDED_CONTROL -> 0.4f
            layoutMode == LayoutMode.FULL_CONTROL -> 0.4f
            else -> 0.4f
        },
        animationSpec = tween(durationMillis = 300),
        label = "right_pane_weight"
    )
    
    val leftPaneWeightWhenHidden by animateFloatAsState(
        targetValue = if (isControlsHidden) 1f else leftPaneWeight,
        animationSpec = tween(durationMillis = 300),
        label = "left_pane_weight_hidden"
    )

    // Custom buttons configuration - Theme-aware colors applied outside remember
    val customButtons = remember {
        listOf(
            ButtonConfig(
                id = "picture-in-picture",
                iconPlaceholder = R.drawable.picture_in_picture_line.toString(),
                text = "PIP View",
                backgroundColor = Color.Transparent, // Will be overridden with theme-aware color
                onClick = { /* Handle snapshot */ }
            ),
            ButtonConfig(
                id = "collapse-screen",
                iconPlaceholder = R.drawable.expand_line.toString(),
                text = "Immersive View",
                backgroundColor = Color.Transparent, // Will be overridden with theme-aware color
                color = Color.Transparent,
                onClick = { /* Handle gallery */ }
            ),
            ButtonConfig(
                id = "ir",
                iconPlaceholder = R.drawable.ir_line.toString(),
                text = "IR",
                backgroundColor = Color.Transparent, // Will be overridden with theme-aware color
                onClick = { /* Handle share */ }
            ),
            ButtonConfig(
                id = "ir-cut-filter",
                iconPlaceholder = R.drawable.headlights.toString(),
                text = "High Beam",
                backgroundColor = Color.Transparent, // Will be overridden with theme-aware color
                onClick = { /* Handle night mode */ }
            ),
            ButtonConfig(
                id = "Settings",
                iconPlaceholder = R.drawable.sliders_horizontal.toString(),
                text = "Settings",
                backgroundColor = Color.Transparent, // Will be overridden with theme-aware color
                onClick = { /* Handle flash */ }
            )
        )
    }

    // Apply theme-aware colors to custom buttons
    val themedCustomButtons = customButtons.map { button ->
        button.copy(
            backgroundColor = MediumDarkBackground,
            color = MediumGray
        )
    }

    // Toggleable icons for Layout 2 - Basic structure without theme-aware colors
    val toggleableIcons = remember {
        mutableStateListOf(
            ToggleableIcon("viewmode", R.drawable.sun,"viewMode", true, colorOnSelect = DefaultColors.SpyBlue),
            ToggleableIcon("hdr", R.drawable.hd_line, description ="Hdr", true, colorOnSelect =  Color.White),
            ToggleableIcon("stabilize", R.drawable.git_commit_line, "Stabilize", true, colorOnSelect = Color.White),
        )
    }

    // Apply theme-aware colors to icons when they change
    LaunchedEffect(SpyBlue, RecordRed) {
        toggleableIcons.forEachIndexed { index, icon ->
            val themedIcon = when (icon.id) {
                "viewmode" -> icon.copy(colorOnSelect = if(darkTheme) DefaultColors.IconOnSelected else DefaultColors.SpyBlue)
                "timer" -> icon.copy(colorOnSelect = DefaultColors.SpyBlue)
                else -> icon.copy(colorOnSelect = if(darkTheme) DefaultColors.IconOnSelected else DefaultColors.SpyBlue)

            }
            if (toggleableIcons[index] != themedIcon) {
                toggleableIcons[index] = themedIcon
            }
        }
    }

    // Effect to handle stream lifecycle
    LaunchedEffect(isStreamReloading.value) {
        if (isStreamReloading.value) {
            // Stop and finalize the stream
            try {
//                MainActivitySingleton.nativePause()
//                MainActivitySingleton.nativeSurfaceFinalize()
                appViewModel.setPlaying(false)
            } catch (e: Exception) {
                Log.e("AdaptiveStreamLayout", "Error stopping stream", e)
            }
        } else {
            // Reinitialize and start the stream
            try {
                appViewModel.setPlaying(true)
            } catch (e: Exception) {
                Log.e("AdaptiveStreamLayout", "Error starting stream", e)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VeryDarkBackground)
            .clip(RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                // Detect all touch interactions to track user activity
                detectTapGestures(
                    onTap = { 
                        Log.d("AutoHide", "Tap gesture detected")
                        onUserActivity() 
                    },
                    onDoubleTap = { 
                        Log.d("AutoHide", "Double tap gesture detected")
                        onUserActivity() 
                    },
                    onLongPress = { 
                        Log.d("AutoHide", "Long press gesture detected")
                        onUserActivity() 
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        Log.d("AutoHide", "Drag start gesture detected")
                        onUserActivity() 
                    },
                    onDrag = { _, _ -> 
                        // Too verbose - only log occasionally
                        // Log.d("AutoHide", "Drag gesture detected")
                        onUserActivity() 
                    },
                    onDragEnd = { 
                        Log.d("AutoHide", "Drag end gesture detected")
                        onUserActivity() 
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures(
                    onGesture = { _, _, _, _ -> 
                        Log.d("AutoHide", "Transform gesture detected")
                        onUserActivity() 
                    }
                )
            }
    ) {
        // The TextureView will handle its own lifecycle based on appViewModel.isPlaying
        ZoomableVideoTextureView(viewModel = appViewModel, context, pointState = pointState)

        // Show loading overlay when stream is reloading
//        AnimatedVisibility(
//            visible = isStreamReloading.value,
//            enter = fadeIn(),
//            exit = fadeOut()
//        ) {
//            LoadingOverlay()
//        }

        // Main consistent layout structure
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Transparent)
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Left Pane - Camera Stream (animated width)
            Box(
                modifier = Modifier
                    .weight(leftPaneWeightWhenHidden)
                    .background(Color.Transparent)
                    .fillMaxHeight()
            ) {
                CameraStreamView(
                    modifier = Modifier
                        .fillMaxSize(),
                    isConnected = systemStatus.isOnline,
                    cameraName = "Camera ${cameraState.currentCamera + 1}",
                    context = context,
                    showTimer = layoutMode != LayoutMode.EXPANDED_CONTROL, // Only show timer in minimal and full layouts
                    showNotifications = layoutMode != LayoutMode.FULL_CONTROL,
                    onSpeedUpdate = { speed -> currentSpeed = speed }
                )

                // Add the corner mask overlay
                RoundedCornerMaskOverlay(
                    cornerRadius = 20.dp,
                    color = VeryDarkBackground // Match the border color
                )

            }

            // Divider between panes - hide when controls are hidden
            AnimatedVisibility(
                visible = !isControlsHidden,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally(),
                modifier = Modifier.fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .fillMaxHeight()
                        .background(VeryDarkBackground)
                )
            }

            // Right Pane - Controls (animated width and content)
            AnimatedVisibility(
                visible = !isControlsHidden,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                AnimatedRightPane(
                layoutMode = layoutMode,
                paneWeight = rightPaneWeight,
                cameraState = cameraState,
                systemStatus = systemStatus,
                detectionSettings = detectionSettings,
                customButtons = themedCustomButtons,
                toggleableIcons = toggleableIcons,
                selectedTab = selectedTab,
                buttonStates = buttonStates,
                onLayoutModeChange = { 
                    Log.d("AutoHide", "Layout mode change requested: $it")
                    layoutMode = it
                    onUserActivity() // Track activity
                },
                onCameraSwitch = {
                    cameraState = cameraState.copy(
                        currentCamera = (cameraState.currentCamera + 1) % 3
                    )
                    onUserActivity() // Track activity
                },
                onRecordingToggle = {
                    cameraState = cameraState.copy(
                        isRecording = !cameraState.isRecording
                    )
                    onUserActivity() // Track activity
                },
                onZoomChange = { zoom ->
                    cameraState = cameraState.copy(zoomLevel = zoom)
                    onUserActivity() // Track activity
                },
                onTabSelected = { 
                    Log.d("AutoHide", "Tab selected: $it")
                    selectedTab = it
                    onUserActivity() // Track activity
                },
                onIconToggle = { iconId ->
                    val index = toggleableIcons.indexOfFirst { it.id == iconId }
                    if (index != -1) {
                        toggleableIcons[index] = toggleableIcons[index].copy(
                            isSelected = !toggleableIcons[index].isSelected
                        )
                    }
                    onUserActivity() // Track activity
                },
                onSpeedUpdate = { speed -> 
                    currentSpeed = speed
                    onUserActivity() // Track activity on speed update
                },
                onSystemStatusChange = onSystemStatusChange,
                onLogout = onLogout,
                onUserActivity = onUserActivity
            )
            }
            
        }
        
        // Floating reveal button - shown when controls are hidden
        // Positioned as overlay on top of everything
        val applicableModes = listOf(
            LayoutMode.MINIMAL_CONTROL,
            LayoutMode.EXPANDED_CONTROL,
            LayoutMode.FULL_CONTROL
        )
        if (isControlsHidden && layoutMode in applicableModes) {
            Log.d("AutoHide", "Showing floating reveal button - Mode: $layoutMode")
            FloatingRevealButton(
                onReveal = {
                    Log.d("AutoHide", "Reveal button clicked - revealing controls")
                    isControlsHidden = false
                    lastActivityTime = System.currentTimeMillis()
                    // Optionally switch to minimal control when revealing from full control
                    if (layoutMode == LayoutMode.FULL_CONTROL) {
                        Log.d("AutoHide", "Switching from FULL_CONTROL to MINIMAL_CONTROL")
                        layoutMode = LayoutMode.MINIMAL_CONTROL
                    }
                }
            )
        }
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
    buttonStates: MutableMap<String, Boolean>,
    onLayoutModeChange: (LayoutMode) -> Unit,
    onCameraSwitch: () -> Unit,
    onRecordingToggle: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onTabSelected: (ControlTab) -> Unit,
    onIconToggle: (String) -> Unit,
    onSpeedUpdate: (Float) -> Unit,
    onSystemStatusChange: (SystemStatus) -> Unit,
    onLogout: () -> Unit,
    onUserActivity: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxWidth(paneWeight)
            .background(VeryDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkBackground2)
        ) {
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
                            customButtons = customButtons,
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
                            buttonStates = buttonStates,
                            onSettingsClick = { onLayoutModeChange(LayoutMode.FULL_CONTROL) },
                            onCameraSwitch = onCameraSwitch,
                            onRecordingToggle = onRecordingToggle,
                            onZoomChange = onZoomChange,
                            onIconToggle = onIconToggle,
                            onCollapseClick = { onLayoutModeChange(LayoutMode.MINIMAL_CONTROL) },
                            onSpeedUpdate = onSpeedUpdate
                        )
                    }

                    LayoutMode.FULL_CONTROL -> {
                        Log.d("AutoHide", "Rendering FULL_CONTROL layout")
                        SettingsControlLayout(
                            selectedTab = selectedTab,
                            onTabSelected = { 
                                Log.d("AutoHide", "Tab selected in FULL_CONTROL: $it")
                                onTabSelected(it)
                                onUserActivity() // Ensure activity is tracked
                            },
                            systemStatus = systemStatus,
                            onSystemStatusChange = { newStatus ->
                                Log.d("AutoHide", "System status changed in FULL_CONTROL")
                                onSystemStatusChange(newStatus)
                                // onSystemStatusChange already calls onUserActivity, but ensure it's called
                            },
                            onCollapseClick = { 
                                Log.d("AutoHide", "Collapse clicked in FULL_CONTROL")
                                onLayoutModeChange(LayoutMode.EXPANDED_CONTROL)
                                onUserActivity() // Track activity
                            },
                            onLogout = {
                                Log.d("AutoHide", "Logout clicked in FULL_CONTROL")
                                onLogout()
                                onUserActivity() // Track activity
                            }
                        )
                    }
                }
            }
        }
    }
} 