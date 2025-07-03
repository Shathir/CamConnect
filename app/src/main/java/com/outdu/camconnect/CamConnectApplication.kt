package com.outdu.camconnect

import android.app.Application
import android.util.Log
import com.outdu.camconnect.auth.SessionManager

class CamConnectApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize SessionManager globally
        SessionManager.initialize(this)
        Log.i("CamConnectApplication", "SessionManager initialized globally")
    }
} 