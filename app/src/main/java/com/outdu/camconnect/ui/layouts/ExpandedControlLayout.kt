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
import com.outdu.camconnect.communication.MotocamAPIAndroidHelper
import com.outdu.camconnect.communication.MotocamAPIHelper
import com.outdu.camconnect.communication.MotocamSocketClient
import com.outdu.camconnect.network.HttpClientProvider
import com.outdu.camconnect.ui.components.buttons.ScreenRecorderUI
import com.outdu.camconnect.ui.components.buttons.ZoomSelector
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.launch


/**
 * Expanded control content - scrollable with multiple control sections
 */
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
    // Manage scroll state with proper cleanup
    val scrollState = rememberScrollState()

    // Cleanup when component is disposed
    DisposableEffect(Unit) {
        Log.d("ExpandedControlContent", "Component created")
        onDispose {
            Log.d("ExpandedControlContent", "Component disposed - cleaning up")
            // Clear any button states that might be lingering
            try {
                MemoryManager.cleanupWeakReferences()
            } catch (e: Exception) {
                Log.e("ExpandedControlContent", "Error during cleanup", e)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {
            // Row 1: Customizable 5-button row with state management
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                customButtons.take(5).forEach { buttonConfig ->
                    // Get current selection state for this button
                    val isSelected = buttonStates[buttonConfig.id] ?: false

                    CustomizableButton(
                        config = when (buttonConfig.id) {
                            "Settings" -> buttonConfig.copy(onClick = onSettingsClick,
                                BorderColor = ButtonBorderColor,
                                backgroundColor = if(isSelected) ButtonSelectedBgColor else ButtonBgColor,
                                color = if(isSelected) ButtonSelectedIconColor else ButtonIconColor
                            )
                            "collapse-screen" -> buttonConfig.copy(onClick = onCollapseClick,
                                BorderColor = ButtonBorderColor,
                                backgroundColor = if(isSelected) ButtonSelectedBgColor else ButtonBgColor,
                                color = if(isSelected) ButtonSelectedIconColor else ButtonIconColor
                            )
                            "ir" -> {
                                val coroutineScope = rememberCoroutineScope()
                                val isLoading = remember { mutableStateOf(false) }

                                buttonConfig.copy(


                                    BorderColor = if(isSelected) RecordRed else ButtonBorderColor,
                                    backgroundColor = if(isSelected) RecordRed else ButtonBgColor,
                                    color = if(isSelected) ButtonSelectedIconColor else ButtonIconColor,
                                    onClick = {
                                    if (isLoading.value) return@copy // avoid duplicate clicks
                                    
                                    // Set loading state
                                    isLoading.value = true
                                    
                                    // Toggle logic: if currently selected (ON), turn OFF, and vice versa
                                    val flip = if(isSelected) MotocamAPIHelper.FLIP.OFF else MotocamAPIHelper.FLIP.ON
                                    
                                    MotocamAPIAndroidHelper.setFlipAsync(
                                        scope = coroutineScope,
                                        flip = flip
                                    ) { result, error ->
                                        // Reset loading state
                                        isLoading.value = false
                                        
                                        if(error != null) {
                                            Log.e("UI", "IR Flip Error: $error")
                                            // Optionally show error to user or handle error state
                                        } else {
                                            Log.i("UI", "IR Flip Result: $result")
                                            // Update button state only on successful API call
                                            if (result) {
                                                buttonStates[buttonConfig.id] = !isSelected
                                            }
                                        }
                                    }
                                })}
                            else -> {
                                // Create dynamic button config based on selection state
                                buttonConfig.copy(
                                    backgroundColor = if (isSelected) {
                                        // Active state - brighter background
                                            ButtonSelectedBgColor
                                    } else {
                                        // Inactive state - default background
                                        ButtonBgColor
                                    },
                                    color = if (isSelected) ButtonSelectedIconColor else ButtonIconColor,
                                    BorderColor = if (isSelected) {
                                        // Active state - brighter background
                                            ButtonBorderColor
                                    } else {
                                        // Inactive state - default background
                                        ButtonBorderColor
                                    },
                                    onClick = {
                                        // Toggle button state
                                        buttonStates[buttonConfig.id] = !isSelected
                                        // Call original onClick if needed
                                        buttonConfig.onClick()
                                    }
                                )
                            }
                        },

                        modifier = Modifier.weight(1f),
                        isCompact = false,
                        showText = false
                    )
                }
            }

            // Row 2: Status Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recording button with dark theme
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .weight(2f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (cameraState.isRecording) RecordRed else MediumDarkBackground
                        )
                        .clickable { onRecordingToggle() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center

                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Record dot indicator
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (cameraState.isRecording) White else RedVariant
                                )
                        )
                        // Record text
                        Text(
                            text = "RECORD",
                            color = if (cameraState.isRecording) White else MediumLightGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                var currentZoom by remember { mutableFloatStateOf(1f) }
                // Zoom selector with dark theme
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSlate)
                        .clickable { /* Handle zoom click */ }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    ZoomSelector(
                        initialZoom = currentZoom,
                        onZoomChanged = { zoom ->
                            var zoomlevel = MotocamAPIHelper.ZOOM.X1
                            when(zoom)
                            {
                                1f -> zoomlevel = MotocamAPIHelper.ZOOM.X1
                                2f -> zoomlevel = MotocamAPIHelper.ZOOM.X2
                                else -> zoomlevel = MotocamAPIHelper.ZOOM.X4
                            }
                            MotocamAPIAndroidHelper.setZoomAsync(
                                scope = coroutineScope,
                                zoom = zoomlevel
                            )
                            {result, error ->
                                if(error != null) {
                                    Log.e("UI", "Zoom Error: $error")
                                    // Optionally show error to user or handle error state
                                } else {
                                    currentZoom = zoom
                                    Log.d("Zoom", "Zoom set to $zoom X")
                                    // Trigger zoom in your camera pipeline here
                                    Log.i("Zoom", "Zoom Result: $result")
                                }
                            }
                        }
                    )
                }
            }

            // Row 3: 6 toggleable icons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBackground2)
                    .padding(1.dp)

            ) {
                ToggleableIconRow(
                    icons = toggleableIcons,
                    onToggle = onIconToggle
                )
            }

            // Row 4: Compass component
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(Color(0xFFE0E0E0))
//                    .padding(16.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                )
//                {
//                toggleableIcons.forEachIndexed { index, icon ->
//
//                        DropInImage(
//                            imageRes = icon.iconPlaceholder,
//                            delayMillis = index * 300,
//                            imageSize = 40.dp
//                        )
//                    }
//                }
//            }

            // Row 5: Video feed and snapshot slots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
