package com.outdu.camconnect.ui.viewmodels

import app.cash.turbine.test
import com.outdu.camconnect.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for NetworkConfigurationViewModel
 * 
 * Tests cover:
 * - Initial state
 * - Hotspot configuration updates (SSID, password, IP, subnet)
 * - WiFi configuration updates (SSID, password, IP, subnet, dynamic IP)
 * - Data class defaults
 * - StateFlow emissions
 * 
 * Note: API call tests (setHotspotMode, setClientMode, load methods) require androidTest
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NetworkConfigurationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: NetworkConfigurationViewModel

    @Before
    fun setup() {
        viewModel = NetworkConfigurationViewModel()
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial hotspot state should have default values`() {
        val state = viewModel.hotspotState.value
        
        assertEquals("SaberAthena01", state.hotspot_ssid)
        assertEquals("1234567890", state.hotspot_password)
        assertEquals(NetworkDefaults.DEFAULT_HOTSPOT_IP, state.hotspot_ip_address)
        assertEquals(NetworkDefaults.DEFAULT_SUBNET_MASK, state.hotspot_subnet_mask)
    }

    @Test
    fun `initial wifi state should have default values`() {
        val state = viewModel.wifiState.value
        
        assertEquals("SaberAthena01", state.wifi_ssid)
        assertEquals("1234567890", state.wifi_password)
        assertEquals(NetworkDefaults.DEFAULT_WIFI_IP, state.wifi_ip_address)
        assertEquals(NetworkDefaults.DEFAULT_SUBNET_MASK, state.wifi_subnet_mask)
        assertTrue(state.dynamicIpEnabled)
    }

    // ========== Hotspot Configuration Tests ==========

    @Test
    fun `updateHotspotSSID should update hotspot SSID`() {
        // Act
        viewModel.updateHotspotSSID("MyHotspot")

        // Assert
        assertEquals("MyHotspot", viewModel.hotspotState.value.hotspot_ssid)
    }

    @Test
    fun `updateHotspotPassword should update hotspot password`() {
        // Act
        viewModel.updateHotspotPassword("NewPassword123")

        // Assert
        assertEquals("NewPassword123", viewModel.hotspotState.value.hotspot_password)
    }

    @Test
    fun `updateHotspotIPAddress should update hotspot IP`() {
        // Act
        viewModel.updateHotspotIPAddress("192.168.5.1")

        // Assert
        assertEquals("192.168.5.1", viewModel.hotspotState.value.hotspot_ip_address)
    }

    @Test
    fun `updateHotspotSubnetMask should update hotspot subnet mask`() {
        // Act
        viewModel.updateHotspotSubnetMask("255.255.0.0")

        // Assert
        assertEquals("255.255.0.0", viewModel.hotspotState.value.hotspot_subnet_mask)
    }

    @Test
    fun `multiple hotspot updates should work correctly`() {
        // Act
        viewModel.updateHotspotSSID("TestHotspot")
        viewModel.updateHotspotPassword("TestPass")
        viewModel.updateHotspotIPAddress("192.168.10.1")
        viewModel.updateHotspotSubnetMask("255.255.255.0")

        // Assert
        val state = viewModel.hotspotState.value
        assertEquals("TestHotspot", state.hotspot_ssid)
        assertEquals("TestPass", state.hotspot_password)
        assertEquals("192.168.10.1", state.hotspot_ip_address)
        assertEquals("255.255.255.0", state.hotspot_subnet_mask)
    }

    // ========== WiFi Configuration Tests ==========

    @Test
    fun `updateWifiSSID should update wifi SSID`() {
        // Act
        viewModel.updateWifiSSID("MyWiFiNetwork")

        // Assert
        assertEquals("MyWiFiNetwork", viewModel.wifiState.value.wifi_ssid)
    }

    @Test
    fun `updateWifiPassword should update wifi password`() {
        // Act
        viewModel.updateWifiPassword("SecurePassword456")

        // Assert
        assertEquals("SecurePassword456", viewModel.wifiState.value.wifi_password)
    }

    @Test
    fun `updateWifiIPAddress should update wifi IP`() {
        // Act
        viewModel.updateWifiIPAddress("192.168.1.200")

        // Assert
        assertEquals("192.168.1.200", viewModel.wifiState.value.wifi_ip_address)
    }

    @Test
    fun `updateWifiSubnetMask should update wifi subnet mask`() {
        // Act
        viewModel.updateWifiSubnetMask("255.255.240.0")

        // Assert
        assertEquals("255.255.240.0", viewModel.wifiState.value.wifi_subnet_mask)
    }

    @Test
    fun `updateDynamicIpEnabled should toggle dynamic IP`() {
        // Arrange - Initial state is true
        assertTrue(viewModel.wifiState.value.dynamicIpEnabled)

        // Act
        viewModel.updateDynamicIpEnabled(false)

        // Assert
        assertFalse(viewModel.wifiState.value.dynamicIpEnabled)
    }

    @Test
    fun `multiple wifi updates should work correctly`() {
        // Act
        viewModel.updateWifiSSID("TestWiFi")
        viewModel.updateWifiPassword("TestWiFiPass")
        viewModel.updateWifiIPAddress("192.168.1.150")
        viewModel.updateWifiSubnetMask("255.255.255.0")
        viewModel.updateDynamicIpEnabled(false)

        // Assert
        val state = viewModel.wifiState.value
        assertEquals("TestWiFi", state.wifi_ssid)
        assertEquals("TestWiFiPass", state.wifi_password)
        assertEquals("192.168.1.150", state.wifi_ip_address)
        assertEquals("255.255.255.0", state.wifi_subnet_mask)
        assertFalse(state.dynamicIpEnabled)
    }

    // ========== StateFlow Emission Tests ==========

    @Test
    fun `hotspotState should emit updates when SSID changes`() = runTest {
        viewModel.hotspotState.test {
            // Initial state
            val initial = awaitItem()
            assertEquals("SaberAthena01", initial.hotspot_ssid)

            // Update SSID
            viewModel.updateHotspotSSID("NewHotspot")
            val updated = awaitItem()
            assertEquals("NewHotspot", updated.hotspot_ssid)
        }
    }

    @Test
    fun `wifiState should emit updates when dynamic IP changes`() = runTest {
        viewModel.wifiState.test {
            // Initial state
            val initial = awaitItem()
            assertTrue(initial.dynamicIpEnabled)

            // Toggle dynamic IP
            viewModel.updateDynamicIpEnabled(false)
            val updated = awaitItem()
            assertFalse(updated.dynamicIpEnabled)
        }
    }

    // ========== Data Class Tests ==========

    @Test
    fun `HotspotConfiguration should have correct defaults`() {
        val config = HotspotConfiguration()

        assertEquals("SaberAthena01", config.hotspot_ssid)
        assertEquals("1234567890", config.hotspot_password)
        assertEquals(NetworkDefaults.DEFAULT_HOTSPOT_IP, config.hotspot_ip_address)
        assertEquals(NetworkDefaults.DEFAULT_SUBNET_MASK, config.hotspot_subnet_mask)
    }

    @Test
    fun `HotspotConfiguration with custom values should work`() {
        val config = HotspotConfiguration(
            hotspot_ssid = "CustomHotspot",
            hotspot_password = "CustomPass",
            hotspot_ip_address = "10.0.0.1",
            hotspot_subnet_mask = "255.255.0.0"
        )

        assertEquals("CustomHotspot", config.hotspot_ssid)
        assertEquals("CustomPass", config.hotspot_password)
        assertEquals("10.0.0.1", config.hotspot_ip_address)
        assertEquals("255.255.0.0", config.hotspot_subnet_mask)
    }

    @Test
    fun `WifiConfiguration should have correct defaults`() {
        val config = WifiConfiguration()

        assertEquals("SaberAthena01", config.wifi_ssid)
        assertEquals("1234567890", config.wifi_password)
        assertEquals(NetworkDefaults.DEFAULT_WIFI_IP, config.wifi_ip_address)
        assertEquals(NetworkDefaults.DEFAULT_SUBNET_MASK, config.wifi_subnet_mask)
        assertTrue(config.dynamicIpEnabled)
    }

    @Test
    fun `WifiConfiguration with custom values should work`() {
        val config = WifiConfiguration(
            wifi_ssid = "CustomWiFi",
            wifi_password = "CustomWiFiPass",
            wifi_ip_address = "192.168.1.50",
            wifi_subnet_mask = "255.255.255.128",
            dynamicIpEnabled = false
        )

        assertEquals("CustomWiFi", config.wifi_ssid)
        assertEquals("CustomWiFiPass", config.wifi_password)
        assertEquals("192.168.1.50", config.wifi_ip_address)
        assertEquals("255.255.255.128", config.wifi_subnet_mask)
        assertFalse(config.dynamicIpEnabled)
    }

    @Test
    fun `HotspotConfiguration copy should work correctly`() {
        val original = HotspotConfiguration(hotspot_ssid = "Original")
        val copied = original.copy(hotspot_password = "NewPass")

        assertEquals("Original", copied.hotspot_ssid)
        assertEquals("NewPass", copied.hotspot_password)
    }

    @Test
    fun `WifiConfiguration copy should work correctly`() {
        val original = WifiConfiguration(wifi_ssid = "Original", dynamicIpEnabled = true)
        val copied = original.copy(dynamicIpEnabled = false)

        assertEquals("Original", copied.wifi_ssid)
        assertFalse(copied.dynamicIpEnabled)
    }

    // ========== Edge Case Tests ==========

    @Test
    fun `updateHotspotSSID with empty string should update`() {
        viewModel.updateHotspotSSID("")
        assertEquals("", viewModel.hotspotState.value.hotspot_ssid)
    }

    @Test
    fun `updateWifiSSID with special characters should update`() {
        viewModel.updateWifiSSID("Test-WiFi_2.4GHz")
        assertEquals("Test-WiFi_2.4GHz", viewModel.wifiState.value.wifi_ssid)
    }

    @Test
    fun `updateHotspotPassword with long string should update`() {
        val longPassword = "ThisIsAVeryLongPasswordWithLotsOfCharacters123!@#"
        viewModel.updateHotspotPassword(longPassword)
        assertEquals(longPassword, viewModel.hotspotState.value.hotspot_password)
    }

    @Test
    fun `NetworkDefaults constants should have expected values`() {
        assertEquals("255.255.255.0", NetworkDefaults.DEFAULT_SUBNET_MASK)
        assertEquals("192.168.2.1", NetworkDefaults.DEFAULT_HOTSPOT_IP)
        assertEquals("192.168.1.100", NetworkDefaults.DEFAULT_WIFI_IP)
    }
}
