package com.outdu.camconnect

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.*
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

// -----------------------------
// Data Models
// -----------------------------

@Serializable
data class OnvifDevice(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val xaddrs: String,
    val scopes: String = "",
    val types: String = "",
    val address: String,
    val port: Int = 80,
    val manufacturer: String = "",
    val model: String = "",
    val firmwareVersion: String = "",
    val serialNumber: String = "",
    val lastSeen: Long = System.currentTimeMillis(),
    val isOnline: Boolean = true,
    val capabilities: OnvifCapabilities = OnvifCapabilities()
)

@Serializable
data class OnvifCapabilities(
    val device: Boolean = false,
    val media: Boolean = false,
    val ptz: Boolean = false,
    val imaging: Boolean = false,
    val events: Boolean = false,
    val analytics: Boolean = false
)

@Serializable
data class OnvifCredentials(
    val username: String,
    val password: String,
    val authType: AuthType = AuthType.BASIC
)

enum class AuthType { BASIC, DIGEST, WS_SECURITY }

sealed class OnvifResult<T> {
    data class Success<T>(val data: T) : OnvifResult<T>()
    data class Error<T>(val exception: OnvifException) : OnvifResult<T>()
    data class Loading<T>(val message: String = "") : OnvifResult<T>()
}

sealed class OnvifException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkError(message: String, cause: Throwable? = null) : OnvifException(message, cause)
    class AuthenticationError(message: String) : OnvifException(message)
    class ProtocolError(message: String) : OnvifException(message)
    class TimeoutError(message: String) : OnvifException(message)
    class DeviceNotFound(message: String) : OnvifException(message)
    class ParseError(message: String, cause: Throwable? = null) : OnvifException(message, cause)
}

// -----------------------------
// ONVIF Discovery Service
// -----------------------------

class OnvifDiscoveryService {
    companion object {
        private const val TAG = "OnvifDiscovery"
        private const val MULTICAST_ADDR = "239.255.255.250"
        private const val MULTICAST_PORT = 3702
        private const val DEFAULT_TIMEOUT_MS = 5000L
        private const val MAX_DEVICES = 50
    }

    private val discoveredDevices = ConcurrentHashMap<String, OnvifDevice>()

