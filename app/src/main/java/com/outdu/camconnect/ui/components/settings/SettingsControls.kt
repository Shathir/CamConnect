package com.outdu.camconnect.ui.components.settings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.ui.models.CameraMode
import com.outdu.camconnect.ui.models.OrientationMode
import com.outdu.camconnect.ui.models.VisionMode
import com.outdu.camconnect.ui.theme.*
import com.outdu.camconnect.ui.theme.AppColors.ButtonBorderColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonIconColor
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType


/**
 * Display settings section with auto day/night mode and vision mode selection
 */
@Composable
fun DisplaySettingsSection(
    isAutoDayNightEnabled: Boolean,
    onAutoDayNightToggle: (Boolean) -> Unit,
    selectedVisionMode: VisionMode,
    onVisionModeSelected: (VisionMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section title placeholder
        Box(
            modifier = Modifier
                .height(16.dp)
                .width(120.dp)
                .background(DarkBackground2)
        )
        
        // Auto Day/Night Mode Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label placeholder
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .width(140.dp)
                    .background(DarkBackground1)
            )
            // Custom switch
            CustomSwitch(
                checked = isAutoDayNightEnabled,
                onCheckedChange = onAutoDayNightToggle
            )
        }
        
        // Vision Mode Selection
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Label placeholder
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .width(80.dp)
                    .background(DarkBackground1)
            )
            VisionModeSelector(
                selectedMode = selectedVisionMode,
                onModeSelected = onVisionModeSelected
            )
        }
    }
}

@Composable
private fun VisionModeSelector(
    selectedMode: VisionMode,
    onModeSelected: (VisionMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VisionMode.values().forEach { mode ->
            CustomChip(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Detection toggles section for object, far object, and motion detection
 */
@Composable
fun DetectionSettingsSection(
    isObjectDetectionEnabled: Boolean,
    onObjectDetectionToggle: (Boolean) -> Unit,
    isFarObjectDetectionEnabled: Boolean,
    onFarObjectDetectionToggle: (Boolean) -> Unit,
    isMotionDetectionEnabled: Boolean,
    onMotionDetectionToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section title placeholder
        Box(
            modifier = Modifier
                .height(16.dp)
                .width(130.dp)
                .background(DarkBackground2)
        )
        
        DetectionToggle(
            label = "Object Detection",
            isEnabled = isObjectDetectionEnabled,
            onToggle = onObjectDetectionToggle
        )
        
        DetectionToggle(
            label = "Far Object Detection",
            isEnabled = isFarObjectDetectionEnabled,
            onToggle = onFarObjectDetectionToggle
        )
        
        DetectionToggle(
            label = "Motion Detection",
            isEnabled = isMotionDetectionEnabled,
            onToggle = onMotionDetectionToggle
        )
    }
}

@Composable
private fun DetectionToggle(
    label: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label placeholder
        Box(
            modifier = Modifier
                .height(14.dp)
                .width((label.length * 8).dp)
                .background(DarkBackground1)
        )
        CustomSwitch(
            checked = isEnabled,
            onCheckedChange = onToggle
        )
    }
}

/**
 * Image settings section for camera mode and orientation
 */
@Composable
fun ImageSettingsSection(
    selectedCameraMode: CameraMode,
    onCameraModeSelected: (CameraMode) -> Unit,
    selectedOrientationMode: OrientationMode,
    onOrientationModeSelected: (OrientationMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section title placeholder
        Box(
            modifier = Modifier
                .height(16.dp)
                .width(100.dp)
                .background(DarkBackground2)
        )
        
        // Camera Mode Selection
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Label placeholder
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .width(90.dp)
                    .background(DarkBackground1)
            )
            CameraModeSelector(
                selectedMode = selectedCameraMode,
                onModeSelected = onCameraModeSelected
            )
        }
        
        // Orientation Mode Selection
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Label placeholder
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .width(80.dp)
                    .background(DarkBackground1)
            )
            OrientationModeSelector(
                selectedMode = selectedOrientationMode,
                onModeSelected = onOrientationModeSelected
            )
        }
    }
}

