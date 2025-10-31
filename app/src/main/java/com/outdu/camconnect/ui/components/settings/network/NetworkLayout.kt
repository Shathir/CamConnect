package com.outdu.camconnect.ui.components.settings.network

import android.bluetooth.BluetoothClass.Device
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outdu.camconnect.R
import com.outdu.camconnect.communication.MotocamAPIAndroidHelper.getDeviceModeAsync
import com.outdu.camconnect.ui.theme.AppColors.ButtonBorderColor
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import com.outdu.camconnect.ui.viewmodels.NetworkConfigurationViewModel


@Composable
fun NetworkLayout() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var deviceMode by remember { mutableStateOf(DeviceMode.HOTSPOT) }
    val networkConfigurationViewModel: NetworkConfigurationViewModel = viewModel()
    LaunchedEffect(Unit) {
        getDeviceModeAsync(
            scope = scope,
            callback = { mode, _ ->

                when (mode) {
                    DeviceMode.HOTSPOT.value -> {
                        deviceMode = DeviceMode.HOTSPOT
                    }

                    DeviceMode.DEVICE.value -> {
                        deviceMode = DeviceMode.DEVICE
                    }
                }
            }
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

        if (deviceMode.value == DeviceMode.HOTSPOT.value) {
            HotspotLayout(
                networkConfigurationViewModel
            )
        } else {
            DeviceLayout(
                networkConfigurationViewModel
            )
        }
    }
}

@Composable
fun DeviceModeCard(
    deviceMode: DeviceMode
) {
    Text(
        text = "Device Mode : ${deviceMode.value}",
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight(500),
            fontFamily = FontFamily(Font(R.font.arial_regular)),
            color = Color(0xFF1A1A1C)
        )
    )
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
    )
    {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(112.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isDarkTheme) 0.dp else 2.dp, // No border in dark theme
                    color = if (deviceMode.value == DeviceMode.HOTSPOT.value) StravionBlue else ButtonBorderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(
                    if (deviceMode.value == DeviceMode.HOTSPOT.value) if (isDarkTheme) Color(
                        0xFF515151
                    ) else StravionBlue
                    else if (isDarkTheme) Color(0xFF333333) else Color(0xFFFFFFFF)
                )
                .clickable {
                    if (deviceMode.value == DeviceMode.HOTSPOT.value) return@clickable
                    onModeChange(DeviceMode.HOTSPOT)
                },
            contentAlignment = Alignment.Center
        )
        {
            Text(
                text = "Hotspot Mode",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight(500),
                    fontFamily = FontFamily(Font(R.font.arial_regular)),
                    color = if (deviceMode.value == DeviceMode.HOTSPOT.value) Color.White else Color(
                        0xFF1A1A1C
                    )
                )
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(112.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isDarkTheme) 0.dp else 2.dp, // No border in dark theme
                    color = if (deviceMode.value == DeviceMode.DEVICE.value) StravionBlue else ButtonBorderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(
                    if (deviceMode.value == DeviceMode.DEVICE.value) if (isDarkTheme) Color(
                        0xFF515151
                    ) else StravionBlue
                    else if (isDarkTheme) Color(0xFF333333) else Color(0xFFFFFFFF)
                )
                .clickable {
                    if (deviceMode.value == DeviceMode.DEVICE.value) return@clickable
                    onModeChange(DeviceMode.DEVICE)
                },
            contentAlignment = Alignment.Center
        )
        {
            Text(
                text = "Device Mode",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight(500),
                    fontFamily = FontFamily(Font(R.font.arial_regular)),
                    color = if (deviceMode.value == DeviceMode.DEVICE.value) Color.White else Color(
                        0xFF1A1A1C
                    )
                )
            )
        }
    }
}

