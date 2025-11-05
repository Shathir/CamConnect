package com.outdu.camconnect.ui.components.settings.network

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outdu.camconnect.R
import com.outdu.camconnect.communication.MotocamAPIAndroidHelper.getDeviceModeAsync
import com.outdu.camconnect.ui.theme.AppColors.ButtonBorderColor
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import com.outdu.camconnect.ui.viewmodels.HotspotConfiguration
import com.outdu.camconnect.ui.viewmodels.NetworkConfigurationViewModel
import com.outdu.camconnect.ui.viewmodels.WifiConfiguration


@Composable
fun NetworkLayout() {
    val scope = rememberCoroutineScope()
    var deviceMode by remember { mutableStateOf(DeviceMode.HOTSPOT) }
    val networkConfigurationViewModel: NetworkConfigurationViewModel = viewModel()

    // Collect both states at parent level to avoid re-subscription on mode switch
    val hotspotConfiguration by networkConfigurationViewModel.hotspotState.collectAsStateWithLifecycle()
    val wifiConfiguration by networkConfigurationViewModel.wifiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        initializeDeviceMode(
            scope = scope,
            networkConfigurationViewModel = networkConfigurationViewModel,
            onModeDetermined = { mode -> deviceMode = mode }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {

        ModeButtonRow(
            deviceMode = deviceMode,
            onModeChange = { deviceMode = it }
        )

        // Use key to help Compose track state properly and avoid unnecessary recomposition
        key(deviceMode) {
            when (deviceMode) {
                DeviceMode.HOTSPOT -> {
                    HotspotLayout(
                        networkConfigurationViewModel = networkConfigurationViewModel,
                        hotspotConfiguration = hotspotConfiguration
                    )
                }

                DeviceMode.DEVICE -> {
                    DeviceLayout(
                        networkConfigurationViewModel = networkConfigurationViewModel,
                        wifiConfiguration = wifiConfiguration
                    )
                }
            }
        }
    }
}

@Composable
fun ModeButtonRow(
    deviceMode: DeviceMode,
    onModeChange: (DeviceMode) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ModeButton(
            text = "Hotspot Mode",
            isSelected = deviceMode == DeviceMode.HOTSPOT,
            onClick = { if (deviceMode != DeviceMode.HOTSPOT) onModeChange(DeviceMode.HOTSPOT) },
            isDarkTheme = isDarkTheme
        )
        ModeButton(
            text = "Device Mode",
            isSelected = deviceMode == DeviceMode.DEVICE,
            onClick = { if (deviceMode != DeviceMode.DEVICE) onModeChange(DeviceMode.DEVICE) },
            isDarkTheme = isDarkTheme
        )
    }
}

@Composable
private fun ModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val backgroundColor = getModeButtonBackgroundColor(isSelected, isDarkTheme)
    val textColor = if (isSelected) Color.White else Color(0xFF1A1A1C)
    val borderColor = if (isSelected) StravionBlue else ButtonBorderColor
    
    Box(
        modifier = Modifier
//            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isDarkTheme) 0.dp else 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight(500),
                fontFamily = FontFamily(Font(R.font.arial_regular)),
                color = textColor
            )
        )
    }
}

@Composable
private fun getModeButtonBackgroundColor(isSelected: Boolean, isDarkTheme: Boolean): Color {
    return when {
        isSelected && isDarkTheme -> Color(0xFF515151)
        isSelected && !isDarkTheme -> StravionBlue
        !isSelected && isDarkTheme -> Color(0xFF333333)
        else -> Color(0xFFFFFFFF)
    }
}

