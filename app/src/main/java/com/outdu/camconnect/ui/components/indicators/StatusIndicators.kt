package com.outdu.camconnect.ui.components.indicators


import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.outdu.camconnect.R
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
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.outdu.camconnect.ui.theme.*
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType
import android.util.Log
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue


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
    val deviceType = rememberDeviceType()
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
        displayBatteryLevel <= 20 -> BatteryRed
        displayBatteryLevel <= 70 -> Color(0xFFD08101)
        else -> BatteryGreen
    }

    val icon = when {
        displayBatteryLevel <= 20 -> R.drawable.batterylow
        displayBatteryLevel <= 70 -> R.drawable.batterymedium
        else -> R.drawable.batteryfull
    }


    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {

        Image(
            painter = painterResource(id = icon),
            contentDescription = "Battery Icon",
            colorFilter = ColorFilter.tint(batteryColor),
            modifier = Modifier.size(if(deviceType == DeviceType.TABLET) 24.dp else 16.dp)
        )

    }
}

/**
 * WiFi connectivity indicator placeholder
 */
@Composable
fun WifiIndicator(
    isConnected: Boolean = true, // Default to true, will be updated by receiver
    signalStrength: Int = 3, // 0-3, default to max
    modifier: Modifier = Modifier,
    onPermissionRequest: (() -> Unit)? = null // Callback to request permissions
) {
    val context = LocalContext.current
    var currentIsConnected by remember { mutableStateOf(isConnected) }
    var currentSignalStrength by remember { mutableStateOf(signalStrength) }
    val deviceType = rememberDeviceType()

    // Check if we have location permissions (required for detailed WiFi info on Android 8.1+)
    // Use derivedStateOf to react to permission changes
    val hasLocationPermission by remember {
        derivedStateOf {
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
    }

    // Simplified WiFi connectivity check
    fun checkWifiConnectivity(): Pair<Boolean, Int> {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        
        Log.d("WifiIndicator", "Checking WiFi connectivity. HasLocationPermission: $hasLocationPermission")
        
        // First check if WiFi is enabled
        if (wifiManager?.isWifiEnabled != true) {
            Log.d("WifiIndicator", "WiFi is not enabled")
            return Pair(false, 0)
        }
        
        var isConnected = false
        var signalStrength = 2 // Default signal strength
        
        // Try to get detailed WiFi info if we have permissions
        if (hasLocationPermission) {
            try {
                wifiManager.connectionInfo?.let { wifiInfo ->
                    isConnected = wifiInfo.networkId != -1
                    Log.d("WifiIndicator", "WiFi networkId: ${wifiInfo.networkId}, SSID: ${wifiInfo.ssid}")
                    
                    if (isConnected) {
                        val rssi = wifiInfo.rssi
                        signalStrength = when {
                            rssi >= -50 -> 3  // Excellent signal
                            rssi >= -60 -> 2  // Good signal
                            rssi >= -70 -> 1  // Fair signal
                            else -> 0         // Poor signal
                        }
                        Log.d("WifiIndicator", "WiFi RSSI: $rssi, Signal strength: $signalStrength")
                    }
                }
            } catch (e: Exception) {
                Log.w("WifiIndicator", "Error getting WiFi info with location permission", e)
                isConnected = false
            }
        }
        
        // If we don't have location permission or couldn't get WiFi info, use connectivity manager
        if (!isConnected) {
            try {
                isConnected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val network = connectivityManager?.activeNetwork
                    val capabilities = connectivityManager?.getNetworkCapabilities(network)
                    val hasWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                    Log.d("WifiIndicator", "ConnectivityManager WiFi transport: $hasWifi")
                    hasWifi
                } else {
                    @Suppress("DEPRECATION")
                    val networkInfo = connectivityManager?.activeNetworkInfo
                    val isWifiConnected = networkInfo?.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
                    Log.d("WifiIndicator", "Legacy WiFi check: $isWifiConnected")
                    isWifiConnected
                }
            } catch (e: Exception) {
                Log.w("WifiIndicator", "Error checking connectivity", e)
                isConnected = false
            }
        }
        
        Log.d("WifiIndicator", "Final WiFi status - Connected: $isConnected, Signal: $signalStrength")
        return Pair(isConnected, signalStrength)
    }

    // Update WiFi status periodically and on network changes
    LaunchedEffect(hasLocationPermission) {
        // Initial check
        val (connected, strength) = checkWifiConnectivity()
        currentIsConnected = connected
        currentSignalStrength = strength
        
        // Set up periodic checks every 5 seconds
        while (true) {
            kotlinx.coroutines.delay(5000)
            val (newConnected, newStrength) = checkWifiConnectivity()
            if (newConnected != currentIsConnected || newStrength != currentSignalStrength) {
                currentIsConnected = newConnected
                currentSignalStrength = newStrength
            }
        }
    }

    // Also listen to network changes via broadcast receiver
    DisposableEffect(context, hasLocationPermission) {
        val wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("WifiIndicator", "Received broadcast: ${intent?.action}")
                val (connected, strength) = checkWifiConnectivity()
                currentIsConnected = connected
                currentSignalStrength = strength
            }
        }
        
        val filter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }
        
        try {
            context.registerReceiver(wifiReceiver, filter)
        } catch (e: Exception) {
            Log.w("WifiIndicator", "Error registering WiFi receiver", e)
        }
        
        onDispose {
            try {
                context.unregisterReceiver(wifiReceiver)
            } catch (e: IllegalArgumentException) {
                Log.w("WifiIndicator", "Receiver was already unregistered", e)
            }
        }
    }

    val wifiColor = when {
        !currentIsConnected -> Color(0xFF8E8E8E)
        currentSignalStrength >=1 -> Color(0xFF4E8EFF)
        else -> Color.Gray
    }

    val wifiIcon = when {
        !currentIsConnected -> R.drawable.wifi_off
        currentSignalStrength >= 2 -> R.drawable.wifi_high
        currentSignalStrength == 1 -> R.drawable.wifi_medium
        else -> R.drawable.wifi_low
    }


    Image(
        painter = painterResource(wifiIcon),
        contentDescription = "Wifi Icon",
        colorFilter = ColorFilter.tint(wifiColor),
        modifier = modifier.size(if(deviceType == DeviceType.TABLET) 24.dp else 16.dp)
    )
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
                            if (isConnected && i <= signalStrength - 1) BatteryGreen
                            else Gray.copy(alpha = 0.3f),
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
                if (isOnline) ConnectionGreen.copy(alpha = 0.1f)
                else ConnectionRed.copy(alpha = 0.1f)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isOnline) ConnectionGreen else ConnectionRed)
        )
        // Text placeholder
        Box(
            modifier = Modifier
                .height(11.dp)
                .width(if (isOnline) 35.dp else 40.dp)
                .background(
                    if (isOnline) ConnectionGreen.copy(alpha = 0.3f) 
                    else ConnectionRed.copy(alpha = 0.3f)
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
    val deviceType = rememberDeviceType()
    Image(
        painter = painterResource(id = R.drawable.ai_line),
        contentDescription = "AI Indicator",
        modifier = modifier.size(if(deviceType == DeviceType.TABLET) 24.dp else 16.dp),
        colorFilter = ColorFilter.tint(if(isEnabled) StravionBlue else Color.Gray),
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
    modifier: Modifier = Modifier,
    onWifiPermissionRequest: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BatteryIndicator(batteryLevel = batteryLevel)
        WifiIndicator(
            isConnected = isWifiConnected, 
            onPermissionRequest = onWifiPermissionRequest
        )
        LteIndicator(isConnected = isLteConnected)
        OnlineIndicator(isOnline = isOnline)
        AiStatusIndicator(isEnabled = isAiEnabled)
    }
} 