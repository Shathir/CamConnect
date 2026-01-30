package com.outdu.camconnect.ui.components.settings.devmode

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.communication.HealthStatus
import com.outdu.camconnect.communication.MotocamAPIAndroidHelper
import com.outdu.camconnect.communication.MotocamAPIHelper
import com.outdu.camconnect.communication.StreamConfiguration
import com.outdu.camconnect.ui.theme.AppColors
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import androidx.compose.ui.platform.LocalContext
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType

@Composable
fun DevLayout() {
    val scope = rememberCoroutineScope()
    var healthStatus by remember { mutableStateOf<HealthStatus?>(null) }
    var streamConfiguration by remember { mutableStateOf<StreamConfiguration?>(null) }
    var wifiState by remember { mutableStateOf<MotocamAPIHelper.WifiState?>(null) }
    var wifiIp by remember { mutableStateOf<String?>(null) }
    var ethernetIp by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var autoRefresh by remember { mutableStateOf(false) }
    var lastUpdateTime by remember { mutableStateOf<String?>(null) }
    
//    val scrollState = rememberScrollState()
    var deviceType = rememberDeviceType()
    // Auto-refresh effect
    LaunchedEffect(autoRefresh) {
        while (autoRefresh) {
            loadHealthStatus(
                scope = scope,
                onLoading = { isLoading = it },
                onSuccess = { 
                    healthStatus = it
                    errorMessage = null
                    lastUpdateTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date())
                },
                onError = { errorMessage = it }
            )
            delay(5000) // Refresh every 5 seconds
        }
    }
    
    // Initial load
    LaunchedEffect(Unit) {
        loadHealthStatus(
            scope = scope,
            onLoading = { isLoading = it },
            onSuccess = { 
                healthStatus = it
                errorMessage = null
                lastUpdateTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date())
            },
            onError = { errorMessage = it }
        )

        loadStreamConfigurations(
            scope = scope,
            onLoading = { isLoading = it },
            onSuccess = {
                streamConfiguration = it
                errorMessage = null
                lastUpdateTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date())
            },
            onError = { errorMessage = it }
        )

        loadRtspNetworkInfo(
            scope = scope,
            onLoading = { isLoading = it },
            onSuccess = { state, wip, eip ->
                wifiState = state
                wifiIp = wip
                ethernetIp = eip
            },
            onError = { errorMessage = it }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(if(deviceType == DeviceType.TABLET)16.dp else 8.dp)
    ) {
        // Header
        DevSectionHeader(
            title = "Developer Mode",
            subtitle = "System Health & Diagnostics"
        )
        
        // Control Panel
        DevControlPanel(
            isLoading = isLoading,
            autoRefresh = autoRefresh,
            lastUpdateTime = lastUpdateTime,
            onRefreshClick = {
                scope.launch {
                    loadHealthStatus(
                        scope = scope,
                        onLoading = { isLoading = it },
                        onSuccess = {
                            healthStatus = it
                            errorMessage = null
                            lastUpdateTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                                .format(java.util.Date())
                        },
                        onError = { errorMessage = it }
                    )
                }
            },
            onAutoRefreshToggle = { autoRefresh = !autoRefresh }
        )

        // Error Display
        errorMessage?.let { error ->
            DevErrorCard(error = error)
        }

        // Health Status Display
        healthStatus?.let { status ->
            DevHealthStatusCard(status = status)
        }

        // Stream Configuration Display
        streamConfiguration?.let { config ->
            DevStreamConfigurationCard(
                config = config,
                wifiState = wifiState,
                wifiIp = wifiIp,
                ethernetIp = ethernetIp
            )
        }


        // System Information
        DevSystemInfoCard()
    }
}


