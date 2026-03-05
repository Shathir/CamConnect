package com.outdu.camconnect.profiler

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for Profiler DeviceSpecs.
 * getDeviceSpecs(context) requires Android runtime (EGL, /proc); covered in instrumented tests.
 */
class ProfilerTest {

    @Test
    fun `DeviceSpecs holds all fields`() {
        val specs = DeviceSpecs(
            manufacturer = "TestMan",
            model = "TestModel",
            cpuName = "TestCPU",
            cpuCores = 8,
            maxCpuFreqMHz = 2400,
            totalRamMB = 4096L,
            gpuRenderer = "Renderer",
            gpuVendor = "Vendor"
        )
        assertEquals("TestMan", specs.manufacturer)
        assertEquals("TestModel", specs.model)
        assertEquals("TestCPU", specs.cpuName)
        assertEquals(8, specs.cpuCores)
        assertEquals(2400, specs.maxCpuFreqMHz)
        assertEquals(4096L, specs.totalRamMB)
        assertEquals("Renderer", specs.gpuRenderer)
        assertEquals("Vendor", specs.gpuVendor)
    }

    @Test
    fun `DeviceSpecs equals by value`() {
        val a = DeviceSpecs("M", "Mo", "C", 4, 2000, 2048L, "G", "V")
        val b = DeviceSpecs("M", "Mo", "C", 4, 2000, 2048L, "G", "V")
        assertEquals(a, b)
    }

    @Test
    fun `DeviceSpecs copy`() {
        val specs = DeviceSpecs("A", "B", "C", 2, 1000, 1024L, "R", "V")
        val copy = specs.copy(totalRamMB = 2048L)
        assertEquals(2048L, copy.totalRamMB)
        assertEquals(1024L, specs.totalRamMB)
    }
}