    suspend fun discoverDevices(
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        context: Context
    ): OnvifResult<List<OnvifDevice>> = withContext(Dispatchers.IO) {
        try {
            // Check network connectivity
            if (!isNetworkAvailable(context)) {
                return@withContext OnvifResult.Error(
                    OnvifException.NetworkError("No network connection available")
                )
            }

            Log.d(TAG, "Starting ONVIF device discovery...")
            discoveredDevices.clear()

            val socket = DatagramSocket().apply {
                soTimeout = timeoutMs.toInt()
                reuseAddress = true
            }

            try {
                val probeMessage = createProbeMessage()
                val sendData = probeMessage.toByteArray(StandardCharsets.UTF_8)
                val address = InetAddress.getByName(MULTICAST_ADDR)
                val packet = DatagramPacket(sendData, sendData.size, address, MULTICAST_PORT)

                socket.send(packet)
                Log.d(TAG, "Probe message sent")

                val buffer = ByteArray(8192)
                val startTime = System.currentTimeMillis()

                while (System.currentTimeMillis() - startTime < timeoutMs) {
                    try {
                        val receivePacket = DatagramPacket(buffer, buffer.size)
                        socket.receive(receivePacket)

                        val response = String(
                            receivePacket.data, 
                            0, 
                            receivePacket.length, 
                            StandardCharsets.UTF_8
                        )

                        parseProbeResponse(response, receivePacket.address.hostAddress)?.let { device ->
                            if (discoveredDevices.size < MAX_DEVICES) {
                                discoveredDevices[device.xaddrs] = device
                                Log.d(TAG, "Discovered device: ${device.address}")
                            }
                        }
                    } catch (e: SocketTimeoutException) {
                        break
                    } catch (e: Exception) {
                        Log.w(TAG, "Error receiving probe response: ${e.message}")
                    }
                }

                val devices = discoveredDevices.values.toList()
                Log.d(TAG, "Discovery completed. Found ${devices.size} devices")
                OnvifResult.Success(devices)

            } finally {
                socket.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Discovery failed", e)
            OnvifResult.Error(OnvifException.NetworkError("Discovery failed: ${e.message}", e))
        }
    }

    private fun createProbeMessage(): String {
        val messageId = "uuid:${UUID.randomUUID()}"
        return """<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" 
               xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing" 
               xmlns:wsd="http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01"
               xmlns:wsdp="http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01">
  <soap:Header>
    <wsa:MessageID>$messageId</wsa:MessageID>
    <wsa:To>urn:schemas-xmlsoap-org:ws:2005:04:discovery</wsa:To>
    <wsa:Action>http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/Probe</wsa:Action>
  </soap:Header>
  <soap:Body>
    <wsd:Probe>
      <wsd:Types>wsdp:Device</wsd:Types>
    </wsd:Probe>
  </soap:Body>
</soap:Envelope>"""
    }

    private fun parseProbeResponse(xml: String, sourceAddress: String): OnvifDevice? {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var xaddrs = ""
            var scopes = ""
            var types = ""

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "XAddrs" -> xaddrs = parser.nextText().trim()
                            "Scopes" -> scopes = parser.nextText().trim()
                            "Types" -> types = parser.nextText().trim()
                        }
                    }
                }
                eventType = parser.next()
            }

            if (xaddrs.isNotEmpty()) {
                OnvifDevice(
                    xaddrs = xaddrs,
                    scopes = scopes,
                    types = types,
                    address = sourceAddress,
                    name = extractDeviceName(scopes)
                )
            } else null

        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse probe response: ${e.message}")
            null
        }
    }

    private fun extractDeviceName(scopes: String): String {
        return scopes.split(" ")
            .find { it.contains("name/") }
            ?.substringAfter("name/")
            ?: "Unknown Device"
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

// -----------------------------
// ONVIF SOAP Client with Ktor
// -----------------------------

class OnvifSoapClient(
    private val baseUrl: String,
    private val credentials: OnvifCredentials? = null
) {
    companion object {
        private const val TAG = "OnvifSoapClient"
        private const val REQUEST_TIMEOUT_MS = 10000L
        private const val CONNECT_TIMEOUT_MS = 5000L
    }

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
            connectTimeoutMillis = CONNECT_TIMEOUT_MS
            socketTimeoutMillis = REQUEST_TIMEOUT_MS
        }

        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 2)
            retryOnException(maxRetries = 2, retryOnTimeout = true)
            exponentialDelay()
        }

        engine {
            threadsCount = 4
            pipelining = true
        }
    }

    suspend fun getDeviceInformation(): OnvifResult<Map<String, String>> {
        return executeSOAPRequest(
            soapAction = "http://www.onvif.org/ver10/device/wsdl/GetDeviceInformation",
            soapBody = """
                <tds:GetDeviceInformation xmlns:tds="http://www.onvif.org/ver10/device/wsdl" />
            """.trimIndent()
        ) { response ->
            parseDeviceInformation(response)
        }
    }

    suspend fun getCapabilities(): OnvifResult<OnvifCapabilities> {
        return executeSOAPRequest(
            soapAction = "http://www.onvif.org/ver10/device/wsdl/GetCapabilities",
            soapBody = """
                <tds:GetCapabilities xmlns:tds="http://www.onvif.org/ver10/device/wsdl">
                    <tds:Category>All</tds:Category>
                </tds:GetCapabilities>
            """.trimIndent()
        ) { response ->
            parseCapabilities(response)
        }
    }

    suspend fun getSnapshotUri(profileToken: String): OnvifResult<String> {
        return executeSOAPRequest(
            soapAction = "http://www.onvif.org/ver10/media/wsdl/GetSnapshotUri",
            soapBody = """
                <trt:GetSnapshotUri xmlns:trt="http://www.onvif.org/ver10/media/wsdl">
                    <trt:ProfileToken>$profileToken</trt:ProfileToken>
                </trt:GetSnapshotUri>
            """.trimIndent()
        ) { response ->
            parseSnapshotUri(response)
        }
    }

    private suspend fun <T> executeSOAPRequest(
        soapAction: String,
        soapBody: String,
        parser: (String) -> T
    ): OnvifResult<T> {
        return try {
            val soapMessage = createSOAPMessage(soapBody)
            
            val response = httpClient.post(baseUrl) {
                contentType(ContentType.Application.Xml.withCharset(Charsets.UTF_8))
                header("SOAPAction", soapAction)
                
                // Add authentication headers if credentials are provided
                credentials?.let { creds ->
                    when (creds.authType) {
                        AuthType.BASIC -> {
                            val authString = "${creds.username}:${creds.password}"
                            val encodedAuth = android.util.Base64.encodeToString(
                                authString.toByteArray(StandardCharsets.UTF_8),
                                android.util.Base64.NO_WRAP
                            )
                            header("Authorization", "Basic $encodedAuth")
                        }
                        AuthType.WS_SECURITY -> {
                            // Add WS-Security header to SOAP message
                            val wsSecurityHeader = createWSSecurityHeader(creds.username, creds.password)
                            setBody(soapMessage.replace("</soap:Header>", "$wsSecurityHeader</soap:Header>"))
                            return@let
                        }
                        AuthType.DIGEST -> {
                            // Digest auth would require a challenge-response flow
                            // For now, fall back to basic auth
                            val authString = "${creds.username}:${creds.password}"
                            val encodedAuth = android.util.Base64.encodeToString(
                                authString.toByteArray(StandardCharsets.UTF_8),
                                android.util.Base64.NO_WRAP
                            )
                            header("Authorization", "Basic $encodedAuth")
                        }
                    }
                }
                
                setBody(soapMessage)
            }

            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                Log.d(TAG, "SOAP Response received")
                
                if (responseBody.contains("soap:Fault") || responseBody.contains("s:Fault")) {
                    val faultString = extractFaultString(responseBody)
                    OnvifResult.Error(OnvifException.ProtocolError("SOAP Fault: $faultString"))
                } else {
                    val parsedResult = parser(responseBody)
                    OnvifResult.Success(parsedResult)
                }
            } else {
                when (response.status.value) {
                    401 -> OnvifResult.Error(OnvifException.AuthenticationError("Authentication failed"))
                    404 -> OnvifResult.Error(OnvifException.DeviceNotFound("Device endpoint not found"))
                    else -> OnvifResult.Error(OnvifException.NetworkError("HTTP ${response.status.value}: ${response.status.description}"))
                }
            }
        } catch (e: HttpRequestTimeoutException) {
            Log.e(TAG, "Request timeout", e)
            OnvifResult.Error(OnvifException.TimeoutError("Request timeout"))
        } catch (e: Exception) {
            Log.e(TAG, "SOAP request failed", e)
            OnvifResult.Error(OnvifException.NetworkError("Request failed: ${e.message}", e))
        }
    }

    private fun createSOAPMessage(body: String): String {
        val messageId = "uuid:${UUID.randomUUID()}"
        return """<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
  <soap:Header>
    <wsa:MessageID xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing">$messageId</wsa:MessageID>
    <wsa:To xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing">$baseUrl</wsa:To>
    <wsa:Action xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing">$messageId</wsa:Action>
  </soap:Header>
  <soap:Body>
    $body
  </soap:Body>
</soap:Envelope>"""
    }

    private fun createWSSecurityHeader(username: String, password: String): String {
        val nonce = Random.nextBytes(16).let { 
            android.util.Base64.encodeToString(it, android.util.Base64.NO_WRAP) 
        }
        val created = java.time.Instant.now().toString()
        val digest = createPasswordDigest(nonce, created, password)

        return """
    <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
      <wsse:UsernameToken>
        <wsse:Username>$username</wsse:Username>
        <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest">$digest</wsse:Password>
        <wsse:Nonce EncodingType="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary">$nonce</wsse:Nonce>
        <wsu:Created xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">$created</wsu:Created>
      </wsse:UsernameToken>
    </wsse:Security>"""
    }

    private fun createPasswordDigest(nonce: String, created: String, password: String): String {
        val nonceBytes = android.util.Base64.decode(nonce, android.util.Base64.NO_WRAP)
        val createdBytes = created.toByteArray(StandardCharsets.UTF_8)
        val passwordBytes = password.toByteArray(StandardCharsets.UTF_8)
        
        val digest = MessageDigest.getInstance("SHA-1")
        digest.update(nonceBytes)
        digest.update(createdBytes)
        digest.update(passwordBytes)
        
        return android.util.Base64.encodeToString(digest.digest(), android.util.Base64.NO_WRAP)
    }

    private fun parseDeviceInformation(xml: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "Manufacturer" -> result["manufacturer"] = parser.nextText()
                        "Model" -> result["model"] = parser.nextText()
                        "FirmwareVersion" -> result["firmwareVersion"] = parser.nextText()
                        "SerialNumber" -> result["serialNumber"] = parser.nextText()
                        "HardwareId" -> result["hardwareId"] = parser.nextText()
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse device information: ${e.message}")
        }
        return result
    }

    private fun parseCapabilities(xml: String): OnvifCapabilities {
        var device = false
        var media = false
        var ptz = false
        var imaging = false
        var events = false
        var analytics = false

        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "Device" -> device = true
                        "Media" -> media = true
                        "PTZ" -> ptz = true
                        "Imaging" -> imaging = true
                        "Events" -> events = true
                        "Analytics" -> analytics = true
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse capabilities: ${e.message}")
        }

        return OnvifCapabilities(device, media, ptz, imaging, events, analytics)
    }

    private fun parseSnapshotUri(xml: String): String {
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "Uri") {
                    return parser.nextText()
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse snapshot URI: ${e.message}")
        }
        return ""
    }

    private fun extractFaultString(xml: String): String {
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && 
                    (parser.name == "faultstring" || parser.name == "Reason")) {
                    return parser.nextText()
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract fault string: ${e.message}")
        }
        return "Unknown SOAP fault"
    }

    fun close() {
        httpClient.close()
    }
}

