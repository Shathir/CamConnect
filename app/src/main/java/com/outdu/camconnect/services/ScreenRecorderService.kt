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
import androidx.window.layout.WindowMetricsCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.FileInputStream

@Parcelize
data class RecordConfig(
    val resultCode: Int,
    val data: Intent
): Parcelable


class ScreenRecorderService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private val mediaRecorder by lazy {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
           MediaRecorder(applicationContext)
        }
        else {
            MediaRecorder()
        }
    }
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val outputFile by lazy {
        File(cacheDir, "tmp.mp4")
    }

    private val mediaProjectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            releaseResources()
            stopService()
            saveToGallery()
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Create notification channel when service is created
        NotificationHelper.createNotificationChannel(applicationContext)
    }

    private fun saveToGallery() {
        serviceScope.launch {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "video_${System.currentTimeMillis()}.mp4")
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
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START -> {
                val notification = NotificationHelper.createNotification(applicationContext)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
                }
                else {
                    startForeground(
                        NOTIFICATION_ID,
                        notification
                    )
                }

                _isServiceRunning.value = true
                startRecording(intent)
            }
            ACTION_STOP -> {
                stopRecording()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startRecording(intent: Intent?) {
        val config = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra<RecordConfig>(
                RECORD_CONFIG,
                RecordConfig::class.java)
        }
        else {
            intent?.getParcelableExtra<RecordConfig>(RECORD_CONFIG)
        }

        if(config == null) {
            return
        }

        mediaProjection = mediaProjectionManager.getMediaProjection(
            config.resultCode,
            config.data
        )

        mediaProjection?.registerCallback(mediaProjectionCallback, null)

        initializeRecorder()
        mediaRecorder.start()

        virtualDisplay = createVirtualDisplay()
    }

    private fun stopRecording() {
        mediaRecorder.stop()
        mediaProjection?.stop()
        mediaRecorder.reset()
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

    private fun getScaledDimensions(maxWidth: Int, maxHeight: Int, scaleFactor: Float = 0.8f): Pair<Int,Int>{
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
        val (width, height) = getWindowSize()
        val (scaledWidth, scaledHeight) = getScaledDimensions(
            maxWidth = width,
            maxHeight = height
        )
        with(mediaRecorder) {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(outputFile)
            setVideoSize(scaledWidth, scaledHeight)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoEncodingBitRate(VIDEO_BIT_RATE_KILOBITS * 1000)
            setVideoFrameRate(VIDEO_FRAME_RATE)
            prepare()
        }
    }

    private fun createVirtualDisplay(): VirtualDisplay? {
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
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false
        serviceScope.coroutineContext.cancelChildren()
    }

    private fun releaseResources() {
        mediaRecorder.release()
        virtualDisplay?.release()
        mediaProjection?.unregisterCallback(mediaProjectionCallback)
        mediaProjection?.stop()
        mediaProjection = null
    }

    companion object {
        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning = _isServiceRunning.asStateFlow()

        private const val VIDEO_FRAME_RATE = 30
        private const val VIDEO_BIT_RATE_KILOBITS = 512
        private const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val RECORD_CONFIG = "RECORD_CONFIG"
    }
}