package com.outdu.camconnect.ui.components.dialogs

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for SingleButtonAlertDialog composable.
 */
@RunWith(AndroidJUnit4::class)
class SingleButtonAlertDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dialog_rendersWithTitleAndMessage() {
        composeTestRule.setContent {
            CamConnectTheme {
                SingleButtonAlertDialog(
                    title = "Test Title",
                    message = "Test message body",
                    onOk = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Test Title").assertExists()
        composeTestRule.onNodeWithText("Test message body").assertExists()
    }

    @Test
    fun dialog_buttonClickTriggersCallback() {
        var okCalled = false
        composeTestRule.setContent {
            CamConnectTheme {
                SingleButtonAlertDialog(
                    title = "Title",
                    message = "Message",
                    onOk = { okCalled = true }
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("OK").performClick()
        Thread.sleep(500) // Allow composition
        assert(okCalled)
    }

    @Test
    fun dialog_customButtonTextDisplays() {
        composeTestRule.setContent {
            CamConnectTheme {
                SingleButtonAlertDialog(
                    title = "Title",
                    message = "Message",
                    buttonText = "Got it",
                    onOk = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Got it").assertExists()
        composeTestRule.onNodeWithText("Got it").performClick()
    }

    @Test
    fun dialog_defaultButtonTextIsOk() {
        composeTestRule.setContent {
            CamConnectTheme {
                SingleButtonAlertDialog(
                    title = "Title",
                    message = "Message",
                    onOk = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("OK").assertExists()
    }

    @Test
    fun dialog_rendersWithoutCrash() {
        composeTestRule.setContent {
            CamConnectTheme {
                SingleButtonAlertDialog(
                    title = "Error",
                    message = "Something went wrong.",
                    buttonText = "Dismiss",
                    onOk = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Error").assertExists()
    }
}
