package com.outdu.camconnect

import android.app.Application
import android.util.Log
import com.outdu.camconnect.auth.SessionManager
import com.outdu.camconnect.communication.CameraConfigurationManager
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
    }
} 