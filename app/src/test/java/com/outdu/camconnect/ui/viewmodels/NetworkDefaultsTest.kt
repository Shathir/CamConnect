package com.outdu.camconnect.ui.viewmodels

import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for NetworkDefaults constants.
 */
class NetworkDefaultsTest {

    @Test
    fun defaultSubnetMask_constant() {
        Assert.assertEquals("255.255.255.0", NetworkDefaults.DEFAULT_SUBNET_MASK)
    }

    @Test
    fun defaultHotspotIp_constant() {
        Assert.assertEquals("192.168.2.1", NetworkDefaults.DEFAULT_HOTSPOT_IP)
    }

    @Test
    fun defaultWifiIp_constant() {
        Assert.assertEquals("192.168.1.100", NetworkDefaults.DEFAULT_WIFI_IP)
    }
}
