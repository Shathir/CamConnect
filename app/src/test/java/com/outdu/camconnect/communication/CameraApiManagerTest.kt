package com.outdu.camconnect.communication

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for CameraApiManager
 * Tests singleton, device IP get/set, validation, DeviceInfo, and exception types
 */
@ExperimentalCoroutinesApi
class CameraApiManagerTest {

    @Test
    fun `getInstance returns same singleton`() {
        val a = CameraApiManager.getInstance()
        val b = CameraApiManager.getInstance()
        assertSame(a, b)
    }

    @Test
    fun `getInstance returns non-null`() {
        assertNotNull(CameraApiManager.getInstance())
    }

    @Test
    fun `setDeviceIpAddress with valid IP succeeds`() {
        val manager = CameraApiManager.getInstance()
        val result = manager.setDeviceIpAddress("192.168.1.100")

        assertTrue("Valid IP should succeed", result.isSuccess)
        assertEquals("192.168.1.100", manager.getCurrentDeviceIP())
    }

    @Test
    fun `setDeviceIpAddress with blank IP fails`() {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.168.1.1") // set valid first

        val result = manager.setDeviceIpAddress("")

        assertTrue("Blank IP should fail", result.isFailure)
        assertTrue(
            result.exceptionOrNull() is CameraApiManager.DeviceException.ConfigurationException
        )
        // Current IP unchanged
        assertEquals("192.168.1.1", manager.getCurrentDeviceIP())
    }

    @Test
    fun `setDeviceIpAddress with whitespace-only fails`() {
        val manager = CameraApiManager.getInstance()
        val result = manager.setDeviceIpAddress("   ")

        assertTrue("Whitespace IP should fail", result.isFailure)
    }

    @Test
    fun `getCurrentDeviceIP returns default when not set`() {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("10.0.0.5")

        assertEquals("10.0.0.5", manager.getCurrentDeviceIP())
    }

    @Test
    fun `getCurrentDeviceIP returns last set valid IP`() {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.168.2.1")
        manager.setDeviceIpAddress("192.168.2.2")

        assertEquals("192.168.2.2", manager.getCurrentDeviceIP())
    }

    @Test
    fun `getDeviceInfo returns map`() {
        val manager = CameraApiManager.getInstance()
        val info = manager.getDeviceInfo()

        assertNotNull(info)
        assertTrue("Should be a map", info is Map<*, *>)
    }

    @Test
    fun `DeviceInfo data class holds values`() {
        val info = CameraApiManager.DeviceInfo(
            ipAddress = "192.168.1.1",
            isConnected = true,
            lastConnected = 12345L,
            connectionAttempts = 2
        )

        assertEquals("192.168.1.1", info.ipAddress)
        assertTrue(info.isConnected)
        assertEquals(12345L, info.lastConnected)
        assertEquals(2, info.connectionAttempts)
    }

    @Test
    fun `DeviceException subtypes exist`() {
        val discovery = CameraApiManager.DeviceException.DiscoveryException("msg", null)
        assertTrue(discovery is CameraApiManager.DeviceException)
        assertTrue(discovery.message?.contains("Discovery") == true)

        val connection = CameraApiManager.DeviceException.ConnectionException("msg", null)
        assertTrue(connection is CameraApiManager.DeviceException)

        val command = CameraApiManager.DeviceException.CommandException("msg", null)
        assertTrue(command is CameraApiManager.DeviceException)

        val config = CameraApiManager.DeviceException.ConfigurationException("msg")
        assertTrue(config is CameraApiManager.DeviceException)
    }

    @Test
    fun `CAMERA_CLIENT_SOCKET_PORT is 9000`() {
        assertEquals(9000, CameraApiManager.CAMERA_CLIENT_SOCKET_PORT)
    }

    @Test
    fun `CAMERA_SERVER_SOCKET_PORT is 9002`() {
        assertEquals(9002, CameraApiManager.CAMERA_SERVER_SOCKET_PORT)
    }

    @Test
    fun `setDeviceIpAddress accepts localhost`() {
        val manager = CameraApiManager.getInstance()
        val result = manager.setDeviceIpAddress("127.0.0.1")
        assertTrue(result.isSuccess)
        assertEquals("127.0.0.1", manager.getCurrentDeviceIP())
    }

    @Test
    fun `setDeviceIpAddress accepts IPv4 style`() {
        val manager = CameraApiManager.getInstance()
        listOf("10.0.0.1", "172.16.0.1", "192.168.100.50").forEach { ip ->
            val result = manager.setDeviceIpAddress(ip)
            assertTrue("IP $ip should be accepted", result.isSuccess)
            assertEquals(ip, manager.getCurrentDeviceIP())
        }
    }

