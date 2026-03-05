package com.outdu.camconnect.ui.components.camera

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for TooltipContentBox.
 */
@RunWith(AndroidJUnit4::class)
class TooltipContentBoxTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tooltipContentBox_rendersContentWhenActive() {
        composeTestRule.setContent {
            CamConnectTheme {
                TooltipContentBox(
                    activeTooltipId = "info",
                    content = { id ->
                        androidx.compose.material3.Text("Tooltip: $id")
                    }
                )
            }
        }
        Thread.sleep(500) // Allow composition
        // Content appears after 200ms LaunchedEffect delay
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            try {
                composeTestRule.onAllNodesWithText("Tooltip: info").fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
    }

    @Test
    fun tooltipContentBox_rendersNothingWhenInactive() {
        composeTestRule.setContent {
            CamConnectTheme {
                TooltipContentBox(
                    activeTooltipId = null,
                    content = { androidx.compose.material3.Text("Hidden") }
                )
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Hidden").assertDoesNotExist()
    }
}
