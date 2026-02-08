package com.outdu.camconnect.Viewmodels

import app.cash.turbine.test
import com.outdu.camconnect.testutils.MainDispatcherRule
import com.outdu.camconnect.ui.models.CameraMode
import com.outdu.camconnect.ui.models.OrientationMode
import com.outdu.camconnect.ui.models.VisionMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for CameraLayoutViewModel
 * 
 * Tests cover:
 * - Initial state
 * - Stream reload logic
 * - State flow emissions
 * - Unsaved changes tracking
 * - Mode state management
 * 
 * Note: Tests requiring actual camera API calls or complex MISC calculations
 * should be implemented as instrumentation tests in androidTest
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CameraLayoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CameraLayoutViewModel

    @Before
    fun setup() {
        viewModel = CameraLayoutViewModel()
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial auto day night should be false`() {
        assertFalse(viewModel.isAutoDayNightEnabled.value)
    }

    @Test
    fun `initial vision mode should be VISION`() {
        assertEquals(VisionMode.VISION, viewModel.currentVisionMode.value)
    }

    @Test
    fun `initial camera mode should be OFF`() {
        assertEquals(CameraMode.OFF, viewModel.currentCameraMode.value)
    }

    @Test
    fun `initial orientation mode should be NORMAL`() {
        assertEquals(OrientationMode.NORMAL, viewModel.currentOrientationMode.value)
    }

    @Test
    fun `initial has unsaved changes should be false`() {
        assertFalse(viewModel.hasUnsavedChanges.value)
    }

    @Test
    fun `initial stream reloading should be false`() {
        assertFalse(viewModel.isStreamReloading.value)
    }

    @Test
    fun `initial stream reload status text should be null`() {
        assertNull(viewModel.streamReloadStatusText.value)
    }

    @Test
    fun `initial UI interactive should be true`() {
        assertTrue(viewModel.isUIInteractive.value)
    }

    @Test
    fun `initial applied vision mode should be VISION`() {
        assertEquals(VisionMode.VISION, viewModel.appliedVisionMode.value)
    }

    @Test
    fun `initial applied camera mode should be OFF`() {
        assertEquals(CameraMode.OFF, viewModel.appliedCameraMode.value)
    }

    // ========== Stream Reload Tests ==========

    @Test
    fun `triggerStreamReload should set isStreamReloading to true`() = runTest {
        // Act
        viewModel.triggerStreamReload(durationMs = 5000L)

        // Assert
        assertTrue(viewModel.isStreamReloading.value)
    }

    @Test
    fun `triggerStreamReload should reset to false after duration`() = runTest {
        // Act
        viewModel.triggerStreamReload(durationMs = 1000L)
        assertTrue(viewModel.isStreamReloading.value)

        // Advance time past duration
        advanceTimeBy(1100L)

        // Assert
        assertFalse(viewModel.isStreamReloading.value)
    }

    @Test
    fun `triggerStreamReload should clear status text after duration`() = runTest {
        // Act
        viewModel.triggerStreamReload(durationMs = 1000L)
        advanceTimeBy(1100L)

        // Assert
        assertNull(viewModel.streamReloadStatusText.value)
    }

    @Test
    fun `beginStreamReload should set isStreamReloading to true`() = runTest {
        // Act
        viewModel.beginStreamReload(reason = "Test reload")

        // Assert
        assertTrue(viewModel.isStreamReloading.value)
    }

    @Test
    fun `beginStreamReload watchdog should clear reload after timeout`() = runTest {
        // Act
        viewModel.beginStreamReload(watchdogMs = 1000L)
        assertTrue(viewModel.isStreamReloading.value)

        // Advance time past watchdog
        advanceTimeBy(1100L)

        // Assert
        assertFalse(viewModel.isStreamReloading.value)
    }

    @Test
    fun `endStreamReload should clear isStreamReloading`() = runTest {
        // Arrange
        viewModel.beginStreamReload()
        assertTrue(viewModel.isStreamReloading.value)

        // Act
        viewModel.endStreamReload()
        advanceTimeBy(100L) // Small delay for coroutine

        // Assert
        assertFalse(viewModel.isStreamReloading.value)
    }

    @Test
    fun `endStreamReload with delay should wait before clearing`() = runTest {
        // Arrange
        viewModel.beginStreamReload()

        // Act
        viewModel.endStreamReload(delayMs = 1000L)
        
        // Assert - should still be reloading before delay
        advanceTimeBy(500L)
        assertTrue(viewModel.isStreamReloading.value)
        
        // After delay
        advanceTimeBy(600L)
        assertFalse(viewModel.isStreamReloading.value)
    }

    @Test
    fun `setStreamReloadCallback should accept callback function`() {
        // Arrange
        var callbackInvoked = false
        val callback = { callbackInvoked = true }

        // Act
        viewModel.setStreamReloadCallback(callback)

        // Assert - callback is set (will be invoked in triggerStreamReload)
        assertFalse(callbackInvoked) // Not yet invoked
    }

    // ========== State Flow Emission Tests ==========

    @Test
    fun `isStreamReloading should emit updates`() = runTest {
        viewModel.isStreamReloading.test {
            // Initial value
            assertFalse(awaitItem())

            // Trigger reload
            viewModel.beginStreamReload()
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `streamReloadStatusText should emit updates`() = runTest {
        viewModel.streamReloadStatusText.test {
            // Initial value
            assertNull(awaitItem())

            // Set text via setWsChangingMisc
            viewModel.setWsChangingMisc(oldMisc = 1, newMisc = 5)
            
            val text = awaitItem()
            assertNotNull(text)
            assertTrue(text!!.contains("mode"))
        }
    }

    @Test
    fun `isUIInteractive should emit updates`() = runTest {
        viewModel.isUIInteractive.test {
            // Initial value
            assertTrue(awaitItem())
        }
    }

    // ========== setWsChangingMisc Tests ==========

    @Test
    fun `setWsChangingMisc with both values should create change message`() {
        // Act
        viewModel.setWsChangingMisc(oldMisc = 1, newMisc = 5)

        // Assert
        val text = viewModel.streamReloadStatusText.value
        assertNotNull(text)
        assertTrue(text!!.contains("Changing"))
        assertTrue(text.contains("mode"))
    }

    @Test
    fun `setWsChangingMisc with only new value should create mode message`() {
        // Act
        viewModel.setWsChangingMisc(oldMisc = null, newMisc = 5)

        // Assert
        val text = viewModel.streamReloadStatusText.value
        assertNotNull(text)
        assertTrue(text!!.contains("mode"))
    }

    @Test
    fun `setWsChangingMisc with null values should clear message`() {
        // Arrange
        viewModel.setWsChangingMisc(oldMisc = 1, newMisc = 5)
        assertNotNull(viewModel.streamReloadStatusText.value)

        // Act
        viewModel.setWsChangingMisc(oldMisc = null, newMisc = null)

        // Assert
        assertNull(viewModel.streamReloadStatusText.value)
    }

    // ========== ViewModel Lifecycle Tests ==========

    @Test
    fun `viewModel should initialize without errors`() {
        // Creating the viewModel should not throw
        val vm = CameraLayoutViewModel()
        assertNotNull(vm)
    }

    @Test
    fun `multiple stream reload triggers should cancel previous`() = runTest {
        // First trigger
        viewModel.triggerStreamReload(durationMs = 5000L)
        assertTrue(viewModel.isStreamReloading.value)

        advanceTimeBy(1000L)

        // Second trigger should cancel first
        viewModel.triggerStreamReload(durationMs = 5000L)
        assertTrue(viewModel.isStreamReloading.value)

        // Advance only 2000ms total (first would have finished, but second is still running)
        advanceTimeBy(1000L)
        assertTrue(viewModel.isStreamReloading.value)
    }

    // ========== Mode Enum Tests ==========

    @Test
    fun `VisionMode enum should have all values`() {
        val modes = VisionMode.values()
        assertTrue(modes.contains(VisionMode.VISION))
        assertTrue(modes.contains(VisionMode.BOTH))
        assertTrue(modes.contains(VisionMode.INFRARED))
    }

    @Test
    fun `CameraMode enum should have all values`() {
        val modes = CameraMode.values()
        assertTrue(modes.contains(CameraMode.OFF))
        assertTrue(modes.contains(CameraMode.HDR))
        assertTrue(modes.contains(CameraMode.EIS))
    }

    @Test
    fun `OrientationMode enum should have all values`() {
        val modes = OrientationMode.values()
        assertTrue(modes.contains(OrientationMode.NORMAL))
        assertTrue(modes.contains(OrientationMode.FLIP))
        assertTrue(modes.contains(OrientationMode.MIRROR))
        assertTrue(modes.contains(OrientationMode.BOTH))
    }
}
