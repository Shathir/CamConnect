package com.outdu.camconnect.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outdu.camconnect.communication.MotocamAPIAndroidHelper
import com.outdu.camconnect.communication.MotocamAPIHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CameraControlState(
    val isIrEnabled: Boolean = false,
    val irBrightness: Int = 0,
    val isLowIntensity: Boolean = false, // true means high intensity (15), false means low intensity (5)
    val currentZoom: Float = 1.0f,
    val isEisEnabled: Boolean = false,
    val isHdrEnabled: Boolean = false,
    val isZoomEnabled: Boolean = true,
    val isAutoDayNightEnabled: Boolean = false
)

class CameraControlViewModel : ViewModel() {
    private val _cameraControlState = MutableStateFlow(CameraControlState())
    val cameraControlState = _cameraControlState.asStateFlow()

    init {
        fetchInitialState()
    }

    // Add public function to refresh camera state
    fun refreshCameraState() {
        fetchInitialState()
    }

    fun setZoom(newZoom: Float) {
        viewModelScope.launch {
            try {
                val zoomLevel = when (newZoom) {
                    1f -> MotocamAPIHelper.ZOOM.X1
                    2f -> MotocamAPIHelper.ZOOM.X2
                    else -> MotocamAPIHelper.ZOOM.X4
                }

                val currentState = _cameraControlState.value
                // Don't update if zoom hasn't changed
                if (currentState.currentZoom == newZoom) {
                    return@launch
                }

                MotocamAPIAndroidHelper.setZoomAsync(
                    scope = viewModelScope,
                    zoom = zoomLevel
                ) { result, error ->
                    if (error != null) {
                        Log.e(TAG, "Error setting zoom: $error")
                        return@setZoomAsync
                    }

                    if (result) {
                        _cameraControlState.value = currentState.copy(
                            currentZoom = newZoom
                        )
                        Log.d(TAG, "Zoom set to ${newZoom}X")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in setZoom", e)
            }
        }
    }

    fun toggleIR() {
        viewModelScope.launch {
            try {
                val currentState = _cameraControlState.value
                val newBrightness = if (currentState.isIrEnabled) 0 else 15 // Default to low intensity when enabling IR

                MotocamAPIAndroidHelper.setIrBrightnessAsync(
                    scope = viewModelScope,
                    brightness = newBrightness
                ) { result, error ->
                    if (error != null) {
                        Log.e(TAG, "Error toggling IR: $error")
                        return@setIrBrightnessAsync
                    }

                    if (result) {
                        _cameraControlState.value = currentState.copy(
                            isIrEnabled = !currentState.isIrEnabled,
                            irBrightness = newBrightness,
                            isLowIntensity = newBrightness == 15 // Low intensity when 15
                        )
                        Log.d(TAG, "IR toggled: enabled=${!currentState.isIrEnabled}, brightness=$newBrightness")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in toggleIR", e)
            }
        }
    }

    fun toggleIrIntensity() {
        viewModelScope.launch {
            try {
                val currentState = _cameraControlState.value
                if (!currentState.isIrEnabled) return@launch

                // Toggle between 15 (low intensity) and 5 (high intensity)
                val newBrightness = if (currentState.isLowIntensity) 5 else 15

                MotocamAPIAndroidHelper.setIrBrightnessAsync(
                    scope = viewModelScope,
                    brightness = newBrightness
                ) { result, error ->
                    if (error != null) {
                        Log.e(TAG, "Error toggling IR intensity: $error")
                        return@setIrBrightnessAsync
                    }

                    if (result) {
                        _cameraControlState.value = currentState.copy(
                            irBrightness = newBrightness,
                            isLowIntensity = newBrightness == 15 // High when 5, Low when 15
                        )
                        Log.d(TAG, "IR intensity toggled: high=${newBrightness == 15}, brightness=$newBrightness")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in toggleIrIntensity", e)
            }
        }
    }

    private fun fetchInitialState() {
        viewModelScope.launch {
            try {
                MotocamAPIAndroidHelper.getConfigAsync(
                    viewModelScope,
                    type = "Current"
                ) { config, error ->
                    if (error != null) {
                        Log.e(TAG, "Error fetching camera config: $error")
                        return@getConfigAsync
                    }
                    
                    config?.let { conf ->
                        Log.i(TAG, "Fetched camera config: $conf")
                        
                        // Parse IR brightness - handle both String and Number types
                        val irBrightness = when (val brightness = conf["IRBRIGHTNESS"]) {
                            is Number -> brightness.toInt()
                            is String -> brightness.toIntOrNull() ?: 0
                            else -> 0
                        }

                        Log.i(TAG, "zoom value is ${conf["ZOOM"]}")
                        
                        // Parse zoom values from X1, X2, X4 format
                        val zoomValue = conf["ZOOM"]?.toString()
                        val currentZoom = when (zoomValue) {
                            "X1" -> 1.0f
                            "X2" -> 2.0f
                            "X4" -> 4.0f
                            else -> 1.0f // Default to 1x zoom if unknown value
                        }
                        
                        val irEnabled = irBrightness > 0
                        val isLowIntensity = irBrightness == 15

                        // Parse EIS and HDR states from MISC value
                        val misc = conf["MISC"]?.toString()?.toIntOrNull() ?: 1
                        val eisEnabled = misc % 4 == 2
                        val hdrEnabled = misc % 4 == 3
                        val zoomEnabled = !eisEnabled && !hdrEnabled

                        // Parse DAYMODE for Auto Low Light
                        val dayMode = conf["DAYMODE"]?.toString()
                        val isAutoDayNightEnabled = dayMode == "ON"
                        
                        Log.d(TAG, "Parsed values - IR Brightness: $irBrightness, IR Enabled: $irEnabled, Low Intensity: $isLowIntensity, Zoom: ${currentZoom}X, EIS: $eisEnabled, HDR: $hdrEnabled, AutoDayNight: $isAutoDayNightEnabled")
                        
                        _cameraControlState.value = CameraControlState(
                            isIrEnabled = irEnabled,
                            irBrightness = irBrightness,
                            isLowIntensity = isLowIntensity,
                            currentZoom = currentZoom,
                            isEisEnabled = eisEnabled,
                            isHdrEnabled = hdrEnabled,
                            isZoomEnabled = zoomEnabled,
                            isAutoDayNightEnabled = isAutoDayNightEnabled
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in fetchInitialState", e)
            }
        }
    }

    companion object {
        private const val TAG = "CameraControlViewModel"
    }
}