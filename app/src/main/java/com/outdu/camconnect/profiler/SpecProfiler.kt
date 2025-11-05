package com.outdu.camconnect.profiler

import android.app.ActivityManager
import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.os.Build
import android.util.Log
import java.io.File

data class DeviceSpecs(
    val manufacturer: String,
    val model: String,
    val cpuName: String,
    val cpuCores: Int,
    val maxCpuFreqMHz: Int,
    val totalRamMB: Long,
    val gpuRenderer: String,
    val gpuVendor: String
)

fun getDeviceSpecs(context: Context): DeviceSpecs {
    Log.d("SpecProfiler", "Getting device specs...")
    val cpuName = readCpuName()
    val cores = Runtime.getRuntime().availableProcessors()
    val freq = readMaxCpuFreqMHz()
    val ram = getTotalRamMB(context)
    val gpu = getGpuInfo()

    val specs = DeviceSpecs(
        manufacturer = Build.MANUFACTURER,
        model = Build.MODEL,
        cpuName = cpuName,
        cpuCores = cores,
        maxCpuFreqMHz = freq,
        totalRamMB = ram,
        gpuRenderer = gpu.first,
        gpuVendor = gpu.second
    )
    
    Log.d("SpecProfiler", "Device specs retrieved:")
    Log.d("SpecProfiler", "  Manufacturer: ${specs.manufacturer}")
    Log.d("SpecProfiler", "  Model: ${specs.model}")
    Log.d("SpecProfiler", "  CPU: ${specs.cpuName}, Cores: ${specs.cpuCores}, Freq: ${specs.maxCpuFreqMHz} MHz")
    Log.d("SpecProfiler", "  RAM: ${specs.totalRamMB} MB")
    Log.d("SpecProfiler", "  GPU Renderer: ${specs.gpuRenderer}")
    Log.d("SpecProfiler", "  GPU Vendor: ${specs.gpuVendor}")
    
    return specs
}

// ----------------------------------------------------------

private fun readCpuName(): String {
    return try {
        File("/proc/cpuinfo").useLines { lines ->
            lines.firstOrNull { it.startsWith("Hardware") || it.startsWith("model name") }
                ?.split(":")?.getOrNull(1)?.trim().orEmpty()
        }
    } catch (_: Exception) { Build.HARDWARE }
}

private fun readMaxCpuFreqMHz(): Int {
    try {
        val cpuDir = File("/sys/devices/system/cpu/")
        val cpuFiles = cpuDir.listFiles { file -> file.name.matches(Regex("cpu[0-9]+")) } ?: return 0
        var maxFreq = 0
        for (cpu in cpuFiles) {
            val path = "${cpu.absolutePath}/cpufreq/cpuinfo_max_freq"
            val file = File(path)
            if (file.exists()) {
                val freq = file.readText().trim().toIntOrNull() ?: 0
                maxFreq = maxOf(maxFreq, freq)
            }
        }
        return maxFreq / 1000 // to MHz
    } catch (_: Exception) {
        return 0
    }
}

private fun getTotalRamMB(context: Context): Long {
    val memInfo = ActivityManager.MemoryInfo()
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    am.getMemoryInfo(memInfo)
    return memInfo.totalMem / (1024 * 1024)
}

