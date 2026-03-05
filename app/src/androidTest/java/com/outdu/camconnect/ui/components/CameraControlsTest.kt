package com.outdu.camconnect.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.components.controls.RecordingToggle
import com.outdu.camconnect.ui.components.controls.ZoomSelector1
import com.outdu.camconnect.ui.theme.CamConnectTheme
import com.outdu.camconnect.ui.viewmodels.IrIntensityLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for camera control types and composables (RecordingToggle, ZoomSelector).
 */
@RunWith(AndroidJUnit4::class)
class CameraControlsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun irIntensityLevel_displayNames() {
        assertEquals("Off", IrIntensityLevel.OFF.displayName)
        assertEquals("Low", IrIntensityLevel.LOW.displayName)
        assertEquals("Medium", IrIntensityLevel.MEDIUM.displayName)
        assertEquals("High", IrIntensityLevel.HIGH.displayName)
        assertEquals("Max", IrIntensityLevel.MAX.displayName)
        assertEquals("Ultra", IrIntensityLevel.ULTRA.displayName)
    }

    @Test
    fun irIntensityLevel_brightnessValues() {
        assertEquals(0, IrIntensityLevel.OFF.brightness)
        assertEquals(10, IrIntensityLevel.ULTRA.brightness)
    }

    @Test
    fun irIntensityLevel_fromBrightness() {
        assertEquals(IrIntensityLevel.OFF, IrIntensityLevel.fromBrightness(0))
        assertEquals(IrIntensityLevel.LOW, IrIntensityLevel.fromBrightness(2))
        assertEquals(IrIntensityLevel.ULTRA, IrIntensityLevel.fromBrightness(10))
        assertEquals(IrIntensityLevel.OFF, IrIntensityLevel.fromBrightness(5))
    }

    @Test
    fun recordingToggle_renders() {
        composeTestRule.setContent {
            CamConnectTheme {
                RecordingToggle(isRecording = false, onToggle = {})
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNode(hasClickAction()).assertExists()
    }

    @Test
    fun recordingToggle_clickTriggersCallback() {
        var toggled = false
        composeTestRule.setContent {
            CamConnectTheme {
                RecordingToggle(isRecording = false, onToggle = { toggled = true })
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNode(hasClickAction()).performClick()
        Thread.sleep(500) // Allow composition
        assertTrue(toggled)
    }

    @Test
    fun recordingToggle_rendersWhenRecording() {
        composeTestRule.setContent {
            CamConnectTheme {
                RecordingToggle(isRecording = true, onToggle = {})
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNode(hasClickAction()).assertExists()
    }

    @Test
    fun zoomSelector1_renders() {
        var selectedZoom = 0f
        composeTestRule.setContent {
            CamConnectTheme {
                ZoomSelector1(
                    currentZoom = 1f,
                    onZoomSelected = { selectedZoom = it },
                    availableZoomLevels = listOf(1f, 2f, 4f)
                )
            }
        }
        Thread.sleep(500) // Allow composition
        // ZoomSelector1 has multiple clickable chips (1f, 2f, 4f)
        composeTestRule.onAllNodes(hasClickAction()).get(0).assertExists()
    }

    @Test
    fun zoomSelector1_clickTriggersCallback() {
        var selectedZoom = 0f
        composeTestRule.setContent {
            CamConnectTheme {
                ZoomSelector1(
                    currentZoom = 1f,
                    onZoomSelected = { selectedZoom = it },
                    availableZoomLevels = listOf(1f, 2f)
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onAllNodes(hasClickAction()).get(0).performClick()
        Thread.sleep(500) // Allow composition
        assertTrue(selectedZoom == 1f || selectedZoom == 2f)
    }
}
