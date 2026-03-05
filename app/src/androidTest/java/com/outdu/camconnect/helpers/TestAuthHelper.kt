package com.outdu.camconnect.helpers

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.outdu.camconnect.auth.SessionManager
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue

/**
 * Helper class for authenticating with real camera in instrumented tests.
 * 
 * Usage in test classes:
 * ```
 * @Before
 * fun setup() {
 *     TestAuthHelper.authenticateIfNeeded()
 * }
 * ```
 */
object TestAuthHelper {
    
    private const val TAG = "TestAuthHelper"
    
    // Real camera credentials - configure these for your test environment
    const val TEST_CAMERA_IP = "192.168.2.1"
    const val TEST_CAMERA_PIN = "1111"
    
    // Track if we've already authenticated in this test session
    @Volatile
    private var isAuthenticated = false
    @Volatile
    private var authenticationAttempted = false
    
    /**
     * Authenticate with the test camera if not already authenticated.
     * Skips the test if authentication fails (camera not available).
     * 
     * Call this in @Before of test classes that need real camera access.
     */
    fun authenticateIfNeeded() {
        if (isAuthenticated) {
            Log.d(TAG, "Already authenticated, skipping")
            return
        }
        
        if (authenticationAttempted) {
            // Previous attempt failed, skip this test
            assumeTrue("Camera authentication failed in previous attempt (skip test)", false)
            return
        }
        
        authenticationAttempted = true
        
        val context: Context = ApplicationProvider.getApplicationContext()
        
        try {
            // Initialize SessionManager
            SessionManager.initialize(context)
            
            // Check if already authenticated from previous test
            if (SessionManager.isAuthenticated()) {
                Log.i(TAG, "Session already exists from previous test")
                isAuthenticated = true
                return
            }
            
            Log.i(TAG, "Authenticating with camera at $TEST_CAMERA_IP...")
            
            // Perform authentication
            val result = runBlocking {
                SessionManager.authenticateWithPin(TEST_CAMERA_PIN, TEST_CAMERA_IP)
            }
            
            if (result.isSuccess) {
                isAuthenticated = true
                val token = SessionManager.getSessionToken()
                Log.i(TAG, "✓ Authentication successful! Token length: ${token?.length ?: 0}")
                Log.i(TAG, "Session status: ${SessionManager.getSessionStatus()}")
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                Log.w(TAG, "✗ Authentication failed: $error")
                Log.w(TAG, "Skipping tests that require camera connection")
                
                // Skip this test since camera is not available
                assumeTrue("Camera not available at $TEST_CAMERA_IP (skip test): $error", false)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during authentication", e)
            assumeTrue("Camera authentication exception (skip test): ${e.message}", false)
        }
    }
    
    /**
     * Check if currently authenticated with test camera.
     */
    fun isAuthenticatedWithCamera(): Boolean {
        return isAuthenticated && SessionManager.isAuthenticated()
    }
    
    /**
     * Get the session token for API requests.
     * Returns null if not authenticated.
     */
    fun getSessionToken(): String? {
        return if (isAuthenticated) SessionManager.getSessionToken() else null
    }
    
    /**
     * Get the session cookie string for HTTP headers.
     */
    fun getSessionCookie(): String? {
        return if (isAuthenticated) SessionManager.getSessionCookie() else null
    }
    
    /**
     * Clear authentication state (useful for logout tests).
     */
    fun clearAuthentication() {
        SessionManager.clearSession()
        isAuthenticated = false
        Log.i(TAG, "Authentication cleared")
    }
    
    /**
     * Reset authentication state for new test run.
     * Call this in @After if you want each test to re-authenticate.
     */
    fun resetForNextTest() {
        authenticationAttempted = false
        // Keep isAuthenticated true to reuse session across tests
    }
    
    /**
     * Force re-authentication on next test.
     * Useful when testing logout/login flows.
     */
    fun forceReauthentication() {
        clearAuthentication()
        authenticationAttempted = false
        isAuthenticated = false
    }
    
    /**
     * Get test camera IP address.
     */
    fun getCameraIp(): String = TEST_CAMERA_IP
    
    /**
     * Get test camera PIN.
     */
    fun getCameraPin(): String = TEST_CAMERA_PIN
}