@Composable
private fun DevStreamConfigurationCard(
    config: StreamConfiguration,
    wifiState: MotocamAPIHelper.WifiState?,
    wifiIp: String?,
    ethernetIp: String?
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "System Stream Configurations",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (wifiIp != null || ethernetIp != null) {
                val wifiLabel = when (wifiState) {
                    MotocamAPIHelper.WifiState.WifiHotspot -> "Hotspot"
                    MotocamAPIHelper.WifiState.WifiClient -> "Client"
                    null -> "Unknown"
                }
                Text(
                    text = buildString {
                        if (wifiIp != null) append("WiFi ($wifiLabel): $wifiIp")
                        if (wifiIp != null && ethernetIp != null) append("  •  ")
                        if (ethernetIp != null) append("Ethernet: $ethernetIp")
                    },
                    fontSize = 12.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (config.streams.isEmpty()) {
                Text(
                    text = "No streams returned by API",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                return@Column
            }

            config.streams.forEachIndexed { index, stream ->
                if (index > 0) {
                    HorizontalDivider(color = AppColors.BorderColor)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                streamConfigSection(
                    label = "Stream ${index + 1}",
                    stream = stream,
                    streamNumber = index + 1,
                    wifiIp = wifiIp,
                    ethernetIp = ethernetIp
                )
            }
        }
    }


}

@Composable
private fun streamConfigSection(
    label: String,
    stream: com.outdu.camconnect.communication.StreamInfo,
    streamNumber: Int,
    wifiIp: String?,
    ethernetIp: String?
)
{
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black
        )

        Text(
            text = stream.resolution.displayName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = StravionBlue
        )
    }

    Spacer(modifier = Modifier.height(4.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "FPS",
            fontSize = 14.sp,
            color = Color.Black
        )

        Text(
            text = stream.fps.toString() + "fps",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = StravionBlue
        )
    }

    Spacer(modifier = Modifier.height(4.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Bitrate",
            fontSize = 14.sp,
            color = Color.Black
        )

        Text(
            text = stream.bitrate.toString() + "Mb/s",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = StravionBlue
        )
    }

    Spacer(modifier = Modifier.height(4.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Encoder",
            fontSize = 14.sp,
            color = Color.Black
        )

        Text(
            text = stream.encoder.displayName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = StravionBlue
        )
    }

    val path = "/live${streamNumber}.sdp"
    if (wifiIp != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "RTSP (WiFi)",
                fontSize = 12.sp,
                color = Color.Black
            )
            Text(
                text = "rtsp://$wifiIp$path",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = StravionBlue
            )
        }
    }

    if (ethernetIp != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "RTSP (Ethernet)",
                fontSize = 12.sp,
                color = Color.Black
            )
            Text(
                text = "rtsp://$ethernetIp$path",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = StravionBlue
            )
        }
    }

}


@Composable
private fun DevSectionHeader(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun DevControlPanel(
    isLoading: Boolean,
    autoRefresh: Boolean,
    lastUpdateTime: String?,
    onRefreshClick: () -> Unit,
    onAutoRefreshToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Controls",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
//                Button(
//                    onClick = onRefreshClick,
//                    enabled = !isLoading,
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color.White
//                    )
//                ) {

                Text("Refresh")

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
//                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = autoRefresh,
                        onCheckedChange = { onAutoRefreshToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color.Black.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Auto",
                        color = Color.Black
                    )
                }
            }
            
            lastUpdateTime?.let { time ->
                Text(
                    text = "Last updated: $time",
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun DevErrorCard(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Error",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Red
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                fontSize = 14.sp,
                color = Color.Red.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun DevHealthStatusCard(status: HealthStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "System Health Status",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            
            // Streaming Status
            DevStatusRow(
                label = "RTSPS Stream",
                value = if (status.rtsps) "Active" else "Inactive",
                isGood = status.rtsps
            )
            
            DevStatusRow(
                label = "Portable RTC",
                value = if (status.portableRtc) "Active" else "Inactive",
                isGood = status.portableRtc
            )
            
            Divider(color = AppColors.BorderColor)
            
            // Performance Metrics
            DevMetricRow(
                label = "CPU Usage",
                value = "${status.cpuUsage}%",
                percentage = status.cpuUsage
            )
            
            DevMetricRow(
                label = "Memory Usage",
                value = "${status.memoryUsage}%",
                percentage = status.memoryUsage
            )

            HorizontalDivider(color = AppColors.BorderColor)
            
            // Temperature Readings
            DevTemperatureRow(
                label = "ISP Temperature",
                value = "${status.ispTemp}°C",
                temperature = status.ispTemp
            )
            
        }
    }
}

@Composable
private fun DevStatusRow(
    label: String,
    value: String,
    isGood: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black
        )
        
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isGood) StravionBlue else Color.Red
        )
    }
}

