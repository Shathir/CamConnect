package com.outdu.camconnect

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.outdu.camconnect.ui.theme.CamConnectTheme
import com.outdu.camconnect.viewmodels.SetupViewModel
import com.outdu.camconnect.ui.setupflow.*
import com.outdu.camconnect.auth.SessionManager
import com.outdu.camconnect.auth.SetupFlowDetector
import com.outdu.camconnect.security.MandatoryPermissionManager
import com.outdu.camconnect.security.MandatoryPermissionScreen
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge

class SetupActivity : ComponentActivity() {
    private val viewModel: SetupViewModel by viewModels()
    private lateinit var setupFlowDetector: SetupFlowDetector
    private val permissionManager = MandatoryPermissionManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize setup flow detection
        setupFlowDetector = SetupFlowDetector(this)
        
        // Check mandatory permissions first
        if (!permissionManager.hasAllMandatoryPermissions(this)) {
            Log.i("SetupActivity", "Mandatory permissions not granted - showing permission screen")
            showPermissionScreen()
            return
        }
        
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
    
    private fun showPermissionScreen() {
        enableEdgeToEdge()
        setContent {
            CamConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MandatoryPermissionScreen(
                        onPermissionsGranted = {
                            // Permissions granted, restart the activity to continue with setup
                            Log.i("SetupActivity", "Mandatory permissions granted - restarting activity")
                            recreate()
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