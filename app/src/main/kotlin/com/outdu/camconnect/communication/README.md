# CamConnect - Production-Ready Kotlin Communication Layer

> **Modern, thread-safe, and production-ready camera communication framework for Android**

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Key Features](#key-features)
- [Components](#components)
- [Quick Start](#quick-start)
- [Usage Examples](#usage-examples)
- [Migration Guide](#migration-guide)
- [Best Practices](#best-practices)
- [API Reference](#api-reference)
- [Dependencies](#dependencies)
- [Contributing](#contributing)

---

## 🎯 Overview

The CamConnect communication layer provides a robust, production-ready framework for communicating with camera devices over network protocols. This completely refactored Kotlin implementation replaces the original Java-based communication system with modern Android development practices.

### What's New in Kotlin Version

- ✅ **Modern Coroutines** instead of deprecated AsyncTask
- ✅ **Thread-Safe Operations** with proper concurrency handling
- ✅ **Production-Ready Error Handling** with custom exception hierarchies
- ✅ **Connection Management** with pooling and health checks
- ✅ **Lifecycle Awareness** for Android components
- ✅ **Real-time Monitoring** with StateFlow
- ✅ **Type Safety** with sealed classes and Result types

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   Android Application Layer                 │
├─────────────────────────────────────────────────────────────┤
│              AndroidCameraController.kt                     │
│              (Lifecycle-aware UI Controller)                │
├─────────────────────────────────────────────────────────────┤
│                CameraApiManager.kt                          │
│           (High-level API Management)                       │
├─────────────────────────────────────────────────────────────┤
│  CameraSocketClient.kt  │  CameraNotificationServer.kt     │
│  (Command Communication) │     (Event Notifications)       │
├─────────────────────────────────────────────────────────────┤
│  CameraCommandProtocol.kt  │  CameraFileUploader.kt        │
│    (Protocol Definition)    │    (File Transfer)            │
├─────────────────────────────────────────────────────────────┤
│            CameraConfigurationManager.kt                    │
│              (Persistent Configuration)                     │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Key Features

### **1. Thread Safety & Concurrency**
- Atomic operations with `AtomicBoolean` and `AtomicReference`
- Thread-safe collections (`ConcurrentHashMap`)
- Read-write locks for configuration access
- Modern Kotlin coroutines for async operations

### **2. Connection Management**
- Automatic connection pooling and reuse
- Health checks and connection state monitoring
- Retry mechanisms with exponential backoff
- Graceful disconnection and cleanup

### **3. Error Handling**
- Custom exception hierarchies for each component
- Result types for safe error propagation
- Comprehensive logging with appropriate levels
- Graceful failure recovery

### **4. Real-time Monitoring**
- Operation state tracking with StateFlow
- Progress monitoring for long-running operations
- Connection status monitoring
- Health check APIs

### **5. Configuration Management**
- Thread-safe configuration persistence
- Async loading/saving operations
- Validation and type safety
- Batch configuration updates

---

## 📁 Components

| Component | Purpose | Key Features |
|-----------|---------|--------------|
| **`AndroidCameraController`** | Android UI integration | Lifecycle awareness, StateFlow, operation tracking |
| **`CameraApiManager`** | High-level API management | Singleton pattern, connection pooling, device discovery |
| **`CameraSocketClient`** | Network communication | Retry logic, CRC validation, timeout handling |
| **`CameraNotificationServer`** | Event notifications | Client management, server lifecycle, message handling |
| **`CameraCommandProtocol`** | Protocol definition | Type-safe enums, command builders, response parsing |
| **`CameraFileUploader`** | File transfer | Progress tracking, FTP operations, error recovery |
| **`CameraConfigurationManager`** | Configuration persistence | Thread-safe access, validation, async operations |

---

## ⚡ Quick Start

### 1. Basic Camera Connection

```kotlin
import com.outdu.camconnect.communication.*

// Initialize the camera controller
val cameraController = AndroidCameraController.getInstance()

// Connect to camera
cameraController.connectToDeviceAsync("192.168.2.1") { result, error ->
    if (error == null) {
        Log.i("Camera", "Connected successfully")
    } else {
        Log.e("Camera", "Connection failed: ${error.message}")
    }
}
```

### 2. Configuration Management

```kotlin
// Load configuration asynchronously
lifecycleScope.launch {
    val configResult = CameraConfigurationManager.loadConfigurationAsync(context)
    configResult.onSuccess { config ->
        Log.i("Config", "Loaded: $config")
    }.onFailure { error ->
        Log.e("Config", "Failed to load: ${error.message}")
    }
}

// Update configuration
lifecycleScope.launch {
    CameraConfigurationManager.setAudioEnabled(context, true)
        .onSuccess { Log.i("Config", "Audio enabled") }
        .onFailure { Log.e("Config", "Failed to enable audio") }
}
```

### 3. Camera Control

```kotlin
// Set camera zoom
cameraController.setZoomAsync(CameraCommandProtocol.ZOOM.X2) { success, error ->
    if (success) {
        Log.i("Camera", "Zoom set to 2x")
    } else {
        Log.e("Camera", "Failed to set zoom: ${error?.message}")
    }
}

// Start streaming
cameraController.startStreamAsync { success, error ->
    if (success) {
        Log.i("Camera", "Stream started")
    }
}
```

---

## 📚 Usage Examples

### Device Discovery

```kotlin
// Discover camera devices on network
cameraController.discoverDevicesAsync { devices, error ->
    if (error == null) {
        devices.forEach { ipAddress ->
            Log.i("Discovery", "Found camera at: $ipAddress")
        }
    }
}
```

### File Upload with Progress

```kotlin
lifecycleScope.launch {
    val uploaderResult = CameraFileUploader.create("192.168.2.1", "root", "password")
    
    uploaderResult.onSuccess { uploader ->
        uploader.use { 
            val uploadResult = it.uploadFile(
                inputStream = fileInputStream,
                remoteFileName = "firmware.bin",
                progressCallback = object : CameraFileUploader.ProgressCallback {
                    override fun onProgress(transferred: Long, total: Long) {
                        val progress = (transferred * 100 / total).toInt()
                        runOnUiThread {
                            progressBar.progress = progress
                        }
                    }
                    
                    override fun onTransferCompleted(fileName: String, bytesTransferred: Long) {
                        Log.i("Upload", "Upload completed: $fileName")
                    }
                }
            )
            
            uploadResult.onSuccess {
                Log.i("Upload", "File uploaded successfully")
            }
        }
    }
}
```

### Monitoring Connection State

```kotlin
class CameraActivity : AppCompatActivity() {
    private val cameraController = AndroidCameraController.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Observe connection state
        lifecycleScope.launch {
            cameraController.isConnected.collect { isConnected ->
                updateUI(isConnected)
            }
        }
        
        // Observe operations
        lifecycleScope.launch {
            cameraController.currentOperations.collect { operations ->
                updateOperationStatus(operations)
            }
        }
    }
}
```

### Notification Server

```kotlin
// Start notification server
val server = CameraNotificationServer()
server.setNotificationListener(object : CameraNotificationServer.NotificationListener {
    override fun onClientConnected(clientId: String, clientAddress: String) {
        Log.i("Server", "Client connected: $clientId from $clientAddress")
    }
    
    override fun onMessageReceived(clientId: String, message: String) {
        Log.i("Server", "Message from $clientId: $message")
    }
    
    override fun onServerError(error: Throwable) {
        Log.e("Server", "Server error", error)
    }
})

lifecycleScope.launch {
    server.start()
}
```

### Health Monitoring

```kotlin
// Perform health check
cameraController.performHealthCheckAsync { healthInfo, error ->
    if (error == null) {
        val deviceReachable = healthInfo["device_reachable"] as Boolean
        val configAccessible = healthInfo["config_accessible"] as Boolean
        
        Log.i("Health", "Device reachable: $deviceReachable")
        Log.i("Health", "Config accessible: $configAccessible")
    }
}
```

---

## 🔄 Migration Guide

### From Java to Kotlin

| **Old Java Files** | **New Kotlin Files** | **Migration Steps** |
|-------------------|---------------------|-------------------|
| `Data.java` | `CameraConfigurationManager.kt` | Replace direct calls with async methods |
| `MotocamSocketClient.java` | `CameraSocketClient.kt` | Use Result types, handle coroutines |
| `MotocamAPIAndroidHelper.java` | `AndroidCameraController.kt` | Replace AsyncTask with coroutines |

### Key Changes

#### 1. Async Operations
```kotlin
// Old Java way
new GetConfigTask().execute("Current")

// New Kotlin way
cameraController.getConfigurationAsync("Current") { result, error ->
    // Handle result
}
```

#### 2. Error Handling
```kotlin
// Old Java way
try {
    motocamClient.sendCmd(request, response)
} catch (Exception e) {
    // Handle error
}

// New Kotlin way
val result = cameraClient.sendCommand(request, response)
result.onSuccess { bytesRead ->
    // Handle success
}.onFailure { error ->
    // Handle error
}
```

#### 3. Configuration Access
```kotlin
// Old Java way
Data.setAUDIO(context, true)

// New Kotlin way
lifecycleScope.launch {
    CameraConfigurationManager.setAudioEnabled(context, true)
}
```

### Breaking Changes

1. **Async by Default**: All operations are now asynchronous
2. **Result Types**: Methods return `Result<T>` instead of throwing exceptions
3. **Coroutines**: Use coroutines instead of threads
4. **StateFlow**: Use StateFlow for reactive programming
5. **Lifecycle Awareness**: Integrate with Android lifecycle components

---

## 🛠️ Best Practices

### 1. Lifecycle Management

```kotlin
class CameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Add lifecycle observer
        lifecycle.addObserver(AndroidCameraController.getInstance())
    }
}
```

### 2. Error Handling

```kotlin
// Always handle both success and failure cases
cameraController.setZoomAsync(zoom) { success, error ->
    when {
        success -> handleSuccess()
        error != null -> {
            when (error) {
                is CameraSocketClient.SocketException.ConnectionException -> handleConnectionError()
                is CameraSocketClient.SocketException.TimeoutException -> handleTimeout()
                else -> handleGenericError(error)
            }
        }
    }
}
```

### 3. Resource Management

```kotlin
// Use lifecycleScope for automatic cleanup
class CameraFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Operations automatically cancelled when fragment destroyed
        lifecycleScope.launch {
            val config = CameraConfigurationManager.loadConfigurationAsync(requireContext())
            // Handle config
        }
    }
}
```

### 4. Configuration Updates

```kotlin
// Batch configuration updates for efficiency
val newConfig = CameraConfigurationManager.CameraConfig(
    farDetectionEnabled = true,
    objectDetectionEnabled = true,
    audioEnabled = false,
    modelVersion = 2,
    drowsinessThreshold = 0.7f,
    drowsinessDetectionEnabled = true
)

lifecycleScope.launch {
    CameraConfigurationManager.updateConfiguration(context, newConfig)
}
```

---

## 📖 API Reference

### Core Classes

#### `AndroidCameraController`
```kotlin
class AndroidCameraController {
    fun connectToDeviceAsync(ipAddress: String, callback: OperationCallback<Unit>)
    fun setZoomAsync(zoom: ZOOM, callback: OperationCallback<Boolean>)
    fun getConfigurationAsync(type: String, callback: OperationCallback<Map<String, Any>>)
    val isConnected: StateFlow<Boolean>
    val currentOperations: StateFlow<Map<String, OperationInfo>>
}
```

#### `CameraApiManager`
```kotlin
class CameraApiManager {
    suspend fun connectToDevice(ipAddress: String): Result<Unit>
    suspend fun getConfiguration(type: String): Result<Map<String, Any>>
    suspend fun setImageZoom(zoom: ZOOM): Result<Boolean>
    suspend fun startStream(): Result<Boolean>
    suspend fun uploadFile(fileName: String, inputStream: InputStream): Result<Unit>
}
```

#### `CameraConfigurationManager`
```kotlin
object CameraConfigurationManager {
    suspend fun loadConfigurationAsync(context: Context): Result<CameraConfig>
    suspend fun setAudioEnabled(context: Context, enabled: Boolean): Result<Unit>
    fun getCurrentConfiguration(): CameraConfig
    suspend fun updateConfiguration(context: Context, config: CameraConfig): Result<Unit>
}
```

### Command Protocol

#### Available Commands
```kotlin
// Streaming
CameraCommandProtocol.startStreamCmd()
CameraCommandProtocol.stopStreamCmd()

// Image Control
CameraCommandProtocol.setImgZoomCmd("x2")
CameraCommandProtocol.setImgRotationCmd("90")
CameraCommandProtocol.setImgIRBrightnessCmd(128)

// System
CameraCommandProtocol.shutdownCmd()
CameraCommandProtocol.getWifiStateCmd()
```

#### Available Values
```kotlin
// Get available parameter values
val zoomValues = CameraCommandProtocol.getAvailableValues("ZOOM")
// Returns: ["x1", "x2", "x3", "x4"]

val rotationValues = CameraCommandProtocol.getAvailableValues("ROTATION")
// Returns: ["0", "90", "180", "270"]
```

---

## 📦 Dependencies

### Required Dependencies

```kotlin
// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'

// Lifecycle
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.2'

// FTP (for file upload)
implementation 'commons-net:commons-net:3.8.0'
```

### Optional Dependencies

```kotlin
// For StateFlow UI integration
implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.6.2'

// For logging (if using custom logging)
implementation 'com.jakewharton.timber:timber:5.0.1'
```

---

## 🤝 Contributing

### Code Style
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Include unit tests for new features

### Pull Request Process
1. Create feature branch from `main`
2. Implement changes with tests
3. Update documentation if needed
4. Submit pull request with clear description

### Testing
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run integration tests
./gradlew connectedAndroidTest
```

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 🆘 Support

For questions and support:

- **Documentation**: Check this README and inline KDoc comments
- **Issues**: Open an issue on the project repository
- **Examples**: See the usage examples above

---

## 🔄 Changelog

### v2.0.0 (Kotlin Refactor)
- ✅ Complete rewrite in Kotlin
- ✅ Modern coroutines and StateFlow
- ✅ Production-ready error handling
- ✅ Thread-safe operations
- ✅ Connection management and pooling
- ✅ Lifecycle awareness
- ✅ Real-time monitoring

### v1.x.x (Java Legacy)
- Basic camera communication
- AsyncTask-based operations
- Limited error handling

---

**Happy Coding! 🚀** 