package com.outdu.camconnect.ui.layouts

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.outdu.camconnect.R
import com.outdu.camconnect.Viewmodels.AppViewModel
import com.outdu.camconnect.communication.CameraConfigurationManager
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.components.camera.*
import com.outdu.camconnect.ui.components.controls.*
import com.outdu.camconnect.ui.components.settings.*
import com.outdu.camconnect.ui.layouts.streamer.ZoomableVideoTextureView
import com.outdu.camconnect.ui.models.*
import com.outdu.camconnect.ui.theme.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outdu.camconnect.communication.CameraWebSocketManager
import com.outdu.camconnect.ui.viewmodels.CameraControlViewModel
import android.util.Log
import com.outdu.camconnect.ui.theme.camConnectIsDarkTheme
import com.outdu.camconnect.Viewmodels.CameraLayoutViewModel
import com.outdu.camconnect.OverlayPoints
import kotlinx.coroutines.delay
import androidx.compose.ui.input.pointer.pointerInput
import org.json.JSONObject

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

@Composable
private fun WebSocketMessagesOverlay(
    modifier: Modifier = Modifier,
    maxLines: Int = 50
) {
    val wsState by CameraWebSocketManager.state.collectAsState()
    val lines = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        CameraWebSocketManager.messages.collect { msg ->
            lines.add(msg)
            if (lines.size > maxLines) {
                // drop oldest
                val overflow = lines.size - maxLines
                repeat(overflow) { if (lines.isNotEmpty()) lines.removeAt(0) }
            }
        }
    }

    // Simple overlay on the stream pane (top-left)
    Box(
        modifier = modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(10.dp)
            .widthIn(max = 420.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = when (val s = wsState) {
                    is CameraWebSocketManager.ConnectionState.Connected -> "WS: connected"
                    is CameraWebSocketManager.ConnectionState.Connecting -> "WS: connecting (attempt ${s.attempt})"
                    is CameraWebSocketManager.ConnectionState.Disconnected -> "WS: disconnected"
                    is CameraWebSocketManager.ConnectionState.Error -> "WS: error"
                    CameraWebSocketManager.ConnectionState.Idle -> "WS: idle"
                },
                color = Color.White,
            )

            if (lines.isEmpty()) {
                Text(
                    text = "No messages yet",
                    color = Color.White.copy(alpha = 0.75f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 180.dp),
                    reverseLayout = true
                ) {
                    // show newest at bottom (reverseLayout = true)
                    itemsIndexed(lines) { _, item ->
                        Text(
                            text = item,
                            color = Color.White,
                            maxLines = 2
                        )
                    }
                }
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
    val darkTheme = camConnectIsDarkTheme()
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
    val detectionSettings by remember { mutableStateOf(DetectionSettings()) }

    // Settings tab state - persists across theme changes
    var selectedTab by rememberSaveable { mutableStateOf(ControlTab.CAMERA_CONTROL) }

    // Persistent button states for expanded control - survives layout mode changes
    val buttonStates = remember { mutableStateMapOf<String, Boolean>() }

    // Observe stream reloading state (needed for timer logic)
    val isStreamReloading = cameraLayoutViewModel.isStreamReloading.collectAsState()

    // Auto-hide controls state
    var isControlsHidden by remember { mutableStateOf(false) }
    // When true, controls were hidden intentionally by the user (hide FAB) and should not auto-reveal on activity.
    var isControlsManuallyHidden by remember { mutableStateOf(false) }
    var lastActivityTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val applicableModes = remember {
        setOf(
            LayoutMode.MINIMAL_CONTROL,
            LayoutMode.EXPANDED_CONTROL,
            LayoutMode.FULL_CONTROL
        )
    }
    
    // Activity tracking callback - resets timer on any user interaction
    val onUserActivity = {
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastActivityTime
        Log.d("AutoHide", "Activity detected - LayoutMode: $layoutMode, Elapsed since last: ${elapsed}ms, ControlsHidden: $isControlsHidden")
        lastActivityTime = currentTime
        // Only auto-reveal controls if they were auto-hidden due to inactivity.
        // If the user explicitly hid them via the hide FAB, keep them hidden until explicit reveal.
        if (isControlsHidden && !isControlsManuallyHidden) {
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
            isControlsManuallyHidden = false
            return@LaunchedEffect
        }
        
        if (layoutMode !in applicableModes) {
            Log.d("AutoHide", "Not in applicable mode ($layoutMode) - disabling auto-hide")
            isControlsHidden = false
            isControlsManuallyHidden = false
            return@LaunchedEffect
        }
        
        // Get timeout duration based on layout mode
        val timeoutDuration = when (layoutMode) {
            LayoutMode.MINIMAL_CONTROL -> 10000L // 10 seconds
            LayoutMode.EXPANDED_CONTROL -> 10000L // 10 seconds
            LayoutMode.FULL_CONTROL -> 20000L // 20 seconds
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
                isControlsManuallyHidden = false
                return@LaunchedEffect
            }
            
            // Get timeout duration for current mode
            val currentTimeoutDuration = when (currentLayoutMode) {
                LayoutMode.MINIMAL_CONTROL -> 10000L // 10 seconds
                LayoutMode.EXPANDED_CONTROL -> 10000L // 10 seconds
                LayoutMode.FULL_CONTROL -> 20000L // 20 seconds
            }
            
            val elapsed = System.currentTimeMillis() - lastActivityTime
            if (elapsed >= currentTimeoutDuration) {
                if (!isControlsHidden && currentLayoutMode in applicableModes) {
                    Log.d("AutoHide", "${currentTimeoutDuration}ms elapsed - HIDING controls. Mode: $currentLayoutMode, Elapsed: ${elapsed}ms")
                    isControlsHidden = true
                    // Auto-hide (inactivity) should be auto-revealable.
                    isControlsManuallyHidden = false
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
        isControlsManuallyHidden = false
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

    // WebSocket-triggered reload: event-driven.
    LaunchedEffect(Unit) {
        CameraWebSocketManager.messages.collect { msg ->
            try {
                val obj = JSONObject(msg)
                val rawType = obj.optString("event_type", "")
                val eventType = rawType
                    .trim()
                    .lowercase()
                    .replace(' ', '_')

                when (eventType) {
                    "changing_misc" -> {
                        Log.i("AdaptiveStreamLayout", "WS event changing_misc -> start loading + stop stream")
                        // If provided, show "Changing A mode to B mode" under the spinner.
                        val oldMisc = obj.optString("old_misc", obj.optString("oldMisc", "")).toIntOrNull()
                        val newMisc = obj.optString("new_misc", obj.optString("newMisc", "")).toIntOrNull()
                        cameraLayoutViewModel.setWsChangingMisc(oldMisc = oldMisc, newMisc = newMisc)
                        cameraLayoutViewModel.beginStreamReload(reason = "ws:changing_misc")
                    }
                    "started_streaming" -> {
                        // In practice the camera may emit this slightly before the stream is actually ready.
                        // Delay clearing loading a bit to avoid resuming into a "hung" pipeline.
                        if (isStreamReloading.value) {
                            Log.i("AdaptiveStreamLayout", "WS event started_streaming -> stop loading + start stream (delayed)")
                            cameraLayoutViewModel.endStreamReload(delayMs = 3_000L, reason = "ws:started_streaming")
                        } else {
                            // If we didn't see a preceding changing_misc (or state got out of sync),
                            // pulse a short reload to force a clean restart.
                            Log.i("AdaptiveStreamLayout", "WS event started_streaming (no active reload) -> pulse restart")
                            cameraLayoutViewModel.triggerStreamReload(durationMs = 1_000L, reason = "ws:started_streaming_pulse")
                        }
                    }
                    else -> {
                        // Ignore unrelated events; keep log at debug to avoid noise.
                        Log.d("AdaptiveStreamLayout", "WS event ignored (event_type=$rawType): $msg")
                    }
                }
            } catch (e: Exception) {
                // Non-JSON or unexpected payload; ignore to avoid breaking stream.
                Log.d("AdaptiveStreamLayout", "WS message not JSON or missing event_type; ignored: $msg", e)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VeryDarkBackground)
            .clip(RoundedCornerShape(20.dp))
            // Track *any* pointer interaction, even if child composables consume it (scroll, click, etc.)
            // Using Final pass makes this much harder to miss than gesture detectors on the parent.
            .pointerInput(onUserActivity) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Final)
                        if (event.changes.any { it.pressed || it.previousPressed || it.positionChanged() }) {
                            onUserActivity()
                        }
                    }
                }
            }
    ) {
        // The TextureView will handle its own lifecycle based on appViewModel.isPlaying
        ZoomableVideoTextureView(
            viewModel = appViewModel,
            currentContext = context,
            pointState = pointState,
            onUserActivity = onUserActivity
        )
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

                // WebSocket live data overlay (left stream pane)
//                WebSocketMessagesOverlay(
//                    modifier = Modifier.align(Alignment.TopStart)
//                )

                // Add the corner mask overlay
                RoundedCornerMaskOverlay(
                    cornerRadius = 20.dp,
                    color = VeryDarkBackground // Match the border color
                )

                // Floating hide button - shown when controls are visible.
                // Placed inside the left stream pane so it doesn't overlay the controls pane.
                if (!isControlsHidden && layoutMode in applicableModes) {
                    FloatingHideButton(
                        onHide = {
                            Log.d("AutoHide", "Hide button clicked - hiding controls")
                            // Don't call onUserActivity() here, because that would immediately un-hide.
                            isControlsHidden = true
                            isControlsManuallyHidden = true
                            lastActivityTime = System.currentTimeMillis()
                        }
                    )
                }

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
        if (isControlsHidden && layoutMode in applicableModes) {
            Log.d("AutoHide", "Showing floating reveal button - Mode: $layoutMode")
            FloatingRevealButton(
                onReveal = {
                    Log.d("AutoHide", "Reveal button clicked - revealing controls")
                    isControlsHidden = false
                    isControlsManuallyHidden = false
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
                            },
                            onUserActivity = onUserActivity
                        )
                    }
                }
            }
        }
    }
} 