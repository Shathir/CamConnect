package com.outdu.camconnect.ui.layouts

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass.Device
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.components.buttons.CustomizableButton
import com.outdu.camconnect.ui.components.camera.CameraStreamView
import com.outdu.camconnect.ui.components.settings.*
import com.outdu.camconnect.ui.components.settings.ai.AiLayout
import com.outdu.camconnect.ui.components.settings.camera.CameraLayout
import com.outdu.camconnect.ui.components.settings.license.CameraInfoCard
import com.outdu.camconnect.ui.components.settings.license.LicenseLayout
import com.outdu.camconnect.ui.models.*
import com.outdu.camconnect.ui.theme.*
import com.outdu.camconnect.ui.theme.AppColors.ButtonBgColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonBorderColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonSelectedBgColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonSelectedIconColor
import com.outdu.camconnect.utils.MemoryManager
import android.util.Log
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outdu.camconnect.ui.components.recording.RecordingTimer
import com.outdu.camconnect.ui.viewmodels.RecordingViewModel
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType


/**
 * Layout 3: Full Control Panel (Settings and Configuration)
 * Left Pane: 45% - Live camera stream display
 * Right Pane: 55% - In-depth device/camera configuration
 */
@Composable
fun SettingsControlLayout(
    selectedTab: ControlTab,
    onTabSelected: (ControlTab) -> Unit,
    onSystemStatusChange: (SystemStatus) -> Unit,
    systemStatus: SystemStatus,
    modifier: Modifier = Modifier,
    onCollapseClick: () -> Unit
) {
    // Manage scroll state with proper cleanup
    val scrollState = rememberScrollState()
    val deviceType = rememberDeviceType()
    
    // Cleanup when component is disposed
    DisposableEffect(Unit) {
        Log.d("SettingsControlLayout", "Component created")
        onDispose {
            Log.d("SettingsControlLayout", "Component disposed - cleaning up")
            try {
                MemoryManager.cleanupWeakReferences()
            } catch (e: Exception) {
                Log.e("SettingsControlLayout", "Error during cleanup", e)
            }
        }
    }

    // Right Pane - Full Settings (55%)
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxSize()
            .background(Color.Transparent), // Light gray background
        verticalArrangement = Arrangement.spacedBy(if(deviceType == DeviceType.TABLET)24.dp else 12.dp)
    ) {

        // Row 2: Tab switcher
        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            CustomizableButton(
                config = ButtonConfig(
                    id = "settings-screen",
                    iconPlaceholder = R.drawable.sliders_horizontal.toString(),
                    color = ButtonSelectedIconColor,
                    text = "Camera",
                    backgroundColor = ButtonSelectedBgColor,
                    BorderColor = ButtonBorderColor,
                    onClick = onCollapseClick
                ),
                modifier = Modifier,
                isCompact = true,
                showText = false
            )

            ControlTabSwitcher(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.weight(1f)
            )

            CustomizableButton(
                config = ButtonConfig(
                    id = "logout",
                    iconPlaceholder = R.drawable.signout.toString(),
                    color = Color.Red,
                    text = "Camera",
                    backgroundColor = ButtonBgColor,
                    BorderColor = ButtonBorderColor,
                    enabled = false
                ),
                modifier = Modifier,
                isCompact = true,
                showText = false
            )
        }

        // Scrollable content with managed scroll state
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(if(deviceType == DeviceType.TABLET)20.dp else 12.dp)
        ) {
            // Tab content with proper keying for memory management
            key(selectedTab) {
                when (selectedTab) {
                    ControlTab.CAMERA_CONTROL -> {
                        // Using the new CameraLayout with ViewModel
                        CameraLayout()
                    }

                    ControlTab.AI_CONTROL -> {
                        // Device control content placeholder
                        AiLayout(
                            systemStatus = systemStatus,
                            onSystemStatusChange = onSystemStatusChange
                        )
                    }

                    ControlTab.LICENSE_CONTROL -> {
                        LicenseLayout()
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
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
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

@Composable
fun SettingRow(label: String, content: @Composable RowScope.() -> Unit) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(text = label, color = Color.White, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Row(content = content)
    }
}

@Composable
fun FullControlLayout(
    onCollapseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Your existing full control layout content
    // ... existing code ...
}