@Composable
private fun DevMetricRow(
    label: String,
    value: String,
    percentage: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Black
            )
            
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    percentage < 60 -> StravionBlue
                    else -> Color.Red
                }
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = when {
                percentage < 60 -> StravionBlue
                else -> Color.Red
            },
            trackColor = AppColors.BorderColor
        )
    }
}

@Composable
private fun DevTemperatureRow(
    label: String,
    value: String,
    temperature: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black
        )
        
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = when {
                temperature < 60 -> StravionBlue
                else -> Color.Red
            }
        )
    }
}

@Composable
private fun DevSystemInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "System Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            
            DevInfoRow("Build", "Release")
            DevInfoRow("Version", "2025.11.0")
            DevInfoRow("API Level", android.os.Build.VERSION.SDK_INT.toString())
            DevInfoRow("Device", "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
        }
    }
}

@Composable
private fun DevInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

private fun loadHealthStatus(
    scope: kotlinx.coroutines.CoroutineScope,
    onLoading: (Boolean) -> Unit,
    onSuccess: (HealthStatus) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)
    
    MotocamAPIAndroidHelper.getHealthStatusAsync(scope) { status, error ->
        onLoading(false)
        
        if (error != null) {
            Log.e("DevLayout", "Health check failed: $error")
            onError(error)
            return@getHealthStatusAsync
        }

        status?.let {
            Log.d("DevLayout", "HealthStatus → RTSPS=${it.rtsps}, " +
                    "CPU=${it.cpuUsage}%, " +
                    "ISP Temp=${it.ispTemp}°C, " +
                    "memory=${it.memoryUsage}%, " +
                    "portablertc=${it.portableRtc}, " +
                    "irTemp=${it.irTemp}, " +
                    "sensorTemp=${it.sensorTemp}")
            onSuccess(it)
        } ?: onError("No health status received")
    }
}

private fun loadStreamConfigurations(
    scope: kotlinx.coroutines.CoroutineScope,
    onLoading: (Boolean) -> Unit,
    onSuccess: (StreamConfiguration) -> Unit,
    onError: (String) -> Unit
)
{
    onLoading(true)
    MotocamAPIAndroidHelper.getStreamConfigurationAsync(scope) { status, error ->
        onLoading(false)

        if (error != null) {
            Log.e("DevLayout", "Health check failed: $error")
            onError(error)
            return@getStreamConfigurationAsync
        }

        status?.let {
            onSuccess(it)
        } ?: onError("No health status received")
    }
}

private fun loadRtspNetworkInfo(
    scope: kotlinx.coroutines.CoroutineScope,
    onLoading: (Boolean) -> Unit,
    onSuccess: (MotocamAPIHelper.WifiState?, String?, String?) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)

    var wifiDone = false
    var ethDone = false

    var wifiState: MotocamAPIHelper.WifiState? = null
    var wifiIp: String? = null
    var ethIp: String? = null

    var wifiErr: String? = null
    var ethErr: String? = null

    fun maybeFinish() {
        if (!wifiDone || !ethDone) return
        onLoading(false)

        // Only surface error if we couldn't fetch any usable IP.
        if (wifiIp.isNullOrBlank() && ethIp.isNullOrBlank()) {
            onError(wifiErr ?: ethErr ?: "Unable to fetch RTSP network info")
            return
        }
        onSuccess(wifiState, wifiIp, ethIp)
    }

    MotocamAPIAndroidHelper.getActiveWifiIpAsync(scope) { state, ip, error ->
        wifiState = state
        wifiIp = ip
        wifiErr = error
        wifiDone = true
        maybeFinish()
    }

    MotocamAPIAndroidHelper.getEthernetConfigAsync(scope) { config, error ->
        ethIp = config?.get("ipaddress")?.toString()
        ethErr = error
        ethDone = true
        maybeFinish()
    }
}