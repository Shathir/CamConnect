package com.outdu.camconnect.ui.components.buttons

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ZoomSelector (primary export from buttons package used for recording/zoom).
 */
@RunWith(AndroidJUnit4::class)
class RecordingButtonTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun zoomSelector_rendersZoomLevels() {
        composeTestRule.setContent {
            CamConnectTheme {
                ZoomSelector(
                    zoomLevels = listOf(1f, 2f, 4f),
                    initialZoom = 1f,
                    onZoomChanged = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("1X").assertExists()
        composeTestRule.onNodeWithText("2X").assertExists()
        composeTestRule.onNodeWithText("4X").assertExists()
    }

    @Test
    fun zoomSelector_clickTriggersCallback() {
        var lastZoom: Float? = null
        composeTestRule.setContent {
            CamConnectTheme {
                ZoomSelector(
                    zoomLevels = listOf(1f, 2f, 4f),
                    initialZoom = 1f,
                    onZoomChanged = { lastZoom = it }
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("2X").performClick()
        Thread.sleep(500) // Allow composition
        assert(lastZoom == 2f)
    }
}
