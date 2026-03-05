package com.outdu.camconnect.communication

import com.outdu.camconnect.helpers.TestAuthHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

/**
 * Instrumented tests for MotocamAPIAndroidHelper.
 * Tests real camera API calls with authenticated session.
 * Tests are skipped if camera at 192.168.2.1 is not available.
 */
@RunWith(AndroidJUnit4::class)
class MotocamAPIAndroidHelperInstrumentedTest {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    @Before
    fun setup() {
        // Authenticate with real camera before each test
        // Tests will be skipped if camera is not available
        TestAuthHelper.authenticateIfNeeded()
    }
    
    @After
    fun tearDown() {
        // Keep authentication for next test (reuse session)
        TestAuthHelper.resetForNextTest()
    }

    @Test
    fun getConfigAsync_invokesCallback() = runBlocking {
        var config: Map<String, Any>? = null
        var error: String? = null
        MotocamAPIAndroidHelper.getConfigAsync(scope, "Current") { c, e ->
            config = c
            error = e
        }
        waitForCallback()
        assertTrue("Callback should be invoked with authenticated session", config != null || error != null)
    }

    // Wait for callback response from camera API
    private suspend fun waitForCallback() = delay(3000)

    @Test
    fun setIrBrightnessAsync_invokesCallback() = runBlocking {
        var success: Boolean? = null
        var error: String? = null
        MotocamAPIAndroidHelper.setIrBrightnessAsync(scope, 50) { s, e ->
            success = s
            error = e
        }
        waitForCallback()
        assertTrue("Callback should be invoked with authenticated session", success != null || error != null)
    }

    @Test
    fun setZoomAsync_invokesCallback() = runBlocking {
        var success: Boolean? = null
        var error: String? = null
        MotocamAPIAndroidHelper.setZoomAsync(scope, MotocamAPIHelper.ZOOM.X1) { s, e ->
            success = s
            error = e
        }
        waitForCallback()
        assertTrue("Callback should be invoked with authenticated session", success != null || error != null)
    }

    @Test
    fun setDayModeAsync_invokesCallback() = runBlocking {
        var success: Boolean? = null
        var error: String? = null
        MotocamAPIAndroidHelper.setDayModeAsync(scope, MotocamAPIHelper.DAYMODE.OFF) { s, e ->
            success = s
            error = e
        }
        waitForCallback()
        assertTrue("Callback should be invoked with authenticated session", success != null || error != null)
    }

    @Test
    fun setMiscAsync_invokesCallback() = runBlocking {
        var success: Boolean? = null
        var error: String? = null
        MotocamAPIAndroidHelper.setMiscAsync(scope, 1) { s, e ->
            success = s
            error = e
        }
        waitForCallback()
        assertTrue("Callback should be invoked with authenticated session", success != null || error != null)
    }

    @Test
    fun setFlipAsync_invokesCallback() = runBlocking {
        var success: Boolean? = null
        var error: String? = null
        MotocamAPIAndroidHelper.setFlipAsync(scope, MotocamAPIHelper.FLIP.OFF) { s, e ->
            success = s
            error = e
        }
        waitForCallback()
        assertTrue("Callback should be invoked with authenticated session", success != null || error != null)
    }

    @Test
    fun setMirrorAsync_invokesCallback() = runBlocking {
        var success: Boolean? = null
        var error: String? = null
        MotocamAPIAndroidHelper.setMirrorAsync(scope, MotocamAPIHelper.MIRROR.OFF) { s, e ->
            success = s
            error = e
        }
        waitForCallback()
        assertTrue("Callback should be invoked with authenticated session", success != null || error != null)
    }

    @Test
    fun getHealthStatusAsync_invokesCallback() = runBlocking {
        var status: HealthStatus? = null
        var error: String? = null
        MotocamAPIAndroidHelper.getHealthStatusAsync(scope) { s, e ->
            status = s
            error = e
        }
        waitForCallback()
        assertTrue("Callback should be invoked with authenticated session", status != null || error != null)
    }

    @Test
    fun getStreamConfigurationAsync_invokesCallback() = runBlocking {
        var config: StreamConfiguration? = null
        var error: String? = null
        MotocamAPIAndroidHelper.getStreamConfigurationAsync(scope) { c, e ->
            config = c
            error = e
        }
        waitForCallback()
        assertTrue("Callback should be invoked with authenticated session", config != null || error != null)
    }

    @Test
    fun EncoderType_fromCode_returnsCorrectEnum() {
        assertEquals(EncoderType.H264, EncoderType.fromCode(0))
        assertEquals(EncoderType.H265, EncoderType.fromCode(1))
        assertEquals(EncoderType.UNKNOWN, EncoderType.fromCode(-1))
        assertEquals(EncoderType.UNKNOWN, EncoderType.fromCode(99))
    }

    @Test
    fun ImageResolution_fromCode_returnsCorrectEnum() {
        assertEquals(ImageResolution.R640x360, ImageResolution.fromCode(1))
        assertEquals(ImageResolution.R1920x1080, ImageResolution.fromCode(3))
        assertEquals(ImageResolution.UNKNOWN, ImageResolution.fromCode(-1))
    }
}
