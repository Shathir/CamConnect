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
 * Instrumented tests for MyCamerasScreen.
 * Tests empty state, camera list, Add Camera / Scan QR actions.
 */
@RunWith(AndroidJUnit4::class)
class MyCamerasScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun myCamerasScreen_emptyState_rendersTitle() {
        composeTestRule.setContent {
            CamConnectTheme {
                MyCamerasScreen(
                    onScanQR = {},
                    onConnectAndStream = {},
                    cameras = emptyList()
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("My Cameras").assertExists()
    }

    @Test
    fun myCamerasScreen_emptyState_rendersAddCamera() {
        composeTestRule.setContent {
            CamConnectTheme {
                MyCamerasScreen(
                    onScanQR = {},
                    onConnectAndStream = {},
                    cameras = emptyList()
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Add Camera").assertExists()
    }

    @Test
    fun myCamerasScreen_emptyState_scanQRButtonTriggersCallback() {
        var scanQRClicked = false
        composeTestRule.setContent {
            CamConnectTheme {
                MyCamerasScreen(
                    onScanQR = { scanQRClicked = true },
                    onConnectAndStream = {},
                    cameras = emptyList()
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Scan QR").performClick()
        Thread.sleep(500) // Allow composition

        assert(scanQRClicked)
    }

    @Test
    fun myCamerasScreen_emptyState_rendersScanQRCodeHint() {
        composeTestRule.setContent {
            CamConnectTheme {
                MyCamerasScreen(
                    onScanQR = {},
                    onConnectAndStream = {},
                    cameras = emptyList()
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Scan QR Code on back of camera").assertExists()
    }

    @Test
    fun myCamerasScreen_withCameras_rendersCameraList() {
        val cameras = listOf(
            CameraInfo(
                id = "cam1",
                name = "Front Camera",
                macId = "AA:BB:CC:DD:EE:FF",
                ipAddress = "192.168.1.100",
                serialNumber = "SN123",
                manufacturedDate = "2024-01"
            )
        )
        composeTestRule.setContent {
            CamConnectTheme {
                MyCamerasScreen(
                    onScanQR = {},
                    onConnectAndStream = {},
                    cameras = cameras
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("My Cameras").assertExists()
        composeTestRule.onNodeWithText("Front Camera").assertExists()
        composeTestRule.onNodeWithText("Add New Camera").assertExists()
    }

    @Test
    fun myCamerasScreen_withCameras_scanQRCodeButtonExists() {
        val cameras = listOf(
            CameraInfo(
                id = "cam1",
                name = "Test Cam",
                macId = "00:11:22:33:44:55",
                ipAddress = "10.0.0.1"
            )
        )
        composeTestRule.setContent {
            CamConnectTheme {
                MyCamerasScreen(
                    onScanQR = {},
                    onConnectAndStream = {},
                    cameras = cameras
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Scan QR Code").assertExists()
    }
}
