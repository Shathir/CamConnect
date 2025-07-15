package com.outdu.camconnect.ui.layouts

import android.bluetooth.BluetoothClass.Device
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
import com.outdu.camconnect.utils.MemoryManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.outdu.camconnect.R
import com.outdu.camconnect.communication.MotocamAPIAndroidHelper
import com.outdu.camconnect.communication.MotocamAPIHelper
import com.outdu.camconnect.communication.MotocamSocketClient
import com.outdu.camconnect.network.HttpClientProvider
import com.outdu.camconnect.ui.components.buttons.ScreenRecorderUI
import com.outdu.camconnect.ui.components.buttons.ZoomSelector
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.launch
import com.outdu.camconnect.ui.viewmodels.RecordingViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.scale
import com.outdu.camconnect.ui.models.RecordingState
import com.outdu.camconnect.ui.viewmodels.CameraControlViewModel
import com.outdu.camconnect.Viewmodels.CameraLayoutViewModel
import com.outdu.camconnect.ui.models.VisionMode


/**
 * Expanded control content - scrollable with multiple control sections
 */
//@Composable
//fun ExpandedControlContent(
//    cameraState: CameraState,
//    systemStatus: SystemStatus,
//    customButtons: List<ButtonConfig>,
//    toggleableIcons: List<ToggleableIcon>,
//    buttonStates: MutableMap<String, Boolean>,
//    onSettingsClick: () -> Unit,
//    onCameraSwitch: () -> Unit,
//    onRecordingToggle: () -> Unit,
//    onZoomChange: (Float) -> Unit,
//    onIconToggle: (String) -> Unit,
//    onCollapseClick: () -> Unit,
//    onSpeedUpdate: (Float) -> Unit = {}
//) {
//    // Manage scroll state with proper cleanup
//    val scrollState = rememberScrollState()
//
//    // Cleanup when component is disposed
//    DisposableEffect(Unit) {
//        Log.d("ExpandedControlContent", "Component created")
//        onDispose {
//            Log.d("ExpandedControlContent", "Component disposed - cleaning up")
//            // Clear any button states that might be lingering
//            try {
//                MemoryManager.cleanupWeakReferences()
//            } catch (e: Exception) {
//                Log.e("ExpandedControlContent", "Error during cleanup", e)
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        // Scrollable content
//        Column(
//            modifier = Modifier
//                .weight(1f)
//                .verticalScroll(rememberScrollState())
//                .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//
//        ) {
//
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    painter = painterResource(R.drawable.scout_logo), // Replace with your logo
//                    contentDescription = "Scout Logo",
//                    tint = Color.White,
//                    modifier = Modifier
//                        .width(24.dp)
//                        .height(18.dp)
//                )
//                Spacer(Modifier.width(4.dp))
//                Text(
//                    text = "Scout",
//                    style = TextStyle(
//                        fontSize = 16.sp,
//                        lineHeight = 14.02.sp,
//                        fontFamily = FontFamily(Font(R.font.onest_regular)),
//                        fontWeight = FontWeight(700),
//                        color = Color(0xFFC5C5C5)
//                    )
//                )
//            }
//
//
//            val firstRowIds = listOf("ir", "ir-cut-filter", "Settings")
//            val secondRowIds = listOf("picture-in-picture", "collapse-screen")
//
//            val firstRowButtons = customButtons.filter { it.id in firstRowIds }
//            val secondRowButtons = customButtons.filter { it.id in secondRowIds }
//
//            ButtonRow(buttons = firstRowButtons, buttonStates = buttonStates , onSettingsClick = onSettingsClick, onCollapseClick = onCollapseClick )
//            ButtonRow(buttons = secondRowButtons, buttonStates = buttonStates, onSettingsClick = onSettingsClick, onCollapseClick = onCollapseClick)
//
//            // Row 2: Status Indicators
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Recording button with dark theme
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(12.dp))
//                        .background(
//                            if (cameraState.isRecording) RecordRed else MediumDarkBackground
//                        )
//                        .clickable { onRecordingToggle() }
//                        .padding(horizontal = 16.dp, vertical = 8.dp),
//                    contentAlignment = Alignment.Center
//
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        // Record dot indicator
//                        Box(
//                            modifier = Modifier
//                                .size(12.dp)
//                                .clip(RoundedCornerShape(6.dp))
//                                .background(
//                                    if (cameraState.isRecording) White else RedVariant
//                                )
//                        )
//                        // Record text
//                        Text(
//                            text = "RECORD",
//                            color = if (cameraState.isRecording) White else MediumLightGray,
//                            fontSize = 14.sp,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//            }
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            )
//            {
//                var currentZoom by remember { mutableFloatStateOf(1f) }
//                // Zoom selector with dark theme
//                Box(
//                    modifier = Modifier
//                        .weight(1f)
//                        .background(Color.Transparent)
//                        .padding(horizontal = 16.dp, vertical = 8.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    val coroutineScope = rememberCoroutineScope()
//                    ZoomSelector(
//                        initialZoom = currentZoom,
//                        onZoomChanged = { zoom ->
//                            var zoomlevel = MotocamAPIHelper.ZOOM.X1
//                            zoomlevel = when(zoom) {
//                                1f -> MotocamAPIHelper.ZOOM.X1
//                                2f -> MotocamAPIHelper.ZOOM.X2
//                                else -> MotocamAPIHelper.ZOOM.X4
//                            }
//                            MotocamAPIAndroidHelper.setZoomAsync(
//                                scope = coroutineScope,
//                                zoom = zoomlevel
//                            )
//                            {result, error ->
//                                if(error != null) {
//                                    Log.e("UI", "Zoom Error: $error")
//                                    // Optionally show error to user or handle error state
//                                } else {
//                                    currentZoom = zoom
//                                    Log.d("Zoom", "Zoom set to $zoom X")
//                                    // Trigger zoom in your camera pipeline here
//                                    Log.i("Zoom", "Zoom Result: $result")
//                                }
//                            }
//                        }
//                    )
//                }
//            }
//
//            // Row 3: 6 toggleable icons
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(DarkBackground2)
//                    .padding(1.dp)
//
//            ) {
//                ToggleableIconRow(
//                    icons = toggleableIcons,
//                    onToggle = onIconToggle
//                )
//            }
//
//        }
//    }
//}


