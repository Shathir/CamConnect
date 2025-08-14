# HTTP Communication Architecture Documentation

## Overview
This document details the complete HTTP communication flow used in the Motocam API system, including request construction, header management, data conversion, and response parsing.

## HTTP Client Configuration

### Ktor HTTP Client Setup
The system uses Ktor CIO (Coroutine-based I/O) HTTP client for all communications:

```kotlin
httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
    engine {
        requestTimeout = 10_000L        // 10 second timeout
        endpoint {
            connectTimeout = 10_000L    // 10 second connection timeout
            connectAttempts = 1         // Single connection attempt
        }
    }
}
```

### Client Features
- **Engine**: CIO (Coroutine I/O) - High-performance async networking
- **Timeout**: 10 seconds for both request and connection
- **Content Negotiation**: JSON support for API responses
- **Connection Strategy**: Single attempt with fast failure

## Request Construction Flow

### 1. Command Creation (Integer Array)
Commands are initially created as integer arrays using the MotocamAPIHelper:

```java
// Example: setMiscCmd(int val)
public static int[] setMiscCmd(int val) throws Exception {
    int cmd[] = new int[6];
    cmd[0] = Header.SET.getVal();           // 1 (SET command)
    cmd[1] = Commands.IMAGE.getVal();       // 4 (IMAGE command)
    cmd[2] = ImageSubCommands.MISC.getVal(); // 13 (MISC subcommand)
    cmd[3] = 1;                             // Data length
    cmd[4] = val;                           // MISC value (1-11)
    cmd[5] = 0;                             // CRC (calculated later)
    return cmd;
}
```

### 2. Integer Array to Byte Array Conversion
The integer array is converted to a byte array for HTTP transmission:

```kotlin
private fun convert(intArray: IntArray): ByteArray {
    return ByteArray(intArray.size) { i -> intArray[i].toByte() }
}
```

**Conversion Process**:
- Each integer is cast to byte (truncated to 8 bits)
- Maintains array structure and order
- Values > 255 are truncated (& 0xFF operation)

### 3. CRC Calculation
A Cyclic Redundancy Check is calculated and appended:

```kotlin
private fun calcCRC(reqBytes: ByteArray): Byte {
    val sum = reqBytes.dropLast(1).sumOf { it.toInt() and 0xFF }
    return ((sum xor 0xFF) + 1).toByte()
}
```

