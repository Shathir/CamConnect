package com.outdu.camconnect.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.outdu.camconnect.communication.MotocamAPIAndroidHelper

data class HotspotConfiguration (
    val hotspot_ssid: String = "SaberAthena01",
    val hotspot_password: String = "1234567890",
    val hotspot_ip_address: String = NetworkDefaults.DEFAULT_HOTSPOT_IP,
    val hotspot_subnet_mask: String = NetworkDefaults.DEFAULT_SUBNET_MASK
)

data class WifiConfiguration (
    val wifi_ssid: String = "SaberAthena01",
    val wifi_password: String = "1234567890",
    val wifi_ip_address: String = NetworkDefaults.DEFAULT_WIFI_IP,
    val wifi_subnet_mask: String = NetworkDefaults.DEFAULT_SUBNET_MASK,
    val dynamicIpEnabled: Boolean = true
)

class NetworkConfigurationViewModel: ViewModel() {
    private val _hotspotState = MutableStateFlow(HotspotConfiguration())
    val hotspotState: StateFlow<HotspotConfiguration> = _hotspotState.asStateFlow()

    private val _wifiState = MutableStateFlow(WifiConfiguration())
    val wifiState: StateFlow<WifiConfiguration> = _wifiState.asStateFlow()

    fun updateHotspotSSID(ssid: String){
        _hotspotState.value = _hotspotState.value.copy(hotspot_ssid = ssid)
    }

    fun updateHotspotPassword(password: String){
        _hotspotState.value = _hotspotState.value.copy(hotspot_password = password)
    }

    fun updateHotspotIPAddress(ipAddress: String){
        _hotspotState.value = _hotspotState.value.copy(hotspot_ip_address = ipAddress)
    }

    fun updateHotspotSubnetMask(subnetMask: String){
        _hotspotState.value = _hotspotState.value.copy(hotspot_subnet_mask = subnetMask)
    }


    fun updateWifiSSID(ssid: String){
        _wifiState.value = _wifiState.value.copy(wifi_ssid = ssid)
    }

    fun updateWifiPassword(password: String){
        _wifiState.value = _wifiState.value.copy(wifi_password = password)
    }

    fun updateWifiIPAddress(ipAddress: String){
        _wifiState.value = _wifiState.value.copy(wifi_ip_address = ipAddress)
    }

    fun updateWifiSubnetMask(subnetMask: String){
        _wifiState.value = _wifiState.value.copy(wifi_subnet_mask = subnetMask)
    }

    fun updateDynamicIpEnabled(isEnabled: Boolean){
        _wifiState.value = _wifiState.value.copy(dynamicIpEnabled = isEnabled)
    }

    fun setHotspotMode(
        scope: kotlinx.coroutines.CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val config = _hotspotState.value
        
        // Validate required fields
        if (config.hotspot_ssid.isEmpty() || config.hotspot_ssid == "Enter hotspot name") {
            onError("Please enter a valid SSID")
            return
        }
        
        if (config.hotspot_password.isEmpty() || config.hotspot_password == "Enter Password") {
            onError("Please enter a password")
            return
        }

        scope.launch {
            MotocamAPIAndroidHelper.setWifiHotspotAsync(
                scope = scope,
                ssid = config.hotspot_ssid,
                encryptionType = "WPA2", // Default encryption type
                encryptionKey = config.hotspot_password,
                ipAddress = config.hotspot_ip_address,
                subnetMask = config.hotspot_subnet_mask
            ) { success, error ->
                if (success) {
                    onSuccess()
                } else {
                    onError(error ?: "Failed to start hotspot mode")
                }
            }
        }
    }

    fun setClientMode(
        scope: kotlinx.coroutines.CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val config = _wifiState.value
        
        // Validate required fields
        if (config.wifi_ssid.isEmpty()) {
            onError("Please enter a valid SSID")
            return
        }
        
        if (config.wifi_password.isEmpty()) {
            onError("Please enter a password")
            return
        }

        // For dynamic IP, use empty strings for IP and subnet
        val ipAddress = if (config.dynamicIpEnabled) "" else {
            if (config.wifi_ip_address.isEmpty()) {
                onError("Please enter an IP address")
                return
            }
            config.wifi_ip_address
        }
        
        val subnetMask = if (config.dynamicIpEnabled) "" else {
            if (config.wifi_subnet_mask.isEmpty()) {
                onError("Please enter a subnet mask")
                return
            }
            config.wifi_subnet_mask
        }

        scope.launch {
            MotocamAPIAndroidHelper.setWifiClientAsync(
                scope = scope,
                ssid = config.wifi_ssid,
                encryptionType = "WPA2", // Default encryption type
                encryptionKey = config.wifi_password,
                ipAddress = ipAddress,
                subnetMask = subnetMask
            ) { success, error ->
                if (success) {
                    onSuccess()
                } else {
                    onError(error ?: "Failed to connect to network")
                }
            }
        }
    }

    fun loadHotspotConfiguration() {
        viewModelScope.launch {
            MotocamAPIAndroidHelper.getWifiHotspotConfigAsync(viewModelScope) { config, error ->
                if (error != null) {
                    Log.e("NetworkConfigViewModel", "Error loading hotspot config: $error")
                    return@getWifiHotspotConfigAsync
                }

                config?.let { cfg ->
                    val ssid = cfg["ssid"]?.toString() ?: ""
                    val password = cfg["encryptionkey"]?.toString() ?: ""
                    val ipAddress = cfg["ipaddress"]?.toString() ?: ""
                    val subnetMask = cfg["subnetmask"]?.toString() ?: ""

                    _hotspotState.value = HotspotConfiguration(
                        hotspot_ssid = ssid.ifEmpty { "Enter hotspot name" },
                        hotspot_password = password.ifEmpty { "Enter Password" },
                        hotspot_ip_address = ipAddress.ifEmpty { NetworkDefaults.DEFAULT_HOTSPOT_IP },
                        hotspot_subnet_mask = subnetMask.ifEmpty { NetworkDefaults.DEFAULT_SUBNET_MASK }
                    )
                }
            }
        }
    }

    fun loadWifiConfiguration() {
        viewModelScope.launch {
            MotocamAPIAndroidHelper.getWifiClientConfigAsync(viewModelScope) { config, error ->
                if (error != null) {
                    Log.e("NetworkConfigViewModel", "Error loading wifi config: $error")
                    return@getWifiClientConfigAsync
                }

                config?.let { cfg ->
                    val ssid = cfg["ssid"]?.toString() ?: ""
                    val password = cfg["encryptionkey"]?.toString() ?: ""
                    val ipAddress = cfg["ipaddress"]?.toString() ?: ""
                    val subnetMask = cfg["subnetmask"]?.toString() ?: ""

                    // Determine if dynamic IP is enabled (if IP address is empty or null, dynamic IP is enabled)
                    val isDynamicIp = ipAddress.isEmpty()

                    _wifiState.value = WifiConfiguration(
                        wifi_ssid = ssid.ifEmpty { "test_Network" },
                        wifi_password = password.ifEmpty { "1234567890" },
                        wifi_ip_address = if (isDynamicIp) "" else ipAddress.ifEmpty { NetworkDefaults.DEFAULT_WIFI_IP },
                        wifi_subnet_mask = if (isDynamicIp) "" else subnetMask.ifEmpty { NetworkDefaults.DEFAULT_SUBNET_MASK },
                        dynamicIpEnabled = isDynamicIp
                    )
                }
            }
        }
    }
}