private fun getGpuInfo(): Pair<String, String> {
    Log.d("SpecProfiler", "Starting GPU info detection...")
    var display: EGLDisplay? = null
    var context: EGLContext? = null
    var surface: EGLSurface? = null
    
    try {
        // Initialize EGL
        Log.d("SpecProfiler", "Attempting to get EGL display...")
        display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display == EGL14.EGL_NO_DISPLAY) {
            Log.e("SpecProfiler", "Failed to get EGL display")
            return "Unknown" to "Unknown"
        }
        Log.d("SpecProfiler", "EGL display obtained successfully")
        
        val version = IntArray(2)
        val initResult = EGL14.eglInitialize(display, version, 0, version, 1)
        Log.d("SpecProfiler", "EGL initialize result: $initResult, version: ${version[0]}.${version[1]}")
        if (!initResult) {
            val error = EGL14.eglGetError()
            Log.e("SpecProfiler", "Failed to initialize EGL, error code: $error")
            return "Unknown" to "Unknown"
        }
        
        // Configure EGL
        val attribList = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_NONE
        )
        
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        val chooseConfigResult = EGL14.eglChooseConfig(
            display,
            attribList, 0,
            configs, 0,
            configs.size,
            numConfigs, 0
        )
        Log.d("SpecProfiler", "EGL choose config result: $chooseConfigResult, numConfigs: ${numConfigs[0]}")
        if (!chooseConfigResult || numConfigs[0] == 0) {
            val error = EGL14.eglGetError()
            Log.e("SpecProfiler", "Failed to choose EGL config, error code: $error")
            EGL14.eglTerminate(display)
            return "Unknown" to "Unknown"
        }
        
        val config = configs[0] ?: run {
            Log.e("SpecProfiler", "Config is null after eglChooseConfig")
            EGL14.eglTerminate(display)
            return "Unknown" to "Unknown"
        }
        Log.d("SpecProfiler", "EGL config obtained successfully")
        
        // Create a pixel buffer surface (PBuffer)
        val surfaceAttribs = intArrayOf(
            EGL14.EGL_WIDTH, 1,
            EGL14.EGL_HEIGHT, 1,
            EGL14.EGL_NONE
        )
        
        surface = EGL14.eglCreatePbufferSurface(display, config, surfaceAttribs, 0)
        if (surface == EGL14.EGL_NO_SURFACE) {
            val error = EGL14.eglGetError()
            Log.e("SpecProfiler", "Failed to create EGL surface, error code: $error")
            EGL14.eglTerminate(display)
            return "Unknown" to "Unknown"
        }
        Log.d("SpecProfiler", "EGL surface created successfully")
        
        // Create context
        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        
        context = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
        if (context == EGL14.EGL_NO_CONTEXT) {
            val error = EGL14.eglGetError()
            Log.e("SpecProfiler", "Failed to create EGL context, error code: $error")
            EGL14.eglDestroySurface(display, surface)
            EGL14.eglTerminate(display)
            return "Unknown" to "Unknown"
        }
        Log.d("SpecProfiler", "EGL context created successfully")
        
        // Make context current
        val makeCurrentResult = EGL14.eglMakeCurrent(display, surface, surface, context)
        Log.d("SpecProfiler", "EGL make current result: $makeCurrentResult")
        if (!makeCurrentResult) {
            val error = EGL14.eglGetError()
            Log.e("SpecProfiler", "Failed to make EGL context current, error code: $error")
            EGL14.eglDestroyContext(display, context)
            EGL14.eglDestroySurface(display, surface)
            EGL14.eglTerminate(display)
            return "Unknown" to "Unknown"
        }
        
        // Now we can safely query GPU info
        Log.d("SpecProfiler", "Querying GPU information...")
        val renderer = GLES20.glGetString(GLES20.GL_RENDERER)
        val vendor = GLES20.glGetString(GLES20.GL_VENDOR)
        val versionStr = GLES20.glGetString(GLES20.GL_VERSION)
        
        val rendererStr = renderer ?: "Unknown"
        val vendorStr = vendor ?: "Unknown"
        
        Log.d("SpecProfiler", "GPU Renderer: $rendererStr")
        Log.d("SpecProfiler", "GPU Vendor: $vendorStr")
        Log.d("SpecProfiler", "OpenGL Version: ${versionStr ?: "Unknown"}")
        
        return rendererStr to vendorStr
        
    } catch (e: Exception) {
        Log.e("SpecProfiler", "Exception getting GPU info", e)
        e.printStackTrace()
        return "Unknown" to "Unknown"
    } finally {
        // Clean up EGL resources
        try {
            if (display != null && display != EGL14.EGL_NO_DISPLAY) {
                if (context != null && context != EGL14.EGL_NO_CONTEXT) {
                    EGL14.eglMakeCurrent(
                        display,
                        EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_CONTEXT
                    )
                    EGL14.eglDestroyContext(display, context)
                    Log.d("SpecProfiler", "EGL context destroyed")
                }
                if (surface != null && surface != EGL14.EGL_NO_SURFACE) {
                    EGL14.eglDestroySurface(display, surface)
                    Log.d("SpecProfiler", "EGL surface destroyed")
                }
                EGL14.eglTerminate(display)
                Log.d("SpecProfiler", "EGL terminated")
            }
        } catch (e: Exception) {
            Log.e("SpecProfiler", "Error cleaning up EGL", e)
        }
    }
}