**CRC Algorithm**:
1. Sum all bytes except the last (CRC) position
2. Apply XOR with 0xFF (bitwise NOT)
3. Add 1 (two's complement)
4. Result becomes the CRC byte

### 4. Hex String Formatting
The byte array is formatted as a space-separated hex string:

```kotlin
private fun formatToHexString(byteArray: ByteArray): String {
    return byteArray.joinToString(" ") { byte ->
        "0x" + (byte.toInt() and 0xFF).toString(16).padStart(2, '0').uppercase()
    }
}
```

**Format Example**:
- Input: `[1, 4, 13, 1, 5, 242]`
- Output: `"0x01 0x04 0x0D 0x01 0x05 0xF2"`

## HTTP Request Structure

### Request Headers
```kotlin
headers {
    append(HttpHeaders.ContentType, "application/octet-stream")
    append(HttpHeaders.Cookie, getSessionCookie())
}
```

### Header Details
| Header | Value | Purpose |
|--------|-------|---------|
| `Content-Type` | `application/octet-stream` | Indicates binary data |
| `Cookie` | `session=<session_token>` | Authentication |

### Session Management
```kotlin
private fun getSessionCookie(): String {
    val dynamicSession = SessionManager.getSessionCookie()
    return if (dynamicSession != null) {
        Log.d(TAG, "Using dynamic session token")
        dynamicSession
    } else {
        Log.d(TAG, "Using default session token (fallback)")
        "session=$DEFAULT_SESSION"
    }
}
```

**Session Strategy**:
- **Primary**: Dynamic session from SessionManager (after login)
- **Fallback**: Default session token for development/testing
- **Format**: `session=<64-character-hex-string>`

### Complete HTTP Request
```kotlin
val responseText: String = client.post(url) {
    contentType(ContentType.Text.Plain)
    setBody(hexString)
    
    headers {
        append(HttpHeaders.ContentType, "application/octet-stream")
        append(HttpHeaders.Cookie, getSessionCookie())
    }
}.body()
```

**Request Properties**:
- **Method**: POST
- **URL**: `http://192.168.2.1:80/api/motocam_api`
- **Body**: Hex string (e.g., `"0x01 0x04 0x0D 0x01 0x05 0xF2"`)
- **Content-Type**: Dual specification (Text.Plain + application/octet-stream)

## Response Processing Flow

### 1. Raw Response Reception
The server returns a space-separated hex string:
```
Example Response: "0x03 0x04 0x0D 0x02 0x00 0x00 0xF4"
```

### 2. Hex String Parsing
```kotlin
fun parseHexStringToByteArray(hexString: String): ByteArray {
    return hexString
        .trim()
        .split(Regex("\\s+"))
        .map { token ->
            require(token.startsWith("0x", ignoreCase = true)) { "Invalid token: $token" }
            token.removePrefix("0x").toInt(16).toByte()
        }
        .toByteArray()
}
```

**Parsing Process**:
1. Trim whitespace and split by spaces
2. Validate each token starts with "0x"
3. Remove "0x" prefix and parse as hexadecimal
4. Convert to byte array

### 3. CRC Validation
```kotlin
private fun validateCRC(response: ByteArray, length: Int): Boolean {
    val sum = response.take(length).sumOf { it.toInt() and 0xFF }
    return (sum and 0xFF).toByte() == 0.toByte()
}
```

**Validation Logic**:
- Sum all bytes in the response (including CRC)
- Valid response: sum & 0xFF == 0
- Invalid CRC throws exception

### 4. Byte Array to Integer Array Conversion
```kotlin
private fun convert(byteArray: ByteArray, intArray: IntArray) {
    for (i in byteArray.indices) {
        intArray[i] = byteArray[i].toInt() and 0xFF
    }
}
```

**Conversion Process**:
- Convert each byte to unsigned integer (& 0xFF)
- Populate provided integer array
- Maintains response structure for parsing

## Complete Example: setMisc API Call

### Step-by-Step Flow

#### 1. Command Construction
```kotlin
// User calls: setMiscAsync(scope, miscValue = 5, callback)
// This triggers: MotocamAPIHelper.setMiscCmd(5)

val cmd = [1, 4, 13, 1, 5, 0]  // Initial integer array
```

#### 2. Conversion to Bytes
```kotlin
val reqBytes = convert(cmd)
// Result: [1, 4, 13, 1, 5, 0] (as bytes)
```

#### 3. CRC Calculation
```kotlin
val crc = calcCRC(reqBytes)
// Sum: 1 + 4 + 13 + 1 + 5 = 24
// CRC: (24 XOR 255) + 1 = 232
reqBytes[5] = 232  // 0xE8
// Final: [1, 4, 13, 1, 5, 232]
```

#### 4. Hex String Formatting
```kotlin
val hexString = formatToHexString(reqBytes)
// Result: "0x01 0x04 0x0D 0x01 0x05 0xE8"
```

#### 5. HTTP Request
```http
POST http://192.168.2.1:80/api/motocam_api
Content-Type: application/octet-stream
Cookie: session=E5F102590722B5788B9CE04885ED845A3CA815E93753B2D7885C86DC5BB4647A

0x01 0x04 0x0D 0x01 0x05 0xE8
```

#### 6. HTTP Response
```http
HTTP/1.1 200 OK
Content-Type: text/plain

0x03 0x04 0x0D 0x02 0x00 0x00 0xF4
```

#### 7. Response Parsing
```kotlin
// Parse hex string to bytes
val responseBytes = parseHexStringToByteArray("0x03 0x04 0x0D 0x02 0x00 0x00 0xF4")
// Result: [3, 4, 13, 2, 0, 0, 244]

// Validate CRC
val isValid = validateCRC(responseBytes, 7)
// Sum: 3 + 4 + 13 + 2 + 0 + 0 + 244 = 266
// 266 & 0xFF = 10 ≠ 0, so CRC validation would fail
// (This is example data; real response would have valid CRC)

// Convert to integer array
val res = IntArray(256)
convert(responseBytes, res)
// Result: res[0]=3, res[1]=4, res[2]=13, res[3]=2, res[4]=0, res[5]=0
```

#### 8. Response Structure Analysis
```kotlin
// Response packet structure:
// [0] = 3    (ACK header)
// [1] = 4    (IMAGE command)
// [2] = 13   (MISC subcommand)
// [3] = 2    (Data length)
// [4] = 0    (Success flag)
// [5] = 0    (Success data)
// [6] = CRC  (Checksum)
```

## Error Handling

### Common Error Scenarios

#### 1. Connection Timeout
```kotlin
catch (e: Exception) {
    Log.e(TAG, "sendCmd failed", e)
    throw e
}
```

#### 2. Invalid CRC Response
```kotlin
if (responseText.isEmpty() || !validateCRC(responseBytes, responseBytes.size)) {
    throw Exception("Invalid CRC or empty response")
}
```

#### 3. Session Authentication Failure
```http
HTTP/1.1 401 Unauthorized
```

#### 4. Malformed Request
```http
HTTP/1.1 415 Unsupported Media Type
```

### Error Code Mapping
Based on the response packet error codes:

| Error Code | Description |
|------------|-------------|
| -1 | Error in executing the command |
| -2 | Invalid packet header |
| -3 | Invalid command |
| -4 | Invalid sub-command |
| -5 | Invalid Data/Data Length |
| -6 | CRC does not match |

## Performance Characteristics

### Network Configuration
- **Protocol**: HTTP/1.1 over TCP
- **Connection**: Keep-alive with timeout
- **Payload Size**: Typically 5-10 bytes (commands)
- **Response Time**: ~50-200ms on local network

### Optimization Features
- **Coroutine-based**: Non-blocking async operations
- **Connection Pooling**: Ktor handles connection reuse
- **Timeout Management**: Fast failure for unresponsive devices
- **Session Persistence**: Cookie-based authentication

### Threading Model
```kotlin
suspend fun sendCmd(req: IntArray, res: IntArray): Int = withContext(Dispatchers.IO) {
    // All HTTP operations run on IO dispatcher
    // Non-blocking for UI thread
}
```

## Security Considerations

### Authentication
- **Session Cookies**: Secure session management
- **Token Validation**: Server-side session validation
- **Timeout**: 1-hour session expiration

### Data Integrity
- **CRC Validation**: Both request and response validation
- **Binary Protocol**: Compact, less prone to injection
- **Structured Commands**: Fixed packet format

### Network Security
- **Local Network**: Operates on 192.168.2.1 (device network)
- **HTTP Protocol**: Unencrypted (local trust model)
- **Session Cookies**: HTTP-only flag for client security

---
*This documentation provides comprehensive coverage of the HTTP communication architecture used in the Motocam API system, including all conversion processes, error handling, and security considerations.* 