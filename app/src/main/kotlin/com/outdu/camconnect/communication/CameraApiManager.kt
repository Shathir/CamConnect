package com.outdu.camconnect.communication

import android.util.Log
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.FileReader
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.seconds

/**
 * camera API manager that provides high-level camera control operations
 * with connection management, device discovery, and robust error handling.
 */
class CameraApiManager private constructor() {
    
    companion object {
        private const val TAG = "CameraApiManager"
        private const val MAX_BYTES = 255
        private const val DEFAULT_DEVICE_IP = "192.168.2.1"
        const val CAMERA_CLIENT_SOCKET_PORT = 9000
        const val CAMERA_SERVER_SOCKET_PORT = 9002
        
        // Singleton instance management
        @Volatile private var instance: CameraApiManager? = null
        private val instanceLock = Any()
        
        /**
         * Gets the singleton instance of CameraApiManager
         */
        fun getInstance(): CameraApiManager {
            return instance ?: synchronized(instanceLock) {
                instance ?: CameraApiManager().also { instance = it }
            }
        }
        
        /**
         * Asynchronously discovers camera devices on the network
         */
        suspend fun discoverDevices(): Result<List<String>> = withContext(Dispatchers.IO) {
            Log.i(TAG, "Starting device discovery...")
            
            try {
                val discoveredDevices = mutableListOf<String>()
                val arpFile = "/proc/net/arp"
                
                if (!java.io.File(arpFile).exists()) {
                    Log.w(TAG, "ARP file not found, cannot discover devices")
                    return@withContext Result.success(listOf(DEFAULT_DEVICE_IP))
                }
                
                BufferedReader(FileReader(arpFile)).use { reader ->
                    reader.readLines().forEach { line ->
                        if (line.isNotBlank() && !line.startsWith("IP address")) {
                            val parts = line.split(Regex("\\s+"))
                            if (parts.size >= 4) {
                                val ipAddress = parts[0].trim()
                                val macAddress = parts[3].trim()
                                
                                // Skip invalid entries
                                if (ipAddress != "IP" && macAddress != "00:00:00:00:00:00") {
                                    discoveredDevices.add(ipAddress)
                                    Log.d(TAG, "Found potential device: $ipAddress (MAC: $macAddress)")
                                }
                            }
                        }
                    }
                }
                
                // Test connectivity to discovered devices
                val reachableDevices = mutableListOf<String>()
                val socketClient = CameraSocketClient()
                
                try {
                    for (ipAddress in discoveredDevices) {
                        if (socketClient.isDeviceReachable(ipAddress, CAMERA_CLIENT_SOCKET_PORT)) {
                            reachableDevices.add(ipAddress)
                            Log.i(TAG, "Camera device confirmed at: $ipAddress")
                        }
                    }
                } finally {
                    socketClient.close()
                }
                
                if (reachableDevices.isEmpty()) {
                    Log.w(TAG, "No camera devices discovered, using default IP")
                    reachableDevices.add(DEFAULT_DEVICE_IP)
                }
                
                Log.i(TAG, "Device discovery completed. Found ${reachableDevices.size} devices")
                return@withContext Result.success(reachableDevices)
                
            } catch (e: Exception) {
                Log.e(TAG, "Device discovery failed", e)
                return@withContext Result.failure(DeviceException.DiscoveryException("Device discovery failed", e))
            }
        }
    }
    
    /**
     * Custom exceptions for camera API operations
     */
    sealed class DeviceException(message: String, cause: Throwable? = null) : Exception(message, cause) {
        class DiscoveryException(message: String, cause: Throwable? = null) : DeviceException("Discovery failed: $message", cause)
        class ConnectionException(message: String, cause: Throwable? = null) : DeviceException("Connection failed: $message", cause)
        class CommandException(message: String, cause: Throwable? = null) : DeviceException("Command failed: $message", cause)
        class ConfigurationException(message: String) : DeviceException("Configuration error: $message")
    }
    
    /**
     * Device connection info data class
     */
    data class DeviceInfo(
        val ipAddress: String,
        val isConnected: Boolean,
        val lastConnected: Long,
        val connectionAttempts: Int
    )
    
