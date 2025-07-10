package com.outdu.camconnect.ui.layouts.streamer

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import com.outdu.camconnect.communication.Data
import com.outdu.camconnect.singleton.MainActivitySingleton
import com.outdu.camconnect.Viewmodels.AppViewModel
import com.outdu.camconnect.utils.MemoryManager
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Composable
fun VideoSurfaceView(viewModel: AppViewModel, currentContext: Context) {
    if (viewModel.isPlaying.value) {
        
        // Store SurfaceView reference for proper cleanup
        var surfaceView by remember { mutableStateOf<SurfaceView?>(null) }
        
        // Cleanup when composable is disposed
        DisposableEffect(Unit) {
            onDispose {
                // Cleanup SurfaceView
                surfaceView?.holder?.removeCallback(surfaceView?.holder?.let { holder ->
                    // Find and remove the callback - this prevents memory leaks
                    holder.surface?.let { surface ->
                        try {
                            MainActivitySingleton.nativePause()
                            MainActivitySingleton.nativeSurfaceFinalize()
                        } catch (e: Exception) {
                            Log.e("VideoSurfaceView", "Error during cleanup", e)
                        }
                    }
                    null
                })
                surfaceView = null
            }
        }
        
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
                    }.also { createdSurfaceView ->
                        surfaceView = createdSurfaceView
                        val surfaceHolder = createdSurfaceView.holder
                        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
                            override fun surfaceChanged(
                                holder: SurfaceHolder, format: Int, width: Int,
                                height: Int
                            ) {
                                try {
                                    Log.d("Gstreamer MainActivity", "Loading Data")
                                    Log.d("Gstreamer MainActivity", Data.isOD().toString())
                                    Log.d(
                                        "GStreamer MainActivity",
                                        "Surface changed to format " + format + " width "
                                                + width + " height " + height
                                    )
                                    Log.i("Data values : ", Data.isOD().toString())
                                    MemoryManager.registerSurface(holder.surface)
                                    MainActivitySingleton.nativeSurfaceInit(holder.surface)
                                    val recording_path = MainActivitySingleton.getRecordingPath()
                                    Log.i("Gstreamer MainActivity", "Playing Stream")
                                    MainActivitySingleton.nativePlay(width = width, height = height)
                                } catch (e: Exception) {
                                    Log.e("VideoSurfaceView", "Surface changed error", e)
                                }
                            }

                            override fun surfaceCreated(holder: SurfaceHolder) {
                                Log.d(
                                    "GStreamer MainActivity",
                                    "Surface created: " + holder.surface
                                )
                            }

                            override fun surfaceDestroyed(holder: SurfaceHolder) {
                                try {
                                    Log.d("GStreamer MainActivity", "Surface destroyed")
                                    MemoryManager.unregisterSurface(holder.surface)
                                    MainActivitySingleton.nativePause()
                                    MainActivitySingleton.nativeSurfaceFinalize()
                                } catch (e: Exception) {
                                    Log.e("VideoSurfaceView", "Surface destroy error", e)
                                }
                            }
                        })
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ZoomableVideoTextureView(viewModel: AppViewModel, currentContext: Context) {
    if (!viewModel.isPlaying.value) return

    val scaleRange = 1f..8f
    val defaultScale = 1f

    var scale by remember { mutableStateOf(defaultScale) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    // Animation state for smooth transitions
    val animatedScale = remember { Animatable(defaultScale) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val coroutineScope = rememberCoroutineScope()

    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    
    // Store TextureView reference for proper cleanup
    var textureView by remember { mutableStateOf<TextureView?>(null) }

    var isSurfaceFinalized by remember { mutableStateOf(false) }

    fun safelyFinalizeSurface() {
        if (!isSurfaceFinalized) {
            try {
                MainActivitySingleton.nativePause()
                MainActivitySingleton.nativeSurfaceFinalize()
            } catch (e: Exception) {
                Log.e("ZoomableTextureView", "Error during surface finalization", e)
            }
            isSurfaceFinalized = true
        }
    }

    // Cleanup when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            // Cancel all running coroutines
            coroutineScope.cancel()
            // Cleanup TextureView
            textureView?.surfaceTextureListener = null
            textureView = null
            // Ensure native cleanup
            safelyFinalizeSurface()
//            try {
//                MainActivitySingleton.nativePause()
//                MainActivitySingleton.nativeSurfaceFinalize()
//            } catch (e: Exception) {
//                Log.e("ZoomableTextureView", "Error during cleanup", e)
//            }
        }
    }

    val doubleTapGesture = rememberUpdatedState(newValue = {
        // Smooth animation back to default state
        coroutineScope.launch {
            try {
                // Launch both animations in parallel
                val scaleJob = launch {
                    animatedScale.animateTo(
                        targetValue = defaultScale,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    )
                }
                val offsetJob = launch {
                    animatedOffset.animateTo(
                        targetValue = Offset.Zero,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    )
                }
                // Wait for both animations to complete
                scaleJob.join()
                offsetJob.join()
                // Update the state variables
                scale = animatedScale.value
                offset = animatedOffset.value
            } catch (e: Exception) {
                Log.e("ZoomableTextureView", "Animation error", e)
            }
        }
    })

    fun clampOffset(offset: Offset, scale: Float, viewSize: IntSize): Offset {
        val maxX = (viewSize.width * (scale - 1)) / 2
        val maxY = (viewSize.height * (scale - 1)) / 2
        return Offset(
            x = offset.x.coerceIn(-maxX, maxX),
            y = offset.y.coerceIn(-maxY, maxY)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(16f/9f)
            .onGloballyPositioned {
                viewSize = it.size
            }
            .pointerInput(Unit) {
                detectTransformGestures(
                    onGesture = { centroid, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(scaleRange)
                        
                        // Calculate focal point zooming
                        if (zoom != 1f) {
                            // Convert centroid to offset from center
                            val viewCenter = Offset(viewSize.width / 2f, viewSize.height / 2f)
                            val focalPoint = centroid - viewCenter
                            
                            // Calculate the offset adjustment for focal point zooming
                            val scaleDelta = newScale - scale
                            val focalOffset = focalPoint * scaleDelta / scale
                            
                            // Apply the focal point adjustment
                            val adjustedOffset = offset - focalOffset + pan
                            offset = clampOffset(adjustedOffset, newScale, viewSize)
                        } else {
                            // Just pan without zoom
                            val newOffset = offset + pan
                            offset = clampOffset(newOffset, scale, viewSize)
                        }
                        
                        scale = newScale
                        
                        // Update animated values to current state (for smooth interaction)
                        coroutineScope.launch {
                            try {
                                animatedScale.snapTo(scale)
                                animatedOffset.snapTo(offset)
                            } catch (e: Exception) {
                                Log.e("ZoomableTextureView", "Snap animation error", e)
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        doubleTapGesture.value()
                    }
                )
            }
    ) {
        AndroidView(
            factory = { context ->
                TextureView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                            try {
                                val s = Surface(surface)
                                MemoryManager.registerSurface(s)
                                MainActivitySingleton.nativeSurfaceInit(s)
                                MainActivitySingleton.nativePlay(width, height)
                                isSurfaceFinalized = false
                            } catch (e: Exception) {
                                Log.e("ZoomableTextureView", "Surface init error", e)
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                            // Handle size changes if needed
                        }
                        
                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            try {
                                MemoryManager.unregisterSurface(Surface(surface))
                                safelyFinalizeSurface()
                            } catch (e: Exception) {
                                Log.e("ZoomableTextureView", "Surface destroy error", e)
                            }
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                            // Handle frame updates if needed
                        }
                    }
                    textureView = this
                }
            },
            modifier = Modifier
                .graphicsLayer {
                    // Use animated values for smooth transitions, fall back to state values during gestures
                    scaleX = if (animatedScale.isRunning) animatedScale.value else scale
                    scaleY = if (animatedScale.isRunning) animatedScale.value else scale
                    translationX = if (animatedOffset.isRunning) animatedOffset.value.x else offset.x
                    translationY = if (animatedOffset.isRunning) animatedOffset.value.y else offset.y
                }
                .fillMaxSize()
        )
    }
}




