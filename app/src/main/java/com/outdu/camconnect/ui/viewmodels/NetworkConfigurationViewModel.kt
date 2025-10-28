package com.outdu.camconnect.ui.viewmodels

import androidx.lifecycle.ViewModel


data class NetworkConfiguration (
    val hotspot_ssid: String,
    val hotspot_password: String,
    val ip_address: String,
    val subnet_mask: String
)

class NetworkConfigurationViewModel: ViewModel() {

}