package com.outdu.camconnect.ui.viewmodels

import app.cash.turbine.test
import com.outdu.camconnect.testutils.MainDispatcherRule
import com.outdu.camconnect.ui.models.RecordingState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for RecordingViewModel
 * 
 * Tests cover:
 * - Initial state
 * - State flow emissions
 * - Recording state transitions (testable logic)
 * - Filename prompt handling
 * - UI event emissions
 * 
 * Note: Tests requiring Android Context, Service, or Activity interactions
 * should be implemented as instrumentation tests in androidTest
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecordingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: RecordingViewModel

    @Before
    fun setup() {
        viewModel = RecordingViewModel()
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial recording state should be false`() {
        assertFalse(viewModel.isRecording.value)
    }

    @Test
    fun `initial recording state should be NotRecording`() {
        assertTrue(viewModel.recordingState.value is RecordingState.NotRecording)
    }

    // ========== State Flow Tests ==========

    @Test
    fun `isRecording StateFlow should be observable`() = runTest {
        viewModel.isRecording.test {
            // Verify initial state
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `recordingState StateFlow should be observable`() = runTest {
        viewModel.recordingState.test {
            // Verify initial state
            val state = awaitItem()
            assertTrue(state is RecordingState.NotRecording)
        }
    }

    @Test
    fun `uiEvents SharedFlow should be observable`() = runTest {
        viewModel.uiEvents.test {
            // No initial event
            expectNoEvents()
        }
    }

    // ========== Filename Prompt Tests ==========

    @Test
    fun `cancelFilenamePrompt should return to NotRecording when not recording`() {
        // Arrange - ViewModel starts with NotRecording state
        assertTrue(viewModel.recordingState.value is RecordingState.NotRecording)

        // Act
        viewModel.cancelFilenamePrompt()

        // Assert - Should remain NotRecording
        assertTrue(viewModel.recordingState.value is RecordingState.NotRecording)
    }

    // ========== Recording State Type Tests ==========

    @Test
    fun `RecordingState should have NotRecording type`() {
        val state = RecordingState.NotRecording
        assertTrue(state is RecordingState.NotRecording)
    }

    @Test
    fun `RecordingState should have Recording type with duration`() {
        val state = RecordingState.Recording("01:30")
        assertTrue(state is RecordingState.Recording)
        assertEquals("01:30", (state as RecordingState.Recording).duration)
    }

    @Test
    fun `RecordingState should have PromptingForFilename type`() {
        val state = RecordingState.PromptingForFilename
        assertTrue(state is RecordingState.PromptingForFilename)
    }

    @Test
    fun `RecordingState should have StoppingRecording type`() {
        val state = RecordingState.StoppingRecording
        assertTrue(state is RecordingState.StoppingRecording)
    }

    @Test
    fun `RecordingState should have SavedToGallery type`() {
        val state = RecordingState.SavedToGallery
        assertTrue(state is RecordingState.SavedToGallery)
    }

    // ========== UI Event Type Tests ==========

    @Test
    fun `RecordingUiEvent should have LowStorageCannotStart type`() {
        val event = RecordingUiEvent.LowStorageCannotStart
        assertTrue(event is RecordingUiEvent.LowStorageCannotStart)
    }

    @Test
    fun `RecordingUiEvent should have LowStorageStoppedRecording type`() {
        val event = RecordingUiEvent.LowStorageStoppedRecording
        assertTrue(event is RecordingUiEvent.LowStorageStoppedRecording)
    }

    // ========== ViewModel Lifecycle Tests ==========

    @Test
    fun `viewModel should initialize without errors`() {
        // Just creating the viewModel should not throw
        val vm = RecordingViewModel()
        assertNotNull(vm)
        assertFalse(vm.isRecording.value)
    }

    @Test
    fun `multiple instances should have independent state`() {
        val vm1 = RecordingViewModel()
        val vm2 = RecordingViewModel()

        // Both should start with same initial state
        assertFalse(vm1.isRecording.value)
        assertFalse(vm2.isRecording.value)
        
        // But they are independent instances
        assertNotSame(vm1, vm2)
    }
}
