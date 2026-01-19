package com.outdu.camconnect.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outdu.camconnect.auth.SessionManager
import com.outdu.camconnect.services.OnvifDevice
import com.outdu.camconnect.services.discoverOnvifDevices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the Viewer Flow state and operations
 */
class ViewerFlowViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "ViewerFlowViewModel"
    }
    
    // UI State
    private val _uiState = MutableStateFlow(ViewerFlowState())
    val uiState: StateFlow<ViewerFlowState> = _uiState.asStateFlow()
    
    // Selected camera for authentication
    private val _selectedCamera = MutableStateFlow<OnvifDevice?>(null)
    val selectedCamera: StateFlow<OnvifDevice?> = _selectedCamera.asStateFlow()
    
    /**
     * Start ONVIF discovery service
     */
    fun startDiscovery() {
        Log.i(TAG, "Starting ONVIF discovery...")
        
        _uiState.value = _uiState.value.copy(
            isDiscovering = true,
            discoveredCameras = emptyList(),
            errorMessage = null
        )
        
        viewModelScope.launch {
            try {
                discoverOnvifDevices { devices ->
                    Log.i(TAG, "Discovery completed. Found ${devices.size} cameras")
                    
                    _uiState.value = _uiState.value.copy(
                        isDiscovering = false,
                        discoveredCameras = devices,
                        showNoCamerasDialog = devices.isEmpty()
                    )
                    
                    if (devices.isEmpty()) {
                        Log.w(TAG, "No cameras discovered")
                    } else {
                        devices.forEachIndexed { index, camera ->
                            Log.d(TAG, "Camera ${index + 1}: IP=${camera.ipAddress}, Type=${camera.deviceType}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during discovery", e)
                _uiState.value = _uiState.value.copy(
                    isDiscovering = false,
                    errorMessage = "Discovery failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Retry discovery (called when user returns from WiFi settings)
     */
    fun retryDiscovery() {
        Log.i(TAG, "Retrying discovery...")
        dismissNoCamerasDialog()
        startDiscovery()
    }
    
    /**
     * Select a camera for authentication
     */
    fun selectCamera(camera: OnvifDevice) {
        Log.i(TAG, "Camera selected: ${camera.ipAddress}")
        _selectedCamera.value = camera
        _uiState.value = _uiState.value.copy(
            showPinDialog = true
        )
    }
    
    /**
     * Authenticate with PIN
     */
    fun authenticateWithPin(pin: String, onSuccess: (OnvifDevice) -> Unit) {
        val camera = _selectedCamera.value
        if (camera == null) {
            Log.e(TAG, "No camera selected for authentication")
            return
        }
        
        Log.i(TAG, "Authenticating with PIN for camera: ${camera.ipAddress}")
        
        _uiState.value = _uiState.value.copy(
            isAuthenticating = true,
            authError = null
        )
        
        viewModelScope.launch {
            try {
                val result = SessionManager.authenticateWithPin(pin, camera.ipAddress)
                
                if (result.isSuccess) {
                    Log.i(TAG, "PIN authentication successful")
                    _uiState.value = _uiState.value.copy(
                        isAuthenticating = false,
                        showPinDialog = false
                    )
                    onSuccess(camera)
                } else {
                    val error = result.exceptionOrNull()
                    Log.w(TAG, "PIN authentication failed: ${error?.message}")
                    _uiState.value = _uiState.value.copy(
                        isAuthenticating = false,
                        authError = error?.message ?: "Authentication failed"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during authentication", e)
                _uiState.value = _uiState.value.copy(
                    isAuthenticating = false,
                    authError = "Authentication error: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Dismiss the no cameras dialog
     */
    fun dismissNoCamerasDialog() {
        _uiState.value = _uiState.value.copy(showNoCamerasDialog = false)
    }
    
    /**
     * Dismiss the PIN dialog
     */
    fun dismissPinDialog() {
        _uiState.value = _uiState.value.copy(
            showPinDialog = false,
            authError = null
        )
        _selectedCamera.value = null
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Clear authentication error
     */
    fun clearAuthError() {
        _uiState.value = _uiState.value.copy(authError = null)
    }
    
    /**
     * Show QR scanner
     */
    fun showQRScanner() {
        _uiState.value = _uiState.value.copy(showQRScanner = true)
    }
    
    /**
     * Dismiss QR scanner
     */
    fun dismissQRScanner() {
        _uiState.value = _uiState.value.copy(
            showQRScanner = false,
            wifiConnectionError = null
        )
    }
    
    /**
     * Set WiFi connection state
     */
    fun setWifiConnecting(isConnecting: Boolean) {
        _uiState.value = _uiState.value.copy(
            isConnectingToWifi = isConnecting,
            wifiConnectionSuccess = false
        )
    }
    
    /**
     * Set WiFi connection success
     */
    fun setWifiConnectionSuccess(success: Boolean) {
        _uiState.value = _uiState.value.copy(
            wifiConnectionSuccess = success,
            isConnectingToWifi = false
        )
    }
    
    /**
     * Set WiFi connection error
     */
    fun setWifiConnectionError(error: String?) {
        _uiState.value = _uiState.value.copy(
            wifiConnectionError = error,
            isConnectingToWifi = false,
            wifiConnectionSuccess = false
        )
    }
    
    /**
     * Clear WiFi connection error
     */
    fun clearWifiConnectionError() {
        _uiState.value = _uiState.value.copy(wifiConnectionError = null)
    }
}

/**
 * Data class representing the UI state of the Viewer Flow
 */
data class ViewerFlowState(
    val isDiscovering: Boolean = false,
    val discoveredCameras: List<OnvifDevice> = emptyList(),
    val showNoCamerasDialog: Boolean = false,
    val showPinDialog: Boolean = false,
    val isAuthenticating: Boolean = false,
    val errorMessage: String? = null,
    val authError: String? = null,
    val showQRScanner: Boolean = false,
    val isConnectingToWifi: Boolean = false,
    val wifiConnectionError: String? = null,
    val wifiConnectionSuccess: Boolean = false
)
