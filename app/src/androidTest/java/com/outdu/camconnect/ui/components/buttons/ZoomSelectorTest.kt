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
 * Instrumented tests for ZoomSelector composable.
 */
@RunWith(AndroidJUnit4::class)
class ZoomSelectorTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun zoomSelector_rendersDefaultLevels() {
        composeTestRule.setContent {
            CamConnectTheme {
                ZoomSelector(initialZoom = 1f, onZoomChanged = {})
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("1X").assertExists()
        composeTestRule.onNodeWithText("2X").assertExists()
        composeTestRule.onNodeWithText("4X").assertExists()
    }

    @Test
    fun zoomSelector_callbackReceivesSelectedZoom() {
        var receivedZoom: Float? = null
        composeTestRule.setContent {
            CamConnectTheme {
                ZoomSelector(
                    initialZoom = 1f,
                    onZoomChanged = { receivedZoom = it }
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("4X").performClick()
        Thread.sleep(500) // Allow composition
        assert(receivedZoom == 4f)
    }
}
