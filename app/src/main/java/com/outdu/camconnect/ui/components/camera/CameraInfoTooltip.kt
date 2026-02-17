package com.outdu.camconnect.ui.components.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.outdu.camconnect.Viewmodels.CameraLayoutViewModel
import com.outdu.camconnect.ui.models.CameraMode
import com.outdu.camconnect.ui.models.VisionMode
import com.outdu.camconnect.ui.theme.DarkBackground2
import com.outdu.camconnect.ui.theme.MediumLightGray
import com.outdu.camconnect.ui.theme.RecordRed
import com.outdu.camconnect.ui.theme.White
import com.outdu.camconnect.ui.viewmodels.CameraControlViewModel
import com.outdu.camconnect.ui.viewmodels.IrIntensityLevel

/**
 * Icon button for camera info tooltip with active state highlighting
 */
@Composable
fun CameraInfoIcon(
    tooltipManager: TooltipManager,
    modifier: Modifier = Modifier
) {
    val tooltipId = "camera_info"
    val isActive = tooltipManager.isTooltipVisible(tooltipId)
    
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isActive) 
                    DarkBackground2.copy(alpha = 1f) 
                else 
                    DarkBackground2.copy(alpha = 0.8f)
            )
            .border(
                width = if (isActive) 2.dp else 0.dp,
                color = if (isActive) White.copy(alpha = 0.6f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { tooltipManager.toggleTooltip(tooltipId) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ⓘ",
            color = if (isActive) White else White.copy(alpha = 0.8f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Tooltip component that shows camera info (IR intensity and current mode) on click
 */
@Composable
fun CameraInfoTooltip(
    cameraControlViewModel: CameraControlViewModel,
    cameraLayoutViewModel: CameraLayoutViewModel,
    tooltipManager: TooltipManager,
    modifier: Modifier = Modifier
) {
    val tooltipId = "camera_info"
    val isTooltipVisible = tooltipManager.isTooltipVisible(tooltipId)
    val cameraControlState by cameraControlViewModel.cameraControlState.collectAsStateWithLifecycle()
    
    // Use applied values (what the camera is actually running), not pending UI selections
    val currentVisionMode by cameraLayoutViewModel.appliedVisionMode
    val currentCameraMode by cameraLayoutViewModel.appliedCameraMode

    // Format the mode string
    val modeString = remember(currentVisionMode, currentCameraMode) {
        buildModeString(currentVisionMode, currentCameraMode)
    }

    // Format IR intensity string
    val irIntensityString = when (cameraControlState.irIntensityLevel) {
        IrIntensityLevel.OFF -> "OFF"
        IrIntensityLevel.LOW -> "LOW"
        IrIntensityLevel.MEDIUM -> "MEDIUM"
        IrIntensityLevel.HIGH -> "HIGH"
        IrIntensityLevel.MAX -> "MAX"
        IrIntensityLevel.ULTRA -> "ULTRA"
    }

    // Auto-hide tooltip after 5 seconds
    LaunchedEffect(isTooltipVisible) {
        if (isTooltipVisible) {
            delay(5000) // 5 seconds
            tooltipManager.closeTooltip(tooltipId)
        }
    }

    // Fixed-size container to prevent layout shift
    Box(modifier = modifier) {
        // Clickable info icon/button - fixed size prevents movement
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DarkBackground2.copy(alpha = 0.8f))
                .clickable { tooltipManager.toggleTooltip(tooltipId) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ⓘ",
                color = White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Tooltip popup - positioned below button, right-aligned so button doesn't move
        AnimatedVisibility(
            visible = isTooltipVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 0.dp, y = 40.dp) // Position below button with consistent spacing
                .zIndex(10f)
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 180.dp, max = 220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        tooltipManager.closeTooltip(tooltipId)
                    }
                    .background(DarkBackground2.copy(alpha = 0.95f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // IR Intensity section
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "IR Intensity",
                        color = MediumLightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = irIntensityString,
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MediumLightGray.copy(alpha = 0.3f))
                )

                // Mode section
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Mode",
                        color = MediumLightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = modeString,
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Content display for camera info tooltip (icon and mode information)
 */
@Composable
fun CameraInfoContent(
    cameraControlViewModel: CameraControlViewModel,
    cameraLayoutViewModel: CameraLayoutViewModel,
    modifier: Modifier = Modifier
) {
    val cameraControlState by cameraControlViewModel.cameraControlState.collectAsStateWithLifecycle()
    val currentVisionMode by cameraLayoutViewModel.appliedVisionMode
    val currentCameraMode by cameraLayoutViewModel.appliedCameraMode
    
    val modeString = remember(currentVisionMode, currentCameraMode) {
        buildModeString(currentVisionMode, currentCameraMode)
    }
    
    val irIntensityString = when (cameraControlState.irIntensityLevel) {
        IrIntensityLevel.OFF -> "OFF"
        IrIntensityLevel.LOW -> "LOW"
        IrIntensityLevel.MEDIUM -> "MEDIUM"
        IrIntensityLevel.HIGH -> "HIGH"
        IrIntensityLevel.MAX -> "MAX"
        IrIntensityLevel.ULTRA -> "ULTRA"
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // IR Intensity section
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "IR Intensity",
                color = MediumLightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = irIntensityString,
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MediumLightGray.copy(alpha = 0.3f))
        )
        
        // Mode section
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Mode",
                color = MediumLightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = modeString,
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Content display for camera info with live data from camera
 * Polls camera API directly instead of using ViewModel cache
 */
@Composable
fun LiveCameraInfoContent(
    modifier: Modifier = Modifier,
    pollIntervalMs: Long = 2000L
) {
    val liveCameraData = rememberLiveCameraData(
        pollIntervalMs = pollIntervalMs,
        enabled = true
    )
    
    val modeString = remember(liveCameraData.visionMode, liveCameraData.cameraMode) {
        buildModeString(liveCameraData.visionMode, liveCameraData.cameraMode)
    }
    
    val irIntensityString = when (liveCameraData.irIntensityLevel) {
        IrIntensityLevel.OFF -> "OFF"
        IrIntensityLevel.LOW -> "LOW"
        IrIntensityLevel.MEDIUM -> "MEDIUM"
        IrIntensityLevel.HIGH -> "HIGH"
        IrIntensityLevel.MAX -> "MAX"
        IrIntensityLevel.ULTRA -> "ULTRA"
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Show loading indicator or error
        if (liveCameraData.isLoading && liveCameraData.error == null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = White.copy(alpha = 0.6f),
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Fetching...",
                    color = MediumLightGray,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
        
        if (liveCameraData.error != null) {
            Text(
                text = "Error: ${liveCameraData.error}",
                color = RecordRed,
                fontSize = 11.sp
            )
        }
        
        // IR Intensity section
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "IR Intensity",
                    color = MediumLightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                // Live indicator
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (!liveCameraData.isLoading && liveCameraData.error == null)
                                Color(0xFF00FF00)
                            else
                                Color.Gray
                        )
                )
            }
            Text(
                text = irIntensityString,
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MediumLightGray.copy(alpha = 0.3f))
        )
        
        // Mode section
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Mode",
                color = MediumLightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = modeString,
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Builds a formatted mode string based on vision mode and camera mode
 */
private fun buildModeString(visionMode: VisionMode, cameraMode: CameraMode): String {
    val visionPart = when (visionMode) {
        VisionMode.VISION -> "VISIBLE"
        VisionMode.INFRARED -> "IR"
        VisionMode.BOTH -> "LOWLIGHT"
    }

    val cameraPart = when (cameraMode) {
        CameraMode.OFF -> if (visionMode == VisionMode.BOTH) "COLOR" else ""
        CameraMode.EIS -> "EIS"
        CameraMode.HDR -> "HDR"
        CameraMode.BOTH -> "EIS+HDR"
        CameraMode.FOURK -> "4K"
    }

    return when {
        cameraPart.isEmpty() -> visionPart
        else -> "$visionPart+$cameraPart"
    }
}

