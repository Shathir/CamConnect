package com.outdu.camconnect.Viewmodels

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AiControlViewModel
 * Tests cover AI detection settings state management
 */
class AiControlViewModelTest {

    private lateinit var viewModel: AiControlViewModel

    @Before
    fun setup() {
        viewModel = AiControlViewModel()
    }

    @Test
    fun `initial detect objects mode should be false`() {
        assertFalse(viewModel.detectObjects.value)
    }

    @Test
    fun `changeDetectObjectMode should toggle detect objects`() {
        viewModel.changeDetectObjectMode()
        assertTrue(viewModel.detectObjects.value)
        
        viewModel.changeDetectObjectMode()
        assertFalse(viewModel.detectObjects.value)
    }

    @Test
    fun `changeDetectFarObjectsMode should toggle detect far objects`() {
        viewModel.changeDetectFarObjectsMode()
        assertTrue(viewModel.detectFarObjects.value)
    }

    @Test
    fun `changeDepthSenseEnable should toggle depth sense`() {
        viewModel.changeDepthSenseEnable()
        assertTrue(viewModel.depthSenseEnable.value)
        
        viewModel.changeDepthSenseEnable()
        assertFalse(viewModel.depthSenseEnable.value)
    }

    @Test
    fun `initial detection threshold should be 0`() {
        assertEquals(0, viewModel.detectionThreshold.value)
    }

    @Test
    fun `changeDetectionThreshold should update threshold`() {
        viewModel.changeDetectionThreshold(50)
        assertEquals(50, viewModel.detectionThreshold.value)
        
        viewModel.changeDetectionThreshold(75)
        assertEquals(75, viewModel.detectionThreshold.value)
    }

    @Test
    fun `initial detection model should be 0`() {
        assertEquals(0, viewModel.detectionModel.value)
    }

    @Test
    fun `changeDetectionModel should update model`() {
        viewModel.changeDetectionModel(1)
        assertEquals(1, viewModel.detectionModel.value)
        
        viewModel.changeDetectionModel(2)
        assertEquals(2, viewModel.detectionModel.value)
    }

    @Test
    fun `initial isChecked states should be false`() {
        assertFalse(viewModel.isCheckedDetectObjects.value)
        assertFalse(viewModel.isCheckedDetectFarObjects.value)
        assertFalse(viewModel.isCheckedDepthSenseEnable.value)
        assertFalse(viewModel.isCheckedDetectionModel.value)
    }

    @Test
    fun `changeIsCheckedDetectObjectMode should toggle`() {
        viewModel.changeIsCheckedDetectObjectMode()
        assertTrue(viewModel.isCheckedDetectObjects.value)
    }

    @Test
    fun `changeIsCheckedDetectFarObjectsMode should toggle`() {
        viewModel.changeIsCheckedDetectFarObjectsMode()
        assertTrue(viewModel.isCheckedDetectFarObjects.value)
    }

    @Test
    fun `changeIsCheckedDepthSenseEnable should toggle`() {
        viewModel.changeIsCheckedDepthSenseEnable()
        assertTrue(viewModel.isCheckedDepthSenseEnable.value)
    }

    @Test
    fun `changeIsCheckedDetectionModel should toggle`() {
        viewModel.changeIsCheckedDetectionModel()
        assertTrue(viewModel.isCheckedDetectionModel.value)
    }

    @Test
    fun `multiple AI settings should update independently`() {
        // Update multiple settings
        viewModel.changeDetectObjectMode()
        viewModel.changeDetectionThreshold(60)
        viewModel.changeDetectionModel(1)
        
        // Verify
        assertTrue(viewModel.detectObjects.value)
        assertEquals(60, viewModel.detectionThreshold.value)
        assertEquals(1, viewModel.detectionModel.value)
        assertFalse(viewModel.detectFarObjects.value) // Should remain unchanged
    }
}
