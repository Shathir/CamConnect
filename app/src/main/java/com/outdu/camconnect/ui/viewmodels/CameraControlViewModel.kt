package com.outdu.camconnect.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outdu.camconnect.communication.MotocamAPIAndroidHelper
import com.outdu.camconnect.communication.MotocamAPIHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class IrIntensityLevel(val brightness: Int, val displayName: String) {
    OFF(0, "Off"),
    LOW(2, "Low"),
    MEDIUM(4, "Medium"),
    HIGH(6, "High"),
    MAX(8, "Max"),
    ULTRA(10, "Ultra");

    companion object {
        fun fromBrightness(brightness: Int): IrIntensityLevel {
            val level = values().find { it.brightness == brightness } ?: OFF
            Log.d("IrIntensityLevel", "fromBrightness($brightness) -> ${level.displayName}")
            return level
        }
    }
}

data class CameraControlState(
    val irIntensityLevel: IrIntensityLevel = IrIntensityLevel.OFF,
    val irBrightness: Int = 0,
    val currentZoom: Float = 1.0f,
    val isEisEnabled: Boolean = false,
    val isHdrEnabled: Boolean = false,
    val isZoomEnabled: Boolean = true,
    val isAutoDayNightEnabled: Boolean = false
) {
    val isIrEnabled: Boolean get() = irIntensityLevel != IrIntensityLevel.OFF
}

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

                MotocamAPIAndroidHelper.getHealthStatusAsync(
                    scope = viewModelScope
                ) { status, error ->
                    if (error != null) {
                        Log.e(TAG, "Health check failed: $error")
                        return@getHealthStatusAsync
                    }

                    status?.let {
                        Log.d(TAG, "HealthStatus → RTSPS=${it.rtsps}, " +
                                "CPU=${it.cpuUsage}%, " +
                                "ISP Temp=${it.ispTemp}°C, " +
                                "memory=${it.memoryUsage}%, " +
                            "portablertc=${it.portableRtc}, " +
                            "irTemp=${it.irTemp}, " +
                        "sensorTemp=${it.sensorTemp} ")
                        // Update your ViewModel or UI state
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
                Log.d(TAG, "toggleIR called - Current state: ${currentState.irIntensityLevel.displayName} (brightness=${currentState.irBrightness})")
                
                // Cycle through the IR intensity levels: Off → Low → Medium → High → Max → Ultra → Off
                val nextLevel = when (currentState.irIntensityLevel) {
                    IrIntensityLevel.OFF -> IrIntensityLevel.LOW
                    IrIntensityLevel.LOW -> IrIntensityLevel.MEDIUM
                    IrIntensityLevel.MEDIUM -> IrIntensityLevel.HIGH
                    IrIntensityLevel.HIGH -> IrIntensityLevel.MAX
                    IrIntensityLevel.MAX -> IrIntensityLevel.ULTRA
                    IrIntensityLevel.ULTRA -> IrIntensityLevel.OFF
                }
                
                Log.d(TAG, "toggleIR - Next level will be: ${nextLevel.displayName} (brightness=${nextLevel.brightness})")

                MotocamAPIAndroidHelper.setIrBrightnessAsync(
                    scope = viewModelScope,
                    brightness = nextLevel.brightness
                ) { result, error ->
                    if (error != null) {
                        Log.e(TAG, "Error toggling IR: $error")
                        return@setIrBrightnessAsync
                    }

                    if (result) {
                        _cameraControlState.value = currentState.copy(
                            irIntensityLevel = nextLevel,
                            irBrightness = nextLevel.brightness
                        )
                        Log.d(TAG, "IR toggled successfully to: ${nextLevel.displayName} (brightness=${nextLevel.brightness})")
                    } else {
                        Log.e(TAG, "API call returned false - IR toggle failed")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in toggleIR", e)
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
                        
                        val irIntensityLevel = IrIntensityLevel.fromBrightness(irBrightness)

                        // Parse EIS and HDR states from MISC value
                        val misc = conf["MISC"]?.toString()?.toIntOrNull() ?: 1
                        val eisEnabled = misc % 4 == 2
                        val hdrEnabled = misc % 4 == 3
                        val zoomEnabled = !eisEnabled && !hdrEnabled

                        // Parse DAYMODE for Auto Low Light
                        val dayMode = conf["DAYMODE"]?.toString()
                        val isAutoDayNightEnabled = dayMode == "ON"
                        
                        Log.d(TAG, "Parsed values - IR Brightness: $irBrightness, IR Level: ${irIntensityLevel.displayName}, Zoom: ${currentZoom}X, EIS: $eisEnabled, HDR: $hdrEnabled, AutoDayNight: $isAutoDayNightEnabled")
                        
                        _cameraControlState.value = CameraControlState(
                            irIntensityLevel = irIntensityLevel,
                            irBrightness = irBrightness,
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