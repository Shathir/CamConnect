package com.outdu.camconnect.ui.components.settings.camera

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.models.*
import com.outdu.camconnect.ui.theme.AppColors.ButtonBorderColor
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

    val isDarkTheme = isSystemInDarkTheme()
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
                    isRed && isSelected -> Color(0xFFF43823)
                    isSelected -> if(!isDarkTheme) Color(0xFFD7D7D7) else Color(0xFF515151)
                    else -> DarkBackground3
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(if(deviceType == DeviceType.TABLET)16.dp else 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
//            if (isSelected && !isRed) {
                Icon(
                    painter = painterResource(iconVal),
                    contentDescription = null,
                    tint = if(!enabled) {
                        if(!isDarkTheme) Color(0xFFCCCCCC) else Color(0xFF666666)
                    } else if(isSelected) {
                        if(!isDarkTheme) Color(0xFF222222) else Color(0xFFFFFFFF)
                    } else {
                        if(!isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF8E8E8E)
                    }
                ,
                    modifier = Modifier.size(if(deviceType == DeviceType.TABLET) (24.dp) else (16.dp))
                        .padding(1.dp)
                )
//            }
            Text(
                text = text,
                style = TextStyle(
                    fontSize = if(deviceType == DeviceType.TABLET) 18.sp else 12.sp,
                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                    fontWeight = FontWeight(400),
                    color = if(!enabled) {
                        if(!isDarkTheme) Color(0xFFCCCCCC) else Color(0xFF666666)
                    } else if(isSelected) {
                        if(!isDarkTheme) Color(0xFF222222) else Color(0xFFFFFFFF)
                    } else {
                        if(!isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF8E8E8E)
                    }
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun CameraLayout(
    modifier: Modifier = Modifier,
    viewModel: CameraLayoutViewModel = viewModel()
) {
    val isDarkTheme = isSystemInDarkTheme()
    val deviceType = rememberDeviceType()

    // Observe states from ViewModel using collectAsState
    val autoDayNightEnabled = viewModel.isAutoDayNightEnabled.value
    val currentVisionMode = viewModel.currentVisionMode.value
    val currentCameraMode = viewModel.currentCameraMode.value
    val currentOrientationMode = viewModel.currentOrientationMode.value
    val hasChanges = viewModel.hasUnsavedChanges.value
    val isLowLightModeActive = viewModel.isLowLightModeActive

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Apply Changes Button - always visible, enabled only when there are changes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { viewModel.applyChanges() },
                enabled = hasChanges,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = Color(0xFF2C2C2C)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Apply Changes",
                    color = if (hasChanges) Color.White else Color(0xFF777777)
                )
            }
        }

        // Settings sections
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
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
                            fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
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
                            isRed = true,
                            iconVal = R.drawable.router_line
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
                        text = "Camera Capture",
                        style = TextStyle(
                            fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
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
                            text = "EIS",
                            isSelected = if (isLowLightModeActive) false else (currentCameraMode == CameraMode.EIS || currentCameraMode == CameraMode.BOTH),
                            onClick = {
                                if (!isLowLightModeActive) {
                                    viewModel.toggleCameraMode(CameraMode.EIS)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.git_commit_line,
                            enabled = !isLowLightModeActive
                        )
                        OptionButton(
                            text = "HDR",
                            isSelected = if (isLowLightModeActive) true else (currentCameraMode == CameraMode.HDR || currentCameraMode == CameraMode.BOTH),
                            onClick = {
                                if (!isLowLightModeActive) {
                                    viewModel.toggleCameraMode(CameraMode.HDR)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.hd_settings_line,
                            enabled = !isLowLightModeActive
                        )
                    }
                }

                // Orientation section
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Orientation",
                        style = TextStyle(
                            fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
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
                            text = if(deviceType == DeviceType.TABLET) "Flip Vertical" else "Flip",
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

            // Warning text for zoom control
            Text(
                text = "To Activate Zoom Control, Disable HDR & EIS",
                style = TextStyle(
                    fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
                    lineHeight = 10.51.sp,
                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                    fontWeight = FontWeight(500),
                    color = if(isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF777777)
                )
            )
        }
    }

    // Effect to refresh settings when the composable enters composition
    LaunchedEffect(Unit) {
        viewModel.refreshSettings()
    }
}