/**
 * Normalizes GPU renderer string by removing special characters and spaces
 * for easier matching. Example: "Adreno (TM) 750" -> "adreno750"
 */
private fun normalizeGpuRenderer(renderer: String): String {
    return renderer
        .lowercase()
        .replace(Regex("[^a-z0-9]"), "") // Remove all non-alphanumeric characters
}

/**
 * Calculates GPU performance score based on renderer and vendor information.
 * Returns a score between 500 (low-end) and 8000 (high-end).
 */
private fun calculateGpuScore(renderer: String, vendor: String): Int {
    val normalized = normalizeGpuRenderer(renderer)
    val normalizedVendor = vendor.lowercase()
    
    Log.d("SpecProfiler", "Normalized GPU renderer: '$normalized' (from: '$renderer')")
    
    // Extract GPU model number if it's an Adreno or Mali GPU
    val isAdreno = normalized.contains("adreno")
    val isMaliG = normalized.contains("malig")
    val isMaliT = normalized.contains("malit")
    
    val adrenoModel = if (isAdreno) extractNumber(normalized, "adreno") else 0
    val maliGModel = if (isMaliG) extractNumber(normalized, "malig") else 0
    val maliTModel = if (isMaliT) extractNumber(normalized, "malit") else 0
    
    return when {
        // Adreno 7xx series (Snapdragon 8 Gen 3/4) - Flagship
        isAdreno && adrenoModel >= 700 -> {
            val score = when {
                adrenoModel >= 780 -> 12000  // Adreno 780+ (Snapdragon 8 Gen 4) - Top tier
                adrenoModel >= 750 -> 10000  // Adreno 750 (Snapdragon 8 Gen 4) - Flagship
                adrenoModel >= 740 -> 8500   // Adreno 740 (Snapdragon 8 Gen 3) - High-end
                adrenoModel >= 730 -> 7500   // Adreno 730 - High-end
                else -> 6500                 // Adreno 7xx (other)
            }
            Log.d("SpecProfiler", "GPU matched: Adreno 7xx series (model $adrenoModel) -> $score points")
            score
        }
        
        // Adreno 6xx series (Snapdragon 888/8 Gen 1/2) - High-end
        isAdreno && adrenoModel >= 600 && adrenoModel < 700 -> {
            val score = when {
                adrenoModel >= 680 -> 5500  // Adreno 680 (Snapdragon 8 Gen 2)
                adrenoModel >= 660 -> 5000  // Adreno 660 (Snapdragon 888)
                adrenoModel >= 650 -> 4500  // Adreno 650 (Snapdragon 865)
                adrenoModel >= 640 -> 4000  // Adreno 640
                adrenoModel >= 630 -> 3500  // Adreno 630
                else -> 3000                // Adreno 6xx (other)
            }
            Log.d("SpecProfiler", "GPU matched: Adreno 6xx series (model $adrenoModel) -> $score points")
            score
        }
        
        // Adreno 5xx series (Older flagships) - Mid-high-end
        isAdreno && adrenoModel >= 500 && adrenoModel < 600 -> {
            val score = when {
                adrenoModel >= 540 -> 2500  // Adreno 540
                adrenoModel >= 530 -> 2200  // Adreno 530
                else -> 1800                // Adreno 5xx (other)
            }
            Log.d("SpecProfiler", "GPU matched: Adreno 5xx series (model $adrenoModel) -> $score points")
            score
        }
        
        // Adreno 4xx and below - Mid-range
        isAdreno -> {
            val score = when {
                adrenoModel >= 430 -> 1500
                adrenoModel >= 420 -> 1200
                else -> 1000
            }
            Log.d("SpecProfiler", "GPU matched: Adreno series (model $adrenoModel) -> $score points")
            score
        }
        
        // Mali-G7xx series (Exynos, MediaTek) - High-end
        isMaliG && maliGModel >= 700 -> {
            val score = when {
                maliGModel >= 710 -> 5000  // Mali-G710
                maliGModel >= 700 -> 4500  // Mali-G700 series
                else -> 4000              // Mali-G7xx (other)
            }
            Log.d("SpecProfiler", "GPU matched: Mali-G7xx series (model $maliGModel) -> $score points")
            score
        }
        
        // Mali-G6xx series - Mid-high-end
        isMaliG && maliGModel >= 600 && maliGModel < 700 -> {
            val score = when {
                maliGModel >= 610 -> 3500  // Mali-G610
                maliGModel >= 600 -> 3000  // Mali-G600 series
                else -> 2500              // Mali-G6xx (other)
            }
            Log.d("SpecProfiler", "GPU matched: Mali-G6xx series (model $maliGModel) -> $score points")
            score
        }
        
        // Mali-G5xx and below - Mid-range
        isMaliG -> {
            val score = when {
                maliGModel >= 500 -> 2000  // Mali-G5xx
                maliGModel >= 400 -> 1500  // Mali-G4xx
                else -> 1200              // Other Mali-G
            }
            Log.d("SpecProfiler", "GPU matched: Mali-G series (model $maliGModel) -> $score points")
            score
        }
        
        // Mali-T series (Older) - Mid-range
        isMaliT -> {
            val score = when {
                maliTModel >= 800 -> 1800  // Mali-T8xx
                maliTModel >= 700 -> 1500  // Mali-T7xx
                else -> 1200              // Other Mali-T
            }
            Log.d("SpecProfiler", "GPU matched: Mali-T series (model $maliTModel) -> $score points")
            score
        }
        
        // PowerVR (Apple, older MediaTek) - Varies
        normalized.contains("powervr") -> {
            val score = 1500
            Log.d("SpecProfiler", "GPU matched: PowerVR -> $score points")
            score
        }
        
        // Apple GPU (for iOS compatibility on some devices)
        normalized.contains("apple") || normalizedVendor.contains("apple") -> {
            val score = 6000 // High-end Apple GPUs
            Log.d("SpecProfiler", "GPU matched: Apple GPU -> $score points")
            score
        }
        
        // Intel (for emulators or x86 devices)
        normalized.contains("intel") || normalizedVendor.contains("intel") -> {
            val score = 1500
            Log.d("SpecProfiler", "GPU matched: Intel GPU -> $score points")
            score
        }
        
        // Unknown or unrecognized GPU - use vendor-based fallback
        else -> {
            val fallbackScore = when {
                normalizedVendor.contains("qualcomm") -> 2000  // Qualcomm but unrecognized model
                normalizedVendor.contains("arm") -> 1500       // ARM but unrecognized model
                normalizedVendor.contains("imagination") -> 1500 // Imagination Technologies
                else -> 1000  // Completely unknown
            }
            Log.w("SpecProfiler", "GPU not matched to known series, renderer: '$renderer', vendor: '$vendor' -> using fallback score: $fallbackScore points")
            fallbackScore
        }
    }
}

