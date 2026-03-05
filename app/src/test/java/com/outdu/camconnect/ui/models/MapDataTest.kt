package com.outdu.camconnect.ui.models

import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for MapType (from MapData.kt).
 */
class MapDataTest {

    @Test
    fun mapType_hasExpectedValues() {
        Assert.assertNotNull(MapType.NORMAL)
        Assert.assertNotNull(MapType.SATELLITE)
        Assert.assertNotNull(MapType.TERRAIN)
    }
}
