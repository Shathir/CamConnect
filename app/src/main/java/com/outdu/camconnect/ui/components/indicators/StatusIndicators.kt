package com.outdu.camconnect.ui.components.indicators

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.ui.components.buttons.ButtonConfig
import com.outdu.camconnect.ui.components.buttons.CustomizableButton
import com.outdu.camconnect.R
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.net.wifi.WifiManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresPermission
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Battery level indicator with placeholder icon and percentage
 */
@Composable
fun BatteryIndicator(
    batteryLevel: Int = -1, // Default value, will be overridden by BroadcastReceiver
    modifier: Modifier = Modifier,
    showPercentage: Boolean = true
) {
    val context = LocalContext.current
    var currentBatteryLevel by remember { mutableStateOf(batteryLevel) }
    
    DisposableEffect(context) {
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        currentBatteryLevel = (level * 100 / scale)
                    }
                }
            }
        }
        
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(batteryReceiver, filter)
        
        // Get initial battery level
        batteryStatus?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                currentBatteryLevel = (level * 100 / scale)
            }
        }
        
        onDispose {
            context.unregisterReceiver(batteryReceiver)
        }
    }
    
    // Use the battery level from receiver if available, otherwise use the passed parameter
    val displayBatteryLevel = if (currentBatteryLevel != -1) currentBatteryLevel else batteryLevel
    
    val batteryColor = when {
        displayBatteryLevel <= 10 -> Color.Red
        displayBatteryLevel <= 30 -> Color.Yellow
        displayBatteryLevel <= 50 -> Color(0xFFFF9800) // Orange
        else -> Color.Green
    }

    Column (
        modifier = Modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Battery icon with realistic shape
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Battery body
                Box(
                    modifier = Modifier
                        .size(width = 22.dp, height = 14.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(batteryColor)
                        .padding(1.dp)
                ) {
                    // Inner battery fill area
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color(0xFF222222))
                    ) {
                        // Battery level fill
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(displayBatteryLevel / 100f)
                                .background(
                                    batteryColor,
                                    RoundedCornerShape(
                                        topStart = 1.dp,
                                        bottomStart = 1.dp,
                                        topEnd = if (displayBatteryLevel >= 95) 1.dp else 0.dp,
                                        bottomEnd = if (displayBatteryLevel >= 95) 1.dp else 0.dp
                                    )
                                )
                                .align(Alignment.CenterStart)
                        )
                    }
                }
                
                // Battery terminal cap
                Box(
                    modifier = Modifier
                        .size(width = 2.dp, height = 6.dp)
                        .clip(RoundedCornerShape(topEnd = 1.dp, bottomEnd = 1.dp))
                        .background(batteryColor)
                )
            }
            
            if (showPercentage) {
                // Percentage text
                Text(
                    text = "$displayBatteryLevel%",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * WiFi connectivity indicator placeholder
 */
@Composable
fun WifiIndicator(
    isConnected: Boolean = true, // Default to true, will be updated by receiver
    signalStrength: Int = 3, // 0-3, default to max
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentIsConnected by remember { mutableStateOf(isConnected) }
    var currentSignalStrength by remember { mutableStateOf(signalStrength) }
    
    // Check if we have location permissions (required for detailed WiFi info on Android 8.1+)
    val hasLocationPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Location permission not required for older versions
        }
    }
    
    DisposableEffect(context) {
        val wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    WifiManager.RSSI_CHANGED_ACTION -> {
                        val wifiManager = context?.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                        
                        // Only access WiFi info if we have permissions
                        if (hasLocationPermission) {
                            wifiManager?.connectionInfo?.let { wifiInfo ->
                                // Check if WiFi is actually connected
                                currentIsConnected = wifiInfo.networkId != -1
                                
                                if (currentIsConnected) {
                                    // Convert RSSI to signal strength (0-3)
                                    val rssi = wifiInfo.rssi
                                    currentSignalStrength = when {
                                        rssi >= -50 -> 3  // Excellent signal
                                        rssi >= -60 -> 2  // Good signal
                                        rssi >= -70 -> 1  // Fair signal
                                        else -> 0         // Poor signal
                                    }
                                }
                            }
                        }
                    }
                    ConnectivityManager.CONNECTIVITY_ACTION -> {
                        // Additional check for connectivity status
                        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val network = connectivityManager?.activeNetwork
                            val capabilities = connectivityManager?.getNetworkCapabilities(network)
                            currentIsConnected = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                        } else {
                            @Suppress("DEPRECATION")
                            val networkInfo = connectivityManager?.activeNetworkInfo
                            currentIsConnected = networkInfo?.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
                        }
                    }
                }
            }
        }
        
        // Register for WiFi RSSI changes and connectivity changes
        val filter = IntentFilter().apply {
            addAction(WifiManager.RSSI_CHANGED_ACTION)
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }
        context.registerReceiver(wifiReceiver, filter)
        
        // Get initial WiFi status
        if (hasLocationPermission) {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifiManager?.connectionInfo?.let { wifiInfo ->
                currentIsConnected = wifiInfo.networkId != -1
                if (currentIsConnected) {
                    val rssi = wifiInfo.rssi
                    currentSignalStrength = when {
                        rssi >= -50 -> 3
                        rssi >= -60 -> 2
                        rssi >= -70 -> 1
                        else -> 0
                    }
                }
            }
        }
        
        onDispose {
            context.unregisterReceiver(wifiReceiver)
        }
    }
    
    // WiFi icon placeholder
    Box(
        modifier = modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // WiFi symbol with filled segments
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val bottomY = size.height * 0.85f
            
            // Define colors based on signal strength
            val activeColor = when {
                !currentIsConnected -> Color.Gray
                currentSignalStrength >= 2 -> Color.Green
                currentSignalStrength == 1 -> Color(0xFFFF9800) // Orange
                else -> Color.Red
            }
            val inactiveColor = Color.Gray.copy(alpha = 0.3f)
            
            // Helper function to draw WiFi segment
            fun drawWifiSegment(
                startAngle: Float,
                sweepAngle: Float,
                radius: Float,
                thickness: Float,
                isActive: Boolean
            ) {
                // Outer arc
                drawArc(
                    color = if (isActive) activeColor else inactiveColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, bottomY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = thickness)
                )
                
                // Fill the segment by drawing multiple concentric arcs
                var currentRadius = radius - thickness / 2
                val step = thickness / 4
                while (currentRadius > radius - thickness) {
                    drawArc(
                        color = if (isActive) activeColor else inactiveColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(centerX - currentRadius, bottomY - currentRadius),
                        size = Size(currentRadius * 2, currentRadius * 2),
                        style = Stroke(width = step)
                    )
                    currentRadius -= step
                }
            }
            
            // Draw WiFi segments (from smallest to largest)
            // Segment 1 (smallest) - always visible when connected
            drawWifiSegment(
                startAngle = -135f,
                sweepAngle = 90f,
                radius = 5.dp.toPx(),
                thickness = 3.dp.toPx(),
                isActive = currentIsConnected && currentSignalStrength >= 1
            )
            
            // Segment 2 (medium)
            drawWifiSegment(
                startAngle = -135f,
                sweepAngle = 90f,
                radius = 8.dp.toPx(),
                thickness = 3.dp.toPx(),
                isActive = currentIsConnected && currentSignalStrength >= 2
            )
            
            // Segment 3 (largest)
            drawWifiSegment(
                startAngle = -135f,
                sweepAngle = 90f,
                radius = 11.dp.toPx(),
                thickness = 3.dp.toPx(),
                isActive = currentIsConnected && currentSignalStrength >= 3
            )
            
            // Center dot (always filled when connected)
            drawCircle(
                color = if (currentIsConnected) activeColor else inactiveColor,
                radius = 2.dp.toPx(),
                center = Offset(centerX, bottomY)
            )
        }
    }
}

