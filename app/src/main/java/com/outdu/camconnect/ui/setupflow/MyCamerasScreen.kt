package com.outdu.camconnect.ui.setupflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue

@Composable
fun MyCamerasScreen(
    onScanQR: () -> Unit,
    onConnectAndStream: (String) -> Unit = {},
    cameras: List<CameraInfo> = emptyList()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.stravion_logo),
                    contentDescription = "Stravion Logo",
                    tint = StravionBlue,
                    modifier = Modifier
                        .width(120.dp)
                        .height(20.dp)
                )
            }

            // Title
            Text(
                text = "My Cameras",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontFamily = FontFamily(Font(R.font.arial_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF1A1A1C)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (cameras.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // QR Code icon
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                StravionBlue,
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // QR code pattern
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(3) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(3) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    Color.White,
                                                    RoundedCornerShape(2.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Add Camera",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontFamily = FontFamily(Font(R.font.arial_regular)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFF1A1A1C)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Scan QR Code on back of camera",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.arial_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF9097A0)
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Scan QR button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(25.dp))
                            .background(StravionBlue)
                            .clickable { onScanQR() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.camera_line),
                                contentDescription = "Scan QR",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )

                            Text(
                                text = "Scan QR",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily(Font(R.font.arial_regular)),
                                    fontWeight = FontWeight(500),
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            } else {
                // Show cameras list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    cameras.forEach { camera ->
                        CameraCard(
                            camera = camera,
                            onConnectAndStream = { onConnectAndStream(camera.id) }
                        )
                    }

                    // Add new camera card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color(0xFFE0E0E0)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onScanQR() }
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Plus icon
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        Color(0xFFF5F5F5),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.camera_line),
                                    contentDescription = "Add Camera",
                                    tint = Color(0xFF9097A0),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Add New Camera",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                                        fontWeight = FontWeight(500),
                                        color = Color(0xFF1A1A1C)
                                    )
                                )

                                Text(
                                    text = "Scan QR code at the back to add & register camera",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                                        fontWeight = FontWeight(400),
                                        color = Color(0xFF9097A0)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Scan QR Code button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(StravionBlue)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Scan QR Code",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                                        fontWeight = FontWeight(500),
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            // AI Licenses notification (always show at bottom)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF4E6)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // AI icon with yellow background
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Color(0xFFFFB800),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ai_line),
                            contentDescription = "AI",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "You have 3 AI licenses ready to be activated.",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(R.font.arial_regular)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF1A1A1C)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CameraCard(
    camera: CameraInfo,
    onConnectAndStream: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFFE0E0E0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Camera icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color(0xFFF5F5F5),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.camera_line),
                        contentDescription = "Camera",
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = camera.name,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.arial_regular)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFF1A1A1C)
                        )
                    )

                    Text(
                        text = "MAC ${camera.macId}  IP ${camera.ipAddress}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(R.font.arial_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF9097A0)
                        )
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Connect & Stream button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(StravionBlue)
                        .clickable { onConnectAndStream() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Connect & Stream",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.arial_regular)),
                            fontWeight = FontWeight(500),
                            color = Color.White
                        ),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                // Activate AI button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            1.dp,
                            StravionBlue,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { /* Handle activate AI */ },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Activate AI",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.arial_regular)),
                            fontWeight = FontWeight(500),
                            color = StravionBlue
                        ),
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

// Data class for camera information
data class CameraInfo(
    val id: String,
    val name: String,
    val macId: String,
    val ipAddress: String,
    val serialNumber: String? = null,
    val manufacturedDate: String? = null
)
