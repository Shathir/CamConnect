package com.outdu.camconnect.ui.components.settings.camera

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.outdu.camconnect.ui.theme.camConnectIsDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outdu.camconnect.R
import com.outdu.camconnect.Viewmodels.CameraLayoutViewModel
import com.outdu.camconnect.ui.models.*
import com.outdu.camconnect.ui.theme.AppColors.ButtonBorderColor
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import com.outdu.camconnect.ui.theme.DarkBackground3
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType

@Composable
fun OptionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isRed: Boolean = false,
    iconVal: Int,
    enabled: Boolean = true
) {

    val isDarkTheme = camConnectIsDarkTheme()
    val deviceType = rememberDeviceType()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isDarkTheme) 0.dp else 2.dp, // No border in dark theme
                color = ButtonBorderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                when {
                    !enabled -> if (!isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF2C2C2C) // Disabled state background
                    isRed && isSelected -> Color(0xFFF43823)
                    isSelected -> if (!isDarkTheme) Color(0xFFD7D7D7) else Color(0xFF515151)
                    else -> DarkBackground3
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(if (deviceType == DeviceType.TABLET) 16.dp else 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
//            if (isSelected && !isRed) {
            Icon(
                painter = painterResource(iconVal),
                contentDescription = null,
                tint = if (!enabled) {
                    if (!isDarkTheme) Color(0xFFCCCCCC) else Color(0xFF666666)
                } else if (isSelected) {
                    if (!isDarkTheme) if (isRed) Color.White else Color(0xFF222222) else Color(
                        0xFFFFFFFF
                    )
                } else {
                    if (!isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF8E8E8E)
                },
                modifier = Modifier
                    .size(if (deviceType == DeviceType.TABLET) (24.dp) else (16.dp))
                    .padding(1.dp)
            )
//            }
            Text(
                text = text,
                style = TextStyle(
                    fontSize = if (deviceType == DeviceType.TABLET) 18.sp else 12.sp,
                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                    fontWeight = FontWeight(400),
                    color = if (!enabled) {
                        if (!isDarkTheme) Color(0xFFCCCCCC) else Color(0xFF666666)
                    } else if (isSelected) {
                        if (!isDarkTheme) if (isRed) Color(0xFFFFFFFF) else Color(0xFF222222) else Color(
                            0xFFFFFFFF
                        )
                    } else {
                        if (!isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF8E8E8E)
                    }
                ),
                maxLines = 1
            )
        }
    }
}

// Apply Changes Button Component - separated for flexible layout
@Composable
fun CameraLayoutApplyButton(
    viewModel: CameraLayoutViewModel = viewModel()
) {
    val hasChanges by remember { viewModel.hasUnsavedChanges }
    val isUIInteractive by viewModel.isUIInteractive.collectAsState()
    val isApplying = !isUIInteractive
    
    // Pulse animation when applying changes
    val pulseAnimation by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Scale animation for button press feedback
    val scale by animateFloatAsState(
        targetValue = if (isApplying) pulseAnimation else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = { viewModel.applyChanges() },
            enabled = hasChanges && isUIInteractive,
            colors = ButtonDefaults.buttonColors(
                containerColor = StravionBlue,
                disabledContainerColor = Color(0xFF2C2C2C)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.scale(scale)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show loading indicator when applying
                if (isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                }
                
                Text(
                    text = if (isApplying) "Applying..." else "Apply Changes",
                    color = if (hasChanges && isUIInteractive) Color.White else Color(0xFF777777)
                )
            }
        }
    }
}

