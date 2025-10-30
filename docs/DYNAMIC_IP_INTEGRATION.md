# Dynamic Camera IP Integration

## Overview

This document describes the comprehensive changes made to support dynamic camera IP addresses throughout the entire communication stack, enabling the viewer flow to work with any ONVIF-discovered camera.

## Problem Statement

The original implementation had hardcoded IP addresses (`192.168.2.1`) in multiple layers:
- `SessionManager.kt` - Authentication endpoint
- `MotocamSocketClient.kt` - API communication
- `MotoCamApiHelperWrapper.kt` - Device communication wrapper

This prevented the viewer flow from connecting to discovered cameras with different IP addresses.

## Solution Architecture

### 1. Dynamic IP Support at Authentication Layer

**File: `SessionManager.kt`**

**Enhanced Functions:**
```kotlin
// Before
suspend fun authenticateWithPin(pin: String): Result<Boolean>

// After  
suspend fun authenticateWithPin(pin: String, cameraIp: String? = null): Result<Boolean>
```

**Key Changes:**
- Added optional `cameraIp` parameter to authentication functions
- Dynamic endpoint URL generation based on camera IP
- Backward compatibility maintained (null defaults to original endpoint)

**Implementation:**
```kotlin
val loginEndpoint = if (cameraIp != null) {
    "http://$cameraIp:80/api/login"
} else {
    LOGIN_ENDPOINT // Default: "http://192.168.2.1:80/api/login"
}
```

### 2. Dynamic IP Support at Socket Communication Layer

**File: `MotocamSocketClient.kt`**

**Enhanced Architecture:**
- Added `cameraIp` property to store current target camera
- Enhanced `init()` function to accept camera IP
- Dynamic URL generation for all API endpoints

**Key Changes:**
```kotlin
// Added property
private var cameraIp: String = DEFAULT_CAMERA_IP

// Enhanced initialization
fun init(cameraIp: String? = null)

// New utility functions
fun setCameraIp(ipAddress: String)
fun getCameraIp(): String
```

**Dynamic Endpoints:**
- **API Endpoint**: `http://$cameraIp:80/api/motocam_api`
- **Login Test**: `http://$cameraIp:80/api/login`

### 3. Dynamic IP Support at Wrapper Layer

**File: `MotoCamApiHelperWrapper.kt`**

**Enhanced Functions:**
```kotlin
// Enhanced internal functions
private suspend fun <T> withSocketClient(cameraIp: String? = null, block: suspend (MotocamSocketClient) -> T): T

private suspend fun <T> sendCommand(
    reqCmd: IntArray,
    parse: (IntArray, Int) -> T,
    cameraIp: String? = null
): T
```

**New Viewer-Specific Functions:**
```kotlin
suspend fun startStream(cameraIp: String)
suspend fun stopStream(cameraIp: String)  
suspend fun getCurrentConfig(cameraIp: String)
fun setDeviceIpAddress(ipAddress: String)
fun getDeviceIpAddress(): String
```

### 4. Integration at Application Layer

**File: `MainActivity.kt`**

**Viewer Flow Integration:**
```kotlin
private fun handleViewerFlowParameters() {
    val cameraIp = intent.getStringExtra("CAMERA_IP")
    if (cameraIp != null && userType == "VIEWER") {
        // Configure communication layer for specific camera
        MotocamAPIHelperWrapper.setDeviceIpAddress(cameraIp)
    }
}
```

## Communication Flow

### 1. Viewer Authentication Flow
```
User selects camera (IP: 10.109.79.100)
    ↓
ViewerFlowViewModel.authenticateWithPin(pin, "10.109.79.100")
    ↓
SessionManager.authenticateWithPin(pin, "10.109.79.100")
    ↓
performLogin(pin, "10.109.79.100")
    ↓
POST http://10.109.79.100:80/api/login
```

### 2. Camera Communication Flow
```
MainActivity receives camera IP
    ↓
MotocamAPIHelperWrapper.setDeviceIpAddress("10.109.79.100")
    ↓
API calls use dynamic IP
    ↓
MotocamSocketClient.init("10.109.79.100")
    ↓
POST http://10.109.79.100:80/api/motocam_api
```

## Backward Compatibility

### Owner Flow
- **Unchanged**: All existing functionality preserved
- **Default Behavior**: When `cameraIp` is null, uses original hardcoded IPs
- **No Breaking Changes**: Existing API signatures maintained with optional parameters

