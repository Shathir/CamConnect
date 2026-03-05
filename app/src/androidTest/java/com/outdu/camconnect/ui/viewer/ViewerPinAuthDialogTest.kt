package com.outdu.camconnect.ui.viewer

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.services.OnvifDevice
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ViewerPinAuthDialog.
 */
@RunWith(AndroidJUnit4::class)
class ViewerPinAuthDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun testDevice(): OnvifDevice = OnvifDevice(
        ipAddress = "192.168.1.1",
        endpointUrls = listOf("http://192.168.1.1/onvif/device_service"),
        deviceType = "NetworkVideoTransmitter",
        scopes = listOf("onvif://www.onvif.org/name/TestCamera")
    )

    @Test
    fun viewerPinAuthDialog_rendersWithoutCrash() {
        composeTestRule.setContent {
            CamConnectTheme {
                ViewerPinAuthDialog(
                    camera = testDevice(),
                    isAuthenticating = false,
                    authError = null,
                    onPinEntered = {},
                    onDismiss = {},
                    onClearAuthError = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Connect to Camera").assertExists()
    }
}
