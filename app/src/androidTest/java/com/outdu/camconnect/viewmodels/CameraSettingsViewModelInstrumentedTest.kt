package com.outdu.camconnect.viewmodels

import com.outdu.camconnect.Viewmodels.CameraSettingsViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraSettingsViewModelInstrumentedTest {

    private lateinit var viewModel: CameraSettingsViewModel

    @Before
    fun setup() {
        viewModel = CameraSettingsViewModel()
    }

    @Test
    fun initial_visibleViewMode_isFalse() {
        assertFalse(viewModel.visibleViewMode.value)
    }

    @Test
    fun changeVisibleViewMode_togglesValue() {
        viewModel.changeVisibleViewMode()
        assertTrue(viewModel.visibleViewMode.value)
        viewModel.changeVisibleViewMode()
        assertFalse(viewModel.visibleViewMode.value)
    }

    @Test
    fun changeHdrCameraMode_togglesValue() {
        viewModel.changeHdrCameraMode()
        assertTrue(viewModel.hdrCameraMode.value)
    }

    @Test
    fun changeIsCheckedVisibleViewMode_togglesChecked() {
        viewModel.changeIsCheckedVisibleViewMode()
        assertTrue(viewModel.isCheckedVisibleViewMode.value)
    }
}
