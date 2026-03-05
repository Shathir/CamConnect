package com.outdu.camconnect.ui.modelLabels

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for model label data (COCO, alert, boat).
 */
class ModelLabelsTest {

    @Test
    fun cocoLabels80_hasAtLeast80Entries() {
        assertTrue("COCO_LABELS80 should have at least 80 entries", COCO_LABELS80.size >= 80)
    }

    @Test
    fun cocoLabels80_hasNoDuplicateLabels() {
        val set = COCO_LABELS80.toSet()
        assertEquals("COCO_LABELS80 should have no duplicates", COCO_LABELS80.size, set.size)
    }

    @Test
    fun cocoLabels80_firstEntry_isPerson() {
        assertEquals("person", COCO_LABELS80[0])
    }

    @Test
    fun cocoLabels80_containsExpectedLabels() {
        assertTrue(COCO_LABELS80.contains("car"))
        assertTrue(COCO_LABELS80.contains("boat"))
        assertTrue(COCO_LABELS80.contains("sail_boat"))
    }

    @Test
    fun cocoLabels80_labelsHaveValidFormat_noEmptyStrings() {
        assertFalse(COCO_LABELS80.any { it.isBlank() })
    }

    @Test
    fun cocoLabels_hasEntries() {
        assertTrue(COCO_LABELS.isNotEmpty())
    }

    @Test
    fun cocoLabels_hasNoDuplicates() {
        val set = COCO_LABELS.toSet()
        assertEquals(COCO_LABELS.size, set.size)
    }

    @Test
    fun cocoLabels_containsLargeVessel() {
        assertTrue(COCO_LABELS.contains("large_vessel"))
    }

    @Test
    fun boatLabels_hasFiveEntries() {
        assertEquals(5, BOAT_LABELS.size)
    }

    @Test
    fun boatLabels_containsExpectedEntries() {
        assertTrue(BOAT_LABELS.contains("human"))
        assertTrue(BOAT_LABELS.contains("boat"))
        assertTrue(BOAT_LABELS.contains("buoy"))
        assertTrue(BOAT_LABELS.contains("light_house"))
        assertTrue(BOAT_LABELS.contains("bridge"))
    }

    @Test
    fun boatLabels_noDuplicates() {
        val set = BOAT_LABELS.toSet()
        assertEquals(BOAT_LABELS.size, set.size)
    }

    @Test
    fun alertLabels_person_containsPerson() {
        assertEquals(listOf("person"), PERSON)
    }

    @Test
    fun alertLabels_vehicle_containsExpected() {
        assertTrue(VEHICLE.contains("car"))
        assertTrue(VEHICLE.contains("truck"))
        assertTrue(VEHICLE.size >= 4)
    }

    @Test
    fun alertLabels_animals_containsExpected() {
        assertTrue(ANIMALS.contains("dog"))
        assertTrue(ANIMALS.contains("cat"))
    }

    @Test
    fun alertLabels_face_containsFace() {
        assertEquals(listOf("face"), FACE)
    }
}
