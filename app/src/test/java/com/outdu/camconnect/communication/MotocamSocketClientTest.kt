package com.outdu.camconnect.communication

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MotocamSocketClientTest {

    @Test
    fun sendCmd_success() = runTest {
        // Mock response for a simple command (e.g., set brightness)
        // Request: [0x55, 0x03, 0x01, 0x04, 0x32, 0x00, 0x00, 0x00, CRC]
        // Response: [0x55, 0x03, 0x01, 0x01, 0x01, CRC] (Sample success response)
        
        // CRC for [0x55, 0x03, 0x01, 0x01, 0x01]
        // Sum = 0x55 + 0x03 + 0x01 + 0x01 + 0x01 = 0x5B
        // XOR FF = 0xA4, +1 = 0xA5
        val mockResponseHex = "0x55 0x03 0x01 0x01 0x01 0xA5"

        val mockEngine = MockEngine { request ->
            respond(
                content = mockResponseHex,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }

        val client = MotocamSocketClient(mockEngine)
        client.init("192.168.2.1")

        val reqCmd = intArrayOf(0x55, 0x03, 0x01, 0x04, 0x32, 0x00, 0x00, 0x00, 0x00) // Last 0x00 is placeholder for CRC
        val res = IntArray(255)
        
        val len = client.sendCmd(reqCmd, res)
        
        // Expecting length 6
        assertEquals(6, len)
        // Verify response content
        assertEquals(0x55, res[0])
        assertEquals(0x03, res[1])
        assertEquals(0xA5, res[5])
    }

    @Test(expected = Exception::class)
    fun sendCmd_invalidCrc() = runTest {
        // Response with INVALID CRC
        // Correct CRC is 0xA5, but we send 0x00
        val mockResponseHex = "0x55 0x03 0x01 0x01 0x01 0x00"

        val mockEngine = MockEngine { request ->
            respond(
                content = mockResponseHex,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }

        val client = MotocamSocketClient(mockEngine)
        client.init()

        val reqCmd = intArrayOf(0x55, 0x03, 0x00) // Dummy
        val res = IntArray(255)
        
        client.sendCmd(reqCmd, res) // Should throw Exception
    }

    @Test
    fun uploadFile_success() = runTest {
        val mockEngine = MockEngine { request ->
            // Check request details (MockEngine doesn't include default port :80 in URL)
            assertTrue(request.url.toString().startsWith("http://192.168.2.1"))
            
            respond(
                content = "Upload Success",
                status = HttpStatusCode.OK
            )
        }

        val client = MotocamSocketClient(mockEngine)
        client.init("192.168.2.1")

        val fileBytes = "Test Content".toByteArray()
        val result = client.uploadFile("test.txt", fileBytes)

        assertTrue(result)
    }
}