@Composable
fun HotspotLayout(
    networkConfigurationViewModel: NetworkConfigurationViewModel
) {

//    var ssid by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var ipAddress by remember { mutableStateOf("192.168.2.1") }
//    var subnetMask by remember { mutableStateOf("255.255.255.0") }
    var errorMessage by remember { mutableStateOf("") }

    val hotspotConfiguration by networkConfigurationViewModel.hotspotState.collectAsStateWithLifecycle()
    Text(
        text = "Configure network settings to create a Wi-Fi hotspot hosted by your camera",
        style = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight(400),
            fontFamily = FontFamily(Font(R.font.arial_regular)),
            color = Color(0xFF1A1A1C)
        )
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        //SSID
        Text(
            text = "SSID",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight(700),
                fontFamily = FontFamily(Font(R.font.arial_regular)),
                color = Color(0xFF1A1A1C)
            )
        )

        OutlinedTextField(
            value = hotspotConfiguration.hotspot_ssid,
            onValueChange = { networkConfigurationViewModel.updateHotspotSSID(it)},
            label = { Text(hotspotConfiguration.hotspot_ssid) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = hotspotConfiguration.hotspot_ssid.isEmpty() && errorMessage.isNotEmpty(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        //Password
        Text(
            text = "Password",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight(700),
                fontFamily = FontFamily(Font(R.font.arial_regular)),
                color = Color(0xFF1A1A1C)
            )
        )
        OutlinedTextField(
            value = hotspotConfiguration.hotspot_password,
            onValueChange = { networkConfigurationViewModel.updateHotspotPassword(it) },
            label = { Text(hotspotConfiguration.hotspot_password) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = hotspotConfiguration.hotspot_password.isEmpty() && errorMessage.isNotEmpty(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        //IP Address
        Text(
            text = "IP Address",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight(700),
                fontFamily = FontFamily(Font(R.font.arial_regular)),
                color = Color(0xFF1A1A1C)
            )
        )
        OutlinedTextField(
            value = hotspotConfiguration.hotspot_ip_address,
            onValueChange = { networkConfigurationViewModel.updateHotspotIPAddress(it) },
            label = { Text(hotspotConfiguration.hotspot_ip_address) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = hotspotConfiguration.hotspot_ip_address.isEmpty() && errorMessage.isNotEmpty(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            enabled = false
        )
        //Subnet Mask
        Text(
            text = "Subnet Mask",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight(700),
                fontFamily = FontFamily(Font(R.font.arial_regular)),
                color = Color(0xFF1A1A1C)
            )
        )
        OutlinedTextField(
            value = hotspotConfiguration.hotspot_subnet_mask,
            onValueChange = { networkConfigurationViewModel.updateHotspotSubnetMask(it) },
            label = { Text("Enter subnet mask") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = hotspotConfiguration.hotspot_subnet_mask.isEmpty() && errorMessage.isNotEmpty(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            enabled = false
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))

                .background(StravionBlue)
                .clickable {

                },
            contentAlignment = Alignment.Center
        )
        {

            Text(
                text = "Start Hosting",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight(400),
                    fontFamily = FontFamily(Font(R.font.arial_regular)),
                    color = Color(0xFFFFFFFF)
                )
            )
        }
    }
}

@Composable
fun DeviceLayout(
    networkConfigurationViewModel: NetworkConfigurationViewModel
) {
    Text(
        text = "Configure network settings to connect your camera to an existing Wi-Fi network",
        style = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight(400),
            fontFamily = FontFamily(Font(R.font.arial_regular)),
            color = Color(0xFF1A1A1C)
        )
    )



//    var ssid by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var ipAddress by remember { mutableStateOf("192.168.2.1") }
//    var subnetMask by remember { mutableStateOf("255.255.255.0") }
    val wifiConfiguration by networkConfigurationViewModel.wifiState.collectAsStateWithLifecycle()
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        //SSID
        Text(
            text = "SSID",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight(700),
                fontFamily = FontFamily(Font(R.font.arial_regular)),
                color = Color(0xFF1A1A1C)
            )
        )

        OutlinedTextField(
            value = wifiConfiguration.wifi_ssid,
            onValueChange = { networkConfigurationViewModel.updateWifiSSID(it) },
            label = { Text(wifiConfiguration.wifi_ssid) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = wifiConfiguration.wifi_ssid.isEmpty() && errorMessage.isNotEmpty(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        //Password
        Text(
            text = "Password",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight(700),
                fontFamily = FontFamily(Font(R.font.arial_regular)),
                color = Color(0xFF1A1A1C)
            )
        )
        OutlinedTextField(
            value = wifiConfiguration.wifi_password,
            onValueChange = { networkConfigurationViewModel.updateWifiPassword(it) },
            label = { Text(wifiConfiguration.wifi_password) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = wifiConfiguration.wifi_password.isEmpty() && errorMessage.isNotEmpty(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        //IP Address
        Text(
            text = "IP Address",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight(700),
                fontFamily = FontFamily(Font(R.font.arial_regular)),
                color = Color(0xFF1A1A1C)
            )
        )
        OutlinedTextField(
            value = wifiConfiguration.wifi_ip_address,
            onValueChange = { networkConfigurationViewModel.updateWfiIPAddress(it) },
            label = { Text(wifiConfiguration.wifi_ip_address) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = wifiConfiguration.wifi_ip_address.isEmpty() && errorMessage.isNotEmpty(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            enabled = false
        )
        //Subnet Mask
        Text(
            text = "Subnet Mask",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight(700),
                fontFamily = FontFamily(Font(R.font.arial_regular)),
                color = Color(0xFF1A1A1C)
            )
        )
        OutlinedTextField(
            value = wifiConfiguration.wifi_subnet_mask,
            onValueChange = { networkConfigurationViewModel.updateWifiSubnetMask(it) },
            label = { Text(wifiConfiguration.wifi_subnet_mask) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = wifiConfiguration.wifi_subnet_mask.isEmpty() && errorMessage.isNotEmpty(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            enabled = false
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))

                .background(StravionBlue)
                .clickable {

                },
            contentAlignment = Alignment.Center
        )
        {

            Text(
                text = "Connect to Network",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight(400),
                    fontFamily = FontFamily(Font(R.font.arial_regular)),
                    color = Color(0xFFFFFFFF)
                )
            )
        }
    }
}

enum class DeviceMode(
    val displayName: String,
    val value: String
) {
    HOTSPOT("Hotspot Mode", "Hotspot"),
    DEVICE("Device Mode", "Client")
}