//                VideoFeedSlot(
//                    modifier = Modifier.weight(1f),
//                    label = "Rear Camera"
//                )
                SnapshotSlot(
                    modifier = Modifier.weight(1f),
                    hasSnapshot = false,
                    onSpeedUpdate = onSpeedUpdate
                )
//                ScreenRecorderUI(context = LocalContext.current)
            }

            // Row 6: Status information
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(Color(0xFFE0E0E0))
//                    .padding(12.dp)
//            ) {
////                Column(
////                    verticalArrangement = Arrangement.spacedBy(8.dp)
////                ) {
//                    // Speed indicator
////                    SpeedIndicator(
////                        speed = systemStatus.currentSpeed,
////                        modifier = Modifier.fillMaxWidth()
////                    )
//
//                    // Status indicators in a row
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceEvenly
//                    ) {
//                        SpeedIndicator(
//                            speed = systemStatus.currentSpeed,
////                            modifier = Modifier.fillMaxWidth()
//                        )
//                        BatteryIndicator(
//                            batteryLevel = systemStatus.batteryLevel,
//                            showPercentage = true
//                        )
//                        WifiIndicator(isConnected = systemStatus.isWifiConnected)
////                        OnlineIndicator(isOnline = systemStatus.isOnline)
//                        AiStatusIndicator(isEnabled = systemStatus.isAiEnabled)
//                    }
////                }
//            }

        }
    }
}
