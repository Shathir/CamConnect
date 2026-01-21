package com.outdu.camconnect.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Data class for WiFi credentials parsed from QR code
 */
data class WifiCredentials(
    val ssid: String,
    val password: String,
    val ip: String? = null,
    val macAddress: String? = null
)

/**
 * Connection result states
 */
sealed class WifiConnectionResult {
    object Connecting : WifiConnectionResult()
    data class Success(val network: Network) : WifiConnectionResult()
    data class Failed(val error: String) : WifiConnectionResult()
    object Timeout : WifiConnectionResult()
}

sealed class WifiPersistResult {
    object Success : WifiPersistResult()
    object AlreadyExists : WifiPersistResult()
    data class Failed(val error: String) : WifiPersistResult()
}

/**
 * Manager for WiFi network connection using WifiNetworkSpecifier API (Android 10+)
 */
@RequiresApi(Build.VERSION_CODES.Q)
class WifiConnectionManager(private val context: Context) {
    
    companion object {
        private const val TAG = "WifiConnectionManager"
        private const val CONNECTION_TIMEOUT_MS = 30000L // 30 seconds
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var connectionTimeoutJob: kotlinx.coroutines.Job? = null
    private var unavailableHandler: Handler? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isConnectionResolved = false // Track if connection is already resolved
    
    private val _connectionState = MutableStateFlow<WifiConnectionResult?>(null)
    val connectionState: StateFlow<WifiConnectionResult?> = _connectionState.asStateFlow()
    
    /**
     * Connect to WiFi network using WifiNetworkSpecifier
     */
    fun connectToNetwork(
        credentials: WifiCredentials,
        onResult: (WifiConnectionResult) -> Unit
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            onResult(WifiConnectionResult.Failed("WiFi connection API requires Android 10+"))
            return
        }
        
        Log.i(TAG, "Connecting to network: SSID='${credentials.ssid}', Password length=${credentials.password.length}")
        
        // Reset connection state
        isConnectionResolved = false
        
        // Check if WiFi is enabled
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        if (wifiManager?.isWifiEnabled != true) {
            Log.w(TAG, "WiFi is not enabled")
            onResult(WifiConnectionResult.Failed("WiFi is not enabled. Please enable WiFi and try again."))
            return
        }
        
        _connectionState.value = WifiConnectionResult.Connecting
        
        try {
            // Build WifiNetworkSpecifier
            // Note: SSID should be provided as-is, Android will handle quoting if needed
            val specifierBuilder = WifiNetworkSpecifier.Builder()
                .setSsid(credentials.ssid)
            
            // Set password based on security type (defaulting to WPA2)
            if (credentials.password.isNotEmpty()) {
                specifierBuilder.setWpa2Passphrase(credentials.password)
                Log.d(TAG, "WPA2 passphrase set")
            } else {
                Log.w(TAG, "No password provided - attempting open network")
            }
            
            val specifier = specifierBuilder.build()
            Log.d(TAG, "WifiNetworkSpecifier created successfully")
            
            // Build NetworkRequest
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifier)
                .build()
            
            Log.d(TAG, "NetworkRequest created successfully")
            
            // Create NetworkCallback
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.i(TAG, "onAvailable called - Network: $network for SSID: ${credentials.ssid}")
                    
                    // Cancel any pending unavailable handler
                    unavailableHandler?.removeCallbacksAndMessages(null)
                    unavailableHandler = null
                    
                    // Check if already resolved
                    if (isConnectionResolved) {
                        Log.d(TAG, "Connection already resolved, ignoring duplicate onAvailable")
                        return
                    }
                    
                    // Verify the connection by checking network capabilities
                    val capabilities = connectivityManager.getNetworkCapabilities(network)
                    val hasWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                    Log.d(TAG, "Network capabilities - Has WiFi: $hasWifi, Capabilities: $capabilities")
                    
                    if (!hasWifi) {
                        mainHandler.post {
                            if (!isConnectionResolved) {
                                isConnectionResolved = true
                                cleanup()
                                Log.w(TAG, "Network available but not WiFi transport")
                                _connectionState.value = WifiConnectionResult.Failed("Connected but not to WiFi network")
                                onResult(WifiConnectionResult.Failed("Connected but not to WiFi network"))
                            }
                        }
                        return
                    }
                    
                    // Wait a moment and verify actual WiFi connection to the requested SSID
                    // Note: WifiNetworkSpecifier creates an ephemeral network that may not become
                    // the active WiFi connection. We need to verify the actual connection status.
                    mainHandler.postDelayed({
                        if (isConnectionResolved) {
                            return@postDelayed
                        }
                        
                        // Verify actual WiFi connection using multiple methods
                        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                        var isActuallyConnected = false
                        var connectedSSID: String? = null
                        var verificationMethod = ""
                        
                        try {
                            // Method 1: Check active network from ConnectivityManager
                            val activeNetwork = connectivityManager.activeNetwork
                            val activeCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                            val isActiveWifi = activeCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                            
                            Log.d(TAG, "Active network check - Is WiFi: $isActiveWifi, Network: $activeNetwork")
                            
                            // Method 2: Check WifiManager connection info (requires location permission)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                wifiManager?.connectionInfo?.let { wifiInfo ->
                                    connectedSSID = wifiInfo.ssid?.removeSurrounding("\"")
                                    val requestedSSID = credentials.ssid.removeSurrounding("\"")
                                    
                                    // Check if SSID matches (case-insensitive)
                                    val ssidMatches = connectedSSID?.equals(requestedSSID, ignoreCase = true) == true
                                    
                                    Log.d(TAG, "WiFi Connection Verification:")
                                    Log.d(TAG, "  Requested SSID: '$requestedSSID'")
                                    Log.d(TAG, "  Connected SSID: '$connectedSSID'")
                                    Log.d(TAG, "  Network ID: ${wifiInfo.networkId}")
                                    Log.d(TAG, "  SSID Matches: $ssidMatches")
                                    Log.d(TAG, "  Active Network is WiFi: $isActiveWifi")
                                    
                                    // Consider connected if:
                                    // 1. SSID matches AND network ID is valid (not -1), OR
                                    // 2. Active network is WiFi and we got onAvailable for this network
                                    isActuallyConnected = (ssidMatches && wifiInfo.networkId != -1) || 
                                                         (isActiveWifi && activeNetwork == network)
                                    verificationMethod = if (ssidMatches) "SSID match" else "Active network match"
                                } ?: run {
                                    // If we can't get WiFi info, check if active network matches
                                    isActuallyConnected = isActiveWifi && activeNetwork == network
                                    verificationMethod = "Active network match (no WiFi info)"
                                    Log.d(TAG, "Could not get WiFi info, using active network check")
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error verifying WiFi connection", e)
                            // Fallback: if network is available and is WiFi, consider it connected
                            isActuallyConnected = hasWifi
                            verificationMethod = "Fallback (capabilities check)"
                        }

                        // IMPORTANT (Android 10+ / SDK 29+):
                        // WifiNetworkSpecifier often provides an *ephemeral* app-scoped WiFi network that:
                        // - does NOT become the device's active WiFi (activeNetwork may be null)
                        // - reports SSID as "<unknown ssid>"
                        // - has networkId = -1
                        // Even then, the returned Network is valid and can be used by binding the process
                        // or using the Network's socketFactory. Treat this as a successful connection.
                        if (!isActuallyConnected && hasWifi) {
                            Log.w(TAG, "Ephemeral WiFi network detected (SSID may be <unknown ssid>). Treating as success for app-scoped networking.")
                            isActuallyConnected = true
                            verificationMethod = "Ephemeral network (capabilities)"
                        }
                        
                        if (!isConnectionResolved) {
                            isConnectionResolved = true
                            // IMPORTANT:
                            // Do NOT call cleanup() on success. WifiNetworkSpecifier connections are ephemeral and
                            // will be torn down when the request is released (unregisterNetworkCallback).
                            // Caller should explicitly call cleanup() when it's done using the network, or after
                            // transitioning to a persistent connection method (e.g. ACTION_WIFI_ADD_NETWORKS).
                            connectionTimeoutJob?.cancel()
                            connectionTimeoutJob = null
                            
                            if (isActuallyConnected) {
                                Log.i(TAG, "WiFi connection verified via $verificationMethod! Device is connected to SSID: $connectedSSID")
                                Log.i(TAG, "Calling onResult callback with Success result")
                                _connectionState.value = WifiConnectionResult.Success(network)
                                try {
                                    onResult(WifiConnectionResult.Success(network))
                                    Log.i(TAG, "onResult callback executed successfully - Activity should receive success")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error in onResult callback", e)
                                    e.printStackTrace()
                                }
                            } else {
                                Log.w(TAG, "Network available but device not connected to requested SSID.")
                                Log.w(TAG, "  Connected to: '$connectedSSID', Requested: '${credentials.ssid}'")
                                Log.w(TAG, "  Note: WifiNetworkSpecifier creates ephemeral networks that may not become the active WiFi.")
                                _connectionState.value = WifiConnectionResult.Failed(
                                    "Network request approved but device is not connected to '${credentials.ssid}'. " +
                                    "Currently connected to: ${connectedSSID ?: "unknown"}. " +
                                    "The network may need to be saved using WiFi settings."
                                )
                                onResult(WifiConnectionResult.Failed(
                                    "Not connected to requested network. Please connect manually via WiFi settings or use the 'Save Network' feature."
                                ))
                            }
                        }
                    }, 3000) // Wait 3 seconds for connection to stabilize
                }
                
