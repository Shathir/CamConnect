package com.outdu.camconnect.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.outdu.camconnect.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dialog for prompting user to enter a custom filename for recordings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilenamePromptDialog(
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filename by remember { mutableStateOf(generateDefaultFilename()) }
    var isError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Request focus when dialog opens (delay so focusRequester is attached to the text field)
    LaunchedEffect(Unit) {
        delay(100)
        try {
            focusRequester.requestFocus()
        } catch (_: IllegalStateException) {
            // FocusRequester not yet attached (e.g. dialog window not ready)
        }
    }
    
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = modifier
                .width(340.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = DarkBackground2,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoFile,
                        contentDescription = "Video File",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Save Recording",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    )
                    
                    Text(
                        text = "Enter a name for your recording",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MediumLightGray,
                            textAlign = TextAlign.Center
                        )
                    )
                }
                
                // Filename Input Field
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = filename,
                        onValueChange = { newValue ->
                            filename = newValue.take(50) // Limit filename length
                            isError = !isValidFilename(newValue)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        label = { Text("Filename") },
                        suffix = { Text(".mp4", color = MediumLightGray) },
                        isError = isError,
                        supportingText = {
                            if (isError) {
                                Text(
                                    text = "Invalid filename. Avoid special characters: < > : \" | ? * \\",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            } else {
                                Text(
                                    text = "${filename.length}/50 characters",
                                    color = MediumLightGray,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                if (!isError && filename.isNotBlank()) {
                                    onConfirm(filename.trim())
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MediumGray,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MediumLightGray,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = White,
                            unfocusedTextColor = White
                        ),
                        singleLine = true
                    )
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = LightGray
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    // Save Button
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            onConfirm(filename.trim())
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isError && filename.trim().isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = White,
                            disabledContainerColor = MediumGray,
                            disabledContentColor = DarkGray
                        )
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

/**
 * Generate a default filename based on current timestamp
 */
private fun generateDefaultFilename(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    return "recording_${dateFormat.format(Date())}"
}

/**
 * Validate filename for common filesystem restrictions
 */
private fun isValidFilename(filename: String): Boolean {
    if (filename.isBlank()) return false
    
    // Check for invalid characters
    val invalidChars = listOf('<', '>', ':', '"', '|', '?', '*', '\\', '/')
    if (filename.any { it in invalidChars }) return false
    
    // Check for reserved names (Windows)
    val reservedNames = listOf("CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9")
    if (filename.uppercase() in reservedNames) return false
    
    // Check if filename ends with a period or space
    if (filename.endsWith('.') || filename.endsWith(' ')) return false
    
    return true
}
