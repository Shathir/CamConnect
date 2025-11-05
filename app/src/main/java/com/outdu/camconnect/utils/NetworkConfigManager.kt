package com.outdu.camconnect.utils

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
 * Manages network configuration defaults loaded from .env-like properties file.
 * 
 * This follows the same pattern as CameraConfigurationManager, allowing
 * configuration to be loaded from a properties file (similar to .env files).
 * 
 * Configuration can be loaded from:
 * 1. Assets folder (packaged with app) - for defaults
 * 2. External storage (runtime) - for user/custom configurations
 */
object NetworkConfigManager {
    
    private const val TAG = "NetworkConfigManager"
    private const val CONFIG_FILE_NAME = "network_config.properties"
    private const val ASSETS_CONFIG_FILE = "network_config.properties"
    
    // Default values (fallback if config file doesn't exist)
    private const val DEFAULT_SUBNET_MASK = "255.255.255.0"
    private const val DEFAULT_HOTSPOT_IP = "192.168.2.1"
    private const val DEFAULT_WIFI_IP = "192.168.1.100"
    
    // Cached configuration
    @Volatile private var subnetMask: String = DEFAULT_SUBNET_MASK
    @Volatile private var hotspotIp: String = DEFAULT_HOTSPOT_IP
    @Volatile private var wifiIp: String = DEFAULT_WIFI_IP
    
    // Thread-safe read-write lock
    private val configLock = ReentrantReadWriteLock()
    
    /**
     * Network configuration data class
     */
    data class NetworkConfig(
        val subnetMask: String,
        val hotspotIp: String,
        val wifiIp: String
    )
    
    /**
     * Loads configuration from properties file.
     * First tries external storage, then falls back to assets, then defaults.
     */
    suspend fun loadConfiguration(context: Context): Result<NetworkConfig> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Loading network configuration...")
            
            // Try to load from external storage first (user config)
            val externalFile = getConfigFile(context)
            if (externalFile.exists()) {
                Log.d(TAG, "Loading from external storage: ${externalFile.absolutePath}")
                loadFromFile(externalFile)
                return@withContext Result.success(getCurrentConfig())
            }
            
            // Try to load from assets (default config packaged with app)
            try {
                context.assets.open(ASSETS_CONFIG_FILE).use { inputStream ->
                    val properties = Properties()
                    properties.load(inputStream)
                    loadFromProperties(properties)
                    Log.d(TAG, "Loaded from assets")
                    return@withContext Result.success(getCurrentConfig())
                }
            } catch (e: IOException) {
                Log.d(TAG, "Assets config not found, using defaults")
            }
            
            // Use defaults
            resetToDefaults()
            Result.success(getCurrentConfig())
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading network configuration", e)
            resetToDefaults()
            Result.failure(e)
        }
    }
    
    /**
     * Saves current configuration to external storage
     */
    suspend fun saveConfiguration(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val configFile = getConfigFile(context)
            val properties = Properties()
            
            configLock.read {
                properties.setProperty("subnet_mask", subnetMask)
                properties.setProperty("hotspot_ip", hotspotIp)
                properties.setProperty("wifi_ip", wifiIp)
            }
            
            configFile.parentFile?.mkdirs()
            configFile.outputStream().use { outputStream ->
                properties.store(outputStream, "Network Configuration")
            }
            
            Log.d(TAG, "Configuration saved to ${configFile.absolutePath}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving network configuration", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets current configuration
     */
    fun getCurrentConfig(): NetworkConfig = configLock.read {
        NetworkConfig(
            subnetMask = subnetMask,
            hotspotIp = hotspotIp,
            wifiIp = wifiIp
        )
    }
    
    /**
     * Gets default subnet mask
     */
    fun getSubnetMask(): String = configLock.read { subnetMask }
    
    /**
     * Gets default hotspot IP
     */
    fun getHotspotIp(): String = configLock.read { hotspotIp }
    
    /**
     * Gets default WiFi IP
     */
    fun getWifiIp(): String = configLock.read { wifiIp }
    
    /**
     * Updates subnet mask
     */
    fun setSubnetMask(value: String) {
        configLock.write {
            subnetMask = value
        }
    }
    
    /**
     * Updates hotspot IP
     */
    fun setHotspotIp(value: String) {
        configLock.write {
            hotspotIp = value
        }
    }
    
    /**
     * Updates WiFi IP
     */
    fun setWifiIp(value: String) {
        configLock.write {
            wifiIp = value
        }
    }
    
    /**
     * Resets to default values
     */
    fun resetToDefaults() {
        configLock.write {
            subnetMask = DEFAULT_SUBNET_MASK
            hotspotIp = DEFAULT_HOTSPOT_IP
            wifiIp = DEFAULT_WIFI_IP
        }
    }
    
    private fun loadFromFile(file: File) {
        FileInputStream(file).use { inputStream ->
            val properties = Properties()
            properties.load(inputStream)
            loadFromProperties(properties)
        }
    }
    
    private fun loadFromProperties(properties: Properties) {
        configLock.write {
            subnetMask = properties.getProperty("subnet_mask", DEFAULT_SUBNET_MASK)
            hotspotIp = properties.getProperty("hotspot_ip", DEFAULT_HOTSPOT_IP)
            wifiIp = properties.getProperty("wifi_ip", DEFAULT_WIFI_IP)
        }
    }
    
    private fun getConfigFile(context: Context): File {
        val configDir = context.getExternalFilesDir(null) ?: context.filesDir
        return File(configDir, CONFIG_FILE_NAME)
    }
}


