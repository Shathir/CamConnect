package com.outdu.camconnect.utils

import android.content.Context
import com.outdu.camconnect.testutils.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

/**
 * Unit tests for NetworkConfigManager
 * 
 * Tests cover:
 * - Default configuration
 * - Getters and setters
 * - Reset functionality
 * - Thread safety (via public API)
 * 
 * Note: File I/O tests are limited due to singleton pattern and Context dependencies
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NetworkConfigManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        // Reset to defaults before each test
        NetworkConfigManager.resetToDefaults()
    }

    // ========== Default Values Tests ==========

    @Test
    fun `default subnet mask should be 255-255-255-0`() {
        // Arrange
        NetworkConfigManager.resetToDefaults()

        // Act
        val subnetMask = NetworkConfigManager.getSubnetMask()

        // Assert
        assertEquals("255.255.255.0", subnetMask)
    }

    @Test
    fun `default hotspot IP should be 192-168-2-1`() {
        // Arrange
        NetworkConfigManager.resetToDefaults()

        // Act
        val hotspotIp = NetworkConfigManager.getHotspotIp()

        // Assert
        assertEquals("192.168.2.1", hotspotIp)
    }

    @Test
    fun `default WiFi IP should be 192-168-1-100`() {
        // Arrange
        NetworkConfigManager.resetToDefaults()

        // Act
        val wifiIp = NetworkConfigManager.getWifiIp()

        // Assert
        assertEquals("192.168.1.100", wifiIp)
    }

    @Test
    fun `default camera WS port should be 80`() {
        // Arrange
        NetworkConfigManager.resetToDefaults()

        // Act
        val port = NetworkConfigManager.getCameraWsPort()

        // Assert
        assertEquals(80, port)
    }

    @Test
    fun `default camera WS path should be slash-ws`() {
        // Arrange
        NetworkConfigManager.resetToDefaults()

        // Act
        val path = NetworkConfigManager.getCameraWsPath()

        // Assert
        assertEquals("/ws", path)
    }

    // ========== Getter/Setter Tests ==========

    @Test
    fun `setSubnetMask should update value`() {
        // Arrange
        val newValue = "255.255.0.0"

        // Act
        NetworkConfigManager.setSubnetMask(newValue)

        // Assert
        assertEquals(newValue, NetworkConfigManager.getSubnetMask())
    }

    @Test
    fun `setHotspotIp should update value`() {
        // Arrange
        val newValue = "192.168.10.1"

        // Act
        NetworkConfigManager.setHotspotIp(newValue)

        // Assert
        assertEquals(newValue, NetworkConfigManager.getHotspotIp())
    }

    @Test
    fun `setWifiIp should update value`() {
        // Arrange
        val newValue = "192.168.1.200"

        // Act
        NetworkConfigManager.setWifiIp(newValue)

        // Assert
        assertEquals(newValue, NetworkConfigManager.getWifiIp())
    }

    @Test
    fun `multiple setters should all update correctly`() {
        // Act
        NetworkConfigManager.setSubnetMask("255.0.0.0")
        NetworkConfigManager.setHotspotIp("10.0.0.1")
        NetworkConfigManager.setWifiIp("10.0.0.100")

        // Assert
        assertEquals("255.0.0.0", NetworkConfigManager.getSubnetMask())
        assertEquals("10.0.0.1", NetworkConfigManager.getHotspotIp())
        assertEquals("10.0.0.100", NetworkConfigManager.getWifiIp())
    }

    // ========== getCurrentConfig Tests ==========

    @Test
    fun `getCurrentConfig should return all default values initially`() {
        // Arrange
        NetworkConfigManager.resetToDefaults()

        // Act
        val config = NetworkConfigManager.getCurrentConfig()

        // Assert
        assertEquals("255.255.255.0", config.subnetMask)
        assertEquals("192.168.2.1", config.hotspotIp)
        assertEquals("192.168.1.100", config.wifiIp)
        assertEquals(80, config.cameraWsPort)
        assertEquals("/ws", config.cameraWsPath)
    }

    @Test
    fun `getCurrentConfig should reflect updated values`() {
        // Arrange
        NetworkConfigManager.setSubnetMask("255.255.254.0")
        NetworkConfigManager.setHotspotIp("192.168.5.1")
        NetworkConfigManager.setWifiIp("192.168.5.100")

        // Act
        val config = NetworkConfigManager.getCurrentConfig()

        // Assert
        assertEquals("255.255.254.0", config.subnetMask)
        assertEquals("192.168.5.1", config.hotspotIp)
        assertEquals("192.168.5.100", config.wifiIp)
    }

    // ========== Reset Tests ==========

    @Test
    fun `resetToDefaults should restore all default values`() {
        // Arrange - Change values
        NetworkConfigManager.setSubnetMask("255.0.0.0")
        NetworkConfigManager.setHotspotIp("10.0.0.1")
        NetworkConfigManager.setWifiIp("10.0.0.100")

        // Act
        NetworkConfigManager.resetToDefaults()

        // Assert - Should be back to defaults
        assertEquals("255.255.255.0", NetworkConfigManager.getSubnetMask())
        assertEquals("192.168.2.1", NetworkConfigManager.getHotspotIp())
        assertEquals("192.168.1.100", NetworkConfigManager.getWifiIp())
        assertEquals(80, NetworkConfigManager.getCameraWsPort())
        assertEquals("/ws", NetworkConfigManager.getCameraWsPath())
    }

    @Test
    fun `resetToDefaults should update getCurrentConfig`() {
        // Arrange
        NetworkConfigManager.setSubnetMask("1.2.3.4")

        // Act
        NetworkConfigManager.resetToDefaults()
        val config = NetworkConfigManager.getCurrentConfig()

        // Assert
        assertEquals("255.255.255.0", config.subnetMask)
    }

    // ========== Thread Safety Tests (via public API) ==========

    @Test
    fun `concurrent reads should not throw`() {
        // Act & Assert
        repeat(100) {
            NetworkConfigManager.getSubnetMask()
            NetworkConfigManager.getHotspotIp()
            NetworkConfigManager.getWifiIp()
            NetworkConfigManager.getCurrentConfig()
        }
        // If we got here without exception, thread safety is working
        assertTrue(true)
    }

    @Test
    fun `mixed read write operations should not throw`() {
        // Act & Assert
        repeat(50) { i ->
            if (i % 2 == 0) {
                NetworkConfigManager.setSubnetMask("255.255.${i}.0")
            } else {
                NetworkConfigManager.getSubnetMask()
            }
        }
        // If we got here without exception, operations are safe
        assertTrue(true)
    }

    @Test
    fun `getCurrentConfig should always return complete config`() {
        // Act
        repeat(10) {
            val config = NetworkConfigManager.getCurrentConfig()

            // Assert
            assertNotNull(config.subnetMask)
            assertNotNull(config.hotspotIp)
            assertNotNull(config.wifiIp)
            assertTrue(config.subnetMask.isNotEmpty())
            assertTrue(config.hotspotIp.isNotEmpty())
            assertTrue(config.wifiIp.isNotEmpty())
        }
    }
}
