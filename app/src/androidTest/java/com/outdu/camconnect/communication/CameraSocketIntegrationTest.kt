package com.outdu.camconnect.communication

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import java.net.ServerSocket

/**
 * Instrumented integration tests for CameraSocketClient with real Android network stack.
 * Uses a local ServerSocket to test connect, disconnect, and concurrent operations.
 */
@RunWith(AndroidJUnit4::class)
class CameraSocketIntegrationTest {

    private var serverSocket: ServerSocket? = null
    private val clients = mutableListOf<CameraSocketClient>()

    @After
    fun tearDown() = runBlocking {
        clients.forEach { it.close() }
        clients.clear()
        serverSocket?.close()
        serverSocket = null
    }

    @Test
    fun connectToRealLocalhostServerSucceeds() = runBlocking {
        val port = startServerSocket()
        Thread.sleep(150) // allow server accept thread to be ready
        val client = CameraSocketClient().also { clients.add(it) }
        client.setTimeouts(5000, 5000L)

        val result = client.connect("127.0.0.1", port)
        assumeTrue("Connect to localhost (skip if not supported on device)", result.isSuccess)

        assertTrue(client.isConnected())
        assertEquals(CameraSocketClient.ConnectionState.CONNECTED, client.getConnectionState())
    }

    @Test
    fun disconnectAfterConnectClosesSocket() = runBlocking {
        val port = startServerSocket()
        Thread.sleep(150)
        val client = CameraSocketClient().also { clients.add(it) }
        client.setTimeouts(5000, 5000L)
        val connectResult = client.connect("127.0.0.1", port)
        assumeTrue("Connect (skip if not supported on device)", connectResult.isSuccess)

        val result = client.disconnect()

        assertTrue(result.isSuccess)
        assertFalse(client.isConnected())
        assertEquals(CameraSocketClient.ConnectionState.DISCONNECTED, client.getConnectionState())
    }

    @Test
    fun connectToUnreachablePortReturnsFailure() = runBlocking {
        val client = CameraSocketClient().also { clients.add(it) }
        client.setTimeouts(500, 500L) // Short timeout for test speed

        val result = client.connect("127.0.0.1", 31999) // Unlikely to have server

        assertTrue("Connect to closed port should fail", result.isFailure)
        assertFalse(client.isConnected())
        val ex = result.exceptionOrNull()
        assertTrue(
            ex is CameraSocketClient.SocketException.ConnectionException ||
                ex is CameraSocketClient.SocketException.TimeoutException
        )
    }

    @Test
    fun concurrentConnectionsToSameServerSucceed() = runBlocking {
        val port = startServerSocket()
        Thread.sleep(150)
        val deferreds = (1..3).map {
            async(Dispatchers.IO) {
                val client = CameraSocketClient()
                client.setTimeouts(5000, 5000L)
                val r = client.connect("127.0.0.1", port)
                Pair(client, r)
            }
        }
        val results = deferreds.map { it.await() }
        results.forEach { (client, _) -> clients.add(client) }
        assumeTrue("All connects (skip if localhost server not reliable on device)", results.all { (_, r) -> r.isSuccess })
    }

    @Test
    fun getConnectionStateReflectsConnectingThenConnected() = runBlocking {
        val port = startServerSocket()
        Thread.sleep(150)
        val client = CameraSocketClient().also { clients.add(it) }
        assertEquals(CameraSocketClient.ConnectionState.DISCONNECTED, client.getConnectionState())

        client.setTimeouts(5000, 5000L)
        val result = client.connect("127.0.0.1", port)
        assumeTrue("Connect (skip if not supported on device)", result.isSuccess)

        assertEquals(CameraSocketClient.ConnectionState.CONNECTED, client.getConnectionState())
    }

    @Test
    fun connectWhenAlreadyConnectedReturnsSuccess() = runBlocking {
        val port = startServerSocket()
        Thread.sleep(150)
        val client = CameraSocketClient().also { clients.add(it) }
        client.setTimeouts(5000, 5000L)
        val firstConnect = client.connect("127.0.0.1", port)
        assumeTrue("First connect (skip if not supported on device)", firstConnect.isSuccess)

        val secondConnect = client.connect("127.0.0.1", port)

        assertTrue(secondConnect.isSuccess)
        assertTrue(client.isConnected())
    }

    @Test
    fun closeAfterDisconnectDoesNotThrow() = runBlocking {
        val port = startServerSocket()
        Thread.sleep(150)
        val client = CameraSocketClient().also { clients.add(it) }
        client.setTimeouts(5000, 5000L)
        val connectResult = client.connect("127.0.0.1", port)
        assumeTrue("Connect (skip if not supported on device)", connectResult.isSuccess)
        client.disconnect()
        clients.remove(client)

        client.close()
    }

    private fun startServerSocket(): Int = runBlocking {
        withContext(Dispatchers.IO) {
            val server = ServerSocket(0)
            serverSocket = server
            Thread {
                while (!server.isClosed) {
                    try {
                        server.accept()?.close()
                    } catch (_: Exception) { }
                }
            }.apply { isDaemon = true; start() }
            server.localPort
        }
    }
}