/**
 * Extracts a number from a normalized string after a prefix.
 * Skips non-digit characters and extracts the first sequence of digits.
 * Example: extractNumber("adrenotm750", "adreno") returns 750
 * Example: extractNumber("adreno750", "adreno") returns 750
 */
private fun extractNumber(text: String, prefix: String): Int {
    val prefixIndex = text.indexOf(prefix)
    if (prefixIndex == -1) {
        Log.d("SpecProfiler", "extractNumber: prefix '$prefix' not found in '$text'")
        return 0
    }
    
    val startIndex = prefixIndex + prefix.length
    val remaining = text.substring(startIndex)
    
    // Find the first sequence of digits, skipping any non-digit characters
    val numberStr = remaining.dropWhile { !it.isDigit() }.takeWhile { it.isDigit() }
    val number = numberStr.toIntOrNull() ?: 0
    Log.d("SpecProfiler", "extractNumber: extracted $number from '$text' after '$prefix' (remaining: '$remaining', digits: '$numberStr')")
    return number
}

fun estimatePerformanceScore(specs: DeviceSpecs): Int {
    // CPU Score: Based on total processing power (cores * frequency)
    // Formula: (cores * max_freq_mhz) / 10
    // This gives a score roughly proportional to CPU performance
    val cpuScore = specs.cpuCores * specs.maxCpuFreqMHz / 10
    
    // RAM Score: Based on total RAM available
    // Formula: (total_ram_mb / 512) * 100
    // This gives 100 points per 512MB of RAM
    val ramScore = (specs.totalRamMB / 512).toInt() * 100
    
    // GPU Score: Based on GPU model and capabilities
    // Uses a lookup table for known GPU models
    val gpuScore = calculateGpuScore(specs.gpuRenderer, specs.gpuVendor)

    // Total Score: Sum of all components
    val totalScore = cpuScore + ramScore + gpuScore
    
    Log.d("SpecProfiler", "Performance score breakdown:")
    Log.d("SpecProfiler", "  CPU Score: $cpuScore (cores: ${specs.cpuCores} * freq: ${specs.maxCpuFreqMHz} MHz / 10)")
    Log.d("SpecProfiler", "  RAM Score: $ramScore (${specs.totalRamMB} MB / 512 * 100)")
    Log.d("SpecProfiler", "  GPU Score: $gpuScore (${specs.gpuRenderer})")
    Log.d("SpecProfiler", "  Total Score: $totalScore")
    
    return totalScore
}

