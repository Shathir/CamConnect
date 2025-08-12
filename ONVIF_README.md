# ONVIF Client - Production Ready Implementation

## Overview

This is a production-ready ONVIF (Open Network Video Interface Forum) client implementation for Android, built with Kotlin and Jetpack Compose. It provides robust discovery and communication capabilities for IP cameras and network video devices.

## Features

### ✅ Device Discovery
- **WS-Discovery Protocol**: UDP multicast discovery on local networks
- **Network Connectivity Check**: Validates network availability before discovery
- **Concurrent Discovery**: Thread-safe device collection with deduplication
- **Timeout Management**: Configurable discovery timeouts

### ✅ SOAP Communication
- **Ktor HTTP Client**: Modern, coroutine-based HTTP client
- **Multiple Authentication**: Basic Auth, Digest Auth, and WS-Security support
- **Proper XML Parsing**: XMLPull parser instead of regex
- **Error Handling**: Comprehensive error classification and recovery
- **Request Retry**: Automatic retry with exponential backoff
- **Connection Pooling**: Efficient HTTP connection management

### ✅ Security
- **Network Security Config**: Allows HTTP for local cameras, HTTPS for external
- **Authentication Support**: Username/password with secure credential handling
- **WS-Security**: Full WS-UsernameToken with password digest support
- **Certificate Validation**: Proper SSL/TLS certificate handling

### ✅ Production Features
- **Structured Logging**: Android Log integration with proper log levels
- **Resource Management**: Proper cleanup of HTTP clients and connections
- **Memory Management**: Efficient state management without memory leaks
- **Thread Safety**: Concurrent device access with thread-safe collections
- **Error Recovery**: Graceful handling of network failures and timeouts

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    OnvifScreen (Compose UI)                 │
├─────────────────────────────────────────────────────────────┤
│                   OnvifViewModel                            │
├─────────────────────────────────────────────────────────────┤
│  OnvifDiscoveryService     │     OnvifSoapClient           │
│  (UDP WS-Discovery)        │     (HTTP SOAP + Ktor)        │
├─────────────────────────────────────────────────────────────┤
│                    Data Models & Results                    │
│  OnvifDevice, OnvifResult, OnvifException, etc.           │
└─────────────────────────────────────────────────────────────┘
```

## Key Components

### 1. OnvifDiscoveryService
- Handles UDP multicast WS-Discovery protocol
- Network connectivity validation
- Concurrent device discovery with proper threading
- XML parsing for probe responses

### 2. OnvifSoapClient
- Ktor-based HTTP client with production configurations
- Multiple authentication methods (Basic, Digest, WS-Security)
- Proper SOAP message construction and parsing
- Comprehensive error handling and retry logic

### 3. OnvifViewModel
- Production-ready state management
- Proper resource cleanup
- Thread-safe device updates
- Comprehensive error handling

## Usage

### Basic Discovery
```kotlin
val viewModel = OnvifViewModel()
viewModel.discoverDevices(context)

// Observe results
val uiState by viewModel.uiState
```

### Device Communication
```kotlin
// Basic authentication
val credentials = OnvifCredentials(
    username = "admin",
    password = "password",
    authType = AuthType.BASIC
)

viewModel.fetchDeviceInfo(device, credentials)
```

### WS-Security Authentication
```kotlin
val credentials = OnvifCredentials(
    username = "admin",
    password = "password",
    authType = AuthType.WS_SECURITY
)
```

## Configuration

### Network Security
The app includes a network security configuration that:
- Allows HTTP connections to local network IP cameras
- Requires HTTPS for external connections
- Maintains proper certificate validation

### Timeouts
- **Discovery Timeout**: 5 seconds (configurable)
- **HTTP Connection**: 5 seconds
- **HTTP Request**: 10 seconds
- **Socket Timeout**: 10 seconds

### Retry Policy
- **Max Retries**: 2 attempts
- **Retry Strategy**: Exponential backoff
- **Retry Conditions**: Server errors and timeouts

## Error Handling

The implementation includes comprehensive error types:

- `NetworkError`: Connection and network-related issues
- `AuthenticationError`: Authentication failures
- `ProtocolError`: SOAP/ONVIF protocol errors
- `TimeoutError`: Request timeout issues
- `DeviceNotFound`: Device endpoint not found
- `ParseError`: XML parsing failures

## Dependencies

### Ktor Client Stack
```kotlin
implementation("io.ktor:ktor-client-core:2.3.5")
implementation("io.ktor:ktor-client-cio:2.3.5")
implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
```

### Android Components
- Jetpack Compose for UI
- ViewModel and Lifecycle components
- Kotlinx Coroutines for async operations
- XMLPull parser for SOAP response parsing

## Permissions Required

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

## Production Considerations

### ✅ Implemented
- Proper error handling and recovery
- Resource cleanup and memory management
- Thread-safe operations
- Network security configuration
- Comprehensive logging
- Retry mechanisms
- Authentication support
- XML parsing instead of regex

### 🔄 Future Enhancements
- Device capability caching
- Background discovery service
- PTZ (Pan-Tilt-Zoom) control
- Live streaming integration
- Device configuration management
- Snapshot image caching
- Database persistence
- Unit and integration tests

## Testing

### Unit Tests
```kotlin
// TODO: Add comprehensive unit tests for:
// - OnvifDiscoveryService
// - OnvifSoapClient
// - XML parsing functions
// - Error handling scenarios
```

### Integration Tests
```kotlin
// TODO: Add integration tests for:
// - End-to-end device discovery
// - SOAP communication with mock servers
// - Authentication flows
// - Error recovery scenarios
```

## Performance

- **Memory Efficient**: Proper cleanup of HTTP clients and coroutines
- **Network Optimized**: Connection pooling and request batching
- **UI Responsive**: All network operations on background threads
- **Resource Conscious**: Configurable timeouts and retry limits

## Security Best Practices

1. **Credential Management**: Secure handling of authentication credentials
2. **Network Security**: HTTP only for local networks, HTTPS for external
3. **Input Validation**: Proper validation of device URLs and responses
4. **Resource Cleanup**: Prevents resource leaks and DoS scenarios
5. **Error Information**: Limited error information exposure

## Troubleshooting

### Common Issues

1. **No Devices Found**
   - Check network connectivity
   - Verify devices are on same network
   - Check firewall settings
   - Ensure UDP multicast is enabled

2. **Authentication Failures**
   - Verify credentials are correct
   - Try different authentication types
   - Check device authentication requirements

3. **Connection Timeouts**
   - Increase timeout values
   - Check network stability
   - Verify device IP addresses

### Debugging

Enable verbose logging:
```kotlin
// Logs are automatically enabled in debug builds
// Check Android Logcat for detailed information
```

## License

This implementation follows Android development best practices and ONVIF protocol specifications. Suitable for commercial and open-source projects. 