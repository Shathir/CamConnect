package com.outdu.camconnect.tflite

import android.app.Application
import android.graphics.*
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.pow
import android.util.Log

data class UIState(
    val cameraGranted: Boolean = false,
    val enhancedBitmap: Bitmap? = null,
    val lastMs: Double = 0.0,
    val fps: Double = 0.0
)

class PairLIEViewModel(app: Application) : AndroidViewModel(app) {

    private val _ui = MutableStateFlow(UIState())
    val uiState = _ui.asStateFlow()

    // Frame processing control
    private var isProcessingFrame = false
    private var frameCount = 0
    private val maxConcurrentFrames = 1 // Only process one frame at a time

    private val interpreter: Interpreter by lazy {
        try {
            Log.d(TAG, "Loading model: pairlie_float32.tflite")
            val model = FileUtil.loadMappedFile(getApplication(), "pairlie_float16.tflite")
            Log.d(TAG, "Model loaded successfully, size: ${model.remaining()} bytes")
            
            // Check device information for NNAPI compatibility
            Log.d(TAG, "🔍 Device hardware debugging:")
            Log.d(TAG, "  - Device model: ${android.os.Build.MODEL}")
            Log.d(TAG, "  - Android version: ${android.os.Build.VERSION.RELEASE}")
            Log.d(TAG, "  - API level: ${android.os.Build.VERSION.SDK_INT}")
            Log.d(TAG, "  - Attempting NNAPI acceleration (NPU/DSP)")
            
            Log.d(TAG, "Creating interpreter with NNAPI + ${Runtime.getRuntime().availableProcessors().coerceAtMost(4)} CPU threads")
            
            val opts = Interpreter.Options().apply {
                // Try NNAPI delegate for hardware acceleration on Samsung devices
                try {
                    Log.d(TAG, "🚀 Attempting to create NNAPI delegate...")
                    val nnApiDelegate = NnApiDelegate()
                    addDelegate(nnApiDelegate)
                    Log.d(TAG, "✅ NNAPI delegate added successfully - using NPU/DSP acceleration")
                } catch (e: Exception) {
                    Log.w(TAG, "❌ NNAPI delegate failed, falling back to CPU", e)
                    Log.w(TAG, "NNAPI Error details: ${e.javaClass.simpleName}: ${e.message}")
                    // Continue with CPU - don't add any delegate
                }
                
                // Set CPU threads (used as fallback or alongside NNAPI)
                setNumThreads(Runtime.getRuntime().availableProcessors().coerceAtMost(4))
            }
            
            val interp = Interpreter(model, opts)
            Log.d(TAG, "Interpreter created successfully")
            Log.d(TAG, "Input tensor count: ${interp.inputTensorCount}")
            Log.d(TAG, "Output tensor count: ${interp.outputTensorCount}")
            
            // Log input/output tensor info
            for (i in 0 until interp.inputTensorCount) {
                val inputTensor = interp.getInputTensor(i)
                Log.d(TAG, "Input $i: name='${inputTensor.name()}', shape=${inputTensor.shape().contentToString()}, dataType=${inputTensor.dataType()}")
            }
            
            for (i in 0 until interp.outputTensorCount) {
                val outputTensor = interp.getOutputTensor(i)
                Log.d(TAG, "Output $i: name='${outputTensor.name()}', shape=${outputTensor.shape().contentToString()}, dataType=${outputTensor.dataType()}")
            }
            
            // Detect if outputs are single-channel or 3-channel
            val outputShape = interp.getOutputTensor(0).shape()
            val outputChannels = if (outputShape.size >= 4) outputShape[3] else 1
            Log.d(TAG, "Detected output channels: $outputChannels")
            
            interp
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model", e)
            throw e
        }
    }

    // Store detected output channels
    private var outputChannels: Int = 0

