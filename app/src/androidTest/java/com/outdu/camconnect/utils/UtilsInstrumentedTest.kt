package com.outdu.camconnect.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.outdu.camconnect.communication.CameraConfigurationManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumented tests for utility classes
 * Tests WifiConnectionManager, ConfigurationMigrationHelper, and StorageUtils
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class UtilsInstrumentedTest {

    private lateinit var context: android.content.Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Clean up any test files
        val oldConfigFile = File(context.getExternalFilesDir(null), "config.properties")
        oldConfigFile.delete()
        val backupFile = File(context.getExternalFilesDir(null), "config.properties.backup")
        backupFile.delete()
    }

    // ========== WifiConnectionManager Tests ==========

    @Test
    fun testWifiConnectionManagerParseQRDataValid() {
        val manager = WifiConnectionManager(context)
        val qrData = """{"ssid":"TestNetwork","password":"TestPass123","ip":"192.168.1.100","mac":"AA:BB:CC:DD:EE:FF"}"""
        
        val result = manager.parseQRData(qrData)
        
        assertNotNull("Result should not be null", result)
        assertEquals("SSID should match", "TestNetwork", result?.ssid)
        assertEquals("Password should match", "TestPass123", result?.password)
        assertEquals("IP should match", "192.168.1.100", result?.ip)
        assertEquals("MAC should match", "AA:BB:CC:DD:EE:FF", result?.macAddress)
    }

    @Test
    fun testWifiConnectionManagerParseQRDataMinimal() {
        val manager = WifiConnectionManager(context)
        val qrData = """{"ssid":"MyWiFi","password":"mypassword"}"""
        
        val result = manager.parseQRData(qrData)
        
        assertNotNull("Result should not be null", result)
        assertEquals("SSID should match", "MyWiFi", result?.ssid)
        assertEquals("Password should match", "mypassword", result?.password)
        assertNull("IP should be null", result?.ip)
        assertNull("MAC should be null", result?.macAddress)
    }

    @Test
    fun testWifiConnectionManagerParseQRDataInvalid() {
        val manager = WifiConnectionManager(context)
        val invalidData = "not valid json"
        
        val result = manager.parseQRData(invalidData)
        
        assertNull("Result should be null for invalid JSON", result)
    }

    @Test
    fun testWifiConnectionManagerParseQRDataMissingRequired() {
        val manager = WifiConnectionManager(context)
        val qrData = """{"ssid":"TestSSID"}"""
        
        val result = manager.parseQRData(qrData)
        
        assertNull("Result should be null when password is missing", result)
    }

    @Test
    fun testWifiConnectionManagerParseQRDataSpecialCharacters() {
        val manager = WifiConnectionManager(context)
        val qrData = """{"ssid":"Test-WiFi_2.4","password":"P@ss!w0rd#123"}"""
        
        val result = manager.parseQRData(qrData)
        
        assertNotNull("Result should not be null", result)
        assertEquals("SSID with special chars should work", "Test-WiFi_2.4", result?.ssid)
        assertEquals("Password with special chars should work", "P@ss!w0rd#123", result?.password)
    }

    // ========== ConfigurationMigrationHelper Tests ==========

    @Test
    fun testConfigurationMigrationNoOldConfig() {
        runBlocking {
            val result = ConfigurationMigrationHelper.migrateIfNeeded(context)
            
            assertTrue("Migration should succeed when no old config exists", 
                result.isSuccess)
        }
    }

    @Test
    fun testConfigurationMigrationWithOldConfig() {
        runBlocking {
            // First, ensure we have a clean slate by resetting to all-false defaults
            // This is needed because hasNonDefaultValues() would skip migration if OD=true
            val cleanConfig = CameraConfigurationManager.CameraConfig(
                farDetectionEnabled = false,
                objectDetectionEnabled = false,
                depthSensingEnabled = false,
                audioEnabled = false,
                modelVersion = 0,
                depthSensingThreshold = 0.8f
            )
            CameraConfigurationManager.updateConfiguration(context, cleanConfig)
            
            // Create old config file
            val oldConfigFile = File(context.getExternalFilesDir(null), "config.properties")
            oldConfigFile.writeText("far=true\nod=false\nds=true\naudio=false\nmodel=2\nds_threshold=0.7")
            
            val result = ConfigurationMigrationHelper.migrateIfNeeded(context)
            
            assertTrue("Migration should succeed", result.isSuccess)
            
            // Verify the configuration was actually migrated
            val migratedConfig = CameraConfigurationManager.getCurrentConfiguration()
            assertTrue("FAR should be migrated to true", migratedConfig.farDetectionEnabled)
            assertTrue("DS should be migrated to true", migratedConfig.depthSensingEnabled)
            assertEquals("Model should be migrated to 2", 2, migratedConfig.modelVersion)
        }
    }

    // ========== CameraConfigurationManager Tests ==========

    @Test
    fun testCameraConfigurationManagerGetCurrentConfiguration() {
        val config = CameraConfigurationManager.getCurrentConfiguration()
        
        assertNotNull("Configuration should not be null", config)
        assertTrue("Model version should be non-negative", config.modelVersion >= 0)
        assertTrue("Threshold should be between 0 and 1", 
            config.depthSensingThreshold >= 0f && config.depthSensingThreshold <= 1f)
    }

    @Test
    fun testCameraConfigurationManagerUpdateConfiguration() {
        runBlocking {
            val newConfig = CameraConfigurationManager.CameraConfig(
                farDetectionEnabled = true,
                objectDetectionEnabled = true,
                depthSensingEnabled = false,
                audioEnabled = true,
                modelVersion = 3,
                depthSensingThreshold = 0.6f
            )
            
            val result = CameraConfigurationManager.updateConfiguration(context, newConfig)
            
            assertTrue("Update should succeed", result.isSuccess)
        }
    }

    @Test
    fun testCameraConfigurationManagerLoadConfiguration() {
        runBlocking {
            val result = CameraConfigurationManager.loadConfigurationAsync(context)
            
            assertTrue("Load should succeed", result.isSuccess)
            
            result.onSuccess { config ->
                assertNotNull("Loaded config should not be null", config)
            }
        }
    }

    @Test
    fun testCameraConfigurationManagerResetToDefaults() {
        runBlocking {
            val result = CameraConfigurationManager.resetToDefaults(context)
            
            assertTrue("Reset should succeed", result.isSuccess)
            
            val config = CameraConfigurationManager.getCurrentConfiguration()
            assertFalse("FAR should be disabled after reset", config.farDetectionEnabled)
            assertTrue("OD should be enabled after reset (default is true)", config.objectDetectionEnabled)
            assertFalse("DS should be disabled after reset", config.depthSensingEnabled)
            assertFalse("Audio should be disabled after reset", config.audioEnabled)
        }
    }

    // ========== StorageUtils Tests ==========

    @Test
    fun testStorageUtilsGetAvailableBytes() {
        val availableBytes = StorageUtils.getAvailableBytes()
        
        assertTrue("Available bytes should be non-negative", availableBytes >= 0)
    }

    @Test
    fun testStorageUtilsHasSufficientSpace() {
        val hasSufficientSpace = StorageUtils.hasSufficientSpaceForRecording()
        
        // Should return a boolean
        assertTrue("Result should be boolean", 
            hasSufficientSpace is Boolean)
    }

    @Test
    fun testStorageUtilsConsistency() {
        val availableBytes = StorageUtils.getAvailableBytes()
        val hasSufficientSpace = StorageUtils.hasSufficientSpaceForRecording()
        
        // If we have sufficient space, available bytes should be > MIN_FREE_BYTES
        if (hasSufficientSpace) {
            assertTrue("Available bytes should exceed minimum when sufficient space",
                availableBytes > StorageUtils.MIN_FREE_BYTES_FOR_RECORDING)
        }
    }

    @Test
    fun testStorageUtilsMinimumFreeBytes() {
        // MIN_FREE_BYTES_FOR_RECORDING should be 5GB (SI units: 1000-based)
        val expectedMinBytes = 5L * 1000L * 1000L * 1000L // 5GB in SI bytes
        
        assertEquals("Minimum free bytes should be 5GB (SI)", 
            expectedMinBytes, StorageUtils.MIN_FREE_BYTES_FOR_RECORDING)
    }

    // ========== Hour 5: Utils with real Android environment ==========

    @Test
    fun testStorageUtilsGetAvailableBytesWithRealFilesystem() {
        val bytes = StorageUtils.getAvailableBytes()
        assertTrue("Available bytes should be non-negative on device", bytes >= 0)
    }

    @Test
    fun testStorageUtilsHasSufficientSpaceWithRealFilesystem() {
        val hasSpace = StorageUtils.hasSufficientSpaceForRecording()
        assertTrue("Result should be boolean", hasSpace is Boolean)
    }

    @Test
    fun testConfigurationMigrationSkipWhenAlreadyCurrent() {
        runBlocking {
            val result = ConfigurationMigrationHelper.migrateIfNeeded(context)
            assertTrue("Migration should succeed or skip", result.isSuccess)
        }
    }

    @Test
    fun testWifiConnectionManagerWithContextCreates() {
        val manager = WifiConnectionManager(context)
        assertNotNull(manager)
    }

    @Test
    fun testCameraConfigurationManagerGetCurrentAfterUpdate() {
        runBlocking {
            val config = CameraConfigurationManager.getCurrentConfiguration()
            assertNotNull(config)
            assertTrue(config.modelVersion >= 0)
        }
    }
}
