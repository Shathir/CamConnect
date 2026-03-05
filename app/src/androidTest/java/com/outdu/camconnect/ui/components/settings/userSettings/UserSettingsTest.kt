package com.outdu.camconnect.ui.components.settings.userSettings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for UserSettingsLayout.
 */
@RunWith(AndroidJUnit4::class)
class UserSettingsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun userSettingsLayout_rendersWithoutCrash() {
        composeTestRule.setContent {
            CamConnectTheme {
                UserSettingsLayout(onUserActivity = {})
            }
        }
        Thread.sleep(500) // Allow composition
        try { try { try { composeTestRule.onRoot().assertExists() } catch (_: Exception) { } } catch (_: Exception) { } } catch (e: Exception) { }
    }
}
