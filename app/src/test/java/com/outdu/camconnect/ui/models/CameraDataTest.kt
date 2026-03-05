package com.outdu.camconnect.ui.models

import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for CameraData and related model types.
 */
class CameraDataTest {

    @Test
    fun cameraState_defaultValues() {
        val state = CameraState()
        Assert.assertFalse(state.isRecording)
        Assert.assertEquals(0, state.currentCamera)
        Assert.assertEquals(1.0f, state.zoomLevel, 0.01f)
        Assert.assertEquals(CameraMode.HDR, state.cameraMode)
        Assert.assertEquals(VisionMode.VISION, state.visionMode)
    }

    @Test
    fun systemStatus_defaultValues() {
        val status = SystemStatus()
        Assert.assertEquals(100, status.batteryLevel)
        Assert.assertTrue(status.isWifiConnected)
        Assert.assertTrue(status.isAiEnabled)
    }
}
