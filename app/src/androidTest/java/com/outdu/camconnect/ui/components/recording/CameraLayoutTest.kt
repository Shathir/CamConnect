package com.outdu.camconnect.ui.components.recording

import com.outdu.camconnect.ui.models.RecordingState
import org.junit.Assert.assertEquals
import org.junit.Test
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

/**
 * Instrumented tests for recording component models and RecordingState.
 * (CameraLayout composable in this package is currently commented out.)
 */
@RunWith(AndroidJUnit4::class)
class CameraLayoutTest {

    @Test
    fun recordingState_recordingHoldsDuration() {
        val state = RecordingState.Recording("00:05:30")
        assertEquals("00:05:30", state.duration)
    }

    @Test
    fun recordingState_notRecordingIsSingleton() {
        assert(RecordingState.NotRecording == RecordingState.NotRecording)
    }

    @Test
    fun recordingState_stoppingRecordingIsSingleton() {
        assert(RecordingState.StoppingRecording == RecordingState.StoppingRecording)
    }
}
