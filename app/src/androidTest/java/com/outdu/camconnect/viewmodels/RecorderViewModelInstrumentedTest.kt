package com.outdu.camconnect.viewmodels

import com.outdu.camconnect.Viewmodels.RecorderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecorderViewModelInstrumentedTest {

    private lateinit var viewModel: RecorderViewModel

    @Before
    fun setup() {
        viewModel = RecorderViewModel()
    }

    @Test
    fun initial_isRecording_isFalse() = runBlocking {
        assertFalse(viewModel.isRecording.first())
    }

    @Test
    fun initial_elapsedTime_isZero() = runBlocking {
        assertEquals(0, viewModel.elapsedTime.first())
    }

    @Test
    fun start_setsIsRecordingTrue() = runBlocking {
        viewModel.start()
        delay(50)
        assertTrue(viewModel.isRecording.first())
    }

    @Test
    fun stop_afterStart_setsIsRecordingFalse() = runBlocking {
        viewModel.start()
        delay(100)
        viewModel.stop()
        delay(50)
        assertFalse(viewModel.isRecording.first())
    }

    @Test
    fun elapsedTime_increasesAfterStart() = runBlocking {
        viewModel.start()
        delay(1500)
        val elapsed = viewModel.elapsedTime.first()
        assertTrue(elapsed >= 1)
        viewModel.stop()
    }
}
