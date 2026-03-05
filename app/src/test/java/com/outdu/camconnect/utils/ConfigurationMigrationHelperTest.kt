package com.outdu.camconnect.utils

import android.content.Context
import com.outdu.camconnect.communication.CameraConfigurationManager
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
 * Unit tests for ConfigurationMigrationHelper
 * Tests migration logic from old config.properties format to CameraConfigurationManager
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ConfigurationMigrationHelperTest {

    private lateinit var context: Context
    private lateinit var oldConfigFile: File
    private lateinit var backupFile: File

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        val externalFilesDir = context.getExternalFilesDir(null) ?: context.filesDir
        oldConfigFile = File(externalFilesDir, "config.properties")
        backupFile = File(externalFilesDir, "config.properties.backup")
        
        // Clean up any existing files
        oldConfigFile.delete()
        backupFile.delete()
    }

    @After
    fun tearDown() {
        // Clean up test files
        oldConfigFile.delete()
        backupFile.delete()
    }

    @Test
    fun `test migrateIfNeeded with no old config file`() = runTest {
        // Ensure no old config exists
        assertFalse("Old config should not exist", oldConfigFile.exists())
        
        val result = runBlocking { ConfigurationMigrationHelper.migrateIfNeeded(context) }
        
        assertTrue("Migration should succeed when no old config exists", result.isSuccess)
    }

    @Test
    fun `test migrateIfNeeded with valid old config`() = runTest {
        // Ensure clean state in CameraConfigurationManager first (must complete before writing old file)
        val cleanConfig = CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = false,
            objectDetectionEnabled = false,
            depthSensingEnabled = false,
            audioEnabled = false,
            modelVersion = 0,
            depthSensingThreshold = 0.8f
        )
        runBlocking {
            val updateResult = CameraConfigurationManager.updateConfiguration(context, cleanConfig)
            assertTrue("Clean config should be applied", updateResult.isSuccess)
        }
        // Now write old-format config file (overwrites the file just saved)
        oldConfigFile.writeText("""
            far=true
            od=false
            ds=true
            audio=false
            model=2
            ds_threshold=0.7
        """.trimIndent())
        
        val result = runBlocking { ConfigurationMigrationHelper.migrateIfNeeded(context) }
        
        assertTrue("Migration should succeed", result.isSuccess)
        
        // Verify configuration was migrated
        val config = CameraConfigurationManager.getCurrentConfiguration()
        assertTrue("FAR should be enabled", config.farDetectionEnabled)
        assertFalse("OD should be disabled", config.objectDetectionEnabled)
        assertTrue("DS should be enabled", config.depthSensingEnabled)
        assertFalse("Audio should be disabled", config.audioEnabled)
        assertEquals("Model should be 2", 2, config.modelVersion)
        assertEquals("DS threshold should be 0.7", 0.7f, config.depthSensingThreshold, 0.001f)
    }

    @Test
    fun `test migrateIfNeeded skips when new config already exists`() = runTest {
        // Set up new configuration with non-default values first
        val existingConfig = CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = false,
            objectDetectionEnabled = true, // Non-default value
            depthSensingEnabled = false,
            audioEnabled = false,
            modelVersion = 0,
            depthSensingThreshold = 0.8f
        )
        runBlocking {
            assertTrue(CameraConfigurationManager.updateConfiguration(context, existingConfig).isSuccess)
        }
        // Create old config file (migration will skip due to existing config)
        oldConfigFile.writeText("far=true\nod=true")
        
        val result = runBlocking { ConfigurationMigrationHelper.migrateIfNeeded(context) }
        
        assertTrue("Migration should succeed (skipped)", result.isSuccess)
        
        // Verify old config was not migrated (OD should still be true from existing config)
        val config = CameraConfigurationManager.getCurrentConfiguration()
        assertTrue("OD should remain true (not migrated)", config.objectDetectionEnabled)
        assertFalse("FAR should remain false (not migrated)", config.farDetectionEnabled)
    }

    @Test
    fun `test migrateIfNeeded with missing properties uses defaults`() = runTest {
        // Ensure clean state first
        val cleanConfig = CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = false,
            objectDetectionEnabled = false,
            depthSensingEnabled = false,
            audioEnabled = false,
            modelVersion = 0,
            depthSensingThreshold = 0.8f
        )
        runBlocking {
            assertTrue(CameraConfigurationManager.updateConfiguration(context, cleanConfig).isSuccess)
        }
        // Create old config with only some properties
        oldConfigFile.writeText("far=true")
        
        val result = runBlocking { ConfigurationMigrationHelper.migrateIfNeeded(context) }
        
        assertTrue("Migration should succeed", result.isSuccess)
        
        val config = CameraConfigurationManager.getCurrentConfiguration()
        assertTrue("FAR should be enabled", config.farDetectionEnabled)
        assertFalse("OD should use default (false)", config.objectDetectionEnabled)
        assertFalse("DS should use default (false)", config.depthSensingEnabled)
        assertFalse("Audio should use default (false)", config.audioEnabled)
        assertEquals("Model should use default (1)", 1, config.modelVersion)
        assertEquals("DS threshold should use default (0.5)", 0.5f, config.depthSensingThreshold, 0.001f)
    }

    @Test
    fun `test migrateIfNeeded with invalid boolean values uses defaults`() = runTest {
        // Ensure clean state first
        val cleanConfig = CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = false,
            objectDetectionEnabled = false,
            depthSensingEnabled = false,
            audioEnabled = false,
            modelVersion = 0,
            depthSensingThreshold = 0.8f
        )
        runBlocking {
            assertTrue(CameraConfigurationManager.updateConfiguration(context, cleanConfig).isSuccess)
        }
        // Create old config with invalid boolean values
        oldConfigFile.writeText("""
            far=invalid
            od=yes
            ds=1
            audio=no
        """.trimIndent())
        
        val result = runBlocking { ConfigurationMigrationHelper.migrateIfNeeded(context) }
        
        assertTrue("Migration should succeed with defaults", result.isSuccess)
        
        val config = CameraConfigurationManager.getCurrentConfiguration()
        // All invalid booleans should default to false
        assertFalse("FAR should default to false", config.farDetectionEnabled)
        assertFalse("OD should default to false", config.objectDetectionEnabled)
        assertFalse("DS should default to false", config.depthSensingEnabled)
        assertFalse("Audio should default to false", config.audioEnabled)
    }

    @Test
    fun `test migrateIfNeeded with invalid numeric values uses defaults`() = runTest {
        // Ensure clean state first
        val cleanConfig = CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = false,
            objectDetectionEnabled = false,
            depthSensingEnabled = false,
            audioEnabled = false,
            modelVersion = 0,
            depthSensingThreshold = 0.8f
        )
        runBlocking {
            assertTrue(CameraConfigurationManager.updateConfiguration(context, cleanConfig).isSuccess)
        }
        // Create old config with invalid numeric values
        oldConfigFile.writeText("""
            model=invalid
            ds_threshold=not_a_number
        """.trimIndent())
        
        val result = runBlocking { ConfigurationMigrationHelper.migrateIfNeeded(context) }
        
        assertTrue("Migration should succeed with defaults", result.isSuccess)
        
        val config = CameraConfigurationManager.getCurrentConfiguration()
        assertEquals("Model should default to 1", 1, config.modelVersion)
        assertEquals("DS threshold should default to 0.5", 0.5f, config.depthSensingThreshold, 0.001f)
    }

    @Test
    fun `test migrateIfNeeded with empty config file`() = runTest {
        // Ensure clean state first
        val cleanConfig = CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = false,
            objectDetectionEnabled = false,
            depthSensingEnabled = false,
            audioEnabled = false,
            modelVersion = 0,
            depthSensingThreshold = 0.8f
        )
        runBlocking {
            assertTrue(CameraConfigurationManager.updateConfiguration(context, cleanConfig).isSuccess)
        }
        // Create empty old config file
        oldConfigFile.writeText("")
        
        val result = runBlocking { ConfigurationMigrationHelper.migrateIfNeeded(context) }
        
        assertTrue("Migration should succeed with empty file", result.isSuccess)
        
        // All values should be defaults
        val config = CameraConfigurationManager.getCurrentConfiguration()
        assertFalse("FAR should be default", config.farDetectionEnabled)
        assertFalse("OD should be default", config.objectDetectionEnabled)
        assertEquals("Model should be default", 1, config.modelVersion)
    }

    @Test
    fun `test migrateIfNeeded with all properties set`() = runTest {
        // Ensure clean state first
        val cleanConfig = CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = false,
            objectDetectionEnabled = false,
            depthSensingEnabled = false,
            audioEnabled = false,
            modelVersion = 0,
            depthSensingThreshold = 0.8f
        )
        runBlocking {
            assertTrue(CameraConfigurationManager.updateConfiguration(context, cleanConfig).isSuccess)
        }
        // Create comprehensive old config
        oldConfigFile.writeText("""
            far=true
            od=true
            ds=true
            audio=true
            model=3
            ds_threshold=0.9
        """.trimIndent())
        
        val result = runBlocking { ConfigurationMigrationHelper.migrateIfNeeded(context) }
        
        assertTrue("Migration should succeed", result.isSuccess)
        
        val config = CameraConfigurationManager.getCurrentConfiguration()
        assertTrue("FAR should be enabled", config.farDetectionEnabled)
        assertTrue("OD should be enabled", config.objectDetectionEnabled)
        assertTrue("DS should be enabled", config.depthSensingEnabled)
        assertTrue("Audio should be enabled", config.audioEnabled)
        assertEquals("Model should be 3", 3, config.modelVersion)
        assertEquals("DS threshold should be 0.9", 0.9f, config.depthSensingThreshold, 0.001f)
    }

    @Test
    fun `test migrateIfNeeded handles corrupt file gracefully`() = runTest {
        // Ensure clean state first
        val cleanConfig = CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = false,
            objectDetectionEnabled = false,
            depthSensingEnabled = false,
            audioEnabled = false,
            modelVersion = 0,
            depthSensingThreshold = 0.8f
        )
        runBlocking {
            assertTrue(CameraConfigurationManager.updateConfiguration(context, cleanConfig).isSuccess)
        }
        // Create corrupt config file (not valid properties format)
        oldConfigFile.writeText("This is not a valid properties file!@#$%")
        
        // Should not throw exception, but may fail or use defaults
        val result = runBlocking { ConfigurationMigrationHelper.migrateIfNeeded(context) }
        
        // Result should be either success (with defaults) or failure (gracefully handled)
        assertNotNull("Result should not be null", result)
    }
}
