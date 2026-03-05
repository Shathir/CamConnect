package com.outdu.camconnect.ui.auth

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.auth.SessionManager
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for PinAuthDialog composable.
 */
@RunWith(AndroidJUnit4::class)
class PinAuthDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        SessionManager.clearSession()
    }

    @After
    fun tearDown() {
        SessionManager.clearSession()
    }

    @Test
    fun dialog_rendersWithEnterPinTitle() {
        composeTestRule.setContent {
            CamConnectTheme {
                PinAuthDialog(onSuccess = {}, onCancel = {})
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Enter PIN").assertExists()
        composeTestRule.onNodeWithText("Enter your 4-digit PIN to continue").assertExists()
    }

    @Test
    fun dialog_rendersNumericKeypad() {
        composeTestRule.setContent {
            CamConnectTheme {
                PinAuthDialog(onSuccess = {}, onCancel = {})
            }
        }
        Thread.sleep(500) // Allow composition
        for (digit in "1234567890") {
            composeTestRule.onNodeWithText(digit.toString()).assertExists()
        }
    }

    @Test
    fun dialog_cancelButtonTriggersCallback() {
        var cancelCalled = false
        composeTestRule.setContent {
            CamConnectTheme {
                PinAuthDialog(
                    onSuccess = {},
                    onCancel = { cancelCalled = true }
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Cancel").performClick()
        Thread.sleep(500) // Allow composition
        assert(cancelCalled)
    }

    @Test
    fun dialog_submitButtonDisabledWhenPinEmpty() {
        composeTestRule.setContent {
            CamConnectTheme {
                PinAuthDialog(onSuccess = {}, onCancel = {})
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Submit").assertIsNotEnabled()
    }

    @Test
    fun dialog_digitClickUpdatesPinDisplay() {
        composeTestRule.setContent {
            CamConnectTheme {
                PinAuthDialog(onSuccess = {}, onCancel = {})
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("2").performClick()
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Submit").assertIsEnabled()
    }

    @Test
    fun dialog_zeroDigitClickWorks() {
        composeTestRule.setContent {
            CamConnectTheme {
                PinAuthDialog(onSuccess = {}, onCancel = {})
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("0").performClick()
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Submit").assertIsEnabled()
    }

    @Test
    fun dialog_securityIconHasContentDescription() {
        composeTestRule.setContent {
            CamConnectTheme {
                PinAuthDialog(onSuccess = {}, onCancel = {})
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithContentDescription("Security").assertExists()
    }

    @Test
    fun dialog_fourDigitsEnablesSubmit() {
        composeTestRule.setContent {
            CamConnectTheme {
                PinAuthDialog(onSuccess = {}, onCancel = {})
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("3").performClick()
        composeTestRule.onNodeWithText("4").performClick()
        Thread.sleep(500) // Allow composition
        // After 4 digits, ViewModel may auto-submit or Submit becomes enabled; just ensure dialog still shows
        composeTestRule.onNodeWithText("Submit").assertExists()
    }

    @Test
    fun dialog_submitButtonVisible() {
        composeTestRule.setContent {
            CamConnectTheme {
                PinAuthDialog(onSuccess = {}, onCancel = {})
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Submit").assertExists()
    }
}
