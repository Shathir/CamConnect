package com.outdu.camconnect.ui.layouts.streamer

import android.content.Context
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.outdu.camconnect.communication.Data
import com.outdu.camconnect.singleton.MainActivitySingleton
import com.outdu.camconnect.Viewmodels.AppViewModel

@Composable
fun VideoSurfaceView(viewModel: AppViewModel, currentContext: Context) {
    if (viewModel.isPlaying.value) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AndroidView(
                factory = { context ->
                    SurfaceView(context).apply {
                        // Set up SurfaceView here if needed
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }.also { surfaceView ->
                        val surfaceHolder = surfaceView.holder
                        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
                            override fun surfaceChanged(
                                holder: SurfaceHolder, format: Int, width: Int,
                                height: Int
                            ) {
                                Log.d("Gstreamer MainActivity", "Loading Data")
//                            Data.loadData(currentContext)
                                Log.d("Gstreamer MainActivity", Data.isOD().toString())
                                Log.d(
                                    "GStreamer MainActivity",
                                    "Surface changed to format " + format + " width "
                                            + width + " height " + height
                                )
//                            Data.loadData(currentContext)
                                Log.i("Data values : ", Data.isOD().toString())
                                MainActivitySingleton.nativeSurfaceInit(holder.surface)
                                val recording_path = MainActivitySingleton.getRecordingPath()
                                Log.i("Gstreamer MainActivity", "Playing Stream")
                                MainActivitySingleton.nativePlay(width = width, height = height)
//                            MainActivitySingleton.nativePlay(Data.isOD(), false, Data.isFAR())
                            }

                            override fun surfaceCreated(holder: SurfaceHolder) {
                                Log.d(
                                    "GStreamer MainActivity",
                                    "Surface created: " + holder.surface
                                )
                            }

                            override fun surfaceDestroyed(holder: SurfaceHolder) {
                                Log.d("GStreamer MainActivity", "Surface destroyed")
                                MainActivitySingleton.nativePause()
                                MainActivitySingleton.nativeSurfaceFinalize()
                            }
                        })
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}