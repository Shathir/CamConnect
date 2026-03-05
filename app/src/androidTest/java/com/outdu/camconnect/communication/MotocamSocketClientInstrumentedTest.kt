package com.outdu.camconnect.communication

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

/**
 * Instrumented tests for MotocamSocketClient.
 * Covers init, IP getter/setter, parseHexStringToByteArray, checkDevice, and destroy.
 */
@RunWith(AndroidJUnit4::class)
class MotocamSocketClientInstrumentedTest {

    private val client = MotocamSocketClient()

    @After
    fun tearDown() {
        client.destroy()
    }

    @Test
    fun getCameraIp_beforeInit_returnsDefault() {
        assertEquals("192.168.2.1", client.getCameraIp())
    }

    @Test
    fun init_withNull_usesDefaultIp() {
        client.init(null)
        assertEquals("192.168.2.1", client.getCameraIp())
    }

    @Test
    fun init_withEmpty_usesDefaultIp() {
        client.init("")
        assertEquals("192.168.2.1", client.getCameraIp())
    }

    @Test
    fun init_withIp_setsCameraIp() {
        client.init("192.168.1.100")
        assertEquals("192.168.1.100", client.getCameraIp())
    }

    @Test
    fun setCameraIp_updatesIp() {
        client.init("192.168.1.1")
        client.setCameraIp("10.0.0.5")
        assertEquals("10.0.0.5", client.getCameraIp())
    }

    @Test
    fun parseHexStringToByteArray_validHex_returnsBytes() {
        val hex = "0x01 0x02 0x0A 0xFF"
        val bytes = client.parseHexStringToByteArray(hex)
        assertArrayEquals(byteArrayOf(0x01, 0x02, 0x0A, 0xFF.toByte()), bytes)
    }

    @Test
    fun parseHexStringToByteArray_singleByte_returnsOneByte() {
        val bytes = client.parseHexStringToByteArray("0xAB")
        assertArrayEquals(byteArrayOf(0xAB.toByte()), bytes)
    }

    @Test
    fun checkDevice_unreachableIp_returnsFalse() = runBlocking {
        val result = client.checkDevice("192.168.255.254")
        assertFalse(result)
    }

    @Test
    fun checkDevice_invalidHost_returnsFalse() = runBlocking {
        val result = client.checkDevice("not.a.real.host.name.xyz")
        assertFalse(result)
    }

    @Test
    fun destroy_canBeCalledMultipleTimes() {
        client.init("192.168.1.1")
        client.destroy()
        client.destroy()
    }
}
