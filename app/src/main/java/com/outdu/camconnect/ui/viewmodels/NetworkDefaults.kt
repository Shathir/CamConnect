package com.outdu.camconnect.ui.viewmodels

import android.content.Context
import com.outdu.camconnect.utils.NetworkConfigManager

/**
 * Network configuration defaults.
 * 
 * This object provides centralized access to network configuration defaults.
 * Values are loaded from network_config.properties file (similar to .env files).
 * 
 * Configuration loading priority:
 * 1. External storage (user/custom config) - network_config.properties
 * 2. Assets folder (packaged defaults) - network_config.properties
 * 3. Hardcoded defaults (fallback)
 * 
 * Note: "255.255.255.0" is the standard /24 subnet mask, which is the most
 * common subnet mask for local area networks. This is a safe default value.
 */
object NetworkDefaults {
    
    /**
     * Gets the default subnet mask from configuration file.
     * Default: "255.255.255.0" (standard /24 subnet mask)
     * 
     * This is safe to use as it's a standard network configuration value
     * used by most home and office networks.
     */
    fun getDefaultSubnetMask(context: Context): String {
        return NetworkConfigManager.getSubnetMask()
    }
    
    /**
     * Gets the default hotspot IP address from configuration file.
     * Default: "192.168.2.1"
     */
    fun getDefaultHotspotIp(context: Context): String {
        return NetworkConfigManager.getHotspotIp()
    }
    
    /**
     * Gets the default WiFi IP address from configuration file.
     * Default: "192.168.1.100"
     */
    fun getDefaultWifiIp(context: Context): String {
        return NetworkConfigManager.getWifiIp()
    }
    
    /**
     * Fallback constant for cases where Context is not available or
     * before configuration is loaded. This should only be used in
     * data class default parameters.
     * 
     * Note: "255.255.255.0" is the standard /24 subnet mask.
     * This is a safe, standard network configuration value.
     */
    const val DEFAULT_SUBNET_MASK = "255.255.255.0"
    
    /**
     * Fallback constant for hotspot IP when Context is not available.
     */
    const val DEFAULT_HOTSPOT_IP = "192.168.2.1"
    
    /**
     * Fallback constant for WiFi IP when Context is not available.
     */
    const val DEFAULT_WIFI_IP = "192.168.1.100"
}

