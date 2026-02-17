package com.outdu.camconnect.ui.components.camera

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.outdu.camconnect.ui.theme.DarkBackground2
import com.outdu.camconnect.ui.theme.MediumLightGray
import com.outdu.camconnect.ui.theme.RecordRed
import com.outdu.camconnect.ui.theme.White

/**
 * Data class to hold system warning state
 */
data class SystemWarningData(
    val batteryLevel: Int,
    val formattedStorageSize: String,
    val isLowStorage: Boolean,
    val isLowBattery: Boolean,
    val hasWarning: Boolean
)

/**
 * Hook to monitor system warnings (battery and storage)
 */
@Composable
fun rememberSystemWarningData(): SystemWarningData {
    val context = LocalContext.current
    var batteryLevel by remember { mutableStateOf(100) }
    var freeBytes by remember { mutableStateOf(0L) }
    var formattedStorageSize by remember { mutableStateOf("") }
    
    // Monitor battery level
    DisposableEffect(context) {
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        batteryLevel = (level * 100 / scale)
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
                batteryLevel = (level * 100 / scale)
            }
        }

        onDispose {
            context.unregisterReceiver(batteryReceiver)
        }
    }

    // Calculate storage - update periodically
    LaunchedEffect(Unit) {
        while (true) {
            val statFs = StatFs(Environment.getDataDirectory().path)
            freeBytes = statFs.availableBytes
            formattedStorageSize = Formatter.formatFileSize(context, freeBytes)
            kotlinx.coroutines.delay(10000) // Update every 10 seconds
        }
    }
    
    val fiveGBInBytes = 5L * 1000 * 1000 * 1000
    val isLowStorage = freeBytes < fiveGBInBytes
    val isLowBattery = batteryLevel < 30
    val hasWarning = isLowStorage || isLowBattery
    
    return SystemWarningData(
        batteryLevel = batteryLevel,
        formattedStorageSize = formattedStorageSize,
        isLowStorage = isLowStorage,
        isLowBattery = isLowBattery,
        hasWarning = hasWarning
    )
}

/**
 * Icon button for system warning tooltip with active state highlighting
 */
@Composable
fun SystemWarningIcon(
    tooltipManager: TooltipManager,
    hasWarning: Boolean,
    modifier: Modifier = Modifier
) {
    val tooltipId = "system_warning"
    val isActive = tooltipManager.isTooltipVisible(tooltipId)
    
    if (hasWarning) {
        Box(
            modifier = modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isActive)
                        RecordRed.copy(alpha = 1f)
                    else
                        RecordRed.copy(alpha = 0.9f)
                )
                .border(
                    width = if (isActive) 2.dp else 0.dp,
                    color = if (isActive) White.copy(alpha = 0.6f) else Color.Transparent,
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable { tooltipManager.toggleTooltip(tooltipId) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚠",
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Spacer(modifier = modifier.size(32.dp))
    }
}

/**
 * Warning tooltip component that shows system warnings (low storage or battery)
 */
@Composable
fun SystemWarningTooltip(
    tooltipManager: TooltipManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tooltipId = "system_warning"
    val isTooltipVisible = tooltipManager.isTooltipVisible(tooltipId)
    var batteryLevel by remember { mutableStateOf(100) }
    var freeBytes by remember { mutableStateOf(0L) }
    var formattedStorageSize by remember { mutableStateOf("") }

    // Monitor battery level
    DisposableEffect(context) {
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        batteryLevel = (level * 100 / scale)
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
                batteryLevel = (level * 100 / scale)
            }
        }

        onDispose {
            context.unregisterReceiver(batteryReceiver)
        }
    }

    // Calculate storage - update periodically
    LaunchedEffect(Unit) {
        while (true) {
            val statFs = StatFs(Environment.getDataDirectory().path)
            freeBytes = statFs.availableBytes
            // Use Formatter.formatFileSize for accurate storage display
            formattedStorageSize = Formatter.formatFileSize(context, freeBytes)
            kotlinx.coroutines.delay(10000) // Update every 10 seconds
        }
    }

    // Check if warnings should be shown
    // Formatter.formatFileSize() uses SI units (1000-based) on Android O+ and binary units (1024-based) on older versions
    // We use SI units (1000-based) to match what users see in the formatted display
    val fiveGBInBytes =
        5L * 1000 * 1000 * 1000  // SI units (matches Formatter.formatFileSize on Android O+)
    val isLowStorage = freeBytes < fiveGBInBytes
    val isLowBattery = batteryLevel < 30
    val hasWarning = isLowStorage || isLowBattery

    // Auto-hide tooltip after 5 seconds
    LaunchedEffect(isTooltipVisible) {
        if (isTooltipVisible) {
            kotlinx.coroutines.delay(5000) // 5 seconds
            tooltipManager.closeTooltip(tooltipId)
        }
    }

    // Always render fixed-size container to prevent layout shift
    Box(modifier = modifier) {
        if (hasWarning) {
            // Clickable warning icon/button - fixed size prevents movement
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(RecordRed.copy(alpha = 0.9f))
                    .clickable { tooltipManager.toggleTooltip(tooltipId) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚠",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // Reserve space even when no warning to prevent Row reflow
            Spacer(modifier = Modifier.size(32.dp))
        }

        // Tooltip popup - only shown when there's a warning
        // positioned below button, right-aligned so button doesn't move
        if (hasWarning) {
            AnimatedVisibility(
                visible = isTooltipVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 0.dp, y = 40.dp) // Position below button with consistent spacing
                    .zIndex(10f)
            ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 200.dp, max = 250.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        tooltipManager.closeTooltip(tooltipId)
                    }
                    .background(DarkBackground2.copy(alpha = 0.95f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Warning header
                Text(
                    text = "System Warning",
                    color = RecordRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MediumLightGray.copy(alpha = 0.3f))
                )

                // Storage warning
                if (isLowStorage) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Storage",
                            color = MediumLightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Low storage: $formattedStorageSize available",
                            color = RecordRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Separator between warnings if both are present
                if (isLowStorage && isLowBattery) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MediumLightGray.copy(alpha = 0.3f))
                    )
                }

                // Battery warning
                if (isLowBattery) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Battery",
                            color = MediumLightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Battery level: ${batteryLevel}%",
                            color = RecordRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        }
    }
}

/**
 * Content display for system warning tooltip (storage and battery information)
 */
@Composable
fun SystemWarningContent(
    isLowStorage: Boolean,
    isLowBattery: Boolean,
    formattedStorageSize: String,
    batteryLevel: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Warning header
        Text(
            text = "System Warning",
            color = RecordRed,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MediumLightGray.copy(alpha = 0.3f))
        )
        
        // Storage warning
        if (isLowStorage) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Storage",
                    color = MediumLightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Low storage: $formattedStorageSize available",
                    color = RecordRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        if (isLowStorage && isLowBattery) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MediumLightGray.copy(alpha = 0.3f))
            )
        }
        
        // Battery warning
        if (isLowBattery) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Battery",
                    color = MediumLightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Battery level: ${batteryLevel}%",
                    color = RecordRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

