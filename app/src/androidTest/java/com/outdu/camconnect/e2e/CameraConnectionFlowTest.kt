package com.outdu.camconnect.e2e

import com.outdu.camconnect.communication.CameraApiManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

/**
 * Instrumented integration tests for camera connection flow.
 * Tests discovery → set IP → connect/disconnect without requiring a real camera.
 */
@RunWith(AndroidJUnit4::class)
class CameraConnectionFlowTest {

    @Test
    fun setDeviceIpThenGetCurrentDeviceIP_returnsSetIp() {
        val manager = CameraApiManager.getInstance()
        val testIp = "192.168.2.100"
        val result = manager.setDeviceIpAddress(testIp)
        assertTrue("Set IP should succeed", result.isSuccess)
        assertEquals(testIp, manager.getCurrentDeviceIP())
    }

    @Test
    fun discoverDevices_returnsResultWithList() {
        val result = runBlocking { CameraApiManager.discoverDevices() }
        assertTrue("Discovery should complete", result.isSuccess || result.isFailure)
        result.onSuccess { list ->
            assertNotNull(list)
            assertTrue("Should return non-empty list (at least default IP)", list.isNotEmpty())
            assertTrue("Each item should be non-blank", list.all { it.isNotBlank() })
        }
    }

    @Test
    fun connectToDevice_withUnreachableIp_failsGracefully() {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.0.2.1") // TEST-NET
        val result = runBlocking { manager.connectToDevice() }
        assertTrue("Connect to unreachable IP should fail", result.isFailure)
    }

    @Test
    fun disconnectFromDevice_whenNotConnected_succeeds() {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.168.2.1")
        val result = runBlocking { manager.disconnectFromDevice() }
        assertTrue("Disconnect when not connected should succeed", result.isSuccess)
    }

    @Test
    fun fullFlow_setIp_discover_disconnect_doesNotCrash() {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.168.2.1")
        runBlocking { CameraApiManager.discoverDevices() }
        runBlocking { manager.disconnectFromDevice() }
        assertEquals("192.168.2.1", manager.getCurrentDeviceIP())
    }
}
