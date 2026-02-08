package com.outdu.camconnect.auth

import android.content.Context
import android.content.SharedPreferences
import app.cash.turbine.test
import com.outdu.camconnect.data.CameraRepository
import com.outdu.camconnect.testutils.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for UserStateManager
 * 
 * Tests cover:
 * - User state transitions
 * - Flow type determination
 * - State persistence
 * - StateFlow emissions
 * - User type management
 * 
 * Note: Testing singletons is challenging. These tests verify public API behavior.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserStateManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        // Mock Context
        mockContext = mockk(relaxed = true)
        
        // Mock SharedPreferences and Editor
        mockSharedPreferences = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)
        
        // Setup SharedPreferences mocking chain
        every { mockContext.getSharedPreferences(any(), any()) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.putLong(any(), any()) } returns mockEditor
        every { mockEditor.putInt(any(), any()) } returns mockEditor
        every { mockEditor.putBoolean(any(), any()) } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor
        every { mockEditor.clear() } returns mockEditor
        every { mockEditor.apply() } just Runs
        
        // Default return values for loading state
        every { mockSharedPreferences.getBoolean(any(), any()) } answers { secondArg() }
        every { mockSharedPreferences.getString(any(), any()) } answers { secondArg() }
        every { mockSharedPreferences.getInt(any(), any()) } answers { secondArg() }
        every { mockSharedPreferences.getLong(any(), any()) } answers { secondArg() }
    }

    // ========== User Flow Type Tests ==========

    @Test
    fun `getUserFlowType should return FIRST_TIME for new user`() = runTest {
        // Act
        val flowType = UserStateManager.getUserFlowType()
        
        // Assert
        assertEquals(UserStateManager.UserFlowType.FIRST_TIME, flowType)
    }

    @Test
    fun `getUserFlowType should return RETURNING_WITH_CAMERAS after setup with cameras`() = runTest {
        // Arrange - Simulate completed setup with cameras
        UserStateManager.markAppLaunched()
        UserStateManager.markSetupCompleted()
        UserStateManager.updateRegisteredCamerasCount(1)
        
        // Act
        val flowType = UserStateManager.getUserFlowType()
        
        // Assert
        assertEquals(UserStateManager.UserFlowType.RETURNING_WITH_CAMERAS, flowType)
    }

    @Test
    fun `getUserFlowType should return RETURNING_NO_CAMERAS after setup without cameras`() = runTest {
        // Arrange - Simulate completed setup but no cameras
        UserStateManager.markAppLaunched()
        UserStateManager.markSetupCompleted()
        UserStateManager.updateRegisteredCamerasCount(0)
        
        // Act
        val flowType = UserStateManager.getUserFlowType()
        
        // Assert
        assertEquals(UserStateManager.UserFlowType.RETURNING_NO_CAMERAS, flowType)
    }

    @Test
    fun `getUserFlowType should return VIEWER for viewer user type`() = runTest {
        // Arrange - Reset and set viewer type without completing setup
        UserStateManager.resetUserState()
        UserStateManager.markAppLaunched() // Not first launch anymore
        UserStateManager.setUserType(UserStateManager.UserType.VIEWER)
        
        // Act
        val flowType = UserStateManager.getUserFlowType()
        
        // Assert
        assertEquals(UserStateManager.UserFlowType.VIEWER, flowType)
    }

    // ========== State Management Tests ==========

    @Test
    fun `markAppLaunched should update state`() = runTest {
        // Act
        UserStateManager.markAppLaunched()
        
        // Assert
        val state = UserStateManager.userState.value
        assertFalse(state.isFirstLaunch)
    }

    @Test
    fun `markSetupCompleted should update state for OWNER`() = runTest {
        // Act
        UserStateManager.markSetupCompleted(UserStateManager.UserType.OWNER)
        
        // Assert
        val state = UserStateManager.userState.value
        assertTrue(state.hasCompletedSetup)
        assertEquals(UserStateManager.UserType.OWNER, state.userType)
        assertFalse(state.needsOnboarding)
    }

    @Test
    fun `markSetupCompleted should update state for VIEWER`() = runTest {
        // Act
        UserStateManager.markSetupCompleted(UserStateManager.UserType.VIEWER)
        
        // Assert
        val state = UserStateManager.userState.value
        assertTrue(state.hasCompletedSetup)
        assertEquals(UserStateManager.UserType.VIEWER, state.userType)
    }

    @Test
    fun `updateRegisteredCamerasCount should update state`() = runTest {
        // Act
        UserStateManager.updateRegisteredCamerasCount(5)
        
        // Assert
        val state = UserStateManager.userState.value
        assertEquals(5, state.registeredCamerasCount)
    }

    @Test
    fun `setUserType should update state`() = runTest {
        // Act
        UserStateManager.setUserType(UserStateManager.UserType.OWNER)
        
        // Assert
        val state = UserStateManager.userState.value
        assertEquals(UserStateManager.UserType.OWNER, state.userType)
    }

    @Test
    fun `updateAuthenticationStatus should update state`() = runTest {
        // Act
        UserStateManager.updateAuthenticationStatus(true)
        
        // Assert
        val state = UserStateManager.userState.value
        assertTrue(state.isAuthenticated)
    }

    // ========== StateFlow Emission Tests ==========

    @Test
    fun `userState flow should emit updates when state changes`() = runTest {
        UserStateManager.userState.test {
            // Initial state
            val initial = awaitItem()
            
            // Trigger change
            UserStateManager.updateAuthenticationStatus(true)
            
            // New state
            val updated = awaitItem()
            assertTrue(updated.isAuthenticated)
            assertNotEquals(initial.isAuthenticated, updated.isAuthenticated)
        }
    }

    // ========== Onboarding Tests ==========

    @Test
    fun `needsOnboarding should return true for first launch`() = runTest {
        // Arrange - Reset state
        UserStateManager.resetUserState()
        
        // Act
        val needsOnboarding = UserStateManager.needsOnboarding()
        
        // Assert
        assertTrue(needsOnboarding)
    }

    @Test
    fun `needsOnboarding should return false after setup completed`() = runTest {
        // Arrange
        UserStateManager.markAppLaunched()
        UserStateManager.markSetupCompleted()
        
        // Act
        val needsOnboarding = UserStateManager.needsOnboarding()
        
        // Assert
        assertFalse(needsOnboarding)
    }

    // ========== getUserStatus Tests ==========

    @Test
    fun `getUserStatus should return formatted status string`() = runTest {
        // Act
        val status = UserStateManager.getUserStatus()
        
        // Assert
        assertNotNull(status)
        assertTrue(status.contains("Flow Type:"))
        assertTrue(status.contains("First Launch:"))
        assertTrue(status.contains("Setup Complete:"))
        assertTrue(status.contains("User Type:"))
    }

    // ========== Reset Tests ==========

    @Test
    fun `resetUserState should clear all state`() = runTest {
        // Arrange - Set some state
        UserStateManager.markAppLaunched()
        UserStateManager.markSetupCompleted()
        UserStateManager.updateRegisteredCamerasCount(5)
        
        // Act
        UserStateManager.resetUserState()
        
        // Assert
        val state = UserStateManager.userState.value
        assertTrue(state.isFirstLaunch)
        assertFalse(state.hasCompletedSetup)
        assertEquals(0, state.registeredCamerasCount)
    }
}