// Camera Layout Content - Settings without the button
@Composable
fun CameraLayoutContent(
    modifier: Modifier = Modifier,
    viewModel: CameraLayoutViewModel = viewModel()
) {
    val isDarkTheme = camConnectIsDarkTheme()
    val deviceType = rememberDeviceType()

    // Observe states from ViewModel using collectAsState
    val autoDayNightEnabled by remember { viewModel.isAutoDayNightEnabled }
    val appliedAutoDayNight by remember { viewModel.appliedAutoDayNight }
    val currentVisionMode by remember { viewModel.currentVisionMode }
    val currentCameraMode by remember { viewModel.currentCameraMode }
    val currentOrientationMode by remember { viewModel.currentOrientationMode }
    val currentVideoFrequency by remember { viewModel.currentVideoFrequency }

    // Settings sections
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Auto / Manual toggle
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Control Mode",
                style = TextStyle(
                    fontSize = if (deviceType == DeviceType.TABLET) 16.sp else 14.sp,
                    lineHeight = 14.02.sp,
                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                    fontWeight = FontWeight(500),
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OptionButton(
                    text = "Auto",
                    isSelected = autoDayNightEnabled,
                    onClick = { viewModel.setAutoDayNight(true) },
                    modifier = Modifier.weight(1f),
                    iconVal = R.drawable.low_light
                )
                OptionButton(
                    text = "Manual",
                    isSelected = !autoDayNightEnabled,
                    onClick = { viewModel.setAutoDayNight(false) },
                    modifier = Modifier.weight(1f),
                    iconVal = R.drawable.settings_line
                )
            }
        }

        // If Auto is enabled (applied state), hide all other controls
        if (!appliedAutoDayNight) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Auto Day/Night
