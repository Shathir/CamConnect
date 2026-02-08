package com.outdu.camconnect.utils

import android.os.Environment
import android.os.StatFs
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for StorageUtils
 * 
 * Tests cover:
 * - Storage availability checks
 * - Minimum storage requirements
 * - Internal vs external storage logic
 * 
 * Note: Uses Robolectric for Android framework APIs (StatFs, Environment)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class StorageUtilsTest {

    @Test
    fun `MIN_FREE_BYTES_FOR_RECORDING should be 5GB in SI units`() {
        // Assert - 5 * 1000 * 1000 * 1000 = 5,000,000,000 bytes = 5 GB (SI)
        val expectedBytes = 5L * 1000L * 1000L * 1000L
        assertEquals(expectedBytes, StorageUtils.MIN_FREE_BYTES_FOR_RECORDING)
    }

    @Test
    fun `getAvailableBytes should return non-negative value`() {
        // Act
        val availableBytes = StorageUtils.getAvailableBytes()

        // Assert
        assertTrue("Available bytes should be non-negative", availableBytes >= 0)
    }

    @Test
    fun `hasSufficientSpaceForRecording should return boolean`() {
        // Act
        val hasSufficientSpace = StorageUtils.hasSufficientSpaceForRecording()

        // Assert
        // Just verify it returns a boolean without throwing
        assertNotNull(hasSufficientSpace)
    }

    @Test
    fun `getAvailableBytes should be consistent with hasSufficientSpaceForRecording`() {
        // Act
        val availableBytes = StorageUtils.getAvailableBytes()
        val hasSufficient = StorageUtils.hasSufficientSpaceForRecording()

        // Assert - Logic consistency
        if (availableBytes >= StorageUtils.MIN_FREE_BYTES_FOR_RECORDING) {
            assertTrue("Should have sufficient space when bytes >= minimum", hasSufficient)
        } else {
            assertFalse("Should not have sufficient space when bytes < minimum", hasSufficient)
        }
    }

    @Test
    fun `multiple calls to getAvailableBytes should return consistent values`() {
        // Act
        val bytes1 = StorageUtils.getAvailableBytes()
        val bytes2 = StorageUtils.getAvailableBytes()

        // Assert - Values should be similar (within 10MB as system may allocate in between)
        val difference = abs(bytes1 - bytes2)
        assertTrue("Consecutive calls should return similar values",
            difference < 10 * 1000 * 1000) // 10 MB tolerance
    }

    @Test
    fun `multiple calls to hasSufficientSpaceForRecording should be consistent`() {
        // Act
        val result1 = StorageUtils.hasSufficientSpaceForRecording()
        val result2 = StorageUtils.hasSufficientSpaceForRecording()

        // Assert
        assertEquals("Consecutive calls should return same result", result1, result2)
    }

    @Test
    fun `getAvailableBytes should handle internal storage`() {
        // Act
        val availableBytes = StorageUtils.getAvailableBytes()

        // Assert - In Robolectric, may return 0, but should not throw
        assertTrue("Should return non-negative value", availableBytes >= 0)
    }

    @Test
    fun `storage check should not throw exception`() {
        // Act & Assert - Should complete without exceptions
        assertDoesNotThrow {
            StorageUtils.getAvailableBytes()
            StorageUtils.hasSufficientSpaceForRecording()
        }
    }

    // Helper function for tests
    private fun assertDoesNotThrow(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            fail("Should not throw exception: ${e.message}")
        }
    }

    private fun abs(value: Long): Long = if (value < 0) -value else value
}
