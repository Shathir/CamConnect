package com.outdu.camconnect.ui.components.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.outdu.camconnect.ui.theme.White
import com.outdu.camconnect.ui.viewmodels.CameraControlViewModel
import com.outdu.camconnect.ui.viewmodels.IrIntensityLevel

/**
 * Tooltip component that shows camera info (IR intensity and current mode) on click
 */
@Composable
fun CameraInfoTooltip(
    cameraControlViewModel: CameraControlViewModel,
    cameraLayoutViewModel: CameraLayoutViewModel,
    modifier: Modifier = Modifier
) {
    var isTooltipVisible by remember { mutableStateOf(false) }
    val cameraControlState by cameraControlViewModel.cameraControlState.collectAsStateWithLifecycle()
    
    // Access State objects using by delegation - Compose will track them for recomposition
    val currentVisionMode by cameraLayoutViewModel.currentVisionMode
    val currentCameraMode by cameraLayoutViewModel.currentCameraMode

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
            isTooltipVisible = false
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
                .clickable { isTooltipVisible = !isTooltipVisible },
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
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 0.dp, y = 36.dp) // Position below button
                .zIndex(10f)
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 180.dp, max = 220.dp)
                    .clip(RoundedCornerShape(12.dp))
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
        CameraMode.HDR -> "WDR"
        CameraMode.BOTH -> "EIS+WDR"
        CameraMode.FOURK -> "4K"
    }

    return when {
        cameraPart.isEmpty() -> visionPart
        else -> "$visionPart+$cameraPart"
    }
}

