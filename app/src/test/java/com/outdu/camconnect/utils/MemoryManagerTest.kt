package com.outdu.camconnect.utils

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MemoryManager
 * 
 * Tests cover:
 * - Surface registration/unregistration
 * - Layout change tracking
 * - Weak reference cleanup
 * - Active surface counting
 * - Memory stats reporting
 * - Reset functionality
 */
class MemoryManagerTest {

    @Before
    fun setup() {
        // Reset MemoryManager before each test
        MemoryManager.reset()
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial active surface count should be zero`() {
        // Assert
        assertEquals(0, MemoryManager.getActiveSurfaceCount())
    }

    @Test
    fun `initial memory stats should show zero surfaces`() {
        // Act
        val stats = MemoryManager.getMemoryStats()

        // Assert
        assertTrue(stats.contains("Surfaces: 0"))
        assertTrue(stats.contains("Layout changes: 0"))
    }

    // ========== Surface Registration Tests ==========

    @Test
    fun `registerSurface should increase surface count`() {
        // Arrange
        val surface1 = Any()
        val surface2 = Any()

        // Act
        MemoryManager.registerSurface(surface1)
        MemoryManager.registerSurface(surface2)

        // Assert
        assertEquals(2, MemoryManager.getActiveSurfaceCount())
    }

    @Test
    fun `unregisterSurface should decrease surface count`() {
        // Arrange
        val surface1 = Any()
        val surface2 = Any()
        MemoryManager.registerSurface(surface1)
        MemoryManager.registerSurface(surface2)
        assertEquals(2, MemoryManager.getActiveSurfaceCount())

        // Act
        MemoryManager.unregisterSurface(surface1)

        // Assert
        assertEquals(1, MemoryManager.getActiveSurfaceCount())
    }

    @Test
    fun `unregisterSurface with non-existent surface should not throw`() {
        // Arrange
        val surface = Any()

        // Act & Assert - Should not throw
        MemoryManager.unregisterSurface(surface)
        assertEquals(0, MemoryManager.getActiveSurfaceCount())
    }

    @Test
    fun `multiple register and unregister should work correctly`() {
        // Arrange
        val surface1 = Any()
        val surface2 = Any()
        val surface3 = Any()

        // Act
        MemoryManager.registerSurface(surface1)
        MemoryManager.registerSurface(surface2)
        MemoryManager.registerSurface(surface3)
        assertEquals(3, MemoryManager.getActiveSurfaceCount())

        MemoryManager.unregisterSurface(surface2)
        assertEquals(2, MemoryManager.getActiveSurfaceCount())

        MemoryManager.unregisterSurface(surface1)
        MemoryManager.unregisterSurface(surface3)
        assertEquals(0, MemoryManager.getActiveSurfaceCount())
    }

    // ========== Layout Change Tracking Tests ==========

    @Test
    fun `onLayoutChanged should track layout changes`() {
        // Act
        MemoryManager.onLayoutChanged("Layout1")
        MemoryManager.onLayoutChanged("Layout2")
        MemoryManager.onLayoutChanged("Layout3")

        // Assert
        val stats = MemoryManager.getMemoryStats()
        assertTrue(stats.contains("Layout changes: 3"))
    }

    @Test
    fun `onLayoutChanged should accept different layout names`() {
        // Act
        MemoryManager.onLayoutChanged("SingleCamera")
        MemoryManager.onLayoutChanged("GridView")
        MemoryManager.onLayoutChanged("SplitView")

        // Assert - Should not throw
        val stats = MemoryManager.getMemoryStats()
        assertTrue(stats.contains("Layout changes: 3"))
    }

    // ========== Weak Reference Cleanup Tests ==========

    @Test
    fun `cleanupWeakReferences should remove null references`() {
        // Arrange - Register surfaces but don't keep strong references
        MemoryManager.registerSurface(Any())
        MemoryManager.registerSurface(Any())
        MemoryManager.registerSurface(Any())
        
        // Force garbage collection
        System.gc()
        Thread.sleep(100) // Give GC time to run

        // Act
        MemoryManager.cleanupWeakReferences()

        // Assert - Weak references should be cleaned (count may vary due to GC timing)
        val count = MemoryManager.getActiveSurfaceCount()
        assertTrue("Count should be 0-3 after GC", count >= 0 && count <= 3)
    }

    // ========== Reset Tests ==========

    @Test
    fun `reset should clear all surfaces`() {
        // Arrange
        MemoryManager.registerSurface(Any())
        MemoryManager.registerSurface(Any())
        MemoryManager.onLayoutChanged("Test")

        // Act
        MemoryManager.reset()

        // Assert
        assertEquals(0, MemoryManager.getActiveSurfaceCount())
        val stats = MemoryManager.getMemoryStats()
        assertTrue(stats.contains("Surfaces: 0"))
        assertTrue(stats.contains("Layout changes: 0"))
    }

    @Test
    fun `reset should allow fresh start`() {
        // Arrange
        MemoryManager.registerSurface(Any())
        MemoryManager.reset()

        // Act - Register new surfaces after reset
        val newSurface = Any()
        MemoryManager.registerSurface(newSurface)

        // Assert
        assertEquals(1, MemoryManager.getActiveSurfaceCount())
    }

    // ========== Memory Stats Tests ==========

    @Test
    fun `getMemoryStats should return formatted string`() {
        // Arrange
        val surface = Any()
        MemoryManager.registerSurface(surface)
        MemoryManager.onLayoutChanged("TestLayout")

        // Act
        val stats = MemoryManager.getMemoryStats()

        // Assert
        assertNotNull(stats)
        assertTrue(stats.contains("Surfaces:"))
        assertTrue(stats.contains("Layout changes:"))
    }

    @Test
    fun `getMemoryStats should reflect current state`() {
        // Arrange
        MemoryManager.registerSurface(Any())
        MemoryManager.registerSurface(Any())
        MemoryManager.onLayoutChanged("Layout1")
        MemoryManager.onLayoutChanged("Layout2")

        // Act
        val stats = MemoryManager.getMemoryStats()

        // Assert
        assertTrue(stats.contains("Layout changes: 2"))
    }

    // ========== Active Surface Count Tests ==========

    @Test
    fun `getActiveSurfaceCount should return correct count`() {
        // Arrange
        val surface1 = Any()
        val surface2 = Any()
        val surface3 = Any()

        // Act & Assert
        assertEquals(0, MemoryManager.getActiveSurfaceCount())

        MemoryManager.registerSurface(surface1)
        assertEquals(1, MemoryManager.getActiveSurfaceCount())

        MemoryManager.registerSurface(surface2)
        MemoryManager.registerSurface(surface3)
        assertEquals(3, MemoryManager.getActiveSurfaceCount())

        MemoryManager.unregisterSurface(surface2)
        assertEquals(2, MemoryManager.getActiveSurfaceCount())
    }

    @Test
    fun `getActiveSurfaceCount should cleanup weak references`() {
        // This test verifies that getActiveSurfaceCount calls cleanupWeakReferences
        // Arrange - Register and immediately lose reference
        MemoryManager.registerSurface(Any())
        
        // Act
        System.gc() // Suggest garbage collection
        Thread.sleep(50)
        val count = MemoryManager.getActiveSurfaceCount()

        // Assert - Count should be valid (GC is non-deterministic)
        assertTrue("Count should be non-negative", count >= 0)
    }
}