//                Column(
//                    modifier = Modifier.weight(1f),
//                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    Text(
//                        text = "Auto Low Light",
//                        style = TextStyle(
//                            fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
//                            lineHeight = 14.02.sp,
//                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
//                            fontWeight = FontWeight(500),
//                            color = if (isDarkTheme) Color.White else Color.Black
//                        )
//                    )
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        OptionButton(
//                            text = "ON",
//                            isSelected = autoDayNightEnabled,
//                            onClick = { viewModel.setAutoDayNight(true) },
//                            modifier = Modifier.weight(1f),
//                            iconVal = R.drawable.yes_line
//                        )
//                        OptionButton(
//                            text = "OFF",
//                            isSelected = !autoDayNightEnabled,
//                            onClick = { viewModel.setAutoDayNight(false) },
//                            modifier = Modifier.weight(1f),
//                            iconVal = R.drawable.no_line
//                        )
//                    }
//                }

                // Display Modes
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Display Modes",
                        style = TextStyle(
                            fontSize = if (deviceType == DeviceType.TABLET) 16.sp else 14.sp,
                            lineHeight = 14.02.sp,
                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                            fontWeight = FontWeight(500),
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OptionButton(
                            text = "Visible",
                            isSelected = currentVisionMode == VisionMode.VISION,
                            onClick = { viewModel.setVisionMode(VisionMode.VISION) },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.eye_line
                        )
                        OptionButton(
                            text = "Low Light",
                            isSelected = currentVisionMode == VisionMode.BOTH,
                            onClick = { viewModel.setVisionMode(VisionMode.BOTH) },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.low_light
                        )
                        OptionButton(
                            text = "IR",
                            isSelected = currentVisionMode == VisionMode.INFRARED,
                            onClick = { viewModel.setVisionMode(VisionMode.INFRARED) },
                            modifier = Modifier.weight(1f),
                            isRed = true,
                            iconVal = R.drawable.night_logo
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Camera Capture section
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Camera Modes",
                        style = TextStyle(
                            fontSize = if (deviceType == DeviceType.TABLET) 16.sp else 14.sp,
                            lineHeight = 14.02.sp,
                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                            fontWeight = FontWeight(500),
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OptionButton(
                            text = if (currentVisionMode == VisionMode.BOTH) "Color" else "EIS",
                            isSelected = if (currentVisionMode == VisionMode.BOTH) {
                                // In Low Light mode, Color button is selected when camera mode is OFF
                                currentCameraMode == CameraMode.OFF
                            } else {
                                // In other modes, EIS button follows normal logic
                                currentCameraMode == CameraMode.EIS || currentCameraMode == CameraMode.BOTH
                            },
                            onClick = {
                                if (currentVisionMode == VisionMode.BOTH) {
                                    // In Low Light mode, Color button sets camera mode to OFF
                                    viewModel.setCameraMode(CameraMode.OFF)
                                } else {
                                    // In other modes, use normal toggle logic
                                    viewModel.toggleCameraMode(CameraMode.EIS)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.git_commit_line,
//                            enabled = currentCameraMode != CameraMode.FOURK
                        )
                        OptionButton(
                            text = if (currentVisionMode == VisionMode.BOTH) "Mono" else "HDR",
                            isSelected = if (currentVisionMode == VisionMode.BOTH) {
                                // In Low Light mode, Mono button is selected when camera mode is BOTH
                                currentCameraMode == CameraMode.BOTH
                            } else {
                                // In other modes, HDR button follows normal logic
                                currentCameraMode == CameraMode.HDR || currentCameraMode == CameraMode.BOTH
                            },
                            onClick = {
                                if (currentVisionMode == VisionMode.BOTH) {
                                    // In Low Light mode, Mono button toggles between BOTH and OFF
                                    if (currentCameraMode == CameraMode.BOTH) {
                                        viewModel.setCameraMode(CameraMode.OFF)
                                    } else {
                                        viewModel.setCameraMode(CameraMode.BOTH)
                                    }
                                } else {
                                    // In other modes, use normal toggle logic
                                    viewModel.toggleCameraMode(CameraMode.HDR)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.hd_settings_line,
//                            enabled = currentCameraMode != CameraMode.FOURK
                        )
                        // 4K button - only show in Visible and IR modes
//                        if (currentVisionMode == VisionMode.VISION || currentVisionMode == VisionMode.INFRARED) {
//                            OptionButton(
//                                text = "4K",
//                                isSelected = currentCameraMode == CameraMode.FOURK,
//                                onClick = {
//                                    viewModel.toggleCameraMode(CameraMode.FOURK)
//                                },
//                                modifier = Modifier.weight(1f),
//                                iconVal = R.drawable.hd_line,
//                                enabled = true
//                            )
//                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Orientation section
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Orientation",
                        style = TextStyle(
                            fontSize = if (deviceType == DeviceType.TABLET) 16.sp else 14.sp,
                            lineHeight = 14.02.sp,
                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                            fontWeight = FontWeight(500),
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OptionButton(
                            text = if (deviceType == DeviceType.TABLET) "Flip Vertical" else "Flip",
                            isSelected = currentOrientationMode == OrientationMode.FLIP || currentOrientationMode == OrientationMode.BOTH,
                            onClick = { viewModel.toggleOrientationMode(OrientationMode.FLIP) },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.flip_vertical_line
                        )
                        OptionButton(
                            text = "Mirror",
                            isSelected = currentOrientationMode == OrientationMode.MIRROR || currentOrientationMode == OrientationMode.BOTH,
                            onClick = { viewModel.toggleOrientationMode(OrientationMode.MIRROR) },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.flip_horizontal_line
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Video Frequency",
                        style = TextStyle(
                            fontSize = if (deviceType == DeviceType.TABLET) 16.sp else 14.sp,
                            lineHeight = 14.02.sp,
                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                            fontWeight = FontWeight(500),
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OptionButton(
                            text = "50 Hz",
                            isSelected = currentVideoFrequency == com.outdu.camconnect.communication.MotocamAPIHelper.VIDEO_FREQUENCY.HZ_50,
                            onClick = { viewModel.setVideoFrequency(com.outdu.camconnect.communication.MotocamAPIHelper.VIDEO_FREQUENCY.HZ_50) },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.git_commit_line
                        )
                        OptionButton(
                            text = "60 Hz",
                            isSelected = currentVideoFrequency == com.outdu.camconnect.communication.MotocamAPIHelper.VIDEO_FREQUENCY.HZ_60,
                            onClick = { viewModel.setVideoFrequency(com.outdu.camconnect.communication.MotocamAPIHelper.VIDEO_FREQUENCY.HZ_60) },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.git_commit_line
                        )
                    }
                }
            }

            // Warning text for zoom control
            Text(
                text = "To Activate Zoom Control, Disable HDR & EIS",
                style = TextStyle(
                    fontSize = if (deviceType == DeviceType.TABLET) 16.sp else 14.sp,
                    lineHeight = 10.51.sp,
                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                    fontWeight = FontWeight(500),
                    color = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF777777)
                )
            )
        }
    }

    // Effect to refresh settings when the composable enters composition
    LaunchedEffect(Unit) {
        viewModel.refreshSettings()
    }
}


// Full Camera Layout - combines button and content (for backward compatibility)
@Composable
fun CameraLayout(
    modifier: Modifier = Modifier,
    viewModel: CameraLayoutViewModel = viewModel()
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CameraLayoutApplyButton(viewModel = viewModel)
        CameraLayoutContent(viewModel = viewModel)
    }
}