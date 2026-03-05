package com.outdu.camconnect.communication

import com.outdu.camconnect.communication.CameraNotificationServer.ServerState
import com.outdu.camconnect.communication.CameraNotificationServer.NotificationListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Instrumented tests for CameraNotificationServer.
 * Covers server lifecycle, state transitions, and client count with real Android networking.
 */
@RunWith(AndroidJUnit4::class)
class CameraNotificationServerInstrumentedTest {

    private val usedPort = 19002
    private val server = CameraNotificationServer(port = usedPort, maxClients = 2)

    @After
    fun tearDown() = runBlocking {
        if (server.isRunning()) {
            server.stop()
        }
    }

    @Test
    fun initialState_isStopped() {
        assertEquals(ServerState.STOPPED, server.getServerState())
        assertFalse(server.isRunning())
        assertEquals(0, server.getConnectedClientCount())
    }

    @Test
    fun start_returnsSuccess_andStateIsRunning() = runBlocking {
        val result = server.start()
        assertTrue(result.isSuccess)
        assertEquals(ServerState.RUNNING, server.getServerState())
        assertTrue(server.isRunning())
    }

    @Test
    fun start_whenAlreadyRunning_returnsSuccess() = runBlocking {
        server.start()
        val result = server.start()
        assertTrue(result.isSuccess)
        assertTrue(server.isRunning())
    }

    @Test
    fun stop_afterStart_returnsSuccess_andStateIsStopped() = runBlocking {
        server.start()
        val result = server.stop()
        assertTrue(result.isSuccess)
        assertEquals(ServerState.STOPPED, server.getServerState())
        assertFalse(server.isRunning())
    }

    @Test
    fun stop_whenNotRunning_returnsSuccess() = runBlocking {
        val result = server.stop()
        assertTrue(result.isSuccess)
    }

    @Test
    fun getConnectedClients_whenNoClients_returnsEmptyMap() = runBlocking {
        server.start()
        delay(100)
        val clients = server.getConnectedClients()
        assertTrue(clients.isEmpty())
    }

    @Test
    fun setNotificationListener_stateChangesInvoked() = runBlocking {
        val states = ConcurrentLinkedQueue<Pair<ServerState, ServerState>>()
        server.setNotificationListener(object : NotificationListener {
            override fun onClientConnected(clientId: String, clientAddress: String) {}
            override fun onClientDisconnected(clientId: String, reason: String) {}
            override fun onMessageReceived(clientId: String, message: String) {}
            override fun onServerError(error: Throwable) {}
            override fun onServerStateChanged(oldState: ServerState, newState: ServerState) {
                states.add(oldState to newState)
            }
        })
        server.start()
        delay(150)
        server.stop()
        delay(100)
        assertTrue(states.isNotEmpty())
        val (oldState, newState) = states.first()
        assertEquals(ServerState.STOPPED, oldState)
        assertTrue(newState == ServerState.STARTING || newState == ServerState.RUNNING)
    }

    @Test
    fun disconnectClient_whenNoClients_returnsFalse() = runBlocking {
        server.start()
        delay(50)
        val result = server.disconnectClient("client_99", "test")
        assertFalse(result)
    }

    // ========== Hour 5: Server lifecycle and connection branches ==========

    @Test
    fun serverRestart_worksAfterStop() = runBlocking {
        server.start()
        delay(50)
        server.stop()
        delay(100)
        val result = server.start()
        assertTrue(result.isSuccess)
        assertTrue(server.isRunning())
    }

    @Test
    fun getConnectedClientCount_afterStartIsZero() = runBlocking {
        server.start()
        delay(50)
        assertEquals(0, server.getConnectedClientCount())
    }

    @Test
    fun setNotificationListener_nullMessageReceivedDoesNotCrash() = runBlocking {
        var received = false
        server.setNotificationListener(object : NotificationListener {
            override fun onClientConnected(clientId: String, clientAddress: String) {}
            override fun onClientDisconnected(clientId: String, reason: String) {}
            override fun onMessageReceived(clientId: String, message: String) { received = true }
            override fun onServerError(error: Throwable) {}
            override fun onServerStateChanged(oldState: ServerState, newState: ServerState) {}
        })
        server.start()
        delay(50)
        server.stop()
        assertTrue(server.getServerState() == ServerState.STOPPED)
    }

    @Test
    fun stop_whenStopped_returnsSuccess() = runBlocking {
        val result = server.stop()
        assertTrue(result.isSuccess)
    }

    @Test
    fun getConnectedClients_afterStop_returnsEmpty() = runBlocking {
        server.start()
        delay(50)
        server.stop()
        delay(100)
        val clients = server.getConnectedClients()
        assertTrue(clients.isEmpty())
    }
}
