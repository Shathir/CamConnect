package com.outdu.camconnect.ui.components.buttons

import android.widget.Button
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.outdu.camconnect.ui.theme.*
import com.outdu.camconnect.ui.theme.AppColors.ButtonBorderColor
import com.outdu.camconnect.R
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType

/**
 * Data class for button configuration
 */
data class ButtonConfig(
    val id: String,
    val iconPlaceholder: String = "", // Drawable resource ID as string
    val text: String = "",
    val color: Color = Color.White,
    val backgroundColor: Color = DefaultColors.BluePrimary,
    val enabled: Boolean = true,
    val onClick: () -> Unit = {},
    val BorderColor: Color = Color.White
)

/**
 * Customizable button component with basic Compose elements
 */
@Composable
fun CustomizableButton(
    config: ButtonConfig,
    modifier: Modifier = Modifier,
    showText: Boolean = true,
    layout: String = "Row",
    isCompact: Boolean = false
) {

    val deviceType = rememberDeviceType()

    // Determine button size
    val buttonSize = if (isCompact) {

        if(deviceType == DeviceType.TABLET) 76.dp else 40.dp
    }else
    {
        if(deviceType == DeviceType.TABLET) 112.dp else 56.dp
    }
    
    // Check if we're in dark theme
    val isDarkTheme = isSystemInDarkTheme()


    // Outer box to ensure square shape
    Box(
        modifier = modifier
            .size(buttonSize) // Enforce square shape
    ) {
        // Inner button with styling
        Box(
            modifier = Modifier
                .fillMaxSize() // Fill the square parent
                .clip(RoundedCornerShape(if(deviceType == DeviceType.TABLET)20.dp else 14.dp))
                .background(
                    if (config.enabled) config.backgroundColor 
                    else config.backgroundColor.copy(alpha = 0.5f)
                )
                .border(
                    width = if (isDarkTheme) 0.dp else 1.dp, // No border in dark theme
                    color = config.BorderColor,
                    shape = RoundedCornerShape(if(deviceType == DeviceType.TABLET)20.dp else 14.dp)
                )
                .clickable(enabled = config.enabled) { config.onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isCompact || config.text.isEmpty() || !showText) {
                // Icon only mode
                // Validate and render  icon
                val resourceId = config.iconPlaceholder.toIntOrNull()
                if (resourceId != null && config.iconPlaceholder.isNotEmpty()) {
                    Image(
                        painter = painterResource(id = resourceId),
                        contentDescription = config.text,
                        modifier = Modifier.size(if (deviceType == DeviceType.TABLET) 24.dp else 16.dp),
                        colorFilter = ColorFilter.tint(config.color),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Fallback placeholder
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 20.dp else 24.dp)
                            .background(config.color, CircleShape)
                    )
                }
            } else {
                // Full mode with icon and text - arranged vertically to fit square shape
                if(layout == "Row") {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconLayout(config = config)
                    }

                }
                else {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconLayout(config = config)
                    }
                }

            }
        }
    }
}


@Composable
fun IconLayout(
    config : ButtonConfig
){
    // Validate and render icon
    val resourceId = config.iconPlaceholder.toIntOrNull()
    val deviceType = rememberDeviceType()
    if (resourceId != null && config.iconPlaceholder.isNotEmpty()) {
        Image(
            painter = painterResource(id = resourceId),
            contentDescription = null,
            modifier = Modifier.size(if(deviceType == DeviceType.TABLET) 24.dp else 16.dp),
            colorFilter = ColorFilter.tint(config.color),
            contentScale = ContentScale.Fit
        )
    }

    // Text
    Text(
        text = config.text,
        style = TextStyle(
            color = config.color,
            fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 12.sp,
            lineHeight = 14.02.sp,
            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
            fontWeight = FontWeight(500),
            textAlign = TextAlign.Center
        )
    )
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
    selectedTint: Color = DefaultColors.BluePrimary,
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