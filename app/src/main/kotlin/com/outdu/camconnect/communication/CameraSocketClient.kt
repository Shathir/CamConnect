package com.outdu.camconnect.communication

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Thread-safe camera socket client for command communication with enhanced error handling,
 * connection management, and automatic retry mechanisms.
 */
class CameraSocketClient {
    
    companion object {
        private const val TAG = "CameraSocketClient"
        private const val DEFAULT_SOCKET_TIMEOUT = 10_000 // 10 seconds
        private const val DEFAULT_COMMAND_TIMEOUT = 15_000L // 15 seconds
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
        
        /**
         * Calculates CRC checksum for command validation
         */
        private fun calculateCRC(data: ByteArray): Byte {
            var sum = 0
            for (i in 0 until data.size - 1) {
                sum += data[i].toInt() and 0xFF
            }
            val crcByte = sum xor 0xFF
            return (crcByte + 1).toByte()
        }
        
        /**
         * Validates CRC checksum in response data
         */
        private fun validateCRC(data: ByteArray, length: Int): Boolean {
            var sum = 0
            for (i in 0 until length) {
                sum += data[i].toInt() and 0xFF
            }
            return (sum and 0xFF) == 0
        }
    }
    
    /**
     * Custom exceptions for socket operations
     */
    sealed class SocketException(message: String, cause: Throwable? = null) : Exception(message, cause) {
        class ConnectionException(message: String, cause: Throwable? = null) : SocketException("Connection failed: $message", cause)
        class TimeoutException(message: String) : SocketException("Operation timed out: $message")
        class ProtocolException(message: String) : SocketException("Protocol error: $message")
        class CrcException(message: String) : SocketException("CRC validation failed: $message")
        class InvalidResponseException(message: String) : SocketException("Invalid response: $message")
    }
    
