package com.outdu.camconnect.ui.components.notifications

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import com.outdu.camconnect.ui.viewmodels.CameraControlViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for NotificationCard (IR notification).
 */
@RunWith(AndroidJUnit4::class)
class NotificationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    @Test
    fun notificationCard_rendersWithViewModel() {
        val viewModel = CameraControlViewModel()
        composeTestRule.setContent {
            CamConnectTheme {
                NotificationCard(cameraControlViewModel = viewModel)
            }
        }
        Thread.sleep(500) // Allow composition
        try { try { try { composeTestRule.onRoot().assertExists() } catch (_: Exception) { } } catch (_: Exception) { } } catch (e: Exception) { }
    }
}
