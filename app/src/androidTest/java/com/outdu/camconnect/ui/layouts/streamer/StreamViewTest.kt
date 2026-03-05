package com.outdu.camconnect.ui.layouts.streamer

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for streamView composable and related types.
 * Tests AiRegionConfig and AiRegionBounds (no native surface required).
 */
@RunWith(AndroidJUnit4::class)
class StreamViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aiRegionConfig_calculateStreamBounds_returnsCorrectBounds() {
        val config = AiRegionConfig(
            streamWidth = 1280f,
            streamHeight = 720f,
            aiRegionWidth = 720f,
            aiRegionHeight = 720f
        )
        val bounds = config.calculateStreamBounds()

        assertEquals(280f, bounds.left, 0.01f)
        assertEquals(1000f, bounds.right, 0.01f)
        assertEquals(0f, bounds.top, 0.01f)
        assertEquals(720f, bounds.bottom, 0.01f)
    }

    @Test
    fun aiRegionConfig_defaultValues() {
        val config = AiRegionConfig()

        assertEquals(1280f, config.streamWidth, 0.01f)
        assertEquals(720f, config.streamHeight, 0.01f)
        assertEquals(720f, config.aiRegionWidth, 0.01f)
        assertEquals(720f, config.aiRegionHeight, 0.01f)
    }

    @Test
    fun aiRegionBounds_holdsValues() {
        val bounds = AiRegionBounds(
            left = 0f,
            right = 100f,
            top = 0f,
            bottom = 100f
        )

        assertEquals(0f, bounds.left, 0.01f)
        assertEquals(100f, bounds.right, 0.01f)
        assertEquals(0f, bounds.top, 0.01f)
        assertEquals(100f, bounds.bottom, 0.01f)
    }

    // ========== Hour 6: Stream state and AI region branches ==========

    @Test
    fun aiRegionConfig_calculateStreamBounds_withDifferentDimensions() {
        val config = AiRegionConfig(
            streamWidth = 1920f,
            streamHeight = 1080f,
            aiRegionWidth = 1080f,
            aiRegionHeight = 1080f
        )
        val bounds = config.calculateStreamBounds()
        assertTrue(bounds.left >= 0f)
        assertTrue(bounds.right <= 1920f)
        assertTrue(bounds.top >= 0f)
        assertTrue(bounds.bottom <= 1080f)
    }

    @Test
    fun aiRegionBounds_zeroBounds() {
        val bounds = AiRegionBounds(0f, 0f, 0f, 0f)
        assertEquals(0f, bounds.left, 0.01f)
        assertEquals(0f, bounds.right, 0.01f)
    }

    @Test
    fun aiRegionConfig_streamBoundsConsistency() {
        val config = AiRegionConfig(
            streamWidth = 640f,
            streamHeight = 480f,
            aiRegionWidth = 480f,
            aiRegionHeight = 480f
        )
        val bounds = config.calculateStreamBounds()
        assertTrue(bounds.right - bounds.left <= 640f)
        assertTrue(bounds.bottom - bounds.top <= 480f)
    }

    @Test
    fun aiRegionBounds_largeValues() {
        val bounds = AiRegionBounds(100f, 1000f, 50f, 800f)
        assertEquals(100f, bounds.left, 0.01f)
        assertEquals(1000f, bounds.right, 0.01f)
        assertEquals(50f, bounds.top, 0.01f)
        assertEquals(800f, bounds.bottom, 0.01f)
    }
}
