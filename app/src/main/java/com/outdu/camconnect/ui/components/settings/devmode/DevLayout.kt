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
import com.outdu.camconnect.communication.StreamConfiguration
import com.outdu.camconnect.ui.theme.AppColors
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DevLayout() {
    val scope = rememberCoroutineScope()
    var healthStatus by remember { mutableStateOf<HealthStatus?>(null) }
    var streamConfiguration by remember { mutableStateOf<StreamConfiguration?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var autoRefresh by remember { mutableStateOf(false) }
    var lastUpdateTime by remember { mutableStateOf<String?>(null) }
    
//    val scrollState = rememberScrollState()
    
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
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
            DevStreamConfigurationCard(config = config)
        }


        // System Information
        DevSystemInfoCard()
    }
}


@Composable
private fun DevStreamConfigurationCard(config: StreamConfiguration) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            text = "System Stream Configurations",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        streamresolutionRow(
            label = "Stream 1 Resolution",
            value = config.stream1Resolution,
            fps = config.stream1Fps,
            bitrate = config.stream1Bitrate,
            encoder = config.stream1Encoder
        )
//        streamresolutionRow(
//            label = "Stream 2 Resolution",
//            value = config.stream2Resolution,
//            fps = config.stream2Fps,
//            bitrate = config.stream2Bitrate,
//            encoder = config.stream2Encoder
//        )
    }


}

@Composable
private fun streamresolutionRow(
    label: String,
    value: String,
    fps: Int,
    bitrate: Int,
    encoder: String
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
            text = value,
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
            text = fps.toString() + "fps",
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
            text = bitrate.toString() + "Mb/s",
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
            text = encoder,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = StravionBlue
        )
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
                Button(
                    onClick = onRefreshClick,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Refresh")
                }
                
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
                        text = "Auto Refresh",
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
            
//            DevTemperatureRow(
//                label = "IR Temperature",
//                value = "${status.irTemp}°C",
//                temperature = status.irTemp
//            )
//
//            if (status.sensorTemp != -1) {
//                DevTemperatureRow(
//                    label = "Sensor Temperature",
//                    value = "${status.sensorTemp}°C",
//                    temperature = status.sensorTemp
//                )
//            }
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
            DevInfoRow("Version", "1.1.0")
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