package com.outdu.camconnect.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.provider.MediaStore
import java.io.FileDescriptor

class ScreenRecorder(private val context: Context) {

    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null
    private val projectionManager =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    private var resultCode: Int = 0
    private var resultData: Intent? = null
    private var currentVideoUri: Uri? = null

    fun getProjectionIntent(): Intent = projectionManager.createScreenCaptureIntent()

    fun setProjectionResult(code: Int, data: Intent) {
        resultCode = code
        resultData = data
    }

    fun startRecording(): Boolean {
        if (resultData == null) return false

        val (uri, fd) = createRecordingFile(context)
        currentVideoUri = uri

        mediaRecorder = MediaRecorder().apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(fd)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoEncodingBitRate(5_000_000)
            setVideoFrameRate(30)
            setVideoSize(720, 1280)
            prepare()
            start()
        }

        mediaProjection = projectionManager.getMediaProjection(resultCode, resultData!!)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "RecordingDisplay",
            720, 1280, Resources.getSystem().displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder!!.surface, null, null
        )

        return true
    }

    fun stopRecording() {
        virtualDisplay?.release()
        mediaRecorder?.apply {
            stop()
            reset()
            release()
        }
        mediaProjection?.stop()

        // Mark video as not pending
        currentVideoUri?.let {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(it, values, null, null)
        }

        mediaRecorder = null
        mediaProjection = null
        virtualDisplay = null
        currentVideoUri = null
    }

    private fun createRecordingFile(context: Context): Pair<Uri, FileDescriptor?> {
        val resolver = context.contentResolver
        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val fileName = "recording_${System.currentTimeMillis()}.mp4"
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/MyAppRecordings")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        val uri = resolver.insert(collection, values)!!
        val pfd = resolver.openFileDescriptor(uri, "w")?.fileDescriptor
        return Pair(uri, pfd)
    }
}