    // Connection management
    private val currentDeviceIP = AtomicReference(DEFAULT_DEVICE_IP)
    private val connectionPool = ConcurrentHashMap<String, CameraSocketClient>()
    private val deviceInfoMap = ConcurrentHashMap<String, DeviceInfo>()
    private val apiScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Sets the target device IP address
     */
    fun setDeviceIpAddress(ipAddress: String): Result<Unit> {
        return if (ipAddress.isNotBlank()) {
            currentDeviceIP.set(ipAddress)
            Log.i(TAG, "Device IP updated to: $ipAddress")
            Result.success(Unit)
        } else {
            Result.failure(DeviceException.ConfigurationException("Invalid IP address"))
        }
    }
    
    /**
     * Gets the current device IP address
     */
    fun getCurrentDeviceIP(): String = currentDeviceIP.get()
    
    /**
     * Gets device information for all known devices
     */
    fun getDeviceInfo(): Map<String, DeviceInfo> = deviceInfoMap.toMap()
    
    /**
     * Establishes connection to current device
     */
    suspend fun connectToDevice(ipAddress: String = getCurrentDeviceIP()): Result<Unit> {
        return try {
            val client = getOrCreateClient(ipAddress)
            val connectResult = client.connect(ipAddress, CAMERA_CLIENT_SOCKET_PORT)
            
            if (connectResult.isSuccess) {
                updateDeviceInfo(ipAddress, true, 0)
                Log.i(TAG, "Successfully connected to device: $ipAddress")
            } else {
                updateDeviceInfo(ipAddress, false, 1)
                Log.w(TAG, "Failed to connect to device: $ipAddress")
            }
            
            connectResult
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to device: $ipAddress", e)
            Result.failure(DeviceException.ConnectionException("Connection error", e))
        }
    }
    
    /**
     * Disconnects from specified device
     */
    suspend fun disconnectFromDevice(ipAddress: String = getCurrentDeviceIP()): Result<Unit> {
        return try {
            connectionPool[ipAddress]?.let { client ->
                val result = client.disconnect()
                connectionPool.remove(ipAddress)
                updateDeviceInfo(ipAddress, false, 0)
                result
            } ?: Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting from device: $ipAddress", e)
            Result.failure(DeviceException.ConnectionException("Disconnect error", e))
        }
    }
    
    /**
     * Executes a command with automatic connection management
     */
    private suspend fun executeCommand(
        ipAddress: String = getCurrentDeviceIP(),
        command: suspend (CameraSocketClient) -> Result<Int>
    ): Result<Int> {
        return try {
            val client = getOrCreateClient(ipAddress)
            
            // Ensure connection
            if (!client.isConnected()) {
                val connectResult = connectToDevice(ipAddress)
                if (connectResult.isFailure) {
                    return Result.failure(connectResult.exceptionOrNull() ?: Exception("Connection failed"))
                }
            }
            
            // Execute command
            return command(client)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error executing command", e)
            Result.failure(DeviceException.CommandException("Command execution failed", e))
        }
    }
    
    // Configuration retrieval methods
    
    suspend fun getConfiguration(type: String): Result<Map<String, Any>> {
        Log.i(TAG, "Getting configuration: $type")
        
        val commandResult = executeCommand { client ->
            val command = when (type) {
                "Factory" -> CameraCommandProtocol.getFactoryConfigCmd()
                "Default" -> CameraCommandProtocol.getDefaultConfigCmd()
                "Current" -> CameraCommandProtocol.getCurrentConfigCmd()
                else -> return@executeCommand Result.failure(
                    DeviceException.ConfigurationException("Invalid configuration type: $type")
                )
            }
            
            val response = IntArray(MAX_BYTES)
            val result = client.sendCommand(command, response)
            
            if (result.isSuccess) {
                val bytesReceived = result.getOrThrow()
                Result.success(bytesReceived)
            } else {
                result
            }
        }
        
        return if (commandResult.isSuccess) {
            // Parse the configuration from the response
            try {
                val bytesReceived = commandResult.getOrThrow()
                val response = IntArray(MAX_BYTES) // This should be filled from the actual response
                val config = when (type) {
                    "Factory" -> CameraCommandProtocol.getFactoryConfigCmdResponseParse(response, bytesReceived)
                    "Default" -> CameraCommandProtocol.getDefaultConfigCmdResponseParse(response, bytesReceived)
                    "Current" -> CameraCommandProtocol.getCurrentConfigCmdResponseParse(response, bytesReceived)
                    else -> emptyMap()
                }
                Result.success(config)
            } catch (e: Exception) {
                Result.failure(DeviceException.CommandException("Failed to parse configuration", e))
            }
        } else {
            Result.failure(commandResult.exceptionOrNull() ?: Exception("Command execution failed"))
        }
    }
    
