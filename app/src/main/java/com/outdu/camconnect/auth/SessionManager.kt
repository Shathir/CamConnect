package com.outdu.camconnect.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger

/**
 * Singleton class for managing user authentication and session tokens
 * Provides secure PIN-based authentication with persistent session storage
 */
object SessionManager {
    
    private const val TAG = "SessionManager"
    private const val PREFS_NAME = "cam_connect_session"
    private const val SESSION_TOKEN_KEY = "session_token"
    private const val LAST_AUTH_TIME_KEY = "last_auth_time"
    private const val PIN_ATTEMPTS_KEY = "pin_attempts"
    private const val LOCKOUT_START_TIME_KEY = "lockout_start_time"
    private const val LOCKOUT_SEQUENCE_COUNT_KEY = "lockout_sequence_count"
    
    // Configuration constants
    private const val MAX_PIN_ATTEMPTS = 3
    private const val SESSION_TIMEOUT_HOURS = 24
    private const val LOGIN_TIMEOUT_MS = 10_000L
    private const val LOGIN_ENDPOINT = "http://192.168.2.1:80/api/login"
    
    // Exponential backoff lockout durations in milliseconds
    private val LOCKOUT_DURATIONS = longArrayOf(
        30_000L,      // 30 seconds
        60_000L,      // 1 minute  
        120_000L,     // 2 minutes
        300_000L,     // 5 minutes
        900_000L,     // 15 minutes
        1_800_000L,   // 30 minutes
        3_600_000L,   // 1 hour
        7_200_000L,   // 2 hours
        14_400_000L   // 4 hours (max)
    )
    
    // In-memory state
    @Volatile private var currentSessionToken: String? = null
    @Volatile private var isInitialized = false
    private val pinAttempts = AtomicInteger(0)
    private var sharedPreferences: SharedPreferences? = null
    
    /**
     * Request/Response models for login API
     */
    @Serializable
    private data class LoginRequest(val pin: String)
    
