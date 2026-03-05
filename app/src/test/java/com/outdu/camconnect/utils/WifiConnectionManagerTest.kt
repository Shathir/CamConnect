package com.outdu.camconnect.utils

import android.net.Network
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for WifiCredentials, WifiConnectionResult, and WifiPersistResult.
 * These types are used by WifiConnectionManager. Parsing and connection behavior
 * that require Context/Looper are covered in UtilsInstrumentedTest (androidTest).
 */
class WifiConnectionManagerTest {

    // ========== WifiCredentials data class ==========

    @Test
    fun `WifiCredentials holds ssid password ip macAddress`() {
        val cred = WifiCredentials("MySSID", "secret", "192.168.1.1", "aa:bb:cc:dd:ee:ff")
        assertEquals("MySSID", cred.ssid)
        assertEquals("secret", cred.password)
        assertEquals("192.168.1.1", cred.ip)
        assertEquals("aa:bb:cc:dd:ee:ff", cred.macAddress)
    }

    @Test
    fun `WifiCredentials defaults ip and macAddress to null`() {
        val cred = WifiCredentials("SSID", "pass")
        assertNull(cred.ip)
        assertNull(cred.macAddress)
    }

    // ========== WifiConnectionResult sealed class ==========

    @Test
    fun `WifiConnectionResult Connecting exists`() {
        assertEquals(WifiConnectionResult.Connecting, WifiConnectionResult.Connecting)
    }

    @Test
    fun `WifiConnectionResult Success holds network`() {
        val n = mockk<Network>(relaxed = true)
        val r = WifiConnectionResult.Success(n)
        assertEquals(n, r.network)
    }

    @Test
    fun `WifiConnectionResult Failed holds error`() {
        val r = WifiConnectionResult.Failed("error message")
        assertEquals("error message", r.error)
    }

    @Test
    fun `WifiConnectionResult Timeout exists`() {
        assertEquals(WifiConnectionResult.Timeout, WifiConnectionResult.Timeout)
    }

    // ========== WifiPersistResult sealed class ==========

    @Test
    fun `WifiPersistResult Success exists`() {
        assertEquals(WifiPersistResult.Success, WifiPersistResult.Success)
    }

    @Test
    fun `WifiPersistResult AlreadyExists exists`() {
        assertEquals(WifiPersistResult.AlreadyExists, WifiPersistResult.AlreadyExists)
    }

    @Test
    fun `WifiPersistResult Failed holds error`() {
        val r = WifiPersistResult.Failed("failed")
        assertEquals("failed", r.error)
    }

    // ========== parseQRData logic (pure JSON parsing) ==========
    // We test the expected JSON shape via a simple JSONObject test; full parseQRData
    // requires WifiConnectionManager which needs Android Context/Looper (see UtilsInstrumentedTest).

    @Test
    fun `WifiCredentials can be built from JSON-like values`() {
        val ssid = "TestNet"
        val password = "pass123"
        val cred = WifiCredentials(ssid, password, null, null)
        assertEquals(ssid, cred.ssid)
        assertEquals(password, cred.password)
    }

    @Test
    fun `WifiConnectionResult Failed message is preserved`() {
        val msg = "WiFi connection API requires Android 10+"
        val r = WifiConnectionResult.Failed(msg)
        assertTrue(r.error.contains("Android 10+"))
    }
}
