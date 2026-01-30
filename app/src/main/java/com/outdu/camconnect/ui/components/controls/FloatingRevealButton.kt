package com.outdu.camconnect.ui.components.controls

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Floating button that appears when controls are hidden
 * Can be clicked or swiped left to reveal controls
 */
@Composable
fun FloatingRevealButton(
    onReveal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "button_scale"
    )
    
    // Detect swipe left gesture (drag threshold)
    val swipeThreshold = 100f
    
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(48.dp)
                .scale(scale)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (dragOffset < -swipeThreshold) {
                                // Swipe left detected - reveal controls
                                onReveal()
                            }
                            dragOffset = 0f
                            isPressed = false
                        },
                        onDrag = { _, dragAmount ->
                            dragOffset += dragAmount.x
                            if (dragOffset < -swipeThreshold / 2) {
                                isPressed = true
                            }
                        }
                    )
                }
                .background(
                    color = Color.White.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .clickable(onClick = {
                    onReveal()
                }),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Reveal Controls",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Floating button that appears when controls are visible
 * Can be clicked or swiped right to hide controls (fade/slide out via parent AnimatedVisibility)
 */
@Composable
fun FloatingHideButton(
    onHide: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "button_scale_hide"
    )

    // Detect swipe right gesture (drag threshold)
    val swipeThreshold = 100f

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(48.dp)
                .scale(scale)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (dragOffset > swipeThreshold) {
                                // Swipe right detected - hide controls
                                onHide()
                            }
                            dragOffset = 0f
                            isPressed = false
                        },
                        onDrag = { _, dragAmount ->
                            dragOffset += dragAmount.x
                            if (dragOffset > swipeThreshold / 2) {
                                isPressed = true
                            }
                        }
                    )
                }
                .background(
                    color = Color.White.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .clickable(onClick = { onHide() }),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Hide Controls",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

