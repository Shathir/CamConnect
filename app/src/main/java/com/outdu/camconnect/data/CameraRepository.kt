package com.outdu.camconnect.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Repository for managing camera storage and retrieval
 * Handles both local storage and server synchronization
 */
class CameraRepository(private val context: Context) {
    
    companion object {
        private const val TAG = "CameraRepository"
        private const val PREFS_NAME = "cam_connect_cameras"
        private const val CAMERAS_KEY = "registered_cameras"
        private const val LAST_SYNC_KEY = "last_server_sync"
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // In-memory cache with reactive updates
    private val _cameras = MutableStateFlow<List<StoredCamera>>(emptyList())
    val cameras: StateFlow<List<StoredCamera>> = _cameras.asStateFlow()
    
    private val _onlineCameras = MutableStateFlow<Set<String>>(emptySet())
    val onlineCameras: StateFlow<Set<String>> = _onlineCameras.asStateFlow()
    
    init {
        loadCamerasFromStorage()
    }
    
    /**
     * Add a new camera (from QR scan or manual entry)
     */
    suspend fun addCamera(camera: StoredCamera): Result<StoredCamera> = withContext(Dispatchers.IO) {
        try {
            val currentCameras = _cameras.value.toMutableList()
            
            // Check for duplicates
            val existingCamera = currentCameras.find { 
                it.ipAddress == camera.ipAddress || it.serialNumber == camera.serialNumber 
            }
            
            if (existingCamera != null) {
                Log.w(TAG, "Camera already exists: ${camera.name}")
                return@withContext Result.failure(
                    CameraException("Camera with this IP or serial number already exists")
                )
            }
            
            // Add camera with current timestamp
            val newCamera = camera.copy(
                id = camera.id.ifEmpty { UUID.randomUUID().toString() },
                addedAt = System.currentTimeMillis(),
                lastSeen = System.currentTimeMillis()
            )
            
            currentCameras.add(newCamera)
            
            // Update in-memory state
            _cameras.value = currentCameras
            
            // Persist to storage
            saveCamerasToStorage(currentCameras)
            
            Log.i(TAG, "Camera added: ${newCamera.name} (${newCamera.ipAddress})")
            Result.success(newCamera)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add camera", e)
            Result.failure(CameraException("Failed to add camera: ${e.message}", e))
        }
    }
    
    /**
     * Remove a camera
     */
    suspend fun removeCamera(cameraId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val currentCameras = _cameras.value.toMutableList()
            val removed = currentCameras.removeAll { it.id == cameraId }
            
            if (removed) {
                _cameras.value = currentCameras
                saveCamerasToStorage(currentCameras)
                Log.i(TAG, "Camera removed: $cameraId")
            }
            
            Result.success(removed)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove camera", e)
            Result.failure(CameraException("Failed to remove camera: ${e.message}", e))
        }
    }
    
    /**
     * Update camera online status (from ONVIF discovery)
     */
    fun updateCameraOnlineStatus(onlineCameraIPs: List<String>) {
        val onlineSet = onlineCameraIPs.toSet()
        _onlineCameras.value = onlineSet
        
        // Update camera last seen timestamps for online cameras
        val updatedCameras = _cameras.value.map { camera ->
            if (onlineSet.contains(camera.ipAddress)) {
                camera.copy(lastSeen = System.currentTimeMillis())
            } else {
                camera
            }
        }
        
        _cameras.value = updatedCameras
        saveCamerasToStorage(updatedCameras)
        
        Log.d(TAG, "Updated online status: ${onlineSet.size} cameras online")
    }
    
    /**
     * Get camera by ID
     */
    fun getCameraById(id: String): StoredCamera? {
        return _cameras.value.find { it.id == id }
    }
    
    /**
     * Get camera by IP address
     */
    fun getCameraByIP(ipAddress: String): StoredCamera? {
        return _cameras.value.find { it.ipAddress == ipAddress }
    }
    
    /**
     * Check if camera is online
     */
    fun isCameraOnline(cameraId: String): Boolean {
        val camera = getCameraById(cameraId)
        return camera?.let { _onlineCameras.value.contains(it.ipAddress) } ?: false
    }
    
