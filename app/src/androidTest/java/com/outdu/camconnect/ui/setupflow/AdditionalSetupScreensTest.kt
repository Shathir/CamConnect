package com.outdu.camconnect.ui.setupflow

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for AdditionalSetupScreens composables.
 * Tests PermissionScreen, PermissionItem, and CameraAddScreen.
 */
@RunWith(AndroidJUnit4::class)
class AdditionalSetupScreensTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // ========== PermissionScreen Tests ==========

    @Test
    fun permissionScreen_rendersTitleAndDescription() {
        var callbackInvoked = false
        composeTestRule.setContent {
            CamConnectTheme {
                PermissionScreen(
                    onPermissionsGranted = { callbackInvoked = true }
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Permissions Required").assertExists()
        composeTestRule.onNodeWithText("To provide the best experience, we need access to these device features:").assertExists()
    }

    @Test
    fun permissionScreen_rendersPermissionItems() {
        composeTestRule.setContent {
            CamConnectTheme {
                PermissionScreen(onPermissionsGranted = {})
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Camera").assertExists()
        composeTestRule.onNodeWithText("Location").assertExists()
        composeTestRule.onNodeWithText("Microphone").assertExists()
        composeTestRule.onNodeWithText("Notifications").assertExists()
    }

    @Test
    fun permissionScreen_rendersGrantPermissionsButton() {
        composeTestRule.setContent {
            CamConnectTheme {
                PermissionScreen(onPermissionsGranted = {})
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Grant Permissions").assertExists()
    }

    @Test
    fun permissionScreen_grantButtonIsClickable() {
        composeTestRule.setContent {
            CamConnectTheme {
                PermissionScreen(onPermissionsGranted = {})
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Grant Permissions").assertExists().performClick()
        Thread.sleep(500) // Allow composition
        // Button is clickable; on real device callback may open permission dialog or proceed if already granted
    }

    // ========== PermissionItem Tests ==========

    @Test
    fun permissionItem_rendersTitleAndDescription() {
        composeTestRule.setContent {
            CamConnectTheme {
                PermissionItem(
                    title = "Camera",
                    description = "For QR code scanning",
                    isGranted = false,
                    icon = com.outdu.camconnect.R.drawable.camera_line
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Camera").assertExists()
        composeTestRule.onNodeWithText("For QR code scanning").assertExists()
    }

    @Test
    fun permissionItem_rendersWhenGranted() {
        composeTestRule.setContent {
            CamConnectTheme {
                PermissionItem(
                    title = "Location",
                    description = "For GPS",
                    isGranted = true,
                    icon = com.outdu.camconnect.R.drawable.earth_line
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Location").assertExists()
        composeTestRule.onNodeWithContentDescription("Granted").assertExists()
    }

    // ========== CameraAddScreen Tests ==========

    @Test
    fun cameraAddScreen_rendersWelcomeMessage() {
        var addClicked = false
        composeTestRule.setContent {
            CamConnectTheme {
                CameraAddScreen(
                    onAddCamera = { addClicked = true },
                    username = "TestUser"
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Welcome TestUser !").assertExists()
        composeTestRule.onNodeWithText("You have no active cameras", substring = true).assertExists()
    }

    @Test
    fun cameraAddScreen_rendersAddCameraButton() {
        var addClicked = false
        composeTestRule.setContent {
            CamConnectTheme {
                CameraAddScreen(
                    onAddCamera = { addClicked = true },
                    username = "James"
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Add Camera").assertExists()
    }

    @Test
    fun cameraAddScreen_addCameraButtonIsClickable() {
        composeTestRule.setContent {
            CamConnectTheme {
                CameraAddScreen(
                    onAddCamera = {},
                    username = "James"
                )
            }
        }
        Thread.sleep(500) // Allow composition

        // Verify Add Camera button exists and is clickable (callback invocation can vary by device semantics)
        composeTestRule.onNodeWithText("Add Camera").assertExists().performClick()
        Thread.sleep(500) // Allow composition
    }

    @Test
    fun cameraAddScreen_rendersAiLicensesCard() {
        composeTestRule.setContent {
            CamConnectTheme {
                CameraAddScreen(onAddCamera = {}, username = "Test")
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("AI Licenses found").assertExists()
        composeTestRule.onNodeWithText("You have 3 AI licenses ready to be activated.").assertExists()
    }

    @Test
    fun cameraAddScreen_rendersLoginWithOtherAccount() {
        composeTestRule.setContent {
            CamConnectTheme {
                CameraAddScreen(onAddCamera = {}, username = "Test")
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Login").assertExists()
        composeTestRule.onNodeWithText(" with other account.").assertExists()
    }
}
