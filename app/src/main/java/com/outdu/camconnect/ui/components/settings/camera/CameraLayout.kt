package com.outdu.camconnect.ui.components.settings.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Auto Day/ Night",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 14.02.sp,
                        fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFFFFFFFF),

                        )
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Orientation",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 14.02.sp,
                        fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFFFFFFFF),

                        )
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Display Mode",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 14.02.sp,
                        fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFFFFFFFF),

                        )
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Camera Capture",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 14.02.sp,
                        fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFFFFFFFF),

                        )
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            Text(
                text = "To Activate Zoom Control, Disable WDR & EIS",
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 10.51.sp,
                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                    fontWeight = FontWeight(500),
                    color = Color(0xFFFFFFFF),

                    )
            )
        }

    }
}