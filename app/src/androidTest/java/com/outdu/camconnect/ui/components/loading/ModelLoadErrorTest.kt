package com.outdu.camconnect.ui.components.loading

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ModelLoadError and ModelLoadState.
 */
@RunWith(AndroidJUnit4::class)
class ModelLoadErrorTest {

    @Test
    fun modelLoadError_outOfMemoryCannotRetry() {
        val error = ModelLoadError.OutOfMemory
        assertFalse(error.canRetry)
        assertTrue(error.message.contains("memory"))
    }

    @Test
    fun modelLoadError_fileNotFoundCannotRetry() {
        val error = ModelLoadError.FileNotFound
        assertFalse(error.canRetry)
    }

    @Test
    fun modelLoadError_loadTimeoutCanRetry() {
        val error = ModelLoadError.LoadTimeout
        assertTrue(error.canRetry)
    }

    @Test
    fun modelLoadError_fromException_timeout() {
        val e = Exception("loading timeout")
        val error = ModelLoadError.fromException(e)
        assertTrue(error is ModelLoadError.LoadTimeout)
    }

    @Test
    fun modelLoadError_fromException_fileNotFound() {
        val e = Exception("file not found in assets")
        val error = ModelLoadError.fromException(e)
        assertTrue(error is ModelLoadError.FileNotFound)
    }

    @Test
    fun modelLoadError_fromNativeFailure() {
        val error = ModelLoadError.fromNativeFailure()
        assertTrue(error is ModelLoadError.NativeFailure)
    }

    @Test
    fun modelLoadState_errorCanRetry() {
        val state = ModelLoadState.Error(
            error = ModelLoadError.LoadTimeout,
            attemptNumber = 1,
            maxAttempts = 3
        )
        assertTrue(state.canRetry)
    }

    @Test
    fun modelLoadState_errorCannotRetryWhenMaxAttemptsReached() {
        val state = ModelLoadState.Error(
            error = ModelLoadError.LoadTimeout,
            attemptNumber = 3,
            maxAttempts = 3
        )
        assertFalse(state.canRetry)
    }
}
