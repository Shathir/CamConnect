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
import com.outdu.camconnect.Viewmodels.RecorderViewModel
import com.outdu.camconnect.singleton.MainActivitySingleton
import com.outdu.camconnect.utils.MemoryManager
import org.freedesktop.gstreamer.GStreamer
import java.util.Locale
import com.outdu.camconnect.ui.theme.*
import android.content.res.Configuration
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.outdu.camconnect.services.RecordConfig
import com.outdu.camconnect.services.ScreenRecorderService
import com.outdu.camconnect.services.ScreenRecorderService.Companion.ACTION_START
import com.outdu.camconnect.services.ScreenRecorderService.Companion.RECORD_CONFIG
import com.outdu.camconnect.Viewmodels.AppViewModel
import com.outdu.camconnect.ui.viewmodels.RecordingViewModel
import android.app.Activity


class MainActivity : ComponentActivity() {




    var nativeCustomData: Long = 0 // Native code will use this to keep private data
    external fun nativePlay(
        width: Int,
        height: Int
    )
    external fun nativeInit(avcDecoder: String) // Initialize native code, build pipeline, etc.
    external fun nativePause() // Set pipeline to PAUSED
    external fun nativeFinalize()
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

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        @JvmStatic
        private external fun nativeClassInit(currentTimeMillis: Long): Boolean

        init {
            System.loadLibrary("gstreamer_android_player")
            nativeClassInit(System.currentTimeMillis())
        }
    }
    fun onGStreamerInitialized() {
    }

    fun odCallback(
        labels: IntArray,
        probs: FloatArray,
        pointXs: IntArray,
        pointYs: IntArray,
        pointWs: IntArray,
        pointHs: IntArray,
        depThres: FloatArray
    ) {
        Log.i("onCallback","onCallback is called")

    }
    fun setMessage(message: String) {
        runOnUiThread {
            // Update UI with the message
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val deniedPermissions = permissions.filterValues { !it }.keys
        
        if (deniedPermissions.isNotEmpty()) {
            // Some permissions were denied
            Toast.makeText(
                this,
                "Location permissions are required for detailed WiFi information",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    var actualCodecName: String = ""
    private val viewModel: RecorderViewModel by viewModels()
    private val recordingViewModel: RecordingViewModel by viewModels()

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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        val mediaCodecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        val codecName = mediaCodecList.findDecoderForFormat(
            MediaFormat.createVideoFormat(
                "video/avc",
                1920,
                1080
            )
        )
        actualCodecName = codecName.replace(".", "").lowercase(Locale.getDefault())

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            GStreamer.init(this)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            finish()
            return
        }


        // Check permissions
        checkAndRequestPermissions()

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
//                    ScreenRecorderUI(LocalContext.current, viewModel)
                    AdaptiveStreamLayout(context = LocalContext.current)
                }
            }
        }

        nativeInit(actualCodecName)
        MainActivitySingleton.setMainActivity(this)
    }

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
        // Pause native streaming when app goes to background
        try {
            MemoryManager.cleanupWeakReferences()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error during pause", e)
        }
    }
    
    override fun onResume() {
        super.onResume()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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