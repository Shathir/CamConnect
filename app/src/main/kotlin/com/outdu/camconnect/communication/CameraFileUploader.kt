package com.outdu.camconnect.communication

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.apache.commons.net.PrintCommandListener
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.seconds

/**
 * Thread-safe camera file uploader with enhanced connection management,
 * progress tracking, and automatic retry mechanisms.
 */
class CameraFileUploader private constructor(
    private val host: String,
    private val username: String,
    private val password: String,
    private val connectionTimeout: Int = 15000,
    private val dataTimeout: Int = 30000
) : AutoCloseable {
    
    companion object {
        private const val TAG = "CameraFileUploader"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 2000L
        private const val BUFFER_SIZE = 8192
        
        /**
         * Creates a new CameraFileUploader instance with connection validation
         */
        suspend fun create(
            host: String,
            username: String,
            password: String,
            connectionTimeout: Int = 15000,
            dataTimeout: Int = 30000
        ): Result<CameraFileUploader> = withContext(Dispatchers.IO) {
            try {
                val uploader = CameraFileUploader(host, username, password, connectionTimeout, dataTimeout)
                val connectResult = uploader.connect()
                
                if (connectResult.isSuccess) {
                    Result.success(uploader)
                } else {
                    uploader.cleanup()
                    Result.failure(connectResult.exceptionOrNull() ?: Exception("Unknown connection error"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create uploader", e)
                Result.failure(UploaderException.ConnectionException("Failed to create uploader", e))
            }
        }
    }
    
    /**
     * Upload progress callback interface
     */
    interface ProgressCallback {
        fun onProgress(bytesTransferred: Long, totalBytes: Long)
        fun onTransferStarted(fileName: String, totalBytes: Long)
        fun onTransferCompleted(fileName: String, bytesTransferred: Long)
        fun onTransferFailed(fileName: String, error: Throwable)
    }
    
    /**
     * Upload configuration data class
     */
    data class UploadConfig(
        val remoteDirectory: String = "",
        val overwriteExisting: Boolean = true,
        val createDirectories: Boolean = true,
        val binaryMode: Boolean = true,
        val passiveMode: Boolean = true
    )
    
    /**
     * Custom exceptions for upload operations
     */
    sealed class UploaderException(message: String, cause: Throwable? = null) : Exception(message, cause) {
        class ConnectionException(message: String, cause: Throwable? = null) : UploaderException("Connection error: $message", cause)
        class AuthenticationException(message: String) : UploaderException("Authentication failed: $message")
        class TransferException(message: String, cause: Throwable? = null) : UploaderException("Transfer error: $message", cause)
        class ConfigurationException(message: String) : UploaderException("Configuration error: $message")
        class TimeoutException(message: String) : UploaderException("Operation timeout: $message")
    }
    
    // Connection management
    private var ftpClient: FTPClient? = null
    private val connectionState = AtomicBoolean(false)
    
    /**
     * Establishes FTP connection with retry mechanism
     */
    private suspend fun connect(): Result<Unit> = withContext(Dispatchers.IO) {
        Log.i(TAG, "Connecting to FTP server at $host")
        
        var lastException: Exception? = null
        
        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            try {
                withTimeout(connectionTimeout.toLong()) {
                    ftpClient = FTPClient().apply {
                        // Configure timeouts
                        connectTimeout = connectionTimeout
                        defaultTimeout = dataTimeout
                        
                        // Enable debug logging in debug builds
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            addProtocolCommandListener(PrintCommandListener(PrintWriter(object : Writer() {
                                override fun write(cbuf: CharArray, off: Int, len: Int) {
                                    val logMessage = String(cbuf, off, len).trim()
                                    Log.d(TAG, "FTP: $logMessage")
                                }
                                override fun flush() {}
                                override fun close() {}
                            })))
                        }
                        
                        // Connect to server
                        connect(host)
                        
                        val replyCode = replyCode
                        if (!FTPReply.isPositiveCompletion(replyCode)) {
                            disconnect()
                            throw UploaderException.ConnectionException(
                                "FTP server refused connection. Reply code: $replyCode"
                            )
                        }
                        
                        // Authenticate
                        if (!login(username, password)) {
                            disconnect()
                            throw UploaderException.AuthenticationException(
                                "Login failed for user: $username"
                            )
                        }
                        
                        // Configure transfer mode
                        setFileType(FTP.BINARY_FILE_TYPE)
                        enterLocalPassiveMode()
                        
                        connectionState.lazySet(true)
                        Log.i(TAG, "Successfully connected to FTP server")
                    }
                }
                return@withContext Result.success(Unit)
                
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                lastException = UploaderException.TimeoutException("Connection timeout on attempt ${attempt + 1}")
                Log.w(TAG, "Connection attempt ${attempt + 1} timed out")
            } catch (e: UploaderException) {
                lastException = e
                Log.w(TAG, "Connection attempt ${attempt + 1} failed: ${e.message}")
            } catch (e: IOException) {
                lastException = UploaderException.ConnectionException("IO error on attempt ${attempt + 1}", e)
                Log.w(TAG, "Connection attempt ${attempt + 1} failed with IO error: ${e.message}")
            } catch (e: Exception) {
                lastException = UploaderException.ConnectionException("Unexpected error on attempt ${attempt + 1}", e)
                Log.w(TAG, "Connection attempt ${attempt + 1} failed unexpectedly: ${e.message}")
            }
            
            // Cleanup failed connection
            cleanup()
            
            // Wait before retry (except for last attempt)
            if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                kotlinx.coroutines.delay(RETRY_DELAY_MS)
            }
        }
        
        val finalException = lastException ?: UploaderException.ConnectionException("Unknown connection error")
        Log.e(TAG, "Failed to connect after $MAX_RETRY_ATTEMPTS attempts", finalException)
        Result.failure(finalException)
    }
    
    /**
     * Uploads a file from local file system
     */
    suspend fun uploadFile(
        localFilePath: String,
        remoteFileName: String,
        config: UploadConfig = UploadConfig(),
        progressCallback: ProgressCallback? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        
        val localFile = File(localFilePath)
        if (!localFile.exists()) {
            return@withContext Result.failure(
                UploaderException.ConfigurationException("Local file not found: $localFilePath")
            )
        }
        
        if (!localFile.canRead()) {
            return@withContext Result.failure(
                UploaderException.ConfigurationException("Cannot read local file: $localFilePath")
            )
        }
        
        return@withContext try {
            localFile.inputStream().use { inputStream ->
                uploadFile(inputStream, remoteFileName, localFile.length(), config, progressCallback)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to open local file", e)
            Result.failure(UploaderException.TransferException("Failed to open local file", e))
        }
    }
    
    /**
     * Uploads a file from InputStream with progress tracking
     */
    suspend fun uploadFile(
        inputStream: InputStream,
        remoteFileName: String,
        totalBytes: Long = -1,
        config: UploadConfig = UploadConfig(),
        progressCallback: ProgressCallback? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        
        if (!connectionState.get()) {
            return@withContext Result.failure(
                UploaderException.ConnectionException("Not connected to FTP server")
            )
        }
        
        Log.i(TAG, "Starting upload: $remoteFileName")
        progressCallback?.onTransferStarted(remoteFileName, totalBytes)
        
        try {
            val client = ftpClient ?: throw UploaderException.ConnectionException("FTP client is null")
            
            // Change to remote directory if specified
            if (config.remoteDirectory.isNotEmpty()) {
                if (!changeToDirectory(client, config.remoteDirectory, config.createDirectories)) {
                    throw UploaderException.ConfigurationException("Failed to change to directory: ${config.remoteDirectory}")
                }
            }
            
            // Check if file exists and handle overwrite policy
            if (!config.overwriteExisting && fileExists(client, remoteFileName)) {
                throw UploaderException.ConfigurationException("Remote file already exists: $remoteFileName")
            }
            
            // Configure transfer mode
            if (config.binaryMode) {
                client.setFileType(FTP.BINARY_FILE_TYPE)
            } else {
                client.setFileType(FTP.ASCII_FILE_TYPE)
            }
            
            if (config.passiveMode) {
                client.enterLocalPassiveMode()
            } else {
                client.enterLocalActiveMode()
            }
            
            // Perform upload with progress tracking
            val success = if (totalBytes > 0 && progressCallback != null) {
                uploadWithProgress(client, inputStream, remoteFileName, totalBytes, progressCallback)
            } else {
                client.storeFile(remoteFileName, inputStream)
            }
            
            if (success) {
                Log.i(TAG, "Upload completed successfully: $remoteFileName")
                progressCallback?.onTransferCompleted(remoteFileName, totalBytes)
                Result.success(Unit)
            } else {
                val replyString = client.replyString
                val error = UploaderException.TransferException("Upload failed. Server reply: $replyString")
                Log.e(TAG, "Upload failed: $remoteFileName", error)
                progressCallback?.onTransferFailed(remoteFileName, error)
                Result.failure(error)
            }
            
        } catch (e: UploaderException) {
            Log.e(TAG, "Upload error", e)
            progressCallback?.onTransferFailed(remoteFileName, e)
            Result.failure(e)
        } catch (e: IOException) {
            val error = UploaderException.TransferException("IO error during upload", e)
            Log.e(TAG, "IO error during upload", error)
            progressCallback?.onTransferFailed(remoteFileName, error)
            Result.failure(error)
        } catch (e: Exception) {
            val error = UploaderException.TransferException("Unexpected error during upload", e)
            Log.e(TAG, "Unexpected error during upload", error)
            progressCallback?.onTransferFailed(remoteFileName, error)
            Result.failure(error)
        }
    }
    
    /**
     * Checks if currently connected to FTP server
     */
    fun isConnected(): Boolean {
        return connectionState.get() && (ftpClient?.isConnected ?: false)
    }
    
    /**
     * Gets current working directory
     */
    suspend fun getCurrentDirectory(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val client = ftpClient ?: return@withContext Result.failure(
                UploaderException.ConnectionException("Not connected")
            )
            
            val directory = client.printWorkingDirectory()
            if (directory != null) {
                Result.success(directory)
            } else {
                Result.failure(UploaderException.TransferException("Failed to get current directory"))
            }
        } catch (e: IOException) {
            Result.failure(UploaderException.TransferException("IO error getting directory", e))
        }
    }
    
    /**
     * Lists files in current or specified directory
     */
    suspend fun listFiles(directory: String = ""): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val client = ftpClient ?: return@withContext Result.failure(
                UploaderException.ConnectionException("Not connected")
            )
            
            val files = if (directory.isEmpty()) {
                client.listNames()
            } else {
                client.listNames(directory)
            }
            
            Result.success(files?.toList() ?: emptyList())
        } catch (e: IOException) {
            Result.failure(UploaderException.TransferException("Failed to list files", e))
        }
    }
    
    private fun uploadWithProgress(
        client: FTPClient,
        inputStream: InputStream,
        remoteFileName: String,
        totalBytes: Long,
        progressCallback: ProgressCallback
    ): Boolean {
        return try {
            val bufferedInput = BufferedInputStream(inputStream, BUFFER_SIZE)
            var bytesTransferred = 0L
            
            val outputStream = client.storeFileStream(remoteFileName) ?: return false
            outputStream.use { output ->
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                
                while (bufferedInput.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    bytesTransferred += bytesRead
                    
                    progressCallback.onProgress(bytesTransferred, totalBytes)
                }
            }
            
            client.completePendingCommand()
        } catch (e: IOException) {
            Log.e(TAG, "Error during upload with progress", e)
            false
        }
    }
    
    private fun changeToDirectory(client: FTPClient, directory: String, createIfNotExists: Boolean): Boolean {
        return try {
            if (client.changeWorkingDirectory(directory)) {
                true
            } else if (createIfNotExists) {
                createDirectoryPath(client, directory) && client.changeWorkingDirectory(directory)
            } else {
                false
            }
        } catch (e: IOException) {
            Log.w(TAG, "Failed to change directory to: $directory", e)
            false
        }
    }
    
    private fun createDirectoryPath(client: FTPClient, path: String): Boolean {
        val directories = path.split("/").filter { it.isNotEmpty() }
        var currentPath = if (path.startsWith("/")) "/" else ""
        
        for (dir in directories) {
            currentPath += if (currentPath.endsWith("/")) dir else "/$dir"
            
            try {
                val directoryChanged = client.changeWorkingDirectory(currentPath)
                if (!directoryChanged) {
                    val directoryCreated = client.makeDirectory(currentPath)
                    if (!directoryCreated) {
                        Log.w(TAG, "Failed to create directory: $currentPath")
                        return false
                    }
                }
            } catch (e: IOException) {
                Log.w(TAG, "Error creating directory: $currentPath", e)
                return false
            }
        }
        
        return true
    }
    
    private fun fileExists(client: FTPClient, fileName: String): Boolean {
        return try {
            client.listFiles(fileName).isNotEmpty()
        } catch (e: IOException) {
            false
        }
    }
    
    private fun cleanup() {
        connectionState.lazySet(false)
        
        ftpClient?.let { client ->
            try {
                if (client.isConnected) {
                    client.logout()
                    client.disconnect()
                }
                else {
                    Log.w(TAG, "FTP client is not connected")
                }
            } catch (e: IOException) {
                Log.w(TAG, "Error during FTP cleanup", e)
            }
        }
        ftpClient = null
    }
    
    /**
     * Disconnects from FTP server
     */
    suspend fun disconnect(): Result<Unit> = withContext(Dispatchers.IO) {
        Log.i(TAG, "Disconnecting from FTP server")
        
        try {
            cleanup()
            Log.i(TAG, "Successfully disconnected from FTP server")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Error during disconnect", e)
            Result.failure(UploaderException.ConnectionException("Error during disconnect", e))
        }
    }
    
    override fun close() {
        kotlinx.coroutines.runBlocking {
            disconnect()
        }
    }
} 