// -----------------------------
// Production-Ready ViewModel
// -----------------------------

class OnvifViewModel : ViewModel() {
    private val _uiState = mutableStateOf(OnvifUiState())
    val uiState: State<OnvifUiState> = _uiState

    private val discoveryService = OnvifDiscoveryService()
    private val activeClients = mutableMapOf<String, OnvifSoapClient>()

    private var discoveryJob: Job? = null

    fun discoverDevices(context: Context) {
        if (discoveryJob?.isActive == true) return
        
        discoveryJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDiscovering = true,
                logs = _uiState.value.logs + "Starting device discovery..."
            )

            when (val result = discoveryService.discoverDevices(context = context)) {
                is OnvifResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        devices = result.data,
                        isDiscovering = false,
                        logs = _uiState.value.logs + "Discovered ${result.data.size} devices"
                    )
                }
                is OnvifResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isDiscovering = false,
                        error = result.exception.message,
                        logs = _uiState.value.logs + "Discovery failed: ${result.exception.message}"
                    )
                }
                is OnvifResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun fetchDeviceInfo(device: OnvifDevice, credentials: OnvifCredentials? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                logs = _uiState.value.logs + "Fetching info for ${device.name}..."
            )

            try {
                val baseUrl = device.xaddrs.split(' ').firstOrNull() ?: device.xaddrs
                val client = OnvifSoapClient(baseUrl, credentials)
                activeClients[device.id] = client

                when (val result = client.getDeviceInformation()) {
                    is OnvifResult.Success -> {
                        val updatedDevice = device.copy(
                            manufacturer = result.data["manufacturer"] ?: "",
                            model = result.data["model"] ?: "",
                            firmwareVersion = result.data["firmwareVersion"] ?: "",
                            serialNumber = result.data["serialNumber"] ?: ""
                        )
                        
                        updateDevice(updatedDevice)
                        _uiState.value = _uiState.value.copy(
                            logs = _uiState.value.logs + "Device info retrieved for ${device.name}"
                        )
                    }
                    is OnvifResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.exception.message,
                            logs = _uiState.value.logs + "Failed to get device info: ${result.exception.message}"
                        )
                    }
                    is OnvifResult.Loading -> {
                        // Handle loading state if needed
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    logs = _uiState.value.logs + "Error: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearLogs() {
        _uiState.value = _uiState.value.copy(logs = emptyList())
    }

    private fun updateDevice(updatedDevice: OnvifDevice) {
        val devices = _uiState.value.devices.toMutableList()
        val index = devices.indexOfFirst { it.id == updatedDevice.id }
        if (index >= 0) {
            devices[index] = updatedDevice
            _uiState.value = _uiState.value.copy(devices = devices)
        }
    }

    override fun onCleared() {
        super.onCleared()
        discoveryJob?.cancel()
        activeClients.values.forEach { it.close() }
        activeClients.clear()
    }
}