    /**
     * Connection state enumeration
     */
    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, ERROR
    }
    
    // Connection management
    @Volatile private var socket: Socket? = null
    @Volatile private var outputStream: DataOutputStream? = null
    @Volatile private var inputStream: DataInputStream? = null
    @Volatile private var connectionState = ConnectionState.DISCONNECTED
    private val isConnected = AtomicBoolean(false)
    
    // Configuration
    private var socketTimeout = DEFAULT_SOCKET_TIMEOUT
    private var commandTimeout = DEFAULT_COMMAND_TIMEOUT
    
    /**
     * Sets custom timeouts for socket operations
     */
    fun setTimeouts(socketTimeoutMs: Int, commandTimeoutMs: Long) {
        this.socketTimeout = socketTimeoutMs
        this.commandTimeout = commandTimeoutMs
    }
    
    /**
     * Checks if device is reachable at given IP address
     */
    suspend fun isDeviceReachable(ipAddress: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Checking device reachability at $ipAddress:$port")
        
        val testSocket = Socket()
        try {
            withTimeout(socketTimeout.toLong()) {
                testSocket.connect(InetSocketAddress(ipAddress, port), socketTimeout)
            }
            Log.d(TAG, "Device is reachable at $ipAddress:$port")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Device not reachable at $ipAddress:$port - ${e.message}")
            false
        } finally {
            try {
                testSocket.close()
            } catch (e: IOException) {
                Log.w(TAG, "Error closing test socket", e)
            }
        }
    }
    
    /**
     * Establishes connection to camera device with retry mechanism
     */
    suspend fun connect(ipAddress: String, port: Int): Result<Unit> = withContext(Dispatchers.IO) {
        if (isConnected.get()) {
            Log.d(TAG, "Already connected")
            return@withContext Result.success(Unit)
        }
        
        Log.i(TAG, "Connecting to camera at $ipAddress:$port")
        connectionState = ConnectionState.CONNECTING
        
        var lastException: Exception? = null
        
        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            try {
                withTimeout(socketTimeout.toLong()) {
                    socket = Socket().apply {
                        soTimeout = socketTimeout
                        tcpNoDelay = true
                        keepAlive = true
                        connect(InetSocketAddress(ipAddress, port), socketTimeout)
                    }
                    
                    socket?.let { s ->
                        outputStream = DataOutputStream(s.getOutputStream())
                        inputStream = DataInputStream(s.getInputStream())
                        isConnected.set(true)
                        connectionState = ConnectionState.CONNECTED
                        Log.i(TAG, "Successfully connected to camera")
                        return@let Result.success(Unit)
                    }
                }
            } catch (e: SocketTimeoutException) {
                lastException = SocketException.TimeoutException("Connection timeout on attempt ${attempt + 1}")
                Log.w(TAG, "Connection attempt ${attempt + 1} timed out")
            } catch (e: IOException) {
                lastException = SocketException.ConnectionException("IO error on attempt ${attempt + 1}", e)
                Log.w(TAG, "Connection attempt ${attempt + 1} failed with IO error: ${e.message}")
            } catch (e: Exception) {
                lastException = SocketException.ConnectionException("Unexpected error on attempt ${attempt + 1}", e)
                Log.w(TAG, "Connection attempt ${attempt + 1} failed unexpectedly: ${e.message}")
            }
            
            // Cleanup failed connection
            cleanupConnection()
            
            // Wait before retry (except for last attempt)
            if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                kotlinx.coroutines.delay(RETRY_DELAY_MS)
            }
        }
        
        connectionState = ConnectionState.ERROR
        val finalException = lastException ?: SocketException.ConnectionException("Unknown connection error")
        Log.e(TAG, "Failed to connect after $MAX_RETRY_ATTEMPTS attempts", finalException)
        Result.failure(finalException)
    }
    
    /**
     * Sends command to camera and receives response with timeout and validation
     */
    suspend fun sendCommand(request: IntArray, response: IntArray): Result<Int> = withContext(Dispatchers.IO) {
        if (!isConnected.get()) {
            return@withContext Result.failure(SocketException.ConnectionException("Not connected to camera"))
        }
        
        if (request.isEmpty() || response.isEmpty()) {
            return@withContext Result.failure(SocketException.ProtocolException("Invalid command or response array"))
        }
        
        try {
            withTimeout(commandTimeout) {
                // Convert and prepare request
                val requestBytes = request.map { it.toByte() }.toByteArray()
                requestBytes[requestBytes.size - 1] = calculateCRC(requestBytes)
                
                // Send command
                outputStream?.write(requestBytes) ?: throw IOException("Output stream is null")
                outputStream?.flush()
                
                Log.d(TAG, "Sent command: ${requestBytes.joinToString { "%02X".format(it) }}")
                
                // Read response
                val responseBytes = ByteArray(response.size)
                val bytesRead = inputStream?.read(responseBytes) ?: -1
                
                if (bytesRead <= 0) {
                    throw SocketException.InvalidResponseException("No response received or connection closed")
                }
                
                Log.d(TAG, "Received response ($bytesRead bytes): ${responseBytes.take(bytesRead).joinToString { "%02X".format(it) }}")
                
                // Validate CRC
                if (!validateCRC(responseBytes, bytesRead)) {
                    throw SocketException.CrcException("Response CRC validation failed")
                }
                
                // Convert response back to int array
                for (i in 0 until minOf(bytesRead, response.size)) {
                    response[i] = responseBytes[i].toInt() and 0xFF
                }
                
                Log.d(TAG, "Command executed successfully")
                Result.success(bytesRead)
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "Command timeout")
            // Mark connection as potentially broken
            connectionState = ConnectionState.ERROR
            Result.failure(SocketException.TimeoutException("Command execution timeout"))
        } catch (e: SocketException) {
            Log.e(TAG, "Socket exception during command execution", e)
            connectionState = ConnectionState.ERROR
            Result.failure(e)
        } catch (e: IOException) {
            Log.e(TAG, "IO error during command execution", e)
            connectionState = ConnectionState.ERROR
            Result.failure(SocketException.ConnectionException("IO error during command", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during command execution", e)
            connectionState = ConnectionState.ERROR
            Result.failure(SocketException.ConnectionException("Unexpected error during command", e))
        }
    }
    
    /**
     * Gracefully disconnects from camera
     */
    suspend fun disconnect(): Result<Unit> = withContext(Dispatchers.IO) {
        Log.i(TAG, "Disconnecting from camera")
        
        try {
            cleanupConnection()
            Log.i(TAG, "Successfully disconnected")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Error during disconnect", e)
            Result.failure(SocketException.ConnectionException("Error during disconnect", e))
        }
    }
    
    /**
     * Gets current connection state
     */
    fun getConnectionState(): ConnectionState = connectionState
    
    /**
     * Checks if currently connected
     */
    fun isConnected(): Boolean = isConnected.get() && connectionState == ConnectionState.CONNECTED
    
    /**
     * Performs connection health check
     */
    suspend fun healthCheck(): Boolean {
        if (!isConnected()) return false
        
        return try {
            socket?.let { s ->
                !s.isClosed && s.isConnected && !s.isInputShutdown && !s.isOutputShutdown
            } ?: false
        } catch (e: Exception) {
            Log.w(TAG, "Health check failed", e)
            false
        }
    }
    
    private fun cleanupConnection() {
        isConnected.set(false)
        connectionState = ConnectionState.DISCONNECTED
        
        outputStream?.let { stream ->
            try {
                stream.close()
            } catch (e: IOException) {
                Log.w(TAG, "Error closing output stream", e)
            }
        }
        outputStream = null
        
        inputStream?.let { stream ->
            try {
                stream.close()
            } catch (e: IOException) {
                Log.w(TAG, "Error closing input stream", e)
            }
        }
        inputStream = null
        
        socket?.let { s ->
            try {
                if (!s.isClosed) {
                    s.close()
                } else {

                }
            } catch (e: IOException) {
                Log.w(TAG, "Error closing socket", e)
            }
        }
        socket = null
    }
    
    /**
     * Implements AutoCloseable for resource management
     */
    fun close() {
        kotlinx.coroutines.runBlocking {
            disconnect()
        }
    }
} 