    /**
     * Initialize SessionManager with application context
     * Must be called before using any other methods
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadStoredSession()
        loadPinAttempts()
        isInitialized = true
        
        Log.i(TAG, "SessionManager initialized")
    }
    
    /**
     * Authenticate user with 4-digit PIN
     * @param pin The 4-digit PIN to authenticate with
     * @return Result<Boolean> indicating success or failure with error details
     */
    suspend fun authenticateWithPin(pin: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Validate PIN format
            if (!isValidPin(pin)) {
                return@withContext Result.failure(
                    InvalidPinException("PIN must be exactly 4 digits")
                )
            }
            
            // Check if user is currently locked out
            val lockoutInfo = getCurrentLockoutInfo()
            if (lockoutInfo.isLockedOut) {
                return@withContext Result.failure(
                    MaxAttemptsExceededException("Account locked. Try again in ${formatLockoutTime(lockoutInfo.remainingTime)}")
                )
            }
            
            // Increment attempt counter
            pinAttempts.incrementAndGet()
            savePinAttempts()
            
            Log.d(TAG, "Attempting authentication with PIN (attempt ${pinAttempts.get()})")
            
            // Make login API call
            val result = performLogin(pin)
            
            if (result.isSuccess) {
                // Reset all attempt counters and lockout state on success
                resetAllAttemptCounters()
                Log.i(TAG, "Authentication successful")
            } else {
                // Check if max attempts reached for this sequence
                if (pinAttempts.get() >= MAX_PIN_ATTEMPTS) {
                    startLockoutPeriod()
                }
                Log.w(TAG, "Authentication failed: ${result.exceptionOrNull()?.message}")
            }
            
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during authentication", e)
            Result.failure(AuthenticationNetworkException("Unexpected authentication error", e))
        }
    }
    
    /**
     * Perform the actual login API call
     */
    private suspend fun performLogin(pin: String): Result<Boolean> {
        val httpClient = HttpClient(CIO) {
            engine {
                requestTimeout = LOGIN_TIMEOUT_MS
                endpoint {
                    connectTimeout = LOGIN_TIMEOUT_MS
                    connectAttempts = 1
                }
            }
        }
        
        return try {
            val response = httpClient.post(LOGIN_ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(LoginRequest(pin)))
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    // Extract session token from Set-Cookie header
                    val sessionToken = extractSessionToken(response.headers)
                    if (sessionToken != null) {
                        storeSession(sessionToken)
                        Result.success(true)
                    } else {
                        Result.failure(AuthenticationServerException("No session token in response"))
                    }
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(InvalidPinException("Invalid PIN provided"))
                }
                else -> {
                    Result.failure(
                        AuthenticationServerException("Server error: ${response.status}")
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error during login", e)
            Result.failure(AuthenticationNetworkException("Network error during login", e))
        } finally {
            httpClient.close()
        }
    }
    
    /**
     * Extract session token from response headers
     */
    private fun extractSessionToken(headers: Headers): String? {
        val setCookieHeaders = headers.getAll("Set-Cookie") ?: return null
        
        for (cookie in setCookieHeaders) {
            if (cookie.startsWith("session=")) {
                // Extract token value (format: session=TOKEN_VALUE; other attributes)
                val tokenPart = cookie.split(";")[0]
                return tokenPart.substringAfter("session=")
            }
        }
        return null
    }
    
    /**
     * Get current session token for API requests
     * @return Session token if authenticated, null otherwise
     */
    fun getSessionToken(): String? {
        if (!isSessionValid()) {
            return null
        }
        return currentSessionToken
    }
    
    /**
     * Get session cookie string for HTTP headers
     * @return Formatted session cookie or null if not authenticated
     */
    fun getSessionCookie(): String? {
        val token = getSessionToken()
        return if (token != null) "session=$token" else null
    }
    
    /**
     * Check if user is currently authenticated with valid session
     */
    fun isAuthenticated(): Boolean {
        return getSessionToken() != null
    }
    
    /**
     * Check if stored session is still valid (not expired)
     */
    private fun isSessionValid(): Boolean {
        if (currentSessionToken == null) return false
        
        val lastAuthTime = sharedPreferences?.getLong(LAST_AUTH_TIME_KEY, 0) ?: 0
        val currentTime = System.currentTimeMillis()
        val sessionExpiryTime = lastAuthTime + (SESSION_TIMEOUT_HOURS * 60 * 60 * 1000)
        
        return currentTime < sessionExpiryTime
    }
    
    /**
     * Store session token securely
     */
    private fun storeSession(token: String) {
        currentSessionToken = token
        sharedPreferences?.edit()
            ?.putString(SESSION_TOKEN_KEY, token)
            ?.putLong(LAST_AUTH_TIME_KEY, System.currentTimeMillis())
            ?.apply()
        
        Log.d(TAG, "Session token stored successfully")
    }
    
    /**
     * Load stored session from persistent storage
     */
    private fun loadStoredSession() {
        val storedToken = sharedPreferences?.getString(SESSION_TOKEN_KEY, null)
        if (storedToken != null && isSessionValid()) {
            currentSessionToken = storedToken
            Log.d(TAG, "Valid session loaded from storage")
        } else {
            // Clear invalid/expired session
            clearSession()
        }
    }
    
    /**
     * Clear current session and remove from storage
     */
    fun clearSession() {
        currentSessionToken = null
        sharedPreferences?.edit()
            ?.remove(SESSION_TOKEN_KEY)
            ?.remove(LAST_AUTH_TIME_KEY)
            ?.apply()
        
        Log.i(TAG, "Session cleared")
    }
    
    /**
     * Reset PIN attempt counter (useful for admin override or time-based reset)
     */
    fun resetPinAttempts() {
        resetAllAttemptCounters()
        Log.d(TAG, "PIN attempts reset (manual)")
    }
    
    /**
     * Get current PIN attempt count
     */
    fun getPinAttempts(): Int = pinAttempts.get()
    
    /**
     * Check if more PIN attempts are allowed (considering both attempt count and lockout time)
     */
    fun canAttemptPin(): Boolean {
        val lockoutInfo = getCurrentLockoutInfo()
        return !lockoutInfo.isLockedOut && pinAttempts.get() < MAX_PIN_ATTEMPTS
    }
    
    /**
     * Validate PIN format (4 digits)
     */
    private fun isValidPin(pin: String): Boolean {
        return pin.length == 4 && pin.all { it.isDigit() }
    }
    
    /**
     * Save PIN attempts to persistent storage
     */
    private fun savePinAttempts() {
        sharedPreferences?.edit()
            ?.putInt(PIN_ATTEMPTS_KEY, pinAttempts.get())
            ?.apply()
    }
    
    /**
     * Load PIN attempts from persistent storage
     */
    private fun loadPinAttempts() {
        val attempts = sharedPreferences?.getInt(PIN_ATTEMPTS_KEY, 0) ?: 0
        pinAttempts.set(attempts)
    }
    
    /**
     * Get current lockout information
     */
    fun getCurrentLockoutInfo(): LockoutInfo {
        val lockoutStartTime = sharedPreferences?.getLong(LOCKOUT_START_TIME_KEY, 0L) ?: 0L
        val lockoutSequenceCount = sharedPreferences?.getInt(LOCKOUT_SEQUENCE_COUNT_KEY, 0) ?: 0
        
        if (lockoutStartTime == 0L) {
            return LockoutInfo(false, 0L, 0)
        }
        
        val currentTime = System.currentTimeMillis()
        val lockoutDuration = getLockoutDuration(lockoutSequenceCount)
        val elapsedTime = currentTime - lockoutStartTime
        
        if (elapsedTime >= lockoutDuration) {
            // Lockout period has expired, clear lockout state but keep sequence count
            clearLockoutState()
            return LockoutInfo(false, 0L, lockoutSequenceCount)
        }
        
        val remainingTime = lockoutDuration - elapsedTime
        return LockoutInfo(true, remainingTime, lockoutSequenceCount)
    }
    
    /**
     * Start a lockout period with exponential backoff
     */
    private fun startLockoutPeriod() {
        val currentSequenceCount = sharedPreferences?.getInt(LOCKOUT_SEQUENCE_COUNT_KEY, 0) ?: 0
        val newSequenceCount = currentSequenceCount + 1
        val lockoutDuration = getLockoutDuration(newSequenceCount)
        
        sharedPreferences?.edit()
            ?.putLong(LOCKOUT_START_TIME_KEY, System.currentTimeMillis())
            ?.putInt(LOCKOUT_SEQUENCE_COUNT_KEY, newSequenceCount)
            ?.apply()
        
        // Reset current attempt counter for this sequence
        pinAttempts.set(0)
        savePinAttempts()
        
        Log.w(TAG, "Lockout started. Sequence: $newSequenceCount, Duration: ${formatLockoutTime(lockoutDuration)}")
    }
    
    /**
     * Get lockout duration for given sequence count (exponential backoff)
     */
    private fun getLockoutDuration(sequenceCount: Int): Long {
        if (sequenceCount <= 0) return 0L
        val index = minOf(sequenceCount - 1, LOCKOUT_DURATIONS.size - 1)
        return LOCKOUT_DURATIONS[index]
    }
    
    /**
     * Clear lockout state (called when lockout period expires)
     */
    private fun clearLockoutState() {
        sharedPreferences?.edit()
            ?.remove(LOCKOUT_START_TIME_KEY)
            ?.apply()
        
        // Reset current attempt counter when lockout expires
        pinAttempts.set(0)
        savePinAttempts()
        
        Log.d(TAG, "Lockout period expired, attempt counter reset")
    }
    
    /**
     * Reset all attempt counters and lockout state (called on successful authentication)
     */
    private fun resetAllAttemptCounters() {
        pinAttempts.set(0)
        sharedPreferences?.edit()
            ?.remove(LOCKOUT_START_TIME_KEY)
            ?.remove(LOCKOUT_SEQUENCE_COUNT_KEY)
            ?.putInt(PIN_ATTEMPTS_KEY, 0)
            ?.apply()
        
        Log.d(TAG, "All attempt counters and lockout state reset")
    }
    
    /**
     * Format lockout time for user display
     */
    private fun formatLockoutTime(timeMs: Long): String {
        val seconds = timeMs / 1000
        return when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
        }
    }
    
    /**
     * Get session status for debugging/monitoring
     */
    fun getSessionStatus(): String {
        val lockoutInfo = getCurrentLockoutInfo()
        return buildString {
            append("Authenticated: ${isAuthenticated()}")
            append(", PIN Attempts: ${pinAttempts.get()}/$MAX_PIN_ATTEMPTS")
            append(", Session Valid: ${isSessionValid()}")
            if (lockoutInfo.isLockedOut) {
                append(", Locked Out: ${formatLockoutTime(lockoutInfo.remainingTime)} remaining")
                append(", Sequence: ${lockoutInfo.sequenceCount}")
            }
            if (currentSessionToken != null) {
                append(", Token Length: ${currentSessionToken!!.length}")
            }
        }
    }
}

/**
 * Data class representing current lockout information
 */
data class LockoutInfo(
    val isLockedOut: Boolean,
    val remainingTime: Long, // in milliseconds
    val sequenceCount: Int
) 