package com.outdu.camconnect.ui.components.loading

/**
 * Sealed class representing different types of model loading errors
 */
sealed class ModelLoadError(
    val message: String,
    val canRetry: Boolean,
    val technicalDetails: String? = null
) {
    object OutOfMemory : ModelLoadError(
        message = "Insufficient memory to load AI model",
        canRetry = false,
        technicalDetails = "Device doesn't have enough free RAM for model initialization"
    )
    
    object FileNotFound : ModelLoadError(
        message = "AI model files are missing or corrupted",
        canRetry = false,
        technicalDetails = "Model assets not found in APK"
    )
    
    object LoadTimeout : ModelLoadError(
        message = "Model loading timed out",
        canRetry = true,
        technicalDetails = "Loading exceeded expected duration"
    )
    
    object NativeFailure : ModelLoadError(
        message = "Failed to initialize AI model",
        canRetry = true,
        technicalDetails = "Native loadODModel returned false"
    )
    
    object OpenMPError : ModelLoadError(
        message = "System compatibility issue detected",
        canRetry = false,
        technicalDetails = "OpenMP initialization conflict"
    )
    
    data class UnknownError(
        val exception: Exception? = null
    ) : ModelLoadError(
        message = exception?.message ?: "An unexpected error occurred",
        canRetry = true,
        technicalDetails = exception?.stackTraceToString()
    )
    
    companion object {
        /**
         * Categorize an exception into a specific ModelLoadError type
         */
        fun fromException(e: Exception): ModelLoadError {
            return when {
                e is OutOfMemoryError -> OutOfMemory
                e.message?.contains("file not found", ignoreCase = true) == true -> FileNotFound
                e.message?.contains("timeout", ignoreCase = true) == true -> LoadTimeout
                e.message?.contains("openmp", ignoreCase = true) == true -> OpenMPError
                else -> UnknownError(e)
            }
        }
        
        /**
         * Create error from native load failure
         */
        fun fromNativeFailure(): ModelLoadError = NativeFailure
    }
}

/**
 * Sealed class representing the loading state of the AI model
 */
sealed class ModelLoadState {
    object Loading : ModelLoadState()
    object Success : ModelLoadState()
    data class Error(
        val error: ModelLoadError,
        val attemptNumber: Int = 1,
        val maxAttempts: Int = 3
    ) : ModelLoadState() {
        val canRetry: Boolean get() = error.canRetry && attemptNumber < maxAttempts
    }
    object Skipped : ModelLoadState()
}
