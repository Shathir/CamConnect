package com.outdu.camconnect.utils

import android.util.Log
import com.outdu.camconnect.singleton.MainActivitySingleton
import java.lang.ref.WeakReference

/**
 * Utility class to help manage native memory and prevent leaks
 */
object MemoryManager {
    private const val TAG = "MemoryManager"
    
    // Track active surface references
    private val activeSurfaces = mutableSetOf<WeakReference<Any>>()
    
    // Track layout changes for memory monitoring
    private var layoutChangeCount = 0
    private var lastCleanupTime = System.currentTimeMillis()
    
    /**
     * Register a surface for tracking
     */
    fun registerSurface(surface: Any) {
        activeSurfaces.add(WeakReference(surface))
        Log.d(TAG, "Registered surface: ${surface.hashCode()} (Total: ${activeSurfaces.size})")
    }
    
    /**
     * Unregister a surface
     */
    fun unregisterSurface(surface: Any) {
        activeSurfaces.removeAll { it.get() == surface || it.get() == null }
        Log.d(TAG, "Unregistered surface: ${surface.hashCode()} (Total: ${activeSurfaces.size})")
    }
    
    /**
     * Track layout changes
     */
    fun onLayoutChanged(layoutName: String) {
        layoutChangeCount++
        Log.d(TAG, "Layout changed to: $layoutName (Change #$layoutChangeCount)")
        
        // Cleanup every 5 layout changes or every 30 seconds
        val currentTime = System.currentTimeMillis()
        if (layoutChangeCount % 5 == 0 || (currentTime - lastCleanupTime) > 30000) {
            cleanupWeakReferences()
            lastCleanupTime = currentTime
        }
    }
    
    /**
     * Force cleanup of all native resources
     */
    fun forceCleanup() {
        Log.d(TAG, "Force cleanup initiated (Active surfaces: ${activeSurfaces.size})")
        try {
            MainActivitySingleton.nativePause()
            MainActivitySingleton.nativeSurfaceFinalize()
            activeSurfaces.clear()
            layoutChangeCount = 0
            Log.d(TAG, "Force cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during force cleanup", e)
        }
    }
    
    /**
     * Clean up weak references to garbage collected objects
     */
    fun cleanupWeakReferences() {
        val before = activeSurfaces.size
        activeSurfaces.removeAll { it.get() == null }
        val after = activeSurfaces.size
        if (before != after) {
            Log.d(TAG, "Cleaned up ${before - after} weak references (Remaining: $after)")
        }
        
        // Force garbage collection if we have too many dangling references
        if (before - after > 10) {
            Log.d(TAG, "Forcing garbage collection due to many dangling references")
            System.gc()
        }
    }
    
    /**
     * Get count of active surfaces
     */
    fun getActiveSurfaceCount(): Int {
        cleanupWeakReferences()
        return activeSurfaces.size
    }
    
    /**
     * Get memory usage statistics
     */
    fun getMemoryStats(): String {
        cleanupWeakReferences()
        return "Surfaces: ${activeSurfaces.size}, Layout changes: $layoutChangeCount"
    }
    
    /**
     * Reset all counters (useful for testing)
     */
    fun reset() {
        Log.d(TAG, "Resetting MemoryManager")
        activeSurfaces.clear()
        layoutChangeCount = 0
        lastCleanupTime = System.currentTimeMillis()
    }
} 