@Composable
private fun CameraModeSelector(
    selectedMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CameraMode.values().forEach { mode ->
            CustomChip(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OrientationModeSelector(
    selectedMode: OrientationMode,
    onModeSelected: (OrientationMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OrientationMode.values().forEach { mode ->
            CustomChip(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Tab switcher for Camera Control and Device Control
 */
@Composable
fun ControlTabSwitcher(
    selectedTab: ControlTab,
    onTabSelected: (ControlTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val deviceType = rememberDeviceType()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height( if(deviceType == DeviceType.TABLET) 76.dp else 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkBackground2),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ControlTab.entries.forEach { tab ->
            if(tab.displayName == "AI Vision")
            {

                val isSelected = tab == selectedTab

                val infiniteTransition = rememberInfiniteTransition(label = "gradient_anim")
                val animatedOffsetX by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 300f, // controls the looping range
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 3000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "gradient_offset"
                )

                val animatedBrush = if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF4E8EFF), Color(0xFFFFF399)),
                        start = Offset(animatedOffsetX, 0f),
                        end = Offset(animatedOffsetX + 200f, 100f) // create diagonal animation
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(DarkBackground2, DarkBackground2)
                    )
                }

                val borderBrush = if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF4E8EFF), Color(0xFFFFF399)),
                        start = Offset(animatedOffsetX, 0f),
                        end = Offset(animatedOffsetX + 200f, 100f) // create diagonal animation
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF4E8EFF), Color(0xFFFFF399))
                    )
                }

                val backgroundBrush = if(isSelected)
                {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF4E8EFF), Color(0xFFFFF399))
                    )

                }
                else {
                    Brush.linearGradient(
                        colors = listOf(DarkBackground2, DarkBackground2)

                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .gradientBorder(
                            cornerRadius = if (isDarkTheme) 21.dp else 22.dp,
                            borderWidth = if (isDarkTheme) 1.dp else 2.dp,
                            gradient = borderBrush
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            animatedBrush
                        )
                        .clickable { onTabSelected(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            painter = when (tab) {
                                ControlTab.CAMERA_CONTROL -> painterResource(id = com.outdu.camconnect.R.drawable.camera_line)
                                ControlTab.AI_CONTROL -> painterResource(id = com.outdu.camconnect.R.drawable.ai_line)
                                ControlTab.LICENSE_CONTROL -> painterResource(id = com.outdu.camconnect.R.drawable.usercircle)
                            },
                            contentDescription = tab.displayName,
                            tint = if (selectedTab == tab) if(isDarkTheme) Color.White else Color.Black
                            else if(isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF5C5C5C),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tab.displayName,
                            color = if (selectedTab == tab) if(isDarkTheme) Color.White else Color.Black
                            else if(isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF5C5C5C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                }

            }
            else {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = if (isDarkTheme) 0.dp else 2.dp, // No border in dark theme
                            color = ButtonBorderColor,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .background(
                            if (selectedTab == tab) if (isDarkTheme) Color(0xFF515151) else Color(0xFFD7D7D7)
                            else if (isDarkTheme) Color(0xFF333333) else Color(0xFFFFFFFF )
                        )
                        .clickable(
                            enabled = tab != ControlTab.LICENSE_CONTROL,
                            onClick = { onTabSelected(tab) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            painter = when (tab) {
                                ControlTab.CAMERA_CONTROL -> painterResource(id = com.outdu.camconnect.R.drawable.camera_line)
                                ControlTab.AI_CONTROL -> painterResource(id = com.outdu.camconnect.R.drawable.ai_line)
                                ControlTab.LICENSE_CONTROL -> painterResource(id = com.outdu.camconnect.R.drawable.usercircle)
                            },
                            contentDescription = tab.displayName,
                            tint = if (tab == ControlTab.LICENSE_CONTROL) {
                                if (isDarkTheme) Color(0xFF4A4A4A) else Color(0xFFB0B0B0)
                            } else if (selectedTab == tab) {
                                if(isDarkTheme) Color.White else Color.Black
                            } else {
                                if(isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF5C5C5C)
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tab.displayName,
                            color = if (tab == ControlTab.LICENSE_CONTROL) {
                                if (isDarkTheme) Color(0xFF4A4A4A) else Color(0xFFB0B0B0)
                            } else if (selectedTab == tab) {
                                if(isDarkTheme) Color.White else Color.Black
                            } else {
                                if(isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF5C5C5C)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

fun Modifier.gradientBorder(
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 2.dp,
    gradient: Brush
): Modifier = this
    .background(gradient, shape = RoundedCornerShape(cornerRadius))
    .padding(borderWidth)


/**
 * Custom switch component
 */
@Composable
private fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(48.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (checked) ConnectionGreen else Gray.copy(alpha = 0.3f)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
        )
    }
}

/**
 * Custom chip component
 */
@Composable
private fun CustomChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) BluePrimary else Gray.copy(alpha = 0.2f)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Text placeholder
        Box(
            modifier = Modifier
                .height(12.dp)
                .fillMaxWidth(0.6f)
                .background(
                    if (selected) Color.White.copy(alpha = 0.8f)
                    else Color.Black.copy(alpha = 0.3f)
                )
        )
    }
}

enum class ControlTab(val displayName: String) {
    CAMERA_CONTROL("Camera"),
    AI_CONTROL("AI Vision"),
    LICENSE_CONTROL("Manage"),
} 