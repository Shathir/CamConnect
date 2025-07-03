package com.outdu.camconnect.services

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.outdu.camconnect.Viewmodels.RecorderViewModel

class ScreenRecordService : Service() {

    private lateinit var mediaProjection: MediaProjection
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var virtualDisplay: VirtualDisplay
    private lateinit var projectionManager: MediaProjectionManager

    private val filePath: String
        get() = "${getExternalFilesDir(null)?.absolutePath}/recording_${System.currentTimeMillis()}.mp4"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED) ?: return START_NOT_STICKY
        val data = intent.getParcelableExtra<Intent>("data") ?: return START_NOT_STICKY

        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)

        startForeground(1, createNotification())

        setupMediaRecorder()
        mediaRecorder.prepare()
        mediaRecorder.start()

        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenRecorder",
            SCREEN_WIDTH, SCREEN_HEIGHT, screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder.surface, null, null
        )

        return START_STICKY
    }

    private fun setupMediaRecorder() {
        val path = filePath
        Log.d("Screen Recording Logs", "Path: $path")
        RecorderViewModel.outputFilePath = path
        Log.d("Screen Recording Logs", "Path: ${RecorderViewModel.outputFilePath}")

        mediaRecorder = MediaRecorder().apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoEncodingBitRate(8_000_000)
            setVideoFrameRate(30)
            setVideoSize(SCREEN_WIDTH, SCREEN_HEIGHT)
            setOutputFile(path)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaRecorder.stop()
            mediaRecorder.reset()
            virtualDisplay.release()
            mediaProjection.stop()
        } catch (_: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "screen_recorder"
        val channelName = "Screen Recorder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Recording Screen")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }

    companion object {
        private const val SCREEN_WIDTH = 720
        private const val SCREEN_HEIGHT = 1280
        private const val screenDensity = 320
    }
}
