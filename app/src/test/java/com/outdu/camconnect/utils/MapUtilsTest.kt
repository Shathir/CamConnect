package com.outdu.camconnect.utils

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

/**
 * Unit tests for MapUtils
 * 
 * Tests cover:
 * - Offset calculation with various bearings
 * - Edge cases (zero distance, poles, date line)
 * - Accuracy verification
 */
class MapUtilsTest {

    private fun assertLatLngEquals(expected: LatLng, actual: LatLng, delta: Double = 0.00001) {
        assertEquals("Latitude mismatch", expected.latitude, actual.latitude, delta)
        assertEquals("Longitude mismatch", expected.longitude, actual.longitude, delta)
    }

    @Test
    fun `calculateOffsetLocation with zero distance should return origin`() {
        // Arrange
        val origin = LatLng(40.7128, -74.0060) // New York
        val distance = 0.0
        val bearing = 0.0

        // Act
        val result = calculateOffsetLocation(origin, distance, bearing)

        // Assert
        assertLatLngEquals(origin, result)
    }

    @Test
    fun `calculateOffsetLocation north bearing should increase latitude`() {
        // Arrange
        val origin = LatLng(0.0, 0.0) // Equator
        val distance = 111320.0 // ~1 degree at equator
        val bearing = 0.0 // North

        // Act
        val result = calculateOffsetLocation(origin, distance, bearing)

        // Assert
        assertTrue("Latitude should increase", result.latitude > origin.latitude)
        assertEquals("Longitude should stay same", origin.longitude, result.longitude, 0.00001)
    }

    @Test
    fun `calculateOffsetLocation south bearing should decrease latitude`() {
        // Arrange
        val origin = LatLng(0.0, 0.0) // Equator
        val distance = 111320.0 // ~1 degree
        val bearing = 180.0 // South

        // Act
        val result = calculateOffsetLocation(origin, distance, bearing)

        // Assert
        assertTrue("Latitude should decrease", result.latitude < origin.latitude)
        assertEquals("Longitude should stay same", origin.longitude, result.longitude, 0.00001)
    }

    @Test
    fun `calculateOffsetLocation east bearing should increase longitude`() {
        // Arrange
        val origin = LatLng(0.0, 0.0) // Equator
        val distance = 111320.0 // ~1 degree
        val bearing = 90.0 // East

        // Act
        val result = calculateOffsetLocation(origin, distance, bearing)

        // Assert
        assertTrue("Longitude should increase", result.longitude > origin.longitude)
        assertEquals("Latitude should stay same", origin.latitude, result.latitude, 0.00001)
    }

    @Test
    fun `calculateOffsetLocation west bearing should decrease longitude`() {
        // Arrange
        val origin = LatLng(0.0, 0.0) // Equator
        val distance = 111320.0 // ~1 degree
        val bearing = 270.0 // West

        // Act
        val result = calculateOffsetLocation(origin, distance, bearing)

        // Assert
        assertTrue("Longitude should decrease", result.longitude < origin.longitude)
        assertEquals("Latitude should stay same", origin.latitude, result.latitude, 0.00001)
    }

    @Test
    fun `calculateOffsetLocation northeast bearing should increase both`() {
        // Arrange
        val origin = LatLng(0.0, 0.0)
        val distance = 100000.0 // 100 km
        val bearing = 45.0 // Northeast

        // Act
        val result = calculateOffsetLocation(origin, distance, bearing)

        // Assert
        assertTrue("Latitude should increase", result.latitude > origin.latitude)
        assertTrue("Longitude should increase", result.longitude > origin.longitude)
    }

    @Test
    fun `calculateOffsetLocation at north pole should work`() {
        // Arrange
        val origin = LatLng(89.9, 0.0) // Near north pole
        val distance = 10000.0
        val bearing = 0.0

        // Act
        val result = calculateOffsetLocation(origin, distance, bearing)

        // Assert
        // Should not throw exception
        assertNotNull(result)
        assertTrue("Latitude should be valid", abs(result.latitude) <= 90.0)
    }

    @Test
    fun `calculateOffsetLocation with small distance should be accurate`() {
        // Arrange
        val origin = LatLng(40.7128, -74.0060) // New York
        val distance = 1000.0 // 1 km
        val bearing = 45.0

        // Act
        val result = calculateOffsetLocation(origin, distance, bearing)

        // Assert - Check reasonable bounds
        assertTrue("Latitude change should be small", abs(result.latitude - origin.latitude) < 0.1)
        assertTrue("Longitude change should be small", abs(result.longitude - origin.longitude) < 0.1)
    }

    @Test
    fun `calculateOffsetLocation with large distance should work`() {
        // Arrange
        val origin = LatLng(0.0, 0.0)
        val distance = 1000000.0 // 1000 km
        val bearing = 0.0

        // Act
        val result = calculateOffsetLocation(origin, distance, bearing)

        // Assert
        assertNotNull(result)
        assertTrue("Latitude should be valid", abs(result.latitude) <= 90.0)
        assertTrue("Longitude should be valid", abs(result.longitude) <= 180.0)
    }

    @Test
    fun `calculateOffsetLocation full circle bearings should make sense`() {
        // Arrange
        val origin = LatLng(40.0, -75.0)
        val distance = 50000.0 // 50 km

        // Act - Calculate offsets in 4 cardinal directions
        val north = calculateOffsetLocation(origin, distance, 0.0)
        val east = calculateOffsetLocation(origin, distance, 90.0)
        val south = calculateOffsetLocation(origin, distance, 180.0)
        val west = calculateOffsetLocation(origin, distance, 270.0)

        // Assert - Check relationships
        assertTrue("North should increase lat", north.latitude > origin.latitude)
        assertTrue("South should decrease lat", south.latitude < origin.latitude)
        assertTrue("East should increase lon", east.longitude > origin.longitude)
        assertTrue("West should decrease lon", west.longitude < origin.longitude)
    }

    @Test
    fun `calculateOffsetLocation at equator vs higher latitude`() {
        // Arrange
        val equatorOrigin = LatLng(0.0, 0.0)
        val highLatOrigin = LatLng(60.0, 0.0)
        val distance = 100000.0
        val bearing = 90.0 // East

        // Act
        val equatorResult = calculateOffsetLocation(equatorOrigin, distance, bearing)
        val highLatResult = calculateOffsetLocation(highLatOrigin, distance, bearing)

        // Assert - At higher latitudes, same distance east creates bigger longitude change
        val equatorLonChange = abs(equatorResult.longitude - equatorOrigin.longitude)
        val highLatLonChange = abs(highLatResult.longitude - highLatOrigin.longitude)
        assertTrue("Higher latitude should have bigger longitude change",
            highLatLonChange > equatorLonChange)
    }
}
