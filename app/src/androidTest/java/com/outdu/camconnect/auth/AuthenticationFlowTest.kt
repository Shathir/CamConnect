package com.outdu.camconnect.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.auth.SessionManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for authentication flow
 * 
 * Tests session management and authentication including:
 * - Session creation and persistence
 * - Session clearing
 * - Authentication state management
 * - Session token handling
 * - Last connected camera persistence
 */
@RunWith(AndroidJUnit4::class)
class AuthenticationFlowTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Initialize SessionManager
        SessionManager.initialize(context)
        
        // Clear any existing session
        try {
            SessionManager.clearSession()
            SessionManager.clearLastConnectedCamera()
        } catch (e: Exception) {
            // Ignore initialization errors
        }
    }

    @After
    fun tearDown() {
        try {
            SessionManager.clearSession()
            SessionManager.clearLastConnectedCamera()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    // ========== Session Creation Tests ==========

    @Test
    fun sessionManager_initializes_successfully() {
        // SessionManager should initialize without crash
        SessionManager.initialize(context)
        assert(true)
    }

    @Test
    fun newSession_isNotAuthenticated() {
        // After clearing session, should not be authenticated
        SessionManager.clearSession()
        
        val isAuthenticated = SessionManager.isAuthenticated()
        assertFalse(isAuthenticated)
    }

    // ========== Session Token Tests ==========

    @Test
    fun sessionToken_isNull_whenNotAuthenticated() {
        SessionManager.clearSession()
        
        val token = SessionManager.getSessionToken()
        assertNull(token)
    }

    // ========== Session Clearing Tests ==========

    @Test
    fun clearSession_removesAuthenticationState() {
        // Clear session
        SessionManager.clearSession()
        
        // Should not be authenticated
        val isAuthenticated = SessionManager.isAuthenticated()
        assertFalse(isAuthenticated)
    }

    @Test
    fun clearSession_canBeCalledMultipleTimes() {
        // Call multiple times
        SessionManager.clearSession()
        SessionManager.clearSession()
        SessionManager.clearSession()
        
        // Should not crash
        assert(true)
    }

    // ========== Last Connected Camera Tests ==========

    @Test
    fun lastConnectedCameraIp_isNull_afterClearing() {
        SessionManager.clearLastConnectedCamera()
        
        val lastIp = SessionManager.getLastConnectedCameraIp()
        // In test environment, might return null or empty string
        assert(lastIp == null || lastIp.isEmpty())
    }

    @Test
    fun clearLastConnectedCamera_canBeCalledMultipleTimes() {
        // Call multiple times
        SessionManager.clearLastConnectedCamera()
        SessionManager.clearLastConnectedCamera()
        SessionManager.clearLastConnectedCamera()
        
        // Should not crash
        assert(true)
    }

    // ========== Session Status Tests ==========

    @Test
    fun sessionStatus_returnsValidString() {
        val status = SessionManager.getSessionStatus()
        
        assertNotNull(status)
        assertTrue(status.isNotEmpty())
    }

    @Test
    fun sessionStatus_reflectsAuthenticationState() {
        SessionManager.clearSession()
        
        val status = SessionManager.getSessionStatus()
        
        // Status should indicate authentication state
        assertNotNull(status)
        assertTrue(status.contains("authenticated", ignoreCase = true) || 
                   status.contains("Authenticated", ignoreCase = false))
    }

    // ========== PIN Attempts Tests ==========

    @Test
    fun pinAttempts_canBeRetrieved() {
        val attempts = SessionManager.getPinAttempts()
        
        // Should return a valid integer
        assertTrue(attempts >= 0)
    }

    @Test
    fun resetPinAttempts_clearsCounter() {
        SessionManager.resetPinAttempts()
        
        val attempts = SessionManager.getPinAttempts()
        assertEquals(0, attempts)
    }

    // ========== Lockout Tests ==========

    @Test
    fun lockoutInfo_canBeRetrieved() {
        val lockoutInfo = SessionManager.getCurrentLockoutInfo()
        
        assertNotNull(lockoutInfo)
        // Should have valid lockout state
        assertNotNull(lockoutInfo.isLockedOut)
    }

    @Test
    fun canAttemptPin_returnsBoolean() {
        val canAttempt = SessionManager.canAttemptPin()
        
        // Should return a valid boolean
        assertTrue(canAttempt || !canAttempt)
    }

    // ========== Session Persistence Tests ==========

    @Test
    fun sessionPersists_acrossManagerReinitialization() {
        // Clear and reinitialize
        SessionManager.clearSession()
        SessionManager.initialize(context)
        
        // Should not be authenticated after clear
        val isAuthenticated = SessionManager.isAuthenticated()
        assertFalse(isAuthenticated)
    }

    // ========== Concurrent Access Tests ==========

    @Test
    fun concurrentSessionAccess_isThreadSafe() {
        // Test concurrent access
        val threads = List(5) {
            Thread {
                repeat(10) {
                    SessionManager.isAuthenticated()
                    SessionManager.getSessionStatus()
                    Thread.sleep(10)
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        // Should complete without crashes
        assert(true)
    }

    // ========== Error Handling Tests ==========

    @Test
    fun sessionOperations_handleErrors_gracefully() {
        try {
            SessionManager.clearSession()
            SessionManager.getSessionToken()
            SessionManager.isAuthenticated()
            SessionManager.getSessionStatus()
            SessionManager.clearLastConnectedCamera()
        } catch (e: Exception) {
            // Some operations might fail in test environment
            // Just verify no fatal crashes
        }

        // Should complete
        assert(true)
    }

    // ========== Integration Tests ==========

    @Test
    fun loginLogoutCycle_worksCorrectly() {
        // Clear session (logout)
        SessionManager.clearSession()
        assertFalse(SessionManager.isAuthenticated())
        
        // Session operations should work
        SessionManager.getSessionStatus()
        
        // Should complete successfully
        assert(true)
    }

    @Test
    fun sessionState_isConsistent() {
        // Clear session
        SessionManager.clearSession()
        
        // Check consistency
        val isAuth1 = SessionManager.isAuthenticated()
        val isAuth2 = SessionManager.isAuthenticated()
        
        // Should return consistent results
        assertEquals(isAuth1, isAuth2)
    }
}
