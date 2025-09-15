package com.outdu.camconnect.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.outdu.camconnect.data.CameraRepository

/**
 * Manages user state to determine if user is first-time or returning
 * Works alongside SessionManager to provide complete user state management
 */
object UserStateManager {
    
    private const val TAG = "UserStateManager"
    private const val PREFS_NAME = "cam_connect_user_state"
    
    // Keys for SharedPreferences
    private const val IS_FIRST_LAUNCH_KEY = "is_first_launch"
    private const val HAS_COMPLETED_SETUP_KEY = "has_completed_setup"
    private const val USER_TYPE_KEY = "user_type"
    private const val REGISTERED_CAMERAS_COUNT_KEY = "registered_cameras_count"
    private const val LAST_SETUP_COMPLETION_TIME_KEY = "last_setup_completion_time"
    private const val APP_VERSION_KEY = "app_version"
    
    private var sharedPreferences: SharedPreferences? = null
    private var cameraRepository: CameraRepository? = null
    private var isInitialized = false
    
    // State flows for reactive UI
    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState.asStateFlow()
    
    /**
     * User types in the system
     */
    enum class UserType {
        OWNER,      // Can add/manage cameras
        VIEWER,     // Can only view cameras
        UNKNOWN     // Not determined yet
    }
    
    /**
     * Data class representing complete user state
     */
    data class UserState(
        val isFirstLaunch: Boolean = true,
        val hasCompletedSetup: Boolean = false,
        val userType: UserType = UserType.UNKNOWN,
        val registeredCamerasCount: Int = 0,
        val isAuthenticated: Boolean = false,
        val needsOnboarding: Boolean = true
    )
    
    /**
     * Initialize UserStateManager with application context
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        cameraRepository = CameraRepository(context)
        loadUserState()
        isInitialized = true
        
        Log.i(TAG, "UserStateManager initialized")
    }
    
    /**
     * Determine if user is first-time or returning
     */
    fun getUserFlowType(): UserFlowType {
        val state = _userState.value
        
        return when {
            state.isFirstLaunch -> UserFlowType.FIRST_TIME
            state.hasCompletedSetup && state.registeredCamerasCount > 0 -> UserFlowType.RETURNING_WITH_CAMERAS
            state.hasCompletedSetup && state.registeredCamerasCount == 0 -> UserFlowType.RETURNING_NO_CAMERAS
            state.userType == UserType.VIEWER -> UserFlowType.VIEWER
            else -> UserFlowType.INCOMPLETE_SETUP
        }
    }
    
    /**
     * Different user flow types
     */
    enum class UserFlowType {
        FIRST_TIME,              // Brand new user, needs full setup
        RETURNING_WITH_CAMERAS,  // Has cameras registered, can start streaming
        RETURNING_NO_CAMERAS,    // Has completed setup but no cameras registered
        VIEWER,                  // Viewer user type
        INCOMPLETE_SETUP         // Started setup but didn't complete
    }
    
    /**
     * Mark that the user has launched the app (no longer first launch)
     */
    fun markAppLaunched() {
        updateUserState { it.copy(isFirstLaunch = false) }
        saveToPreferences(IS_FIRST_LAUNCH_KEY, false)
        Log.d(TAG, "App launch marked")
    }
    
    /**
     * Mark that setup has been completed
     */
    fun markSetupCompleted(userType: UserType = UserType.OWNER) {
        updateUserState { 
            it.copy(
                hasCompletedSetup = true,
                userType = userType,
                needsOnboarding = false
            )
        }
        saveToPreferences(HAS_COMPLETED_SETUP_KEY, true)
        saveToPreferences(USER_TYPE_KEY, userType.name)
        saveToPreferences(LAST_SETUP_COMPLETION_TIME_KEY, System.currentTimeMillis())
        Log.i(TAG, "Setup completed for user type: $userType")
    }
    
    /**
     * Update registered cameras count
     */
    fun updateRegisteredCamerasCount(count: Int) {
        updateUserState { it.copy(registeredCamerasCount = count) }
        saveToPreferences(REGISTERED_CAMERAS_COUNT_KEY, count)
        Log.d(TAG, "Registered cameras count updated: $count")
    }
    
    /**
     * Set user type
     */
    fun setUserType(userType: UserType) {
        updateUserState { it.copy(userType = userType) }
        saveToPreferences(USER_TYPE_KEY, userType.name)
        Log.d(TAG, "User type set: $userType")
    }
    
    /**
     * Update authentication status (called by SessionManager)
     */
    fun updateAuthenticationStatus(isAuthenticated: Boolean) {
        updateUserState { it.copy(isAuthenticated = isAuthenticated) }
        Log.d(TAG, "Authentication status updated: $isAuthenticated")
    }
    
    /**
     * Check if user needs onboarding
     */
    fun needsOnboarding(): Boolean {
        val state = _userState.value
        return state.isFirstLaunch || !state.hasCompletedSetup
    }
    
    /**
     * Check if user has registered cameras locally
     */
    fun hasRegisteredCameras(): Boolean {
        return cameraRepository?.cameras?.value?.isNotEmpty() ?: false
    }
    
    /**
     * Get camera repository instance
     */
    fun getCameraRepository(): CameraRepository? {
        return cameraRepository
    }
    
    /**
     * Reset user state (for testing or factory reset)
     */
    fun resetUserState() {
        sharedPreferences?.edit()?.clear()?.apply()
        _userState.value = UserState()
        Log.i(TAG, "User state reset")
    }
    
    /**
     * Get detailed user status for debugging
     */
    fun getUserStatus(): String {
        val state = _userState.value
        val flowType = getUserFlowType()
        
        return buildString {
            append("Flow Type: $flowType")
            append(", First Launch: ${state.isFirstLaunch}")
            append(", Setup Complete: ${state.hasCompletedSetup}")
            append(", User Type: ${state.userType}")
            append(", Cameras: ${state.registeredCamerasCount}")
            append(", Authenticated: ${state.isAuthenticated}")
            append(", Needs Onboarding: ${state.needsOnboarding}")
        }
    }
    
    // Private helper methods
    
    private fun loadUserState() {
        val prefs = sharedPreferences ?: return
        
        val isFirstLaunch = prefs.getBoolean(IS_FIRST_LAUNCH_KEY, true)
        val hasCompletedSetup = prefs.getBoolean(HAS_COMPLETED_SETUP_KEY, false)
        val userTypeString = prefs.getString(USER_TYPE_KEY, UserType.UNKNOWN.name)
        val userType = try {
            UserType.valueOf(userTypeString ?: UserType.UNKNOWN.name)
        } catch (e: Exception) {
            UserType.UNKNOWN
        }
        val registeredCamerasCount = prefs.getInt(REGISTERED_CAMERAS_COUNT_KEY, 0)
        
        _userState.value = UserState(
            isFirstLaunch = isFirstLaunch,
            hasCompletedSetup = hasCompletedSetup,
            userType = userType,
            registeredCamerasCount = registeredCamerasCount,
            isAuthenticated = SessionManager.isAuthenticated(),
            needsOnboarding = isFirstLaunch || !hasCompletedSetup
        )
        
        Log.d(TAG, "User state loaded: ${getUserStatus()}")
    }
    
    private fun updateUserState(update: (UserState) -> UserState) {
        _userState.value = update(_userState.value)
    }
    
    private fun saveToPreferences(key: String, value: Any) {
        val editor = sharedPreferences?.edit() ?: return
        
        when (value) {
            is Boolean -> editor.putBoolean(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is String -> editor.putString(key, value)
        }
        
        editor.apply()
    }
}
