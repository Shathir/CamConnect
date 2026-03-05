package com.outdu.camconnect.communication

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for MotocamAPIAndroidHelper types: HealthStatus, EncoderType, ImageResolution, StreamInfo, StreamConfiguration.
 * Async API methods require MotocamAPIHelperWrapper and are covered in instrumented/wrapper tests.
 */
class MotocamAPIAndroidHelperTest {

    // ========== HealthStatus ==========

    @Test
    fun healthStatus_holdsAllFields() {
        val h = HealthStatus(
            streamer = true,
            rtsps = true,
            portableRtc = false,
            cpuUsage = 50,
            memoryUsage = 60,
            ispTemp = 45,
            irTemp = 40,
            sensorTemp = 42
        )
        assertTrue(h.streamer)
        assertTrue(h.rtsps)
        assertFalse(h.portableRtc)
        assertEquals(50, h.cpuUsage)
        assertEquals(60, h.memoryUsage)
        assertEquals(45, h.ispTemp)
        assertEquals(40, h.irTemp)
        assertEquals(42, h.sensorTemp)
    }

    @Test
    fun healthStatus_sensorTempDefaultsToMinusOne() {
        val h = HealthStatus(
            streamer = false,
            rtsps = false,
            portableRtc = false,
            cpuUsage = 0,
            memoryUsage = 0,
            ispTemp = 0,
            irTemp = 0
        )
        assertEquals(-1, h.sensorTemp)
    }

    // ========== EncoderType ==========

    @Test
    fun encoderType_H264() {
        assertEquals(0, EncoderType.H264.code)
        assertEquals("H264", EncoderType.H264.displayName)
        assertEquals(EncoderType.H264, EncoderType.fromCode(0))
    }

    @Test
    fun encoderType_H265() {
        assertEquals(1, EncoderType.H265.code)
        assertEquals("H265", EncoderType.H265.displayName)
        assertEquals(EncoderType.H265, EncoderType.fromCode(1))
    }

    @Test
    fun encoderType_fromCode_unknownForInvalid() {
        assertEquals(EncoderType.UNKNOWN, EncoderType.fromCode(-1))
        assertEquals(EncoderType.UNKNOWN, EncoderType.fromCode(2))
        assertEquals(EncoderType.UNKNOWN, EncoderType.fromCode(100))
    }

    @Test
    fun encoderType_UNKNOWN() {
        assertEquals(-1, EncoderType.UNKNOWN.code)
        assertEquals("Unknown", EncoderType.UNKNOWN.displayName)
    }

    // ========== ImageResolution ==========

    @Test
    fun imageResolution_codesAndNames() {
        assertEquals(1, ImageResolution.R640x360.code)
        assertEquals("640x360", ImageResolution.R640x360.displayName)
        assertEquals(2, ImageResolution.R1280x720.code)
        assertEquals("1280x720", ImageResolution.R1280x720.displayName)
        assertEquals(3, ImageResolution.R1920x1080.code)
        assertEquals("1920x1080", ImageResolution.R1920x1080.displayName)
        assertEquals(4, ImageResolution.R3840x2160.code)
        assertEquals("3840x2160", ImageResolution.R3840x2160.displayName)
    }

    @Test
    fun imageResolution_fromCode() {
        assertEquals(ImageResolution.R640x360, ImageResolution.fromCode(1))
        assertEquals(ImageResolution.R1280x720, ImageResolution.fromCode(2))
        assertEquals(ImageResolution.R1920x1080, ImageResolution.fromCode(3))
        assertEquals(ImageResolution.R3840x2160, ImageResolution.fromCode(4))
    }

    @Test
    fun imageResolution_fromCode_unknownForInvalid() {
        assertEquals(ImageResolution.UNKNOWN, ImageResolution.fromCode(0))
        assertEquals(ImageResolution.UNKNOWN, ImageResolution.fromCode(5))
        assertEquals(ImageResolution.UNKNOWN, ImageResolution.fromCode(-1))
    }

    @Test
    fun imageResolution_UNKNOWN() {
        assertEquals(-1, ImageResolution.UNKNOWN.code)
        assertEquals("Unknown", ImageResolution.UNKNOWN.displayName)
    }

    // ========== StreamInfo ==========

    @Test
    fun streamInfo_holdsFields() {
        val info = StreamInfo(
            resolution = ImageResolution.R1920x1080,
            fps = 30,
            bitrate = 4_000_000,
            encoder = EncoderType.H264
        )
        assertEquals(ImageResolution.R1920x1080, info.resolution)
        assertEquals(30, info.fps)
        assertEquals(4_000_000, info.bitrate)
        assertEquals(EncoderType.H264, info.encoder)
    }

    // ========== StreamConfiguration ==========

    @Test
    fun streamConfiguration_emptyList() {
        val config = StreamConfiguration(streams = emptyList())
        assertTrue(config.streams.isEmpty())
    }

    @Test
    fun streamConfiguration_multipleStreams() {
        val streams = listOf(
            StreamInfo(ImageResolution.R640x360, 15, 1_000_000, EncoderType.H264),
            StreamInfo(ImageResolution.R1920x1080, 30, 4_000_000, EncoderType.H265)
        )
        val config = StreamConfiguration(streams = streams)
        assertEquals(2, config.streams.size)
        assertEquals(ImageResolution.R640x360, config.streams[0].resolution)
        assertEquals(ImageResolution.R1920x1080, config.streams[1].resolution)
    }
}
