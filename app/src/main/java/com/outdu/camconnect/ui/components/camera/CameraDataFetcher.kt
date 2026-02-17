package com.outdu.camconnect.ui.components.camera

import androidx.compose.runtime.*
import com.outdu.camconnect.communication.MotocamAPIAndroidHelper
import com.outdu.camconnect.ui.models.CameraMode
import com.outdu.camconnect.ui.models.VisionMode
import com.outdu.camconnect.ui.viewmodels.IrIntensityLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Data class holding real-time camera information
 */
data class LiveCameraData(
    val irIntensityLevel: IrIntensityLevel,
    val visionMode: VisionMode,
    val cameraMode: CameraMode,
    val isLoading: Boolean,
    val error: String?
)

/**
 * Hook to fetch camera data directly from the device in real-time
 * Polls the camera every [pollIntervalMs] milliseconds
 */
@Composable
fun rememberLiveCameraData(
    pollIntervalMs: Long = 2000L,
    enabled: Boolean = true
): LiveCameraData {
    var irIntensityLevel by remember { mutableStateOf(IrIntensityLevel.OFF) }
    var visionMode by remember { mutableStateOf(VisionMode.VISION) }
    var cameraMode by remember { mutableStateOf(CameraMode.OFF) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect
        
        while (isActive) {
            isLoading = true
            
            MotocamAPIAndroidHelper.getConfigAsync(
                scope = this,
                type = "Current"
            ) { config, errorMsg ->
                if (config != null) {
                    // Parse IR brightness
                    val irBrightness = when (val brightness = config["IRBRIGHTNESS"]) {
                        is Number -> brightness.toInt()
                        is String -> brightness.toIntOrNull() ?: 0
                        else -> 0
                    }
                    irIntensityLevel = IrIntensityLevel.fromBrightness(irBrightness)
                    
                    // Parse MISC for vision mode and camera mode
                    val misc = config["MISC"]?.toString()?.toIntOrNull() ?: 1
                    
                    visionMode = when {
                        misc in 1..4 -> VisionMode.VISION
                        misc in 5..8 -> VisionMode.BOTH
                        misc in 9..12 -> VisionMode.INFRARED
                        else -> VisionMode.VISION
                    }
                    
                    cameraMode = when {
                        misc == 4 || misc == 12 -> CameraMode.FOURK
                        misc % 4 == 1 -> CameraMode.OFF
                        misc % 4 == 2 -> CameraMode.EIS
                        misc % 4 == 3 -> CameraMode.HDR
                        misc % 4 == 0 -> CameraMode.BOTH
                        else -> CameraMode.OFF
                    }
                    
                    error = null
                } else {
                    error = errorMsg ?: "Failed to fetch camera config"
                }
                
                isLoading = false
            }
            
            delay(pollIntervalMs)
        }
    }
    
    return LiveCameraData(
        irIntensityLevel = irIntensityLevel,
        visionMode = visionMode,
        cameraMode = cameraMode,
        isLoading = isLoading,
        error = error
    )
}