/**
 * LTE/Mobile data indicator placeholder
 */
@Composable
fun LteIndicator(
    isConnected: Boolean,
    signalStrength: Int = 3, // 0-4
    modifier: Modifier = Modifier
) {
    // LTE signal placeholder
    Box(
        modifier = modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            for (i in 0..3) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height((6 + i * 3).dp)
                        .background(
                            if (isConnected && i <= signalStrength - 1) Color.Green
                            else Color.Gray.copy(alpha = 0.3f),
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}

/**
 * Online/Offline status indicator
 */
@Composable
fun OnlineIndicator(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isOnline) Color(0xFF4CAF50).copy(alpha = 0.1f)
                else Color.Red.copy(alpha = 0.1f)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isOnline) Color(0xFF4CAF50) else Color.Red)
        )
        // Text placeholder
        Box(
            modifier = Modifier
                .height(11.dp)
                .width(if (isOnline) 35.dp else 40.dp)
                .background(
                    if (isOnline) Color(0xFF4CAF50).copy(alpha = 0.3f) 
                    else Color.Red.copy(alpha = 0.3f)
                )
        )
    }
}

/**
 * AI status indicator
 */
@Composable
fun AiStatusIndicator(
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val iconPainter = rememberVectorPainter(image = aiIcon(isEnabled))
//    Image(
//        painter = painterResource(id = R.drawable.ai_line),
//        contentDescription = "AI Indicator",
//        modifier = modifier.size(14.dp),
////        colorFilter = ColorFilter.tint(if(isEnabled) Color.Green else Color.Gray),
//        contentScale = ContentScale.Fit
//    )
    Image(
        painter = iconPainter,
        contentDescription = "Ai Status Icon",
        modifier = modifier.size(24.dp),
        contentScale = ContentScale.Fit
    )
}

/**
 * Composite status bar showing all indicators
 */
@Composable
fun StatusBar(
    batteryLevel: Int,
    isWifiConnected: Boolean,
    isLteConnected: Boolean,
    isOnline: Boolean,
    isAiEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BatteryIndicator(batteryLevel = batteryLevel)
        WifiIndicator(isConnected = isWifiConnected)
        LteIndicator(isConnected = isLteConnected)
        OnlineIndicator(isOnline = isOnline)
        AiStatusIndicator(isEnabled = isAiEnabled)
    }
} 