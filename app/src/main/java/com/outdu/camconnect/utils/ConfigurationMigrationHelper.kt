package com.outdu.camconnect.utils

import android.content.Context
import android.util.Log
import com.outdu.camconnect.communication.CameraConfigurationManager
import java.io.File
import java.io.FileInputStream
import java.util.Properties

/**
 * Helper utility to migrate configuration from Data.java format to CameraConfigurationManager format
 */
object ConfigurationMigrationHelper {
    
    private const val TAG = "ConfigMigration"
    private const val OLD_CONFIG_FILE = "config.properties"
    
    /**
     * Migrates old Data.java format configuration to CameraConfigurationManager
     * This should be called once during app startup
     */
    suspend fun migrateIfNeeded(context: Context): Result<Unit> {
        return try {
            val oldConfigFile = File(context.getExternalFilesDir(null), OLD_CONFIG_FILE)
            
            if (!oldConfigFile.exists()) {
                Log.d(TAG, "No old configuration file found, skipping migration")
                return Result.success(Unit)
            }
            
            // Check if new configuration already exists
            val currentConfig = CameraConfigurationManager.getCurrentConfiguration()
            if (hasNonDefaultValues(currentConfig)) {
                Log.d(TAG, "New configuration already exists, skipping migration")
                return Result.success(Unit)
            }
            
            // Read old configuration
            val properties = Properties()
            FileInputStream(oldConfigFile).use { inputStream ->
                properties.load(inputStream)
            }
            
            // Create new configuration from old properties
            val migratedConfig = CameraConfigurationManager.CameraConfig(
                farDetectionEnabled = properties.getProperty("far")?.toBoolean() ?: false,
                objectDetectionEnabled = properties.getProperty("od")?.toBoolean() ?: false,
                depthSensingEnabled = properties.getProperty("ds")?.toBoolean() ?: false,
                audioEnabled = properties.getProperty("audio")?.toBoolean() ?: false,
                modelVersion = properties.getProperty("model")?.toIntOrNull() ?: 1,
                depthSensingThreshold = properties.getProperty("ds_threshold")?.toFloatOrNull() ?: 0.5f
            )
            
            // Save migrated configuration
            val result = CameraConfigurationManager.updateConfiguration(context, migratedConfig)
            
            if (result.isSuccess) {
                Log.i(TAG, "Configuration migrated successfully: $migratedConfig")
                // Optionally rename or delete old config file
                val backupFile = File(context.getExternalFilesDir(null), "${OLD_CONFIG_FILE}.backup")
                oldConfigFile.renameTo(backupFile)
                Log.d(TAG, "Old configuration backed up to ${backupFile.name}")
            } else {
                Log.e(TAG, "Failed to save migrated configuration")
            }
            
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during migration", e)
            Result.failure(e)
        }
    }
    
    /**
     * Checks if configuration has non-default values (indicating it was already set up)
     */
    private fun hasNonDefaultValues(config: CameraConfigurationManager.CameraConfig): Boolean {
        return config.farDetectionEnabled || 
               config.objectDetectionEnabled || 
               config.depthSensingEnabled || 
               config.audioEnabled || 
               config.modelVersion != 0 ||
               config.depthSensingThreshold != 0.8f
    }
} 