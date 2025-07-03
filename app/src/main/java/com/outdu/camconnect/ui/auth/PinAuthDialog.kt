package com.outdu.camconnect.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outdu.camconnect.ui.theme.*

/**
 * PIN Authentication Dialog with numeric keypad and visual feedback
 */
@Composable
fun PinAuthDialog(
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PinAuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val haptic = LocalHapticFeedback.current
    
    // Handle success state
    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess) {
            onSuccess()
        }
    }
    
    Dialog(
        onDismissRequest = { if (!authState.isLoading) onCancel() },
        properties = DialogProperties(
            dismissOnBackPress = !authState.isLoading,
            dismissOnClickOutside = !authState.isLoading
        )
    ) {
        Surface(
            modifier = modifier
                .width(320.dp)
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header
                PinAuthHeader(
                    attemptsRemaining = authState.attemptsRemaining,
                    canAttempt = authState.canAttempt,
                    lockoutInfo = authState.lockoutInfo
                )
                
                // PIN Display
                PinDisplay(
                    pin = authState.pin,
                    isLoading = authState.isLoading,
                    hasError = authState.errorMessage != null,
                    lockoutInfo = authState.lockoutInfo
                )
                
                // Error Message
                if (authState.errorMessage != null) {
                    ErrorMessage(authState.errorMessage!!)
                }
                
                // Numeric Keypad or Loading Indicator
                if (!authState.isLoading) {
                    if (!authState.lockoutInfo.isLockedOut) {
                        NumericKeypad(
                            onDigitClick = { digit ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.addDigit(digit)
                            },
                            onBackspaceClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.removeLastDigit()
                            },
                            onClearClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.clearPin()
                            },
                            enabled = authState.canAttempt
                        )
                    } else {
                        // Show lockout information instead of keypad
                        LockoutDisplay(lockoutInfo = authState.lockoutInfo)
                    }
                } else {
                    // Loading indicator
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
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
                        enabled = !authState.isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = LightGray,
                            disabledContentColor = MediumGray
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    // Manual Submit Button (if user wants to submit before 4 digits)
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.authenticateWithPin()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !authState.isLoading && 
                                 authState.pin.isNotEmpty() && 
                                 authState.canAttempt && 
                                 !authState.lockoutInfo.isLockedOut,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = White,
                            disabledContainerColor = MediumGray,
                            disabledContentColor = DarkGray
                        )
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@Composable
private fun PinAuthHeader(
    attemptsRemaining: Int,
    canAttempt: Boolean,
    lockoutInfo: com.outdu.camconnect.auth.LockoutInfo
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (lockoutInfo.isLockedOut) Icons.Default.Timer else Icons.Default.Lock,
            contentDescription = "Security",
            modifier = Modifier.size(48.dp),
            tint = if (canAttempt) MaterialTheme.colorScheme.primary else RedVariant
        )
        
        Text(
            text = if (lockoutInfo.isLockedOut) "Account Locked" else "Enter PIN",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = White
            )
        )
        
        // Prominent countdown timer display for lockout
        if (lockoutInfo.isLockedOut) {
            val animatedAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "lockout_pulse"
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = RedVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Retry in:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MediumLightGray,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    
                    Text(
                        text = formatLockoutTime(lockoutInfo.remainingTime),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = White.copy(alpha = animatedAlpha)
                        ),
                        modifier = Modifier.scale(1.1f)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = RedVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Lockout Level ${lockoutInfo.sequenceCount}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = RedVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        } else {
            Text(
                text = when {
                    canAttempt -> {
                        "Enter your 4-digit PIN to continue"
                    }
                    else -> {
                        "Too many attempts. Please try again later."
                    }
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MediumLightGray,
                    textAlign = TextAlign.Center
                )
            )
            
            if (canAttempt && attemptsRemaining < 3) {
                Text(
                    text = "Attempts remaining: $attemptsRemaining",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (attemptsRemaining <= 1) RedVariant else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

/**
 * Format lockout time for user display
 */
private fun formatLockoutTime(timeMs: Long): String {
    val seconds = timeMs / 1000
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}

@Composable
private fun PinDisplay(
    pin: String,
    isLoading: Boolean,
    hasError: Boolean,
    lockoutInfo: com.outdu.camconnect.auth.LockoutInfo
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (hasError) 1.05f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "pin_display_scale"
    )
    
    if (lockoutInfo.isLockedOut) {
        // Show compact countdown instead of PIN dots when locked out
        Card(
            modifier = Modifier
                .scale(animatedScale)
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkSlate.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Timer",
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = formatLockoutTime(lockoutInfo.remainingTime),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                )
                Text(
                    text = "remaining",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MediumLightGray
                    )
                )
            }
        }
    } else {
        // Show normal PIN dots when not locked out
        Row(
            modifier = Modifier
                .scale(animatedScale)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                PinDigitIndicator(
                    isFilled = index < pin.length,
                    isLoading = isLoading && index < pin.length,
                    hasError = hasError
                )
            }
        }
    }
}

@Composable
private fun PinDigitIndicator(
    isFilled: Boolean,
    isLoading: Boolean,
    hasError: Boolean
) {
    val animatedColor by androidx.compose.animation.animateColorAsState(
        targetValue = when {
            hasError -> RedVariant
            isFilled -> MaterialTheme.colorScheme.primary
            else -> MediumGray
        },
        animationSpec = tween(durationMillis = 200),
        label = "pin_digit_color"
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isFilled) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pin_digit_scale"
    )
    
    Box(
        modifier = Modifier
            .size(16.dp)
            .scale(animatedScale)
            .clip(CircleShape)
            .background(color = animatedColor)
            .border(
                width = 2.dp,
                color = if (isFilled) Color.Transparent else MediumGray,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                color = White,
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300),
        label = "error_alpha"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = RedVariant.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = RedVariant,
                modifier = Modifier.size(20.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RedVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                // Show additional time information for lockout errors
                if (message.contains("Account locked")) {
                    Text(
                        text = "Check the timer above for remaining time",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MediumLightGray,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Rows 1-3: Numbers 1-9
        repeat(3) { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) { col ->
                    val digit = (row * 3 + col + 1).toString()
                    KeypadButton(
                        text = digit,
                        onClick = { onDigitClick(digit) },
                        enabled = enabled
                    )
                }
            }
        }
        
        // Row 4: Clear, 0, Backspace
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeypadButton(
                icon = Icons.Default.Clear,
                onClick = onClearClick,
                enabled = enabled
            )
            KeypadButton(
                text = "0",
                onClick = { onDigitClick("0") },
                enabled = enabled
            )
            KeypadButton(
                icon = Icons.Default.Backspace,
                onClick = onBackspaceClick,
                enabled = enabled
            )
        }
    }
}

@Composable
private fun KeypadButton(
    text: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "keypad_button_scale"
    )
    
    Surface(
        modifier = Modifier
            .size(64.dp)
            .scale(animatedScale)
            .clickable(enabled = enabled) { onClick() },
        shape = CircleShape,
        color = if (enabled) DarkSlate else MediumGray,
        tonalElevation = if (enabled) 4.dp else 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            when {
                text != null -> {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (enabled) White else DarkGray
                        )
                    )
                }
                icon != null -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) White else DarkGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LockoutDisplay(
    lockoutInfo: com.outdu.camconnect.auth.LockoutInfo
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkBackground2.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Block,
                contentDescription = "Input Blocked",
                tint = MediumGray,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = "PIN Input Disabled",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MediumGray,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Wait for the timer above to complete",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MediumLightGray
                ),
                textAlign = TextAlign.Center
            )
        }
    }
} 