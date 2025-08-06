package com.outdu.camconnect.communication

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.InetAddress



data class HealthStatus(
    val streamer: Boolean,
    val rtsps: Boolean,
    val portableRtc: Boolean,
    val cpuUsage: Int,
    val memoryUsage: Int,
    val ispTemp: Int,
    val irTemp: Int,
    val sensorTemp: Int = -1
)

//data class HealthStatus(
//    val rtsps: Boolean,
//    val portableRtc: Boolean,
//    val cpuUsage: Int,
//    val memoryUsage: Int,
//    val ispTemperature: Int,
//    val irTemperature: Int,
//    val sensorTemperature: Int = -1 // fallback for unavailable
//)




object MotocamAPIAndroidHelper {

    private const val TAG = "MotocamAPIAndroidHelper"

    fun getConfigAsync(
        scope: CoroutineScope,
        type: String,
        callback: (Map<String, Any>?, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.getConfig(type)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "getConfigAsync failed", e)
                callback(null, e.message)
            }
        }
    }

    fun setIrBrightnessAsync(
        scope: CoroutineScope,
        brightness: Int,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setIrBrightness(brightness)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setIrBrightnessAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setZoomAsync(
        scope: CoroutineScope,
        zoom: MotocamAPIHelper.ZOOM,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setImgZoom(zoom)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setZoomAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setResolutionAsync(
        scope: CoroutineScope,
        resolution: MotocamAPIHelper.RESOLUTION,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setImgResolution(resolution)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setResolutionAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setTiltAsync(
        scope: CoroutineScope,
        tilt: MotocamAPIHelper.TILT,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setImgTilt(tilt)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setTiltAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setRotationAsync(
        scope: CoroutineScope,
        rotation: MotocamAPIHelper.ROTATION,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setImgRotation(rotation)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setRotationAsync failed", e)
                callback(false, e.message)
            }
        }
    }


    fun setIrCutFilterAsync(
        scope: CoroutineScope,
        filter: MotocamAPIHelper.IRCUTFILTER,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setIrCutFilter(filter)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setIrCutFilterAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setMirrorAsync(
        scope: CoroutineScope,
        mirror: MotocamAPIHelper.MIRROR,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setMirror(mirror)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setMirrorAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setFlipAsync(
        scope: CoroutineScope,
        flip: MotocamAPIHelper.FLIP,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setFlip(flip)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setFlipAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setWdrAsync(
        scope: CoroutineScope,
        wdr: MotocamAPIHelper.WDR,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setWdr(wdr)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setWdrAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setEisAsync(
        scope: CoroutineScope,
        eis: MotocamAPIHelper.EIS,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setEis(eis)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setEisAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setMiscAsync(
        scope: CoroutineScope,
        miscValue: Int,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setMisc(miscValue)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setMiscAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setDayModeAsync(
        scope: CoroutineScope,
        dayMode: MotocamAPIHelper.DAYMODE,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setDayMode(dayMode)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setDayModeAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setGyroReaderAsync(
        scope: CoroutineScope,
        gyroReader: MotocamAPIHelper.GYROREADER,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setGyroReader(gyroReader)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setGyroReaderAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setMicAsync(
        scope: CoroutineScope,
        mic: MotocamAPIHelper.MIC,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setMic(mic)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setMicAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setDefaultToCurrentAsync(
        scope: CoroutineScope,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setDefaultToCurrent()
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setDefaultToCurrentAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setDefaultToFactoryAsync(
        scope: CoroutineScope,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setDefaultToFactory()
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setDefaultToFactoryAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setCurrentToDefaultAsync(
        scope: CoroutineScope,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setCurrentToDefault()
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setCurrentToDefaultAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setCurrentToFactoryAsync(
        scope: CoroutineScope,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setCurrentToFactory()
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setCurrentToFactoryAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun getWifiStateAsync(
        scope: CoroutineScope,
        callback: (MotocamAPIHelper.WifiState?, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.getWifiState()
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "getWifiStateAsync failed", e)
                callback(null, e.message)
            }
        }
    }

    fun setWifiHotspotAsync(
        scope: CoroutineScope,
        ssid: String,
        encryptionType: String,
        encryptionKey: String,
        ipAddress: String,
        subnetMask: String,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setWifiHotspot(ssid, encryptionType, encryptionKey, ipAddress, subnetMask)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setWifiHotspotAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun setWifiClientAsync(
        scope: CoroutineScope,
        ssid: String,
        encryptionType: String,
        encryptionKey: String,
        ipAddress: String,
        subnetMask: String,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.setWifiClient(ssid, encryptionType, encryptionKey, ipAddress, subnetMask)
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "setWifiClientAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun getWifiHotspotConfigAsync(
        scope: CoroutineScope,
        callback: (Map<String, Any>?, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.getWifiHotspotConfig()
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "getWifiHotspotConfigAsync failed", e)
                callback(null, e.message)
            }
        }
    }

    fun getWifiClientConfigAsync(
        scope: CoroutineScope,
        callback: (Map<String, Any>?, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.getWifiClientConfig()
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "getWifiClientConfigAsync failed", e)
                callback(null, e.message)
            }
        }
    }

    fun getDeviceNameAsync(
        scope: CoroutineScope,
        ip: String,
        callback: (String?, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = InetAddress.getByName(ip).hostName
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "getDeviceNameAsync failed", e)
                callback(null, e.message)
            }
        }
    }

    fun startStreamAsync(
        scope: CoroutineScope,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.startStream()
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "startStreamAsync failed", e)
                callback(false, e.message)
            }
        }
    }

    fun rebootAsync(
        scope: CoroutineScope,
        callback: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                val result = MotocamAPIHelperWrapper.shutdownCamera()
                callback(result, null)
            } catch (e: Exception) {
                Log.e(TAG, "rebootAsync failed", e)
                callback(false, e.message)
            }
        }
    }


    fun getHealthStatusAsync(
        scope: CoroutineScope,
        callback: (HealthStatus?, String?) -> Unit
    ) {
        scope.launch {
            try {
                val status = MotocamAPIHelperWrapper.getHealthStatus()
                callback(status, null)
            } catch (e: Exception) {
                Log.e(TAG, "getHealthStatusAsync failed", e)
                callback(null, e.message)
            }
        }
    }



}
