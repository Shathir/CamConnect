package com.outdu.camconnect.ui.components

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.models.RecordingState
import com.outdu.camconnect.ui.viewmodels.RecordingUiEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for recording control types and state.
 */
@RunWith(AndroidJUnit4::class)
class RecordingControlsTest {

    @Test
    fun recordingState_notRecording() {
        assertEquals(RecordingState.NotRecording, RecordingState.NotRecording)
    }

    @Test
    fun recordingState_recordingHoldsDuration() {
        val state = RecordingState.Recording("02:30")
        assertTrue(state is RecordingState.Recording)
        assertEquals("02:30", (state as RecordingState.Recording).duration)
    }

    @Test
    fun recordingState_stoppingRecording() {
        assertEquals(RecordingState.StoppingRecording, RecordingState.StoppingRecording)
    }

    @Test
    fun recordingState_savedToGallery() {
        assertEquals(RecordingState.SavedToGallery, RecordingState.SavedToGallery)
    }

    @Test
    fun recordingUiEvent_lowStorage() {
        assertEquals(RecordingUiEvent.LowStorageCannotStart, RecordingUiEvent.LowStorageCannotStart)
        assertEquals(RecordingUiEvent.LowStorageStoppedRecording, RecordingUiEvent.LowStorageStoppedRecording)
    }
}
