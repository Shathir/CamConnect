package com.outdu.camconnect.ui.viewer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.outdu.camconnect.services.OnvifDevice
import com.outdu.camconnect.ui.theme.*

/**
 * PIN authentication dialog specifically for viewer camera connection
 */
@Composable
fun ViewerPinAuthDialog(
    camera: OnvifDevice,
    isAuthenticating: Boolean,
    authError: String?,
    onPinEntered: (String) -> Unit,
    onDismiss: () -> Unit,
    onClearAuthError: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    
    // Extract camera name for display
    val cameraName = remember(camera) {
        extractCameraName(camera)
    }
    
    Dialog(
        onDismissRequest = { if (!isAuthenticating) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isAuthenticating,
            dismissOnClickOutside = !isAuthenticating
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = DarkBackground2,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header with camera info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(32.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Camera",
                            modifier = Modifier.padding(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Text(
                        text = "Connect to Camera",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = White
                        ),
                        textAlign = TextAlign.Center
                    )
                }
                
                // Camera details card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSlate.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeviceHub,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MediumLightGray
                            )
                            Text(
                                text = cameraName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MediumLightGray
                            )
                            Text(
                                text = camera.ipAddress,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MediumLightGray
                                )
                            )
                        }
                    }
                }
                
                // Instructions
                Text(
                    text = "Enter the 4-digit PIN provided by the camera owner to connect and start streaming.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MediumLightGray
                    ),
                    textAlign = TextAlign.Center
                )
                
                // PIN input
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { newPin ->
                            if (newPin.length <= 4 && newPin.all { it.isDigit() }) {
                                pin = newPin
                                // Clear auth error when user starts typing
                                if (authError != null) {
                                    onClearAuthError()
                                }
                            }
                        },
                        label = { Text("PIN") },
                        placeholder = { Text("Enter 4-digit PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isAuthenticating,
                        isError = authError != null,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            unfocusedLabelColor = MediumLightGray,
                            unfocusedBorderColor = MediumGray,
                            errorBorderColor = RedVariant,
                            errorLabelColor = RedVariant,
                            errorTextColor = White
                        )
                    )
                    
                    // Error message
                    if (authError != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = RedVariant.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    modifier = Modifier.size(20.dp),
                                    tint = RedVariant
                                )
                                Text(
                                    text = authError,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = RedVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isAuthenticating,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MediumLightGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    // Connect button
                    Button(
                        onClick = { onPinEntered(pin) },
                        modifier = Modifier.weight(1f),
                        enabled = !isAuthenticating && pin.length == 4,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = White,
                            disabledContainerColor = MediumGray,
                            disabledContentColor = DarkGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connecting...")
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Connect")
                        }
                    }
                }
                
                // Additional help text
                if (!isAuthenticating) {
                    Text(
                        text = "Don't have the PIN? Contact the camera owner for access.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MediumGray
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
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
    
    // Generate default name based on IP
    return "Camera ${camera.ipAddress.split(".").last()}"
}