    // Image control methods
    
    suspend fun setIrBrightness(brightness: Int): Result<Boolean> {
        Log.i(TAG, "Setting IR brightness: $brightness")
        
        if (brightness !in 0..255) {
            return Result.failure(DeviceException.ConfigurationException("Brightness must be between 0 and 255"))
        }
        
        return executeCommand { client ->
            val command = CameraCommandProtocol.setImgIRBrightnessCmd(brightness)
            val response = IntArray(MAX_BYTES)
            val result = client.sendCommand(command, response)
            
            if (result.isSuccess) {
                val success = CameraCommandProtocol.setImgIRBrightnessCmdResponseParse(response, result.getOrThrow())
                Result.success(if (success) 1 else 0)
            } else {
                result
            }
        }.map { it > 0 }
    }
    
    suspend fun setImageZoom(zoom: CameraCommandProtocol.ZOOM): Result<Boolean> {
        Log.i(TAG, "Setting image zoom: ${zoom.getDisplayVal()}")
        
        return executeCommand { client ->
            val command = CameraCommandProtocol.setImgZoomCmd(zoom.getDisplayVal())
            val response = IntArray(MAX_BYTES)
            val result = client.sendCommand(command, response)
            
            if (result.isSuccess) {
                val success = CameraCommandProtocol.setImgZoomCmdResponseParse(response, result.getOrThrow())
                Result.success(if (success) 1 else 0)
            } else {
                result
            }
        }.map { it > 0 }
    }
    
    suspend fun setImageResolution(resolution: CameraCommandProtocol.RESOLUTION): Result<Boolean> {
        Log.i(TAG, "Setting image resolution: ${resolution.getDisplayVal()}")
        
        return executeCommand { client ->
            val command = CameraCommandProtocol.setImgResolutionCmd(resolution.getDisplayVal())
            val response = IntArray(MAX_BYTES)
            val result = client.sendCommand(command, response)
            
            if (result.isSuccess) {
                val success = CameraCommandProtocol.setImgResolutionCmdResponseParse(response, result.getOrThrow())
                Result.success(if (success) 1 else 0)
            } else {
                result
            }
        }.map { it > 0 }
    }
    
    suspend fun setImageRotation(rotation: CameraCommandProtocol.ROTATION): Result<Boolean> {
        Log.i(TAG, "Setting image rotation: ${rotation.getDisplayVal()}")
        
        return executeCommand { client ->
            val command = CameraCommandProtocol.setImgRotationCmd(rotation.getDisplayVal())
            val response = IntArray(MAX_BYTES)
            val result = client.sendCommand(command, response)
            
            if (result.isSuccess) {
                val success = CameraCommandProtocol.setImgRotationCmdResponseParse(response, result.getOrThrow())
                Result.success(if (success) 1 else 0)
            } else {
                result
            }
        }.map { it > 0 }
    }
    
    // Stream control methods
    
    suspend fun startStream(): Result<Boolean> {
        Log.i(TAG, "Starting camera stream")
        
        return executeCommand { client ->
            val command = CameraCommandProtocol.startStreamCmd()
            val response = IntArray(MAX_BYTES)
            val result = client.sendCommand(command, response)
            
            if (result.isSuccess) {
                val success = CameraCommandProtocol.startStreamCmdResponseParse(response, result.getOrThrow())
                Result.success(if (success) 1 else 0)
            } else {
                result
            }
        }.map { it > 0 }
    }
    
    suspend fun stopStream(): Result<Boolean> {
        Log.i(TAG, "Stopping camera stream")
        
        return executeCommand { client ->
            val command = CameraCommandProtocol.stopStreamCmd()
            val response = IntArray(MAX_BYTES)
            val result = client.sendCommand(command, response)
            
            if (result.isSuccess) {
                val success = CameraCommandProtocol.stopStreamCmdResponseParse(response, result.getOrThrow())
                Result.success(if (success) 1 else 0)
            } else {
                result
            }
        }.map { it > 0 }
    }
    
    // File upload methods
    
