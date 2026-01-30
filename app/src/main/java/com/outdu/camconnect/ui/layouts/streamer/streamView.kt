package com.outdu.camconnect.ui.layouts.streamer

import android.content.Context
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Typeface
import com.outdu.camconnect.singleton.MainActivitySingleton
import com.outdu.camconnect.Viewmodels.AppViewModel
import com.outdu.camconnect.utils.MemoryManager
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import com.outdu.camconnect.OverlayPoints
import com.outdu.camconnect.ui.modelLabels.BOAT_LABELS
import com.outdu.camconnect.ui.modelLabels.COCO_LABELS
import com.outdu.camconnect.communication.CameraConfigurationManager
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Enum defining the type of overlay to display for the AI processing region
 */
enum class AiRegionOverlayType {
    /** Draw a transparent mask over non-AI regions */
    MASK,
    /** Draw a box outline around the AI processing region */
    BOX,
    /** No overlay */
    NONE
}

/**
 * Configuration for AI region dimensions
 */
data class AiRegionConfig(
    val streamWidth: Float = 1280f,
    val streamHeight: Float = 720f,
    val aiRegionWidth: Float = 720f,
    val aiRegionHeight: Float = 720f
) {
    /**
     * Calculates the AI region bounds in stream coordinates
     */
    fun calculateStreamBounds(): AiRegionBounds {
        val aiLeftStream = (streamWidth - aiRegionWidth) / 2f
        val aiRightStream = aiLeftStream + aiRegionWidth
        val aiTopStream = 0f
        val aiBottomStream = aiRegionHeight
        return AiRegionBounds(
            left = aiLeftStream,
            right = aiRightStream,
            top = aiTopStream,
            bottom = aiBottomStream
        )
    }
}

/**
 * Bounds of the AI region in stream coordinates
 */
data class AiRegionBounds(
    val left: Float,
    val right: Float,
    val top: Float,
    val bottom: Float
) {
    /**
     * Scales bounds to view coordinates
     */
    fun scaleToView(viewWidth: Float, viewHeight: Float, streamWidth: Float, streamHeight: Float): AiRegionBounds {
        val scaleX = viewWidth / streamWidth
        val scaleY = viewHeight / streamHeight
        return AiRegionBounds(
            left = left * scaleX,
            right = right * scaleX,
            top = top * scaleY,
            bottom = bottom * scaleY
        )
    }
}

/**
 * Draws the AI region overlay based on the specified overlay type
 */
private fun DrawScope.drawAiRegionOverlay(
    overlayType: AiRegionOverlayType,
    viewSize: Size,
    aiConfig: AiRegionConfig,
    maskAlpha: Float = 0.4f,
    boxStrokeWidth: Float = 4f,
    boxColor: Color = Color.Red
) {
    when (overlayType) {
        AiRegionOverlayType.MASK -> drawMaskOverlay(viewSize, aiConfig, maskAlpha)
        // QR-scanner style: dim outside region + draw corner brackets.
        AiRegionOverlayType.BOX -> {
            drawBoxOverlay(viewSize, aiConfig, boxStrokeWidth, boxColor)
        }
        AiRegionOverlayType.NONE -> { /* No overlay */ }
    }
}

/**
 * Draws a transparent mask over non-AI processing regions
 */
private fun DrawScope.drawMaskOverlay(
    viewSize: Size,
    aiConfig: AiRegionConfig,
    maskAlpha: Float
) {
    val streamBounds = aiConfig.calculateStreamBounds()
    val viewBounds = streamBounds.scaleToView(viewSize.width, viewSize.height, aiConfig.streamWidth, aiConfig.streamHeight)
    val maskColor = Color.Black.copy(alpha = maskAlpha)
    
    // Draw left mask (region before AI processing area)
    if (viewBounds.left > 0) {
        drawRect(
            color = maskColor,
            topLeft = Offset(0f, 0f),
            size = Size(viewBounds.left, viewSize.height)
        )
    }
    
    // Draw right mask (region after AI processing area)
    if (viewBounds.right < viewSize.width) {
        drawRect(
            color = maskColor,
            topLeft = Offset(viewBounds.right, 0f),
            size = Size(viewSize.width - viewBounds.right, viewSize.height)
        )
    }
    
    // Draw top mask (if AI region doesn't start at top)
    if (viewBounds.top > 0) {
        drawRect(
            color = maskColor,
            topLeft = Offset(viewBounds.left, 0f),
            size = Size(viewBounds.right - viewBounds.left, viewBounds.top)
        )
    }
    
    // Draw bottom mask (if AI region doesn't extend to bottom)
    if (viewBounds.bottom < viewSize.height) {
        drawRect(
            color = maskColor,
            topLeft = Offset(viewBounds.left, viewBounds.bottom),
            size = Size(viewBounds.right - viewBounds.left, viewSize.height - viewBounds.bottom)
        )
    }
}

