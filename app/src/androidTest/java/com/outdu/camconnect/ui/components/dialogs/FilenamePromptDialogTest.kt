package com.outdu.camconnect.ui.components.dialogs

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for FilenamePromptDialog composable.
 * Uses Android Compose Rule so FocusRequester has a real window.
 */
@RunWith(AndroidJUnit4::class)
class FilenamePromptDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dialog_rendersWithDefaultFilename() {
        var confirmValue: String? = null
        composeTestRule.setContent {
            CamConnectTheme {
                FilenamePromptDialog(
                    onConfirm = { confirmValue = it },
                    onCancel = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Save Recording").assertExists()
        composeTestRule.onNodeWithText("Enter a name for your recording").assertExists()
        composeTestRule.onNodeWithText("Filename").assertExists()
        composeTestRule.onNodeWithText("Cancel").assertExists()
        composeTestRule.onNodeWithText("Save").assertExists()
    }

    @Test
    fun dialog_confirmButtonTriggersCallbackWithFilename() {
        var confirmValue: String? = null
        composeTestRule.setContent {
            CamConnectTheme {
                FilenamePromptDialog(
                    onConfirm = { confirmValue = it },
                    onCancel = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Save").performClick()
        Thread.sleep(500) // Allow composition
        assertTrue(confirmValue != null)
        assertTrue(confirmValue!!.isNotBlank())
        assertTrue(confirmValue!!.startsWith("recording_"))
    }

    @Test
    fun dialog_cancelButtonTriggersCallback() {
        var cancelCalled = false
        composeTestRule.setContent {
            CamConnectTheme {
                FilenamePromptDialog(
                    onConfirm = {},
                    onCancel = { cancelCalled = true }
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Cancel").performClick()
        Thread.sleep(500) // Allow composition
        assertTrue(cancelCalled)
    }

    @Test
    fun dialog_textInputUpdatesFilename() {
        var confirmValue: String? = null
        composeTestRule.setContent {
            CamConnectTheme {
                FilenamePromptDialog(
                    onConfirm = { confirmValue = it },
                    onCancel = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("my_recording")
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Save").performClick()
        Thread.sleep(500) // Allow composition
        assertEquals("my_recording", confirmValue)
    }

    @Test
    fun dialog_emptyFilenameShowsErrorAndSaveDisabled() {
        composeTestRule.setContent {
            CamConnectTheme {
                FilenamePromptDialog(
                    onConfirm = {},
                    onCancel = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("")
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Invalid filename. Avoid special characters: < > : \" | ? * \\").assertExists()
        composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun dialog_invalidCharactersShowError() {
        composeTestRule.setContent {
            CamConnectTheme {
                FilenamePromptDialog(
                    onConfirm = {},
                    onCancel = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("file<name")
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Invalid filename. Avoid special characters: < > : \" | ? * \\").assertExists()
    }

    @Test
    fun dialog_validFilenameEnablesSave() {
        composeTestRule.setContent {
            CamConnectTheme {
                FilenamePromptDialog(
                    onConfirm = {},
                    onCancel = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("valid_name")
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Save").assertIsEnabled()
    }

    @Test
    fun dialog_trimmedFilenamePassedToConfirm() {
        var confirmValue: String? = null
        composeTestRule.setContent {
            CamConnectTheme {
                FilenamePromptDialog(
                    onConfirm = { confirmValue = it },
                    onCancel = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        // Use only leading spaces (trailing space would make filename invalid and Save disabled)
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("  trimmed")
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Save").performClick()
        Thread.sleep(500) // Allow composition
        assertEquals("trimmed", confirmValue)
    }

    @Test
    fun dialog_videoFileIconHasContentDescription() {
        composeTestRule.setContent {
            CamConnectTheme {
                FilenamePromptDialog(
                    onConfirm = {},
                    onCancel = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithContentDescription("Video File").assertExists()
    }

    @Test
    fun dialog_characterCountDisplayed() {
        composeTestRule.setContent {
            CamConnectTheme {
                FilenamePromptDialog(
                    onConfirm = {},
                    onCancel = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("ab")
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("2/50 characters").assertExists()
    }
}
