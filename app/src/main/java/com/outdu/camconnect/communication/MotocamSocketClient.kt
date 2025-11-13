package com.outdu.camconnect.communication

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64
import com.outdu.camconnect.auth.SessionManager
import java.security.MessageDigest

class MotocamSocketClient {

    companion object {
        private const val TAG = "MotocamSocketClient"
        private const val TIMEOUT_MS = 30_000
        private const val DEFAULT_SESSION = "E5F102590722B5788B9CE04885ED845A3CA815E93753B2D7885C86DC5BB4647A"
        private const val DEFAULT_CAMERA_IP = "192.168.2.1"
    }

    private var httpClient: HttpClient? = null
    private var cameraIp: String = DEFAULT_CAMERA_IP

    suspend fun checkDevice(ipAddress: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = "http://$ipAddress:8080/ping"
            val response = HttpClient(CIO) {
                expectSuccess = false
            }.get(url)
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Log.e(TAG, "checkDevice error", e)
            false
        }
    }

    fun init(cameraIp: String? = null) {
        // Set camera IP if provided and not empty, otherwise use default
        if (!cameraIp.isNullOrEmpty()) {
            this.cameraIp = cameraIp
            Log.i(TAG, "Camera IP set to: $cameraIp")
        } else {
            this.cameraIp = DEFAULT_CAMERA_IP
            Log.i(TAG, "Using default camera IP: $DEFAULT_CAMERA_IP")
        }
        
        httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
            engine {
                requestTimeout = TIMEOUT_MS.toLong()
                endpoint {
                    connectTimeout = TIMEOUT_MS.toLong()
                    connectAttempts = 1
                }
            }
        }
        Log.i(TAG, "HTTP client initialized for camera: ${this.cameraIp}")
    }
    
    /**
     * Set camera IP address for API calls
     */
    fun setCameraIp(ipAddress: String) {
        this.cameraIp = ipAddress
        Log.i(TAG, "Camera IP updated to: $ipAddress")
    }
    
    /**
     * Get current camera IP address
     */
    fun getCameraIp(): String = cameraIp

    private fun convert(intArray: IntArray): ByteArray {
        return ByteArray(intArray.size) { i -> intArray[i].toByte() }
    }

    private fun convert(byteArray: ByteArray, intArray: IntArray) {
        for (i in byteArray.indices) {
            intArray[i] = byteArray[i].toInt() and 0xFF
        }
    }

    private fun calcCRC(reqBytes: ByteArray): Byte {
        val sum = reqBytes.dropLast(1).sumOf { it.toInt() and 0xFF }
        return ((sum xor 0xFF) + 1).toByte()
    }

    private fun validateCRC(response: ByteArray, length: Int): Boolean {
        val sum = response.take(length).sumOf { it.toInt() and 0xFF }
        return (sum and 0xFF).toByte() == 0.toByte()
    }

    private fun formatToHexString(byteArray: ByteArray): String {
        return byteArray.joinToString(" ") { byte ->
            "0x" + (byte.toInt() and 0xFF).toString(16).padStart(2, '0').uppercase()
        }
    }

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

    /**
     * Get session cookie for API requests
     * Uses dynamic session from SessionManager if available, otherwise falls back to default
     */
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

    suspend fun sendCmd(req: IntArray, res: IntArray): Int = withContext(Dispatchers.IO) {
        val client = httpClient ?: throw IllegalStateException("HTTP client not initialized")

        val reqBytes = convert(req).also {
            it[it.size - 1] = calcCRC(it)
        }

        Log.i("MotocamSocketClient", "reqBytes: ${reqBytes.contentToString()}")
        val hexString = formatToHexString(reqBytes)
        Log.i("MotocamSocketClient", "input command: $hexString")
        val url = "http://$cameraIp:80/api/motocam_api"

        try {
            val responseText: String = client.post(url) {
                contentType(ContentType.Text.Plain)
                setBody(hexString)

                headers {
                    append(HttpHeaders.ContentType, "application/octet-stream")
                    append(HttpHeaders.Cookie, getSessionCookie())
                }

            }.body()

            Log.i("MotocamSocketClient", "responseText: $responseText")
            val responseBytes = parseHexStringToByteArray(responseText)
            if (responseText.isEmpty() || !validateCRC(responseBytes, responseBytes.size)) {
                throw Exception("Invalid CRC or empty response")
            }

            // Convert response bytes to integer array
            convert(responseBytes, res)
            Log.i("MotocamSocketClient", "responseBytes: ${responseBytes.contentToString()}")
            Log.i("MotocamSocketClient", "res after conversion: ${res.sliceArray(0..minOf(responseBytes.size-1, res.size-1)).contentToString()}")
            return@withContext responseBytes.size
        } catch (e: Exception) {
            Log.e(TAG, "sendCmd failed", e)
            throw e
        }
    }

    /**
     * Upload a file using multipart/form-data to /api/upload
     * @param fileName name reported to the server (Content-Disposition filename)
     * @param fileBytes content of the file to upload
     * @param port target HTTP port (default 80). Example external UI may use 8082
     * @param fieldName multipart field name expected by server (default "file")
     * @param contentType MIME type of the file content
     * @return true if HTTP 200-299, false otherwise
     */
    suspend fun uploadFile(
        fileName: String,
        fileBytes: ByteArray,
        port: Int = 80,
        fieldName: String = "file",
        contentType: ContentType = ContentType.Application.OctetStream
    ): Boolean = withContext(Dispatchers.IO) {

        Log.d("MotocamSocketClient", "uploadFile called with fileName: $fileName, port: $port, fieldName: $fieldName, contentType: $contentType")
        val client = httpClient ?: throw IllegalStateException("HTTP client not initialized")

        Log.d(TAG,"cameraIp : ${cameraIp}")
        val url = "http://$cameraIp:$port/api/upload"
        Log.d(TAG, "upload url: $url")

        // Log payload characteristics (size, short hex preview, SHA-256 hash)
        val totalBytes = fileBytes.size
        val previewCount = kotlin.math.min(32, totalBytes)
        val previewHex = fileBytes.take(previewCount).joinToString(" ") { b ->
            (b.toInt() and 0xFF).toString(16).padStart(2, '0').uppercase()
        }
        val sha256 = MessageDigest.getInstance("SHA-256").digest(fileBytes)
            .joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
        Log.d(TAG, "payload sizeBytes=$totalBytes sha256=$sha256 previewHex[${previewCount}B]=$previewHex")
        try {
            val response = client.submitFormWithBinaryData(
                url = url,
                formData = formData {
                    append(
                        key = fieldName,
                        value = fileBytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentDisposition, "form-data; name=\"$fieldName\"; filename=\"$fileName\"")
                        }
                    )
                }
            ) {
                headers {
                    append(HttpHeaders.Cookie, getSessionCookie())
                    append(HttpHeaders.UserAgent, "CamConnect-Android")
                    append(HttpHeaders.Accept, "*/*")
                }
            }

            val ok = response.status.isSuccess()
            Log.d(TAG, "upload response status=${response.status}")
            
            // Log response body for debugging
            try {
                val responseBody = response.body<String>()
                Log.d(TAG, "upload response body: $responseBody")
            } catch (e: Exception) {
                Log.d(TAG, "Could not read response body: ${e.message}")
            }
            
            if (!ok) {
                Log.w(TAG, "uploadFile failed: status=${response.status}")
            }
            return@withContext ok
        } catch (e: Exception) {
            Log.e(TAG, "uploadFile exception", e)
            throw e
        }
    }

    /**
     * Test login API connectivity (useful for validation during setup)
     */
    suspend fun testLoginApiConnectivity(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val client = httpClient ?: throw IllegalStateException("HTTP client not initialized")
            val url = "http://$cameraIp:80/api/login"
            
            // Make a simple HEAD request to check if login endpoint is available
            val response = client.request(url) {
                method = HttpMethod.Head
            }
            
            response.status == HttpStatusCode.OK || 
            response.status == HttpStatusCode.MethodNotAllowed || // Some servers don't support HEAD
            response.status == HttpStatusCode.BadRequest // Expected for no body
        } catch (e: Exception) {
            Log.w(TAG, "Login API connectivity test failed", e)
            false
        }
    }

    fun destroy() {
        httpClient?.close()
        httpClient = null
        Log.i(TAG, "HTTP client destroyed")
    }
}
