package com.outdu.camconnect.Viewmodels

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AppViewModel
 * Tests cover state management and toggle functions
 */
class AppViewModelTest {

    private lateinit var viewModel: AppViewModel

    @Before
    fun setup() {
        viewModel = AppViewModel()
    }

    @Test
    fun `initial motion mode should be false`() {
        assertFalse(viewModel.motionvalue.value)
    }

    @Test
    fun `changeMotionMode should toggle motion mode`() {
        // Initial state
        val initialValue = viewModel.motionvalue.value
        
        // Act
        viewModel.changeMotionMode()
        
        // Assert
        assertEquals(!initialValue, viewModel.motionvalue.value)
    }

    @Test
    fun `changeMotionOverrideMode should toggle motion override`() {
        viewModel.changeMotionOverrideMode()
        assertTrue(viewModel.motionoverridevalue.value)
        
        viewModel.changeMotionOverrideMode()
        assertFalse(viewModel.motionoverridevalue.value)
    }

    @Test
    fun `changeRecordMode should toggle record mode`() {
        viewModel.changeRecordMode()
        assertTrue(viewModel.recordmodevalue.value)
    }

    @Test
    fun `changeModeClick should toggle mode clicked`() {
        viewModel.changeModeClick()
        assertTrue(viewModel.modeclicked.value)
    }

    @Test
    fun `initial IR mode should be false`() {
        assertFalse(viewModel.irMode.value)
    }

    @Test
    fun `changeIRMode should toggle IR mode`() {
        viewModel.changeIRMode()
        assertTrue(viewModel.irMode.value)
        
        viewModel.changeIRMode()
        assertFalse(viewModel.irMode.value)
    }

    @Test
    fun `changeIrCutFilterMode should toggle IR cut filter`() {
        viewModel.changeIrCutFilterMode()
        assertTrue(viewModel.irCutFilterMode.value)
    }

    @Test
    fun `changeFlipCameraMode should toggle flip camera`() {
        viewModel.changeFlipCameraMode()
        assertTrue(viewModel.flipCameraMode.value)
    }

    @Test
    fun `changeMirrorCameraMode should toggle mirror camera`() {
        viewModel.changeMirrorCameraMode()
        assertTrue(viewModel.mirrorCameraMode.value)
    }

    @Test
    fun `changeZoomCameraMode should update zoom value`() {
        viewModel.changeZoomCameraMode(5)
        assertEquals(5, viewModel.zoomCameraMode.value)
        
        viewModel.changeZoomCameraMode(10)
        assertEquals(10, viewModel.zoomCameraMode.value)
    }

    @Test
    fun `changeIrMidIntensity should update mid intensity`() {
        viewModel.changeIrMidIntensity(50)
        assertEquals(50, viewModel.irMidIntensity.value)
    }

    @Test
    fun `changeIrExtremeIntensity should update extreme intensity`() {
        viewModel.changeIrExtremeIntensity(100)
        assertEquals(100, viewModel.irExtremeIntensity.value)
    }

    @Test
    fun `initial playing state should be true`() {
        assertTrue(viewModel.isPlaying.value)
    }

    @Test
    fun `changePlayingState should toggle playing state`() {
        viewModel.changePlayingState()
        assertFalse(viewModel.isPlaying.value)
        
        viewModel.changePlayingState()
        assertTrue(viewModel.isPlaying.value)
    }

    @Test
    fun `setPlaying should set playing state`() {
        viewModel.setPlaying(false)
        assertFalse(viewModel.isPlaying.value)
        
        viewModel.setPlaying(true)
        assertTrue(viewModel.isPlaying.value)
    }

    @Test
    fun `multiple toggle operations should work correctly`() {
        // Toggle multiple settings
        viewModel.changeMotionMode()
        viewModel.changeIRMode()
        viewModel.changeFlipCameraMode()
        
        // Verify all are toggled
        assertTrue(viewModel.motionvalue.value)
        assertTrue(viewModel.irMode.value)
        assertTrue(viewModel.flipCameraMode.value)
    }
}
