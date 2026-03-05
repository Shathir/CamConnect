package com.outdu.camconnect.services

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import androidx.window.layout.WindowMetricsCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.FileInputStream
import com.outdu.camconnect.utils.StorageUtils

@Parcelize
data class RecordConfig(
    val resultCode: Int,
    val data: Intent,
    val customFilename: String? = null
): Parcelable

class ScreenRecorderService : Service() {
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private val mediaRecorder by lazy {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        } else {
            MediaRecorder()
        }
    }
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentRecordConfig: RecordConfig? = null
    private var storageMonitorJob: Job? = null

    private val outputFile by lazy {
        File(cacheDir, "tmp.mp4").also {
            if (it.exists() && !it.delete()) {
                    Log.w(TAG, "Failed to delete existing tmp.mp4")
            }
        }
    }

    private val mediaProjectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            Log.d(TAG, "MediaProjection stopped")
            releaseResources()
            stopService()
            saveToGallery()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        NotificationHelper.createNotificationChannel(applicationContext)
    }

    private fun saveToGallery() {
        serviceScope.launch {
            try {
                val filename = currentRecordConfig?.customFilename?.let { "$it.mp4" } 
                    ?: "video_${System.currentTimeMillis()}.mp4"
                
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/nveyetech")
                }
                val videoCollection = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

                contentResolver.insert(videoCollection, contentValues)?.let { uri ->
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        FileInputStream(outputFile).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    Log.d(TAG, "Video saved to gallery")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving video to gallery", e)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")
        when(intent?.action) {
            ACTION_START -> {
                val notification = NotificationHelper.createNotification(applicationContext)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                    )
                } else {
                    startForeground(
                        NOTIFICATION_ID,
                        notification
                    )
                }

                // Safety-net: if started from any other code path, still block recordings on low storage.
                if (!StorageUtils.hasSufficientSpaceForRecording()) {
                    _events.tryEmit(ServiceEvent.CannotStartLowStorage)
                    stopService()
                    return START_NOT_STICKY
                }

                _isServiceRunning.value = true
                startRecording(intent)
            }
            ACTION_STOP -> {
                storageMonitorJob?.cancel()
                storageMonitorJob = null
                stopRecording()
            }
            ACTION_UPDATE_FILENAME -> {
                val customFilename = intent?.getStringExtra(CUSTOM_FILENAME)
                if (customFilename != null && currentRecordConfig != null) {
                    currentRecordConfig = currentRecordConfig!!.copy(customFilename = customFilename)
                    Log.d(TAG, "Updated filename to: $customFilename")
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startRecording(intent: Intent?) {
        try {
            val config = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getParcelableExtra(RECORD_CONFIG, RecordConfig::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent?.getParcelableExtra(RECORD_CONFIG)
            }

            if(config == null) {
                Log.e(TAG, "No recording config provided")
                stopSelf()
                return
            }
            
            if (!StorageUtils.hasSufficientSpaceForRecording()) {
                _events.tryEmit(ServiceEvent.CannotStartLowStorage)
                stopService()
                return
            }

            currentRecordConfig = config

            mediaProjection = mediaProjectionManager.getMediaProjection(
                config.resultCode,
                config.data
            )

            if (mediaProjection == null) {
                Log.e(TAG, "MediaProjection is null")
                stopSelf()
                return
            }

            mediaProjection?.registerCallback(mediaProjectionCallback, null)
            initializeRecorder()
            mediaRecorder.start()
            virtualDisplay = createVirtualDisplay()
            Log.d(TAG, "Recording started successfully")

            startStorageMonitor()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            stopSelf()
        }
    }

    private fun startStorageMonitor() {
        storageMonitorJob?.cancel()
        storageMonitorJob = serviceScope.launch {
            while (true) {
                val available = StorageUtils.getAvailableBytes()
                Log.i(TAG, "Available memory is ${available/ 1_000_000_000}GB")
                if (available < StorageUtils.MIN_FREE_BYTES_FOR_RECORDING) {
                    Log.w(
                        TAG,
                        "Low storage detected (< ${StorageUtils.MIN_FREE_BYTES_FOR_RECORDING / 1_000_000_000}GB). Stopping recording."
                    )
                    _events.tryEmit(ServiceEvent.StoppedLowStorage)
                    stopRecording()
                    break
                }
                delay(5_000)
            }
        }
    }

    private fun stopRecording() {
        storageMonitorJob?.cancel()
        storageMonitorJob = null

        // Be defensive: MediaRecorder.stop() can throw IllegalStateException in edge cases.
        // We still want to stop MediaProjection so the service callback can run cleanup/save.
        try {
            mediaRecorder.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaRecorder", e)
        } finally {
            try {
                mediaProjection?.stop()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping MediaProjection", e)
            }
        }

        try {
            mediaRecorder.reset()
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting MediaRecorder", e)
        }

        Log.d(TAG, "Recording stop requested")
    }

    private fun stopService() {
        _isServiceRunning.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun getWindowSize(): Pair<Int, Int> {
        val calculator = WindowMetricsCalculator.getOrCreate()
        val metrics = calculator.computeMaximumWindowMetrics(applicationContext)
        return metrics.bounds.width() to metrics.bounds.height()
    }

    private fun getScaledDimensions(maxWidth: Int, maxHeight: Int, scaleFactor: Float = 0.8f): Pair<Int,Int> {
        val aspectRatio = maxWidth / maxHeight.toFloat()
        var newWidth = (maxWidth * scaleFactor).toInt()
        var newHeight = (newWidth / aspectRatio).toInt()

        if(newHeight > (maxHeight * scaleFactor)) {
            newHeight = (maxHeight * scaleFactor).toInt()
            newWidth = (newHeight * aspectRatio).toInt()
        }
        return newWidth to newHeight
    }

    private fun initializeRecorder() {
        try {
            val (width, height) = getWindowSize()
            val (scaledWidth, scaledHeight) = getScaledDimensions(
                maxWidth = width,
                maxHeight = height
            )
            with(mediaRecorder) {
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(outputFile.absolutePath)
                setVideoSize(scaledWidth, scaledHeight)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setVideoEncodingBitRate(VIDEO_BIT_RATE_KILOBITS * 1000)
                setVideoFrameRate(VIDEO_FRAME_RATE)
                prepare()
            }
            Log.d(TAG, "MediaRecorder initialized with size: ${scaledWidth}x${scaledHeight}")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing recorder", e)
            throw e
        }
    }

    private fun createVirtualDisplay(): VirtualDisplay? {
        try {
            val (width, height) = getWindowSize()
            return mediaProjection?.createVirtualDisplay(
                "Screen",
                width,
                height,
                resources.displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.surface,
                null,
                null
            ).also {
                Log.d(TAG, "VirtualDisplay created with size: ${width}x${height}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating virtual display", e)
            throw e
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false
        storageMonitorJob?.cancel()
        storageMonitorJob = null
        serviceScope.coroutineContext.cancelChildren()
        Log.d(TAG, "Service destroyed")
    }

    private fun releaseResources() {
        try {
            mediaRecorder.release()
            virtualDisplay?.release()
            mediaProjection?.unregisterCallback(mediaProjectionCallback)
            mediaProjection?.stop()
            mediaProjection = null
            Log.d(TAG, "Resources released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing resources", e)
        }
    }

    companion object {
        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning = _isServiceRunning.asStateFlow()

        private val _events = MutableSharedFlow<ServiceEvent>(extraBufferCapacity = 1)
        val events = _events.asSharedFlow()

        private const val VIDEO_FRAME_RATE = 30
        private const val VIDEO_BIT_RATE_KILOBITS = 8192 // Increased for better quality
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "ScreenRecorderService"

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_UPDATE_FILENAME = "ACTION_UPDATE_FILENAME"
        const val RECORD_CONFIG = "RECORD_CONFIG"
        const val CUSTOM_FILENAME = "CUSTOM_FILENAME"
    }
}

sealed interface ServiceEvent {
    /**
     * Recording could not start because the device has less than the minimum required free space.
     */
    object CannotStartLowStorage : ServiceEvent

    /**
     * Recording was stopped automatically because the device dropped below the minimum required free space.
     */
    object StoppedLowStorage : ServiceEvent

    /**
     * Recording was stopped normally (user or service stop). No low-storage UI event.
     */
    data class RecordingStopped(val outputPath: String) : ServiceEvent
}