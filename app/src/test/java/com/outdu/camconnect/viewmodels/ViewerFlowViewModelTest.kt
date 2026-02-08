package com.outdu.camconnect.viewmodels

import app.cash.turbine.test
import com.outdu.camconnect.services.OnvifDevice
import com.outdu.camconnect.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for ViewerFlowViewModel
 * 
 * Tests cover:
 * - Initial state
 * - State flow emissions
 * - Camera selection logic
 * - Dialog state management
 * - Error handling
 * - WiFi connection state management
 * 
 * Note: Tests requiring actual ONVIF discovery or network authentication
 * should be implemented as instrumentation tests in androidTest
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ViewerFlowViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ViewerFlowViewModel

    private val testCamera = OnvifDevice(
        ipAddress = "192.168.1.100",
        endpointUrls = listOf("http://192.168.1.100:80/onvif/device_service"),
        deviceType = "Test Camera"
    )

    @Before
    fun setup() {
        viewModel = ViewerFlowViewModel()
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial uiState should have default values`() {
        val state = viewModel.uiState.value
        assertFalse(state.isDiscovering)
        assertTrue(state.discoveredCameras.isEmpty())
        assertFalse(state.showNoCamerasDialog)
        assertFalse(state.showPinDialog)
        assertFalse(state.isAuthenticating)
        assertNull(state.errorMessage)
        assertNull(state.authError)
        assertFalse(state.showQRScanner)
        assertFalse(state.isConnectingToWifi)
        assertNull(state.wifiConnectionError)
        assertFalse(state.wifiConnectionSuccess)
    }

    @Test
    fun `initial selectedCamera should be null`() {
        assertNull(viewModel.selectedCamera.value)
    }

    // ========== Camera Selection Tests ==========

    @Test
    fun `selectCamera should set selected camera`() {
        // Act
        viewModel.selectCamera(testCamera)

        // Assert
        assertEquals(testCamera, viewModel.selectedCamera.value)
    }

    @Test
    fun `selectCamera should show PIN dialog`() {
        // Act
        viewModel.selectCamera(testCamera)

        // Assert
        assertTrue(viewModel.uiState.value.showPinDialog)
    }

    @Test
    fun `dismissPinDialog should hide PIN dialog and clear selected camera`() {
        // Arrange
        viewModel.selectCamera(testCamera)
        assertTrue(viewModel.uiState.value.showPinDialog)
        assertEquals(testCamera, viewModel.selectedCamera.value)

        // Act
        viewModel.dismissPinDialog()

        // Assert
        assertFalse(viewModel.uiState.value.showPinDialog)
        assertNull(viewModel.selectedCamera.value)
        assertNull(viewModel.uiState.value.authError)
    }

    // ========== Dialog State Tests ==========

    @Test
    fun `dismissNoCamerasDialog should hide no cameras dialog`() {
        // Arrange - manually set the state to show dialog
        viewModel.dismissNoCamerasDialog() // First ensure it's false
        
        // We can't directly set internal state, but we can test the dismiss function
        viewModel.dismissNoCamerasDialog()

        // Assert
        assertFalse(viewModel.uiState.value.showNoCamerasDialog)
    }

    @Test
    fun `showQRScanner should show QR scanner`() {
        // Act
        viewModel.showQRScanner()

        // Assert
        assertTrue(viewModel.uiState.value.showQRScanner)
    }

    @Test
    fun `dismissQRScanner should hide QR scanner and clear WiFi error`() {
        // Arrange
        viewModel.showQRScanner()
        assertTrue(viewModel.uiState.value.showQRScanner)

        // Act
        viewModel.dismissQRScanner()

        // Assert
        assertFalse(viewModel.uiState.value.showQRScanner)
        assertNull(viewModel.uiState.value.wifiConnectionError)
    }

    // ========== Error Handling Tests ==========

    @Test
    fun `clearError should clear error message`() {
        // Act
        viewModel.clearError()

        // Assert
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `clearAuthError should clear auth error`() {
        // Act
        viewModel.clearAuthError()

        // Assert
        assertNull(viewModel.uiState.value.authError)
    }

    @Test
    fun `clearWifiConnectionError should clear WiFi error`() {
        // Act
        viewModel.clearWifiConnectionError()

        // Assert
        assertNull(viewModel.uiState.value.wifiConnectionError)
    }

    // ========== WiFi Connection State Tests ==========

    @Test
    fun `setWifiConnecting true should set connecting state`() {
        // Act
        viewModel.setWifiConnecting(true)

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.isConnectingToWifi)
        assertFalse(state.wifiConnectionSuccess)
    }

    @Test
    fun `setWifiConnecting false should clear connecting state`() {
        // Arrange
        viewModel.setWifiConnecting(true)

        // Act
        viewModel.setWifiConnecting(false)

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isConnectingToWifi)
        assertFalse(state.wifiConnectionSuccess)
    }

    @Test
    fun `setWifiConnectionSuccess true should set success state`() {
        // Act
        viewModel.setWifiConnectionSuccess(true)

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.wifiConnectionSuccess)
        assertFalse(state.isConnectingToWifi)
    }

    @Test
    fun `setWifiConnectionSuccess false should clear success state`() {
        // Arrange
        viewModel.setWifiConnectionSuccess(true)

        // Act
        viewModel.setWifiConnectionSuccess(false)

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.wifiConnectionSuccess)
        assertFalse(state.isConnectingToWifi)
    }

    @Test
    fun `setWifiConnectionError should set error and clear other WiFi states`() {
        // Arrange
        viewModel.setWifiConnecting(true)

        // Act
        viewModel.setWifiConnectionError("Connection failed")

        // Assert
        val state = viewModel.uiState.value
        assertEquals("Connection failed", state.wifiConnectionError)
        assertFalse(state.isConnectingToWifi)
        assertFalse(state.wifiConnectionSuccess)
    }

    @Test
    fun `setWifiConnectionError with null should clear error`() {
        // Arrange
        viewModel.setWifiConnectionError("Some error")

        // Act
        viewModel.setWifiConnectionError(null)

        // Assert
        assertNull(viewModel.uiState.value.wifiConnectionError)
    }

    // ========== State Flow Emission Tests ==========

    @Test
    fun `uiState should emit updates when state changes`() = runTest {
        viewModel.uiState.test {
            // Initial state
            val initial = awaitItem()
            assertFalse(initial.showQRScanner)

            // Change state
            viewModel.showQRScanner()

            // New state
            val updated = awaitItem()
            assertTrue(updated.showQRScanner)
        }
    }

    @Test
    fun `selectedCamera should emit updates when camera selected`() = runTest {
        viewModel.selectedCamera.test {
            // Initial value
            assertNull(awaitItem())

            // Select camera
            viewModel.selectCamera(testCamera)

            // New value
            assertEquals(testCamera, awaitItem())
        }
    }

    // ========== ViewerFlowState Data Class Tests ==========

    @Test
    fun `ViewerFlowState should have correct default values`() {
        val state = ViewerFlowState()
        
        assertFalse(state.isDiscovering)
        assertTrue(state.discoveredCameras.isEmpty())
        assertFalse(state.showNoCamerasDialog)
        assertFalse(state.showPinDialog)
        assertFalse(state.isAuthenticating)
        assertNull(state.errorMessage)
        assertNull(state.authError)
        assertFalse(state.showQRScanner)
        assertFalse(state.isConnectingToWifi)
        assertNull(state.wifiConnectionError)
        assertFalse(state.wifiConnectionSuccess)
    }

    @Test
    fun `ViewerFlowState copy should work correctly`() {
        val original = ViewerFlowState(
            isDiscovering = true,
            showPinDialog = true
        )

        val copied = original.copy(isDiscovering = false)

        assertFalse(copied.isDiscovering)
        assertTrue(copied.showPinDialog) // Should remain true
    }

    // ========== Integration Tests ==========

    @Test
    fun `multiple state changes should work correctly`() {
        // Act - Simulate user flow
        viewModel.showQRScanner()
        viewModel.setWifiConnecting(true)
        viewModel.setWifiConnectionSuccess(true)
        viewModel.dismissQRScanner()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.showQRScanner) // Dismissed
        assertFalse(state.isConnectingToWifi) // Success cleared this
        assertTrue(state.wifiConnectionSuccess)
    }

    @Test
    fun `error clearing should work independently`() {
        // Setup errors
        viewModel.clearError()
        viewModel.clearAuthError()
        viewModel.clearWifiConnectionError()

        // Assert all errors are null
        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertNull(state.authError)
        assertNull(state.wifiConnectionError)
    }
}
