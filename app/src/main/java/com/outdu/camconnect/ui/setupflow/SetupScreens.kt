package com.outdu.camconnect.ui.setupflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.outdu.camconnect.viewmodels.SetupState
import com.outdu.camconnect.viewmodels.SetupViewModel

@Composable
fun LandingScreen(
    onGetStarted: () -> Unit,
    onRegister: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to CamConnect",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
    }
}

@Composable
fun RegistrationScreen(
    setupState: SetupState,
    onNext: () -> Unit,
    onUpdateDetails: (String, String, String, String) -> Unit
) {
    var username by remember { mutableStateOf(setupState.username) }
    var email by remember { mutableStateOf(setupState.email) }
    var password by remember { mutableStateOf(setupState.password) }
    var confirmPassword by remember { mutableStateOf(setupState.confirmPassword) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        setupState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = "We will send a verification code to your email for successful registration.",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = {
                onUpdateDetails(username, email, password, confirmPassword)
                onNext()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Next")
        }
    }
}

@Composable
fun EmailVerificationScreen(
    setupState: SetupState,
    onVerify: () -> Unit,
    onUpdateCode: (String) -> Unit
) {
    var verificationCode by remember { mutableStateOf(setupState.verificationCode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Email Verification",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Please enter the 6-digit verification code sent to ${setupState.email}",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = verificationCode,
            onValueChange = { 
                if (it.length <= 6) {
                    verificationCode = it
                    onUpdateCode(it)
                }
            },
            label = { Text("Verification Code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        setupState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth(),
            enabled = verificationCode.length == 6
        ) {
            Text("Verify")
        }
    }
}

@Composable
fun CameraConnectionScreen(
    onConnectCamera: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Connect Your Camera",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "To connect your camera:",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "1. Make sure your camera is powered on\n" +
                   "2. Enable WiFi on your camera\n" +
                   "3. Connect your phone to the camera's WiFi network\n" +
                   "4. Click the button below to start the connection process",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = onConnectCamera,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Connect Camera")
        }
    }
}

@Composable
fun SetupCompleteScreen(
    onStartStreaming: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Setup Complete!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your camera is now connected and ready to stream.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartStreaming,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Streaming")
        }
    }
} 