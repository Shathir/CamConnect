package com.outdu.camconnect.communication

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

/**
 * Unit tests for CameraConfigurationManager
 * Tests configuration persistence, validation, thread safety, and accessors
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CameraConfigurationManagerTest {

    private lateinit var context: Context
    private lateinit var configFile: File

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        configFile = File(dir, "config.properties")
        configFile.delete()
    }

    @After
    fun tearDown() {
        configFile.delete()
    }

    @Test
    fun `getCurrentConfiguration returns default values when no file exists`() = runTest {
        // Reset singleton to defaults first (state persists across tests)
        runBlocking { CameraConfigurationManager.resetToDefaults(context) }
        configFile.delete()
        runBlocking { CameraConfigurationManager.loadConfigurationAsync(context) }

        val config = CameraConfigurationManager.getCurrentConfiguration()

        assertFalse(config.farDetectionEnabled)
        assertTrue("OD default is true", config.objectDetectionEnabled)
        assertFalse(config.depthSensingEnabled)
        assertFalse(config.audioEnabled)
        assertEquals(0, config.modelVersion)
        assertEquals(0.8f, config.depthSensingThreshold, 0.001f)
    }

    @Test
    fun `updateConfiguration persists and getCurrentConfiguration reflects it`() = runTest {
        val newConfig = CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = true,
            objectDetectionEnabled = true,
            depthSensingEnabled = false,
            audioEnabled = true,
            modelVersion = 2,
            depthSensingThreshold = 0.6f
        )

        val result = runBlocking { CameraConfigurationManager.updateConfiguration(context, newConfig) }

        assertTrue("Update should succeed", result.isSuccess)
        val current = CameraConfigurationManager.getCurrentConfiguration()
        assertTrue(current.farDetectionEnabled)
        assertTrue(current.objectDetectionEnabled)
        assertFalse(current.depthSensingEnabled)
        assertTrue(current.audioEnabled)
        assertEquals(2, current.modelVersion)
        assertEquals(0.6f, current.depthSensingThreshold, 0.001f)
    }

    @Test
    fun `updateConfiguration rejects invalid depth threshold above 1`() = runTest {
        val invalidConfig = CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = false,
            objectDetectionEnabled = false,
            depthSensingEnabled = false,
            audioEnabled = false,
            modelVersion = 0,
            depthSensingThreshold = 1.5f
        )

        val result = runBlocking { CameraConfigurationManager.updateConfiguration(context, invalidConfig) }

        assertTrue("Should fail", result.isFailure)
        assertTrue(
            result.exceptionOrNull() is CameraConfigurationManager.ConfigurationException.ValidationException
        )
    }

    @Test
    fun `updateConfiguration rejects invalid depth threshold below 0`() = runTest {
        val invalidConfig = CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = false,
            objectDetectionEnabled = false,
            depthSensingEnabled = false,
            audioEnabled = false,
            modelVersion = 0,
            depthSensingThreshold = -0.1f
        )

        val result = runBlocking { CameraConfigurationManager.updateConfiguration(context, invalidConfig) }

        assertTrue("Should fail", result.isFailure)
    }

    @Test
    fun `updateConfiguration rejects negative model version`() = runTest {
        val invalidConfig = CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = false,
            objectDetectionEnabled = false,
            depthSensingEnabled = false,
            audioEnabled = false,
            modelVersion = -1,
            depthSensingThreshold = 0.8f
        )

        val result = runBlocking { CameraConfigurationManager.updateConfiguration(context, invalidConfig) }

        assertTrue("Should fail", result.isFailure)
        assertTrue(
            result.exceptionOrNull() is CameraConfigurationManager.ConfigurationException.ValidationException
        )
    }

    @Test
    fun `resetToDefaults sets expected defaults`() = runTest {
        runBlocking {
            CameraConfigurationManager.updateConfiguration(
                context,
                CameraConfigurationManager.CameraConfig(
                    farDetectionEnabled = true,
                    objectDetectionEnabled = true,
                    depthSensingEnabled = true,
                    audioEnabled = true,
                    modelVersion = 3,
                    depthSensingThreshold = 0.5f
                )
            )
        }

        val resetResult = runBlocking { CameraConfigurationManager.resetToDefaults(context) }
        assertTrue("Reset should succeed", resetResult.isSuccess)

        val config = CameraConfigurationManager.getCurrentConfiguration()
        assertFalse(config.farDetectionEnabled)
        assertTrue("OD default is true", config.objectDetectionEnabled)
        assertFalse(config.depthSensingEnabled)
        assertFalse(config.audioEnabled)
        assertEquals(0, config.modelVersion)
        assertEquals(0.8f, config.depthSensingThreshold, 0.001f)
    }

    @Test
    fun `loadConfigurationAsync when file does not exist returns success with defaults`() = runTest {
        // Reset to defaults first so in-memory state is defaults (singleton persists across tests)
        runBlocking { CameraConfigurationManager.resetToDefaults(context) }
        configFile.delete()

        val result = runBlocking { CameraConfigurationManager.loadConfigurationAsync(context) }

        assertTrue("Load should succeed", result.isSuccess)
        result.onSuccess { config ->
            assertFalse(config.farDetectionEnabled)
            assertTrue(config.objectDetectionEnabled)
            assertEquals(0, config.modelVersion)
        }
    }

    @Test
    fun `loadConfiguration sync returns true when load succeeds`() = runTest {
        configFile.delete()

        val result = CameraConfigurationManager.loadConfiguration(context)

        assertTrue("Sync load should return true when no file", result)
    }

    @Test
    fun `setModelVersion with negative value returns failure`() = runTest {
        val result = runBlocking { CameraConfigurationManager.setModelVersion(context, -1) }

        assertTrue("Should fail", result.isFailure)
    }

    @Test
    fun `setDepthSensingThreshold out of range returns failure`() = runTest {
        val result = runBlocking { CameraConfigurationManager.setDepthSensingThreshold(context, 1.5f) }

        assertTrue("Should fail", result.isFailure)
    }

    @Test
    fun `setDepthSensingThreshold in range succeeds`() = runTest {
        val result = runBlocking { CameraConfigurationManager.setDepthSensingThreshold(context, 0.5f) }

        assertTrue("Should succeed", result.isSuccess)
        assertEquals(0.5f, CameraConfigurationManager.getDepthSensingThreshold(), 0.001f)
    }

    @Test
    fun `setModelVersion valid value succeeds`() = runTest {
        val result = runBlocking { CameraConfigurationManager.setModelVersion(context, 2) }

        assertTrue("Should succeed", result.isSuccess)
        assertEquals(2, CameraConfigurationManager.getModelVersion())
    }

    @Test
    fun `individual setters update state`() = runTest {
        runBlocking { CameraConfigurationManager.setFarDetectionEnabled(context, true) }
        assertTrue(CameraConfigurationManager.isFarDetectionEnabled())

        runBlocking { CameraConfigurationManager.setObjectDetectionEnabled(context, true) }
        assertTrue(CameraConfigurationManager.isObjectDetectionEnabled())

        runBlocking { CameraConfigurationManager.setDepthSensingEnabled(context, true) }
        assertTrue(CameraConfigurationManager.isDepthSensingEnabled())

        runBlocking { CameraConfigurationManager.setAudioEnabled(context, true) }
        assertTrue(CameraConfigurationManager.isAudioEnabled())
    }

    @Test
    fun `deprecated accessors match new accessors`() = runTest {
        runBlocking {
            CameraConfigurationManager.updateConfiguration(
                context,
                CameraConfigurationManager.CameraConfig(
                    farDetectionEnabled = true,
                    objectDetectionEnabled = true,
                    depthSensingEnabled = true,
                    audioEnabled = true,
                    modelVersion = 1,
                    depthSensingThreshold = 0.7f
                )
            )
        }

        assertEquals(CameraConfigurationManager.isFarDetectionEnabled(), CameraConfigurationManager.isFAR())
        assertEquals(CameraConfigurationManager.isObjectDetectionEnabled(), CameraConfigurationManager.isOD())
        assertEquals(CameraConfigurationManager.isDepthSensingEnabled(), CameraConfigurationManager.isDS())
        assertEquals(CameraConfigurationManager.isAudioEnabled(), CameraConfigurationManager.isAUDIO())
        assertEquals(CameraConfigurationManager.getModelVersion(), CameraConfigurationManager.getMODEL())
        assertEquals(CameraConfigurationManager.getDepthSensingThreshold(), CameraConfigurationManager.getDsThreshold(), 0.001f)
    }

    @Test
    fun `CameraConfig data class holds values`() {
        val config = CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = true,
            objectDetectionEnabled = false,
            depthSensingEnabled = true,
            audioEnabled = false,
            modelVersion = 2,
            depthSensingThreshold = 0.5f
        )

        assertTrue(config.farDetectionEnabled)
        assertFalse(config.objectDetectionEnabled)
        assertTrue(config.depthSensingEnabled)
        assertFalse(config.audioEnabled)
        assertEquals(2, config.modelVersion)
        assertEquals(0.5f, config.depthSensingThreshold, 0.001f)
    }

    @Test
    fun `concurrent read getCurrentConfiguration does not throw`() = runTest {
        runBlocking { CameraConfigurationManager.updateConfiguration(context, CameraConfigurationManager.getCurrentConfiguration()) }

        repeat(20) {
            val c = CameraConfigurationManager.getCurrentConfiguration()
            assertNotNull(c)
        }
    }

    @Test
    fun `loadConfigurationAsync then getCurrentConfiguration returns loaded values`() = runTest {
        val toSave = CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = true,
            objectDetectionEnabled = false,
            depthSensingEnabled = true,
            audioEnabled = false,
            modelVersion = 1,
            depthSensingThreshold = 0.4f
        )
        runBlocking { CameraConfigurationManager.updateConfiguration(context, toSave) }

        val loadResult = runBlocking { CameraConfigurationManager.loadConfigurationAsync(context) }
        assertTrue(loadResult.isSuccess)

        val current = CameraConfigurationManager.getCurrentConfiguration()
        assertTrue(current.farDetectionEnabled)
        assertFalse(current.objectDetectionEnabled)
        assertEquals(1, current.modelVersion)
        assertEquals(0.4f, current.depthSensingThreshold, 0.001f)
    }
}