    /**
     * Get cameras with online status
     */
    fun getCamerasWithStatus(): List<CameraWithStatus> {
        val onlineIPs = _onlineCameras.value
        return _cameras.value.map { camera ->
            CameraWithStatus(
                camera = camera,
                isOnline = onlineIPs.contains(camera.ipAddress),
                lastSeenMinutesAgo = (System.currentTimeMillis() - camera.lastSeen) / (1000 * 60)
            )
        }
    }
    
    /**
     * Sync with server (fetch registered devices)
     */
    suspend fun syncWithServer(): Result<List<StoredCamera>> = withContext(Dispatchers.IO) {
        try {
            // TODO: Implement actual server API call
            // For now, return current cameras
            Log.d(TAG, "Server sync not implemented yet")
            Result.success(_cameras.value)
            
        } catch (e: Exception) {
            Log.e(TAG, "Server sync failed", e)
            Result.failure(CameraException("Server sync failed: ${e.message}", e))
        }
    }
    
    /**
     * Clear all cameras (for reset/logout)
     */
    suspend fun clearAllCameras(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            _cameras.value = emptyList()
            _onlineCameras.value = emptySet()
            
            sharedPreferences.edit()
                .remove(CAMERAS_KEY)
                .remove(LAST_SYNC_KEY)
                .apply()
            
            Log.i(TAG, "All cameras cleared")
            Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cameras", e)
            Result.failure(CameraException("Failed to clear cameras: ${e.message}", e))
        }
    }
    
    /**
     * Get storage statistics
     */
    fun getStorageStats(): CameraStorageStats {
        val cameras = _cameras.value
        val onlineCount = _onlineCameras.value.size
        
        return CameraStorageStats(
            totalCameras = cameras.size,
            onlineCameras = onlineCount,
            offlineCameras = cameras.size - onlineCount,
            lastSyncTime = sharedPreferences.getLong(LAST_SYNC_KEY, 0),
            storageSize = estimateStorageSize(cameras)
        )
    }
    
    // Private helper methods
    
    private fun loadCamerasFromStorage() {
        try {
            val camerasJson = sharedPreferences.getString(CAMERAS_KEY, null)
            if (camerasJson != null) {
                val cameras = json.decodeFromString<List<StoredCamera>>(camerasJson)
                _cameras.value = cameras
                Log.d(TAG, "Loaded ${cameras.size} cameras from storage")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load cameras from storage", e)
            _cameras.value = emptyList()
        }
    }
    
    private fun saveCamerasToStorage(cameras: List<StoredCamera>) {
        try {
            val camerasJson = json.encodeToString(cameras)
            sharedPreferences.edit()
                .putString(CAMERAS_KEY, camerasJson)
                .putLong(LAST_SYNC_KEY, System.currentTimeMillis())
                .apply()
            
            Log.d(TAG, "Saved ${cameras.size} cameras to storage")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save cameras to storage", e)
        }
    }
    
    private fun estimateStorageSize(cameras: List<StoredCamera>): Long {
        return try {
            val json = json.encodeToString(cameras)
            json.toByteArray().size.toLong()
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Data class for stored camera information
 */
@Serializable
data class StoredCamera(
    val id: String = "",
    val name: String = "",
    val ipAddress: String = "",
    val port: Int = 80,
    val manufacturer: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val firmwareVersion: String = "",
    val onvifEndpoints: List<String> = emptyList(),
    val capabilities: List<String> = emptyList(),
    val addedAt: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val userNotes: String = "",
    val qrCodeData: String = "" // Original QR code data
)

/**
 * Camera with current online status
 */
data class CameraWithStatus(
    val camera: StoredCamera,
    val isOnline: Boolean,
    val lastSeenMinutesAgo: Long
)

/**
 * Storage statistics
 */
data class CameraStorageStats(
    val totalCameras: Int,
    val onlineCameras: Int,
    val offlineCameras: Int,
    val lastSyncTime: Long,
    val storageSize: Long
)

/**
 * Camera-related exceptions
 */
class CameraException(message: String, cause: Throwable? = null) : Exception(message, cause)