@Composable
fun HotspotLayout(
    networkConfigurationViewModel: NetworkConfigurationViewModel,
    hotspotConfiguration: HotspotConfiguration
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDarkTheme = isSystemInDarkTheme()
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val labelColor = getLabelColor(isDarkTheme)
    val titleTextStyle = getTitleTextStyle(labelColor)
    val descriptionTextStyle = getDescriptionTextStyle(labelColor)

    Text(
        text = "Configure network settings to create a Wi-Fi hotspot hosted by your camera",
        style = descriptionTextStyle
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        LabeledTextField(
            label = "SSID",
            value = hotspotConfiguration.hotspot_ssid,
            onValueChange = networkConfigurationViewModel::updateHotspotSSID,
            textStyle = titleTextStyle,
            keyboardType = KeyboardType.Text,
            config = TextFieldConfig(
                isError = hotspotConfiguration.hotspot_ssid.isEmpty() && errorMessage.isNotEmpty()
            )
        )

        LabeledTextField(
            label = "Password",
            value = hotspotConfiguration.hotspot_password,
            onValueChange = networkConfigurationViewModel::updateHotspotPassword,
            textStyle = titleTextStyle,
            keyboardType = KeyboardType.Password,
            config = TextFieldConfig(
                isError = hotspotConfiguration.hotspot_password.isEmpty() && errorMessage.isNotEmpty(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    PasswordVisibilityToggle(
                        passwordVisible = passwordVisible,
                        onToggle = { passwordVisible = !passwordVisible }
                    )
                }
            )
        )

        LabeledTextField(
            label = "IP Address",
            value = hotspotConfiguration.hotspot_ip_address,
            onValueChange = networkConfigurationViewModel::updateHotspotIPAddress,
            textStyle = titleTextStyle,
            keyboardType = KeyboardType.Text,
            config = TextFieldConfig(
                isError = hotspotConfiguration.hotspot_ip_address.isEmpty() && errorMessage.isNotEmpty(),
                enabled = false
            )
        )

        LabeledTextField(
            label = "Subnet Mask",
            value = hotspotConfiguration.hotspot_subnet_mask,
            onValueChange = networkConfigurationViewModel::updateHotspotSubnetMask,
            textStyle = titleTextStyle,
            keyboardType = KeyboardType.Text,
            config = TextFieldConfig(
                isError = hotspotConfiguration.hotspot_subnet_mask.isEmpty() && errorMessage.isNotEmpty(),
                enabled = false
            )
        )

        ActionButton(text = "Start Hosting") {
            networkConfigurationViewModel.setHotspotMode(
                scope = scope,
                onSuccess = {
                    Toast.makeText(
                        context,
                        "Hotspot mode started successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onError = { error ->
                    Toast.makeText(
                        context,
                        "Failed to start hotspot: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }
}

private data class TextFieldConfig(
    val isError: Boolean = false,
    val enabled: Boolean = true,
    val visualTransformation: VisualTransformation = VisualTransformation.None,
    val trailingIcon: @Composable (() -> Unit)? = null
)

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    textStyle: TextStyle,
    keyboardType: KeyboardType,
    config: TextFieldConfig = TextFieldConfig()
) {
    Text(text = label, style = textStyle)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Enter $label") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        isError = config.isError,
        enabled = config.enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        visualTransformation = config.visualTransformation,
        trailingIcon = config.trailingIcon
    )
}

@Composable
private fun PasswordVisibilityToggle(
    passwordVisible: Boolean,
    onToggle: () -> Unit
) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
            contentDescription = if (passwordVisible) "Hide password" else "Show password"
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(StravionBlue)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily(Font(R.font.arial_regular)),
                color = Color.White
            )
        )
    }
}


