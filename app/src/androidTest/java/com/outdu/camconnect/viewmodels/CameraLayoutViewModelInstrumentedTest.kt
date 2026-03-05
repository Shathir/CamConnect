package com.outdu.camconnect.viewmodels

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.Viewmodels.CameraLayoutViewModel
import com.outdu.camconnect.ui.models.CameraMode
import com.outdu.camconnect.ui.models.OrientationMode
import com.outdu.camconnect.ui.models.VisionMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CameraLayoutViewModel.
 * Covers stream reload, vision/camera/orientation modes, unsaved changes, and apply flow
 * with real Android ViewModel and coroutines.
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CameraLayoutViewModelInstrumentedTest {

    private lateinit var viewModel: CameraLayoutViewModel

    @Before
    fun setup() {
        viewModel = CameraLayoutViewModel()
        // Allow init/fetchCameraSettings to run (async; may complete with error without camera)
        runBlocking { delay(800) }
    }

    // ========== Initial state ==========

    @Test
    fun initial_autoDayNight_isFalse() = runBlocking {
        assertFalse(viewModel.isAutoDayNightEnabled.value)
    }

    @Test
    fun initial_visionMode_isVision() = runBlocking {
        assertEquals(VisionMode.VISION, viewModel.currentVisionMode.value)
    }

    @Test
    fun initial_cameraMode_isOff() = runBlocking {
        assertEquals(CameraMode.OFF, viewModel.currentCameraMode.value)
    }

    @Test
    fun initial_orientationMode_isNormal() = runBlocking {
        assertEquals(OrientationMode.NORMAL, viewModel.currentOrientationMode.value)
    }

    @Test
    fun initial_hasUnsavedChanges_isFalse() = runBlocking {
        assertFalse(viewModel.hasUnsavedChanges.value)
    }

    @Test
    fun initial_isStreamReloading_isFalse() = runBlocking {
        assertFalse(viewModel.isStreamReloading.first())
    }

    @Test
    fun initial_streamReloadStatusText_isNull() = runBlocking {
        assertNull(viewModel.streamReloadStatusText.first())
    }

    @Test
    fun initial_isUIInteractive_isTrue() = runBlocking {
        assertTrue(viewModel.isUIInteractive.first())
    }

    @Test
    fun initial_appliedVisionMode_isVision() = runBlocking {
        assertEquals(VisionMode.VISION, viewModel.appliedVisionMode.value)
    }

    @Test
    fun initial_appliedCameraMode_isOff() = runBlocking {
        assertEquals(CameraMode.OFF, viewModel.appliedCameraMode.value)
    }

    // ========== Stream reload ==========

    @Test
    fun triggerStreamReload_setsIsStreamReloadingTrue() = runBlocking {
        viewModel.triggerStreamReload(durationMs = 5000L)
        delay(50)
        assertTrue(viewModel.isStreamReloading.first())
    }

    @Test
    fun triggerStreamReload_afterDuration_clearsReloading() = runBlocking {
        viewModel.triggerStreamReload(durationMs = 100L)
        delay(50)
        assertTrue(viewModel.isStreamReloading.first())
        delay(120)
        assertFalse(viewModel.isStreamReloading.first())
    }

    @Test
    fun triggerStreamReload_withReason_invokesWithoutCrash() = runBlocking {
        viewModel.triggerStreamReload(durationMs = 200L, reason = "test")
        delay(50)
        assertTrue(viewModel.isStreamReloading.first())
        delay(250)
        assertFalse(viewModel.isStreamReloading.first())
    }

    @Test
    fun beginStreamReload_setsReloadingTrue() = runBlocking {
        viewModel.beginStreamReload(reason = "test", watchdogMs = 60_000L)
        delay(50)
        assertTrue(viewModel.isStreamReloading.first())
    }

    @Test
    fun endStreamReload_clearsReloading() = runBlocking {
        viewModel.beginStreamReload(watchdogMs = 60_000L)
        delay(50)
        assertTrue(viewModel.isStreamReloading.first())
        viewModel.endStreamReload(delayMs = 0L)
        delay(100)
        assertFalse(viewModel.isStreamReloading.first())
    }

    @Test
    fun endStreamReload_withDelay_clearsAfterDelay() = runBlocking {
        viewModel.beginStreamReload(watchdogMs = 60_000L)
        delay(50)
        viewModel.endStreamReload(delayMs = 50L)
        delay(30)
        assertTrue(viewModel.isStreamReloading.first())
        delay(80)
        assertFalse(viewModel.isStreamReloading.first())
    }

    @Test
    fun setStreamReloadCallback_invokedOnTrigger() = runBlocking {
        var invoked = false
        viewModel.setStreamReloadCallback { invoked = true }
        viewModel.triggerStreamReload(durationMs = 200L)
        delay(80)
        assertTrue(invoked)
    }

    // ========== setWsChangingMisc ==========

    @Test
    fun setWsChangingMisc_withOldAndNew_setsStatusText() = runBlocking {
        viewModel.setWsChangingMisc(oldMisc = 1, newMisc = 5)
        val text = viewModel.streamReloadStatusText.first()
        assertNotNull(text)
        assertTrue(text!!.contains("Changing") && text.contains("mode"))
    }

    @Test
    fun setWsChangingMisc_withNewOnly_setsStatusText() = runBlocking {
        viewModel.setWsChangingMisc(oldMisc = null, newMisc = 9)
        val text = viewModel.streamReloadStatusText.first()
        assertNotNull(text)
        assertTrue(text!!.contains("Changing"))
    }

    @Test
    fun setWsChangingMisc_withBothNull_clearsStatusText() = runBlocking {
        viewModel.setWsChangingMisc(oldMisc = 1, newMisc = 5)
        delay(20)
        viewModel.setWsChangingMisc(oldMisc = null, newMisc = null)
        delay(20)
        assertNull(viewModel.streamReloadStatusText.first())
    }

    // ========== Vision / camera / orientation setters ==========

    @Test
    fun setVisionMode_updatesCurrentVisionMode() = runBlocking {
        viewModel.setVisionMode(VisionMode.BOTH)
        assertEquals(VisionMode.BOTH, viewModel.currentVisionMode.value)
    }

    @Test
    fun setVisionMode_toInfrared_updatesMode() = runBlocking {
        viewModel.setVisionMode(VisionMode.INFRARED)
        assertEquals(VisionMode.INFRARED, viewModel.currentVisionMode.value)
    }

    @Test
    fun setVisionMode_toVision_resetsCameraModeToOffWhenNotFourK() = runBlocking {
        viewModel.setCameraMode(CameraMode.EIS)
        viewModel.setVisionMode(VisionMode.BOTH)
        viewModel.setVisionMode(VisionMode.VISION)
        assertEquals(CameraMode.OFF, viewModel.currentCameraMode.value)
    }

    @Test
    fun setCameraMode_updatesCurrentCameraMode() = runBlocking {
        viewModel.setCameraMode(CameraMode.EIS)
        assertEquals(CameraMode.EIS, viewModel.currentCameraMode.value)
    }

    @Test
    fun setCameraMode_toHDR_updatesMode() = runBlocking {
        viewModel.setCameraMode(CameraMode.HDR)
        assertEquals(CameraMode.HDR, viewModel.currentCameraMode.value)
    }

    @Test
    fun setCameraMode_toFourK_updatesMode() = runBlocking {
        viewModel.setCameraMode(CameraMode.FOURK)
        assertEquals(CameraMode.FOURK, viewModel.currentCameraMode.value)
    }

    @Test
    fun setOrientationMode_updatesCurrentOrientationMode() = runBlocking {
        viewModel.setOrientationMode(OrientationMode.FLIP)
        assertEquals(OrientationMode.FLIP, viewModel.currentOrientationMode.value)
    }

    @Test
    fun setOrientationMode_toBoth_updatesMode() = runBlocking {
        viewModel.setOrientationMode(OrientationMode.BOTH)
        assertEquals(OrientationMode.BOTH, viewModel.currentOrientationMode.value)
    }

    // ========== Auto day/night ==========

    @Test
    fun setAutoDayNight_true_updatesState() = runBlocking {
        viewModel.setAutoDayNight(true)
        assertTrue(viewModel.isAutoDayNightEnabled.value)
    }

    @Test
    fun setAutoDayNight_false_updatesState() = runBlocking {
        viewModel.setAutoDayNight(true)
        viewModel.setAutoDayNight(false)
        assertFalse(viewModel.isAutoDayNightEnabled.value)
    }

    // ========== Unsaved changes ==========

    @Test
    fun setVisionMode_setsHasUnsavedChanges() = runBlocking {
        viewModel.setVisionMode(VisionMode.BOTH)
        assertTrue(viewModel.hasUnsavedChanges.value)
    }

    @Test
    fun setCameraMode_setsHasUnsavedChanges() = runBlocking {
        viewModel.setCameraMode(CameraMode.EIS)
        assertTrue(viewModel.hasUnsavedChanges.value)
    }

    @Test
    fun setOrientationMode_setsHasUnsavedChanges() = runBlocking {
        viewModel.setOrientationMode(OrientationMode.MIRROR)
        assertTrue(viewModel.hasUnsavedChanges.value)
    }

    @Test
    fun setAutoDayNight_setsHasUnsavedChanges() = runBlocking {
        viewModel.setAutoDayNight(true)
        assertTrue(viewModel.hasUnsavedChanges.value)
    }

    // ========== Toggle modes ==========

    @Test
    fun toggleCameraMode_eis_togglesToEis() = runBlocking {
        viewModel.toggleCameraMode(CameraMode.EIS)
        assertEquals(CameraMode.EIS, viewModel.currentCameraMode.value)
    }

    @Test
    fun toggleCameraMode_eis_twice_returnsToOff() = runBlocking {
        viewModel.toggleCameraMode(CameraMode.EIS)
        viewModel.toggleCameraMode(CameraMode.EIS)
        assertEquals(CameraMode.OFF, viewModel.currentCameraMode.value)
    }

    @Test
    fun toggleCameraMode_hdr_togglesToHdr() = runBlocking {
        viewModel.toggleCameraMode(CameraMode.HDR)
        assertEquals(CameraMode.HDR, viewModel.currentCameraMode.value)
    }

    @Test
    fun toggleOrientationMode_flip_togglesToFlip() = runBlocking {
        viewModel.toggleOrientationMode(OrientationMode.FLIP)
        assertEquals(OrientationMode.FLIP, viewModel.currentOrientationMode.value)
    }

    @Test
    fun toggleOrientationMode_flip_twice_returnsToNormal() = runBlocking {
        viewModel.toggleOrientationMode(OrientationMode.FLIP)
        viewModel.toggleOrientationMode(OrientationMode.FLIP)
        assertEquals(OrientationMode.NORMAL, viewModel.currentOrientationMode.value)
    }

    @Test
    fun toggleOrientationMode_mirror_togglesToMirror() = runBlocking {
        viewModel.toggleOrientationMode(OrientationMode.MIRROR)
        assertEquals(OrientationMode.MIRROR, viewModel.currentOrientationMode.value)
    }

    // ========== isLowLightModeActive ==========

    @Test
    fun isLowLightModeActive_whenVision_false() = runBlocking {
        viewModel.setVisionMode(VisionMode.VISION)
        assertFalse(viewModel.isLowLightModeActive)
    }

    @Test
    fun isLowLightModeActive_whenBoth_true() = runBlocking {
        viewModel.setVisionMode(VisionMode.BOTH)
        assertTrue(viewModel.isLowLightModeActive)
    }

    // ========== applyChanges & refreshSettings ==========

    @Test
    fun applyChanges_completesWithoutCrash() = runBlocking {
        viewModel.setVisionMode(VisionMode.BOTH)
        viewModel.applyChanges()
        delay(2000)
        assertTrue(viewModel.isUIInteractive.first())
    }

    @Test
    fun applyChanges_onlyOrientation_completesWithoutCrash() = runBlocking {
        viewModel.setOrientationMode(OrientationMode.FLIP)
        viewModel.applyChanges()
        delay(1500)
        assertTrue(viewModel.isUIInteractive.first())
    }

    @Test
    fun refreshSettings_invokesWithoutCrash() = runBlocking {
        viewModel.refreshSettings()
        delay(500)
    }
}
