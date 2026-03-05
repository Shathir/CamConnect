package com.outdu.camconnect.ui.components.camera

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CameraInfoIcon and tooltip-related composables.
 */
@RunWith(AndroidJUnit4::class)
class CameraInfoTooltipTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cameraInfoIcon_renders() {
        val tooltipManager = TooltipManager()
        composeTestRule.setContent {
            CamConnectTheme {
                CameraInfoIcon(tooltipManager = tooltipManager)
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("ⓘ").assertExists()
    }

    @Test
    fun cameraInfoIcon_clickTogglesTooltip() {
        val tooltipManager = TooltipManager()
        composeTestRule.setContent {
            CamConnectTheme {
                CameraInfoIcon(tooltipManager = tooltipManager)
            }
        }
        Thread.sleep(500) // Allow composition
        assert(!tooltipManager.isTooltipVisible("camera_info"))
        composeTestRule.onNodeWithText("ⓘ").performClick()
        Thread.sleep(500) // Allow composition
        assert(tooltipManager.isTooltipVisible("camera_info"))
        composeTestRule.onNodeWithText("ⓘ").performClick()
        Thread.sleep(500) // Allow composition
        assert(!tooltipManager.isTooltipVisible("camera_info"))
    }

    @Test
    fun tooltipManager_isTooltipVisibleReturnsFalseInitially() {
        val manager = TooltipManager()
        assert(!manager.isTooltipVisible("any_id"))
    }

    @Test
    fun tooltipManager_toggleTooltipOpensAndCloses() {
        val manager = TooltipManager()
        manager.toggleTooltip("id1")
        assert(manager.isTooltipVisible("id1"))
        manager.toggleTooltip("id1")
        assert(!manager.isTooltipVisible("id1"))
    }

    @Test
    fun tooltipManager_openTooltipClosesOther() {
        val manager = TooltipManager()
        manager.openTooltip("id1")
        assert(manager.isTooltipVisible("id1"))
        manager.openTooltip("id2")
        assert(manager.isTooltipVisible("id2"))
        assert(!manager.isTooltipVisible("id1"))
    }

    @Test
    fun tooltipManager_closeTooltip() {
        val manager = TooltipManager()
        manager.openTooltip("id1")
        manager.closeTooltip("id1")
        assert(!manager.isTooltipVisible("id1"))
    }
}
