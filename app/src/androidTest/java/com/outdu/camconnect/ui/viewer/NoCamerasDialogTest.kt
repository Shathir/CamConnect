package com.outdu.camconnect.ui.viewer

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for NoCamerasDialog.
 */
@RunWith(AndroidJUnit4::class)
class NoCamerasDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun noCamerasDialog_rendersTitle() {
        composeTestRule.setContent {
            CamConnectTheme {
                NoCamerasDialog(
                    onGoToWifiSettings = {},
                    onRetry = {},
                    onDismiss = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("No Cameras Found").assertExists()
    }

    @Test
    fun noCamerasDialog_retryTriggersCallback() {
        var retryClicked = false
        composeTestRule.setContent {
            CamConnectTheme {
                NoCamerasDialog(
                    onGoToWifiSettings = {},
                    onRetry = { retryClicked = true },
                    onDismiss = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Retry").performClick()
        Thread.sleep(500) // Allow composition
        assert(retryClicked)
    }
}
