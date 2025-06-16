package com.outdu.camconnect.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.ui.models.CameraMode
import com.outdu.camconnect.ui.models.OrientationMode
import com.outdu.camconnect.ui.models.VisionMode
import com.outdu.camconnect.ui.theme.*

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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkBackground2),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ControlTab.values().forEach { tab ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected(tab) }
                    .background(
                        if (selectedTab == tab) MediumDarkBackground
                        else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Tab text placeholder
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .width((tab.displayName.length * 7).dp)
//                        .background(
//                            if (selectedTab == tab) Color.White.copy(alpha = 0.8f)
//                            else Color.Black.copy(alpha = 0.3f)
//                        )
                )
                {
                    Text(
                        text = tab.displayName,
                        color = if (selectedTab == tab) Color.White
                        else MediumGray3,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

            }
        }
    }
}

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
    CAMERA_CONTROL("Camera Control"),
    DEVICE_CONTROL("Device Control")
} 