@Composable
fun DeviceLayout(
    networkConfigurationViewModel: NetworkConfigurationViewModel,
    wifiConfiguration: WifiConfiguration
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDarkTheme = isSystemInDarkTheme()
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val labelColor = getLabelColor(isDarkTheme)
    val titleTextStyle = getTitleTextStyle(labelColor)
    val descriptionTextStyle = getDescriptionTextStyle(labelColor)

    Text(
        text = "Configure network settings to connect your camera to an existing Wi-Fi network",
        style = descriptionTextStyle
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        DynamicIpToggleButton(
            isEnabled = wifiConfiguration.dynamicIpEnabled,
            onToggle = { networkConfigurationViewModel.updateDynamicIpEnabled(!wifiConfiguration.dynamicIpEnabled) },
            isDarkTheme = isDarkTheme
        )
        Spacer(modifier = Modifier.height(8.dp))

        LabeledTextField(
            label = "SSID",
            value = wifiConfiguration.wifi_ssid,
            onValueChange = networkConfigurationViewModel::updateWifiSSID,
            textStyle = titleTextStyle,
            keyboardType = KeyboardType.Text,
            config = TextFieldConfig(
                isError = wifiConfiguration.wifi_ssid.isEmpty() && errorMessage.isNotEmpty()
            )
        )

        LabeledTextField(
            label = "Password",
            value = wifiConfiguration.wifi_password,
            onValueChange = networkConfigurationViewModel::updateWifiPassword,
            textStyle = titleTextStyle,
            keyboardType = KeyboardType.Password,
            config = TextFieldConfig(
                isError = wifiConfiguration.wifi_password.isEmpty() && errorMessage.isNotEmpty(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    PasswordVisibilityToggle(
                        passwordVisible = passwordVisible,
                        onToggle = { passwordVisible = !passwordVisible }
                    )
                }
            )
        )

        if (wifiConfiguration.dynamicIpEnabled) {
            StaticIpConfigurationFields(
                wifiConfiguration = wifiConfiguration,
                networkConfigurationViewModel = networkConfigurationViewModel,
                errorMessage = errorMessage,
                titleTextStyle = titleTextStyle
            )
        }

        ActionButton(text = "Connect to Network") {
            networkConfigurationViewModel.setClientMode(
                scope = scope,
                onSuccess = {
                    Toast.makeText(
                        context,
                        "Connected to network successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onError = { error ->
                    Toast.makeText(
                        context,
                        "Failed to connect: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }
}

@Composable
fun DynamicIpToggleButton(
    isEnabled: Boolean,
    onToggle: () -> Unit,
    isDarkTheme: Boolean
) {
    val backgroundColor = getToggleButtonBackgroundColor(isEnabled, isDarkTheme)
    val borderColor = if (isEnabled) StravionBlue else ButtonBorderColor
    val textColor = if (isEnabled) Color.White else Color(0xFF1A1A1C)
    val buttonText = if (isEnabled) "Dynamic IP Enabled" else "Dynamic IP Disabled"
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isDarkTheme) 0.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .background(backgroundColor)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = buttonText,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight(500),
                fontFamily = FontFamily(Font(R.font.arial_regular)),
                color = textColor
            )
        )
    }
}

@Composable
private fun getToggleButtonBackgroundColor(isEnabled: Boolean, isDarkTheme: Boolean): Color {
    return when {
        isEnabled && isDarkTheme -> Color(0xFF515151)
        isEnabled && !isDarkTheme -> StravionBlue
        !isEnabled && isDarkTheme -> Color(0xFF333333)
        else -> Color(0xFFFFFFFF)
    }
}

@Composable
private fun getLabelColor(isDarkTheme: Boolean): Color {
    return if (isDarkTheme) Color.White else Color(0xFF1A1A1C)
}

@Composable
private fun getTitleTextStyle(labelColor: Color): TextStyle {
    return TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily(Font(R.font.arial_regular)),
        color = labelColor
    )
}

@Composable
private fun getDescriptionTextStyle(labelColor: Color): TextStyle {
    return TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily(Font(R.font.arial_regular)),
        color = labelColor
    )
}

private suspend fun initializeDeviceMode(
    scope: kotlinx.coroutines.CoroutineScope,
    networkConfigurationViewModel: NetworkConfigurationViewModel,
    onModeDetermined: (DeviceMode) -> Unit
) {
    getDeviceModeAsync(
        scope = scope,
        callback = { mode, error ->
            if (error != null) {
                return@getDeviceModeAsync
            }

            val determinedMode = when (mode) {
                DeviceMode.HOTSPOT.value -> DeviceMode.HOTSPOT
                DeviceMode.DEVICE.value -> DeviceMode.DEVICE
                else -> DeviceMode.HOTSPOT
            }
            
            onModeDetermined(determinedMode)
            
            when (determinedMode) {
                DeviceMode.HOTSPOT -> networkConfigurationViewModel.loadHotspotConfiguration()
                DeviceMode.DEVICE -> networkConfigurationViewModel.loadWifiConfiguration()
            }
        }
    )
}

@Composable
private fun StaticIpConfigurationFields(
    wifiConfiguration: WifiConfiguration,
    networkConfigurationViewModel: NetworkConfigurationViewModel,
    errorMessage: String,
    titleTextStyle: TextStyle
) {
    LabeledTextField(
        label = "IP Address",
        value = wifiConfiguration.wifi_ip_address,
        onValueChange = networkConfigurationViewModel::updateWifiIPAddress,
        textStyle = titleTextStyle,
        keyboardType = KeyboardType.Text,
        config = TextFieldConfig(
            isError = wifiConfiguration.wifi_ip_address.isEmpty() && errorMessage.isNotEmpty()
        )
    )

    LabeledTextField(
        label = "Subnet Mask",
        value = wifiConfiguration.wifi_subnet_mask,
        onValueChange = networkConfigurationViewModel::updateWifiSubnetMask,
        textStyle = titleTextStyle,
        keyboardType = KeyboardType.Text,
        config = TextFieldConfig(
            isError = wifiConfiguration.wifi_subnet_mask.isEmpty() && errorMessage.isNotEmpty()
        )
    )
}

enum class DeviceMode(
    val displayName: String,
    val value: String
) {
    HOTSPOT("Hotspot Mode", "Hotspot"),
    DEVICE("Device Mode", "Client")
}
