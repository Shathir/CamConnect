package com.outdu.camconnect

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.outdu.camconnect.ui.layouts.AdaptiveStreamLayout
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.media.MediaCodecList
import android.media.MediaFormat
import android.util.Log
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import com.outdu.camconnect.Viewmodels.AppViewModel
import com.outdu.camconnect.Viewmodels.RecorderViewModel
import com.outdu.camconnect.singleton.MainActivitySingleton
import com.outdu.camconnect.utils.MemoryManager
import org.freedesktop.gstreamer.GStreamer
import java.util.Locale
import com.outdu.camconnect.ui.theme.*
import android.content.res.Configuration
import androidx.compose.runtime.mutableStateOf
import com.outdu.camconnect.ui.viewmodels.RecordingViewModel
import android.app.Activity
import androidx.annotation.RequiresApi
import com.outdu.camconnect.communication.CameraConfigurationManager
import com.outdu.camconnect.utils.ConfigurationMigrationHelper
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import com.outdu.camconnect.security.MandatoryPermissionManager
import com.outdu.camconnect.profiler.selectBestDecoder
import kotlin.system.exitProcess
import com.outdu.camconnect.auth.SessionManager
import com.outdu.camconnect.communication.CameraWebSocketManager
import com.outdu.camconnect.utils.NetworkConfigManager
import com.outdu.camconnect.streaming.StreamLifecycleManager
import android.os.SystemClock

data class OverlayPoints(
    var labels: IntArray,
    var probs: FloatArray,
    var pointXs: IntArray,
    var pointYs: IntArray,
    var pointWs: IntArray,
    var pointHs: IntArray,
    var depThres: FloatArray
)

class MainActivity : ComponentActivity() {
    private val permissionManager = MandatoryPermissionManager.getInstance()