/**
 * Draws a box outline around the AI processing region
 */
private fun DrawScope.drawBoxOverlay(
    viewSize: Size,
    aiConfig: AiRegionConfig,
    strokeWidth: Float,
    boxColor: Color
) {
    val streamBounds = aiConfig.calculateStreamBounds()
    val viewBounds = streamBounds.scaleToView(viewSize.width, viewSize.height, aiConfig.streamWidth, aiConfig.streamHeight)

    // Draw "scanner" style corner brackets (like a QR code scanner) instead of a full box.
    val left = viewBounds.left
    val top = viewBounds.top
    val right = viewBounds.right
    val bottom = viewBounds.bottom

    val w = (right - left).coerceAtLeast(0f)
    val h = (bottom - top).coerceAtLeast(0f)
    if (w <= 0f || h <= 0f) return

    // Make corners bolder than the default "box" stroke.
    val effectiveStroke = strokeWidth * 3.0f
    val inset = effectiveStroke / 2f

    // Inset bounds so strokes aren't clipped at the edges (top/bottom lines were invisible before).
    val l = (left + inset).coerceAtMost(right - inset)
    val r = (right - inset).coerceAtLeast(left + inset)
    val t = (top + inset).coerceAtMost(bottom - inset)
    val b = (bottom - inset).coerceAtLeast(top + inset)

    // Corner segment length (QR-scanner style brackets).
    val cornerLen = minOf(w, h) * 0.22f
    val xLen = cornerLen.coerceIn(effectiveStroke * 3f, w / 2f)
    val yLen = cornerLen.coerceIn(effectiveStroke * 3f, h / 2f)

    // Top-left corner
    drawLine(
        color = boxColor,
        start = Offset(l, t),
        end = Offset(l + xLen, t),
        strokeWidth = effectiveStroke,
        cap = androidx.compose.ui.graphics.StrokeCap.Square
    )
    drawLine(
        color = boxColor,
        start = Offset(l, t),
        end = Offset(l, t + yLen),
        strokeWidth = effectiveStroke,
        cap = androidx.compose.ui.graphics.StrokeCap.Square
    )

    // Top-right corner
    drawLine(
        color = boxColor,
        start = Offset(r - xLen, t),
        end = Offset(r, t),
        strokeWidth = effectiveStroke,
        cap = androidx.compose.ui.graphics.StrokeCap.Square
    )
    drawLine(
        color = boxColor,
        start = Offset(r, t),
        end = Offset(r, t + yLen),
        strokeWidth = effectiveStroke,
        cap = androidx.compose.ui.graphics.StrokeCap.Square
    )

    // Bottom-left corner
    drawLine(
        color = boxColor,
        start = Offset(l, b),
        end = Offset(l + xLen, b),
        strokeWidth = effectiveStroke,
        cap = androidx.compose.ui.graphics.StrokeCap.Square
    )
    drawLine(
        color = boxColor,
        start = Offset(l, b - yLen),
        end = Offset(l, b),
        strokeWidth = effectiveStroke,
        cap = androidx.compose.ui.graphics.StrokeCap.Square
    )

    // Bottom-right corner
    drawLine(
        color = boxColor,
        start = Offset(r - xLen, b),
        end = Offset(r, b),
        strokeWidth = effectiveStroke,
        cap = androidx.compose.ui.graphics.StrokeCap.Square
    )
    drawLine(
        color = boxColor,
        start = Offset(r, b - yLen),
        end = Offset(r, b),
        strokeWidth = effectiveStroke,
        cap = androidx.compose.ui.graphics.StrokeCap.Square
    )
}

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
                                    Log.d("Gstreamer MainActivity", CameraConfigurationManager.isObjectDetectionEnabled().toString())
                                    Log.d(
                                        "GStreamer MainActivity",
                                        "Surface changed to format " + format + " width "
                                                + width + " height " + height
                                    )
                                    Log.i("Data values : ", CameraConfigurationManager.isObjectDetectionEnabled().toString())
                                    MemoryManager.registerSurface(holder.surface)
                                    MainActivitySingleton.nativeSurfaceInit(holder.surface)
                                    Log.i("Gstreamer MainActivity", "Playing Stream")
                                    Log.i("Gstreamer MainActivity", "nativePlay() called at t=${SystemClock.elapsedRealtime()}ms")
                                    MainActivitySingleton.nativePlay(
                                        width = width, 
                                        height = height, 
                                        od = CameraConfigurationManager.isObjectDetectionEnabled(), 
                                        ds = CameraConfigurationManager.isDepthSensingEnabled(), CameraConfigurationManager.isFarDetectionEnabled()
                                    )
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
fun ZoomableVideoTextureView1(viewModel: AppViewModel, currentContext: Context, pointState: MutableState<OverlayPoints>) {
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
                                MainActivitySingleton.nativePlay(width, height, CameraConfigurationManager.isObjectDetectionEnabled(), CameraConfigurationManager.isDepthSensingEnabled(), CameraConfigurationManager.isFarDetectionEnabled())
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


@Composable
fun ZoomableVideoTextureView(
    viewModel: AppViewModel,
    currentContext: Context,
    pointState: MutableState<OverlayPoints>,
    onUserActivity: () -> Unit = {}
) {
    if (!viewModel.isPlaying.value) return

    val scaleRange = 1f..8f
    val defaultScale = 1f

    var scale by remember { mutableStateOf(defaultScale) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val animatedScale = remember { Animatable(defaultScale) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val coroutineScope = rememberCoroutineScope()

    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var textureView by remember { mutableStateOf<TextureView?>(null) }
    var overlaySurfaceView by remember { mutableStateOf<SurfaceView?>(null) }

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

    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.cancel()
            textureView?.surfaceTextureListener = null
            textureView = null
            overlaySurfaceView = null
            safelyFinalizeSurface()
        }
    }

    val doubleTapGesture = rememberUpdatedState {
        coroutineScope.launch {
            try {
                val scaleJob = launch {
                    animatedScale.animateTo(defaultScale, tween(300, easing = FastOutSlowInEasing))
                }
                val offsetJob = launch {
                    animatedOffset.animateTo(Offset.Zero, tween(300, easing = FastOutSlowInEasing))
                }
                scaleJob.join()
                offsetJob.join()
                scale = animatedScale.value
                offset = animatedOffset.value
            } catch (e: Exception) {
                Log.e("ZoomableTextureView", "Animation error", e)
            }
        }
    }

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
            .aspectRatio(16f / 9f)
            .onGloballyPositioned { viewSize = it.size }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    onUserActivity()
                    val newScale = (scale * zoom).coerceIn(scaleRange)
                    val viewCenter = Offset(viewSize.width / 2f, viewSize.height / 2f)
                    val focalPoint = centroid - viewCenter
                    val scaleDelta = newScale - scale
                    val focalOffset = focalPoint * scaleDelta / scale
                    val adjustedOffset = offset - focalOffset + pan

                    offset = clampOffset(adjustedOffset, newScale, viewSize)
                    scale = newScale

                    coroutineScope.launch {
                        animatedScale.snapTo(scale)
                        animatedOffset.snapTo(offset)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onUserActivity() },
                    onDoubleTap = {
                        onUserActivity()
                        doubleTapGesture.value()
                    },
                    onLongPress = { onUserActivity() }
                )
            }
    ) {
        // --- Video Stream (TextureView) ---
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


                                // Load configuration synchronously for immediate use
                                CameraConfigurationManager.loadConfiguration(currentContext)
                                Log.d("CameraConfigurationManager", "${CameraConfigurationManager.isFarDetectionEnabled()}")
                                MainActivitySingleton.nativePlay(
                                    width, 
                                    height, 
                                    CameraConfigurationManager.isObjectDetectionEnabled(), 
                                    CameraConfigurationManager.isDepthSensingEnabled(),
                                    CameraConfigurationManager.isFarDetectionEnabled()
                                )

                                isSurfaceFinalized = false
                            } catch (e: Exception) {
                                Log.e("ZoomableTextureView", "Surface init error", e)
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
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
//                            overlaySurfaceView?.holder?.let {
//                                drawOverlay(it, pointState.value, viewSize.width, viewSize.height)
//                            }
                        }
                    }
                    textureView = this
                }
            },
            modifier = Modifier
                .graphicsLayer {
                    scaleX = animatedScale.value
                    scaleY = animatedScale.value
                    translationX = animatedOffset.value.x
                    translationY = animatedOffset.value.y
                }
                .fillMaxSize()
        )


        val isAiEnabled = CameraConfigurationManager.isObjectDetectionEnabled()
        // --- Detection Overlay (Compose Canvas) ---
        // This overlay is drawn as a Compose element so it respects z-ordering
        // and appears below UI elements but above the video stream
        if (isAiEnabled && viewSize.width > 0 && viewSize.height > 0) {
            Canvas(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = animatedScale.value
                        scaleY = animatedScale.value
                        translationX = animatedOffset.value.x
                        translationY = animatedOffset.value.y
                    }
                    .fillMaxSize()
            ) {
                val labelSize = pointState.value.labels.size
                if (labelSize > 0) {
                    drawIntoCanvas { canvas ->
                        val boxPaint = Paint().apply {
                            style = Paint.Style.STROKE
                            strokeWidth = 6f
                            color = android.graphics.Color.RED
                            isAntiAlias = true
                        }

                        val textPaint = Paint().apply {
                            style = Paint.Style.FILL
                            textSize = 48f
                            color = android.graphics.Color.RED
                            isAntiAlias = true
                            typeface = Typeface.DEFAULT_BOLD
                        }

                        for (index in 0 until labelSize) {
                            val labelIndex = pointState.value.labels[index]
                            val label = COCO_LABELS[labelIndex]

                            // Scale detection coordinates from model space to screen space
                            val x = pointState.value.pointXs[index] * size.width / 1920f
                            val y = pointState.value.pointYs[index] * size.height / 1080f
                            val w = pointState.value.pointWs[index] * size.width / 1920f
                            val h = pointState.value.pointHs[index] * size.height / 1080f

                            val left = x
                            val top = y + 30
                            val right = x + w
                            val bottom = y + h + 30

                            // Optional: color based on depth threshold
                            val depThresh = pointState.value.depThres.getOrNull(index)
                            val isDanger = depThresh != null && depThresh > CameraConfigurationManager.getDepthSensingThreshold()

                            boxPaint.color = if (isDanger) android.graphics.Color.RED else android.graphics.Color.YELLOW
                            textPaint.color = boxPaint.color

                            // Draw the bounding box
                            canvas.nativeCanvas.drawRect(left, top, right, bottom, boxPaint)

                            // Draw the label text position dynamically
                            val textY = if (top - textPaint.textSize - 4f < 0f) {
                                // Not enough space above → draw inside the box
                                top + textPaint.textSize + 4f
                            } else {
                                // Enough space above → draw above the box
                                top - 8f
                            }

                            // Draw the label just above the top-left corner of the box
                            canvas.nativeCanvas.drawText(label, left + 8f, textY, textPaint)
                        }
                    }
                }
            }
        }

        // --- AI Region Overlay (Compose Canvas) ---
        // This overlay is drawn as a Compose element so it respects z-ordering
        // and appears below UI elements but above the video stream
        // Load overlay type from SharedPreferences (reactive)
        var overlayType by remember { 
            mutableStateOf(AiRegionOverlayType.MASK)
        }
        
        // Load overlay type from SharedPreferences and update when stream state changes
        LaunchedEffect(viewModel.isPlaying.value) {
            val prefs = currentContext.getSharedPreferences("ai_config_prefs", Context.MODE_PRIVATE)
            val overlayTypeName = prefs.getString("overlay_type", AiRegionOverlayType.MASK.name)
            overlayType = try {
                AiRegionOverlayType.valueOf(overlayTypeName ?: AiRegionOverlayType.MASK.name)
            } catch (e: IllegalArgumentException) {
                AiRegionOverlayType.MASK
            }
        }
        
        if (isAiEnabled && viewSize.width > 0 && viewSize.height > 0 && overlayType != AiRegionOverlayType.NONE) {
            Canvas(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = animatedScale.value
                        scaleY = animatedScale.value
                        translationX = animatedOffset.value.x
                        translationY = animatedOffset.value.y
                    }
                    .fillMaxSize()
            ) {
                val aiConfig = AiRegionConfig()
                drawAiRegionOverlay(
                    overlayType = overlayType,
                    viewSize = size,
                    aiConfig = aiConfig,
                    maskAlpha = 0.4f,
                    boxStrokeWidth = 4f,
                    boxColor = Color.Red
                )
            }
        }
    }
}

