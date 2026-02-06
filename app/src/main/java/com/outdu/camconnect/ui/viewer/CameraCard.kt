package com.outdu.camconnect.ui.viewer

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.communication.MotocamAPIHelper
import com.outdu.camconnect.services.OnvifDevice
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.outdu.camconnect.ui.theme.*
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType
import kotlinx.coroutines.launch
import java.net.URI
import java.util.*

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
    var showForgotPinDialog by remember { mutableStateOf(false) }
    
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
            
            // Connect button and Forgot Pin button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f)
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
                
                // Forgot Pin button
                TextButton(
                    onClick = { showForgotPinDialog = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = StravionBlue
                    )
                ) {
                    Text(
                        text = "Forgot Pin?",
                        style = TextStyle(
                            fontSize = if(deviceType == DeviceType.TABLET) 14.sp else 12.sp,
                            fontWeight = FontWeight(600),
                            color = StravionBlue
                        )
                    )
                }
            }
        }
    }
    
    // Show Forgot Pin Dialog
    if (showForgotPinDialog) {
        ForgotPinDialog(
            camera = camera,
            onDismiss = { showForgotPinDialog = false }
        )
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

/**
 * Forgot Pin Dialog for resetting camera PIN
 */
@Composable
private fun ForgotPinDialog(
    camera: OnvifDevice,
    onDismiss: () -> Unit
) {
    var dob by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var newPinVisible by remember { mutableStateOf(false) }
    var confirmPinVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val deviceType = rememberDeviceType()
    val context = LocalContext.current
    val isDarkTheme = camConnectIsDarkTheme()
    
    // Calendar for date picker
    val calendar = Calendar.getInstance()
    
    // Date picker dialog
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                // Format as DD-MM-YYYY
                val formattedDate = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
                dob = formattedDate
                errorMessage = null
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
    
    AlertDialog(
        onDismissRequest = { 
            if (!isLoading) {
                onDismiss()
            }
        },
        title = {
            Column {
                Text(
                    text = "Reset PIN",
                    style = TextStyle(
                        fontSize = if (deviceType == DeviceType.TABLET) 22.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color(0xFF1A1A1C)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = extractCameraName(camera),
                    style = TextStyle(
                        fontSize = if (deviceType == DeviceType.TABLET) 14.sp else 12.sp,
                        color = if (isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF9097A0)
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Instructions
                Text(
                    text = "Enter your Date of Birth and set a new PIN to reset access",
                    style = TextStyle(
                        fontSize = if (deviceType == DeviceType.TABLET) 14.sp else 12.sp,
                        color = if (isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF666666)
                    )
                )
                
                // DOB Input with Calendar Picker
                OutlinedTextField(
                    value = dob,
                    onValueChange = { input ->
                        // Only allow digits and dashes
                        val filtered = input.filter { it.isDigit() || it == '-' }
                        
                        // Auto-format as DD-MM-YYYY
                        val formatted = when {
                            filtered.length <= 10 -> filtered
                            else -> dob // Don't allow more than 10 characters
                        }
                        
                        dob = formatted
                        errorMessage = null
                    },
                    label = { Text("Date of Birth") },
                    placeholder = { Text("DD-MM-YYYY") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    isError = errorMessage != null && errorMessage?.contains("DOB") == true,
                    trailingIcon = {
                        IconButton(
                            onClick = { 
                                if (!isLoading) {
                                    datePickerDialog.show()
                                }
                            },
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select date from calendar",
                                tint = if (isLoading) Color(0xFFCCCCCC) else StravionBlue
                            )
                        }
                    }
                )
                
                // New PIN Input
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() } && it.length <= 20) {
                            newPin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("New PIN") },
                    placeholder = { Text("Enter new PIN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (newPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Next
                    ),
                    isError = errorMessage != null && errorMessage?.contains("PIN") == true,
                    trailingIcon = {
                        IconButton(onClick = { newPinVisible = !newPinVisible }) {
                            Icon(
                                imageVector = if (newPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (newPinVisible) "Hide PIN" else "Show PIN"
                            )
                        }
                    }
                )
                
                // Confirm PIN Input
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() } && it.length <= 20) {
                            confirmPin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Confirm PIN") },
                    placeholder = { Text("Re-enter new PIN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (confirmPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    isError = errorMessage != null && errorMessage?.contains("match") == true,
                    trailingIcon = {
                        IconButton(onClick = { confirmPinVisible = !confirmPinVisible }) {
                            Icon(
                                imageVector = if (confirmPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPinVisible) "Hide PIN" else "Show PIN"
                            )
                        }
                    }
                )
            
                // Error message
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD32F2F)
                        )
                    )
                }
                
                // Success message
                successMessage?.let { success ->
                    Text(
                        text = success,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate inputs
                    when {
                        dob.isEmpty() -> {
                            errorMessage = "Please enter your Date of Birth"
                        }
                        dob.length != 10 -> {
                            errorMessage = "DOB must be exactly 10 characters (DD-MM-YYYY)"
                        }
                        !dob.matches(Regex("\\d{2}-\\d{2}-\\d{4}")) -> {
                            errorMessage = "Invalid DOB format. Must be DD-MM-YYYY"
                        }
                        !isValidDobFormat(dob) -> {
                            errorMessage = "Invalid date. Please check day, month, and year"
                        }
                        newPin.isEmpty() -> {
                            errorMessage = "Please enter a new PIN"
                        }
                        !newPin.all { it.isDigit() } -> {
                            errorMessage = "PIN must contain only digits"
                        }
                        newPin.length < 4 -> {
                            errorMessage = "PIN must be at least 4 digits"
                        }
                        confirmPin.isEmpty() -> {
                            errorMessage = "Please confirm your PIN"
                        }
                        newPin != confirmPin -> {
                            errorMessage = "PINs do not match"
                        }
                        else -> {
                            // All validations passed, call API
                            isLoading = true
                            errorMessage = null
                            
                                scope.launch {
                                    try {
                                        Log.d("CameraCard", "Starting PIN reset - PIN: $newPin, DOB: $dob")
                                        val result = resetPin(camera.ipAddress, newPin, dob)
                                        if (result) {
                                            Log.d("CameraCard", "PIN reset successful")
                                            successMessage = "PIN reset successfully!"
                                            kotlinx.coroutines.delay(2000)
                                            onDismiss()
                                        } else {
                                            Log.e("CameraCard", "PIN reset returned false")
                                            errorMessage = "Failed to reset PIN. Please try again."
                                        }
                                    } catch (e: Exception) {
                                        Log.e("CameraCard", "PIN reset exception: ${e.message}", e)
                                        val errorMsg = when {
                                            e.message?.contains("-5") == true -> "Invalid DOB format or length"
                                            e.message?.contains("-6") == true -> "User DOB not set in system"
                                            e.message?.contains("-7") == true -> "User DOB validation failed (mismatch)"
                                            else -> e.message ?: "Unknown error"
                                        }
                                        errorMessage = "Error: $errorMsg"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StravionBlue
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Set Pin")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { if (!isLoading) onDismiss() },
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        },
        containerColor = if (isDarkTheme) Color(0xFF1A1A1C) else Color.White
    )
}

/**
 * Validates date of birth format (DD-MM-YYYY)
 * @param dob The date string to validate
 * @return true if valid, false otherwise
 */
private fun isValidDobFormat(dob: String): Boolean {
    // Check basic format
    if (!dob.matches(Regex("\\d{2}-\\d{2}-\\d{4}"))) {
        return false
    }
    
    try {
        // Parse and validate actual date
        val parts = dob.split("-")
        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val year = parts[2].toInt()
        
        // Basic range checks
        if (day < 1 || day > 31) return false
        if (month < 1 || month > 12) return false
        if (year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR)) return false
        
        // Validate using Calendar
        val calendar = Calendar.getInstance()
        calendar.isLenient = false
        calendar.set(year, month - 1, day)
        calendar.time // This will throw exception if invalid date
        
        return true
    } catch (e: Exception) {
        return false
    }
}

/**
 * Reset PIN by calling the camera's API via MotocamSocketClient
 * 
 * @param ipAddress The IP address of the camera
 * @param pin The new PIN to set
 * @param dob The date of birth for verification (format: DD-MM-YYYY)
 * @return true if the PIN was reset successfully, false otherwise
 * @throws Exception if there's an error during the API call or response parsing
 */
private suspend fun resetPin(ipAddress: String, pin: String, dob: String): Boolean = withContext(Dispatchers.IO) {
    return@withContext try {
        Log.d("CameraCard", "Resetting PIN for camera at $ipAddress")
        Log.d("CameraCard", "PIN: $pin (length: ${pin.length}), DOB: $dob")
        
        // Build command using MotocamAPIHelper
        val commandArray = MotocamAPIHelper.resetLoginPinCmd(pin, dob)
        
        // Convert to byte array
        val commandBytes = ByteArray(commandArray.size) { i -> commandArray[i].toByte() }
        
        // Calculate CRC
        val sum = commandBytes.dropLast(1).sumOf { it.toInt() and 0xFF }
        commandBytes[commandBytes.size - 1] = ((sum xor 0xFF) + 1).toByte()
        
        // Format as hex string
        val hexString = commandBytes.joinToString(" ") { byte ->
            "0x" + (byte.toInt() and 0xFF).toString(16).padStart(2, '0').uppercase()
        }
        
        Log.d("CameraCard", "Sending command: $hexString")
        
        // Create HTTP client
        val client = HttpClient(CIO) {
            engine {
                requestTimeout = 30_000L
                endpoint {
                    connectTimeout = 30_000L
                    connectAttempts = 1
                }
            }
        }
        
        // Send POST request to /api/reset_pin
        val url = "http://$ipAddress/api/reset_pin"
        Log.d("CameraCard", "Posting to: $url")
        
        val responseText: String = client.use { httpClient ->
            httpClient.post(url) {
                contentType(ContentType.Text.Plain)
                setBody(hexString)
                headers {
                    append(HttpHeaders.ContentType, "application/octet-stream")
                }
            }.body()
        }
        
        Log.d("CameraCard", "Response: $responseText")
        
        // Parse response
        if (responseText.isEmpty()) {
            throw Exception("Empty response from camera")
        }
        
        // Convert response hex string to byte array
        val responseBytes = responseText.trim()
            .split(Regex("\\s+"))
            .map { token ->
                token.removePrefix("0x").toInt(16).toByte()
            }
            .toByteArray()
        
        // Convert to int array for parsing
        val responseArray = IntArray(responseBytes.size) { i -> responseBytes[i].toInt() and 0xFF }
        
        // Parse response using MotocamAPIHelper
        val result = MotocamAPIHelper.resetLoginPinCmdResponseParse(responseArray, responseArray.size)
        
        Log.d("CameraCard", "PIN reset successful: $result")
        result
        
    } catch (e: Exception) {
        Log.e("CameraCard", "Error resetting PIN: ${e.message}", e)
        throw e
    }
}
