package com.outdu.camconnect.ui.components.buttons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import com.outdu.camconnect.ui.theme.DefaultColors
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CustomizableButton and ButtonConfig.
 */
@RunWith(AndroidJUnit4::class)
class CustomizableButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun customizableButton_renders() {
        composeTestRule.setContent {
            CamConnectTheme {
                CustomizableButton(
                    config = ButtonConfig(
                        id = "test",
                        text = "Click me",
                        onClick = {}
                    )
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNode(hasClickAction()).assertExists()
    }

    @Test
    fun customizableButton_clickTriggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            CamConnectTheme {
                CustomizableButton(
                    config = ButtonConfig(
                        id = "test",
                        text = "Button",
                        onClick = { clicked = true }
                    )
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNode(hasClickAction()).performClick()
        Thread.sleep(500) // Allow composition
        assert(clicked)
    }

    @Test
    fun buttonConfig_defaults() {
        val config = ButtonConfig(id = "id")
        assert(config.enabled)
        assert(config.text == "")
        assert(config.backgroundColor == DefaultColors.BluePrimary)
    }
}
