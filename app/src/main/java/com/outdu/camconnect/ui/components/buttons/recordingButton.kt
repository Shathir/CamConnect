package com.outdu.camconnect.ui.components.buttons

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.outdu.camconnect.MainActivity.Companion.REQUEST_CODE_SCREEN_CAPTURE
import com.outdu.camconnect.Viewmodels.RecorderViewModel
import com.outdu.camconnect.services.ScreenRecorderService

@SuppressLint("ImplicitSamInstance")
@Composable
fun ScreenRecorderUI(context: Context, viewModel: RecorderViewModel) {
    val recording by viewModel.isRecording.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val fileSize by viewModel.fileSize.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Recording: $recording")
        Text("Elapsed: ${elapsedTime}s")
        Text("Size: ${fileSize / (1024 * 1024)} MB")

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (recording) {
                context.stopService(Intent(context, ScreenRecorderService::class.java))
                viewModel.stop()
            } else {
                val mediaProjectionManager =
                    context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                val intent = mediaProjectionManager.createScreenCaptureIntent()
                (context as Activity).startActivityForResult(intent, REQUEST_CODE_SCREEN_CAPTURE)
            }
        }) {
            Text(if (recording) "Stop Recording" else "Start Recording")
        }
    }
}