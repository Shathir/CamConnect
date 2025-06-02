package com.outdu.camconnect.ui.components.controls

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border

/**
 * Recording toggle button with animated state
 */
@Composable
fun RecordingToggle(
    isRecording: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recordColor by animateColorAsState(
        targetValue = if (isRecording) Color.Red else Color.Gray,
        label = "record_color"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 0.8f else 1f,
        label = "record_scale"
    )

    Box(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable { onToggle() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .clip(CircleShape)
                .background(recordColor)
        ) {
            if (isRecording) {
                // Show stop icon when recording
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                )
            }
        }
    }
}

/**
 * Scrollable zoom selector with predefined zoom levels
 */
@Composable
fun ZoomSelector(
    currentZoom: Float,
    onZoomSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
    availableZoomLevels: List<Float> = listOf(1f, 2f, 4f, 8f)
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(availableZoomLevels) { zoomLevel ->
            ZoomChip(
                zoomLevel = zoomLevel,
                isSelected = currentZoom == zoomLevel,
                onClick = { onZoomSelected(zoomLevel) }
            )
        }
    }
}

@Composable
private fun ZoomChip(
    zoomLevel: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Color(0xFF2196F3) else Color.Gray.copy(alpha = 0.3f)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Zoom level text placeholder
        Box(
            modifier = Modifier
                .height(14.dp)
                .width(20.dp)
                .background(
                    if (isSelected) Color.White.copy(alpha = 0.8f)
                    else Color.Black.copy(alpha = 0.3f)
                )
        )
    }
}

/**
 * Camera switch button for toggling between multiple cameras
 */
@Composable
fun CameraSwitchButton(
    currentCamera: Int,
    totalCameras: Int,
    onSwitch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable { onSwitch() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Camera icon placeholder
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2196F3))
        ) {
            // Camera lens
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
                    .align(Alignment.Center)
            )
        }
        // Badge with camera number
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color.Red)
                .align(Alignment.TopEnd),
            contentAlignment = Alignment.Center
        ) {
            // Number placeholder
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color.White)
            )
        }
    }
}

/**
 * Expand/collapse button for layout transitions
 */
@Composable
fun ExpandButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(
                topStart = 16.dp,
                bottomStart = 16.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp
            ))
            .background(Color(0xFFE0E0E0).copy(alpha = 0.9f))
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Arrow icon placeholder
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Arrow shape using lines
            val arrowColor = Color.Gray
            Canvas(modifier = Modifier.size(16.dp)) {
                val centerY = size.height / 2
                val startX = if (isExpanded) size.width * 0.3f else size.width * 0.7f
                val endX = if (isExpanded) size.width * 0.7f else size.width * 0.3f
                
                // Draw arrow lines
                drawLine(
                    color = arrowColor,
                    start = androidx.compose.ui.geometry.Offset(startX, centerY - 6.dp.toPx()),
                    end = androidx.compose.ui.geometry.Offset(endX, centerY),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = arrowColor,
                    start = androidx.compose.ui.geometry.Offset(startX, centerY + 6.dp.toPx()),
                    end = androidx.compose.ui.geometry.Offset(endX, centerY),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

/**
 * Settings button with gear icon placeholder
 */
@Composable
fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Gear icon placeholder
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
        ) {
            // Inner gear circle
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .align(Alignment.Center)
            )
        }
    }
}

/**
 * Toggleable icon row for Layout 2 (Row 3)
 */
@Composable
fun ToggleableIconRow(
    icons: List<ToggleableIcon>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        icons.take(6).forEach { iconData ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onToggle(iconData.id) }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Validate and render icon
                val resourceId = iconData.iconPlaceholder.toIntOrNull()
                if (resourceId != null && iconData.iconPlaceholder.isNotEmpty()) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = resourceId),
                        contentDescription = iconData.description,
                        modifier = Modifier.size(24.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            if (iconData.isSelected) Color(0xFF2196F3)
                            else Color.Gray.copy(alpha = 0.5f)
                        ),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                } else {
                    // Fallback placeholder
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (iconData.isSelected) Color(0xFF2196F3)
                                else Color.Gray.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}

/**
 * Data class for toggleable icons
 */
data class ToggleableIcon(
    val id: String,
    val iconPlaceholder: String, // Drawable resource ID as string
    val description: String,
    val isSelected: Boolean = false
) 