### Legacy Support
```kotlin
// Still works (uses default IP)
SessionManager.authenticateWithPin("1234")
MotocamSocketClient().init()

// New functionality (uses dynamic IP)  
SessionManager.authenticateWithPin("1234", "10.109.79.100")
MotocamSocketClient().init("10.109.79.100")
```

## Configuration Options

### Default IP Addresses
```kotlin
// SessionManager
private const val LOGIN_ENDPOINT = "http://192.168.2.1:80/api/login"

// MotocamSocketClient  
private const val DEFAULT_CAMERA_IP = "192.168.2.1"

// MotoCamApiHelperWrapper
private var deviceIpAddress: String = "192.168.2.1"
```

### Runtime Configuration
```kotlin
// Set camera IP for viewer flow
MotocamSocketClient().setCameraIp("10.109.79.100")
MotocamAPIHelperWrapper.setDeviceIpAddress("10.109.79.100")

// Authentication with specific camera
SessionManager.authenticateWithPin("1234", "10.109.79.100")
```

## Error Handling

### Network Connectivity
- **Connection Timeouts**: 10 second timeout for all requests
- **Unreachable IPs**: Proper error messages for network failures
- **Invalid Responses**: CRC validation and response parsing

### Authentication Errors
- **Camera-Specific**: Error messages include camera IP context
- **Fallback**: Graceful degradation to default endpoints when needed
- **Logging**: Comprehensive logging for troubleshooting

## Security Considerations

### Network Security
- **Same Protocol**: Uses existing binary protocol and CRC validation
- **Session Management**: Same token-based authentication system
- **IP Validation**: Input validation for camera IP addresses

### Authentication Security
- **PIN Limits**: Same attempt limits per camera IP
- **Lockout Behavior**: Lockouts are camera-specific
- **Session Isolation**: Each camera maintains separate session state

## Testing Scenarios

### Successful Dynamic IP Usage
```kotlin
// Test viewer flow with discovered camera
val camera = OnvifDevice("10.109.79.100", listOf("http://10.109.79.100/onvif/device_service"))
val result = SessionManager.authenticateWithPin("1234", camera.ipAddress)
// Should authenticate with 10.109.79.100

// Test API calls with dynamic IP
MotocamAPIHelperWrapper.startStream("10.109.79.100")
// Should send commands to 10.109.79.100
```

### Backward Compatibility Testing
```kotlin
// Test existing owner flow (should use default IPs)
val result = SessionManager.authenticateWithPin("1234")
// Should use 192.168.2.1

val config = MotocamAPIHelperWrapper.getCurrentConfig()
// Should use default device IP
```

### Error Scenarios
```kotlin
// Test invalid IP
val result = SessionManager.authenticateWithPin("1234", "invalid.ip")
// Should return network error

// Test unreachable IP  
val result = SessionManager.authenticateWithPin("1234", "192.168.1.999")
// Should return connection timeout
```

## Performance Impact

### Minimal Overhead
- **Same Protocol**: No additional network overhead
- **Efficient Caching**: IP addresses cached per instance
- **Connection Reuse**: HTTP client connection pooling maintained

### Network Optimization
- **Direct Connection**: Connects directly to discovered camera IP
- **Reduced Hops**: Eliminates network routing through default gateway
- **Faster Response**: Direct IP connection typically faster than DNS resolution

## Future Enhancements

### Planned Improvements
1. **Port Discovery**: Auto-detect camera API ports via ONVIF
2. **Connection Pooling**: Maintain connections to multiple cameras
3. **Load Balancing**: Distribute requests across multiple camera endpoints
4. **Health Monitoring**: Track connection health per camera IP

### Advanced Features
1. **Certificate Pinning**: Validate SSL certificates for HTTPS cameras
2. **Proxy Support**: Route connections through network proxies
3. **IPv6 Support**: Support for IPv6 camera addresses
4. **Service Discovery**: Integration with mDNS/Bonjour for camera discovery

## Summary

The dynamic IP integration successfully provides:

✅ **Full Dynamic IP Support**: All communication layers support camera-specific IPs
✅ **Backward Compatibility**: Owner flow unchanged, no breaking changes  
✅ **Viewer Flow Integration**: Seamless integration with ONVIF discovery
✅ **Error Handling**: Comprehensive error handling and logging
✅ **Security Maintained**: All existing security features preserved
✅ **Performance Optimized**: Direct connections to discovered cameras

The implementation enables the viewer flow to work with any ONVIF-discovered camera while maintaining full compatibility with the existing owner flow and communication protocols.
