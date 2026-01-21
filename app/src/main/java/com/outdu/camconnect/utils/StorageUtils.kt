package com.outdu.camconnect.utils

import android.os.Environment
import android.os.StatFs

object StorageUtils {
    /**
     * Minimum free storage required to start/continue a recording.
     * Uses SI units (1000-based) as requested.
     */
    const val MIN_FREE_BYTES_FOR_RECORDING: Long = 332L * 1000L * 1000L * 1000L // 332GB (SI)

    fun getAvailableBytes(): Long {
        // Recording touches internal storage (cacheDir/tmp.mp4) and then writes to MediaStore (external primary).
        // To avoid false negatives/positives across devices, take the more constrained value.
        val internalBytes = getAvailableBytesInternalData()
        val externalBytes = getAvailableBytesExternalPrimaryOrNull()
        return if (externalBytes == null) internalBytes else minOf(internalBytes, externalBytes)
    }

    fun hasSufficientSpaceForRecording(): Boolean {
        return getAvailableBytes() >= MIN_FREE_BYTES_FOR_RECORDING
    }

    private fun getAvailableBytesInternalData(): Long {
        val statFs = StatFs(Environment.getDataDirectory().path)
        return statFs.availableBytes
    }

    private fun getAvailableBytesExternalPrimaryOrNull(): Long? {
        return try {
            @Suppress("DEPRECATION")
            val path = Environment.getExternalStorageDirectory().path
            val statFs = StatFs(path)
            statFs.availableBytes
        } catch (_: Throwable) {
            null
        }
    }
}

