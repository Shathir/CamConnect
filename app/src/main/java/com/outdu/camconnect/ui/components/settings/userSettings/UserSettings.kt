package com.outdu.camconnect.ui.components.settings.userSettings

import android.app.Activity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.R
import com.outdu.camconnect.SetupActivity
import com.outdu.camconnect.auth.SessionManager
import com.outdu.camconnect.communication.MotocamAPIAndroidHelper
import com.outdu.camconnect.ui.theme.AppColors.ButtonBorderColor
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import com.outdu.camconnect.ui.theme.camConnectIsDarkTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Intent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

@Composable
fun UserSettingsLayout(
    onUserActivity: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val isDarkTheme = camConnectIsDarkTheme()
    val context = LocalContext.current

    val titleFont = FontFamily(Font(R.font.just_sans_regular))
    val labelColor = if (isDarkTheme) Color.White else Color(0xFF2B2B2B)
    val descriptionColor = if (isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF9097A0)

    var showConfigResetConfirm by remember { mutableStateOf(false) }
    var showDobDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showConfigResetDatePicker by remember { mutableStateOf(false) }
    var configResetStep by remember { mutableStateOf(1) } // 1: enter DOB, 2: confirm text
    var configResetValidatedDate by remember { mutableStateOf<String?>(null) } // DD-MM-YYYY
    var logoutCountdownSeconds by remember { mutableStateOf(60) }
    var showResetProgress by remember { mutableStateOf(false) }
    var logoutJob by remember { mutableStateOf<Job?>(null) }

    // Store digits only (DDMMYYYY). Dashes are displayed via visualTransformation.
    var dobDigits by remember { mutableStateOf("") }
    var dobError by remember { mutableStateOf<String?>(null) }

    // Config reset requires a date payload too (DDMMYYYY digits-only)
    var configResetDigits by remember { mutableStateOf("") }
    var configResetError by remember { mutableStateOf<String?>(null) }

    var isSubmittingDob by remember { mutableStateOf(false) }
    var isSubmittingConfigReset by remember { mutableStateOf(false) }

    var lastStatusMessage by remember { mutableStateOf<String?>(null) }
    var lastStatusIsError by remember { mutableStateOf(false) }

    val isConfigResetDobValid = validateDobDigits(configResetDigits) != null
    val progressFraction = ((60 - logoutCountdownSeconds).coerceIn(0, 60)) / 60f
    val progressPercent = (progressFraction * 100).toInt().coerceIn(0, 100)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "User Settings",
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = titleFont,
                fontWeight = FontWeight(600),
                color = labelColor
            )
        )

        SettingCard(
            title = "Config Reset",
            subtitle = "Reset camera configuration to defaults (device-side).",
            buttonText = if (isSubmittingConfigReset) "Resetting..." else "Reset",
            isDestructive = true,
            enabled = !isSubmittingConfigReset && !isSubmittingDob,
            onClick = {
                // Start at step 1 (DOB input) each time
                configResetStep = 1
                configResetValidatedDate = null
                configResetError = null
                showConfigResetConfirm = true
            },
            isDarkTheme = isDarkTheme
        )

        SettingCard(
            title = "Set DOB",
            subtitle = "Set Date of Birth (DD-MM-YYYY) for the user profile on the camera.",
            buttonText = if (isSubmittingDob) "Saving..." else "Set DOB",
            isDestructive = false,
            enabled = !isSubmittingDob && !isSubmittingConfigReset,
            onClick = { showDobDialog = true },
            isDarkTheme = isDarkTheme
        )

        lastStatusMessage?.let { msg ->
            Text(
                text = msg,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = titleFont,
                    fontWeight = FontWeight(400),
                    color = if (lastStatusIsError) Color(0xFFD32F2F) else Color(0xFF1B8F3A)
                )
            )
        }
    }

    if (showConfigResetConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isSubmittingConfigReset) showConfigResetConfirm = false },
            title = {
                Text(
                    text = if (configResetStep == 1) "Enter DOB" else "Confirm Configuration Reset",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = titleFont,
                        fontWeight = FontWeight(600),
                        color = labelColor
                    )
                )
            },
            text = {
                if (configResetStep == 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Take the user input for DOB.",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = titleFont,
                                fontWeight = FontWeight(500),
                                color = descriptionColor
                            )
                        )

                        OutlinedTextField(
                            value = configResetDigits,
                            onValueChange = { raw ->
                                configResetError = null
                                configResetDigits = raw.onlyDigits().take(8)
                            },
                            singleLine = true,
                            isError = configResetError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = DobVisualTransformation(),
                            placeholder = { Text("DD-MM-YYYY") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = "Pick date",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable(enabled = !isSubmittingConfigReset) { showConfigResetDatePicker = true }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        configResetError?.let { err ->
                            Text(
                                text = err,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontFamily = titleFont,
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFD32F2F)
                                )
                            )
                        }
                    }
                } else {
                    val date = configResetValidatedDate ?: "DD-MM-YYYY"
                    Text(
                        text = "Confirm Configuration Reset\n\n" +
                            "This action will:\n\n" +
                            "Reset all configuration settings to defaults\n\n" +
                            "May require reconnecting to the device\n\n" +
                            "Device may restart or reload settings\n\n" +
                            "DOB: $date\n\n" +
                            "Are you sure you want to continue?",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = titleFont,
                            fontWeight = FontWeight(400),
                            color = descriptionColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !isSubmittingConfigReset && (configResetStep != 1 || isConfigResetDobValid),
                    onClick = {
                        if (configResetStep == 1) {
                            val resetDate = validateDobDigits(configResetDigits)
                            if (resetDate == null) {
                                configResetError = "Invalid date. Please use DD-MM-YYYY."
                                return@Button
                            }
                            // Proceed to confirmation step (text shown AFTER DOB entry)
                            configResetValidatedDate = resetDate
                            configResetStep = 2
                            onUserActivity()
                            return@Button
                        }

                        val resetDate = configResetValidatedDate
                        if (resetDate == null) {
                            // Safety fallback - send user back to step 1
                            configResetStep = 1
                            configResetError = "Please enter DOB first."
                            return@Button
                        }

                        isSubmittingConfigReset = true
                        lastStatusMessage = null

                        MotocamAPIAndroidHelper.configResetAsync(scope, resetDate) { ok, err ->
                            if (!ok) {
                                isSubmittingConfigReset = false
                                lastStatusIsError = true
                                lastStatusMessage = "Error: Config reset failed: ${err ?: "Unknown error"}"
                                return@configResetAsync
                            }

                            // If config reset succeeded, send reboot command (SYSTEM/SHUTDOWN sub=4).
                            MotocamAPIAndroidHelper.rebootAsync(scope) { rebootOk, rebootErr ->
                                lastStatusIsError = !rebootOk
                                lastStatusMessage = if (rebootOk) {
                                    "Success: Config reset applied. Rebooting. Logging out in 60 seconds..."
                                } else {
                                    "Warning: Config reset applied, reboot command failed: ${rebootErr ?: "Unknown error"}. Logging out in 60 seconds..."
                                }
                            }

                            // Keep the UI in non-hidden state while we wait (prevents the 20s auto-hide).
                            showConfigResetConfirm = false
                            showResetProgress = true
                            isSubmittingConfigReset = false
                            logoutCountdownSeconds = 60

                            logoutJob?.cancel()
                            logoutJob = scope.launch {
                                onUserActivity()
                                repeat(60) {
                                    delay(1_000)
                                    logoutCountdownSeconds = 59 - it
                                    onUserActivity()
                                }

                                // Clear local session and navigate to SetupActivity
                                SessionManager.clearSession()
                                SessionManager.clearLastConnectedCamera()
                                val intent = Intent(context, SetupActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                context.startActivity(intent)
                                (context as? Activity)?.finish()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(
                        text = if (configResetStep == 1) "Next" else "Confirm",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = titleFont,
                            fontWeight = FontWeight(500),
                            color = Color.White
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isSubmittingConfigReset,
                    onClick = {
                        if (configResetStep == 2) {
                            // Go back to DOB entry
                            configResetStep = 1
                            onUserActivity()
                        } else {
                            showConfigResetConfirm = false
                        }
                    }
                ) {
                    Text(
                        text = if (configResetStep == 2) "Back" else "Cancel",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = titleFont,
                            fontWeight = FontWeight(500),
                            color = labelColor
                        )
                    )
                }
            },
            containerColor = if (isDarkTheme) Color(0xFF1A1A1C) else Color.White
        )
    }

    if (showDobDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSubmittingDob) {
                    showDobDialog = false
                    dobError = null
                }
            },
            title = {
                Text(
                    text = "Set Date of Birth",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = titleFont,
                        fontWeight = FontWeight(600),
                        color = labelColor
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Format: DD-MM-YYYY",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = titleFont,
                            fontWeight = FontWeight(400),
                            color = descriptionColor
                        )
                    )

                    OutlinedTextField(
                        value = dobDigits,
                        onValueChange = { raw ->
                            dobError = null
                            dobDigits = raw.onlyDigits().take(8)
                        },
                        singleLine = true,
                        isError = dobError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = DobVisualTransformation(),
                        placeholder = { Text("DD-MM-YYYY") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Pick date",
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable(enabled = !isSubmittingDob) { showDatePicker = true }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    dobError?.let { err ->
                        Text(
                            text = err,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = titleFont,
                                fontWeight = FontWeight(400),
                                color = Color(0xFFD32F2F)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isSubmittingDob,
                    onClick = {
                        val validated = validateDobDigits(dobDigits)
                        if (validated == null) {
                            dobError = "Invalid date. Please use DD-MM-YYYY."
                            return@Button
                        }

                        isSubmittingDob = true
                        lastStatusMessage = null
                        MotocamAPIAndroidHelper.setUserDobAsync(scope, validated) { ok, err ->
                            isSubmittingDob = false
                            showDobDialog = false
                            dobError = null
                            lastStatusIsError = !ok
                            lastStatusMessage =
                                if (ok) "Success: DOB updated."
                                else "Error: DOB update failed: ${err ?: "Unknown error"}"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StravionBlue)
                ) {
                    Text(
                        text = "Save",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = titleFont,
                            fontWeight = FontWeight(500),
                            color = Color.White
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isSubmittingDob,
                    onClick = {
                        showDobDialog = false
                        dobError = null
                    }
                ) {
                    Text(
                        text = "Cancel",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = titleFont,
                            fontWeight = FontWeight(500),
                            color = labelColor
                        )
                    )
                }
            },
            containerColor = if (isDarkTheme) Color(0xFF1A1A1C) else Color.White
        )
    }

    if (showDatePicker) {
        DobDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { formatted ->
                dobError = null
                dobDigits = formatted.onlyDigits().take(8)
                showDatePicker = false
            }
        )
    }

    if (showConfigResetDatePicker) {
        DobDatePickerDialog(
            onDismiss = { showConfigResetDatePicker = false },
            onDateSelected = { formatted ->
                configResetError = null
                configResetDigits = formatted.onlyDigits().take(8)
                showConfigResetDatePicker = false
            }
        )
    }

    if (showResetProgress) {
        AlertDialog(
            onDismissRequest = { /* no-op */ },
            title = {
                Text(
                    text = "Applying reset...",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = titleFont,
                        fontWeight = FontWeight(600),
                        color = labelColor
                    )
                )
            },
            text = {
                val trackColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFE2E8F0)
                val shimmer = rememberInfiniteTransition(label = "reset_progress_shimmer")
                val shimmerX by shimmer.animateFloat(
                    initialValue = 0f,
                    targetValue = 400f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1200, easing = LinearEasing)
                    ),
                    label = "reset_progress_shimmer_x"
                )
                val livelyFill = Brush.linearGradient(
                    colors = listOf(
                        StravionBlue,
                        Color(0xFF6AA8FF),
                        StravionBlue
                    ),
                    start = Offset(shimmerX, 0f),
                    end = Offset(shimmerX + 200f, 0f)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Device is rebooting/reloading settings.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = titleFont,
                            fontWeight = FontWeight(400),
                            color = descriptionColor
                        )
                    )

                    Text(
                        text = "${progressPercent}%",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontFamily = titleFont,
                            fontWeight = FontWeight(700),
                            color = StravionBlue
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(trackColor)
                    ) {
                        // Filled portion
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(24.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(livelyFill)
                        )
                    }


                    Text(
                        text = "Logging out in ${logoutCountdownSeconds}s",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = titleFont,
                            fontWeight = FontWeight(500),
                            color = descriptionColor
                        )
                    )
                }
            },
            confirmButton = {},
            dismissButton = {},
            containerColor = if (isDarkTheme) Color(0xFF1A1A1C) else Color.White
        )
    }
}

