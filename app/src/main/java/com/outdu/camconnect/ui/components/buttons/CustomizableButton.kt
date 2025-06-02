package com.outdu.camconnect.ui.components.buttons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape

/**
 * Data class for button configuration
 */
data class ButtonConfig(
    val id: String,
    val iconPlaceholder: String = "", // Drawable resource ID as string
    val text: String = "",
    val color: Color = Color.White,
    val backgroundColor: Color = Color(0xFF2196F3),
    val enabled: Boolean = true,
    val onClick: () -> Unit = {}
)

/**
 * Customizable button component with basic Compose elements
 */
@Composable
fun CustomizableButton(
    config: ButtonConfig,
    modifier: Modifier = Modifier,
    showText: Boolean = true,
    isCompact: Boolean = false
) {
    val buttonModifier = modifier
        .clip(RoundedCornerShape(8.dp))
        .background(
            if (config.enabled) config.backgroundColor 
            else config.backgroundColor.copy(alpha = 0.5f)
        )
        .clickable(enabled = config.enabled) { config.onClick() }
        .padding(
            horizontal = if (isCompact) 8.dp else 12.dp,
            vertical = if (isCompact) 6.dp else 10.dp
        )

    if (isCompact || config.text.isEmpty() || !showText) {
        // Icon only mode for compact layouts
        Box(
            modifier = buttonModifier.size(if (isCompact) 24.dp else 32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Validate and render icon
            val resourceId = config.iconPlaceholder.toIntOrNull()
            if (resourceId != null && config.iconPlaceholder.isNotEmpty()) {
                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = config.text,
                    modifier = Modifier.size(if (isCompact) 16.dp else 24.dp),
                    colorFilter = ColorFilter.tint(config.color),
                    contentScale = ContentScale.Fit
                )
            } else {
                // Fallback placeholder
                Box(
                    modifier = Modifier
                        .size(if (isCompact) 16.dp else 24.dp)
                        .background(config.color, CircleShape)
                )
            }
        }
    } else {
        // Full mode with icon and text
        Row(
            modifier = buttonModifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Validate and render icon
            val resourceId = config.iconPlaceholder.toIntOrNull()
            if (resourceId != null && config.iconPlaceholder.isNotEmpty()) {
                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(config.color),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            
            // Text without using Material Text composable
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = config.text,
                    color = config.color,
                    fontSize = if (isCompact) 12.sp else 14.sp
                )
            }
        }
    }
}

/**
 * Icon toggle button with drawable resource
 */
@Composable
fun IconToggleButton(
    iconPlaceholder: String, // Drawable resource ID as string
    contentDescription: String,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    selectedTint: Color = Color(0xFF2196F3),
    unselectedTint: Color = Color.Gray
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable { onToggle(!isSelected) }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Validate and render icon
        val resourceId = iconPlaceholder.toIntOrNull()
        if (resourceId != null && iconPlaceholder.isNotEmpty()) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(
                    if (isSelected) selectedTint else unselectedTint
                ),
                contentScale = ContentScale.Fit
            )
        } else {
            // Fallback placeholder
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (isSelected) selectedTint else unselectedTint,
                        CircleShape
                    )
            )
        }
    }
}

/**
 * Basic text composable replacement
 */
@Composable
fun BasicText(
    text: String,
    color: Color = Color.Black,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier) {
            // This is a placeholder for text rendering
            // In a real implementation, you would use drawText
        }
        // For now, using a colored box to indicate text presence
        Box(
            modifier = Modifier
                .height(fontSize.value.dp)
                .width((text.length * fontSize.value * 0.6f).dp)
                .background(color.copy(alpha = 0.3f))
        )
    }
} 