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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.components.buttons.CustomizableButton
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.components.buttons.ZoomSelector
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType
import com.outdu.camconnect.ui.viewmodels.RecordingViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import com.outdu.camconnect.ui.theme.camConnectIsDarkTheme
import androidx.compose.ui.draw.scale
import com.outdu.camconnect.ui.models.RecordingState
import com.outdu.camconnect.ui.viewmodels.CameraControlViewModel
import com.outdu.camconnect.Viewmodels.CameraLayoutViewModel
import com.outdu.camconnect.ui.models.VisionMode
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import com.outdu.camconnect.ui.theme.AppColors.immersiveButtonBorderColor
import com.outdu.camconnect.ui.components.dialogs.FilenamePromptDialog
import com.outdu.camconnect.ui.components.dialogs.SingleButtonAlertDialog
import com.outdu.camconnect.ui.viewmodels.IrIntensityLevel
import com.outdu.camconnect.ui.viewmodels.RecordingUiEvent
import kotlinx.coroutines.flow.collectLatest
import com.outdu.camconnect.utils.StorageUtils


/**
 * Expanded control content - scrollable with multiple control sections
 */
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
    val isDarkTheme = camConnectIsDarkTheme()
    val context = LocalContext.current
    val recordingViewModel: RecordingViewModel = viewModel()
    val cameraControlViewModel: CameraControlViewModel = viewModel()
    val cameraLayoutViewModel: CameraLayoutViewModel = viewModel()
    
    val isRecording by recordingViewModel.isRecording.collectAsStateWithLifecycle()
    val recordingState by recordingViewModel.recordingState.collectAsStateWithLifecycle()
    val cameraControlState by cameraControlViewModel.cameraControlState.collectAsStateWithLifecycle()

    var activeRecordingDialogEvent by remember { mutableStateOf<RecordingUiEvent?>(null) }
    LaunchedEffect(recordingViewModel) {
        recordingViewModel.uiEvents.collectLatest { event ->
            activeRecordingDialogEvent = event
        }
    }

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
        if (activeRecordingDialogEvent != null) {
            val minGb = StorageUtils.MIN_FREE_BYTES_FOR_RECORDING / 1_000_000_000
            val message = when (activeRecordingDialogEvent) {
                RecordingUiEvent.LowStorageCannotStart ->
                    "Insufficient storage.\nYou need at least ${minGb}GB free space to start recording."
                RecordingUiEvent.LowStorageStoppedRecording ->
                    "Recording stopped automatically.\nAvailable storage dropped below ${minGb}GB."
                null -> ""
            }

            SingleButtonAlertDialog(
                title = "Storage Warning",
                message = message,
                buttonText = "OK",
                onOk = { activeRecordingDialogEvent = null }
            )
        }

        val isTablet = maxWidth > 600.dp
        val padding = if (deviceType == DeviceType.TABLET) 32.dp else 12.dp
        val spacing = if (deviceType == DeviceType.TABLET) 18.dp else 12.dp
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
                        painter = painterResource(R.drawable.stravion_logo),
                        contentDescription = "Scout Logo",
                        tint = if(isDarkTheme) Color.White else StravionBlue,
                        modifier = Modifier.size(
                            width = if(deviceType == DeviceType.TABLET) 120.dp else 90.dp,
                            height = if(deviceType == DeviceType.TABLET) 48.dp else 36.dp)
                    )
                }

                var allButtons = listOf<List<ButtonConfig>>()
                // Control Button Rows
                if(deviceType == DeviceType.TABLET)
                {
                    allButtons = listOf(
                        customButtons.filter { it.id in listOf( "Settings") },
                        customButtons.filter { it.id in listOf("ir") },
                        customButtons.filter { it.id in listOf("picture-in-picture", "collapse-screen") }
                    )
                }
                else {
                    allButtons = listOf(
                        customButtons.filter { it.id in listOf("ir", "Settings") },
                        customButtons.filter { it.id in listOf("picture-in-picture", "collapse-screen") }
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
                                is RecordingState.PromptingForFilename -> RecordRed
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
                                        is RecordingState.PromptingForFilename -> White
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
                                    is RecordingState.PromptingForFilename -> "ENTER FILENAME..."
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
    
    // Show filename prompt dialog when recording is being stopped
    if (recordingState is RecordingState.PromptingForFilename) {
        FilenamePromptDialog(
            onConfirm = { filename ->
                recordingViewModel.stopRecordingWithFilename(context, filename)
            },
            onCancel = {
                recordingViewModel.cancelFilenamePrompt()
            }
        )
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
    val isDarkTheme = camConnectIsDarkTheme()
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
                else -> buttonStates[buttonConfig.id] ?: false
            }
            val isLoading = remember { mutableStateOf(false) }

            val config = when (buttonConfig.id) {
                "Settings" -> buttonConfig.copy(
                    onClick = onSettingsClick,
                    backgroundColor = if (isSelected) ButtonSelectedBgColor else ButtonBgColor,
                    BorderColor = immersiveButtonBorderColor,
                    color = if (isSelected) ButtonSelectedIconColor else ButtonIconColor,
                    text = buttonConfig.text
                )
                "collapse-screen" -> buttonConfig.copy(
                    onClick = onCollapseClick,
                    backgroundColor = if (isSelected) ButtonSelectedBgColor else ButtonBgColor,
                    BorderColor = immersiveButtonBorderColor,
                    color = if (isSelected) ButtonSelectedIconColor else ButtonIconColor,
                    text = buttonConfig.text
                )
                "ir" -> {
                    // Define colors for each IR intensity level
                    val (backgroundColor, borderColor, iconColor) = when (cameraControlState.irIntensityLevel) {
                        com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.OFF -> Triple(
                            Color.White,
                            immersiveButtonBorderColor,
                            ButtonIconColor
                        )
                        com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.LOW -> Triple(
                            Color(0xFFFFA07D), // Light orange
                            Color(0xFFFFA07D),
                            Color.White
                        )
                        com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.MEDIUM -> Triple(
                            Color(0xFFF87646), // Medium orange
                            Color(0xFFF87646),
                            Color.White
                        )
                        com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.HIGH -> Triple(
                            Color(0xFFF55114), // Dark orange
                            Color(0xFFF55114),
                            Color.White
                        )
                        com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.MAX -> Triple(
                            Color(0xFFE63900), // Very dark orange/red
                            Color(0xFFE63900),
                            Color.White
                        )
                        com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.ULTRA -> Triple(
                            Color(0xFFE63900), // Very dark orange/red
                            Color(0xFFE63900),
                            Color.White
                        )
                    }
                    
                    buttonConfig.copy(
                        onClick = { cameraControlViewModel.toggleIR() },
                        backgroundColor = if (cameraLayoutViewModel.currentVisionMode.value == VisionMode.VISION) {
                            immersiveButtonBorderColor
                        } else {
                            backgroundColor
                        },
                        BorderColor = if (cameraLayoutViewModel.currentVisionMode.value == VisionMode.VISION) {
                            immersiveButtonBorderColor
                        } else {
                            borderColor
                        },
                        color = if (cameraLayoutViewModel.currentVisionMode.value == VisionMode.VISION) {
                            Color(0xFFC5CBD4)
                        } else {
                            iconColor
                        },
                        enabled = cameraLayoutViewModel.currentVisionMode.value != VisionMode.VISION,
//                        enabled = false,
//                        text = buttonConfig.text
                        text = when (cameraControlState.irIntensityLevel) {
                            IrIntensityLevel.OFF -> "OFF"
                            IrIntensityLevel.LOW -> "LOW"
                            IrIntensityLevel.MEDIUM -> "MEDIUM"
                            IrIntensityLevel.HIGH -> "HIGH"
                            IrIntensityLevel.MAX ->"MAX"
                            IrIntensityLevel.ULTRA -> "ULTRA"
                        }
                    )
                }
                "picture-in-picture" -> buttonConfig.copy(
                    backgroundColor = immersiveButtonBorderColor,
                    BorderColor = immersiveButtonBorderColor,
                    color = Color(0xFFC5CBD4),
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

            val weight = 1f

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
