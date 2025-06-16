package com.outdu.camconnect.communication

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Properties
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Manages camera configuration data with thread-safe operations and persistence.
 * Handles loading/saving configuration properties to external storage.
 */
object CameraConfigurationManager {
    
    private const val TAG = "CameraConfigManager"
    private const val CONFIG_FILE_NAME = "camera_config.properties"
    
    // Configuration properties with default values
    @Volatile private var farDetectionEnabled = false
    @Volatile private var objectDetectionEnabled = true
    @Volatile private var drowsinessDetectionEnabled = false
    @Volatile private var audioEnabled = false
    @Volatile private var modelVersion = 0
    @Volatile private var drowsinessThreshold = 0.8f
    
    // Thread-safe read-write lock for configuration access
    private val configLock = ReentrantReadWriteLock()
    
    /**
     * Configuration data class for type-safe access
     */
    data class CameraConfig(
        val farDetectionEnabled: Boolean,
        val objectDetectionEnabled: Boolean,
        val drowsinessDetectionEnabled: Boolean,
        val audioEnabled: Boolean,
        val modelVersion: Int,
        val drowsinessThreshold: Float
    )
    
    /**
     * Custom exceptions for configuration management
     */
    sealed class ConfigurationException(message: String, cause: Throwable? = null) : Exception(message, cause) {
        class LoadException(message: String, cause: Throwable) : ConfigurationException("Failed to load configuration: $message", cause)
        class SaveException(message: String, cause: Throwable) : ConfigurationException("Failed to save configuration: $message", cause)
        class ValidationException(message: String) : ConfigurationException("Configuration validation failed: $message")
    }
    
    /**
     * Asynchronously loads configuration from persistent storage
     */
    suspend fun loadConfigurationAsync(context: Context): Result<CameraConfig> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Loading camera configuration...")
            val configFile = getConfigFile(context)
            
            if (!configFile.exists()) {
                Log.i(TAG, "Configuration file not found, using defaults")
                return@withContext Result.success(getCurrentConfiguration())
            }
            
            val properties = Properties()
            configFile.inputStream().use { inputStream ->
                properties.load(inputStream)
            }
            
            configLock.write {
                properties.getProperty("far_detection")?.toBooleanStrictOrNull()?.let { farDetectionEnabled = it }
                properties.getProperty("object_detection")?.toBooleanStrictOrNull()?.let { objectDetectionEnabled = it }
                properties.getProperty("drowsiness_detection")?.toBooleanStrictOrNull()?.let { drowsinessDetectionEnabled = it }
                properties.getProperty("audio_enabled")?.toBooleanStrictOrNull()?.let { audioEnabled = it }
                properties.getProperty("model_version")?.toIntOrNull()?.let { modelVersion = it }
                properties.getProperty("drowsiness_threshold")?.toFloatOrNull()?.let { 
                    if (it in 0.0..1.0) drowsinessThreshold = it 
                    else throw ConfigurationException.ValidationException("Drowsiness threshold must be between 0.0 and 1.0")
                }
            }
            
