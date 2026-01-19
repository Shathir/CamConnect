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
import com.outdu.camconnect.ui.setupflow.QRScannerScreen
import com.outdu.camconnect.auth.SessionManager
import com.outdu.camconnect.services.OnvifDevice
import com.outdu.camconnect.utils.WifiConnectionManager
import com.outdu.camconnect.utils.WifiCredentials
import com.outdu.camconnect.utils.WifiConnectionResult
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.app.Activity

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
    
    private val saveWifiNetworkLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.i("ViewerFlow", "=== Save WiFi network result received ===")
        Log.d("ViewerFlow", "Result code: ${result.resultCode} (RESULT_OK=${Activity.RESULT_OK}, RESULT_CANCELED=${Activity.RESULT_CANCELED})")
        Log.d("ViewerFlow", "Result data: ${result.data}")
        
        // Clear credentials after processing
        val creds = currentWifiCredentials
        currentWifiCredentials = null
        
        if (result.resultCode == Activity.RESULT_OK) {
            // Network was saved successfully
            val resultList = result.data?.getIntegerArrayListExtra(Settings.EXTRA_WIFI_NETWORK_RESULT_LIST)
            Log.d("ViewerFlow", "Result list: $resultList")
            
            var discoveryScheduled = false
            
            if (resultList != null && resultList.isNotEmpty()) {
                resultList.forEachIndexed { index, code ->
                    Log.d("ViewerFlow", "Result[$index]: $code")
                    when (code) {
                        Settings.ADD_WIFI_RESULT_SUCCESS -> {
                            Log.i("ViewerFlow", "WiFi network saved successfully (ADD_WIFI_RESULT_SUCCESS)")
                            discoveryScheduled = true
                        }
                        Settings.ADD_WIFI_RESULT_ADD_OR_UPDATE_FAILED -> {
                            Log.w("ViewerFlow", "Failed to save WiFi network (ADD_WIFI_RESULT_ADD_OR_UPDATE_FAILED)")
                            viewModel.setWifiConnectionError("Failed to save network configuration")
                        }
                        Settings.ADD_WIFI_RESULT_ALREADY_EXISTS -> {
                            Log.i("ViewerFlow", "Network already exists (ADD_WIFI_RESULT_ALREADY_EXISTS)")
                            discoveryScheduled = true
                        }
                        else -> {
                            Log.w("ViewerFlow", "Unknown result code: $code - treating as success")
                            discoveryScheduled = true
                        }
                    }
                }
            } else {
                Log.w("ViewerFlow", "Result list is null or empty - assuming network was saved")
                discoveryScheduled = true
            }
            
            if (discoveryScheduled) {
                Log.i("ViewerFlow", "Scheduling discovery to start in 3 seconds...")
                // Clear connecting state and show success
                viewModel.setWifiConnecting(false)
                viewModel.setWifiConnectionSuccess(true)
                
                // Wait a moment for connection to establish, then start discovery
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    Log.i("ViewerFlow", "=== Starting discovery after network save ===")
                    viewModel.setWifiConnectionSuccess(false)
                    verifyAndStartDiscovery()
                }, 3000) // 3 second delay for connection to stabilize
            }
        } else {
            Log.w("ViewerFlow", "User cancelled saving WiFi network (resultCode: ${result.resultCode})")
            viewModel.setWifiConnecting(false)
            viewModel.setWifiConnectionError("Network save cancelled. Please connect manually via WiFi settings.")
        }
    }
    
    // Store pending QR data if permission was needed
    private var pendingQRData: String? = null
    
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("ViewerFlow", "Location permission granted")
            // If there's pending QR data, process it now
            pendingQRData?.let { qrData ->
                pendingQRData = null
                handleQRScanned(qrData)
            } ?: run {
                // Otherwise start discovery
                viewModel.startDiscovery()
            }
        } else {
            Log.w("ViewerFlow", "Location permission denied")
            pendingQRData = null
            if (viewModel.uiState.value.isConnectingToWifi) {
                viewModel.setWifiConnectionError("Location permission is required for WiFi connection")
            } else {
                // Try discovery without location permission
                viewModel.startDiscovery()
            }
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
                    val uiState by viewModel.uiState.collectAsState()
                    
                    if (uiState.showQRScanner) {
                        QRScannerScreen(
                            onQRScanned = { qrData -> handleQRScanned(qrData) },
                            onBack = { viewModel.dismissQRScanner() }
                        )
                    } else {
                        ViewerFlowScreen(
                            viewModel = viewModel,
                            onStartStreaming = { handleStartStreaming() },
                            onCameraSelected = { camera -> handleCameraSelected(camera) },
                            onGoToWifiSettings = { handleGoToWifiSettings() },
                            onAuthenticationSuccess = { camera -> handleAuthenticationSuccess(camera) },
                            onBack = { finish() },
                            onScanQRCode = { viewModel.showQRScanner() }
                        )
                    }
                }
            }
        }
        
        Log.i("ViewerFlow", "ViewerFlowActivity created")
    }
    
    // WiFi Connection Manager
    private var wifiConnectionManager: WifiConnectionManager? = null
    private var isConnectingToWifi = false // Prevent duplicate connection attempts
    private var currentWifiCredentials: WifiCredentials? = null // Store credentials for save network
    
    /**
     * Handle Start Streaming button click
     */
    private fun handleStartStreaming() {
        Log.i("ViewerFlow", "Discover Devices clicked - beginning ONVIF discovery")
        
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
     * Handle QR code scanned
     */
    private fun handleQRScanned(qrData: String) {
        Log.i("ViewerFlow", "QR code scanned: $qrData")
        
        // Prevent duplicate connection attempts
        if (isConnectingToWifi) {
            Log.w("ViewerFlow", "Already connecting to WiFi, ignoring duplicate QR scan")
            return
        }
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            viewModel.setWifiConnectionError("WiFi connection requires Android 10+")
            viewModel.dismissQRScanner()
            return
        }
        
        // Check location permission first (required for WiFi operations)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.d("ViewerFlow", "Location permission required for WiFi connection")
            viewModel.dismissQRScanner()
            // Request location permission, then retry connection
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            // Store QR data to retry after permission grant
            pendingQRData = qrData
            return
        }
        
        // Parse QR data
        wifiConnectionManager = WifiConnectionManager(this)
        val credentials = wifiConnectionManager?.parseQRData(qrData)
        
        if (credentials == null) {
            Log.e("ViewerFlow", "Failed to parse QR data")
            viewModel.setWifiConnectionError("Invalid QR code format")
            viewModel.dismissQRScanner()
            return
        }
        
        Log.i("ViewerFlow", "Parsed WiFi credentials - SSID: ${credentials.ssid}")
        
        // Store credentials for later use (save network)
        currentWifiCredentials = credentials
        
        // Mark as connecting to prevent duplicates
        isConnectingToWifi = true
        
        // Dismiss QR scanner and show connecting state
        viewModel.dismissQRScanner()
        viewModel.setWifiConnecting(true)
        viewModel.clearWifiConnectionError()
        
        // Connect to WiFi network
        Log.i("ViewerFlow", "Calling connectToNetwork with credentials: SSID=${credentials.ssid}")
        wifiConnectionManager?.connectToNetwork(credentials) { result ->
            Log.i("ViewerFlow", "Received WiFi connection result: ${result::class.simpleName}")
            Log.d("ViewerFlow", "Result details: $result")
            when (result) {
                is WifiConnectionResult.Success -> {
                    Log.i("ViewerFlow", "WiFi network request approved - Network: ${result.network}")
                    isConnectingToWifi = false
                    
                    // Get stored credentials
                    val creds = currentWifiCredentials ?: credentials
                    
                    // WifiNetworkSpecifier creates ephemeral networks that don't become the active connection
                    // We ALWAYS need to save the network using ACTION_WIFI_ADD_NETWORKS to actually connect
                    Log.i("ViewerFlow", "Ephemeral connection approved. Now saving network to make it the active connection.")
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Log.i("ViewerFlow", "Android 11+ detected (SDK ${Build.VERSION.SDK_INT}), creating save network intent for SSID: ${creds.ssid}")
                        val saveIntent = wifiConnectionManager?.createSaveNetworkIntent(creds)
                        if (saveIntent != null) {
                            Log.i("ViewerFlow", "Save network intent created successfully")
                            Log.d("ViewerFlow", "Intent action: ${saveIntent.action}")
                            Log.d("ViewerFlow", "Intent has extras: ${saveIntent.extras != null}")
                            
                            viewModel.setWifiConnecting(false) // Stop showing connecting state
                            
                            // Launch the save network dialog
                            try {
                                Log.i("ViewerFlow", "Launching save network dialog...")
                                Log.d("ViewerFlow", "Intent details before launch:")
                                Log.d("ViewerFlow", "  Action: ${saveIntent.action}")
                                Log.d("ViewerFlow", "  Component: ${saveIntent.component}")
                                Log.d("ViewerFlow", "  Package: ${saveIntent.`package`}")
                                Log.d("ViewerFlow", "  Has extras: ${saveIntent.extras != null}")
                                
                                // Verify intent can be resolved before launching
                                val resolveInfo = packageManager.resolveActivity(saveIntent, 0)
                                if (resolveInfo == null) {
                                    Log.e("ViewerFlow", "Intent cannot be resolved! Opening WiFi settings instead.")
                                    viewModel.setWifiConnectionError("Save network dialog not available. Opening WiFi settings - please connect to '${creds.ssid}' manually.")
                                    val wifiSettingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                                    startActivity(wifiSettingsIntent)
                                    currentWifiCredentials = null
                                } else {
                                    Log.d("ViewerFlow", "Intent resolved by: ${resolveInfo.activityInfo.packageName}/${resolveInfo.activityInfo.name}")
                                    saveWifiNetworkLauncher.launch(saveIntent)
                                    Log.i("ViewerFlow", "Save network launcher launched - system dialog should appear now")
                                }
                            } catch (e: Exception) {
                                Log.e("ViewerFlow", "Exception launching save network dialog", e)
                                e.printStackTrace()
                                viewModel.setWifiConnectionError("Failed to show save network dialog: ${e.message}. Opening WiFi settings - please connect to '${creds.ssid}' manually.")
                                try {
                                    val wifiSettingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                                    startActivity(wifiSettingsIntent)
                                } catch (e2: Exception) {
                                    Log.e("ViewerFlow", "Failed to open WiFi settings", e2)
                                }
                                currentWifiCredentials = null
                            }
                        } else {
                            Log.e("ViewerFlow", "Failed to create save network intent - intent is null")
                            viewModel.setWifiConnectionError("Network approved but failed to create save dialog. Please connect to '${creds.ssid}' manually via WiFi settings.")
                            currentWifiCredentials = null
                        }
                    } else {
                        Log.w("ViewerFlow", "Android version ${Build.VERSION.SDK_INT} - Android 11+ (API 30) required for save network feature")
                        viewModel.setWifiConnectionError("Network approved but Android 11+ required to save. Please connect to '${creds.ssid}' manually via WiFi settings.")
                        currentWifiCredentials = null
                    }
                }
                is WifiConnectionResult.Failed -> {
                    Log.e("ViewerFlow", "WiFi connection failed: ${result.error}")
                    isConnectingToWifi = false
                    viewModel.setWifiConnecting(false)
                    viewModel.setWifiConnectionError(result.error)
                }
                is WifiConnectionResult.Timeout -> {
                    Log.e("ViewerFlow", "WiFi connection timeout")
                    isConnectingToWifi = false
                    viewModel.setWifiConnecting(false)
                    viewModel.setWifiConnectionError("Connection timeout. The network may not be available or the connection was denied. Please check:\n1. WiFi is enabled\n2. The network is in range\n3. The password is correct")
                }
                is WifiConnectionResult.Connecting -> {
                    // Still connecting, do nothing
                }
            }
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
     * Verify WiFi connection and start discovery
     */
    private fun verifyAndStartDiscovery() {
        Log.i("ViewerFlow", "=== verifyAndStartDiscovery() called ===")
        
        // Clear any success state
        viewModel.setWifiConnectionSuccess(false)
        
        // Check location permission before discovery
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            this, 
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        Log.d("ViewerFlow", "Location permission granted: $hasLocationPermission")
        
        if (!hasLocationPermission) {
            Log.d("ViewerFlow", "Requesting location permission for discovery")
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            Log.i("ViewerFlow", "Location permission already granted, calling viewModel.startDiscovery()")
            try {
                viewModel.startDiscovery()
                Log.i("ViewerFlow", "viewModel.startDiscovery() called successfully")
            } catch (e: Exception) {
                Log.e("ViewerFlow", "Error calling startDiscovery()", e)
                e.printStackTrace()
            }
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
        // Cleanup WiFi connection manager
        wifiConnectionManager?.cleanup()
        wifiConnectionManager = null
        Log.d("ViewerFlow", "ViewerFlowActivity destroyed")
    }
}
