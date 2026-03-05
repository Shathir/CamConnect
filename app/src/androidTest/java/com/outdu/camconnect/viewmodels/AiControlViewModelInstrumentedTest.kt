package com.outdu.camconnect.viewmodels

import com.outdu.camconnect.Viewmodels.AiControlViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AiControlViewModelInstrumentedTest {

    private lateinit var viewModel: AiControlViewModel

    @Before
    fun setup() {
        viewModel = AiControlViewModel()
    }

    @Test
    fun initial_detectObjects_isFalse() {
        assertFalse(viewModel.detectObjects.value)
    }

    @Test
    fun initial_detectionThreshold_isZero() {
        assertEquals(0, viewModel.detectionThreshold.value)
    }

    @Test
    fun changeDetectObjectMode_togglesValue() {
        viewModel.changeDetectObjectMode()
        assertTrue(viewModel.detectObjects.value)
        viewModel.changeDetectObjectMode()
        assertFalse(viewModel.detectObjects.value)
    }

    @Test
    fun changeDetectionThreshold_updatesValue() {
        viewModel.changeDetectionThreshold(75)
        assertEquals(75, viewModel.detectionThreshold.value)
    }

    @Test
    fun changeDetectionModel_updatesValue() {
        viewModel.changeDetectionModel(2)
        assertEquals(2, viewModel.detectionModel.value)
    }

    @Test
    fun changeIsCheckedDetectObjectMode_togglesChecked() {
        viewModel.changeIsCheckedDetectObjectMode()
        assertTrue(viewModel.isCheckedDetectObjects.value)
    }
}
