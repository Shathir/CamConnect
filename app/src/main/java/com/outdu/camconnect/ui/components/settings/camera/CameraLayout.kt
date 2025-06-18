package com.outdu.camconnect.ui.components.settings.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.components.settings.DetectionSettingsSection
import com.outdu.camconnect.ui.components.settings.DisplaySettingsSection
import com.outdu.camconnect.ui.components.settings.ImageSettingsSection
import com.outdu.camconnect.ui.models.CameraMode
import com.outdu.camconnect.ui.models.CameraState
import com.outdu.camconnect.ui.models.DetectionSettings
import com.outdu.camconnect.ui.models.OrientationMode
import com.outdu.camconnect.ui.models.SystemStatus
import com.outdu.camconnect.ui.models.VisionMode
import com.outdu.camconnect.ui.theme.LightGray

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
    // This is a placeholder for the camera layout.
    // You can add your camera-related settings or information here.
    // For now, it will just display a simple message.
    // Replace this with your actual camera settings UI.

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