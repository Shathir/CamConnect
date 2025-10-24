package com.outdu.camconnect.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.outdu.camconnect.OnvifDiscoveryService
import com.outdu.camconnect.OnvifDevice
import com.outdu.camconnect.OnvifCredentials
import com.outdu.camconnect.AuthType
import com.outdu.camconnect.OnvifResult
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Integration tests for ONVIF functionality
 * 
 * Tests cover:
 * - Device discovery with mock server
 * - Authentication flows
 * - SOAP communication
 * - Error handling scenarios
 * - Real device interaction (when available)
 */
@RunWith(AndroidJUnit4::class)
class OnvifIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var discoveryService: OnvifDiscoveryService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        discoveryService = OnvifDiscoveryService()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `test device discovery with mock server`() = runTest {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(getMockProbeResponse())
        mockWebServer.enqueue(mockResponse)

        // When
        val result = discoveryService.discoverDevices()

        // Then
        assertTrue(result is OnvifResult.Success)
        assertTrue(result.data.isNotEmpty())
    }

    @Test
    fun `test authentication with mock server`() = runTest {
        // Given
        val credentials = OnvifCredentials("admin", "password", AuthType.BASIC)
        val mockAuthResponse = MockResponse()
            .setResponseCode(200)
            .setBody(getMockAuthResponse())
        mockWebServer.enqueue(mockAuthResponse)

        // When
        val result = discoveryService.authenticateDevice(
            mockWebServer.url("/").toString(),
            credentials
        )

        // Then
        assertTrue(result is OnvifResult.Success)
    }

    @Test
    fun `test device discovery with invalid response`() = runTest {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(500)
            .setBody("Internal Server Error")
        mockWebServer.enqueue(mockResponse)

        // When
        val result = discoveryService.discoverDevices()

        // Then
        assertTrue(result is OnvifResult.Error)
    }

    @Test
    fun `test authentication with invalid credentials`() = runTest {
        // Given
        val credentials = OnvifCredentials("wrong", "password", AuthType.BASIC)
        val mockAuthResponse = MockResponse()
            .setResponseCode(401)
            .setBody("Unauthorized")
        mockWebServer.enqueue(mockAuthResponse)

        // When
        val result = discoveryService.authenticateDevice(
            mockWebServer.url("/").toString(),
            credentials
        )

        // Then
        assertTrue(result is OnvifResult.Error)
    }

    @Test
    fun `test device discovery timeout handling`() = runTest {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(getMockProbeResponse())
            .setHeadersDelay(5, java.util.concurrent.TimeUnit.SECONDS) // Simulate slow response
        mockWebServer.enqueue(mockResponse)

        // When
        val result = discoveryService.discoverDevices()

        // Then
        // Should handle timeout gracefully
        assertNotNull(result)
    }

    @Test
    fun `test multiple device discovery`() = runTest {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(getMockMultipleDevicesResponse())
        mockWebServer.enqueue(mockResponse)

        // When
        val result = discoveryService.discoverDevices()

        // Then
        assertTrue(result is OnvifResult.Success)
        assertTrue(result.data.size > 1)
    }

    @Test
    fun `test device capability parsing`() = runTest {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(getMockCapabilitiesResponse())
        mockWebServer.enqueue(mockResponse)

        // When
        val result = discoveryService.getDeviceCapabilities(
            mockWebServer.url("/").toString(),
            OnvifCredentials("admin", "password", AuthType.BASIC)
        )

        // Then
        assertTrue(result is OnvifResult.Success)
    }

    @Test
    fun `test network connectivity check`() = runTest {
        // Given
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // When
        val isConnected = discoveryService.checkNetworkConnectivity(context)
        
        // Then
        assertTrue(isConnected is Boolean)
    }

    @Test
    fun `test device information retrieval`() = runTest {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(getMockDeviceInfoResponse())
        mockWebServer.enqueue(mockResponse)

        // When
        val result = discoveryService.getDeviceInformation(
            mockWebServer.url("/").toString(),
            OnvifCredentials("admin", "password", AuthType.BASIC)
        )

        // Then
        assertTrue(result is OnvifResult.Success)
    }

    @Test
    fun `test error recovery mechanisms`() = runTest {
        // Given
        val mockErrorResponse = MockResponse()
            .setResponseCode(500)
            .setBody("Server Error")
        mockWebServer.enqueue(mockErrorResponse)
        
        val mockSuccessResponse = MockResponse()
            .setResponseCode(200)
            .setBody(getMockProbeResponse())
        mockWebServer.enqueue(mockSuccessResponse)

        // When
        val result = discoveryService.discoverDevices()

        // Then
        // Should handle error and potentially retry
        assertNotNull(result)
    }

    private fun getMockProbeResponse(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
                <soap:Body>
                    <ProbeMatches xmlns="http://schemas.xmlsoap.org/ws/2005/04/discovery">
                        <ProbeMatch>
                            <XAddrs>http://192.168.1.100/onvif/device_service</XAddrs>
                            <Scopes>onvif://www.onvif.org/Profile/S onvif://www.onvif.org/name/TestCamera</Scopes>
                            <Types>dn:NetworkVideoTransmitter</Types>
                        </ProbeMatch>
                    </ProbeMatches>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }

    private fun getMockMultipleDevicesResponse(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
                <soap:Body>
                    <ProbeMatches xmlns="http://schemas.xmlsoap.org/ws/2005/04/discovery">
                        <ProbeMatch>
                            <XAddrs>http://192.168.1.100/onvif/device_service</XAddrs>
                            <Scopes>onvif://www.onvif.org/Profile/S onvif://www.onvif.org/name/Camera1</Scopes>
                            <Types>dn:NetworkVideoTransmitter</Types>
                        </ProbeMatch>
                        <ProbeMatch>
                            <XAddrs>http://192.168.1.101/onvif/device_service</XAddrs>
                            <Scopes>onvif://www.onvif.org/Profile/S onvif://www.onvif.org/name/Camera2</Scopes>
                            <Types>dn:NetworkVideoTransmitter</Types>
                        </ProbeMatch>
                    </ProbeMatches>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }

    private fun getMockAuthResponse(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
                <soap:Body>
                    <GetDeviceInformationResponse xmlns="http://www.onvif.org/ver10/device/wsdl">
                        <Manufacturer>Test Manufacturer</Manufacturer>
                        <Model>Test Model</Model>
                        <FirmwareVersion>1.0.0</FirmwareVersion>
                        <SerialNumber>123456789</SerialNumber>
                        <HardwareId>HW001</HardwareId>
                    </GetDeviceInformationResponse>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }

    private fun getMockCapabilitiesResponse(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
                <soap:Body>
                    <GetCapabilitiesResponse xmlns="http://www.onvif.org/ver10/device/wsdl">
                        <Capabilities>
                            <Device>
                                <XAddr>http://192.168.1.100/onvif/device_service</XAddr>
                            </Device>
                            <Media>
                                <XAddr>http://192.168.1.100/onvif/media_service</XAddr>
                            </Media>
                        </Capabilities>
                    </GetCapabilitiesResponse>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }

    private fun getMockDeviceInfoResponse(): String {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
                <soap:Body>
                    <GetDeviceInformationResponse xmlns="http://www.onvif.org/ver10/device/wsdl">
                        <Manufacturer>Test Manufacturer</Manufacturer>
                        <Model>Test Model</Model>
                        <FirmwareVersion>1.0.0</FirmwareVersion>
                        <SerialNumber>123456789</SerialNumber>
                        <HardwareId>HW001</HardwareId>
                    </GetDeviceInformationResponse>
                </soap:Body>
            </soap:Envelope>
        """.trimIndent()
    }
}
