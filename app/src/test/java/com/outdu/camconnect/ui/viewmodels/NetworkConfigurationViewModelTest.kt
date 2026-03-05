package com.outdu.camconnect.ui.viewmodels

import app.cash.turbine.test
import com.outdu.camconnect.communication.MotocamAPIAndroidHelper
import com.outdu.camconnect.testutils.MainDispatcherRule
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkConfigurationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: NetworkConfigurationViewModel

    @Before
    fun setup() {
        mockkObject(MotocamAPIAndroidHelper)
        every {
            MotocamAPIAndroidHelper.setWifiClientAsync(
                any(), any(), any(), any(), any(), any(), any()
            )
        } answers {
            val callback = lastArg<(Boolean, String?) -> Unit>()
            callback(false, "unavailable")
        }
        viewModel = NetworkConfigurationViewModel()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial hotspot state has default values`() = runTest {
        viewModel.hotspotState.test {
            val state = awaitItem()
            assertEquals("SaberAthena01", state.hotspot_ssid)
            assertEquals("1234567890", state.hotspot_password)
            assertEquals(NetworkDefaults.DEFAULT_HOTSPOT_IP, state.hotspot_ip_address)
            assertEquals(NetworkDefaults.DEFAULT_SUBNET_MASK, state.hotspot_subnet_mask)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial wifi state has default values`() = runTest {
        viewModel.wifiState.test {
            val state = awaitItem()
            assertEquals("SaberAthena01", state.wifi_ssid)
            assertEquals("1234567890", state.wifi_password)
            assertEquals(NetworkDefaults.DEFAULT_WIFI_IP, state.wifi_ip_address)
            assertEquals(NetworkDefaults.DEFAULT_SUBNET_MASK, state.wifi_subnet_mask)
            assertTrue(state.dynamicIpEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== Hotspot State Update Tests ==========

    @Test
    fun `updateHotspotSSID updates hotspot ssid`() = runTest {
        viewModel.updateHotspotSSID("MyHotspot")
        viewModel.hotspotState.test {
            assertEquals("MyHotspot", awaitItem().hotspot_ssid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateHotspotPassword updates hotspot password`() = runTest {
        viewModel.updateHotspotPassword("secret123")
        viewModel.hotspotState.test {
            assertEquals("secret123", awaitItem().hotspot_password)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateHotspotIPAddress updates hotspot ip`() = runTest {
        viewModel.updateHotspotIPAddress("192.168.10.1")
        viewModel.hotspotState.test {
            assertEquals("192.168.10.1", awaitItem().hotspot_ip_address)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateHotspotSubnetMask updates hotspot subnet mask`() = runTest {
        viewModel.updateHotspotSubnetMask("255.255.0.0")
        viewModel.hotspotState.test {
            assertEquals("255.255.0.0", awaitItem().hotspot_subnet_mask)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== WiFi State Update Tests ==========

    @Test
    fun `updateWifiSSID updates wifi ssid`() = runTest {
        viewModel.updateWifiSSID("MyWiFi")
        viewModel.wifiState.test {
            assertEquals("MyWiFi", awaitItem().wifi_ssid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateWifiPassword updates wifi password`() = runTest {
        viewModel.updateWifiPassword("wifiSecret")
        viewModel.wifiState.test {
            assertEquals("wifiSecret", awaitItem().wifi_password)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateWifiIPAddress updates wifi ip`() = runTest {
        viewModel.updateWifiIPAddress("192.168.1.50")
        viewModel.wifiState.test {
            assertEquals("192.168.1.50", awaitItem().wifi_ip_address)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateWifiSubnetMask updates wifi subnet mask`() = runTest {
        viewModel.updateWifiSubnetMask("255.255.255.0")
        viewModel.wifiState.test {
            assertEquals("255.255.255.0", awaitItem().wifi_subnet_mask)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateDynamicIpEnabled updates dynamic ip flag`() = runTest {
        viewModel.updateDynamicIpEnabled(false)
        viewModel.wifiState.test {
            assertFalse(awaitItem().dynamicIpEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== setHotspotMode Validation Tests ==========

    @Test
    fun `setHotspotMode with empty SSID calls onError`() = runTest {
        viewModel.updateHotspotSSID("")
        viewModel.updateHotspotPassword("validpass")
        var errorMessage: String? = null
        var successCalled = false
        viewModel.setHotspotMode(this, onSuccess = { successCalled = true }, onError = { errorMessage = it })
        advanceUntilIdle()
        assertEquals("Please enter a valid SSID", errorMessage)
        assertFalse(successCalled)
    }

    @Test
    fun `setHotspotMode with placeholder SSID calls onError`() = runTest {
        viewModel.updateHotspotSSID("Enter hotspot name")
        viewModel.updateHotspotPassword("validpass")
        var errorMessage: String? = null
        viewModel.setHotspotMode(this, onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()
        assertEquals("Please enter a valid SSID", errorMessage)
    }

    @Test
    fun `setHotspotMode with empty password calls onError`() = runTest {
        viewModel.updateHotspotSSID("ValidSSID")
        viewModel.updateHotspotPassword("")
        var errorMessage: String? = null
        viewModel.setHotspotMode(this, onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()
        assertEquals("Please enter a password", errorMessage)
    }

    @Test
    fun `setHotspotMode with placeholder password calls onError`() = runTest {
        viewModel.updateHotspotSSID("ValidSSID")
        viewModel.updateHotspotPassword("Enter Password")
        var errorMessage: String? = null
        viewModel.setHotspotMode(this, onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()
        assertEquals("Please enter a password", errorMessage)
    }

    @Test
    fun `setHotspotMode with valid config invokes API and onSuccess when API succeeds`() = runTest {
        mockkObject(MotocamAPIAndroidHelper)
        every {
            MotocamAPIAndroidHelper.setWifiHotspotAsync(
                any(), any(), any(), any(), any(), any(), any()
            )
        } answers {
            val callback = lastArg<(Boolean, String?) -> Unit>()
            callback(true, null)
        }
        viewModel.updateHotspotSSID("ValidSSID")
        viewModel.updateHotspotPassword("validpass")
        var successCalled = false
        var errorMessage: String? = "not null"
        viewModel.setHotspotMode(this, onSuccess = { successCalled = true }, onError = { errorMessage = it })
        advanceUntilIdle()
        assertTrue(successCalled)
        assertEquals("not null", errorMessage)
    }

    @Test
    fun `setHotspotMode with valid config invokes API and onError when API fails`() = runTest {
        mockkObject(MotocamAPIAndroidHelper)
        every {
            MotocamAPIAndroidHelper.setWifiHotspotAsync(
                any(), any(), any(), any(), any(), any(), any()
            )
        } answers {
            val callback = lastArg<(Boolean, String?) -> Unit>()
            callback(false, "Network error")
        }
        viewModel.updateHotspotSSID("ValidSSID")
        viewModel.updateHotspotPassword("validpass")
        var errorMessage: String? = null
        viewModel.setHotspotMode(this, onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()
        assertEquals("Network error", errorMessage)
    }

    @Test
    fun `setHotspotMode onError uses default message when API returns null error`() = runTest {
        mockkObject(MotocamAPIAndroidHelper)
        every {
            MotocamAPIAndroidHelper.setWifiHotspotAsync(
                any(), any(), any(), any(), any(), any(), any()
            )
        } answers {
            val callback = lastArg<(Boolean, String?) -> Unit>()
            callback(false, null)
        }
        viewModel.updateHotspotSSID("ValidSSID")
        viewModel.updateHotspotPassword("validpass")
        var errorMessage: String? = null
        viewModel.setHotspotMode(this, onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()
        assertEquals("Failed to start hotspot mode", errorMessage)
    }

    // ========== setClientMode Validation Tests ==========

    @Test
    fun `setClientMode with empty SSID calls onError`() = runTest {
        viewModel.updateWifiSSID("")
        viewModel.updateWifiPassword("validpass")
        var errorMessage: String? = null
        viewModel.setClientMode(this, onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()
        assertEquals("Please enter a valid SSID", errorMessage)
    }

    @Test
    fun `setClientMode with empty password calls onError`() = runTest {
        viewModel.updateWifiSSID("ValidSSID")
        viewModel.updateWifiPassword("")
        var errorMessage: String? = null
        viewModel.setClientMode(this, onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()
        assertEquals("Please enter a password", errorMessage)
    }

    @Test
    fun `setClientMode with static IP and empty IP address calls onError`() = runTest {
        viewModel.updateWifiSSID("ValidSSID")
        viewModel.updateWifiPassword("pass")
        viewModel.updateDynamicIpEnabled(false)
        viewModel.updateWifiIPAddress("")
        viewModel.updateWifiSubnetMask("255.255.255.0")
        var errorMessage: String? = null
        viewModel.setClientMode(this, onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()
        assertEquals("Please enter an IP address", errorMessage)
    }

    @Test
    fun `setClientMode with static IP and empty subnet mask calls onError`() = runTest {
        viewModel.updateWifiSSID("ValidSSID")
        viewModel.updateWifiPassword("pass")
        viewModel.updateDynamicIpEnabled(false)
        viewModel.updateWifiIPAddress("192.168.1.100")
        viewModel.updateWifiSubnetMask("")
        var errorMessage: String? = null
        viewModel.setClientMode(this, onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()
        assertEquals("Please enter a subnet mask", errorMessage)
    }

    @Test
    fun `setClientMode with valid config and dynamic IP invokes API and onSuccess`() = runTest {
        mockkObject(MotocamAPIAndroidHelper)
        every {
            MotocamAPIAndroidHelper.setWifiClientAsync(
                any(), any(), any(), any(), any(), any(), any()
            )
        } answers {
            val callback = lastArg<(Boolean, String?) -> Unit>()
            callback(true, null)
        }
        viewModel.updateWifiSSID("ValidSSID")
        viewModel.updateWifiPassword("pass")
        viewModel.updateDynamicIpEnabled(true)
        var successCalled = false
        viewModel.setClientMode(this, onSuccess = { successCalled = true }, onError = {})
        advanceUntilIdle()
        assertTrue(successCalled)
    }

    @Test
    fun `setClientMode with valid config and static IP invokes API and onError when API fails`() = runTest {
        mockkObject(MotocamAPIAndroidHelper)
        every {
            MotocamAPIAndroidHelper.setWifiClientAsync(
                any(), any(), any(), any(), any(), any(), any()
            )
        } answers {
            val callback = lastArg<(Boolean, String?) -> Unit>()
            callback(false, "Connection failed")
        }
        viewModel.updateWifiSSID("ValidSSID")
        viewModel.updateWifiPassword("pass")
        viewModel.updateDynamicIpEnabled(false)
        viewModel.updateWifiIPAddress("192.168.1.100")
        viewModel.updateWifiSubnetMask("255.255.255.0")
        var errorMessage: String? = null
        viewModel.setClientMode(this, onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()
        assertEquals("Connection failed", errorMessage)
    }

    @Test
    fun `setClientMode onError uses default message when API returns null error`() = runTest {
        mockkObject(MotocamAPIAndroidHelper)
        every {
            MotocamAPIAndroidHelper.setWifiClientAsync(
                any(), any(), any(), any(), any(), any(), any()
            )
        } answers {
            val callback = lastArg<(Boolean, String?) -> Unit>()
            callback(false, null)
        }
        viewModel.updateWifiSSID("ValidSSID")
        viewModel.updateWifiPassword("pass")
        viewModel.updateDynamicIpEnabled(true)
        var errorMessage: String? = null
        viewModel.setClientMode(this, onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()
        assertEquals("Failed to connect to network", errorMessage)
    }

    // ========== loadHotspotConfiguration Tests ==========

    @Test
    fun `loadHotspotConfiguration updates state when API returns config`() = runTest {
        mockkObject(MotocamAPIAndroidHelper)
        every {
            MotocamAPIAndroidHelper.getWifiHotspotConfigAsync(any(), any())
        } answers {
            val callback = lastArg<(Map<String, Any>?, String?) -> Unit>()
            callback(
                mapOf(
                    "ssid" to "LoadedSSID",
                    "encryptionkey" to "LoadedPass",
                    "ipaddress" to "192.168.2.2",
                    "subnetmask" to "255.255.0.0"
                ),
                null
            )
        }
        viewModel.loadHotspotConfiguration()
        advanceUntilIdle()
        viewModel.hotspotState.test {
            val state = awaitItem()
            assertEquals("LoadedSSID", state.hotspot_ssid)
            assertEquals("LoadedPass", state.hotspot_password)
            assertEquals("192.168.2.2", state.hotspot_ip_address)
            assertEquals("255.255.0.0", state.hotspot_subnet_mask)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadHotspotConfiguration uses placeholders when API returns empty strings`() = runTest {
        mockkObject(MotocamAPIAndroidHelper)
        every {
            MotocamAPIAndroidHelper.getWifiHotspotConfigAsync(any(), any())
        } answers {
            val callback = lastArg<(Map<String, Any>?, String?) -> Unit>()
            callback(
                mapOf(
                    "ssid" to "",
                    "encryptionkey" to "",
                    "ipaddress" to "",
                    "subnetmask" to ""
                ),
                null
            )
        }
        viewModel.loadHotspotConfiguration()
        advanceUntilIdle()
        viewModel.hotspotState.test {
            val state = awaitItem()
            assertEquals("Enter hotspot name", state.hotspot_ssid)
            assertEquals("Enter Password", state.hotspot_password)
            assertEquals(NetworkDefaults.DEFAULT_HOTSPOT_IP, state.hotspot_ip_address)
            assertEquals(NetworkDefaults.DEFAULT_SUBNET_MASK, state.hotspot_subnet_mask)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadHotspotConfiguration does not update state when API returns error`() = runTest {
        mockkObject(MotocamAPIAndroidHelper)
        every {
            MotocamAPIAndroidHelper.getWifiHotspotConfigAsync(any(), any())
        } answers {
            val callback = lastArg<(Map<String, Any>?, String?) -> Unit>()
            callback(null, "Load failed")
        }
        viewModel.updateHotspotSSID("BeforeLoad")
        viewModel.loadHotspotConfiguration()
        advanceUntilIdle()
        viewModel.hotspotState.test {
            assertEquals("BeforeLoad", awaitItem().hotspot_ssid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== loadWifiConfiguration Tests ==========

    @Test
    fun `loadWifiConfiguration updates state when API returns config with static IP`() = runTest {
        mockkObject(MotocamAPIAndroidHelper)
        every {
            MotocamAPIAndroidHelper.getWifiClientConfigAsync(any(), any())
        } answers {
            val callback = lastArg<(Map<String, Any>?, String?) -> Unit>()
            callback(
                mapOf(
                    "ssid" to "LoadedWiFi",
                    "encryptionkey" to "LoadedWifiPass",
                    "ipaddress" to "192.168.1.200",
                    "subnetmask" to "255.255.255.0"
                ),
                null
            )
        }
        viewModel.loadWifiConfiguration()
        advanceUntilIdle()
        viewModel.wifiState.test {
            val state = awaitItem()
            assertEquals("LoadedWiFi", state.wifi_ssid)
            assertEquals("LoadedWifiPass", state.wifi_password)
            assertEquals("192.168.1.200", state.wifi_ip_address)
            assertEquals("255.255.255.0", state.wifi_subnet_mask)
            assertFalse(state.dynamicIpEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadWifiConfiguration sets dynamicIpEnabled when API returns empty IP`() = runTest {
        mockkObject(MotocamAPIAndroidHelper)
        every {
            MotocamAPIAndroidHelper.getWifiClientConfigAsync(any(), any())
        } answers {
            val callback = lastArg<(Map<String, Any>?, String?) -> Unit>()
            callback(
                mapOf(
                    "ssid" to "DHCPNetwork",
                    "encryptionkey" to "pass",
                    "ipaddress" to "",
                    "subnetmask" to ""
                ),
                null
            )
        }
        viewModel.loadWifiConfiguration()
        advanceUntilIdle()
        viewModel.wifiState.test {
            val state = awaitItem()
            assertEquals("DHCPNetwork", state.wifi_ssid)
            assertTrue(state.dynamicIpEnabled)
            assertEquals("", state.wifi_ip_address)
            assertEquals("", state.wifi_subnet_mask)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadWifiConfiguration does not update state when API returns error`() = runTest {
        mockkObject(MotocamAPIAndroidHelper)
        every {
            MotocamAPIAndroidHelper.getWifiClientConfigAsync(any(), any())
        } answers {
            val callback = lastArg<(Map<String, Any>?, String?) -> Unit>()
            callback(null, "Load failed")
        }
        viewModel.updateWifiSSID("BeforeLoad")
        viewModel.loadWifiConfiguration()
        advanceUntilIdle()
        viewModel.wifiState.test {
            assertEquals("BeforeLoad", awaitItem().wifi_ssid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== Hour 3: Validation and connection state branches ==========

    @Test
    fun `valid IP address format is accepted`() = runTest {
        viewModel.updateHotspotIPAddress("192.168.1.1")
        viewModel.hotspotState.test {
            assertEquals("192.168.1.1", awaitItem().hotspot_ip_address)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty SSID is accepted by state`() = runTest {
        viewModel.updateWifiSSID("")
        viewModel.wifiState.test {
            assertEquals("", awaitItem().wifi_ssid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subnet mask update is reflected`() = runTest {
        viewModel.updateWifiSubnetMask("255.255.255.0")
        viewModel.wifiState.test {
            assertEquals("255.255.255.0", awaitItem().wifi_subnet_mask)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dynamic IP enabled flag can be set`() = runTest {
        viewModel.updateDynamicIpEnabled(false)
        viewModel.wifiState.test {
            assertFalse(awaitItem().dynamicIpEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dynamic IP enabled true by default`() = runTest {
        viewModel.wifiState.test {
            assertTrue(awaitItem().dynamicIpEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hotspot IP with different octets is stored`() = runTest {
        viewModel.updateHotspotIPAddress("10.0.0.1")
        viewModel.hotspotState.test {
            assertEquals("10.0.0.1", awaitItem().hotspot_ip_address)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `wifi password update is stored`() = runTest {
        viewModel.updateWifiPassword("newpass")
        viewModel.wifiState.test {
            assertEquals("newpass", awaitItem().wifi_password)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadHotspotConfiguration with error keeps previous state`() = runTest {
        mockkObject(MotocamAPIAndroidHelper)
        every {
            MotocamAPIAndroidHelper.getWifiHotspotConfigAsync(any(), any())
        } answers {
            val callback = lastArg<(Map<String, Any>?, String?) -> Unit>()
            callback(null, "error")
        }
        viewModel.updateHotspotSSID("BeforeHotspotLoad")
        viewModel.loadHotspotConfiguration()
        advanceUntilIdle()
        viewModel.hotspotState.test {
            val state = awaitItem()
            assertEquals("BeforeHotspotLoad", state.hotspot_ssid)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
