package com.outdu.camconnect.ui.components.settings.license

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.outdu.camconnect.R

@Composable
fun CameraInfoCard(
    title: String,
    macId: String,
    key: String,
    status: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(200.dp)
            .wrapContentHeight()
    ) {
        // Main Card (positioned to allow space for overlapping elements)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF333333)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 9.dp) // Add padding for overlapping elements
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, bottom = 40.dp, top = 20.dp) // Remove bottom content, add space
                    .wrapContentWidth()
            ) {
                // Title
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )

                // MAC ID
                Text(
                    text = "MAC ID: $macId",
                    color = Color(0xFF919191),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                // KEY field
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .fillMaxWidth() // Match the MAC ID text width
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center // Align text to start
                ) {

                    Text(
                        text = "KEY: $key",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Camera icon positioned outside/overlapping the card (top-left)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color(0xFF222222).copy(alpha = 1f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(2.dp)
                .zIndex(1f)
                .align(Alignment.TopStart)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color(0xFF2D2D2D),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(6.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera_skeleton),
                    contentDescription = "Camera Icon",
                    tint = Color(0xFFBCBCBC),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom popped out elements
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp), // Close spacing between elements
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 0.dp) // Position from right edge
        ) {
            // Gradient sparkle button (popped out)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = Color(0xFF222222).copy(alpha = 1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(2.dp) // Border thickness
                    .zIndex(1f)    // Ensures it sits above card
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF4E8EFF), Color(0xFFFFF399))
                            ),
                            shape = RoundedCornerShape(6.dp) // Slightly smaller than outer
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Magic",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Status Chip (popped out)
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFF222222).copy(alpha = 1f), // Shadow/border color
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(2.dp) // Border thickness
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFF29C96B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = status,
                        color = Color.White,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp)
                    )
                }
            }
        }
    }
}
