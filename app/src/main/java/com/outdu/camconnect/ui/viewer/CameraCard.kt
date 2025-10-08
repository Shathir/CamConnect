package com.outdu.camconnect.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.services.OnvifDevice
import com.outdu.camconnect.ui.theme.*
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType
import java.net.URI

/**
 * Card component displaying camera information with connect button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraCard(
    camera: OnvifDevice,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Extract camera name from scopes or use default
    val cameraName = extractCameraName(camera)
    val macAddress = extractMacAddress(camera)
    val deviceType = rememberDeviceType()
    
//    Card(
//        modifier = modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(16.dp))
//            .clickable { onSelected() },
//        colors = CardDefaults.cardColors(
//            containerColor = DarkBackground2
//        ),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = 8.dp,
//            pressedElevation = 12.dp
//        )
//    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .border(
                width = 1.dp,
                color = Color(0xFFD7D7D7),
                shape = RoundedCornerShape(16.dp)
            )
    )
    {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Header with camera icon and name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFD7D7D7),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Camera",
                        modifier = Modifier.padding(12.dp),
                        tint = StravionBlue
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    )
                    {
                        Text(
                            text = cameraName,
                            style = TextStyle(
                                fontSize = if(deviceType == DeviceType.TABLET) 24.sp else 16.sp,
                                fontWeight = FontWeight(700),
                                color = Color(0xFF1A1A1C)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        )
                        {
                            Surface(
                                modifier = Modifier.size(12.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = StravionBlue
                            ) {}

                            Text(
                                text = "Online",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = StravionBlue
                                ),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    // Camera details
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .height(48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        CameraDetailRow(
                            icon = Icons.Default.Computer,
                            label = "IpAddress",
                            value = camera.ipAddress
                        )

                        if (macAddress != null) {
                            CameraDetailRow(
                                icon = Icons.Default.DeviceHub,
                                label = "MAC Address",
                                value = macAddress
                            )
                        }

                        if (camera.endpointUrls.isNotEmpty()) {
                            CameraDetailRow(
                                icon = Icons.Default.Link,
                                label = "Endpoints",
                                value = "${camera.endpointUrls.size} available"
                            )
                        }
                    }
                }

            }
            
            // Connect button
//            Button(
//                onClick = onSelected,
//                modifier = Modifier.fillMaxWidth(0.5f),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    contentColor = White
//                ),
//                shape = RoundedCornerShape(12.dp)
//            )
            Box(
                modifier = Modifier.fillMaxWidth(if(deviceType == DeviceType.TABLET)0.5f else 0.7f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(StravionBlue)
                    .clickable {
                        onSelected()
                    },
                contentAlignment = Alignment.Center
            )
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = null,
                        modifier = Modifier.size(if(deviceType == DeviceType.TABLET) 24.dp else 18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Connect and Stream",
                        style = TextStyle(
                            fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
                            fontWeight = FontWeight(700),
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MediumLightGray
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MediumLightGray
            ),
            modifier = Modifier.width(80.dp)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = White,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Extract camera name from ONVIF device scopes or generate a default name
 */
private fun extractCameraName(camera: OnvifDevice): String {
    // Try to extract name from scopes
    camera.scopes.forEach { scope ->
        when {
            scope.contains("name/", ignoreCase = true) -> {
                val name = scope.substringAfterLast("/")
                if (name.isNotBlank()) return name
            }
            scope.contains("device_name/", ignoreCase = true) -> {
                val name = scope.substringAfterLast("/")
                if (name.isNotBlank()) return name
            }
            scope.contains("model/", ignoreCase = true) -> {
                val model = scope.substringAfterLast("/")
                if (model.isNotBlank()) return model
            }
        }
    }
    
    // Try to extract from endpoint URL hostname
    camera.endpointUrls.firstOrNull()?.let { url ->
        try {
            val uri = URI(url)
            val host = uri.host
            if (host != camera.ipAddress) {
                return host
            }
        } catch (e: Exception) {
            // Ignore parsing errors
        }
    }
    
    // Generate default name based on IP
    return "Camera ${camera.ipAddress.split(".").last()}"
}

/**
 * Extract MAC address from ONVIF device scopes
 */
private fun extractMacAddress(camera: OnvifDevice): String? {
    camera.scopes.forEach { scope ->
        when {
            scope.contains("mac/", ignoreCase = true) -> {
                val mac = scope.substringAfterLast("/")
                if (mac.isNotBlank() && mac.contains(":")) {
                    return mac.uppercase()
                }
            }
            scope.contains("hardware/", ignoreCase = true) -> {
                val hardware = scope.substringAfterLast("/")
                if (hardware.contains(":") && hardware.length >= 12) {
                    return hardware.uppercase()
                }
            }
        }
    }
    return null
}
