package com.outdu.camconnect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.outdu.camconnect.ui.theme.CamConnectTheme
import com.outdu.camconnect.viewmodels.ViewerFlowViewModel
import com.outdu.camconnect.ui.viewer.*
import com.outdu.camconnect.auth.SessionManager
import com.outdu.camconnect.services.OnvifDevice

/**
 * Activity that handles the Viewer Flow for camera streaming application
 * 
 * Flow:
 * 1. Start Streaming button clicked
 * 2. Run ONVIF discovery service
 * 3. If cameras discovered: Show camera cards with IP, MAC, Name
 * 4. If no cameras: Prompt to connect to WiFi and retry
 * 5. User selects camera and enters PIN for authentication
 * 6. Navigate to MainActivity for stream consumption
 */
class ViewerFlowActivity : ComponentActivity() {
    
    private val viewModel: ViewerFlowViewModel by viewModels()
    
    private val wifiSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // User returned from WiFi settings, retry discovery
        Log.d("ViewerFlow", "User returned from WiFi settings, retrying discovery")
        viewModel.retryDiscovery()
    }
    
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("ViewerFlow", "Location permission granted, starting discovery")
            viewModel.startDiscovery()
        } else {
            Log.w("ViewerFlow", "Location permission denied, starting discovery without location")
            viewModel.startDiscovery()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SessionManager
        SessionManager.initialize(this)
        
        // Set up immersive mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val decorView = window.decorView
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
        }
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        
        setContent {
            CamConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ViewerFlowScreen(
                        viewModel = viewModel,
                        onStartStreaming = { handleStartStreaming() },
                        onCameraSelected = { camera -> handleCameraSelected(camera) },
                        onGoToWifiSettings = { handleGoToWifiSettings() },
                        onAuthenticationSuccess = { camera -> handleAuthenticationSuccess(camera) },
                        onBack = { finish() }
                    )
                }
            }
        }
        
        Log.i("ViewerFlow", "ViewerFlowActivity created")
    }
    
    /**
     * Handle Start Streaming button click
     */
    private fun handleStartStreaming() {
        Log.i("ViewerFlow", "Start Streaming clicked - beginning ONVIF discovery")
        
        // Check location permission for WiFi scanning (optional but recommended)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.d("ViewerFlow", "Requesting location permission for WiFi scanning")
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.startDiscovery()
        }
    }
    
    /**
     * Handle camera selection from the list
     */
    private fun handleCameraSelected(camera: OnvifDevice) {
        Log.i("ViewerFlow", "Camera selected: ${camera.ipAddress}")
        viewModel.selectCamera(camera)
    }
    
    /**
     * Handle going to WiFi settings when no cameras are found
     */
    private fun handleGoToWifiSettings() {
        Log.i("ViewerFlow", "Opening WiFi settings")
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            wifiSettingsLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("ViewerFlow", "Error opening WiFi settings", e)
            Toast.makeText(this, "Unable to open WiFi settings", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Handle successful PIN authentication
     */
    private fun handleAuthenticationSuccess(camera: OnvifDevice) {
        Log.i("ViewerFlow", "Authentication successful for camera: ${camera.ipAddress}")
        
        // Generate RTSP URL for the camera
        val rtspUrl = generateRtspUrl(camera)
        
        // Navigate to MainActivity for stream consumption
        val intent = Intent(this, MainActivity::class.java).apply {
            // Pass camera information to MainActivity
            putExtra("CAMERA_IP", camera.ipAddress)
            putExtra("CAMERA_ENDPOINTS", camera.endpointUrls.toTypedArray())
            putExtra("CAMERA_TYPE", camera.deviceType)
            putExtra("CAMERA_RTSP_URL", rtspUrl)
            putExtra("USER_TYPE", "VIEWER")
            
            // Clear the task stack so user can't go back
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        startActivity(intent)
        finish()
        
        Log.i("ViewerFlow", "Navigated to MainActivity for stream consumption")
    }
    
    /**
     * Generate RTSP URL for the camera based on its information
     */
    private fun generateRtspUrl(camera: OnvifDevice): String {
        // Try to extract stream URL from ONVIF endpoints
        val streamUrl = extractStreamUrl(camera)
        if (streamUrl != null) {
            Log.i("ViewerFlow", "Using extracted stream URL: $streamUrl")
            return streamUrl
        }
        
        // Fallback to standard RTSP URL format
        val defaultUrl = "rtsp://onvif:test@${camera.ipAddress}/live1.sdp"
        Log.i("ViewerFlow", "Using default RTSP URL: $defaultUrl")
        return defaultUrl
    }
    
    /**
     * Try to extract stream URL from ONVIF device endpoints
     */
    private fun extractStreamUrl(camera: OnvifDevice): String? {
        // Look for streaming endpoints in the ONVIF device information
        camera.endpointUrls.forEach { endpoint ->
            if (endpoint.contains("stream", ignoreCase = true) || 
                endpoint.contains("media", ignoreCase = true)) {
                // Convert HTTP ONVIF endpoint to RTSP stream URL
                try {
                    val uri = java.net.URI(endpoint)
                    val host = uri.host ?: camera.ipAddress
                    val port = if (uri.port > 0) uri.port else 554 // Default RTSP port
                    
                    // Try common RTSP stream paths
                    val commonPaths = listOf(
                        "/live1.sdp",
                        "/stream1",
                        "/cam/realmonitor?channel=1&subtype=0",
                        "/h264Preview_01_main",
                        "/video1"
                    )
                    
                    // Return first common path (can be enhanced with actual ONVIF media profile discovery)
                    return "rtsp://onvif:test@$host:$port${commonPaths[0]}"
                } catch (e: Exception) {
                    Log.w("ViewerFlow", "Error parsing endpoint: $endpoint", e)
                }
            }
        }
        return null
    }
    
    override fun onResume() {
        super.onResume()
        Log.d("ViewerFlow", "ViewerFlowActivity resumed")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("ViewerFlow", "ViewerFlowActivity destroyed")
    }
}
