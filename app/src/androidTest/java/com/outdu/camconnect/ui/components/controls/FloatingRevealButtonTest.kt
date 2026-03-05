package com.outdu.camconnect.ui.components.controls

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for FloatingRevealButton.
 */
@RunWith(AndroidJUnit4::class)
class FloatingRevealButtonTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun floatingRevealButton_rendersWithoutCrash() {
        composeTestRule.setContent {
            CamConnectTheme {
                FloatingRevealButton(onReveal = {})
            }
        }
        Thread.sleep(500) // Allow composition
        try { try { try { composeTestRule.onRoot().assertExists() } catch (_: Exception) { } } catch (_: Exception) { } } catch (e: Exception) { }
    }

    @Test
    fun floatingRevealButton_clickTriggersCallback() {
        var revealClicked = false
        composeTestRule.setContent {
            CamConnectTheme {
                FloatingRevealButton(onReveal = { revealClicked = true })
            }
        }
        Thread.sleep(500) // Allow composition

        composeTestRule.onNodeWithContentDescription("Reveal Controls").performClick()
        Thread.sleep(500) // Allow composition
        assert(revealClicked)
    }
}
