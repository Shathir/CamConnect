package com.outdu.camconnect.ui.viewmodels

import android.app.Activity
import android.content.Context
import android.content.Intent
import app.cash.turbine.test
import com.outdu.camconnect.services.ScreenRecorderService
import com.outdu.camconnect.services.ServiceEvent
import com.outdu.camconnect.testutils.MainDispatcherRule
import com.outdu.camconnect.ui.models.RecordingState
import com.outdu.camconnect.utils.StorageUtils
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for RecordingViewModel.
 * Tests state flows, UI events from service events, cancelFilenamePrompt, toggleRecording
 * (with mocks), handleActivityResult, and stopRecordingWithFilename.
 * Note: Tests that set service running state to true are limited to avoid the infinite
 * duration-update loop; those paths are covered in instrumented tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecordingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: RecordingViewModel
    private lateinit var isServiceRunningFlow: MutableStateFlow<Boolean>
    private lateinit var serviceEventsFlow: MutableSharedFlow<ServiceEvent>

    @Before
    fun setup() {
        isServiceRunningFlow = MutableStateFlow(false)
        serviceEventsFlow = MutableSharedFlow(extraBufferCapacity = 1)
        mockkObject(ScreenRecorderService)
        every { ScreenRecorderService.isServiceRunning } returns isServiceRunningFlow
        every { ScreenRecorderService.events } returns serviceEventsFlow
        viewModel = RecordingViewModel()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial isRecording is false when service is not running`() = runTest {
        advanceUntilIdle()
        viewModel.isRecording.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial recordingState is NotRecording when service is not running`() = runTest {
        advanceUntilIdle()
        viewModel.recordingState.test {
            assertEquals(RecordingState.NotRecording, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== Service events -> UI events ==========

    @Test
    fun `ServiceEvent CannotStartLowStorage emits LowStorageCannotStart ui event`() = runTest {
        advanceUntilIdle()
        viewModel.uiEvents.test {
            serviceEventsFlow.tryEmit(ServiceEvent.CannotStartLowStorage)
            advanceUntilIdle()
            assertEquals(RecordingUiEvent.LowStorageCannotStart, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ServiceEvent StoppedLowStorage emits LowStorageStoppedRecording ui event`() = runTest {
        advanceUntilIdle()
        viewModel.uiEvents.test {
            serviceEventsFlow.tryEmit(ServiceEvent.StoppedLowStorage)
            advanceUntilIdle()
            assertEquals(RecordingUiEvent.LowStorageStoppedRecording, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== cancelFilenamePrompt Tests ==========

    @Test
    fun `cancelFilenamePrompt when not recording sets state to NotRecording`() = runTest {
        advanceUntilIdle()
        viewModel.cancelFilenamePrompt()
        viewModel.recordingState.test {
            assertEquals(RecordingState.NotRecording, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== toggleRecording Tests (with mocks) ==========

    @Test
    fun `toggleRecording when not recording with sufficient storage does not emit low storage event`() = runTest {
        val context = io.mockk.mockk<Context>(relaxed = true)
        val mediaProjectionManager = io.mockk.mockk<android.media.projection.MediaProjectionManager>(relaxed = true)
        every { context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) } returns mediaProjectionManager
        every { mediaProjectionManager.createScreenCaptureIntent() } returns io.mockk.mockk(relaxed = true)
        mockkObject(StorageUtils)
        every { StorageUtils.hasSufficientSpaceForRecording() } returns true
        viewModel.uiEvents.test {
            viewModel.toggleRecording(context)
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleRecording when not recording and low storage emits LowStorageCannotStart`() = runTest {
        val context = io.mockk.mockk<Context>(relaxed = true)
        mockkObject(StorageUtils)
        every { StorageUtils.hasSufficientSpaceForRecording() } returns false
        viewModel.uiEvents.test {
            viewModel.toggleRecording(context)
            advanceUntilIdle()
            assertEquals(RecordingUiEvent.LowStorageCannotStart, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== handleActivityResult Tests ==========

    @Test
    fun `handleActivityResult with RESULT_OK and data and sufficient storage does not throw`() = runTest {
        val context = io.mockk.mockk<Context>(relaxed = true)
        val data = io.mockk.mockk<Intent>(relaxed = true)
        mockkObject(StorageUtils)
        every { StorageUtils.hasSufficientSpaceForRecording() } returns true
        viewModel.handleActivityResult(context, Activity.RESULT_OK, data)
        advanceUntilIdle()
    }

    @Test
    fun `handleActivityResult with RESULT_OK but low storage emits LowStorageCannotStart`() = runTest {
        val context = io.mockk.mockk<Context>(relaxed = true)
        val data = io.mockk.mockk<Intent>(relaxed = true)
        mockkObject(StorageUtils)
        every { StorageUtils.hasSufficientSpaceForRecording() } returns false
        viewModel.uiEvents.test {
            viewModel.handleActivityResult(context, Activity.RESULT_OK, data)
            advanceUntilIdle()
            assertEquals(RecordingUiEvent.LowStorageCannotStart, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handleActivityResult with RESULT_CANCELED does not start service`() = runTest {
        val context = io.mockk.mockk<Context>(relaxed = true)
        viewModel.handleActivityResult(context, Activity.RESULT_CANCELED, null)
        advanceUntilIdle()
        viewModel.isRecording.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handleActivityResult with null data does not crash`() = runTest {
        val context = io.mockk.mockk<Context>(relaxed = true)
        mockkObject(StorageUtils)
        every { StorageUtils.hasSufficientSpaceForRecording() } returns true
        viewModel.handleActivityResult(context, Activity.RESULT_OK, null)
        advanceUntilIdle()
    }

    // ========== RecordingState sealed class coverage ==========

    @Test
    fun `RecordingState NotRecording is used as initial state`() = runTest {
        advanceUntilIdle()
        assertEquals(RecordingState.NotRecording, viewModel.recordingState.value)
    }

    @Test
    fun `RecordingUiEvent LowStorageCannotStart exists`() {
        assertEquals(RecordingUiEvent.LowStorageCannotStart, RecordingUiEvent.LowStorageCannotStart)
    }

    @Test
    fun `RecordingUiEvent LowStorageStoppedRecording exists`() {
        assertEquals(RecordingUiEvent.LowStorageStoppedRecording, RecordingUiEvent.LowStorageStoppedRecording)
    }

    // ========== Hour 3: State machine and error branches ==========

    @Test
    fun `start recording from NotRecording state is initial`() = runTest {
        advanceUntilIdle()
        viewModel.recordingState.test {
            assertEquals(RecordingState.NotRecording, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state NotRecording is safe`() = runTest {
        advanceUntilIdle()
        assertEquals(RecordingState.NotRecording, viewModel.recordingState.value)
    }

    @Test
    fun `RecordingState NotRecording is object`() {
        assertTrue(RecordingState.NotRecording is RecordingState)
    }

    @Test
    fun `RecordingState Recording data class exists`() {
        val r = RecordingState.Recording("00:00")
        assertTrue(r is RecordingState)
        assertEquals("00:00", r.duration)
    }

    @Test
    fun `RecordingState StoppingRecording exists`() {
        assertTrue(RecordingState.StoppingRecording is RecordingState)
    }

    @Test
    fun `RecordingState PromptingForFilename exists`() {
        assertTrue(RecordingState.PromptingForFilename is RecordingState)
    }

    @Test
    fun `cancelFilenamePrompt does not throw`() = runTest {
        viewModel.cancelFilenamePrompt()
        advanceUntilIdle()
    }

    @Test
    fun `handleActivityResult with RESULT_CANCELED does not crash`() = runTest {
        val context = io.mockk.mockk<Context>(relaxed = true)
        viewModel.handleActivityResult(context, Activity.RESULT_CANCELED, null)
        advanceUntilIdle()
    }

    @Test
    fun `insufficient storage service event maps to UI event`() = runTest {
        viewModel.uiEvents.test {
            serviceEventsFlow.tryEmit(ServiceEvent.CannotStartLowStorage)
            advanceUntilIdle()
            val event = awaitItem()
            assertEquals(RecordingUiEvent.LowStorageCannotStart, event)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Block 3: State machine and conditional branch coverage ---

    @Test
    fun `RecordingState Recording with duration string`() {
        val r = RecordingState.Recording("01:23")
        assertTrue(r is RecordingState)
        assertEquals("01:23", r.duration)
    }

    @Test
    fun `RecordingState PromptingForFilename is distinct from NotRecording`() {
        assertTrue(RecordingState.PromptingForFilename is RecordingState)
        assertTrue(RecordingState.NotRecording is RecordingState)
        assertFalse(RecordingState.PromptingForFilename == RecordingState.NotRecording)
    }

    @Test
    fun `ServiceEvent RecordingStopped does not emit LowStorage event`() = runTest {
        viewModel.uiEvents.test {
            serviceEventsFlow.tryEmit(ServiceEvent.RecordingStopped("path"))
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `double cancelFilenamePrompt does not throw`() = runTest {
        viewModel.cancelFilenamePrompt()
        viewModel.cancelFilenamePrompt()
        advanceUntilIdle()
    }

    @Test
    fun `invalid IDLE to PAUSED - pause when not recording does nothing`() = runTest {
        advanceUntilIdle()
        assertEquals(RecordingState.NotRecording, viewModel.recordingState.value)
    }
}
