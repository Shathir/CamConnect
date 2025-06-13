package com.outdu.camconnect.ui.components.camera


import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.R
import com.outdu.camconnect.Viewmodels.AppViewModel
import com.outdu.camconnect.ui.layouts.maps.LiveTrackingMap
import com.outdu.camconnect.ui.layouts.maps.MapLibreTrackingScreen
import com.outdu.camconnect.ui.layouts.streamer.VideoSurfaceView


/**
 * Camera stream view component - main video display
 */
@Composable
fun CameraStreamView(
    modifier: Modifier = Modifier,
    isConnected: Boolean = true,
    cameraName: String = "Camera 1",
    context: Context,
    onSpeedUpdate: (Float) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2A2A2A)),
        contentAlignment = Alignment.Center
    ) {
        if (isConnected) {
            // TODO: Replace with actual camera view implementation
            // This is a placeholder for the actual camera stream
//            CameraPlaceholder(cameraName = cameraName)
//            Image(
//                painter = painterResource(R.drawable.stream_sample_visible),
//                contentDescription = "Stream Sample Icon",
//                modifier = Modifier.fillMaxSize(), // size like an icon
//                contentScale = ContentScale.FillBounds,
//                alignment = Alignment.Center,
//            )
            VideoSurfaceView(viewModel = AppViewModel(), context)
        } else {
            DisconnectedView()
        }
    }
}

/**
 * Placeholder for camera stream during development
 */
@Composable
private fun CameraPlaceholder(cameraName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Camera icon placeholder
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
                    .align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Camera name text placeholder
        Box(
            modifier = Modifier
                .height(18.dp)
                .width((cameraName.length * 10).dp)
                .background(Color.White.copy(alpha = 0.7f))
        )
        Spacer(modifier = Modifier.height(4.dp))
        // "Live Stream" text placeholder
        Box(
            modifier = Modifier
                .height(14.dp)
                .width(80.dp)
                .background(Color.White.copy(alpha = 0.5f))
        )
    }
}

/**
 * View shown when camera is disconnected
 */
@Composable
private fun DisconnectedView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Disconnected icon placeholder
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.Red.copy(alpha = 0.2f))
        ) {
            // X mark for disconnected
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color.Red.copy(alpha = 0.7f))
                        .align(Alignment.Center)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // "Camera Disconnected" text placeholder
        Box(
            modifier = Modifier
                .height(18.dp)
                .width(150.dp)
                .background(Color.Red.copy(alpha = 0.7f))
        )
        Spacer(modifier = Modifier.height(4.dp))
        // "Check your connection" text placeholder
        Box(
            modifier = Modifier
                .height(14.dp)
                .width(140.dp)
                .background(Color.White.copy(alpha = 0.5f))
        )
    }
}

/**
 * Small video feed slot for Layout 2 (Row 5)
 */
@Composable
fun VideoFeedSlot(
    modifier: Modifier = Modifier,
    label: String = "Secondary Feed"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Video icon placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                )
                {
                    Image(
                        painter = painterResource(id = R.drawable.boat_hologram), // your .png file
                        contentDescription = "My PNG Icon",
                        modifier = Modifier.fillMaxSize(), // size like an icon
                        contentScale = ContentScale.FillBounds,

                        alignment = Alignment.Center,
                    )
                }
//                Spacer(modifier = Modifier.height(8.dp))
//                // Label text placeholder
//                Box(
//                    modifier = Modifier
//                        .height(12.dp)
//                        .width((label.length * 7).dp)
//                        .background(Color.White.copy(alpha = 0.5f))
//                )

            }
        }
    }
}

/**
 * Snapshot image slot for Layout 2 (Row 5)
 */
@Composable
fun SnapshotSlot(
    modifier: Modifier = Modifier,
    hasSnapshot: Boolean = false,
    onSpeedUpdate: (Float) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.DarkGray)
    ) {

        LiveTrackingMap(onSpeedUpdate = onSpeedUpdate)
//        MapLibreTrackingScreen()


//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            if (hasSnapshot) {
//                // TODO: Display actual snapshot
//                // "Last Snapshot" text placeholder
//                Box(
//                    modifier = Modifier
//                        .height(14.dp)
//                        .width(100.dp)
//                        .background(Color.White.copy(alpha = 0.7f))
//                )
//            } else {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    // Photo icon placeholder
//                    Box(
//                        modifier = Modifier
//                            .size(32.dp)
//                            .clip(RoundedCornerShape(6.dp))
//                            .background(Color.White.copy(alpha = 0.2f))
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .size(20.dp)
//                                .clip(CircleShape)
//                                .background(Color.White.copy(alpha = 0.3f))
//                                .align(Alignment.Center)
//                        )
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//                    // "No Snapshot" text placeholder
//                    Box(
//                        modifier = Modifier
//                            .height(12.dp)
//                            .width(80.dp)
//                            .background(Color.White.copy(alpha = 0.5f))
//                    )
//                }
//            }
//        }
    }
}

/**
 * Camera info overlay that can be shown on top of the stream
 */
@Composable
fun CameraInfoOverlay(
    cameraName: String,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Camera name text placeholder
        Box(
            modifier = Modifier
                .height(14.dp)
                .width((cameraName.length * 8).dp)
                .background(Color.White.copy(alpha = 0.8f))
        )
        
        if (isRecording) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
                // "REC" text placeholder
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .width(25.dp)
                        .background(Color.Red.copy(alpha = 0.8f))
                )
            }
        }
    }
} 