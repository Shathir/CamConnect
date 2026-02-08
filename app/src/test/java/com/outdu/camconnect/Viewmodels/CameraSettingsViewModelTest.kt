package com.outdu.camconnect.Viewmodels

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CameraSettingsViewModel
 * Tests cover camera view mode settings state management
 */
class CameraSettingsViewModelTest {

    private lateinit var viewModel: CameraSettingsViewModel

    @Before
    fun setup() {
        viewModel = CameraSettingsViewModel()
    }

    @Test
    fun `initial visible view mode should be false`() {
        assertFalse(viewModel.visibleViewMode.value)
    }

    @Test
    fun `changeVisibleViewMode should toggle visible view`() {
        viewModel.changeVisibleViewMode()
        assertTrue(viewModel.visibleViewMode.value)
        
        viewModel.changeVisibleViewMode()
        assertFalse(viewModel.visibleViewMode.value)
    }

    @Test
    fun `changeInfraredViewMode should toggle infrared view`() {
        viewModel.changeInfraredViewMode()
        assertTrue(viewModel.infraredViewMode.value)
    }

    @Test
    fun `changeHdrCameraMode should toggle HDR`() {
        viewModel.changeHdrCameraMode()
        assertTrue(viewModel.hdrCameraMode.value)
        
        viewModel.changeHdrCameraMode()
        assertFalse(viewModel.hdrCameraMode.value)
    }

    @Test
    fun `changeStabilisationViewMode should toggle stabilisation`() {
        viewModel.changeStabilisationViewMode()
        assertTrue(viewModel.stabilisationViewMode.value)
    }

    @Test
    fun `changeIrOnMotionMode should toggle IR on motion`() {
        viewModel.changeIrOnMotionMode()
        assertTrue(viewModel.irOnMotion.value)
    }

    @Test
    fun `changeIrDayNightMode should toggle IR day night`() {
        viewModel.changeIrDayNightMode()
        assertTrue(viewModel.irDayNightMode.value)
    }

    @Test
    fun `initial isChecked states should be false`() {
        assertFalse(viewModel.isCheckedVisibleViewMode.value)
        assertFalse(viewModel.isCheckedInfraredViewMode.value)
        assertFalse(viewModel.isCheckedHdrCameraMode.value)
        assertFalse(viewModel.isCheckedStabilisationViewMode.value)
        assertFalse(viewModel.isCheckedIrOnMotionMode.value)
        assertFalse(viewModel.isCheckedIrDayNightMode.value)
    }

    @Test
    fun `changeIsCheckedVisibleViewMode should toggle`() {
        viewModel.changeIsCheckedVisibleViewMode()
        assertTrue(viewModel.isCheckedVisibleViewMode.value)
    }

    @Test
    fun `changeIsCheckedInfraredViewMode should toggle`() {
        viewModel.changeIsCheckedInfraredViewMode()
        assertTrue(viewModel.isCheckedInfraredViewMode.value)
    }

    @Test
    fun `changeIsCheckedHdrCameraMode should toggle`() {
        viewModel.changeIsCheckedHdrCameraMode()
        assertTrue(viewModel.isCheckedHdrCameraMode.value)
    }

    @Test
    fun `changeIsCheckedStabilisationViewMode should toggle`() {
        viewModel.changeIsCheckedStabilisationViewMode()
        assertTrue(viewModel.isCheckedStabilisationViewMode.value)
    }

    @Test
    fun `changeIsCheckedIrOnMotionMode should toggle`() {
        viewModel.changeIsCheckedIrOnMotionMode()
        assertTrue(viewModel.isCheckedIrOnMotionMode.value)
    }

    @Test
    fun `changeIsCheckedIrDayNightMode should toggle`() {
        viewModel.changeIsCheckedIrDayNightMode()
        assertTrue(viewModel.isCheckedIrDayNightMode.value)
    }

    @Test
    fun `multiple camera settings should update independently`() {
        // Update multiple settings
        viewModel.changeVisibleViewMode()
        viewModel.changeHdrCameraMode()
        viewModel.changeIsCheckedStabilisationViewMode()
        
        // Verify
        assertTrue(viewModel.visibleViewMode.value)
        assertTrue(viewModel.hdrCameraMode.value)
        assertTrue(viewModel.isCheckedStabilisationViewMode.value)
        assertFalse(viewModel.infraredViewMode.value) // Should remain unchanged
        assertFalse(viewModel.stabilisationViewMode.value) // Mode vs isChecked are independent
    }
}
