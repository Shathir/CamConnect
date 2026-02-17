package com.outdu.camconnect.ui.components.camera

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.outdu.camconnect.ui.theme.DarkBackground2
import com.outdu.camconnect.ui.theme.White
import kotlinx.coroutines.delay

/**
 * Shared content box for tooltips with loading animation
 * Displays content for the active tooltip with smooth transitions
 */
@Composable
fun TooltipContentBox(
    activeTooltipId: String?,
    content: @Composable (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(false) }
    var currentContent by remember { mutableStateOf<String?>(null) }
    
    // Trigger loading animation when tooltip changes
    LaunchedEffect(activeTooltipId) {
        if (activeTooltipId != null && activeTooltipId != currentContent) {
            isLoading = true
            delay(200) // Short loading animation
            currentContent = activeTooltipId
            isLoading = false
        } else if (activeTooltipId == null) {
            currentContent = null
        }
    }
    
    if (currentContent != null || isLoading) {
        Box(
            modifier = modifier
                .widthIn(min = 200.dp, max = 250.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkBackground2.copy(alpha = 0.95f))
                .padding(16.dp)
        ) {
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tooltip_content"
            ) { loading ->
                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = White.copy(alpha = 0.8f),
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    currentContent?.let { content(it) }
                }
            }
        }
    }
}
