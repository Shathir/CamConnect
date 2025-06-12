package com.outdu.camconnect.ui.layouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.ui.components.camera.CameraStreamView
import com.outdu.camconnect.ui.models.CameraState
import com.outdu.camconnect.ui.models.SystemStatus
import com.outdu.camconnect.R

/**
 * Layout 1: Minimal Control Panel (Stream Focused)
 * Left Pane: 90% - Live camera stream display
 * Right Pane: 10% - Compact controls and status indicators
 */
@Composable
fun MinimalControlLayout(
    cameraState: CameraState,
    systemStatus: SystemStatus,
    onSettingsClick: () -> Unit,
    onCameraSwitch: () -> Unit,
    onRecordingToggle: () -> Unit,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // Left Pane - Camera Stream (90%)
        Box(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
        ) {
            CameraStreamView(
                modifier = Modifier.fillMaxSize(),
                isConnected = systemStatus.isOnline,
                cameraName = "Camera ${cameraState.currentCamera + 1}",
                context = LocalContext.current
            )
        }
        
        // Right Pane - Minimal Controls (10%)
        Column(
            modifier = Modifier
                .weight(0.1f)
                .fillMaxHeight()
                .background(Color(0xFF1A1A1A)) // Very dark background
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section - Icons and Controls
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Settings icon
                Image(
                    painter = painterResource(id = R.drawable.settings_line),
                    contentDescription = "Settings",
                    colorFilter = ColorFilter.tint(Color.Gray),
                    modifier = Modifier.size(24.dp)
                )
                
                // Camera icon
                Image(
                    painter = painterResource(id = R.drawable.camera_lens_line),
                    contentDescription = "Camera",
                    colorFilter = ColorFilter.tint(Color.Gray),
                    modifier = Modifier.size(24.dp)
                )
                
                // Recording indicator - Red dot
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF4444)) // Bright red
                )
                
                // Compass showing "N"
                Box(
                    modifier = Modifier
                        .size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Yellow/Orange square indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFFFFCC00)) // Bright yellow/orange
                )
                
                // Green circular indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00DD00)) // Bright green
                )
                
                // Blue diamond indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(45f) // Rotate to make diamond shape
                        .background(Color(0xFF00AAFF)) // Bright blue
                )
            }
            
            // Bottom Section - Speed Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "149",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Mph",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
} 