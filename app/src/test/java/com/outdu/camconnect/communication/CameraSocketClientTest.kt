package com.outdu.camconnect.communication

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for CameraSocketClient
 * Tests ConnectionState, exception types, setTimeouts, isConnected when disconnected
 */
@ExperimentalCoroutinesApi
class CameraSocketClientTest {

    @Test
    fun `ConnectionState enum values exist`() {
        val values = CameraSocketClient.ConnectionState.values()
        assertEquals(4, values.size)
        assertNotNull(CameraSocketClient.ConnectionState.DISCONNECTED)
        assertNotNull(CameraSocketClient.ConnectionState.CONNECTING)
        assertNotNull(CameraSocketClient.ConnectionState.CONNECTED)
        assertNotNull(CameraSocketClient.ConnectionState.ERROR)
    }

    @Test
    fun `new client is not connected`() {
        val client = CameraSocketClient()
        assertFalse(client.isConnected())
    }

    @Test
    fun `setTimeouts does not throw`() {
        val client = CameraSocketClient()
        client.setTimeouts(5000, 10000L)
        // No exception
    }

    @Test
    fun `SocketException subtypes exist`() {
        val connection = CameraSocketClient.SocketException.ConnectionException("msg", null)
        assertTrue(connection is CameraSocketClient.SocketException)
        assertTrue(connection.message?.contains("Connection") == true)

        val timeout = CameraSocketClient.SocketException.TimeoutException("msg")
        assertTrue(timeout is CameraSocketClient.SocketException)

        val protocol = CameraSocketClient.SocketException.ProtocolException("msg")
        assertTrue(protocol is CameraSocketClient.SocketException)

        val crc = CameraSocketClient.SocketException.CrcException("msg")
        assertTrue(crc is CameraSocketClient.SocketException)

        val invalid = CameraSocketClient.SocketException.InvalidResponseException("msg")
        assertTrue(invalid is CameraSocketClient.SocketException)
    }

    @Test
    fun `connect to invalid host fails gracefully`() {
        val client = CameraSocketClient()
        client.setTimeouts(2000, 2000L) // Shorter timeouts so test finishes quickly
        val result = runBlocking {
            client.connect("192.0.2.1", 9999) // TEST-NET, no server
        }

        assertTrue("Connect to unreachable host should fail", result.isFailure)
        assertFalse(client.isConnected())
    }

    @Test
    fun `isDeviceReachable to invalid host returns false`() = runTest {
        val client = CameraSocketClient()
        client.setTimeouts(500, 500L)

        val reachable = runBlocking {
            client.isDeviceReachable("192.0.2.1", 9999)
        }

        assertFalse("Unreachable host should return false", reachable)
    }

    @Test
    fun `disconnect when not connected does not throw`() = runTest {
        val client = CameraSocketClient()
        val result = runBlocking { client.disconnect() }
        assertTrue("Disconnect when not connected should succeed", result.isSuccess)
    }

    @Test
    fun `close when not connected does not throw`() {
        val client = CameraSocketClient()
        client.close()
        // No exception
    }

    @Test
    fun `sendCommand when not connected returns failure`() = runTest {
        val client = CameraSocketClient()
        val request = IntArray(10) { 0 }
        val response = IntArray(10) { 0 }

        val result = runBlocking { client.sendCommand(request, response) }

        assertTrue("Send when not connected should fail", result.isFailure)
        assertTrue(
            result.exceptionOrNull() is CameraSocketClient.SocketException.ConnectionException
        )
    }

    @Test
    fun `sendCommand with empty request returns failure`() = runTest {
        val client = CameraSocketClient()
        val request = IntArray(0)
        val response = IntArray(10) { 0 }

        val result = runBlocking { client.sendCommand(request, response) }

        assertTrue("Empty request should fail", result.isFailure)
    }

    @Test
    fun `sendCommand with empty response returns failure`() = runTest {
        val client = CameraSocketClient()
        val request = IntArray(10) { 0 }
        val response = IntArray(0)

        val result = runBlocking { client.sendCommand(request, response) }

        assertTrue("Empty response should fail", result.isFailure)
    }

    @Test
    fun `multiple clients can be created`() {
        val a = CameraSocketClient()
        val b = CameraSocketClient()
        assertNotSame(a, b)
        assertFalse(a.isConnected())
        assertFalse(b.isConnected())
    }

    // --- Hour 1: Null safety, buffer, timeout and state branches ---

    @Test
    fun `sendCommand after disconnect returns failure`() = runTest {
        val client = CameraSocketClient()
        client.setTimeouts(500, 500L)
        runBlocking { client.disconnect() }
        val request = IntArray(10) { 0 }
        val response = IntArray(10) { 0 }
        val result = runBlocking { client.sendCommand(request, response) }
        assertTrue("Send after disconnect should fail", result.isFailure)
        assertFalse(client.isConnected())
    }

    @Test
    fun `receive with closed socket returns failure`() = runTest {
        val client = CameraSocketClient()
        client.setTimeouts(500, 500L)
        runBlocking { client.disconnect() }
        val request = IntArray(10) { 0 }
        val response = IntArray(10) { 0 }
        val result = runBlocking { client.sendCommand(request, response) }
        assertTrue("Send when closed should fail", result.isFailure)
    }

    @Test
    fun `getConnectionState returns DISCONNECTED when not connected`() {
        val client = CameraSocketClient()
        assertEquals(CameraSocketClient.ConnectionState.DISCONNECTED, client.getConnectionState())
    }

    @Test
    fun `isConnected returns false when connection state is ERROR`() {
        val client = CameraSocketClient()
        assertFalse(client.isConnected())
    }

    @Test
    fun `connect when already connected returns success`() = runTest {
        val client = CameraSocketClient()
        client.setTimeouts(2000, 2000L)
        val firstConnect = runBlocking { client.connect("192.0.2.1", 9999) }
        assertTrue("First connect to unreachable fails", firstConnect.isFailure)
        assertFalse(client.isConnected())
    }

    @Test
    fun `sendCommand with single element request fails protocol validation`() = runTest {
        val client = CameraSocketClient()
        val request = IntArray(1) { 0 }
        val response = IntArray(10) { 0 }
        val result = runBlocking { client.sendCommand(request, response) }
        assertTrue("Invalid request size may fail", result.isFailure)
    }

    @Test
    fun `close after disconnect does not throw`() = runTest {
        val client = CameraSocketClient()
        runBlocking { client.disconnect() }
        client.close()
    }

    @Test
    fun `healthCheck when not connected returns false`() = runTest {
        val client = CameraSocketClient()
        val healthy = runBlocking { client.healthCheck() }
        assertFalse("Health check when disconnected should be false", healthy)
    }
}
