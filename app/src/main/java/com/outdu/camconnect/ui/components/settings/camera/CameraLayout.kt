package com.outdu.camconnect.ui.components.settings.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.models.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.res.painterResource
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
    iconVal: Int
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
            .clickable(onClick = onClick)
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
                    tint = if(isSelected) {if(!isDarkTheme) Color(0xFF222222) else Color(0xFFFFFFFF)} else{ if(!isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF8E8E8E)},
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
                    color = if(isSelected) {if(!isDarkTheme) Color(0xFF222222) else Color(0xFFFFFFFF)} else{ if(!isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF8E8E8E)}
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun CameraLayout(
    cameraState: CameraState,
    systemStatus: SystemStatus,
    detectionSettings: DetectionSettings,
    customButtons: List<ButtonConfig>,
    onAutoDayNightToggle: (Boolean) -> Unit,
    onVisionModeSelected: (VisionMode) -> Unit,
    onObjectDetectionToggle: (Boolean) -> Unit,
    onFarObjectDetectionToggle: (Boolean) -> Unit,
    onMotionDetectionToggle: (Boolean) -> Unit,
    onCameraModeSelected: (CameraMode) -> Unit,
    onOrientationModeSelected: (OrientationMode) -> Unit,
    modifier: Modifier = Modifier
) {

    val isDarkTheme = isSystemInDarkTheme()

    val deviceType = rememberDeviceType()
    // Initial values
    var initialAutoDayNight by remember { mutableStateOf(cameraState.isAutoDayNightEnabled) }
    var initialVisionMode by remember { mutableStateOf(cameraState.visionMode) }
    var initialCameraMode by remember { mutableStateOf(cameraState.cameraMode) }
    var initialOrientationMode by remember { mutableStateOf(cameraState.orientationMode) }
    
    // Current state
    var autoDayNightEnabled by remember { mutableStateOf(initialAutoDayNight) }
    var currentVisionMode by remember { mutableStateOf(initialVisionMode) }
    var currentCameraMode by remember { mutableStateOf(initialCameraMode) }
    var currentOrientationMode by remember { mutableStateOf(initialOrientationMode) }
    
    // Check if there are changes from initial values
    val hasChanges = remember(
        autoDayNightEnabled, currentVisionMode, currentCameraMode, currentOrientationMode,
        initialAutoDayNight, initialVisionMode, initialCameraMode, initialOrientationMode
    ) {
        autoDayNightEnabled != initialAutoDayNight ||
        currentVisionMode != initialVisionMode ||
        currentCameraMode != initialCameraMode ||
        currentOrientationMode != initialOrientationMode
    }
    
    // Function to save changes
    val saveChanges = {
        onAutoDayNightToggle(autoDayNightEnabled)
        onVisionModeSelected(currentVisionMode)
        onCameraModeSelected(currentCameraMode)
        onOrientationModeSelected(currentOrientationMode)
        
        // Update initial values
        initialAutoDayNight = autoDayNightEnabled
        initialVisionMode = currentVisionMode
        initialCameraMode = currentCameraMode
        initialOrientationMode = currentOrientationMode
    }

    // Function to toggle Camera Mode
    fun toggleCameraMode(mode: CameraMode) {
        currentCameraMode = when (mode) {
            CameraMode.HDR -> {
                when (currentCameraMode) {
                    CameraMode.HDR -> CameraMode.OFF    // HDR → OFF
                    CameraMode.EIS -> CameraMode.BOTH   // EIS → BOTH
                    CameraMode.BOTH -> CameraMode.EIS   // BOTH → EIS
                    CameraMode.OFF -> CameraMode.HDR    // OFF → HDR
                }
            }
            CameraMode.EIS -> {
                when (currentCameraMode) {
                    CameraMode.EIS -> CameraMode.OFF    // EIS → OFF
                    CameraMode.HDR -> CameraMode.BOTH   // HDR → BOTH
                    CameraMode.BOTH -> CameraMode.HDR   // BOTH → HDR
                    CameraMode.OFF -> CameraMode.EIS    // OFF → EIS
                }
            }
            else -> currentCameraMode  // Keep current state for other modes
        }
    }

    // Function to toggle Orientation Mode
    fun toggleOrientationMode(mode: OrientationMode) {
        currentOrientationMode = when (mode) {
            OrientationMode.FLIP -> {
                when (currentOrientationMode) {
                    OrientationMode.FLIP -> OrientationMode.NORMAL  // FLIP → OFF
                    OrientationMode.MIRROR -> OrientationMode.BOTH  // MIRROR → BOTH
                    OrientationMode.BOTH -> OrientationMode.MIRROR  // BOTH → MIRROR
                    OrientationMode.NORMAL -> OrientationMode.FLIP  // OFF → FLIP
                }
            }
            OrientationMode.MIRROR -> {
                when (currentOrientationMode) {
                    OrientationMode.MIRROR -> OrientationMode.NORMAL  // MIRROR → OFF
                    OrientationMode.FLIP -> OrientationMode.BOTH     // FLIP → BOTH
                    OrientationMode.BOTH -> OrientationMode.FLIP     // BOTH → FLIP
                    OrientationMode.NORMAL -> OrientationMode.MIRROR  // OFF → MIRROR
                }
            }
            else -> currentOrientationMode  // Keep current state for other modes
        }
    }

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
                onClick = { saveChanges() },
                enabled = hasChanges,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = Color(0xFF2C2C2C) // Dark theme disabled color
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Apply Changes",
                    color = if (hasChanges) Color.White else Color(0xFF777777) // Gray text when disabled
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
            )
            {
                // Auto Day/Night
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Auto Low Light",
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
                            text = "ON",
                            isSelected = autoDayNightEnabled,
                            onClick = { autoDayNightEnabled = true },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.yes_line
                        )
                        OptionButton(
                            text = "OFF",
                            isSelected = !autoDayNightEnabled,
                            onClick = { autoDayNightEnabled = false },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.no_line
                        )
                    }
                }

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
                            onClick = { currentVisionMode = VisionMode.VISION },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.eye_line
                        )
                        OptionButton(
                            text = "Low Light",
                            isSelected = currentVisionMode == VisionMode.INFRARED,
                            onClick = { currentVisionMode = VisionMode.INFRARED },
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
            )
            {
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
                            isSelected = currentCameraMode == CameraMode.EIS,
                            onClick = { currentCameraMode = if (currentCameraMode == CameraMode.EIS) CameraMode.OFF else CameraMode.EIS },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.git_commit_line
                        )
                        OptionButton(
                            text = "HDR",
                            isSelected = currentCameraMode == CameraMode.HDR,
                            onClick = { currentCameraMode = if (currentCameraMode == CameraMode.HDR) CameraMode.OFF else CameraMode.HDR },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.hd_settings_line
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
                            onClick = { toggleOrientationMode(OrientationMode.FLIP) },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.flip_vertical_line
                        )
                        OptionButton(
                            text = "Mirror",
                            isSelected = currentOrientationMode == OrientationMode.MIRROR || currentOrientationMode == OrientationMode.BOTH,
                            onClick = { toggleOrientationMode(OrientationMode.MIRROR) },
                            modifier = Modifier.weight(1f),
                            iconVal = R.drawable.flip_horizontal_line
                        )
                    }
                }
            }

            // Warning text for zoom control
            Text(
                text = "To Activate Zoom Control, Disable WDR & EIS",
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
}