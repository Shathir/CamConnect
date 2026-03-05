package com.outdu.camconnect.auth

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for SessionManager
 * Tests authentication, session management, and persistence
 */
@RunWith(AndroidJUnit4::class)
class SessionManagerInstrumentedTest {

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SessionManager.initialize(context)
        runBlocking {
            SessionManager.logout() // Clear any existing session
        }
    }

    @Test
    fun testSessionManagerInitialization() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SessionManager.initialize(context)
        
        // Should not throw exception
        assertTrue("SessionManager should initialize successfully", true)
    }

    @Test
    fun testInitialAuthenticationState() {
        assertFalse("Should not be authenticated initially", 
            SessionManager.isAuthenticated())
    }

    @Test
    fun testGetSessionTokenInitially() {
        assertNull("Should have no session token initially", 
            SessionManager.getSessionToken())
    }

    @Test
    fun testAuthenticationWithInvalidPin() {
        runBlocking {
            val result = SessionManager.authenticateWithPin("invalid")
            
            assertFalse("Authentication should fail with invalid PIN", 
                result.isSuccess || result.getOrDefault(false))
        }
    }

    @Test
    fun testGetCurrentLockoutInfo() {
        val lockoutInfo = SessionManager.getCurrentLockoutInfo()
        
        assertNotNull("Lockout info should not be null", lockoutInfo)
        assertFalse("Should not be locked out initially", lockoutInfo.isLockedOut)
    }

    @Test
    fun testGetLastConnectedCameraIp() {
        val ip = SessionManager.getLastConnectedCameraIp()
        
        // Should return null or a valid IP string
        assertTrue("IP should be null or string", ip == null || ip is String)
    }

    @Test
    fun testLogoutClearsSession() {
        runBlocking {
            val result = SessionManager.logout()
            
            assertTrue("Logout should succeed", result.isSuccess)
            assertFalse("Should not be authenticated after logout", 
                SessionManager.isAuthenticated())
            assertNull("Token should be cleared after logout", 
                SessionManager.getSessionToken())
        }
    }

    @Test
    fun testGetSessionCookie() {
        val cookie = SessionManager.getSessionCookie()
        
        // Should return null initially
        assertNull("Cookie should be null initially", cookie)
    }

    @Test
    fun testLockoutInfoStructure() {
        val lockoutInfo = SessionManager.getCurrentLockoutInfo()
        
        assertNotNull("Lockout info should have isLockedOut property", lockoutInfo.isLockedOut)
        assertTrue("Remaining time should be non-negative", 
            lockoutInfo.remainingTime >= 0)
        assertTrue("Sequence count should be non-negative", 
            lockoutInfo.sequenceCount >= 0)
    }

    @Test
    fun testMultipleLogoutCallsSafe() {
        runBlocking {
            SessionManager.logout()
            SessionManager.logout()
            SessionManager.logout()
            
            // Should not throw exception
            assertTrue("Multiple logout calls should be safe", true)
        }
    }

    @Test
    fun testAuthenticationStateConsistency() {
        val isAuth1 = SessionManager.isAuthenticated()
        val token1 = SessionManager.getSessionToken()
        
        // Authentication state should be consistent with token presence
        assertEquals("Authentication state should match token presence",
            token1 != null, isAuth1)
    }
}
