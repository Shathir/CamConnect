package com.outdu.camconnect.ui.viewmodels

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outdu.camconnect.MainActivity
import com.outdu.camconnect.services.RecordConfig
import com.outdu.camconnect.services.ScreenRecorderService
import com.outdu.camconnect.ui.models.RecordingState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class RecordingViewModel : ViewModel() {
    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.NotRecording)
    val recordingState = _recordingState.asStateFlow()

    private var recordingStartTime = 0L
    private var durationUpdateJob: Job? = null

    init {
        viewModelScope.launch {
            ScreenRecorderService.isServiceRunning.collectLatest { isRunning ->
                _isRecording.value = isRunning
                if (isRunning) {
                    _recordingState.value = RecordingState.Recording("00:00")
                    startDurationUpdates()
                } else {
                    durationUpdateJob?.cancel()
                }
            }
        }
    }

    private fun startDurationUpdates() {
        recordingStartTime = System.currentTimeMillis()
        durationUpdateJob?.cancel()
        durationUpdateJob = viewModelScope.launch {
            while (true) {
                val duration = System.currentTimeMillis() - recordingStartTime
                val formattedDuration = formatDuration(duration)
                _recordingState.value = RecordingState.Recording(formattedDuration)
                delay(1000) // Update every second
            }
        }
    }

    private fun formatDuration(duration: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun toggleRecording(context: Context) {
        if (_isRecording.value) {
            _recordingState.value = RecordingState.StoppingRecording
            stopRecording(context)
            // Show "Saved to Gallery" message briefly
            viewModelScope.launch {
                delay(1000) // Wait for the recording to stop
                _recordingState.value = RecordingState.SavedToGallery
                delay(2000) // Show "Saved" message for 2 seconds
                _recordingState.value = RecordingState.NotRecording
            }
        } else {
            startRecording(context)
        }
    }

    private fun startRecording(context: Context) {
        try {
            val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            if (context is Activity) {
                context.startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    MainActivity.REQUEST_CODE_SCREEN_CAPTURE
                )
            } else {
                val captureIntent = mediaProjectionManager.createScreenCaptureIntent().apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("android.intent.extra.USE_FULL_SCREEN", true)
                    putExtra("android.intent.extra.SHOW_APP_SELECTOR", false)
                }
                context.startActivity(captureIntent)
            }
        } catch (e: Exception) {
            Log.e("RecordingViewModel", "Error starting recording", e)
            Toast.makeText(context, "Failed to start recording: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording(context: Context) {
        try {
            Intent(context, ScreenRecorderService::class.java).also {
                it.action = ScreenRecorderService.ACTION_STOP
                ContextCompat.startForegroundService(context, it)
            }
        } catch (e: Exception) {
            Log.e("RecordingViewModel", "Error stopping recording", e)
            Toast.makeText(context, "Failed to stop recording: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleActivityResult(context: Context, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            try {
                val config = RecordConfig(resultCode, data)
                val serviceIntent = Intent(context, ScreenRecorderService::class.java).apply {
                    action = ScreenRecorderService.ACTION_START
                    putExtra(ScreenRecorderService.RECORD_CONFIG, config)
                }
                ContextCompat.startForegroundService(context, serviceIntent)
            } catch (e: Exception) {
                Log.e("RecordingViewModel", "Error handling activity result", e)
                Toast.makeText(context, "Failed to start recording service: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
} 