    @JvmField
    var nativeCustomData: Long = 0 // Native code will use this to keep private data
    external fun nativePlay(
        width: Int,
        height: Int,
        od: Boolean = false,
        ds: Boolean = false,
        far_roi: Boolean = false
    )
    external fun nativeInit(avcDecoder: String) // Initialize native code, build pipeline, etc.
    external fun nativePause() // Set pipeline to PAUSED
    external fun nativeFinalize()
    external fun nativeSetCameraIp(cameraIp: String) // Set RTSP URL for streaming
    external fun nativeSurfaceInit(surface: Any) // A new surface is available
    external fun nativeSurfaceFinalize() // Surface about to be destroyed
    external fun nativeLoadOdModel(
        mgr: AssetManager,
        modelId: Int,
        cpuGpu: Int,
        midas: Boolean,
        model: Int
    ): Boolean
    companion object {
        const val REQUEST_CODE_SCREEN_CAPTURE = 1001
        const val EXTRA_CAMERA_WS_PORT = "CAMERA_WS_PORT"
        const val EXTRA_CAMERA_WS_PATH = "CAMERA_WS_PATH"

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        @JvmStatic
        private external fun nativeClassInit(currentTimeMillis: Long): Boolean

        // Track if model has been loaded in this process lifetime
        // This prevents OpenMP re-initialization crashes when logging out/in
        @Volatile
        private var isModelLoaded = false
        
        private val modelLoadLock = Any()

        init {
            // CRITICAL: Set OpenMP environment variables BEFORE loading native library
            // This prevents OpenMP from trying to set CPU affinity on Android which causes crashes
            try {
                android.system.Os.setenv("KMP_AFFINITY", "disabled", true)
                android.system.Os.setenv("OMP_PROC_BIND", "false", true)
                android.system.Os.setenv("OMP_NUM_THREADS", "2", true)
                android.system.Os.setenv("OMP_WAIT_POLICY", "PASSIVE", true)
                android.util.Log.i("MainActivity", "OpenMP environment variables set from Java")
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "Failed to set OpenMP env vars", e)
            }
            
            System.loadLibrary("gstreamer_android_player")
            nativeClassInit(System.currentTimeMillis())
        }
    }

    private fun loadODModel(modelId: Int) {
        val retInit = nativeLoadOdModel(assets, 0,1, CameraConfigurationManager.isDepthSensingEnabled(), 1)
        if (!retInit) {
            Log.e("MainActivity", "yolov8ncnn loadModel failed")
            runOnUiThread {
                Toast.makeText(this@MainActivity, "yolov8ncnn loadModel failed", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Load OD model asynchronously on a background thread to avoid blocking the main thread.
     * Uses a static flag to prevent reloading the model if it's already loaded in this process,
     * which prevents OpenMP re-initialization crashes on logout/login.
     * 
     * @param modelId The model version to load
     * @param forceReload If true, will attempt to reload even if already loaded (may crash due to OpenMP)
     */
    private fun loadODModelAsync(modelId: Int, forceReload: Boolean = false) {
        // Check if model is already loaded (prevents OpenMP crash on re-login)
        synchronized(modelLoadLock) {
            if (isModelLoaded && !forceReload) {
                Log.i("MainActivity", "OD model already loaded in this process - skipping reload")
                return
            }
            
            if (isModelLoaded && forceReload) {
                Log.w("MainActivity", "Force reloading OD model - this may cause OpenMP crash!")
            }
        }
        
        Thread {
            try {
                val t0 = SystemClock.elapsedRealtime()
                Log.i("MainActivity", "OD model load start on background thread (modelVersion=$modelId, forceReload=$forceReload)")
                
                // Double-check inside thread with lock
                synchronized(modelLoadLock) {
                    if (isModelLoaded && !forceReload) {
                        Log.i("MainActivity", "OD model already loaded (checked in thread) - skipping reload")
                        return@Thread
                    }
                    
                    // Load model on background thread
                    val retInit = nativeLoadOdModel(assets, 0, 1, CameraConfigurationManager.isDepthSensingEnabled(), 1)
                    
                    val loadTime = SystemClock.elapsedRealtime() - t0
                    Log.i("MainActivity", "OD model load end (took ${loadTime}ms)")
                    
                    if (!retInit) {
                        Log.e("MainActivity", "yolov8ncnn loadModel failed")
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "yolov8ncnn loadModel failed", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        isModelLoaded = true // Mark as loaded
                        Log.i("MainActivity", "OD model loaded successfully - marked as loaded")
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading OD model on background thread", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error loading model: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }.start()
    }

    private fun startCameraWebSocketIfConfigured() {
        val cameraIp = intent.getStringExtra("CAMERA_IP") ?: SessionManager.getLastConnectedCameraIp()
        if (cameraIp.isNullOrBlank()) {
            Log.w("MainActivity", "No camera IP available; skipping CameraWebSocketManager start")
            return
        }

        val wsPort = intent.getIntExtra(EXTRA_CAMERA_WS_PORT, NetworkConfigManager.getCameraWsPort())
        val wsPath = intent.getStringExtra(EXTRA_CAMERA_WS_PATH) ?: NetworkConfigManager.getCameraWsPath()
        CameraWebSocketManager.setTarget(cameraIp = cameraIp, port = wsPort, path = wsPath)
        Log.i("MainActivity", "Camera WebSocket target set: ws://$cameraIp:$wsPort$wsPath")
    }



    fun onGStreamerInitialized() {
    }

    private var odPointsState = mutableStateOf(
        OverlayPoints(
            labels = intArrayOf(),
            probs = floatArrayOf(),
            pointXs = intArrayOf(),
            pointYs = intArrayOf(),
            pointWs = intArrayOf(),
            pointHs = intArrayOf(),
            depThres = floatArrayOf()
        )
    )

    fun odCallback(
        labels: IntArray,
        probs: FloatArray,
        pointXs: IntArray,
        pointYs: IntArray,
        pointWs: IntArray,
        pointHs: IntArray,
        depThres: FloatArray
    ) {
        odPointsState.value = OverlayPoints(
            labels = labels,
            probs = probs,
            pointXs = pointXs,
            pointYs = pointYs,
            pointWs = pointWs,
            pointHs = pointHs,
            depThres = depThres
        )

        Log.i("onCallback","onCallback is called")
        Log.i("POint CallBack : ", odPointsState.value.labels.size.toString())

    }

    fun onStreamError(status: Int){
        Log.i("onStreamError","onStreamError is called with $status value")
    }

    fun setMessage(message: String) {
        runOnUiThread {
            // Update UI with the message
            Log.i("setMessage", "Message received: $message")
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val deniedPermissions = permissions.filterValues { !it }.keys

        if (deniedPermissions.isNotEmpty()) {
            // Show explanation dialog for denied permissions
            showPermissionExplanationDialog(deniedPermissions)
        }
    }

    private fun showPermissionExplanationDialog(deniedPermissions: Set<String>) {
        val hasLocationPermissions = deniedPermissions.any { 
            it == Manifest.permission.ACCESS_FINE_LOCATION || 
            it == Manifest.permission.ACCESS_COARSE_LOCATION 
        }
        
        if (hasLocationPermissions) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Location Permission Required")
                .setMessage("Location permission is needed to display WiFi signal strength information. Without this permission, the WiFi indicator will still show connection status but not signal strength.\n\nYou can grant this permission later in the app settings.")
                .setPositiveButton("Open Settings") { _, _ ->
                    // Open app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                .setNegativeButton("Continue Without") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
        }
    }

    private fun showUnsupportedDeviceDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Device Not Supported")
            .setMessage("Your device does not support HEVC (H.265) video decoding, which is required for this application. The application will now exit.")
            .setPositiveButton("OK") { _, _ ->
                exitApp()
            }
            .setCancelable(false)
            .show()
    }

    private fun exitApp() {
        try {
            // 1. Stop native pipeline first (CRITICAL)
            try {
                nativePause()
                nativeSurfaceFinalize()
                nativeFinalize()
            } catch (_: Exception) {}

            // 2. Clear static/singleton references
            MainActivitySingleton.clearMainActivity()

            // 3. Remove task from recent apps
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask()
            } else {
                finishAffinity()
            }

            // 4. Kill process FORCEFULLY
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)

        } catch (e: Exception) {
            Log.e("EXIT", "Exit error", e)
            // emergency exit fallback
            Runtime.getRuntime().exit(0)
        }
    }

    // Public function to request location permissions from UI
    fun requestLocationPermissions() {
        checkAndRequestPermissions()
    }

    fun listDownloadsLegacy() {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        if (downloadsDir.exists() && downloadsDir.isDirectory) {
            val files = downloadsDir.listFiles()
            files?.forEach { file ->
                Log.i("Downloads", "File: ${file.name} | Path: ${file.absolutePath}")
            }
        } else {
            Log.w("Downloads", "Downloads directory does not exist.")
        }
    }




    private var actualCodecName: String = ""
    private val viewModel: RecorderViewModel by viewModels()
    private val recordingViewModel: RecordingViewModel by viewModels()
    private val appViewModel: AppViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {

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

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Select best available HEVC (h265) decoder with priority: 4K HEVC > 2K HEVC
        val decoderResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selectBestDecoder()
        } else {
            // For Android < Q, try to find HEVC decoder using old method
            val mediaCodecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            // Try 4K first
            val codecName4K = mediaCodecList.findDecoderForFormat(
                MediaFormat.createVideoFormat(
                    "video/hevc",
                    3840,
                    2160
                )
            )
            if (codecName4K != null) {
                codecName4K to "4K"
            } else {
                // Try 2K
                val codecName2K = mediaCodecList.findDecoderForFormat(
                    MediaFormat.createVideoFormat(
                        "video/hevc",
                        1920,
                        1080
                    )
                )
                if (codecName2K != null) {
                    codecName2K to "2K"
                } else {
                    null
                }
            }
        }
        
        super.onCreate(savedInstanceState)
        
        if (decoderResult != null) {
            val (codecName, resolution) = decoderResult
            actualCodecName = codecName.replace(".", "").lowercase(Locale.getDefault())
            Log.i("CODECLISTS", "Selected decoder: $codecName (resolution: $resolution)")
            Log.i("CODECLISTS", "Normalized codec name: $actualCodecName")
        } else {
            // No HEVC decoder available - show error dialog and exit
            Log.e("CODECLISTS", "No HEVC decoder found - device not supported")
            showUnsupportedDeviceDialog()
            return
        }
        enableEdgeToEdge()

        listDownloadsLegacy()


        // Handle viewer flow parameters
        handleViewerFlowParameters()

        // Check mandatory permissions first
        if (!permissionManager.hasAllMandatoryPermissions(this)) {
            Log.w("MainActivity", "Mandatory permissions not granted - redirecting to SetupActivity")
            val intent = Intent(this, SetupActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }

        // Start camera WebSocket (foreground-only, survives UI recompositions/navigation)
        startCameraWebSocketIfConfigured()

        try {
            GStreamer.init(this)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Check additional permissions (location for WiFi signal strength)
        checkAndRequestPermissions()

        val tInit0 = SystemClock.elapsedRealtime()
        nativeInit(actualCodecName)
        Log.i("MainActivity", "nativeInit() returned (took ${SystemClock.elapsedRealtime() - tInit0}ms)")
        
        // Load model on MAIN THREAD BEFORE showing UI - prevents OpenMP crashes and ANR
        // This runs during app startup (splash screen still visible), so no ANR
        val modelVersion = try {
            // Try to migrate old config and load current configuration
            runBlocking {
                ConfigurationMigrationHelper.migrateIfNeeded(this@MainActivity)
                val config = CameraConfigurationManager.loadConfigurationAsync(this@MainActivity).getOrNull()
                config?.modelVersion ?: CameraConfigurationManager.getModelVersion()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to load configuration, using default", e)
            CameraConfigurationManager.getModelVersion()
        }
        
        // Load model synchronously on main thread BEFORE UI
        loadODModel(modelVersion)
        Log.i("MainActivity", "Model loaded on main thread - ready for inference")

        setContent {
            CamConnectTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VeryDarkBackground)
                        .padding(
                            start = 24.dp,
                            top = 8.dp,
                            end = 8.dp,
                            bottom = 8.dp
                        )
                ) {
                    AdaptiveStreamLayout(
                        context = LocalContext.current,
                        pointState = odPointsState,
                        onLogout = {
                            // Handle logout - navigate back to SetupActivity
                            handleLogout()
                        }
                    )
                }
            }
        }
        
        MainActivitySingleton.setMainActivity(this)
        StreamLifecycleManager.bind(appViewModel)
    }

    /**
     * Handle viewer flow parameters passed from ViewerFlowActivity
     */
    private fun handleViewerFlowParameters() {
        val cameraIp = intent.getStringExtra("CAMERA_IP")
        val cameraEndpoints = intent.getStringArrayExtra("CAMERA_ENDPOINTS")
        val cameraType = intent.getStringExtra("CAMERA_TYPE")
        val cameraRtspUrl = intent.getStringExtra("CAMERA_RTSP_URL")
        val userType = intent.getStringExtra("USER_TYPE")
        
        if (cameraIp != null && userType == "VIEWER") {
            Log.i("MainActivity", "Viewer flow detected - Camera IP: $cameraIp")
            Log.i("MainActivity", "Camera endpoints: ${cameraEndpoints?.joinToString(", ")}")
            Log.i("MainActivity", "Camera type: $cameraType")
            
            // Configure communication layer for the specific camera
            try {
                com.outdu.camconnect.communication.MotocamAPIHelperWrapper.setDeviceIpAddress(cameraIp)
                Log.i("MainActivity", "Communication layer configured for camera: $cameraIp")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error configuring communication layer", e)
            }
            
            // Set RTSP URL for GStreamer pipeline
            try {
                val CameraIp = cameraIp ?: "192.168.2.1"
                nativeSetCameraIp(CameraIp)
                Log.i("MainActivity", "Camera IP set to: $CameraIp")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error setting RTSP URL", e)
            }
            
            // Show a toast to indicate viewer mode
            runOnUiThread {
                Toast.makeText(this, "Connected to camera: $cameraIp", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Handle logout - clear session, cleanup resources, and navigate to SetupActivity
     */
    private fun handleLogout() {
        Log.i("MainActivity", "Handling logout...")
        
        lifecycleScope.launch {
            try {
                // Clear local session + last connected camera to prevent auto-reconnect
                try {
                    SessionManager.clearSession()
                    SessionManager.clearLastConnectedCamera()
                } catch (e: Exception) {
                    Log.w("MainActivity", "Failed to clear session during logout", e)
                }

                // Stop streaming and cleanup native resources
                try {
                    nativePause()
                    nativeSurfaceFinalize()
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error cleaning up native resources", e)
                }
                
                // Clear MainActivitySingleton reference
                MainActivitySingleton.clearMainActivity()
                
                // Navigate to SetupActivity (login screen)
                val intent = Intent(this@MainActivity, SetupActivity::class.java).apply {
                    // Clear the task stack so user can't go back to MainActivity
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                
                // Finish this activity
                finish()
                
                Log.i("MainActivity", "Logout completed - navigated to SetupActivity")
                
            } catch (e: Exception) {
                Log.e("MainActivity", "Error during logout", e)
                // Still try to navigate even if cleanup fails
                val intent = Intent(this@MainActivity, SetupActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                recordingViewModel.handleActivityResult(this, resultCode, data)
            } else {
                Toast.makeText(this, "Screen recording permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause called")
        // Pause native streaming when app goes to background
        try {
            MemoryManager.cleanupWeakReferences()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error during pause", e)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume called")
        // Resume will be handled by surface callbacks when they become available
        Log.d("MainActivity", "Memory stats: ${MemoryManager.getMemoryStats()}")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        try {
            // Ensure clean pipeline shutdown before configuration change
            MemoryManager.cleanupWeakReferences()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error during configuration change", e)
        }
    }

    override fun onDestroy() {
        StreamLifecycleManager.unbind(appViewModel)
        super.onDestroy()
        // Force cleanup of all native resources
//        MemoryManager.forceCleanup()
        // Clear the singleton reference to prevent memory leaks
        MainActivitySingleton.clearMainActivity()
        // Cleanup native resources
        try {
            nativeFinalize()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error during native cleanup", e)
        }
    }

    private fun checkAndRequestPermissions() {
        // Check if we need to request permissions (only for Android 6.0+)
        val permissionsToRequest = mutableListOf<String>()

        // Check each permission
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        // Request permissions if needed
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun CamConnectPreview() {
    CamConnectTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VeryDarkBackground)
        ) {
            // Preview content
        }
    }
}