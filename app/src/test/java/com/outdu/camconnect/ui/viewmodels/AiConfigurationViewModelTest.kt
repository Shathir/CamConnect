package com.outdu.camconnect.ui.viewmodels

import app.cash.turbine.test
import com.outdu.camconnect.testutils.MainDispatcherRule
import com.outdu.camconnect.ui.layouts.streamer.AiRegionOverlayType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for AiConfigurationViewModel
 * 
 * Tests cover:
 * - Initial state
 * - Configuration updates (OD, FAR, DS, Audio, Model)
 * - Unsaved changes tracking
 * - Threshold updates
 * - Overlay type changes (MASK, BOX, NONE)
 * - Error handling
 * - StateFlow emissions
 * 
 * Note: Tests requiring Context (load/save) should be in androidTest
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AiConfigurationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AiConfigurationViewModel

    @Before
    fun setup() {
        viewModel = AiConfigurationViewModel()
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial state should have default values`() {
        val state = viewModel.uiState.value
        
        assertFalse(state.far)
        assertFalse(state.od)
        assertFalse(state.ds)
        assertFalse(state.audio)
        assertEquals(1, state.model)
        assertEquals(0.5f, state.dsThreshold, 0.001f)
        assertEquals(AiRegionOverlayType.MASK, state.overlayType)
        assertFalse(state.isLoading)
        assertFalse(state.hasUnsavedChanges)
        assertNull(state.errorMessage)
    }

    // ========== Object Detection (OD) Tests ==========

    @Test
    fun `updateOD should update OD state`() {
        // Act
        viewModel.updateOD(true)

        // Assert
        assertTrue(viewModel.uiState.value.od)
    }

    @Test
    fun `updateOD should update OD state correctly`() {
        // Act
        viewModel.updateOD(true)
        assertTrue(viewModel.uiState.value.od)

        viewModel.updateOD(false)
        assertFalse(viewModel.uiState.value.od)

        // Note: hasUnsavedChanges requires originalState to be set (via loadConfiguration),
        // which requires Context and should be tested in androidTest
    }

    @Test
    fun `updateOD multiple times should toggle correctly`() {
        // Act
        viewModel.updateOD(true)
        assertTrue(viewModel.uiState.value.od)

        viewModel.updateOD(false)
        assertFalse(viewModel.uiState.value.od)

        viewModel.updateOD(true)
        assertTrue(viewModel.uiState.value.od)
    }

    // ========== Far Detection (FAR) Tests ==========

    @Test
    fun `updateFAR should update FAR state`() {
        // Act
        viewModel.updateFAR(true)

        // Assert
        assertTrue(viewModel.uiState.value.far)
    }

    @Test
    fun `updateFAR should toggle correctly`() {
        viewModel.updateFAR(true)
        assertTrue(viewModel.uiState.value.far)

        viewModel.updateFAR(false)
        assertFalse(viewModel.uiState.value.far)
    }

    // ========== Depth Sensing (DS) Tests ==========

    @Test
    fun `updateDS should update DS state`() {
        // Act
        viewModel.updateDS(true)

        // Assert
        assertTrue(viewModel.uiState.value.ds)
    }

    @Test
    fun `updateDS should toggle correctly`() {
        viewModel.updateDS(true)
        assertTrue(viewModel.uiState.value.ds)

        viewModel.updateDS(false)
        assertFalse(viewModel.uiState.value.ds)
    }

    // ========== Audio Tests ==========

    @Test
    fun `updateAudio should update audio state`() {
        // Act
        viewModel.updateAudio(true)

        // Assert
        assertTrue(viewModel.uiState.value.audio)
    }

    @Test
    fun `updateAudio should toggle correctly`() {
        viewModel.updateAudio(true)
        assertTrue(viewModel.uiState.value.audio)

        viewModel.updateAudio(false)
        assertFalse(viewModel.uiState.value.audio)
    }

    // ========== Model Version Tests ==========

    @Test
    fun `updateModel should update model version`() {
        // Act
        viewModel.updateModel(2)

        // Assert
        assertEquals(2, viewModel.uiState.value.model)
    }

    @Test
    fun `updateModel should accept different versions`() {
        viewModel.updateModel(1)
        assertEquals(1, viewModel.uiState.value.model)

        viewModel.updateModel(3)
        assertEquals(3, viewModel.uiState.value.model)

        viewModel.updateModel(5)
        assertEquals(5, viewModel.uiState.value.model)
    }

    // ========== Depth Sensing Threshold Tests ==========

    @Test
    fun `updateDsThreshold should update threshold value`() {
        // Act
        viewModel.updateDsThreshold(0.75f)

        // Assert
        assertEquals(0.75f, viewModel.uiState.value.dsThreshold, 0.001f)
    }

    @Test
    fun `updateDsThreshold should accept various threshold values`() {
        viewModel.updateDsThreshold(0.0f)
        assertEquals(0.0f, viewModel.uiState.value.dsThreshold, 0.001f)

        viewModel.updateDsThreshold(0.5f)
        assertEquals(0.5f, viewModel.uiState.value.dsThreshold, 0.001f)

        viewModel.updateDsThreshold(1.0f)
        assertEquals(1.0f, viewModel.uiState.value.dsThreshold, 0.001f)
    }

    // ========== Overlay Type Tests ==========

    @Test
    fun `updateOverlayType should update overlay type`() {
        // Act
        viewModel.updateOverlayType(AiRegionOverlayType.BOX)

        // Assert
        assertEquals(AiRegionOverlayType.BOX, viewModel.uiState.value.overlayType)
    }

    @Test
    fun `updateOverlayType should cycle through types`() {
        viewModel.updateOverlayType(AiRegionOverlayType.MASK)
        assertEquals(AiRegionOverlayType.MASK, viewModel.uiState.value.overlayType)

        viewModel.updateOverlayType(AiRegionOverlayType.BOX)
        assertEquals(AiRegionOverlayType.BOX, viewModel.uiState.value.overlayType)

        viewModel.updateOverlayType(AiRegionOverlayType.NONE)
        assertEquals(AiRegionOverlayType.NONE, viewModel.uiState.value.overlayType)
    }

    // ========== Error Handling Tests ==========

    @Test
    fun `clearError should clear error message`() {
        // Act
        viewModel.clearError()

        // Assert
        assertNull(viewModel.uiState.value.errorMessage)
    }

    // ========== StateFlow Emission Tests ==========

    @Test
    fun `uiState should emit updates when OD changes`() = runTest {
        viewModel.uiState.test {
            // Initial state
            val initial = awaitItem()
            assertFalse(initial.od)

            // Update OD
            viewModel.updateOD(true)
            val updated = awaitItem()
            assertTrue(updated.od)
        }
    }

    @Test
    fun `uiState should emit updates when model changes`() = runTest {
        viewModel.uiState.test {
            // Initial state
            val initial = awaitItem()
            assertEquals(1, initial.model)

            // Update model
            viewModel.updateModel(3)
            val updated = awaitItem()
            assertEquals(3, updated.model)
        }
    }

    // ========== Integration Tests ==========

    @Test
    fun `multiple configuration changes should work together`() {
        // Act - Update multiple settings
        viewModel.updateOD(true)
        viewModel.updateFAR(true)
        viewModel.updateDS(true)
        viewModel.updateAudio(true)
        viewModel.updateModel(2)
        viewModel.updateDsThreshold(0.8f)
        viewModel.updateOverlayType(AiRegionOverlayType.BOX)

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.od)
        assertTrue(state.far)
        assertTrue(state.ds)
        assertTrue(state.audio)
        assertEquals(2, state.model)
        assertEquals(0.8f, state.dsThreshold, 0.001f)
        assertEquals(AiRegionOverlayType.BOX, state.overlayType)
    }

    @Test
    fun `AiConfigurationUiState data class should have correct defaults`() {
        val state = AiConfigurationUiState()

        assertFalse(state.far)
        assertFalse(state.od)
        assertFalse(state.ds)
        assertFalse(state.audio)
        assertEquals(1, state.model)
        assertEquals(0.5f, state.dsThreshold, 0.001f)
        assertEquals(AiRegionOverlayType.MASK, state.overlayType)
        assertFalse(state.isLoading)
        assertFalse(state.hasUnsavedChanges)
        assertNull(state.errorMessage)
    }

    @Test
    fun `AiConfigurationUiState copy should work correctly`() {
        val original = AiConfigurationUiState(od = true, far = true)
        val copied = original.copy(ds = true)

        assertTrue(copied.od)
        assertTrue(copied.far)
        assertTrue(copied.ds)
        assertFalse(copied.audio) // Unchanged
    }
}
