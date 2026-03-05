package com.outdu.camconnect.ui.components.settings.license

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CardComponents (CameraInfoCard etc.).
 */
@RunWith(AndroidJUnit4::class)
class LicenseCardComponentsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun cameraInfoCard_rendersTitleAndMacId() {
        composeTestRule.setContent {
            CamConnectTheme {
                CameraInfoCard(
                    title = "Boat-Front",
                    macId = "11:22:33:44:55:66",
                    key = "key123",
                    status = "Active"
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Boat-Front").assertExists()
        composeTestRule.onNodeWithText("MAC ID: 11:22:33:44:55:66").assertExists()
    }
}
