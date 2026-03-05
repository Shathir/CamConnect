package com.outdu.camconnect.utils

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ScreenRecorder utility class.
 */
class ScreenRecorderTest {

    private lateinit var context: Context
    private lateinit var projectionManager: MediaProjectionManager

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        projectionManager = mockk(relaxed = true)
        every { context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) } returns projectionManager
        every { projectionManager.createScreenCaptureIntent() } returns Intent()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getProjectionIntent returns intent from manager`() {
        val recorder = ScreenRecorder(context)
        val intent = recorder.getProjectionIntent()
        assertNotNull(intent)
    }

    @Test
    fun `setProjectionResult stores code and data`() {
        val recorder = ScreenRecorder(context)
        val data = Intent()
        recorder.setProjectionResult(android.app.Activity.RESULT_OK, data)
        // Cannot assert internal state; startRecording will use it
    }

    @Test
    fun `startRecording returns false when resultData not set`() {
        val recorder = ScreenRecorder(context)
        val result = recorder.startRecording()
        assertFalse(result)
    }
}