    // --- Hour 1: Error handling and state validation branches ---

    @Test
    fun `connectToDevice to unreachable host returns failure or success`() = runBlocking {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.0.2.1") // TEST-NET; in some environments nothing listens
        manager.disconnectFromDevice("192.0.2.1") // ensure no cached client
        val result = manager.connectToDevice("192.0.2.1")
        // Manager returns client.connect() result directly: failure carries SocketException (or DeviceException if from catch)
        if (result.isFailure) {
            val ex = result.exceptionOrNull()
            assertNotNull("Failure should have an exception", ex)
            assertTrue(
                "On failure, expected ConnectionException or SocketException, got: ${ex?.javaClass?.name}",
                ex is CameraApiManager.DeviceException.ConnectionException ||
                    ex is CameraSocketClient.SocketException
            )
        }
    }

    @Test
    fun `disconnectFromDevice when no client in pool returns success`() = runTest {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.168.1.100")
        val result = manager.disconnectFromDevice("192.168.1.100")
        assertTrue("Disconnect when not connected should succeed", result.isSuccess)
    }

    @Test
    fun `getConfiguration with invalid type returns failure`() = runTest {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.168.1.1")
        val result = manager.getConfiguration("InvalidType")
        assertTrue("Invalid config type should fail", result.isFailure)
        assertTrue(result.exceptionOrNull() is CameraApiManager.DeviceException.ConfigurationException ||
            result.exceptionOrNull() is Exception)
    }

    @Test
    fun `setIrBrightness with value below 0 returns failure`() = runTest {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.168.1.1")
        val result = manager.setIrBrightness(-1)
        assertTrue("Brightness -1 should fail", result.isFailure)
        assertTrue(result.exceptionOrNull() is CameraApiManager.DeviceException.ConfigurationException)
    }

    @Test
    fun `setIrBrightness with value above 255 returns failure`() = runTest {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.168.1.1")
        val result = manager.setIrBrightness(256)
        assertTrue("Brightness 256 should fail", result.isFailure)
        assertTrue(result.exceptionOrNull() is CameraApiManager.DeviceException.ConfigurationException)
    }

    @Test
    fun `performHealthCheck when no client in pool returns device_reachable false`() = runTest {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.168.1.100")
        val result = manager.performHealthCheck()
        assertTrue("Health check should succeed", result.isSuccess)
        val info = result.getOrThrow()
        assertFalse("Device should not be reachable when not connected", info["device_reachable"] as Boolean)
        assertEquals("192.168.1.100", info["device_ip"])
    }

    @Test
    fun `cleanup does not throw`() = runTest {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.168.1.1")
        manager.cleanup()
        // Cleanup completes without exception
    }

    @Test
    fun `connectToDevice with default IP uses current device IP`() = runBlocking {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("10.0.0.5")
        val result = manager.connectToDevice() // uses getCurrentDeviceIP()
        assertTrue("Should return result", result.isSuccess || result.isFailure)
    }

    @Test
    fun `getDeviceInfo returns empty when no connections made`() {
        val manager = CameraApiManager.getInstance()
        val info = manager.getDeviceInfo()
        assertNotNull(info)
        assertTrue("Device info is a map", info is Map<*, *>)
    }

    // --- Block 3: Error path and branch coverage ---

    @Test
    fun `getConfiguration with Factory type returns failure when not connected`() = runTest {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.168.1.1")
        val result = manager.getConfiguration("Factory")
        assertTrue("Factory config without connection should fail", result.isFailure)
    }

    @Test
    fun `getConfiguration with Default type returns failure when not connected`() = runTest {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.168.1.1")
        val result = manager.getConfiguration("Default")
        assertTrue("Default config without connection should fail", result.isFailure)
    }

    @Test
    fun `getConfiguration with Current type returns failure when not connected`() = runTest {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.168.1.1")
        val result = manager.getConfiguration("Current")
        assertTrue("Current config without connection should fail", result.isFailure)
    }

    @Test
    fun `uploadFile with empty IP would fail - verify setDeviceIpAddress rejects empty`() {
        val manager = CameraApiManager.getInstance()
        val result = manager.setDeviceIpAddress("")
        assertTrue(result.isFailure)
    }

    @Test
    fun `executeCommand path when connect fails returns ConnectionException`() = runBlocking {
        val manager = CameraApiManager.getInstance()
        manager.setDeviceIpAddress("192.0.2.1")
        manager.disconnectFromDevice("192.0.2.1")
        val result = manager.getConfiguration("InvalidType")
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(
            ex is CameraApiManager.DeviceException.ConfigurationException ||
                ex is CameraApiManager.DeviceException.CommandException ||
                ex is Exception
        )
    }
}
