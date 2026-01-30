package com.outdu.camconnect.communication

import android.util.Log
import com.outdu.camconnect.network.HttpClientProvider
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * App-scoped WebSocket manager intended to run while the app is in the foreground.
 * - Survives recompositions and screen navigation (UI changes).
 * - Automatically stops when app goes background (via CamConnectApplication lifecycle observer).
 *
 * IMPORTANT: If the app is using an ephemeral WifiNetworkSpecifier network, the app process must be
 * bound to that Network before opening this WebSocket (ConnectivityManager.bindProcessToNetwork).
 */
object CameraWebSocketManager {
    private const val TAG = "CameraWebSocketManager"

    data class Config(
        val cameraIp: String,
        val port: Int,
        val path: String
    ) {
        fun url(): String {
            val normalizedPath = if (path.startsWith("/")) path else "/$path"
            return "ws://$cameraIp:$port$normalizedPath"
        }
    }

    sealed class ConnectionState {
        object Idle : ConnectionState()
        data class Connecting(val attempt: Int) : ConnectionState()
        data class Connected(val url: String) : ConnectionState()
        data class Disconnected(val reason: String? = null) : ConnectionState()
        data class Error(val message: String, val throwable: Throwable? = null) : ConnectionState()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private var appInForeground: Boolean = false
    private var config: Config? = null
    private var wsJob: Job? = null

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    // Unbounded is dangerous; keep some buffer capacity and drop if consumer is slow.
    private val _messages = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 256
    )
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun setTarget(cameraIp: String, port: Int, path: String) {
        scope.launch {
            mutex.withLock {
                val newConfig = Config(cameraIp = cameraIp, port = port, path = path)
                val old = config
                config = newConfig
                Log.i(TAG, "Target set: ${newConfig.url()}")

                // Restart connection if already running with different target
                val shouldRestart = wsJob != null && old != null && old != newConfig
                if (shouldRestart) {
                    Log.i(TAG, "Target changed; restarting WebSocket")
                    stopLocked()
                }
                if (appInForeground) {
                    startLocked()
                }
            }
        }
    }

    fun clearTarget() {
        scope.launch {
            mutex.withLock {
                Log.i(TAG, "Clearing target and stopping WebSocket")
                config = null
                stopLocked()
                _state.value = ConnectionState.Idle
            }
        }
    }

    /** Called by app lifecycle observer */
    fun onAppForegrounded() {
        scope.launch {
            mutex.withLock {
                appInForeground = true
                Log.d(TAG, "App foregrounded")
                startLocked()
            }
        }
    }

    /** Called by app lifecycle observer */
    fun onAppBackgrounded() {
        scope.launch {
            mutex.withLock {
                appInForeground = false
                Log.d(TAG, "App backgrounded -> stopping WebSocket")
                stopLocked()
            }
        }
    }

    private fun startLocked() {
        if (wsJob != null) return
        val cfg = config ?: return

        wsJob = scope.launch {
            val url = cfg.url()
            var attempt = 0
            var backoffMs = 1_000L

            while (true) {
                if (!appInForeground) {
                    _state.value = ConnectionState.Disconnected("background")
                    return@launch
                }
                val current = config
                if (current == null) {
                    _state.value = ConnectionState.Idle
                    return@launch
                }
                val currentUrl = current.url()
                if (currentUrl != url) {
                    // Target changed; let outer restart logic handle it
                    _state.value = ConnectionState.Disconnected("target_changed")
                    return@launch
                }

                attempt++
                _state.value = ConnectionState.Connecting(attempt)
                Log.i(TAG, "Connecting to $url (attempt=$attempt)")

                try {
                    HttpClientProvider.client.webSocket(urlString = url) {
                        _state.value = ConnectionState.Connected(url)
                        backoffMs = 1_000L
                        attempt = 0
                        Log.i(TAG, "WebSocket connected: $url")

                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Text -> {
                                    val text = frame.readText()
                                    Log.d(TAG, "WebSocket message received (text): $text")
                                    _messages.tryEmit(text)
                                }
                                is Frame.Binary -> {
                                    // If you need binary, expose a separate flow; for now, hex-ish summary.
                                    Log.d(TAG, "WebSocket message received (binary): ${frame.data.size} bytes")
                                    _messages.tryEmit("BINARY(${frame.data.size})")
                                }
                                is Frame.Close -> {
                                    val reason = frame.readReason()
                                    Log.i(TAG, "WebSocket close: $reason")
                                }
                                else -> Unit
                            }
                        }
                    }

                    // If we exit the webSocket block, it disconnected; retry with backoff.
                    _state.value = ConnectionState.Disconnected("disconnected")
                    Log.w(TAG, "WebSocket disconnected; retrying in ${backoffMs}ms")
                    delay(backoffMs)
                    backoffMs = (backoffMs * 2).coerceAtMost(30_000L)
                } catch (ce: CancellationException) {
                    _state.value = ConnectionState.Disconnected("cancelled")
                    throw ce
                } catch (t: Throwable) {
                    _state.value = ConnectionState.Error("WebSocket error: ${t.message}", t)
                    Log.e(TAG, "WebSocket error; retrying in ${backoffMs}ms", t)
                    delay(backoffMs)
                    backoffMs = (backoffMs * 2).coerceAtMost(30_000L)
                }
            }
        }
    }

    private fun stopLocked() {
        wsJob?.cancel()
        wsJob = null
    }
}


