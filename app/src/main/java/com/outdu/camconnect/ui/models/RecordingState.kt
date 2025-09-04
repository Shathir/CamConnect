package com.outdu.camconnect.ui.models

sealed class RecordingState {
    object NotRecording : RecordingState()
    data class Recording(val duration: String) : RecordingState()
    object StoppingRecording : RecordingState()
    object PromptingForFilename : RecordingState()
    object SavedToGallery : RecordingState()
} 