//@Composable
//fun ExpandedControlContent(
//    cameraState: CameraState,
//    systemStatus: SystemStatus,
//    customButtons: List<ButtonConfig>,
//    toggleableIcons: List<ToggleableIcon>,
//    buttonStates: MutableMap<String, Boolean>,
//    onSettingsClick: () -> Unit,
//    onCameraSwitch: () -> Unit,
//    onRecordingToggle: () -> Unit,
//    onZoomChange: (Float) -> Unit,
//    onIconToggle: (String) -> Unit,
//    onCollapseClick: () -> Unit,
//    onSpeedUpdate: (Float) -> Unit = {}
//) {
//    val scrollState = rememberScrollState()
//    val coroutineScope = rememberCoroutineScope()
//    var currentZoom by remember { mutableFloatStateOf(1f) }
//    val deviceType = rememberDeviceType()
//
//
//    DisposableEffect(Unit) {
//        Log.d("ExpandedControlContent", "Component created")
//        onDispose {
//            Log.d("ExpandedControlContent", "Component disposed - cleaning up")
//            try {
//                MemoryManager.cleanupWeakReferences()
//            } catch (e: Exception) {
//                Log.e("ExpandedControlContent", "Error during cleanup", e)
//            }
//        }
//    }
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        Column(
//            modifier = Modifier
//                .weight(1f)
//                .verticalScroll(scrollState)
//                .padding(horizontal = 24.dp, vertical = 16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Scout Header
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    painter = painterResource(R.drawable.scout_logo),
//                    contentDescription = "Scout Logo",
//                    tint = Color.White,
//                    modifier = Modifier.size(width = 24.dp, height = 18.dp)
//                )
//                Spacer(Modifier.width(4.dp))
//                Text(
//                    text = "Scout",
//                    style = TextStyle(
//                        fontSize = 16.sp,
//                        lineHeight = 14.02.sp,
//                        fontFamily = FontFamily(Font(R.font.onest_regular)),
//                        fontWeight = FontWeight.Bold,
//                        color = Color(0xFFC5C5C5)
//                    )
//                )
//            }
//
//            // Control Buttons Row 1
//            ButtonRow(
//                buttons = customButtons.filter { it.id in listOf("ir", "ir-cut-filter", "Settings") },
//                buttonStates = buttonStates,
//                onSettingsClick = onSettingsClick,
//                onCollapseClick = onCollapseClick
//            )
//
//            // Control Buttons Row 2
//            ButtonRow(
//                buttons = customButtons.filter { it.id in listOf("picture-in-picture", "collapse-screen") },
//                buttonStates = buttonStates,
//                onSettingsClick = onSettingsClick,
//                onCollapseClick = onCollapseClick
//            )
//
//            // Recording Button
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Recording button with dark theme
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(12.dp))
//                        .background(
//                            if (cameraState.isRecording) RecordRed else MediumDarkBackground
//                        )
//                        .clickable { onRecordingToggle() }
//                        .padding(horizontal = 16.dp, vertical = 8.dp),
//                    contentAlignment = Alignment.Center
//
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        // Record dot indicator
//                        Box(
//                            modifier = Modifier
//                                .size(12.dp)
//                                .clip(RoundedCornerShape(6.dp))
//                                .background(
//                                    if (cameraState.isRecording) White else RedVariant
//                                )
//                        )
//                        // Record text
//                        Text(
//                            text = "RECORD",
//                            color = if (cameraState.isRecording) White else MediumLightGray,
//                            fontSize = 14.sp,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//            }
//
//            // Zoom Selector Row
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp)
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(DarkBackground2),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                ZoomSelector(
//                    initialZoom = currentZoom,
//                    onZoomChanged = { zoom ->
//                        val zoomLevel = when (zoom) {
//                            1f -> MotocamAPIHelper.ZOOM.X1
//                            2f -> MotocamAPIHelper.ZOOM.X2
//                            else -> MotocamAPIHelper.ZOOM.X4
//                        }
//                        MotocamAPIAndroidHelper.setZoomAsync(
//                            scope = coroutineScope,
//                            zoom = zoomLevel
//                        ) { result, error ->
//                            if (error == null && result) {
//                                currentZoom = zoom
//                                Log.d("Zoom", "Zoom set to $zoom X")
//                            } else {
//                                Log.e("Zoom", "Zoom Error: $error")
//                            }
//                        }
//                    }
//                )
//            }
//
//            // Toggleable Icons Row
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(DarkBackground2)
//                    .padding(1.dp)
//            ) {
//                ToggleableIconRow(
//                    icons = toggleableIcons,
//                    onToggle = onIconToggle
//                )
//            }
//        }
//    }
//}


@OptIn(ExperimentalAnimationApi::class)
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
    val coroutineScope = rememberCoroutineScope()
    val deviceType = rememberDeviceType()
    val isDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val recordingViewModel: RecordingViewModel = viewModel()
    val cameraControlViewModel: CameraControlViewModel = viewModel()
    val cameraLayoutViewModel: CameraLayoutViewModel = viewModel()
    
    val isRecording by recordingViewModel.isRecording.collectAsStateWithLifecycle()
    val recordingState by recordingViewModel.recordingState.collectAsStateWithLifecycle()
    val cameraControlState by cameraControlViewModel.cameraControlState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        Log.d("ExpandedControlContent", "Component created")
        onDispose {
            Log.d("ExpandedControlContent", "Component disposed - cleaning up")
            try {
                MemoryManager.cleanupWeakReferences()
            } catch (e: Exception) {
                Log.e("ExpandedControlContent", "Error during cleanup", e)
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth > 600.dp
        val padding = if (deviceType == DeviceType.TABLET) 32.dp else 12.dp
        val spacing = if (deviceType == DeviceType.TABLET) 22.dp else 12.dp
        val layoutModifier = Modifier
            .fillMaxSize()
            .padding(horizontal = padding, vertical = spacing)

        val layoutDirection: @Composable (@Composable () -> Unit) -> Unit =
            if (deviceType == DeviceType.TABLET) { content -> Row(modifier = layoutModifier, horizontalArrangement = Arrangement.spacedBy(spacing)) { content() } }
            else { content -> Column(modifier = layoutModifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(spacing)) { content() } }

        layoutDirection {
            // Sidebar Column (for Tablet) or Full Column (for Phone)
            Column(
                modifier = Modifier
//                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.scout_logo),
                        contentDescription = "Scout Logo",
                        tint = if(isDarkTheme) Color.White else Color(0xFFC5C5C5),
                        modifier = Modifier.size(width = 24.dp, height = 18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Scout",
                        fontSize = if (deviceType == DeviceType.TABLET) 20.sp else 16.sp,
                        fontFamily = FontFamily(Font(R.font.onest_regular)),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC5C5C5)
                    )
                }

                var allButtons = listOf<List<ButtonConfig>>()
                // Control Button Rows
                if(deviceType == DeviceType.TABLET)
                {
                    allButtons = listOf(
                        customButtons.filter { it.id in listOf("Settings") },
                        customButtons.filter { it.id in listOf("ir", "ir-cut-filter") },
//                        customButtons.filter { it.id in listOf("picture-in-picture", "collapse-screen") }
                        customButtons.filter { it.id in listOf("collapse-screen") }
                    )
                }
                else {
                    allButtons = listOf(
                        customButtons.filter { it.id in listOf("ir", "ir-cut-filter", "Settings") },
//                        customButtons.filter { it.id in listOf("picture-in-picture", "collapse-screen") }
                        customButtons.filter { it.id in listOf( "collapse-screen") }
                    )
                }


                allButtons.forEach { buttonSet ->
                    ButtonRow(
                        buttons = buttonSet,
                        buttonStates = buttonStates,
                        onSettingsClick = onSettingsClick,
                        onCollapseClick = onCollapseClick
                    )
                }

                // Record Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if(deviceType == DeviceType.TABLET) 112.dp else 56.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            when (recordingState) {
                                is RecordingState.Recording -> RecordRed
                                is RecordingState.StoppingRecording -> RecordRed.copy(alpha = 0.7f)
                                is RecordingState.SavedToGallery -> Color(0xFF4CAF50) // Green color for success
                                RecordingState.NotRecording -> MediumDarkBackground
                            }
                        )
                        .clickable { 
                            if (!isRecording) {
                                onCollapseClick() // First collapse to minimal layout
                                recordingViewModel.toggleRecording(context) // Then start recording
                            } else {
                                recordingViewModel.toggleRecording(context) // Just stop recording if already recording
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Record dot indicator with pulsing animation when recording
                        val infiniteTransition = rememberInfiniteTransition()
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = if (recordingState is RecordingState.Recording) 1.2f else 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500),
                                repeatMode = RepeatMode.Reverse
                            )
                        )

                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .scale(if (recordingState is RecordingState.Recording) scale else 1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (recordingState) {
                                        is RecordingState.Recording -> White
                                        is RecordingState.StoppingRecording -> White.copy(alpha = 0.7f)
                                        is RecordingState.SavedToGallery -> Color(0xFF4CAF50)
                                        RecordingState.NotRecording -> RedVariant
                                    }
                                )
                        )

                        // Animated text content
                        AnimatedContent(
                            targetState = recordingState,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) with
                                fadeOut(animationSpec = tween(300))
                            }
                        ) { state ->
                            Text(
                                text = when (state) {
                                    is RecordingState.Recording -> "RECORDING ${state.duration}"
                                    is RecordingState.StoppingRecording -> "STOPPING RECORDING..."
                                    is RecordingState.SavedToGallery -> "SAVED TO GALLERY"
                                    RecordingState.NotRecording -> "RECORD"
                                },
                                color = if (state is RecordingState.NotRecording) MediumLightGray else Color(0xFFFFFFFF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Zoom Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if(deviceType == DeviceType.TABLET) 112.dp else 56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (cameraControlState.isZoomEnabled) DarkBackground2 else DarkBackground2.copy(alpha = 0.5f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cameraControlViewModel: CameraControlViewModel = viewModel()
                    val cameraControlState by cameraControlViewModel.cameraControlState.collectAsStateWithLifecycle()
                    
                    // Use key to force recomposition when zoom changes
                    key(cameraControlState.currentZoom) {
                        if (!cameraControlState.isZoomEnabled) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Zoom disabled when EIS or HDR is enabled",
                                    color = ButtonIconColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        } else {
                            ZoomSelector(
                                initialZoom = cameraControlState.currentZoom,
                                onZoomChanged = { newZoom ->
                                    if (cameraControlState.isZoomEnabled) {
                                        cameraControlViewModel.setZoom(newZoom)
                                    }
                                }
                            )
                        }
                    }
                }

                // Toggle Icons
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground2)
                        .padding(1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        toggleableIcons.take(toggleableIcons.size).forEach { iconData ->
                            // Update icon selection state based on camera control state
                            val isSelected = when (iconData.id) {
                                "stabilize" -> cameraControlState.isEisEnabled
                                "hdr" -> cameraControlState.isHdrEnabled
                                "viewmode" -> cameraControlState.isAutoDayNightEnabled
                                else -> iconData.isSelected
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .padding(1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Validate and render icon
                                val resourceId = iconData.iconPlaceholder
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = resourceId),
                                    contentDescription = iconData.description,
                                    modifier = Modifier.size(iconData.iconSize),
                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                        if (isSelected) iconData.colorOnSelect
                                        else Color(0xFF777777)
                                    ),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            }
                        }


                            WifiIndicator(isConnected = systemStatus.isWifiConnected)
                            AiStatusIndicator(
                                isEnabled = systemStatus.isAiEnabled,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            BatteryIndicator(
                                batteryLevel = systemStatus.batteryLevel,
                                showPercentage = false
                            )
                    }
                }
            }
        }
    }
}





