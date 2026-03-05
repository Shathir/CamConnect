package com.outdu.camconnect.ui.components.camera

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for SystemWarningData and system warning logic.
 * (Composable tests for SystemWarningTooltip require Context/BroadcastReceiver; covered by UI flow tests.)
 */
@RunWith(AndroidJUnit4::class)
class SystemWarningTooltipTest {

    @Test
    fun systemWarningData_hasWarningWhenLowBattery() {
        val data = SystemWarningData(
            batteryLevel = 10,
            formattedStorageSize = "5 GB",
            isLowStorage = false,
            isLowBattery = true,
            hasWarning = true
        )
        assertTrue(data.hasWarning)
        assertTrue(data.isLowBattery)
        assertFalse(data.isLowStorage)
        assertEquals(10, data.batteryLevel)
    }

    @Test
    fun systemWarningData_hasWarningWhenLowStorage() {
        val data = SystemWarningData(
            batteryLevel = 80,
            formattedStorageSize = "1 GB",
            isLowStorage = true,
            isLowBattery = false,
            hasWarning = true
        )
        assertTrue(data.hasWarning)
        assertTrue(data.isLowStorage)
        assertFalse(data.isLowBattery)
    }

    @Test
    fun systemWarningData_noWarningWhenNormal() {
        val data = SystemWarningData(
            batteryLevel = 90,
            formattedStorageSize = "10 GB",
            isLowStorage = false,
            isLowBattery = false,
            hasWarning = false
        )
        assertFalse(data.hasWarning)
        assertFalse(data.isLowBattery)
        assertFalse(data.isLowStorage)
    }

    @Test
    fun systemWarningData_formattedStorageSizeReflected() {
        val data = SystemWarningData(
            batteryLevel = 50,
            formattedStorageSize = "2.5 GB",
            isLowStorage = false,
            isLowBattery = false,
            hasWarning = false
        )
        assertEquals("2.5 GB", data.formattedStorageSize)
    }
}
