package com.outdu.camconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.outdu.camconnect.ui.theme.CamConnectTheme
import com.outdu.camconnect.viewmodels.SetupViewModel
import com.outdu.camconnect.viewmodels.SetupState
import com.outdu.camconnect.ui.setupflow.*

class SetupActivity : ComponentActivity() {
    private val viewModel: SetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CamConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val setupState by viewModel.setupState.collectAsState()
                    SetupFlow(
                        setupState = setupState,
                        onGetStarted = {
                            // Skip registration and go directly to camera setup
                            viewModel.updateNetworkConfig(true)
                            viewModel.updatePermissionsStatus(true)
                        },
                        onRegister = {
                            // Start registration process
                            viewModel.updateNetworkConfig(true)
                        },
                        onUpdateRegistrationDetails = viewModel::updateRegistrationDetails,
                        onUpdateVerificationCode = viewModel::updateVerificationCode,
                        onVerifyEmail = viewModel::verifyEmail,
                        onConnectCamera = {
                            // Here you would typically start the WiFi activity
                            // For now, we'll just simulate successful connection
                            viewModel.updateCameraConfig(true)
                        },
                        onStartStreaming = {
                            // Navigate to MainActivity and finish SetupActivity
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SetupFlow(
    setupState: SetupState,
    onGetStarted: () -> Unit,
    onRegister: () -> Unit,
    onUpdateRegistrationDetails: (String, String, String, String) -> Unit,
    onUpdateVerificationCode: (String) -> Unit,
    onVerifyEmail: () -> Unit,
    onConnectCamera: () -> Unit,
    onStartStreaming: () -> Unit
) {
    when {
        // Show landing screen if no registration started
        !setupState.isNetworkConfigured -> {
            LandingScreen(
                onGetStarted = onGetStarted,
                onRegister = onRegister
            )
        }
        // Show registration screen if network is configured but email not verified
        !setupState.isEmailVerified && setupState.username.isEmpty() -> {
            RegistrationScreen(
                setupState = setupState,
                onNext = { /* Move to verification screen */ },
                onUpdateDetails = onUpdateRegistrationDetails
            )
        }
        // Show email verification screen
        !setupState.isEmailVerified -> {
            EmailVerificationScreen(
                setupState = setupState,
                onVerify = onVerifyEmail,
                onUpdateCode = onUpdateVerificationCode
            )
        }
        // Show camera connection screen after email verification
        !setupState.isCameraConfigured -> {
            CameraConnectionScreen(
                onConnectCamera = onConnectCamera
            )
        }
        // Show setup complete screen
        else -> {
            SetupCompleteScreen(
                onStartStreaming = onStartStreaming
            )
        }
    }
} 