    suspend fun uploadFile(fileName: String, inputStream: InputStream): Result<Unit> {
        Log.i(TAG, "Uploading file: $fileName")
        
        if (getCurrentDeviceIP().isEmpty()) {
            return Result.failure(DeviceException.ConfigurationException("No device IP configured"))
        }
        
        return try {
            val uploader = CameraFileUploader.create(
                host = getCurrentDeviceIP(),
                username = "root",
                password = "ota"
            ).getOrThrow()
            
            uploader.use { 
                it.uploadFile(inputStream, fileName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "File upload failed", e)
            Result.failure(DeviceException.CommandException("File upload failed", e))
        }
    }
    
    // System control methods
    
    suspend fun shutdownCamera(): Result<Boolean> {
        Log.i(TAG, "Shutting down camera")
        
        return executeCommand { client ->
            val command = CameraCommandProtocol.shutdownCmd()
            val response = IntArray(MAX_BYTES)
            val result = client.sendCommand(command, response)
            
            if (result.isSuccess) {
                val success = CameraCommandProtocol.shutdownCmdResponseParse(response, result.getOrThrow())
                Result.success(if (success) 1 else 0)
            } else {
                result
            }
        }.map { it > 0 }
    }
    
    // WiFi management methods
    
    suspend fun getWifiState(): Result<CameraCommandProtocol.WifiState> {
        Log.i(TAG, "Getting WiFi state")
        
        return try {
            val client = getOrCreateClient(getCurrentDeviceIP())
            
            // Ensure connection
            if (!client.isConnected()) {
                val connectResult = connectToDevice(getCurrentDeviceIP())
                if (connectResult.isFailure) {
                    return Result.failure(connectResult.exceptionOrNull() ?: Exception("Connection failed"))
                }
            }
            
            val command = CameraCommandProtocol.getWifiStateCmd()
            val response = IntArray(MAX_BYTES)
            val result = client.sendCommand(command, response)
            
            if (result.isSuccess) {
                val bytesReceived = result.getOrThrow()
                val wifiState = CameraCommandProtocol.getWifiStateCmdResponseParse(response, bytesReceived)
                Result.success(wifiState)
            } else {
                Result.failure(Exception("Failed to get WiFi state: ${result.exceptionOrNull()?.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "WiFi state query failed", e)
            Result.failure(DeviceException.CommandException("WiFi state query failed", e))
        }
    }
    
    // Health check and diagnostics
    
    suspend fun performHealthCheck(): Result<Map<String, Any>> {
        Log.i(TAG, "Performing health check")
        
        val healthInfo = mutableMapOf<String, Any>()
        
        return try {
            // Check device connectivity
            val isReachable = connectionPool[getCurrentDeviceIP()]?.isDeviceReachable(
                getCurrentDeviceIP(), 
                CAMERA_CLIENT_SOCKET_PORT
            ) ?: false
            
            healthInfo["device_reachable"] = isReachable
            healthInfo["device_ip"] = getCurrentDeviceIP()
            healthInfo["timestamp"] = System.currentTimeMillis()
            
            if (isReachable) {
                // Try to get basic configuration
                val configResult = getConfiguration("Current")
                healthInfo["config_accessible"] = configResult.isSuccess
                if (configResult.isSuccess) {
                    healthInfo["config_properties"] = configResult.getOrThrow().size
                }
            }
            
            healthInfo["connection_pool_size"] = connectionPool.size
            healthInfo["known_devices"] = deviceInfoMap.size
            
            Result.success(healthInfo)
            
        } catch (e: Exception) {
            Log.e(TAG, "Health check failed", e)
            healthInfo["error"] = e.message ?: "Unknown error"
            Result.failure(DeviceException.CommandException("Health check failed", e))
        }
    }
    
    // Resource management
    
    suspend fun cleanup(): Unit {
        Log.i(TAG, "Cleaning up resources...")
        
        // Disconnect all clients
        connectionPool.values.forEach { client ->
            try {
                client.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "Error disconnecting client during cleanup", e)
            }
        }
        connectionPool.clear()
        deviceInfoMap.clear()
        
        // Cancel scope
        apiScope.cancel()
        
        Log.i(TAG, "Cleanup completed")
        return
    }
    
    private fun getOrCreateClient(ipAddress: String): CameraSocketClient {
        return connectionPool.getOrPut(ipAddress) {
            CameraSocketClient()
        }
    }
    
    private fun updateDeviceInfo(ipAddress: String, connected: Boolean, attemptsDelta: Int) {
        deviceInfoMap.compute(ipAddress) { _, existing ->
            val current = existing ?: DeviceInfo(ipAddress, false, 0, 0)
            current.copy(
                isConnected = connected,
                lastConnected = if (connected) System.currentTimeMillis() else current.lastConnected,
                connectionAttempts = current.connectionAttempts + attemptsDelta
            )
        }
    }
} 