@Composable
private fun SettingCard(
    title: String,
    subtitle: String,
    buttonText: String,
    isDestructive: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val titleFont = FontFamily(Font(R.font.just_sans_regular))
    val labelColor = if (isDarkTheme) Color.White else Color(0xFF2B2B2B)
    val descriptionColor = if (isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF9097A0)
    val cardBg = if (isDarkTheme) Color(0xFF1A1A1C) else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ButtonBorderColor, RoundedCornerShape(16.dp))
            .background(cardBg, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = titleFont,
                fontWeight = FontWeight(600),
                color = labelColor
            )
        )
        Text(
            text = subtitle,
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = titleFont,
                fontWeight = FontWeight(400),
                color = descriptionColor
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                enabled = enabled,
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) Color(0xFFD32F2F) else StravionBlue,
                    disabledContainerColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFE2E8F0)
                )
            ) {
                Text(
                    text = buttonText,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = titleFont,
                        fontWeight = FontWeight(500),
                        color = Color.White
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DobDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val state = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis
                    if (millis == null) {
                        onDismiss()
                        return@TextButton
                    }
                    val localDate =
                        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    val formatted = localDate.format(DateTimeFormatter.ofPattern("dd-MM-uuuu"))
                    onDateSelected(formatted)
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = state)
    }
}

private fun String.onlyDigits(): String = filter { it.isDigit() }

/**
 * Visual mask that displays digits as DD-MM-YYYY while storing only digits.
 */
private class DobVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.onlyDigits().take(8)

        val out = buildString {
            raw.forEachIndexed { i, c ->
                append(c)
                if (i == 1 && raw.length > 2) append('-')
                if (i == 3 && raw.length > 4) append('-')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, raw.length)
                var t = o
                if (o > 2) t += 1 // after DD-
                if (o > 4) t += 1 // after DD-MM-
                return t
            }

            override fun transformedToOriginal(offset: Int): Int {
                val t = offset.coerceIn(0, out.length)
                var o = t
                if (t > 2) o -= 1
                if (t > 5) o -= 1
                return o.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

private fun validateDobDigits(dobDigits: String): String? {
    val digits = dobDigits.onlyDigits().take(8)
    if (digits.length != 8) return null
    val trimmed = digits.substring(0, 2) + "-" + digits.substring(2, 4) + "-" + digits.substring(4, 8)
    return try {
        val fmt = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)
        LocalDate.parse(trimmed, fmt)
        trimmed
    } catch (_: Exception) {
        null
    }
}