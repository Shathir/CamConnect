package com.outdu.camconnect

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ONVIF client data types, exceptions, and SOAP client behavior.
 * Discovery and network operations require Context and are covered by instrumented tests.
 */
@ExperimentalCoroutinesApi
class OnvifClientTest {

    // --- Data model branches ---

    @Test
    fun `OnvifDevice holds default and custom values`() {
        val device = OnvifDevice(
            id = "id1",
            name = "Camera 1",
            xaddrs = "http://192.168.1.1/onvif",
            address = "192.168.1.1",
            port = 80,
            manufacturer = "Vendor",
            model = "Model X",
            isOnline = true
        )
        assertEquals("id1", device.id)
        assertEquals("Camera 1", device.name)
        assertEquals("http://192.168.1.1/onvif", device.xaddrs)
        assertEquals("192.168.1.1", device.address)
        assertEquals(80, device.port)
        assertEquals("Vendor", device.manufacturer)
        assertEquals("Model X", device.model)
        assertTrue(device.isOnline)
    }

    @Test
    fun `OnvifCapabilities defaults are false`() {
        val cap = OnvifCapabilities()
        assertFalse(cap.device)
        assertFalse(cap.media)
        assertFalse(cap.ptz)
        assertFalse(cap.imaging)
        assertFalse(cap.events)
        assertFalse(cap.analytics)
    }

    @Test
    fun `OnvifCapabilities holds custom values`() {
        val cap = OnvifCapabilities(device = true, media = true, ptz = true)
        assertTrue(cap.device)
        assertTrue(cap.media)
        assertTrue(cap.ptz)
        assertFalse(cap.imaging)
    }

    @Test
    fun `OnvifCredentials with BASIC auth type`() {
        val creds = OnvifCredentials("user", "pass", AuthType.BASIC)
        assertEquals("user", creds.username)
        assertEquals("pass", creds.password)
        assertEquals(AuthType.BASIC, creds.authType)
    }

    @Test
    fun `OnvifCredentials default auth type is BASIC`() {
        val creds = OnvifCredentials("u", "p")
        assertEquals(AuthType.BASIC, creds.authType)
    }

    @Test
    fun `AuthType enum values`() {
        assertEquals(3, AuthType.values().size)
        assertNotNull(AuthType.BASIC)
        assertNotNull(AuthType.DIGEST)
        assertNotNull(AuthType.WS_SECURITY)
    }

    // --- OnvifResult branches ---

    @Test
    fun `OnvifResult Success holds data`() {
        val result = OnvifResult.Success(listOf<OnvifDevice>())
        assertTrue(result is OnvifResult.Success)
        assertNotNull((result as OnvifResult.Success).data)
        assertTrue((result).data.isEmpty())
    }

    @Test
    fun `OnvifResult Error holds exception`() {
        val ex = OnvifException.NetworkError("fail")
        val result = OnvifResult.Error<Unit>(ex)
        assertTrue(result is OnvifResult.Error)
        assertEquals(ex, (result as OnvifResult.Error).exception)
    }

    @Test
    fun `OnvifResult Loading holds message`() {
        val result = OnvifResult.Loading<Unit>("loading...")
        assertTrue(result is OnvifResult.Loading)
        assertEquals("loading...", (result as OnvifResult.Loading).message)
    }

    // --- OnvifException branches ---

    @Test
    fun `OnvifException NetworkError with cause`() {
        val cause = RuntimeException("io")
        val e = OnvifException.NetworkError("net", cause)
        assertTrue(e is OnvifException)
        assertEquals(cause, e.cause)
    }

    @Test
    fun `OnvifException AuthenticationError`() {
        val e = OnvifException.AuthenticationError("auth failed")
        assertTrue(e is OnvifException)
        assertTrue(e.message!!.contains("auth failed"))
    }

    @Test
    fun `OnvifException ProtocolError`() {
        val e = OnvifException.ProtocolError("SOAP fault")
        assertTrue(e is OnvifException)
    }

    @Test
    fun `OnvifException TimeoutError`() {
        val e = OnvifException.TimeoutError("timeout")
        assertTrue(e is OnvifException)
    }

    @Test
    fun `OnvifException DeviceNotFound`() {
        val e = OnvifException.DeviceNotFound("404")
        assertTrue(e is OnvifException)
    }

    @Test
    fun `OnvifException ParseError with cause`() {
        val cause = Exception("parse")
        val e = OnvifException.ParseError("parse failed", cause)
        assertTrue(e is OnvifException)
        assertEquals(cause, e.cause)
    }

    // --- OnvifUiState ---

    @Test
    fun `OnvifUiState defaults`() {
        val state = OnvifUiState()
        assertTrue(state.devices.isEmpty())
        assertFalse(state.isDiscovering)
        assertNull(state.error)
        assertTrue(state.logs.isEmpty())
    }

    @Test
    fun `OnvifUiState with values`() {
        val device = OnvifDevice(xaddrs = "http://x", address = "1.2.3.4")
        val state = OnvifUiState(
            devices = listOf(device),
            isDiscovering = true,
            error = "err",
            logs = listOf("log1")
        )
        assertEquals(1, state.devices.size)
        assertTrue(state.isDiscovering)
        assertEquals("err", state.error)
        assertEquals(1, state.logs.size)
    }

    // --- OnvifSoapClient (unreachable URL returns error) ---

    @Test
    fun `OnvifSoapClient getDeviceInformation with unreachable URL returns error`() = runBlocking {
        val client = OnvifSoapClient("http://192.0.2.1:9999/onvif/device_service")
        try {
            val result = client.getDeviceInformation()
            assertTrue("Unreachable URL should yield Error or timeout", result is OnvifResult.Error || result is OnvifResult.Loading)
            if (result is OnvifResult.Error) {
                assertTrue(result.exception is OnvifException.NetworkError || result.exception is OnvifException.TimeoutError)
            }
        } finally {
            client.close()
        }
    }

    @Test
    fun `OnvifSoapClient getCapabilities with unreachable URL returns error`() = runBlocking {
        val client = OnvifSoapClient("http://192.0.2.1:9999/onvif/device_service")
        try {
            val result = client.getCapabilities()
            assertTrue("Unreachable URL should yield Error", result is OnvifResult.Error || result is OnvifResult.Loading)
        } finally {
            client.close()
        }
    }

    @Test
    fun `OnvifSoapClient getSnapshotUri with unreachable URL returns error`() = runBlocking {
        val client = OnvifSoapClient("http://192.0.2.1:9999/onvif/device_service")
        try {
            val result = client.getSnapshotUri("profile1")
            assertTrue("Unreachable URL should yield Error", result is OnvifResult.Error || result is OnvifResult.Loading)
        } finally {
            client.close()
        }
    }

    @Test
    fun `OnvifSoapClient with credentials constructs`() {
        val creds = OnvifCredentials("user", "pass")
        val client = OnvifSoapClient("http://192.168.1.1/onvif", creds)
        client.close()
    }
}