fun drawOverlay1(
    holder: SurfaceHolder,
    pointState: OverlayPoints,
    viewWidth: Int,
    viewHeight: Int
) {
    val canvas = holder.lockCanvas() ?: return
    try {
        canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val paint = Paint().apply {
            color = android.graphics.Color.RED
            style = Paint.Style.FILL
            textSize = 50f
            isAntiAlias = true
        }

        val labelList = pointState.labels
        val size = labelList.size

        for (i in 0 until size) {
            val label = BOAT_LABELS[labelList[i]]

            val x = pointState.pointXs[i] * viewWidth / 1920f
            val y = pointState.pointYs[i] * viewHeight / 1080f
            val w = pointState.pointWs[i] * viewWidth / 1920f
            val h = pointState.pointHs[i] * viewHeight / 1080f

            val centerX = x + w / 2f
            val centerY = y + h / 2f
            val textOffsetX = centerX + 40f
            val textOffsetY = centerY

            canvas.drawCircle(centerX, centerY, 20f, paint)
            canvas.drawText(label, textOffsetX, textOffsetY, paint)
        }
    } catch (e: Exception) {
        Log.e("OverlayDraw", "Error drawing overlay", e)
    } finally {
        holder.unlockCanvasAndPost(canvas)
    }
}

fun drawOverlay(
    holder: SurfaceHolder,
    pointState: OverlayPoints,
    viewWidth: Int,
    viewHeight: Int
) {
    val canvas = holder.lockCanvas() ?: return
    try {
        canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val boxPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = android.graphics.Color.RED
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            style = Paint.Style.FILL
            textSize = 48f
            color = android.graphics.Color.RED
            isAntiAlias = true
        }

        val labelSize = pointState.labels.size
        for (index in 0 until labelSize) {
            val labelIndex = pointState.labels[index]
            val label = BOAT_LABELS[labelIndex]

            // Scale detection coordinates from model space to screen space
            val x = pointState.pointXs[index] * viewWidth / 1920f
            val y = pointState.pointYs[index] * viewHeight / 1080f
            val w = pointState.pointWs[index] * viewWidth / 1920f
            val h = pointState.pointHs[index] * viewHeight / 1080f

            val left = x
            val top = y
            val right = x + w
            val bottom = y + h

            // Optional: color based on depth threshold
            val depThresh = pointState.depThres.getOrNull(index)
            val isDanger = depThresh != null && depThresh > CameraConfigurationManager.getDepthSensingThreshold()

            boxPaint.color = if (isDanger) android.graphics.Color.RED else android.graphics.Color.YELLOW
            textPaint.color = boxPaint.color

            // Draw the bounding box
            canvas.drawRect(left, top, right, bottom, boxPaint)

            // Draw the label text position dynamically
            val textY = if (top - textPaint.textSize - 4f < 0f) {
                // Not enough space above → draw inside the box
                top + textPaint.textSize + 4f
            } else {
                // Enough space above → draw above the box
                top - 8f
            }

            // Draw the label just above the top-left corner of the box
            canvas.drawText(label, left + 8f, textY, textPaint)
            Log.d("CameraConfigurationManager", "Overlay text is : ${label}")
        }

    } catch (e: Exception) {
        Log.e("OverlayDraw", "Error drawing overlay", e)
    } finally {
        holder.unlockCanvasAndPost(canvas)
    }
}