package com.outdu.camconnect

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.outdu.camconnect.auth.SessionManager
import com.outdu.camconnect.communication.CameraConfigurationManager
import com.outdu.camconnect.communication.CameraWebSocketManager
import com.outdu.camconnect.streaming.StreamLifecycleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CamConnectApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize SessionManager globally
        SessionManager.initialize(this)
        Log.i("CamConnectApplication", "SessionManager initialized globally")
        
        // Load camera configuration
        applicationScope.launch {
            val result = CameraConfigurationManager.loadConfigurationAsync(this@CamConnectApplication)
            result.onSuccess { config ->
                Log.i("CamConnectApplication", "Camera configuration loaded successfully: $config")
            }.onFailure { error ->
                Log.e("CamConnectApplication", "Failed to load camera configuration", error)
            }
        }
        
        // Load network configuration from .env-like properties file
        applicationScope.launch {
            val networkResult = com.outdu.camconnect.utils.NetworkConfigManager.loadConfiguration(this@CamConnectApplication)
            networkResult.onSuccess { config ->
                Log.i("CamConnectApplication", "Network configuration loaded successfully: $config")
            }.onFailure { error ->
                Log.e("CamConnectApplication", "Failed to load network configuration", error)
            }
        }

        // Keep the camera WebSocket alive across UI changes while app is in foreground.
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                CameraWebSocketManager.onAppForegrounded()
                StreamLifecycleManager.onAppForegrounded()
            }

            override fun onStop(owner: LifecycleOwner) {
                CameraWebSocketManager.onAppBackgrounded()
                StreamLifecycleManager.onAppBackgrounded()
            }
        })
    }
} 