    private fun detectOutputChannels(): Int {
        if (outputChannels == 0) {
            try {
                val outputShape = interpreter.getOutputTensor(0).shape()
                Log.d(TAG, "Output tensor 0 shape: ${outputShape.contentToString()}")
                
                // For shape [1, H, W, C], channels are at index 3
                // For shape [1, C, H, W], channels are at index 1
                outputChannels = when (outputShape.size) {
                    4 -> {
                        // NHWC format: [batch, height, width, channels]
                        val channels = outputShape[3]
                        Log.d(TAG, "Detected NHWC format with $channels channels")
                        channels
                    }
                    3 -> {
                        // HWC format: [height, width, channels]
                        val channels = outputShape[2]
                        Log.d(TAG, "Detected HWC format with $channels channels")
                        channels
                    }
                    else -> {
                        Log.w(TAG, "Unexpected output shape size: ${outputShape.size}, defaulting to 3 channels")
                        3 // Default to 3 channels
                    }
                }
                
                Log.d(TAG, "Final detected output channels: $outputChannels")
            } catch (e: Exception) {
                Log.e(TAG, "Error detecting output channels, defaulting to 3", e)
                outputChannels = 3 // Default to 3 channels on error
            }
        }
        return outputChannels
    }

    // Reusable buffers (avoid GC) - pre-allocated for efficiency
    private var lastInputFloat: FloatArray = FloatArray(0)
    private var nhwcBuffer: ByteBuffer? = null

    // latest outputs for optional saving
    private var lastL: FloatArray? = null
    private var lastR: FloatArray? = null
    private var lastX: FloatArray? = null
    private var lastW = 0
    private var lastH = 0

    // FPS tracking
    private val lastTs = AtomicLong(System.nanoTime())

    fun onCameraPermission(granted: Boolean) {
        Log.d(TAG, "Camera permission: $granted")
        _ui.value = _ui.value.copy(cameraGranted = granted)
    }

