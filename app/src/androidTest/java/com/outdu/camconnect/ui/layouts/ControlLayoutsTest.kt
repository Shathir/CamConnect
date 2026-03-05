package com.outdu.camconnect.ui.layouts

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.components.settings.ControlTab
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for control layout types (ControlTab, etc.).
 * Full layout composables require ViewModel/context setup; exercised via AdaptiveStreamLayoutTest.
 */
@RunWith(AndroidJUnit4::class)
class ControlLayoutsTest {

    @Test
    fun controlTab_networkLayout() {
        assertEquals("Network", ControlTab.NETWORK_LAYOUT.displayName)
    }

    @Test
    fun controlTab_cameraControl() {
        assertEquals("Camera", ControlTab.CAMERA_CONTROL.displayName)
        assertEquals(ControlTab.CAMERA_CONTROL, ControlTab.entries[0])
    }

    @Test
    fun controlTab_aiControl() {
        assertEquals("AI Vision", ControlTab.AI_CONTROL.displayName)
    }

    @Test
    fun controlTab_licenseControl() {
        assertEquals("Manage", ControlTab.LICENSE_CONTROL.displayName)
    }

    @Test
    fun controlTab_allTabsHaveDisplayNames() {
        ControlTab.entries.forEach { tab ->
            assert(tab.displayName.isNotBlank()) { "ControlTab.${tab.name} should have displayName" }
        }
    }

    @Test
    fun controlTab_systemSettings() {
        assertEquals("System", ControlTab.SETTINGS_LAYOUT.displayName)
    }

    @Test
    fun controlTab_devMode() {
        assertEquals("Dev Mode", ControlTab.DEV_LAYOUT.displayName)
    }

    @Test
    fun controlTab_otaLayout() {
        assertEquals("OTA", ControlTab.OTA_LAYOUT.displayName)
    }
}
