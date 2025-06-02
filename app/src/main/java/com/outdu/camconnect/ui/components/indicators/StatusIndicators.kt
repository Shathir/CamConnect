package com.outdu.camconnect.ui.components.indicators

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Battery level indicator with placeholder icon and percentage
 */
@Composable
fun BatteryIndicator(
    batteryLevel: Int,
    modifier: Modifier = Modifier,
    showPercentage: Boolean = true
) {
    val batteryColor = when {
        batteryLevel <= 20 -> Color.Red
        batteryLevel <= 50 -> Color(0xFFFF9800) // Orange
        else -> Color.Green
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Battery icon placeholder
        Box(
            modifier = Modifier
                .size(width = 24.dp, height = 16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Gray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(batteryLevel / 100f)
                    .background(batteryColor)
                    .align(Alignment.CenterStart)
            )
        }
        
        if (showPercentage) {
            // Percentage text placeholder
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .width(28.dp)
                    .background(Color(0xFF333333).copy(alpha = 0.3f))
            )
            {
                Text(
                    text = "$batteryLevel%",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
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
    isConnected: Boolean,
    signalStrength: Int = 3, // 0-3
    modifier: Modifier = Modifier
) {
    // WiFi icon placeholder
    Box(
        modifier = modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Signal bars
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            for (i in 0..2) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height((8 + i * 4).dp)
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
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isEnabled) Color(0xFF2196F3).copy(alpha = 0.1f)
                else Color.Gray.copy(alpha = 0.1f)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // AI icon placeholder
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(if (isEnabled) Color(0xFF2196F3) else Color.Gray)
        ) {
            // Star-like pattern for AI
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color.White, CircleShape)
                    .align(Alignment.Center)
            )
        }
        // "AI" text placeholder
        Box(
            modifier = Modifier
                .height(11.dp)
                .width(12.dp)
                .background(
                    if (isEnabled) Color(0xFF2196F3).copy(alpha = 0.3f)
                    else Color.Gray.copy(alpha = 0.3f)
                )
        )
    }
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