    fun onFrame(image: ImageProxy) {
        // Skip frame if already processing one
        if (isProcessingFrame) {
            Log.d(TAG, "Skipping frame - already processing")
            image.close()
            return
        }
        
        frameCount++
        val frameStartTime = System.currentTimeMillis()
        Log.d(TAG, "🎬 Frame $frameCount START at ${java.text.SimpleDateFormat("HH:mm:ss.SSS").format(java.util.Date(frameStartTime))}")
        Log.d(TAG, "Processing frame $frameCount, size: ${image.width}x${image.height}")
        
        isProcessingFrame = true
        
        // Run off main thread
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val step1 = System.currentTimeMillis()
                Log.d(TAG, "⏱️ Frame $frameCount: Step 1 - Image conversion started")
                
                // Convert YUV -> RGB Bitmap (fast enough; keeps template dependency-free)
                val originalBmp = imageProxyToBitmap(image)
                image.close()
                
                val step2 = System.currentTimeMillis()
                Log.d(TAG, "⏱️ Frame $frameCount: Step 2 - Image conversion done (${step2 - step1}ms)")
                
                // Downscale to reduce memory usage (keep aspect ratio)
//                val maxDimension = 640 // Reduce from 1280x720 to ~640x360
//                val scaledBmp = if (originalBmp.width > maxDimension || originalBmp.height > maxDimension) {
//                    val scale = maxDimension.toFloat() / maxOf(originalBmp.width, originalBmp.height)
//                    val newWidth = (originalBmp.width * scale).toInt()
//                    val newHeight = (originalBmp.height * scale).toInt()
//                    val scaled = Bitmap.createScaledBitmap(originalBmp, newWidth, newHeight, true)
//                    originalBmp.recycle() // Free original bitmap
//                    Log.d(TAG, "Frame $frameCount: Scaled from ${originalBmp.width}x${originalBmp.height} to ${scaled.width}x${scaled.height}")
//                    scaled
//                } else {
//                    originalBmp
//                }

                val scaledBmp = Bitmap.createScaledBitmap(originalBmp, originalBmp.width / 6, originalBmp.height / 6, true)
                
                val step3 = System.currentTimeMillis()
                Log.d(TAG, "⏱️ Frame $frameCount: Step 3 - Scaling done (${step3 - step2}ms)")
                Log.d(TAG, "Frame $frameCount: Bitmap prepared, size: ${scaledBmp.width}x${scaledBmp.height}")

                val w = scaledBmp.width
                val h = scaledBmp.height
                lastW = w; lastH = h

                // Prepare NHWC float32 [1, h, w, 3] in range [0,1]
                ensureInput(w, h)
                bitmapToNhwcFloat(scaledBmp, lastInputFloat)
                
                val step4 = System.currentTimeMillis()
                Log.d(TAG, "⏱️ Frame $frameCount: Step 4 - Input tensor prep (${step4 - step3}ms), array size: ${lastInputFloat.size}")

                // Reuse ByteBuffer to avoid allocations
                ensureByteBuffer(lastInputFloat.size)
                nhwcBuffer!!.clear()
                nhwcBuffer!!.asFloatBuffer().put(lastInputFloat)
                
                val step5 = System.currentTimeMillis()
                Log.d(TAG, "⏱️ Frame $frameCount: Step 5 - ByteBuffer prep (${step5 - step4}ms), size: ${nhwcBuffer!!.capacity()}")

                // Dynamically resize input to [1,h,w,3]
                try {
                    val inputIndex = interpreter.getInputIndex("input")
                    Log.d(TAG, "Frame $frameCount: Input tensor index: $inputIndex")
                    interpreter.resizeInput(inputIndex, intArrayOf(1, h, w, 3))
                    interpreter.allocateTensors()
                    val step6 = System.currentTimeMillis()
                    Log.d(TAG, "⏱️ Frame $frameCount: Step 6 - Tensor allocation (${step6 - step5}ms) for shape [1, $h, $w, 3]")
                } catch (e: Exception) {
                    Log.e(TAG, "Frame $frameCount: Error setting up tensors", e)
                    // Try with index 0 as fallback
                    try {
                        Log.d(TAG, "Frame $frameCount: Trying with input index 0 as fallback")
                        interpreter.resizeInput(0, intArrayOf(1, h, w, 3))
                        interpreter.allocateTensors()
                        Log.d(TAG, "Frame $frameCount: Fallback successful")
                    } catch (e2: Exception) {
                        Log.e(TAG, "Frame $frameCount: Fallback also failed", e2)
                        throw e2
                    }
                }

                val t0 = System.nanoTime()
                val inferenceStartTime = System.currentTimeMillis()
                Log.d(TAG, "🚀 Frame $frameCount: INFERENCE STARTING at ${java.text.SimpleDateFormat("HH:mm:ss.SSS").format(java.util.Date(inferenceStartTime))}")
                
                // Detect output channels and prepare outputs accordingly
                val channels = detectOutputChannels()
                Log.d(TAG, "Frame $frameCount: Using $channels channels for outputs")
                
                try {
                    // Get individual output tensor shapes for mixed-channel handling
                    val output0Shape = interpreter.getOutputTensor(0).shape()
                    val output1Shape = interpreter.getOutputTensor(1).shape()
                    val output2Shape = interpreter.getOutputTensor(2).shape()
                    
                    val channels0 = if (output0Shape.size >= 4) output0Shape[3] else 1 // L channels
                    val channels1 = if (output1Shape.size >= 4) output1Shape[3] else 1 // R channels
                    val channels2 = if (output2Shape.size >= 4) output2Shape[3] else 1 // X channels
                    
                    Log.d(TAG, "Frame $frameCount: Mixed channel outputs - L:$channels0, R:$channels1, X:$channels2")
                    
                    // Prepare outputs with correct individual channel counts
                    val out0 = Array(1) { Array(h) { Array(w) { FloatArray(channels0) } } } // L
                    val out1 = Array(1) { Array(h) { Array(w) { FloatArray(channels1) } } } // R  
                    val out2 = Array(1) { Array(h) { Array(w) { FloatArray(channels2) } } } // X
                    
                    val inferenceStart = System.nanoTime()
                    interpreter.runForMultipleInputsOutputs(arrayOf(nhwcBuffer), mutableMapOf(
                        0 to out0, 1 to out1, 2 to out2
                    ) as Map<Int, Any>)
                    val inferenceEnd = System.nanoTime()
                    
                    val t1 = System.nanoTime()
                    val totalMs = (t1 - t0) / 1e6
                    val inferenceMs = (inferenceEnd - inferenceStart) / 1e6
                    val inferenceEndTime = System.currentTimeMillis()
                    Log.d(TAG, "✅ Frame $frameCount: INFERENCE COMPLETED at ${java.text.SimpleDateFormat("HH:mm:ss.SSS").format(java.util.Date(inferenceEndTime))}")
                    Log.d(TAG, "Frame $frameCount: ⚡ INFERENCE TIME: ${inferenceMs}ms (Total: ${totalMs}ms) - mixed channels")

                    // Convert outputs to CHW format, handling different channel counts
                    val L = if (channels0 == 1) {
                        expandSingleChannelToRGB(nhwcToCHWSingleChannel(out0), w, h)
                    } else {
                        nhwcToCHW(out0)
                    }
                    
                    val R = if (channels1 == 1) {
                        expandSingleChannelToRGB(nhwcToCHWSingleChannel(out1), w, h)
                    } else {
                        nhwcToCHW(out1)
                    }
                    
                    val X = if (channels2 == 1) {
                        expandSingleChannelToRGB(nhwcToCHWSingleChannel(out2), w, h)
                    } else {
                        nhwcToCHW(out2)
                    }
                    
                    lastL = L; lastR = R; lastX = X
                    Log.d(TAG, "Frame $frameCount: Mixed outputs converted - L size: ${L.size}, R size: ${R.size}, X size: ${X.size}")
                    
                    processOutputs(L, R, X, scaledBmp, w, h, inferenceMs, frameCount)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Frame $frameCount: Inference failed with detected channels=$channels", e)
                    
                    // Let's try to get actual tensor shapes for debugging
                    try {
                        for (i in 0 until interpreter.outputTensorCount) {
                            val tensor = interpreter.getOutputTensor(i)
                            Log.d(TAG, "Actual output tensor $i shape: ${tensor.shape().contentToString()}")
                        }
                    } catch (debugE: Exception) {
                        Log.e(TAG, "Failed to get tensor shapes for debugging", debugE)
                    }
                    
                    throw e
                }
                
            } catch (e: Throwable) {
                Log.e(TAG, "Error processing frame $frameCount", e)
                image.close()
                // In production, report/log
            } finally {
                isProcessingFrame = false
                val frameEndTime = System.currentTimeMillis()
                val totalFrameTime = frameEndTime - frameStartTime
                Log.d(TAG, "🏁 Frame $frameCount COMPLETED at ${java.text.SimpleDateFormat("HH:mm:ss.SSS").format(java.util.Date(frameEndTime))} (Total: ${totalFrameTime}ms)")
            }
        }
    }

    fun saveOutputs() {
        // Removed - not saving images, just showing live output
        Log.d(TAG, "Save function disabled - showing live output only")
    }

    // ---------- Helpers (optimized, allocation-conscious) ----------

    private fun ensureInput(w: Int, h: Int) {
        val needed = 1 * h * w * 3
        if (lastInputFloat.size != needed) {
            lastInputFloat = FloatArray(needed)
        }
    }

    // Ensure ByteBuffer is allocated and sized correctly
    private fun ensureByteBuffer(floatArraySize: Int) {
        val neededCapacity = floatArraySize * 4 // 4 bytes per float
        if (nhwcBuffer == null || nhwcBuffer!!.capacity() < neededCapacity) {
            Log.d(TAG, "Allocating new ByteBuffer with capacity: $neededCapacity")
            nhwcBuffer = ByteBuffer.allocateDirect(neededCapacity).order(ByteOrder.nativeOrder())
        }
    }

    // YUV420 -> RGB Bitmap (NV21 JPEG path; acceptable for baseline)
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    // Fill NHWC float array in [0,1]
    private fun bitmapToNhwcFloat(bmp: Bitmap, dest: FloatArray) {
        val w = bmp.width; val h = bmp.height
        val pixels = IntArray(w * h)
        bmp.getPixels(pixels, 0, w, 0, 0, w, h)
        var idx = 0
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                val c = pixels[row + x]
                dest[idx++] = ((c shr 16) and 0xFF) / 255f // R
                dest[idx++] = ((c shr 8) and 0xFF) / 255f  // G
                dest[idx++] = (c and 0xFF) / 255f          // B
            }
        }
    }

    private fun rgbBitmapToCHW(bmp: Bitmap): FloatArray {
        val w = bmp.width; val h = bmp.height
        val out = FloatArray(3 * w * h)
        val pixels = IntArray(w * h)
        bmp.getPixels(pixels, 0, w, 0, 0, w, h)
        val plane = w * h
        for (i in 0 until w * h) {
            val c = pixels[i]
            val r = ((c shr 16) and 0xFF) / 255f
            val g = ((c shr 8) and 0xFF) / 255f
            val b = (c and 0xFF) / 255f
            out[i] = r
            out[i + plane] = g
            out[i + 2 * plane] = b
        }
        return out
    }

    private fun nhwcToCHW(nhwc: Array<Array<Array<FloatArray>>>): FloatArray {
        val h = nhwc[0].size
        val w = nhwc[0][0].size
        val out = FloatArray(3 * w * h)
        val plane = w * h
        var idx = 0
        for (y in 0 until h) {
            for (x in 0 until w) {
                val p = nhwc[0][y][x]
                val r = p[0]; val g = p[1]; val b = p[2]
                val flat = y * w + x
                out[flat] = r
                out[flat + plane] = g
                out[flat + 2 * plane] = b
                idx += 3
            }
        }
        return out
    }

    private fun nhwcToCHWSingleChannel(nhwc: Array<Array<Array<FloatArray>>>): FloatArray {
        val h = nhwc[0].size
        val w = nhwc[0][0].size
        val out = FloatArray(w * h)
        var idx = 0
        for (y in 0 until h) {
            for (x in 0 until w) {
                val p = nhwc[0][y][x]
                out[idx++] = p[0] // Single channel
            }
        }
        return out
    }

    private fun expandSingleChannelToRGB(singleChannel: FloatArray, w: Int, h: Int): FloatArray {
        val rgb = FloatArray(3 * w * h)
        val plane = w * h
        for (i in 0 until w * h) {
            rgb[i] = singleChannel[i]
            rgb[i + plane] = singleChannel[i]
            rgb[i + 2 * plane] = singleChannel[i]
        }
        return rgb
    }

    private fun chwToBitmap(chw: FloatArray, w: Int, h: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val plane = w * h
        val pixels = IntArray(w * h)
        for (i in 0 until w * h) {
            val r = (chw[i].coerceIn(0f, 1f) * 255f + 0.5f).toInt()
            val g = (chw[i + plane].coerceIn(0f, 1f) * 255f + 0.5f).toInt()
            val b = (chw[i + 2 * plane].coerceIn(0f, 1f) * 255f + 0.5f).toInt()
            pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        return bmp
    }

    private fun processOutputs(L: FloatArray, R: FloatArray, X: FloatArray, scaledBmp: Bitmap, w: Int, h: Int, ms: Double, frameCount: Int) {
        // inp CHW from bitmap (matches Python script: (1,3,H,W))
        val inpCHW = rgbBitmapToCHW(scaledBmp)

        // D = inp - X (exactly like Python script)
        val D = FloatArray(3 * h * w)
        for (i in D.indices) D[i] = (inpCHW[i] - X[i])

        // I = L^0.2 * R (exactly like Python script: np.power(L, 0.2) * R)
        val I = FloatArray(3 * h * w)
        for (i in 0 until 3 * h * w) {
            val l = L[i].coerceIn(0f, 1f)
            val r = R[i]
            I[i] = l.toDouble().pow(0.2).toFloat() * r
//            I[i] = r
        }
        Log.d(TAG, "Frame $frameCount: Post-processing completed")

        // Convert I to Bitmap for display
        val enhanced = chwToBitmap(I, w, h)
        Log.d(TAG, "Frame $frameCount: Enhanced bitmap created")

        // FPS update
        val now = System.nanoTime()
        val fps = 1e9 / (now - lastTs.getAndSet(now)).coerceAtLeast(1L).toDouble()

        _ui.value = _ui.value.copy(
            enhancedBitmap = enhanced,
            lastMs = ms,
            fps = fps
        )
        Log.d(TAG, "Frame $frameCount: UI updated, FPS: ${"%.1f".format(fps)}, Inference: ${"%.1f".format(ms)}ms")
    }

    companion object {
        private const val TAG = "PairLIEViewModel"
    }
}