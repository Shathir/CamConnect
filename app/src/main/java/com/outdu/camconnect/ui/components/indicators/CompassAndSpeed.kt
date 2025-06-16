package com.outdu.camconnect.ui.components.indicators

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import com.outdu.camconnect.ui.theme.*
import com.outdu.camconnect.ui.theme.AppColors.PrimaryText
import com.outdu.camconnect.ui.theme.AppColors.SecondaryText

/**
 * Directional compass component with live direction feedback
 */
@Composable
fun CompassIndicator(
    direction: Float, // Direction in degrees (0-360)
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 80.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCompass(direction)
        }
        
        // Direction text in the center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Degree text placeholder
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .width(32.dp)
                    .background(Color.Black.copy(alpha = 0.2f))
            )
            Spacer(modifier = Modifier.height(2.dp))
            // Direction label placeholder
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(16.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }
    }
}

private fun DrawScope.drawCompass(direction: Float) {
    val center = size.center
    val radius = size.minDimension / 2f - 8.dp.toPx()
    
    // Draw outer circle
    drawCircle(
        color = Color.Gray.copy(alpha = 0.3f),
        radius = radius,
        style = Stroke(width = 2.dp.toPx())
    )
    
    // Draw cardinal directions
    val cardinals = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
    cardinals.forEach { (label, angle) ->
        val angleRad = Math.toRadians((angle - 90).toDouble()).toFloat()
        val x = center.x + radius * 0.85f * cos(angleRad)
        val y = center.y + radius * 0.85f * sin(angleRad)
        
        // Draw direction markers
        drawCircle(
            color = if (angle == 0f) Color.Red else Color.Gray,
            radius = 3.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(x, y)
        )
    }
    
    // Draw compass needle
    rotate(degrees = direction, pivot = center) {
        // North pointer (red)
        drawLine(
            color = Color.Red,
            start = center,
            end = androidx.compose.ui.geometry.Offset(center.x, center.y - radius * 0.7f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        
        // South pointer (white)
        drawLine(
            color = Color.Gray,
            start = center,
            end = androidx.compose.ui.geometry.Offset(center.x, center.y + radius * 0.7f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
    
    // Center dot
    drawCircle(
        color = Color.DarkGray,
        radius = 4.dp.toPx()
    )
}

private fun getDirectionLabel(degrees: Float): String {
    return when ((degrees + 22.5) % 360) {
        in 0f..45f -> "N"
        in 45f..90f -> "NE"
        in 90f..135f -> "E"
        in 135f..180f -> "SE"
        in 180f..225f -> "S"
        in 225f..270f -> "SW"
        in 270f..315f -> "W"
        in 315f..360f -> "NW"
        else -> "N"
    }
}

/**
 * Speed indicator component showing speed from phone sensors
 */
@Composable
fun SpeedIndicator(
    speed: Float, // Speed in km/h
    modifier: Modifier = Modifier,
    unit: String = "km/h"
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LightGray.copy(alpha = 0.7f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Speed icon placeholder
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(BluePrimary)
            ) {
                // Speed meter indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape)
                        .align(Alignment.Center)
                )
            }
            Column {
                // Speed value placeholder
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(40.dp)
                        .background(Color.Black.copy(alpha = 0.2f))
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Unit placeholder
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .width(25.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
            }
        }
    }
}

/**
 * Compact speed display for minimal layouts
 */
@Composable
fun CompactSpeedIndicator(
    speed: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
            Text(
                text = "$speed",
                color = PrimaryText,
                fontFamily = FontFamily.SansSerif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "KMph",
                color = SecondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}