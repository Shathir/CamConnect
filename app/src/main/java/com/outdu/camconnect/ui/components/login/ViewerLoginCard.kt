package com.outdu.camconnect.ui.components.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.outdu.camconnect.viewmodels.SetupState

@Composable
fun ViewerLoginCard(
    setupState: SetupState,
    onUpdateDetails: (String, String, String, String) -> Unit,
    onPinEntered: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Enter PIN",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = pin,
                onValueChange = { newPin ->
                    // Only allow numbers and limit to 4 digits
                    if (newPin.length <= 4 && newPin.all { it.isDigit() }) {
                        pin = newPin
                        onPinEntered(newPin)
                    }
                },
                label = { Text("PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Enter the 4-digit PIN provided by the camera owner",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 