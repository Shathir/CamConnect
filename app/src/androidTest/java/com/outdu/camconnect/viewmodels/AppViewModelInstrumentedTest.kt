package com.outdu.camconnect.viewmodels

import com.outdu.camconnect.Viewmodels.AppViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppViewModelInstrumentedTest {

    private lateinit var viewModel: AppViewModel

    @Before
    fun setup() {
        viewModel = AppViewModel()
    }

    @Test
    fun initial_motionValue_isFalse() {
        assertFalse(viewModel.motionvalue.value)
    }

    @Test
    fun initial_recordModeValue_isFalse() {
        assertFalse(viewModel.recordmodevalue.value)
    }

    @Test
    fun initial_isPlaying_isTrue() {
        assertTrue(viewModel.isPlaying.value)
    }

    @Test
    fun setPlaying_updatesState() {
        viewModel.setPlaying(false)
        assertFalse(viewModel.isPlaying.value)
        viewModel.setPlaying(true)
        assertTrue(viewModel.isPlaying.value)
    }

    @Test
    fun changeMotionMode_togglesValue() {
        viewModel.changeMotionMode()
        assertTrue(viewModel.motionvalue.value)
        viewModel.changeMotionMode()
        assertFalse(viewModel.motionvalue.value)
    }

    @Test
    fun changeRecordMode_togglesValue() {
        viewModel.changeRecordMode()
        assertTrue(viewModel.recordmodevalue.value)
        viewModel.changeRecordMode()
        assertFalse(viewModel.recordmodevalue.value)
    }

    @Test
    fun initial_irMode_isFalse() {
        assertFalse(viewModel.irMode.value)
    }

    @Test
    fun initial_zoomCameraMode_isZero() {
        assertEquals(0, viewModel.zoomCameraMode.value)
    }
}