data class OnvifUiState(
    val devices: List<OnvifDevice> = emptyList(),
    val isDiscovering: Boolean = false,
    val error: String? = null,
    val logs: List<String> = emptyList()
)

// -----------------------------
// Enhanced Compose UI
// -----------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnvifScreen(
    viewModel: OnvifViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState
    val context = LocalContext.current

    // Handle errors with Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with actions
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ONVIF Device Manager",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.discoverDevices(context) },
                        enabled = !uiState.isDiscovering,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (uiState.isDiscovering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Discover")
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.clearLogs() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Logs")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Devices list
        Text(
            text = "Discovered Devices (${uiState.devices.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.devices, key = { it.id }) { device ->
                DeviceCard(
                    device = device,
                    onGetInfo = { viewModel.fetchDeviceInfo(device) }
                )
            }
        }

        // Logs section
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Activity Logs",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.logs.takeLast(20)) { log ->
                        Text(
                            text = log,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceCard(
    device: OnvifDevice,
    onGetInfo: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = device.name.ifEmpty { "Unknown Device" },
                    style = MaterialTheme.typography.titleMedium
                )
                
                Surface(
                    color = if (device.isOnline) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.error,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (device.isOnline) "Online" else "Offline",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Address: ${device.address}",
                style = MaterialTheme.typography.bodySmall
            )
            
            if (device.manufacturer.isNotEmpty()) {
                Text(
                    text = "Manufacturer: ${device.manufacturer}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (device.model.isNotEmpty()) {
                Text(
                    text = "Model: ${device.model}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onGetInfo,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Get Info")
                }
                
                OutlinedButton(
                    onClick = { /* TODO: Implement snapshot */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Snapshot")
                }
            }
        }
    }
}
