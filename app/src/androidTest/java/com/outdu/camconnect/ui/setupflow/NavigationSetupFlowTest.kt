package com.outdu.camconnect.ui.setupflow

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for NavigationSetupFlow.
 * Tests that setup flow composables can be navigated (landing -> login flow via direct composable tests).
 * Full NavHost testing requires Activity; we test the composables used in the flow.
 */
@RunWith(AndroidJUnit4::class)
class NavigationSetupFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun landingScreen_navigationTriggerRenders() {
        composeTestRule.setContent {
            CamConnectTheme {
                LandingScreen(onGetStarted = {})
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Get started").assertExists()
    }

    @Test
    fun loginScreen_inFlowRenders() {
        composeTestRule.setContent {
            CamConnectTheme {
                LoginScreen(
                    setupState = com.outdu.camconnect.viewmodels.SetupState(),
                    onNext = {},
                    onUpdateDetails = { _, _, _, _ -> },
                    onAuthenticate = {}
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Hello").assertExists()
    }

    @Test
    fun cameraAddScreen_inFlowRenders() {
        composeTestRule.setContent {
            CamConnectTheme {
                CameraAddScreen(
                    onAddCamera = {},
                    username = "Test"
                )
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithText("Add Camera").assertExists()
    }
}
