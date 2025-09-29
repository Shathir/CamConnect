# Authentication Integration for Viewer Flow

## Overview

The Viewer Flow has been successfully integrated with the existing SessionManager authentication system. The implementation uses the existing `performLogin` function and binary protocol while adding support for dynamic camera IP addresses.

## Changes Made

### 1. Enhanced SessionManager.kt

**Function: `authenticateWithPin`**
- **Before**: `suspend fun authenticateWithPin(pin: String): Result<Boolean>`
- **After**: `suspend fun authenticateWithPin(pin: String, cameraIp: String? = null): Result<Boolean>`

**Function: `performLogin`**
- **Before**: `private suspend fun performLogin(pin: String): Result<Boolean>`
- **After**: `private suspend fun performLogin(pin: String, cameraIp: String? = null): Result<Boolean>`

**Key Enhancement**: Dynamic endpoint URL generation
```kotlin
// Determine the login endpoint URL
val loginEndpoint = if (cameraIp != null) {
    "http://$cameraIp:80/api/login"
} else {
    LOGIN_ENDPOINT // Default: "http://192.168.2.1:80/api/login"
}
```

### 2. Updated ViewerFlowViewModel.kt

**Enhanced Authentication Call**:
```kotlin
// Before
val result = SessionManager.authenticateWithPin(pin)

// After  
val result = SessionManager.authenticateWithPin(pin, camera.ipAddress)
```

## Authentication Flow

### 1. User Journey
1. User discovers cameras via ONVIF
2. User selects a camera (e.g., IP: `10.109.79.100`)
3. User enters 4-digit PIN in the authentication dialog
4. System calls `SessionManager.authenticateWithPin(pin, "10.109.79.100")`

### 2. Technical Flow
1. **PIN Validation**: Validates 4-digit format
2. **Lockout Check**: Checks for existing lockouts/attempt limits
3. **Binary Protocol Construction**: Creates binary command with PIN data
4. **Dynamic Endpoint**: Uses discovered camera IP for login endpoint
5. **HTTP Request**: Sends binary data to `http://{camera_ip}:80/api/login`
6. **Response Processing**: Parses JSON response and extracts session token
7. **Session Storage**: Stores session token for authenticated access

### 3. Protocol Details

**Binary Command Structure** (from existing implementation):
```
Header=2 (GET) | Command=6 (System) | Sub-command=4 (login_pin) | Data Length | PIN bytes | CRC
```

**HTTP Request**:
- **Method**: POST
- **Endpoint**: `http://{discovered_camera_ip}:80/api/login`
- **Content-Type**: `application/octet-stream`
- **Body**: Hex string representation of binary command

**Response Format**:
```json
{
  "message": "Login successful"  // Success case
}
// OR
{
  "error": "Invalid PIN provided"  // Error case
}
```

## Security Features

### Maintained Security Measures
- **PIN Attempt Limits**: Maximum 3 attempts per sequence
- **Exponential Backoff**: Lockout durations from 30s to 4 hours
- **Session Management**: 24-hour session timeout
- **Secure Storage**: SharedPreferences for session tokens
- **CRC Validation**: Binary protocol integrity checking

### Enhanced Security
- **Camera-Specific Authentication**: Each camera has its own login endpoint
- **Dynamic IP Support**: No hardcoded camera addresses
- **Network Isolation**: Authentication happens directly with discovered camera

## Error Handling

### Authentication Errors
- **Invalid PIN Format**: "PIN must be exactly 4 digits"
- **Lockout Active**: "Account locked. Try again in {time}"
- **Network Errors**: "Network error during login"
- **Server Errors**: "Server error: {status_code}"
- **Invalid Response**: "Invalid response format"

### User Experience
- Real-time error display in authentication dialog
- Lockout countdown timer with visual feedback
- Retry mechanisms for network failures
- Clear error messages for troubleshooting

## Backward Compatibility

### Owner Flow
- **Unchanged**: Existing owner authentication continues to work
- **Default Behavior**: When `cameraIp` is null, uses default endpoint
- **No Breaking Changes**: All existing functionality preserved

### Existing API Calls
- **Session Token**: Same format and usage
- **Cookie Management**: Unchanged session cookie handling
- **Logout Process**: Same server logout procedure

## Testing Scenarios

### Successful Authentication
```kotlin
// Test with discovered camera
val result = SessionManager.authenticateWithPin("1234", "10.109.79.100")
// Should return Result.success(true)
```

### Error Cases
```kotlin
// Invalid PIN format
val result = SessionManager.authenticateWithPin("12", "10.109.79.100")
// Should return Result.failure(InvalidPinException)

// Network unreachable
val result = SessionManager.authenticateWithPin("1234", "192.168.1.999")
// Should return Result.failure(AuthenticationNetworkException)
```

### Lockout Behavior
```kotlin
// After 3 failed attempts
val result = SessionManager.authenticateWithPin("0000", "10.109.79.100")
// Should trigger lockout with exponential backoff
```

## Integration Benefits

### For Viewer Flow
1. **Dynamic Camera Support**: Works with any discovered ONVIF camera
2. **Secure Authentication**: Uses proven binary protocol and PIN system
3. **Network Flexibility**: Adapts to different network configurations
4. **Error Resilience**: Comprehensive error handling and recovery

### For Overall System
1. **Code Reuse**: Leverages existing authentication infrastructure
2. **Consistency**: Same security model across owner and viewer flows
3. **Maintainability**: Single authentication system to maintain
4. **Scalability**: Supports multiple cameras and network topologies

## Configuration

### Network Requirements
- Camera must be accessible on port 80
- Camera must support `/api/login` endpoint
- Camera must respond with JSON format as specified

### Camera Compatibility
- Must support ONVIF discovery for IP detection
- Must implement the binary protocol login system
- Must return session tokens in Set-Cookie headers

## Future Enhancements

### Potential Improvements
1. **HTTPS Support**: Add SSL/TLS encryption for login requests
2. **Port Discovery**: Auto-detect camera login port via ONVIF
3. **Certificate Validation**: Verify camera SSL certificates
4. **Multi-Camera Sessions**: Support concurrent camera authentications
5. **Credential Caching**: Secure storage of camera-specific credentials

### Monitoring and Analytics
1. **Authentication Metrics**: Track success/failure rates per camera
2. **Network Diagnostics**: Monitor connection quality and timeouts
3. **Security Auditing**: Log authentication attempts and patterns
4. **Performance Monitoring**: Track login response times

## Summary

The authentication integration successfully combines:
- ✅ Existing SessionManager authentication logic
- ✅ Dynamic camera IP support for viewer flow
- ✅ Binary protocol implementation (`performLogin`)
- ✅ Comprehensive error handling and security features
- ✅ Backward compatibility with owner flow
- ✅ Network flexibility for ONVIF-discovered cameras

The viewer flow now provides secure, camera-specific authentication while maintaining all existing security features and protocols.
