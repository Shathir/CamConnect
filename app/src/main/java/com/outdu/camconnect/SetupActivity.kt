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
import com.outdu.camconnect.auth.UserStateManager
import com.outdu.camconnect.auth.SetupFlowDetector
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
    private lateinit var setupFlowDetector: SetupFlowDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize setup flow detection
        setupFlowDetector = SetupFlowDetector(this)
        
        // Check if user should skip setup entirely
        if (setupFlowDetector.shouldSkipSetup()) {
            Log.i("SetupActivity", "Skipping setup - user already configured and authenticated")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
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
                    // Use the new Navigation-based setup flow
                    NavigationSetupFlow(
                        onNavigateToMain = {
                            // Navigate to MainActivity when setup is complete
                            startActivity(Intent(this@SetupActivity, MainActivity::class.java))
                            Toast.makeText(this@SetupActivity, "Setup completed successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Log current session status for debugging
        Log.d("SetupActivity", "Session status on resume: ${SessionManager.getSessionStatus()}")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("SetupActivity", "SetupActivity destroyed")
    }
} 