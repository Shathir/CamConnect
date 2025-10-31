package com.outdu.camconnect.ui.viewmodels

import android.net.Network
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


data class HotspotConfiguration (
    val hotspot_ssid: String = "Enter hotspot name",
    val hotspot_password: String = "Enter Password",
    val hotspot_ip_address: String = "192.168.2.1",
    val hotspot_subnet_mask: String = "255.255.255.0"
)

data class WifiConfiguration (
    val wifi_ssid: String = "test_Network",
    val wifi_password: String = "1234567890",
    val wifi_ip_address: String = "192.168.1.100",
    val wifi_subnet_mask: String = "255.255.255.0"
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

    fun updateWfiIPAddress(ipAddress: String){
        _wifiState.value = _wifiState.value.copy(wifi_ip_address = ipAddress)
    }

    fun updateWifiSubnetMask(subnetMask: String){
        _wifiState.value = _wifiState.value.copy(wifi_subnet_mask = subnetMask)
    }
}