enum class PerformanceTier(val label: String) {
    HIGH("Excellent Performance"),
    MEDIUM("Moderate Performance"),
    LOW("Limited Performance"),
    CRITICAL("Poor Performance")
}

fun classifyPerformance(score: Int): PerformanceTier {
    return when {
        score >= 12000 -> PerformanceTier.HIGH      // High-end flagships (e.g., Snapdragon 8 Gen 4)
        score >= 7000 -> PerformanceTier.MEDIUM     // Mid-high-end devices
        score >= 4000 -> PerformanceTier.LOW        // Mid-range devices
        else -> PerformanceTier.CRITICAL           // Low-end devices (e.g., UNISOC T618)
    }
}

fun getPerformanceMessage(tier: PerformanceTier): String {
    return when (tier) {
        PerformanceTier.HIGH ->
            "Your device is well-suited for this app. Expect smooth performance with ~10 FPS during object detection."
        PerformanceTier.MEDIUM ->
            "Your device can handle this app fairly well, but heavy operations may slow down. Expected FPS: ~7-8 FPS."
        PerformanceTier.LOW ->
            "Your device may struggle with performance. Expect frame drops and higher battery use. Expected FPS: ~4-5 FPS."
        PerformanceTier.CRITICAL ->
            "This device's hardware is below recommended specs. Performance will likely be poor with ~3 FPS during object detection."
    }
}