@Composable
fun ButtonRow(
    buttons: List<ButtonConfig>,
    buttonStates: MutableMap<String, Boolean>,
    onSettingsClick: () -> Unit,
    onCollapseClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val deviceType = rememberDeviceType()
    val isDarkTheme = isSystemInDarkTheme()
    val cameraControlViewModel: CameraControlViewModel = viewModel()
    val cameraControlState by cameraControlViewModel.cameraControlState.collectAsStateWithLifecycle()
    val cameraLayoutViewModel: CameraLayoutViewModel = viewModel()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        buttons.forEach { buttonConfig ->
            val isSelected = when (buttonConfig.id) {
                "ir" -> cameraControlState.isIrEnabled
                "ir-cut-filter" -> !cameraControlState.isLowIntensity
                else -> buttonStates[buttonConfig.id] ?: false
            }
            val isLoading = remember { mutableStateOf(false) }

            val config = when (buttonConfig.id) {
                "Settings" -> buttonConfig.copy(
                    onClick = onSettingsClick,
                    backgroundColor = if (isSelected) ButtonSelectedBgColor else ButtonBgColor,
                    BorderColor = ButtonBorderColor,
                    color = if (isSelected) ButtonSelectedIconColor else ButtonIconColor,
                    text = buttonConfig.text
                )
                "collapse-screen" -> buttonConfig.copy(
                    onClick = onCollapseClick,
                    backgroundColor = if (isSelected) ButtonSelectedBgColor else ButtonBgColor,
                    BorderColor = ButtonBorderColor,
                    color = if (isSelected) ButtonSelectedIconColor else ButtonIconColor,
                    text = buttonConfig.text
                )
                "ir" -> buttonConfig.copy(
                    onClick = { cameraControlViewModel.toggleIR() },
                    backgroundColor = if (cameraLayoutViewModel.currentVisionMode.value == VisionMode.VISION) Color(0xFF272727)
                                    else if (isSelected) RecordRed 
                                    else ButtonBgColor,
                    BorderColor = if (isSelected) RecordRed else ButtonBorderColor,
                    color = if (cameraLayoutViewModel.currentVisionMode.value == VisionMode.VISION) Color(0xFF363636)
                           else if (isSelected) {if(isDarkTheme) ButtonSelectedIconColor else Color.White} 
                           else ButtonIconColor,
                    enabled = cameraLayoutViewModel.currentVisionMode.value != VisionMode.VISION,
                    text = buttonConfig.text
                )
                "ir-cut-filter" -> buttonConfig.copy(
                    backgroundColor = if(cameraControlState.isIrEnabled) {if (!cameraControlState.isLowIntensity) ButtonSelectedBgColor else ButtonBgColor} else {Color(0xFF272727)},
                    BorderColor = ButtonBorderColor,
                    color = if (cameraControlState.isIrEnabled) { if(!cameraControlState.isLowIntensity) ButtonSelectedIconColor else ButtonIconColor} else Color(0xFF363636),
                    enabled = cameraControlState.isIrEnabled,
                    onClick = { cameraControlViewModel.toggleIrIntensity() },
                    text = "IR Intensity"
                )
                "picture-in-picture" -> buttonConfig.copy(
                    backgroundColor = Color(0xFF272727),
                    BorderColor = ButtonBorderColor,
                    color = Color(0xFF363636),
                    enabled = false,
                    onClick = {},
                    text = buttonConfig.text
                )
                else -> buttonConfig.copy(
                    backgroundColor = if (isSelected) ButtonSelectedBgColor else ButtonBgColor,
                    BorderColor = ButtonBorderColor,
                    color = if (isSelected) ButtonSelectedIconColor else ButtonIconColor,
                    onClick = {
                        buttonStates[buttonConfig.id] = !isSelected
                        buttonConfig.onClick()
                    },
                    text = buttonConfig.text
                )
            }

            val weight = when (buttonConfig.id) {
                "Settings" -> 2f
                "ir-cut-filter" -> {
                    if (deviceType == DeviceType.TABLET) 1f else 2f
                }
                else -> 1f
            }

            val layout = when (buttonConfig.id) {
                "ir" -> {
                    if (deviceType == DeviceType.TABLET) "Row" else "Column"
                }
                else -> "Row"
            }

            CustomizableButton(
                config = config,
                modifier = Modifier.weight(weight),
                isCompact = false,
                showText = true,
                layout = layout
            )
        }
    }
}
