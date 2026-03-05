package com.outdu.camconnect.viewmodels

import com.outdu.camconnect.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for SetupViewModel
 * 
 * Tests the setup flow state management, including:
 * - Network configuration
 * - Camera configuration
 * - Permissions management
 * - User registration
 * - Email verification
 * - Setup completion logic
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SetupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SetupViewModel

    @Before
    fun setup() {
        viewModel = SetupViewModel()
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial state should have default values`() = runTest {
        // Assert
        val state = viewModel.setupState.value
        assertFalse(state.isNetworkConfigured)
        assertFalse(state.isCameraConfigured)
        assertFalse(state.arePermissionsGranted)
        assertEquals(0, state.currentStep)
        assertNull(state.error)
        assertFalse(state.isSetupComplete)
        assertEquals("", state.username)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertFalse(state.isEmailVerified)
        assertEquals("", state.verificationCode)
        assertFalse(state.isRegistrationComplete)
    }

    // ========== Network Configuration Tests ==========

    @Test
    fun `updateNetworkConfig with true should update state and mark setup complete`() = runTest {
        // Act
        viewModel.updateNetworkConfig(true)

        // Assert
        val state = viewModel.setupState.value
        assertTrue(state.isNetworkConfigured)
        assertTrue(state.isSetupComplete) // Note: Based on current implementation
        assertNull(state.error)
    }

    @Test
    fun `updateNetworkConfig with false should update state`() = runTest {
        // Act
        viewModel.updateNetworkConfig(false)

        // Assert
        val state = viewModel.setupState.value
        assertFalse(state.isNetworkConfigured)
        assertNull(state.error)
    }

    @Test
    fun `updateNetworkConfig should clear any existing error`() = runTest {
        // Arrange
        viewModel.setError("Previous error")

        // Act
        viewModel.updateNetworkConfig(true)

        // Assert
        assertNull(viewModel.setupState.value.error)
    }

    // ========== Camera Configuration Tests ==========

    @Test
    fun `updateCameraConfig with true should update state`() = runTest {
        // Act
        viewModel.updateCameraConfig(true)

        // Assert
        val state = viewModel.setupState.value
        assertTrue(state.isCameraConfigured)
        assertNull(state.error)
    }

    @Test
    fun `updateCameraConfig with false should update state`() = runTest {
        // Act
        viewModel.updateCameraConfig(false)

        // Assert
        val state = viewModel.setupState.value
        assertFalse(state.isCameraConfigured)
        assertNull(state.error)
    }

    @Test
    fun `updateCameraConfig should clear any existing error`() = runTest {
        // Arrange
        viewModel.setError("Previous error")

        // Act
        viewModel.updateCameraConfig(true)

        // Assert
        assertNull(viewModel.setupState.value.error)
    }

    // ========== Permissions Tests ==========

    @Test
    fun `updatePermissionsStatus with true should update state`() = runTest {
        // Act
        viewModel.updatePermissionsStatus(true)

        // Assert
        val state = viewModel.setupState.value
        assertTrue(state.arePermissionsGranted)
        assertNull(state.error)
    }

    @Test
    fun `updatePermissionsStatus with false should update state`() = runTest {
        // Act
        viewModel.updatePermissionsStatus(false)

        // Assert
        val state = viewModel.setupState.value
        assertFalse(state.arePermissionsGranted)
        assertNull(state.error)
    }

    @Test
    fun `updatePermissionsStatus should clear any existing error`() = runTest {
        // Arrange
        viewModel.setError("Previous error")

        // Act
        viewModel.updatePermissionsStatus(true)

        // Assert
        assertNull(viewModel.setupState.value.error)
    }

    // ========== Registration Details Tests ==========

    @Test
    fun `updateRegistrationDetails should update state when passwords match`() = runTest {
        // Arrange
        val testUsername = "testuser"
        val testEmail = "test@example.com"
        val testPassword = "password123"

        // Act
        viewModel.updateRegistrationDetails(
            username = testUsername,
            email = testEmail,
            password = testPassword,
            confirmPassword = testPassword
        )

        // Assert
        val state = viewModel.setupState.value
        assertEquals(testUsername, state.username)
        assertEquals(testEmail, state.email)
        assertEquals(testPassword, state.password)
        assertEquals(testPassword, state.confirmPassword)
        assertNull(state.error)
    }

    @Test
    fun `updateRegistrationDetails should set error when passwords do not match`() = runTest {
        // Arrange
        val testUsername = "testuser"
        val testEmail = "test@example.com"
        val testPassword = "password123"
        val wrongConfirmPassword = "password456"

        // Act
        viewModel.updateRegistrationDetails(
            username = testUsername,
            email = testEmail,
            password = testPassword,
            confirmPassword = wrongConfirmPassword
        )

        // Assert
        val state = viewModel.setupState.value
        assertEquals("Passwords do not match", state.error)
        // Note: Based on implementation, other fields are not updated when passwords mismatch
    }

    @Test
    fun `updateRegistrationDetails should handle empty passwords`() = runTest {
        // Act
        viewModel.updateRegistrationDetails(
            username = "user",
            email = "test@example.com",
            password = "",
            confirmPassword = ""
        )

        // Assert
        val state = viewModel.setupState.value
        assertEquals("user", state.username)
        assertEquals("test@example.com", state.email)
        assertEquals("", state.password)
        assertNull(state.error)
    }

    // ========== Email Verification Tests ==========

    @Test
    fun `updateVerificationCode should update state`() = runTest {
        // Arrange
        val testCode = "123456"

        // Act
        viewModel.updateVerificationCode(testCode)

        // Assert
        val state = viewModel.setupState.value
        assertEquals(testCode, state.verificationCode)
    }

    @Test
    fun `verifyEmail should mark email as verified and clear error`() = runTest {
        // Act
        viewModel.verifyEmail()

        // Assert
        val state = viewModel.setupState.value
        assertTrue(state.isEmailVerified)
        assertNull(state.error)
    }

    @Test
    fun `verifyEmail should clear any existing error`() = runTest {
        // Arrange
        viewModel.setError("Verification failed")

        // Act
        viewModel.verifyEmail()

        // Assert
        val state = viewModel.setupState.value
        assertTrue(state.isEmailVerified)
        assertNull(state.error)
    }

    // ========== Error Handling Tests ==========

    @Test
    fun `setError should set error message`() = runTest {
        // Arrange
        val errorMessage = "Test error message"

        // Act
        viewModel.setError(errorMessage)

        // Assert
        assertEquals(errorMessage, viewModel.setupState.value.error)
    }

    @Test
    fun `clearError should remove error message`() = runTest {
        // Arrange
        viewModel.setError("Some error")

        // Act
        viewModel.clearError()

        // Assert
        assertNull(viewModel.setupState.value.error)
    }

    @Test
    fun `setError should overwrite existing error`() = runTest {
        // Arrange
        viewModel.setError("First error")
        val newError = "Second error"

        // Act
        viewModel.setError(newError)

        // Assert
        assertEquals(newError, viewModel.setupState.value.error)
    }

    // ========== Setup Completion Tests ==========

    @Test
    fun `isSetupComplete should return true when all requirements met`() = runTest {
        // Arrange - Complete all setup steps in correct order
        viewModel.verifyEmail()
        viewModel.updateCameraConfig(true)
        viewModel.updatePermissionsStatus(true)
        viewModel.updateNetworkConfig(true)

        // Act
        val isComplete = viewModel.isSetupComplete()

        // Assert
        assertTrue(isComplete)
    }

    @Test
    fun `isSetupComplete should return false when network not configured`() = runTest {
        // Arrange - Everything except network
        viewModel.updateCameraConfig(true)
        viewModel.updatePermissionsStatus(true)
        viewModel.verifyEmail()

        // Act
        val isComplete = viewModel.isSetupComplete()

        // Assert
        // Note: checkSetupCompletion() verifies all conditions including network
        assertFalse(isComplete)
    }

    @Test
    fun `updateNetworkConfig sets isSetupComplete to true directly`() = runTest {
        // Act - Note: current implementation sets isSetupComplete=true when network is configured
        viewModel.updateNetworkConfig(true)

        // Assert
        // This is the current implementation behavior
        assertTrue(viewModel.setupState.value.isSetupComplete)
    }

    @Test
    fun `checkSetupCompletion requires all conditions when called by updateCameraConfig`() = runTest {
        // Arrange - Only camera configured
        viewModel.updateCameraConfig(true)

        // Assert - Should be false because other conditions not met
        assertFalse(viewModel.setupState.value.isSetupComplete)
    }

    @Test
    fun `checkSetupCompletion requires all conditions when called by updatePermissionsStatus`() = runTest {
        // Arrange - Only permissions granted
        viewModel.updatePermissionsStatus(true)

        // Assert - Should be false because other conditions not met
        assertFalse(viewModel.setupState.value.isSetupComplete)
    }

    // ========== Integration Tests ==========

    @Test
    fun `complete setup flow should mark setup as complete`() = runTest {
        // Act - Complete all setup steps
        viewModel.updateRegistrationDetails(
            username = "user",
            email = "user@example.com",
            password = "pass123",
            confirmPassword = "pass123"
        )
        viewModel.updateVerificationCode("123456")
        viewModel.verifyEmail()
        viewModel.updateNetworkConfig(true)
        viewModel.updateCameraConfig(true)
        viewModel.updatePermissionsStatus(true)

        // Assert
        val state = viewModel.setupState.value
        assertTrue(state.isSetupComplete)
        assertTrue(state.isNetworkConfigured)
        assertTrue(state.isCameraConfigured)
        assertTrue(state.arePermissionsGranted)
        assertTrue(state.isEmailVerified)
        assertEquals("user", state.username)
        assertNull(state.error)
    }

    @Test
    fun `error during setup should be recoverable`() = runTest {
        // Arrange
        viewModel.updateRegistrationDetails(
            username = "user",
            email = "test@example.com",
            password = "pass123",
            confirmPassword = "wrongpass"
        )

        // Assert error occurred
        assertEquals("Passwords do not match", viewModel.setupState.value.error)

        // Act - Correct the error
        viewModel.clearError()
        viewModel.updateRegistrationDetails(
            username = "user",
            email = "test@example.com",
            password = "pass123",
            confirmPassword = "pass123"
        )

        // Assert - Error cleared and state updated
        assertNull(viewModel.setupState.value.error)
        assertEquals("user", viewModel.setupState.value.username)
    }

    @Test
    fun `multiple state updates should maintain consistency`() = runTest {
        // Act
        viewModel.updateNetworkConfig(true)
        viewModel.updateNetworkConfig(false)
        viewModel.updateNetworkConfig(true)

        // Assert
        assertTrue(viewModel.setupState.value.isNetworkConfigured)
        assertNull(viewModel.setupState.value.error)
    }

    // ========== Error Handling / Edge Cases ==========

    @Test
    fun `setError then clearError clears error`() = runTest {
        viewModel.setError("Some error")
        assertEquals("Some error", viewModel.setupState.value.error)
        viewModel.clearError()
        assertNull(viewModel.setupState.value.error)
    }

    @Test
    fun `updateRegistrationDetails with empty username is accepted`() = runTest {
        viewModel.updateRegistrationDetails(
            username = "",
            email = "e@e.com",
            password = "p",
            confirmPassword = "p"
        )
        assertEquals("", viewModel.setupState.value.username)
        assertEquals("e@e.com", viewModel.setupState.value.email)
    }
}
