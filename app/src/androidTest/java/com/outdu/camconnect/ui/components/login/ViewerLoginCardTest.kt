package com.outdu.camconnect.ui.components.login

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import com.outdu.camconnect.viewmodels.SetupState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ViewerLoginCard composable.
 */
@RunWith(AndroidJUnit4::class)
class ViewerLoginCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun card_rendersWithEnterPinTitle() {
        composeTestRule.setContent {
            CamConnectTheme {
                ViewerLoginCard(
                    setupState = SetupState(),
                    onUpdateDetails = { _, _, _, _ -> },
                    onPinEntered = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Enter PIN").assertExists()
    }

    @Test
    fun card_rendersPinInputField() {
        composeTestRule.setContent {
            CamConnectTheme {
                ViewerLoginCard(
                    setupState = SetupState(),
                    onUpdateDetails = { _, _, _, _ -> },
                    onPinEntered = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("PIN").assertExists()
        composeTestRule.onNode(hasSetTextAction()).assertExists()
    }

    @Test
    fun card_rendersHelperText() {
        composeTestRule.setContent {
            CamConnectTheme {
                ViewerLoginCard(
                    setupState = SetupState(),
                    onUpdateDetails = { _, _, _, _ -> },
                    onPinEntered = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Enter the 4-digit PIN provided by the camera owner").assertExists()
    }

    @Test
    fun card_pinInputTriggersCallback() {
        var lastPin: String? = null
        composeTestRule.setContent {
            CamConnectTheme {
                ViewerLoginCard(
                    setupState = SetupState(),
                    onUpdateDetails = { _, _, _, _ -> },
                    onPinEntered = { lastPin = it }
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("1234")
        Thread.sleep(500) // Allow composition
        assertEquals("1234", lastPin)
    }

    @Test
    fun card_pinInputLimitedToFourDigits() {
        var lastPin: String? = null
        composeTestRule.setContent {
            CamConnectTheme {
                ViewerLoginCard(
                    setupState = SetupState(),
                    onUpdateDetails = { _, _, _, _ -> },
                    onPinEntered = { lastPin = it }
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("1234")
        Thread.sleep(500) // Allow composition
        assertEquals("1234", lastPin)
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("12345")
        Thread.sleep(500) // Allow composition
        // Component only updates when input is valid (<=4 digits); 5th digit not accepted so lastPin stays "1234"
        assertEquals("1234", lastPin)
    }

    @Test
    fun card_rendersWithoutCrash() {
        composeTestRule.setContent {
            CamConnectTheme {
                ViewerLoginCard(
                    setupState = SetupState(isSetupComplete = true),
                    onUpdateDetails = { _, _, _, _ -> },
                    onPinEntered = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Enter PIN").assertExists()
    }
}
