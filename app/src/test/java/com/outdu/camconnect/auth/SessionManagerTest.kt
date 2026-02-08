package com.outdu.camconnect.auth

import com.outdu.camconnect.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for SessionManager
 * 
 * Tests cover public methods that can be reliably tested with a singleton:
 * - PIN attempt tracking
 * - Session status checking
 * - Lockout information
 * 
 * Note: Full testing of SessionManager is limited due to:
 * - Singleton pattern with internal state
 * - HTTP client dependencies requiring extensive mocking
 * - SharedPreferences state management
 * 
 * For comprehensive SessionManager testing, consider:
 * - Refactoring to dependency injection pattern
 * - Integration tests in androidTest
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ========== Session Management Tests ==========

    @Test
    fun `isAuthenticated should return false initially`() {
        // Act - Before any authentication
        val result = SessionManager.isAuthenticated()
        
        // Assert
        assertFalse(result)
    }

    @Test
    fun `getSessionToken should return null when not authenticated`() {
        // Act
        val token = SessionManager.getSessionToken()
        
        // Assert
        assertNull(token)
    }

    @Test
    fun `clearSession should not throw exception`() {
        // Act & Assert - Should complete without error
        SessionManager.clearSession()
        
        // Verify it completed successfully
        assertFalse(SessionManager.isAuthenticated())
    }

    @Test
    fun `getSessionStatus should return meaningful status string`() {
        // Act
        val status = SessionManager.getSessionStatus()
        
        // Assert
        assertNotNull(status)
        assertTrue(status.isNotEmpty())
        assertTrue(status.contains("Authenticated:") || status.contains("authenticated"))
    }

    // ========== Lockout Logic Tests ==========

    @Test
    fun `getCurrentLockoutInfo should return not locked out initially`() {
        // Act
        val info = SessionManager.getCurrentLockoutInfo()
        
        // Assert - No lockout at start
        assertFalse(info.isLockedOut)
        assertTrue(info.remainingTime <= 0L)
    }

    @Test
    fun `canAttemptPin should return true when not locked out`() {
        // Act
        val canAttempt = SessionManager.canAttemptPin()
        
        // Assert
        assertTrue(canAttempt)
    }

    // ========== PIN Attempts Tests ==========

    @Test
    fun `getPinAttempts should return current attempt count`() {
        // Act
        val attempts = SessionManager.getPinAttempts()
        
        // Assert - Should be a valid integer
        assertTrue(attempts >= 0)
    }

    @Test
    fun `resetPinAttempts should clear attempt counter`() {
        // Act
        SessionManager.resetPinAttempts()
        
        // Assert
        val attempts = SessionManager.getPinAttempts()
        assertEquals(0, attempts)
    }
}
