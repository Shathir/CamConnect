package com.outdu.camconnect.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.components.buttons.CustomizableButton
import com.outdu.camconnect.ui.components.camera.CameraStreamView
import com.outdu.camconnect.ui.components.settings.*
import com.outdu.camconnect.ui.models.*
import com.outdu.camconnect.ui.theme.*

/**
 * Layout 3: Full Control Panel (Settings and Configuration)
 * Left Pane: 45% - Live camera stream display
 * Right Pane: 55% - In-depth device/camera configuration
 */
@Composable
fun FullControlLayout(
    cameraState: CameraState,
    systemStatus: SystemStatus,
    detectionSettings: DetectionSettings,
    customButtons: List<ButtonConfig>,
    selectedTab: ControlTab,
    onTabSelected: (ControlTab) -> Unit,
    onCustomButtonClick: (String) -> Unit,
    onAutoDayNightToggle: (Boolean) -> Unit,
    onVisionModeSelected: (VisionMode) -> Unit,
    onObjectDetectionToggle: (Boolean) -> Unit,
    onFarObjectDetectionToggle: (Boolean) -> Unit,
    onMotionDetectionToggle: (Boolean) -> Unit,
    onCameraModeSelected: (CameraMode) -> Unit,
    onOrientationModeSelected: (OrientationMode) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // Left Pane - Camera Stream (45%)
        Box(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
        ) {
            CameraStreamView(
                modifier = Modifier.fillMaxSize(),
                isConnected = systemStatus.isOnline,
                cameraName = "Camera ${cameraState.currentCamera + 1}",
                context = LocalContext.current
            )
        }
        
        // Right Pane - Full Settings (55%)
        Column(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
                .background(LightGrayBackground) // Light gray background
        ) {
            // Top bar with back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp)
                    .background(BluePrimary) // Blue primary color
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onBackClick() }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Back arrow placeholder
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.White, RoundedCornerShape(4.dp))
                        )
                    }
                    // "Settings" text placeholder
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .width(80.dp)
                            .padding(start = 8.dp)
                            .background(Color.White.copy(alpha = 0.8f))
                    )
                }
            }
            
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Row 1: Customizable 5-button row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    customButtons.take(5).forEach { buttonConfig ->
                        CustomizableButton(
                            config = buttonConfig,
                            modifier = Modifier.weight(1f),
                            isCompact = false,
                            showText = true
                        )
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
                                    .background(LightGray)
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
                                    .background(LightGray)
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
                                    .background(LightGray)
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
                    
                    ControlTab.DEVICE_CONTROL -> {
                        // Device control content placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightGray)
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
                }
            }
        }
    }
}



@Composable
fun CustomToggleButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) BlueVariant else DarkGray2)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CustomSelectableButton(
    label: String,
    isSelected: Boolean,
    selectedColor: Color = Color.Red,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) selectedColor else DarkGray2)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

class CameraControlViewModel : ViewModel() {
    var autoDayNight by mutableStateOf(true)
    var displayMode by mutableStateOf("Visible")
    var objectDetection by mutableStateOf(true)
    var detectFarObjects by mutableStateOf(true)
    var motionDetection by mutableStateOf(true)
    var captureMode by mutableStateOf("EIS")
    var orientation by mutableStateOf("Flip")
}

@Composable
fun CameraControlScreen(viewModel: CameraControlViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {

        Text("Camera Control", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(16.dp))

        SettingRow("Auto Day/Night") {
            CustomToggleButton(
                "ON", viewModel.autoDayNight,
                onClick = { viewModel.autoDayNight = true}
            )
            Spacer(Modifier.width(8.dp))
            CustomToggleButton("OFF", !viewModel.autoDayNight,
                onClick = {viewModel.autoDayNight = false}
            )
        }

        SettingRow("Display Modes") {
            CustomToggleButton("Visible", viewModel.displayMode == "Visible",
                onClick =  {
                    viewModel.displayMode = "Visible"
                }
            )
            Spacer(Modifier.width(8.dp))
            CustomSelectableButton("Infra Red", viewModel.displayMode == "Infra Red",
                onClick = {
                    viewModel.displayMode = "Infra Red"
                }
            )
            Spacer(Modifier.width(8.dp))
            CustomToggleButton("Auto", viewModel.displayMode == "Auto",
                onClick = {
                    viewModel.displayMode = "Auto"
                }
            )
        }

//        SettingRow("Object Detection") {
//            CustomToggleButton("ON", viewModel.objectDetection) {
//                viewModel.objectDetection = true
//            }
//            Spacer(Modifier.width(8.dp))
//            CustomToggleButton("OFF", !viewModel.objectDetection) {
//                viewModel.objectDetection = false
//            }
//        }
//
//        SettingRow("Detect Far Away Objects") {
//            CustomToggleButton("YES", viewModel.detectFarObjects) {
//                viewModel.detectFarObjects = true
//            }
//            Spacer(Modifier.width(8.dp))
//            CustomToggleButton("NO", !viewModel.detectFarObjects) {
//                viewModel.detectFarObjects = false
//            }
//        }
//
//        SettingRow("Motion Detection") {
//            CustomToggleButton("YES", viewModel.motionDetection) {
//                viewModel.motionDetection = true
//            }
//            Spacer(Modifier.width(8.dp))
//            CustomToggleButton("NO", !viewModel.motionDetection) {
//                viewModel.motionDetection = false
//            }
//        }
//
//        SettingRow("Camera Capture") {
//            CustomToggleButton("EIS", viewModel.captureMode == "EIS") {
//                viewModel.captureMode = "EIS"
//            }
//            Spacer(Modifier.width(8.dp))
//            CustomToggleButton("HDR", viewModel.captureMode == "HDR") {
//                viewModel.captureMode = "HDR"
//            }
//        }
//
//        SettingRow("Orientation") {
//            CustomToggleButton("Flip Vertical", viewModel.orientation == "Flip") {
//                viewModel.orientation = "Flip"
//            }
//            Spacer(Modifier.width(8.dp))
//            CustomToggleButton("Mirror", viewModel.orientation == "Mirror") {
//                viewModel.orientation = "Mirror"
//            }
//        }
    }
}

@Composable
fun SettingRow(label: String, content: @Composable RowScope.() -> Unit) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(text = label, color = Color.White, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Row(content = content)
    }
}





