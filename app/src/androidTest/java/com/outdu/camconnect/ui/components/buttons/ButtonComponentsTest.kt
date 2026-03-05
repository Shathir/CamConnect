package com.outdu.camconnect.ui.components.buttons

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.components.controls.RecordingToggle
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for button components (RecordingToggle, zoom selector).
 */
@RunWith(AndroidJUnit4::class)
class ButtonComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun recordingToggle_rendersWhenNotRecording() {
        composeTestRule.setContent {
            CamConnectTheme {
                RecordingToggle(isRecording = false, onToggle = {})
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNode(hasClickAction()).assertExists()
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
    fun recordingToggle_clickFiresCallback() {
        var clicked = false
        composeTestRule.setContent {
            CamConnectTheme {
                RecordingToggle(isRecording = true, onToggle = { clicked = true })
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNode(hasClickAction()).performClick()
        Thread.sleep(500) // Allow composition
        assert(clicked)
    }
}
