package com.outdu.camconnect

import android.content.Intent
import android.os.Build
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
import com.outdu.camconnect.auth.SessionManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import kotlinx.coroutines.delay
import java.lang.Thread.sleep

class SetupActivity : ComponentActivity() {
    private val viewModel: SetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Enable immersive mode to hide the navigation bar
            val decorView = window.decorView
            // Hide nav bar
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or  // Hide nav bar
                    View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        enableEdgeToEdge()
        setContent {
            CamConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val setupState by viewModel.setupState.collectAsState()
                    SetupFlow(
                        setupState = setupState,
                        onGetStarted = {
                            // Skip registration and go directly to login
                            viewModel.updateNetworkConfig(true)
                        },
                        onUpdateRegistrationDetails = viewModel::updateRegistrationDetails,
                        onUpdateVerificationCode = viewModel::updateVerificationCode,
                        onVerifyEmail = viewModel::verifyEmail,
                        onConnectCamera = {
                            viewModel.updateCameraConfig(true)
                        },
                        onAuthenticate = { isAuthenticated ->
                            if (isAuthenticated) {
                                // Navigate to MainActivity with authentication successful
                                startActivity(Intent(this@SetupActivity, MainActivity::class.java))
                                Toast.makeText(this@SetupActivity, "Authentication successful", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this@SetupActivity, "Invalid PIN", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onStartStreaming = {
                            if (SessionManager.isAuthenticated()) {
                                startActivity(Intent(this@SetupActivity, MainActivity::class.java))
                                Toast.makeText(this@SetupActivity, "Authentication successful", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Log.w("SetupActivity", "Attempted to start streaming without authentication")
                            }
                        }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Log current session status for debugging
//        if (::SessionManager.isInitialized) {
//            Log.d("SetupActivity", "Session status on resume: ${SessionManager.getSessionStatus()}")
//        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("SetupActivity", "SetupActivity destroyed")
    }
}

@Composable
fun SetupFlow(
    setupState: SetupState,
    onGetStarted: () -> Unit,
    onUpdateRegistrationDetails: (String, String, String, String) -> Unit,
    onUpdateVerificationCode: (String) -> Unit,
    onVerifyEmail: () -> Unit,
    onConnectCamera: () -> Unit,
    onAuthenticate: (Boolean) -> Unit,
    onStartStreaming: () -> Unit
) {
    when {
        // Show landing screen if no registration started
        !setupState.isNetworkConfigured -> {
            Box(modifier = Modifier.fillMaxSize()) {
                LandingScreen(
                    onGetStarted = onGetStarted
                )
            }
        }
        // Show login screen after landing
        !setupState.isEmailVerified -> {
            LoginScreen(
                setupState = setupState,
                onNext = { /* Move to verification screen */ },
                onUpdateDetails = onUpdateRegistrationDetails,
                onAuthenticate = onAuthenticate
            )
        }
        // Show camera connection screen after successful login
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