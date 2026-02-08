//package com.outdu.camconnect.utils
//
//import android.content.Context
//import org.junit.Assert.*
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.RobolectricTestRunner
//import org.robolectric.RuntimeEnvironment
//
///**
// * Unit tests for WifiConnectionManager
// *
// * Tests focus on parseQRData function which is pure logic without Android dependencies
// *
// * Note: Network connection tests require androidTest due to ConnectivityManager,
// * WifiManager, and system permissions
// */
//@RunWith(RobolectricTestRunner::class)
//class WifiConnectionManagerTest {
//
//    private lateinit var context: Context
//
//    @Before
//    fun setup() {
//        context = RuntimeEnvironment.getApplication()
//    }
//
//    // ========== parseQRData Tests ==========
//
//    @Test
//    fun `parseQRData should parse valid JSON with all fields`() {
//        // Arrange
//        val qrData = """{"ssid":"TestNetwork","password":"TestPass123","ip":"192.168.1.100","mac":"AA:BB:CC:DD:EE:FF"}"""
//
//        // Act
//        val result = WifiConnectionManager(context).parseQRData(qrData)
//
//        // Assert
//        assertNotNull(result)
//        assertEquals("TestNetwork", result?.ssid)
//        assertEquals("TestPass123", result?.password)
//        assertEquals("192.168.1.100", result?.ip)
//        assertEquals("AA:BB:CC:DD:EE:FF", result?.macAddress)
//    }
//
//    @Test
//    fun `parseQRData should parse JSON with only required fields`() {
//        // Arrange
//        val qrData = """{"ssid":"MyWiFi","password":"mypassword"}"""
//
//        // Act
//        val result = WifiConnectionManager(context).parseQRData(qrData)
//
//        // Assert
//        assertNotNull(result)
//        assertEquals("MyWiFi", result?.ssid)
//        assertEquals("mypassword", result?.password)
//        assertNull(result?.ip)
//        assertNull(result?.macAddress)
//    }
//
//    @Test
//    fun `parseQRData should handle empty optional fields`() {
//        // Arrange
//        val qrData = """{"ssid":"TestSSID","password":"pass","ip":"","mac":""}"""
//
//        // Act
//        val result = WifiConnectionManager(context).parseQRData(qrData)
//
//        // Assert
//        assertNotNull(result)
//        assertEquals("TestSSID", result?.ssid)
//        assertEquals("pass", result?.password)
//        assertNull(result?.ip) // Empty strings should be null
//        assertNull(result?.macAddress)
//    }
//
//    @Test
//    fun `parseQRData should handle macaddress field variant`() {
//        // Arrange - lowercase "macaddress"
//        val qrData = """{"ssid":"Test","password":"pass","macaddress":"11:22:33:44:55:66"}"""
//
//        // Act
//        val result = WifiConnectionManager(context).parseQRData(qrData)
//
//        // Assert
//        assertNotNull(result)
//        assertEquals("11:22:33:44:55:66", result?.macAddress)
//    }
//
//    @Test
//    fun `parseQRData should handle macAddress field variant`() {
//        // Arrange - camelCase "macAddress"
//        val qrData = """{"ssid":"Test","password":"pass","macAddress":"AA:BB:CC:DD:EE:FF"}"""
//
//        // Act
//        val result = WifiConnectionManager(context).parseQRData(qrData)
//
//        // Assert
//        assertNotNull(result)
//        assertEquals("AA:BB:CC:DD:EE:FF", result?.macAddress)
//    }
//
//    @Test
//    fun `parseQRData should prefer mac over macaddress variant`() {
//        // Arrange - Both "mac" and "macaddress" present
//        val qrData = """{"ssid":"Test","password":"pass","mac":"11:11:11:11:11:11","macaddress":"22:22:22:22:22:22"}"""
//
//        // Act
//        val result = WifiConnectionManager(context).parseQRData(qrData)
//
//        // Assert
//        assertNotNull(result)
//        assertEquals("11:11:11:11:11:11", result?.macAddress) // "mac" has priority
//    }
//
//    @Test
//    fun `parseQRData should return null for invalid JSON`() {
//        // Arrange
//        val invalidQrData = "not valid json at all"
//
//        // Act
//        val result = WifiConnectionManager(context).parseQRData(invalidQrData)
//
//        // Assert
//        assertNull(result)
//    }
//
//    @Test
//    fun `parseQRData should return null for missing required fields`() {
//        // Arrange - Missing password
//        val qrData = """{"ssid":"TestSSID"}"""
//
//        // Act
//        val result = WifiConnectionManager(context).parseQRData(qrData)
//
//        // Assert
//        assertNull(result) // Should fail due to missing password
//    }
//
//    @Test
//    fun `parseQRData should return null for empty JSON`() {
//        // Arrange
//        val qrData = "{}"
//
//        // Act
//        val result = WifiConnectionManager(context).parseQRData(qrData)
//
//        // Assert
//        assertNull(result)
//    }
//
//    @Test
//    fun `parseQRData should handle special characters in credentials`() {
//        // Arrange
//        val qrData = """{"ssid":"Test-WiFi_2.4","password":"P@ss!w0rd#123"}"""
//
//        // Act
//        val result = WifiConnectionManager(context).parseQRData(qrData)
//
//        // Assert
//        assertNotNull(result)
//        assertEquals("Test-WiFi_2.4", result?.ssid)
//        assertEquals("P@ss!w0rd#123", result?.password)
//    }
//
//    @Test
//    fun `parseQRData should handle spaces in SSID`() {
//        // Arrange
//        val qrData = """{"ssid":"My Home Network","password":"password123"}"""
//
//        // Act
//        val result = WifiConnectionManager(context).parseQRData(qrData)
//
//        // Assert
//        assertNotNull(result)
//        assertEquals("My Home Network", result?.ssid)
//    }
//
//    @Test
//    fun `parseQRData should handle Unicode characters`() {
//        // Arrange
//        val qrData = """{"ssid":"WiFi-网络","password":"пароль123"}"""
//
//        // Act
//        val result = WifiConnectionManager(context).parseQRData(qrData)
//
//        // Assert
//        assertNotNull(result)
//        assertEquals("WiFi-网络", result?.ssid)
//        assertEquals("пароль123", result?.password)
//    }
//
//    // ========== WifiCredentials Data Class Tests ==========
//
//    @Test
//    fun `WifiCredentials should have correct default values`() {
//        val credentials = WifiCredentials(
//            ssid = "TestSSID",
//            password = "TestPass"
//        )
//
//        assertEquals("TestSSID", credentials.ssid)
//        assertEquals("TestPass", credentials.password)
//        assertNull(credentials.ip)
//        assertNull(credentials.macAddress)
//    }
//
//    @Test
//    fun `WifiCredentials with all fields should work`() {
//        val credentials = WifiCredentials(
//            ssid = "Test",
//            password = "Pass",
//            ip = "192.168.1.1",
//            macAddress = "AA:BB:CC:DD:EE:FF"
//        )
//
//        assertEquals("Test", credentials.ssid)
//        assertEquals("Pass", credentials.password)
//        assertEquals("192.168.1.1", credentials.ip)
//        assertEquals("AA:BB:CC:DD:EE:FF", credentials.macAddress)
//    }
//
//    // ========== WifiConnectionResult Sealed Class Tests ==========
//
//    @Test
//    fun `WifiConnectionResult should have Connecting type`() {
//        val result = WifiConnectionResult.Connecting
//        assertTrue(result is WifiConnectionResult.Connecting)
//    }
//
//    @Test
//    fun `WifiConnectionResult should have Timeout type`() {
//        val result = WifiConnectionResult.Timeout
//        assertTrue(result is WifiConnectionResult.Timeout)
//    }
//
//    // ========== WifiPersistResult Sealed Class Tests ==========
//
//    @Test
//    fun `WifiPersistResult should have Success type`() {
//        val result = WifiPersistResult.Success
//        assertTrue(result is WifiPersistResult.Success)
//    }
//
//    @Test
//    fun `WifiPersistResult should have AlreadyExists type`() {
//        val result = WifiPersistResult.AlreadyExists
//        assertTrue(result is WifiPersistResult.AlreadyExists)
//    }
//
//    @Test
//    fun `WifiPersistResult should have Failed type with message`() {
//        val result = WifiPersistResult.Failed("Test error")
//        assertTrue(result is WifiPersistResult.Failed)
//        assertEquals("Test error", (result as WifiPersistResult.Failed).error)
//    }
//
//}