            val config = getCurrentConfiguration()
            Log.i(TAG, "Configuration loaded successfully: $config")
            Result.success(config)
            
        } catch (e: IOException) {
            Log.e(TAG, "IO error loading configuration", e)
            Result.failure(ConfigurationException.LoadException("IO error", e))
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Invalid number format in configuration", e)
            Result.failure(ConfigurationException.LoadException("Invalid number format", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading configuration", e)
            Result.failure(ConfigurationException.LoadException("Unexpected error", e))
        }
    }
    
    /**
     * Synchronously loads configuration (backward compatibility)
     */
    fun loadConfiguration(context: Context): Boolean {
        return try {
            kotlinx.coroutines.runBlocking {
                loadConfigurationAsync(context).isSuccess
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in synchronous load", e)
            false
        }
    }
    
    /**
     * Asynchronously saves configuration to persistent storage
     */
    suspend fun saveConfigurationAsync(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Saving camera configuration...")
            val configFile = getConfigFile(context)
            
            // Ensure parent directory exists
            configFile.parentFile?.let { parentDir ->
                if (!parentDir.exists() && !parentDir.mkdirs()) {
                    throw IOException("Failed to create configuration directory")
                }
            }
            
            val properties = Properties()
            configLock.read {
                properties.setProperty("far_detection", farDetectionEnabled.toString())
                properties.setProperty("object_detection", objectDetectionEnabled.toString())
                properties.setProperty("drowsiness_detection", drowsinessDetectionEnabled.toString())
                properties.setProperty("audio_enabled", audioEnabled.toString())
                properties.setProperty("model_version", modelVersion.toString())
                properties.setProperty("drowsiness_threshold", drowsinessThreshold.toString())
            }
            
            configFile.outputStream().use { outputStream ->
                properties.store(outputStream, "Camera Configuration - Generated on ${System.currentTimeMillis()}")
            }
            
            Log.i(TAG, "Configuration saved successfully")
            Result.success(Unit)
            
        } catch (e: IOException) {
            Log.e(TAG, "IO error saving configuration", e)
            Result.failure(ConfigurationException.SaveException("IO error", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error saving configuration", e)
            Result.failure(ConfigurationException.SaveException("Unexpected error", e))
        }
    }
    
    // Thread-safe property accessors with validation
    
    fun isFarDetectionEnabled(): Boolean = configLock.read { farDetectionEnabled }
    
    suspend fun setFarDetectionEnabled(context: Context, enabled: Boolean): Result<Unit> {
        configLock.write { farDetectionEnabled = enabled }
        return saveConfigurationAsync(context)
    }
    
    fun isObjectDetectionEnabled(): Boolean = configLock.read { objectDetectionEnabled }
    
    suspend fun setObjectDetectionEnabled(context: Context, enabled: Boolean): Result<Unit> {
        configLock.write { objectDetectionEnabled = enabled }
        return saveConfigurationAsync(context)
    }
    
    fun isDrowsinessDetectionEnabled(): Boolean = configLock.read { drowsinessDetectionEnabled }
    
    suspend fun setDrowsinessDetectionEnabled(context: Context, enabled: Boolean): Result<Unit> {
        configLock.write { drowsinessDetectionEnabled = enabled }
        return saveConfigurationAsync(context)
    }
    
    fun isAudioEnabled(): Boolean = configLock.read { audioEnabled }
    
    suspend fun setAudioEnabled(context: Context, enabled: Boolean): Result<Unit> {
        configLock.write { audioEnabled = enabled }
        return saveConfigurationAsync(context)
    }
    
    fun getModelVersion(): Int = configLock.read { modelVersion }
    
    suspend fun setModelVersion(context: Context, version: Int): Result<Unit> {
        if (version < 0) {
            return Result.failure(ConfigurationException.ValidationException("Model version cannot be negative"))
        }
        configLock.write { modelVersion = version }
        return saveConfigurationAsync(context)
    }
    
    fun getDrowsinessThreshold(): Float = configLock.read { drowsinessThreshold }
    
    suspend fun setDrowsinessThreshold(context: Context, threshold: Float): Result<Unit> {
        if (threshold !in 0.0..1.0) {
            return Result.failure(ConfigurationException.ValidationException("Drowsiness threshold must be between 0.0 and 1.0"))
        }
        configLock.write { drowsinessThreshold = threshold }
        return saveConfigurationAsync(context)
    }
    
    /**
     * Gets current configuration as immutable data class
     */
    fun getCurrentConfiguration(): CameraConfig = configLock.read {
        CameraConfig(
            farDetectionEnabled = farDetectionEnabled,
            objectDetectionEnabled = objectDetectionEnabled,
            drowsinessDetectionEnabled = drowsinessDetectionEnabled,
            audioEnabled = audioEnabled,
            modelVersion = modelVersion,
            drowsinessThreshold = drowsinessThreshold
        )
    }
    
    /**
     * Updates configuration in batch operation
     */
    suspend fun updateConfiguration(context: Context, config: CameraConfig): Result<Unit> {
        // Validate configuration
        if (config.drowsinessThreshold !in 0.0..1.0) {
            return Result.failure(ConfigurationException.ValidationException("Invalid drowsiness threshold"))
        }
        if (config.modelVersion < 0) {
            return Result.failure(ConfigurationException.ValidationException("Invalid model version"))
        }
        
        configLock.write {
            farDetectionEnabled = config.farDetectionEnabled
            objectDetectionEnabled = config.objectDetectionEnabled
            drowsinessDetectionEnabled = config.drowsinessDetectionEnabled
            audioEnabled = config.audioEnabled
            modelVersion = config.modelVersion
            drowsinessThreshold = config.drowsinessThreshold
        }
        
        return saveConfigurationAsync(context)
    }
    
    /**
     * Resets configuration to default values
     */
    suspend fun resetToDefaults(context: Context): Result<Unit> {
        configLock.write {
            farDetectionEnabled = false
            objectDetectionEnabled = true
            drowsinessDetectionEnabled = false
            audioEnabled = false
            modelVersion = 0
            drowsinessThreshold = 0.8f
        }
        return saveConfigurationAsync(context)
    }
    
    private fun getConfigFile(context: Context): File {
        val configDir = context.getExternalFilesDir(null) ?: context.filesDir
        return File(configDir, CONFIG_FILE_NAME)
    }
    
    // Backward compatibility methods (deprecated)
    @Deprecated("Use isFarDetectionEnabled() instead", ReplaceWith("isFarDetectionEnabled()"))
    fun isFAR(): Boolean = isFarDetectionEnabled()
    
    @Deprecated("Use isObjectDetectionEnabled() instead", ReplaceWith("isObjectDetectionEnabled()"))
    fun isOD(): Boolean = isObjectDetectionEnabled()
    
    @Deprecated("Use isDrowsinessDetectionEnabled() instead", ReplaceWith("isDrowsinessDetectionEnabled()"))
    fun isDS(): Boolean = isDrowsinessDetectionEnabled()
    
    @Deprecated("Use isAudioEnabled() instead", ReplaceWith("isAudioEnabled()"))
    fun isAUDIO(): Boolean = isAudioEnabled()
    
    @Deprecated("Use getModelVersion() instead", ReplaceWith("getModelVersion()"))
    fun getMODEL(): Int = getModelVersion()
    
    @Deprecated("Use getDrowsinessThreshold() instead", ReplaceWith("getDrowsinessThreshold()"))
    fun getDsThreshold(): Float = getDrowsinessThreshold()
} 