package com.outdu.camconnect.auth

import android.content.Context
import android.util.Log
import com.outdu.camconnect.auth.UserStateManager.UserFlowType
import com.outdu.camconnect.auth.UserStateManager.UserType

/**
 * Detects and determines the appropriate setup flow for users
 * Combines UserStateManager and SessionManager to make flow decisions
 */
class SetupFlowDetector(private val context: Context) {
    
    companion object {
        private const val TAG = "SetupFlowDetector"
    }
    
    /**
     * Determine the starting destination for the setup flow
     */
    fun getStartingDestination(): String {
        // Ensure managers are initialized
        UserStateManager.initialize(context)
        SessionManager.initialize(context)
        
        val flowType = UserStateManager.getUserFlowType()
        val isAuthenticated = SessionManager.isAuthenticated()
        
        Log.i(TAG, "Determining flow - Type: $flowType, Authenticated: $isAuthenticated")
        Log.d(TAG, "User Status: ${UserStateManager.getUserStatus()}")
        Log.d(TAG, "Session Status: ${SessionManager.getSessionStatus()}")
        
        return when (flowType) {
            UserFlowType.FIRST_TIME -> {
                // Brand new user - start with landing/intro screen
                UserStateManager.markAppLaunched()
                "landing"
            }
            
            UserFlowType.RETURNING_WITH_CAMERAS -> {
                if (isAuthenticated) {
                    // User has cameras and is authenticated - go straight to main app
                    "main_activity"
                } else {
                    // User has cameras but needs authentication - go to viewer camera list
                    "viewer_camera_list"
                }
            }
            
            UserFlowType.RETURNING_NO_CAMERAS -> {
                if (isAuthenticated) {
                    // User is authenticated but has no cameras - show camera add screen
                    "camera_add"
                } else {
                    // User has no cameras and needs authentication - start with login
                    "login"
                }
            }
            
            UserFlowType.VIEWER -> {
                // Viewer user - start with viewer camera list
                "viewer_camera_list"
            }
            
            UserFlowType.INCOMPLETE_SETUP -> {
                // User started setup but didn't complete - restart with login
                "login"
            }
        }
    }
    
    /**
     * Get the complete setup flow configuration based on user state
     */
    fun getSetupFlowConfig(): SetupFlowConfig {
        val flowType = UserStateManager.getUserFlowType()
        val userState = UserStateManager.userState.value
        
        return when (flowType) {
            UserFlowType.FIRST_TIME -> SetupFlowConfig(
                showLanding = true,
                showLicenseFetch = true,
                showCameraDiscovery = true,
                showQRScanner = true,
                requireAuthentication = true,
                userType = UserType.UNKNOWN
            )
            
            UserFlowType.RETURNING_WITH_CAMERAS -> SetupFlowConfig(
                showLanding = false,
                showLicenseFetch = false,
                showCameraDiscovery = true, // Still show to add more cameras
                showQRScanner = true,
                requireAuthentication = !SessionManager.isAuthenticated(),
                userType = userState.userType
            )
            
            UserFlowType.RETURNING_NO_CAMERAS -> SetupFlowConfig(
                showLanding = false,
                showLicenseFetch = true, // Re-fetch in case user added cameras on web
                showCameraDiscovery = true,
                showQRScanner = true,
                requireAuthentication = true,
                userType = userState.userType
            )
            
            UserFlowType.VIEWER -> SetupFlowConfig(
                showLanding = false,
                showLicenseFetch = false,
                showCameraDiscovery = true,
                showQRScanner = false, // Viewers can't add cameras
                requireAuthentication = true,
                userType = UserType.VIEWER
            )
            
            UserFlowType.INCOMPLETE_SETUP -> SetupFlowConfig(
                showLanding = false,
                showLicenseFetch = true,
                showCameraDiscovery = true,
                showQRScanner = true,
                requireAuthentication = true,
                userType = userState.userType
            )
        }
    }
    
    /**
     * Check if user should skip directly to main activity
     */
    fun shouldSkipSetup(): Boolean {
        val flowType = UserStateManager.getUserFlowType()
        val isAuthenticated = SessionManager.isAuthenticated()
        val hasRegisteredCameras = UserStateManager.hasRegisteredCameras()
        
        return flowType == UserFlowType.RETURNING_WITH_CAMERAS && 
               isAuthenticated && 
               hasRegisteredCameras
    }
    
    /**
     * Determine if ONVIF discovery should run automatically
     */
    fun shouldRunAutoDiscovery(): Boolean {
        val flowType = UserStateManager.getUserFlowType()
        return when (flowType) {
            UserFlowType.FIRST_TIME -> false // Only after cameras are added
            UserFlowType.RETURNING_WITH_CAMERAS -> true // Always check status
            UserFlowType.RETURNING_NO_CAMERAS -> false // Only after cameras are added
            UserFlowType.VIEWER -> true // Always discover for viewers
            UserFlowType.INCOMPLETE_SETUP -> false // Only after setup resumes
        }
    }
    
    /**
     * Get appropriate authentication method based on user state
     */
    fun getAuthenticationMethod(): AuthenticationMethod {
        val userState = UserStateManager.userState.value
        
        return when (userState.userType) {
            UserType.OWNER -> AuthenticationMethod.PIN_WITH_SETUP
            UserType.VIEWER -> AuthenticationMethod.PIN_ONLY
            UserType.UNKNOWN -> AuthenticationMethod.DETERMINE_ON_LOGIN
        }
    }
    
    enum class AuthenticationMethod {
        PIN_WITH_SETUP,        // Owner can set up cameras after PIN
        PIN_ONLY,              // Viewer just needs PIN to view
        DETERMINE_ON_LOGIN     // Determine user type during login process
    }
}

/**
 * Configuration for setup flow based on user state
 */
data class SetupFlowConfig(
    val showLanding: Boolean,
    val showLicenseFetch: Boolean,
    val showCameraDiscovery: Boolean,
    val showQRScanner: Boolean,
    val requireAuthentication: Boolean,
    val userType: UserType
)