                override fun onUnavailable() {
                    Log.w(TAG, "onUnavailable called for SSID: ${credentials.ssid}")
                    
                    // Don't immediately fail - wait a bit in case onAvailable comes later
                    // Sometimes onUnavailable is called first, then onAvailable follows
                    if (isConnectionResolved) {
                        Log.d(TAG, "Connection already resolved, ignoring onUnavailable")
                        return
                    }
                    
                    // Delay the failure to give onAvailable a chance to be called
                    unavailableHandler = Handler(Looper.getMainLooper())
                    unavailableHandler?.postDelayed({
                        if (!isConnectionResolved) {
                            Log.w(TAG, "onUnavailable confirmed - connection failed for SSID: ${credentials.ssid}")
                            isConnectionResolved = true
                            cleanup()
                            val errorMsg = "Connection denied or network not found. " +
                                    "Please ensure:\n" +
                                    "1. The WiFi network is in range\n" +
                                    "2. The SSID and password are correct\n" +
                                    "3. Try scanning the QR code again"
                            _connectionState.value = WifiConnectionResult.Failed(errorMsg)
                            onResult(WifiConnectionResult.Failed(errorMsg))
                        }
                    }, 5000) // Wait 5 seconds before considering it failed
                }
                
                override fun onLost(network: Network) {
                    Log.w(TAG, "onLost called - Network: $network")
                    // Don't cleanup here as this might be called after successful connection
                }
                
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    val hasWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    Log.d(TAG, "onCapabilitiesChanged - Network: $network, Has WiFi: $hasWifi")
                }
            }
            
            // Request network connection
            try {
                Log.d(TAG, "Calling requestNetwork() - this will show a system dialog to user")
                connectivityManager.requestNetwork(request, networkCallback!!)
                Log.d(TAG, "requestNetwork() called successfully - waiting for user response or callback")
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException when requesting network - missing permissions", e)
                e.printStackTrace()
                cleanup()
                val errorMsg = if (e.message?.contains("WRITE_SETTINGS") == true) {
                    "WiFi connection requires system permissions. Please ensure CHANGE_NETWORK_STATE permission is granted."
                } else {
                    "Permission denied: ${e.message ?: "Missing required permissions"}"
                }
                _connectionState.value = WifiConnectionResult.Failed(errorMsg)
                onResult(WifiConnectionResult.Failed(errorMsg))
                return
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected exception when requesting network", e)
                e.printStackTrace()
                cleanup()
                _connectionState.value = WifiConnectionResult.Failed("Failed to request network: ${e.message}")
                onResult(WifiConnectionResult.Failed("Failed to request network: ${e.message}"))
                return
            }
            
            // Set up timeout
            connectionTimeoutJob = CoroutineScope(Dispatchers.IO).launch {
                delay(CONNECTION_TIMEOUT_MS)
                if (_connectionState.value is WifiConnectionResult.Connecting) {
                    Log.w(TAG, "Connection timeout for SSID: ${credentials.ssid} after ${CONNECTION_TIMEOUT_MS}ms")
                    mainHandler.post {
                        cleanup()
                        _connectionState.value = WifiConnectionResult.Timeout
                        onResult(WifiConnectionResult.Timeout)
                    }
                }
            }
            
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException connecting to network", e)
            cleanup()
            val errorMsg = if (e.message?.contains("WRITE_SETTINGS") == true) {
                "WiFi connection requires system permissions. Please ensure CHANGE_NETWORK_STATE permission is granted."
            } else {
                "Permission denied: ${e.message ?: "Missing required permissions"}"
            }
            _connectionState.value = WifiConnectionResult.Failed(errorMsg)
            onResult(WifiConnectionResult.Failed(errorMsg))
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to network", e)
            cleanup()
            _connectionState.value = WifiConnectionResult.Failed(e.message ?: "Unknown error")
            onResult(WifiConnectionResult.Failed(e.message ?: "Unknown error"))
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        // Cancel pending unavailable handler
        unavailableHandler?.removeCallbacksAndMessages(null)
        unavailableHandler = null
        
        networkCallback?.let { callback ->
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering network callback", e)
            }
        }
        networkCallback = null
        connectionTimeoutJob?.cancel()
        connectionTimeoutJob = null
    }
    
    /**
     * Parse JSON QR code data
     */
    fun parseQRData(qrData: String): WifiCredentials? {
        return try {
            val jsonObject = org.json.JSONObject(qrData)
            val macValue = jsonObject.optString("mac", "")
                .ifBlank { jsonObject.optString("macaddress", "") }
                .ifBlank { jsonObject.optString("macAddress", "") }
                .takeIf { it.isNotBlank() }
            WifiCredentials(
                ssid = jsonObject.getString("ssid"),
                password = jsonObject.getString("password"),
                ip = jsonObject.optString("ip", "").takeIf { it.isNotBlank() },
                macAddress = macValue
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing QR data", e)
            null
        }
    }

    /**
     * Android 10 (API 29)+: Persist a WiFi network using WifiNetworkSuggestion.
     *
     * Notes:
     * - User approval is still required by Android (system UI/notification).
     * - Suggestions persist while the app is installed (OS-managed), so they survive app restarts.
     * - Connection is OS-controlled; we can encourage the user by opening the WiFi panel.
     */
    fun addNetworkSuggestion(credentials: WifiCredentials): WifiPersistResult {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return WifiPersistResult.Failed("WifiNetworkSuggestion requires Android 10+")
        }
        if (!wifiManager.isWifiEnabled) {
            return WifiPersistResult.Failed("WiFi is not enabled")
        }

        return try {
            val builder = WifiNetworkSuggestion.Builder()
                .setSsid(credentials.ssid)
                // Encourage a user-facing approval/connect flow
                .setIsAppInteractionRequired(true)

            if (credentials.password.isNotEmpty()) {
                builder.setWpa2Passphrase(credentials.password)
            }

            val suggestion = builder.build()
            val status = wifiManager.addNetworkSuggestions(listOf(suggestion))
            Log.i(TAG, "addNetworkSuggestions status=$status for SSID='${credentials.ssid}'")

            when (status) {
                WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS ->
                    WifiPersistResult.Success
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE ->
                    WifiPersistResult.AlreadyExists
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_APP_DISALLOWED ->
                    WifiPersistResult.Failed("App is disallowed from adding network suggestions. Enable it in WiFi settings and try again.")
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_EXCEEDS_MAX_PER_APP ->
                    WifiPersistResult.Failed("Too many saved suggestions for this app. Remove some and try again.")
                WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_INTERNAL ->
                    WifiPersistResult.Failed("Internal error while saving WiFi suggestion")
                else ->
                    WifiPersistResult.Failed("Failed to save WiFi suggestion (status=$status)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding network suggestion", e)
            WifiPersistResult.Failed(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Create intent to save WiFi network using ACTION_WIFI_ADD_NETWORKS (Android 11+)
     * This will actually save the network and make it the active connection
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun createSaveNetworkIntent(credentials: WifiCredentials): Intent? {
        return try {
            Log.i(TAG, "Creating save network intent for SSID: '${credentials.ssid}'")
            val suggestions = ArrayList<WifiNetworkSuggestion>()
            
            // Build WifiNetworkSuggestion
            val suggestionBuilder = WifiNetworkSuggestion.Builder()
                .setSsid(credentials.ssid)
            
            // Set password if provided
            if (credentials.password.isNotEmpty()) {
                suggestionBuilder.setWpa2Passphrase(credentials.password)
                Log.d(TAG, "WPA2 passphrase set for save network")
            } else {
                Log.d(TAG, "No password - creating open network suggestion")
            }
            
            val suggestion = suggestionBuilder.build()
            suggestions.add(suggestion)
            
            Log.d(TAG, "WifiNetworkSuggestion created, count: ${suggestions.size}")
            Log.d(TAG, "Suggestion SSID: ${suggestion.ssid}")
            
            // Create intent
            val bundle = Bundle()
            bundle.putParcelableArrayList(Settings.EXTRA_WIFI_NETWORK_LIST, suggestions)
            
            val intent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS).apply {
                putExtras(bundle)
            }
            
            Log.i(TAG, "Save network intent created successfully")
            Log.d(TAG, "Intent action: ${intent.action}")
            Log.d(TAG, "Intent flags: ${intent.flags}")
            Log.d(TAG, "Intent has extras: ${intent.extras != null}")
            if (intent.extras != null) {
                Log.d(TAG, "Extras keys: ${intent.extras!!.keySet()}")
                val list = intent.extras!!.getParcelableArrayList<WifiNetworkSuggestion>(Settings.EXTRA_WIFI_NETWORK_LIST)
                Log.d(TAG, "Network list in extras: ${list?.size} items")
            }
            
            // Verify intent can be resolved
            val resolveInfo = context.packageManager.resolveActivity(intent, 0)
            if (resolveInfo == null) {
                Log.e(TAG, "Intent cannot be resolved - ACTION_WIFI_ADD_NETWORKS not available on this device")
                return null
            }
            Log.d(TAG, "Intent can be resolved by: ${resolveInfo.activityInfo.packageName}/${resolveInfo.activityInfo.name}")
            
            intent
        } catch (e: Exception) {
            Log.e(TAG, "Error creating save network intent", e)
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Check if device is actually connected to the requested SSID
     */
    fun isConnectedToSSID(requestedSSID: String): Boolean {
        return try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val wifiInfo = wifiManager?.connectionInfo
            val connectedSSID = wifiInfo?.ssid?.removeSurrounding("\"")
            val requested = requestedSSID.removeSurrounding("\"")
            
            val isConnected = connectedSSID?.equals(requested, ignoreCase = true) == true
            Log.d(TAG, "Checking connection - Requested: '$requested', Connected: '$connectedSSID', Match: $isConnected")
            isConnected
        } catch (e: Exception) {
            Log.w(TAG, "Error checking SSID connection", e)
            false
        }
    }
}
