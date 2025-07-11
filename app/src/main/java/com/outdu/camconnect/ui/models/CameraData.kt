package com.outdu.camconnect.ui.models

/**
 * Camera modes available in the app
 */
enum class CameraMode {
    HDR,
    EIS,
    BOTH,
    OFF
}

/**
 * Vision modes for camera display
 */
enum class VisionMode {
    VISION,
    INFRARED,
    BOTH
}

/**
 * Orientation settings for camera
 */
enum class OrientationMode {
    FLIP,
    MIRROR,
    BOTH,
    NORMAL
}

/**
 * Data class for camera state
 */
data class CameraState(
    val isRecording: Boolean = false,
    val currentCamera: Int = 0, // Camera index for multiple cameras
    val zoomLevel: Float = 1.0f, // 1x, 2x, 4x etc
    val cameraMode: CameraMode = CameraMode.HDR,
    val visionMode: VisionMode = VisionMode.VISION,
    val orientationMode: OrientationMode = OrientationMode.NORMAL,
    val isAutoDayNightEnabled: Boolean = true
)

/**
 * Detection settings
 */
data class DetectionSettings(
    val isObjectDetectionEnabled: Boolean = false,
    val isFarObjectDetectionEnabled: Boolean = false,
    val isMotionDetectionEnabled: Boolean = false
)

/**
 * System status indicators
 */
data class SystemStatus(
    val batteryLevel: Int = 100, // 0-100
    val isWifiConnected: Boolean = true,
    val isLteConnected: Boolean = false,
    val isOnline: Boolean = true,
    val isAiEnabled: Boolean = true,
    val currentSpeed: Float = 0f, // Speed from phone sensors in km/h
    val compassDirection: Float = 0f // Direction in degrees (0-360)
) 