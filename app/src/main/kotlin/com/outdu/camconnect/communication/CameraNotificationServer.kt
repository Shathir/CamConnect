package com.outdu.camconnect.communication

import android.util.Log
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Thread-safe camera notification server that handles incoming connections from camera devices.
 * Provides lifecycle management, client tracking, and proper resource cleanup.
 */
class CameraNotificationServer(
    private val port: Int = CameraApiManager.CAMERA_SERVER_SOCKET_PORT,
    private val maxClients: Int = 10
) {
    
    companion object {
        private const val TAG = "CameraNotificationServer"
        private const val CLIENT_TIMEOUT_MS = 30_000L // 30 seconds
        private const val MESSAGE_BUFFER_SIZE = 1000
    }
    
    /**
     * Server state enumeration
     */
    enum class ServerState {
        STOPPED, STARTING, RUNNING, STOPPING, ERROR
    }
    
    /**
     * Notification callback interface
     */
    interface NotificationListener {
        fun onClientConnected(clientId: String, clientAddress: String)
        fun onClientDisconnected(clientId: String, reason: String)
        fun onMessageReceived(clientId: String, message: String)
        fun onServerError(error: Throwable)
        fun onServerStateChanged(oldState: ServerState, newState: ServerState)
    }
    
    /**
     * Client connection data class
     */
    private data class ClientConnection(
        val id: String,
        val socket: Socket,
        val address: String,
        val connectedAt: Long,
        val job: Job
    )
    
    // Server state management
    @Volatile private var serverState = ServerState.STOPPED
    private val isRunning = AtomicBoolean(false)
    private val clientIdGenerator = AtomicInteger(0)
    
    // Server components
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Client management
    private val connectedClients = ConcurrentHashMap<String, ClientConnection>()
    private var notificationListener: NotificationListener? = null
    
    /**
     * Sets the notification listener for server events
     */
    fun setNotificationListener(listener: NotificationListener) {
        this.notificationListener = listener
    }
    
    /**
     * Starts the notification server
     */
    suspend fun start(): Result<Unit> = withContext(Dispatchers.IO) {
        if (isRunning.get()) {
            Log.w(TAG, "Server is already running")
            return@withContext Result.success(Unit)
        }
        
        Log.i(TAG, "Starting notification server on port $port")
        changeState(ServerState.STARTING)
        
        try {
            serverSocket = ServerSocket(port).apply {
                reuseAddress = true
                soTimeout = 5000 // Non-blocking accept with timeout
            }
            
            isRunning.set(true)
            changeState(ServerState.RUNNING)
            
            serverJob = serverScope.launch {
                runServerLoop()
            }
            
            Log.i(TAG, "Notification server started successfully on port $port")
            Result.success(Unit)
            
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start server", e)
            changeState(ServerState.ERROR)
            notificationListener?.onServerError(e)
            cleanupServer()
            Result.failure(ServerException.StartException("Failed to bind to port $port", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error starting server", e)
            changeState(ServerState.ERROR)
            notificationListener?.onServerError(e)
            cleanupServer()
            Result.failure(ServerException.StartException("Unexpected error", e))
        }
    }
    
    /**
     * Stops the notification server gracefully
     */
    suspend fun stop(): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isRunning.get()) {
            Log.w(TAG, "Server is not running")
            return@withContext Result.success(Unit)
        }
        
        Log.i(TAG, "Stopping notification server...")
        changeState(ServerState.STOPPING)
        
        try {
            isRunning.set(false)
            
            // Disconnect all clients
            disconnectAllClients("Server shutdown")
            
            // Cancel server job
            serverJob?.cancel()
            serverJob?.join()
            
            // Close server socket
            cleanupServer()
            
            changeState(ServerState.STOPPED)
            Log.i(TAG, "Notification server stopped successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server", e)
            changeState(ServerState.ERROR)
            notificationListener?.onServerError(e)
            Result.failure(ServerException.StopException("Error during shutdown", e))
        }
    }
    
    /**
     * Gets current server state
     */
    fun getServerState(): ServerState = serverState
    
    /**
     * Checks if server is running
     */
    fun isRunning(): Boolean = isRunning.get() && serverState == ServerState.RUNNING
    
    /**
     * Gets connected client count
     */
    fun getConnectedClientCount(): Int = connectedClients.size
    
    /**
     * Gets connected client information
     */
    fun getConnectedClients(): Map<String, String> {
        return connectedClients.mapValues { it.value.address }
    }
    
    /**
     * Disconnects a specific client
     */
    suspend fun disconnectClient(clientId: String, reason: String = "Requested disconnect"): Boolean {
        val client = connectedClients[clientId] ?: return false
        
        Log.i(TAG, "Disconnecting client $clientId: $reason")
        disconnectClient(client, reason)
        return true
    }
    
    /**
     * Main server loop for accepting connections
     */
    private suspend fun runServerLoop() {
        Log.d(TAG, "Server loop started")
        
        while (isRunning.get() && !currentCoroutineContext().isActive.not()) {
            try {
                // Accept new connection (with timeout to allow cancellation)
                val clientSocket = withTimeoutOrNull(5000) {
                    serverSocket?.accept()
                }
                
                clientSocket?.let { socket ->
                    if (connectedClients.size >= maxClients) {
                        Log.w(TAG, "Maximum clients reached, rejecting connection from ${socket.remoteSocketAddress}")
                        socket.close()
                        return@let
                    }
                    
                    val clientId = "client_${clientIdGenerator.incrementAndGet()}"
                    val clientAddress = socket.remoteSocketAddress.toString()
                    
                    Log.i(TAG, "New client connected: $clientId from $clientAddress")
                    
                    val clientJob = serverScope.launch {
                        handleClient(clientId, socket, clientAddress)
                    }
                    
                    val clientConnection = ClientConnection(
                        id = clientId,
                        socket = socket,
                        address = clientAddress,
                        connectedAt = System.currentTimeMillis(),
                        job = clientJob
                    )
                    
                    connectedClients[clientId] = clientConnection
                    notificationListener?.onClientConnected(clientId, clientAddress)
                }
                
            } catch (e: java.net.SocketTimeoutException) {
                // Normal timeout, continue loop
                continue
            } catch (e: SocketException) {
                if (isRunning.get()) {
                    Log.w(TAG, "Socket exception in server loop", e)
                }
                break
            } catch (e: Exception) {
                if (isRunning.get()) {
                    Log.e(TAG, "Unexpected error in server loop", e)
                    notificationListener?.onServerError(e)
                }
                break
            }
        }
        
        Log.d(TAG, "Server loop ended")
    }
    
    /**
     * Handles individual client connection
     */
    private suspend fun handleClient(clientId: String, socket: Socket, clientAddress: String) {
        Log.d(TAG, "Handling client $clientId")
        
        try {
            socket.soTimeout = CLIENT_TIMEOUT_MS.toInt()
            
            socket.getInputStream().use { inputStream ->
                val dataInputStream = DataInputStream(inputStream)
                val messageBuffer = ByteArray(MESSAGE_BUFFER_SIZE)
                
                while (isRunning.get() && !socket.isClosed && socket.isConnected) {
                    try {
                        val bytesRead = withTimeoutOrNull(CLIENT_TIMEOUT_MS) {
                            dataInputStream.read(messageBuffer)
                        }
                        
                        if (bytesRead == null) {
                            Log.d(TAG, "Client $clientId read timeout")
                            break
                        }
                        
                        if (bytesRead <= 0) {
                            Log.d(TAG, "Client $clientId disconnected (no data)")
                            break
                        }
                        
                        val message = String(messageBuffer, 0, bytesRead, Charsets.UTF_8)
                        Log.d(TAG, "Received message from $clientId: $message")
                        
                        notificationListener?.onMessageReceived(clientId, message)
                        
                    } catch (e: java.net.SocketTimeoutException) {
                        Log.d(TAG, "Client $clientId timeout, checking connection")
                        if (!socket.isConnected) break
                    } catch (e: IOException) {
                        Log.d(TAG, "Client $clientId IO error: ${e.message}")
                        break
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.w(TAG, "Error handling client $clientId", e)
        } finally {
            disconnectClient(connectedClients[clientId], "Connection ended")
        }
    }
    
    /**
     * Disconnects a specific client
     */
    private fun disconnectClient(client: ClientConnection?, reason: String) {
        client?.let {
            Log.d(TAG, "Disconnecting client ${it.id}: $reason")
            
            connectedClients.remove(it.id)
            it.job.cancel()
            
            try {
                if (!it.socket.isClosed) {
                    it.socket.close()
                }
            } catch (e: IOException) {
                Log.w(TAG, "Error closing client socket ${it.id}", e)
            }
            
            notificationListener?.onClientDisconnected(it.id, reason)
        }
    }
    
    /**
     * Disconnects all connected clients
     */
    private fun disconnectAllClients(reason: String) {
        Log.d(TAG, "Disconnecting all clients: $reason")
        
        val clients = connectedClients.values.toList()
        connectedClients.clear()
        
        clients.forEach { client ->
            disconnectClient(client, reason)
        }
    }
    
    /**
     * Changes server state and notifies listener
     */
    private fun changeState(newState: ServerState) {
        val oldState = serverState
        serverState = newState
        
        if (oldState != newState) {
            Log.d(TAG, "Server state changed: $oldState -> $newState")
            notificationListener?.onServerStateChanged(oldState, newState)
        }
    }
    
    /**
     * Cleans up server resources
     */
    private fun cleanupServer() {
        serverSocket?.let { socket ->
            try {
                if (!socket.isClosed) {
                    socket.close()
                } else {

                }
            } catch (e: IOException) {
                Log.w(TAG, "Error closing server socket", e)
            }
        }
        serverSocket = null
    }
    
    /**
     * Custom exceptions for server operations
     */
    sealed class ServerException(message: String, cause: Throwable? = null) : Exception(message, cause) {
        class StartException(message: String, cause: Throwable? = null) : ServerException("Failed to start server: $message", cause)
        class StopException(message: String, cause: Throwable? = null) : ServerException("Failed to stop server: $message", cause)
    }
    
    /**
     * Cleanup resources when object is garbage collected
     */
    protected fun finalize() {
        serverScope.cancel()
        kotlinx.coroutines.runBlocking {
            if (isRunning.get()) {
                stop()
            }
        }
    }
} 