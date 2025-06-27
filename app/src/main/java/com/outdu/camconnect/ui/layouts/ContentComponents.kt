package com.outdu.camconnect.ui.layouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.components.buttons.CustomizableButton
import com.outdu.camconnect.ui.components.camera.*
import com.outdu.camconnect.ui.components.controls.*
import com.outdu.camconnect.ui.components.indicators.*
import com.outdu.camconnect.ui.components.settings.*
import com.outdu.camconnect.ui.models.*
import com.outdu.camconnect.ui.theme.*
import com.outdu.camconnect.ui.theme.AppColors.ButtonBgColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonBorderColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonIconColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonSelectedBgColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonSelectedIconColor
import com.outdu.camconnect.utils.MemoryManager
import kotlinx.coroutines.delay
import android.util.Log

/**
 * Full control content - comprehensive settings interface
 */
@Composable
fun FullControlContent(
    cameraState: CameraState,
    systemStatus: SystemStatus,
    detectionSettings: DetectionSettings,
    customButtons: List<ButtonConfig>,
    selectedTab: ControlTab,
    onTabSelected: (ControlTab) -> Unit,
    onAutoDayNightToggle: (Boolean) -> Unit,
    onVisionModeSelected: (VisionMode) -> Unit,
    onObjectDetectionToggle: (Boolean) -> Unit,
    onFarObjectDetectionToggle: (Boolean) -> Unit,
    onMotionDetectionToggle: (Boolean) -> Unit,
    onCameraModeSelected: (CameraMode) -> Unit,
    onOrientationModeSelected: (OrientationMode) -> Unit,
    onCollapseClick: () -> Unit
) {
    // Manage scroll state with proper cleanup
    val scrollState = rememberScrollState()
    
    // Cleanup when component is disposed
    DisposableEffect(Unit) {
        Log.d("FullControlContent", "Component created")
        onDispose {
            Log.d("FullControlContent", "Component disposed - cleaning up")
            try {
                MemoryManager.cleanupWeakReferences()
            } catch (e: Exception) {
                Log.e("FullControlContent", "Error during cleanup", e)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Row 1: Customizable 5-button row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            customButtons.take(5).forEach { buttonConfig ->
                if (buttonConfig.id != "Settings") {

                    CustomizableButton(
                        config =
                            if (buttonConfig.id == "collapse-screen") {
                                buttonConfig.copy(onClick = onCollapseClick)
                            } else {
                                buttonConfig
                            },
                        modifier = Modifier.weight(1f),
                        isCompact = false,
                        showText = false
                    )
                }

            }
        }

        // Row 2: Tab switcher
        ControlTabSwitcher(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.fillMaxWidth()
        )

        // Tab content
        when (selectedTab) {
            ControlTab.CAMERA_CONTROL -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Row 3: Display Settings
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground2)
                            .padding(16.dp)
                    ) {
                        DisplaySettingsSection(
                            isAutoDayNightEnabled = cameraState.isAutoDayNightEnabled,
                            onAutoDayNightToggle = onAutoDayNightToggle,
                            selectedVisionMode = cameraState.visionMode,
                            onVisionModeSelected = onVisionModeSelected
                        )
                    }

                    // Row 4: Detection Settings
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground2)
                            .padding(16.dp)
                    ) {
                        DetectionSettingsSection(
                            isObjectDetectionEnabled = detectionSettings.isObjectDetectionEnabled,
                            onObjectDetectionToggle = onObjectDetectionToggle,
                            isFarObjectDetectionEnabled = detectionSettings.isFarObjectDetectionEnabled,
                            onFarObjectDetectionToggle = onFarObjectDetectionToggle,
                            isMotionDetectionEnabled = detectionSettings.isMotionDetectionEnabled,
                            onMotionDetectionToggle = onMotionDetectionToggle
                        )
                    }

                    // Row 5: Image Settings
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground2)
                            .padding(16.dp)
                    ) {
                        ImageSettingsSection(
                            selectedCameraMode = cameraState.cameraMode,
                            onCameraModeSelected = onCameraModeSelected,
                            selectedOrientationMode = cameraState.orientationMode,
                            onOrientationModeSelected = onOrientationModeSelected
                        )
                    }
                }
            }

            ControlTab.AI_CONTROL -> {
                // Device control content placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground2)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Device icon placeholder
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // "Device Control" text placeholder
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .width(120.dp)
                                .background(Color.Black.copy(alpha = 0.3f))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Description text placeholder
                        Box(
                            modifier = Modifier
                                .height(16.dp)
                                .width(200.dp)
                                .background(Color.Gray.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            ControlTab.LICENSE_CONTROL -> {

            }
        }
    }
}

@Composable
fun DropInImage(
    imageRes: Int,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    imageSize: Dp = 80.dp
) {
    var visible by remember { mutableStateOf(false) }

    // Cleanup when component is disposed
    DisposableEffect(Unit) {
        Log.d("DropInImage", "Animation component created")
        onDispose {
            Log.d("DropInImage", "Animation component disposed")
            visible = false
        }
    }

    // Delay the visibility trigger for staggered effect
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight * 2 },
            animationSpec = tween(
                durationMillis = 800,
                easing = EaseOutBounce
            )
        ) + fadeIn(),
        exit = fadeOut()
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = modifier
                .size(imageSize)
                .padding(8.dp)
        )
    }
}

