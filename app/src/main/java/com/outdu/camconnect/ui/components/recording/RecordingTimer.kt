package com.outdu.camconnect.ui.components.recording

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.ui.models.RecordingState
import com.outdu.camconnect.ui.theme.RecordRed
import kotlinx.coroutines.delay

//@Composable
//fun RecordingTimer(
//    recordingState: RecordingState,
//    modifier: Modifier = Modifier,
//    showBackground: Boolean = true
//) {
//    AnimatedVisibility(
//        visible = recordingState is RecordingState.Recording,
//        enter = fadeIn(),
//        exit = fadeOut(),
//        modifier = modifier
//    ) {
//        Row(
//            modifier = if (showBackground) {
//                Modifier
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(RecordRed)
//                    .padding(horizontal = 8.dp, vertical = 4.dp)
//            } else {
//                Modifier
//            },
//            horizontalArrangement = Arrangement.spacedBy(4.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Pulsing dot
//            Box(
//                modifier = Modifier
//                    .size(8.dp)
//                    .clip(RoundedCornerShape(4.dp))
//                    .background(Color.White)
//            )
//
//            // Timer text with horizontal slide animation
//            AnimatedVisibility(
//                visible = recordingState is RecordingState.Recording,
//                enter = slideInHorizontally(
//                    initialOffsetX = { -40 }, // Slide in from left
//                ) + fadeIn(),
//                exit = slideOutHorizontally(
//                    targetOffsetX = { 40 } // Slide out to right
//                ) + fadeOut()
//            ) {
//                if (recordingState is RecordingState.Recording) {
//                    Text(
//                        text = recordingState.duration,
//                        color = Color.White,
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }
//        }
//    }
//}



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RecordingTimer(
    recordingState: RecordingState,
    modifier: Modifier = Modifier,
    showBackground: Boolean = true
) {
    AnimatedVisibility(
        visible = recordingState is RecordingState.Recording,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = if (showBackground) {
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(RecordRed)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            } else {
                Modifier
            },
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🔴 Dot pulse synced with seconds
            val pulseState = remember { mutableStateOf(false) }

            // Toggle pulse every second
            LaunchedEffect(recordingState) {
                while (recordingState is RecordingState.Recording) {
                    pulseState.value = true
                    delay(150) // Pulse "on"
                    pulseState.value = false
                    delay(850) // Pulse "off" (total = 1s)
                }
            }

            val scale by animateFloatAsState(
                targetValue = if (pulseState.value) 1.3f else 1f,
                animationSpec = tween(durationMillis = 150),
                label = "ScaleAnimation"
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .background(Color.White)
            )

            // ⏱ Timer Text with smooth entry/exit
//            AnimatedContent(
//                targetState = if (recordingState is RecordingState.Recording) recordingState.duration else "",
//                transitionSpec = {
//                    slideInHorizontally { -40 } + fadeIn() with
//                            slideOutHorizontally { 40 } + fadeOut()
//                },
//                label = "RecordingDurationTransition"
//            ) { duration ->
            if(recordingState is RecordingState.Recording) {
                Text(
                    text = recordingState.duration,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
//            }
        }
    }
}
