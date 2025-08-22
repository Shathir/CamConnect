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
import android.util.Log

data class ZeroDCEUIState(
    val cameraGranted: Boolean = false,
    val enhancedBitmap: Bitmap? = null,
    val lastMs: Double = 0.0,
    val fps: Double = 0.0
)

class ZeroDCEViewModel(app: Application) : AndroidViewModel(app) {

    private val _ui = MutableStateFlow(ZeroDCEUIState())
    val uiState = _ui.asStateFlow()

    // Frame processing control
    private var isProcessingFrame = false
    private var frameCount = 0
    private val maxConcurrentFrames = 1 // Only process one frame at a time

    // Model constants (from original Zero-DCE implementation)
    private val BATCH_SIZE = 1
    private val PIXEL_SIZE = 3
    private val IMAGE_MEAN = 128
    private val IMAGE_STD = 128.0f
    private val TARGET_WIDTH = 400
    private val TARGET_HEIGHT = 600

    private val interpreter: Interpreter by lazy {
        try {
            Log.d(TAG, "Loading Zero-DCE model: zero_dce.tflite")
            val model = FileUtil.loadMappedFile(getApplication(), "zero_dce.tflite")
            Log.d(TAG, "Zero-DCE model loaded successfully, size: ${model.remaining()} bytes")
            
            // Check device information for NNAPI compatibility
            Log.d(TAG, "🔍 Zero-DCE Device hardware debugging:")
            Log.d(TAG, "  - Device model: ${android.os.Build.MODEL}")
            Log.d(TAG, "  - Android version: ${android.os.Build.VERSION.RELEASE}")
            Log.d(TAG, "  - API level: ${android.os.Build.VERSION.SDK_INT}")
            Log.d(TAG, "  - Attempting NNAPI acceleration (NPU/DSP)")
            
            Log.d(TAG, "Creating Zero-DCE interpreter with NNAPI + ${Runtime.getRuntime().availableProcessors().coerceAtMost(4)} CPU threads")
            
            val opts = Interpreter.Options().apply {
                // Try NNAPI delegate for hardware acceleration
                try {
                    Log.d(TAG, "🚀 Zero-DCE: Attempting to create NNAPI delegate...")
                    val nnApiDelegate = NnApiDelegate()
                    addDelegate(nnApiDelegate)
                    Log.d(TAG, "✅ Zero-DCE: NNAPI delegate added successfully - using NPU/DSP acceleration")
                } catch (e: Exception) {
                    Log.w(TAG, "❌ Zero-DCE: NNAPI delegate failed, falling back to CPU", e)
                    Log.w(TAG, "Zero-DCE NNAPI Error details: ${e.javaClass.simpleName}: ${e.message}")
                    // Continue with CPU - don't add any delegate
                }
                
                // Set CPU threads (used as fallback or alongside NNAPI)
                setNumThreads(Runtime.getRuntime().availableProcessors().coerceAtMost(4))
            }
            
            val interp = Interpreter(model, opts)
            Log.d(TAG, "Zero-DCE interpreter created successfully")
            Log.d(TAG, "Zero-DCE input tensor count: ${interp.inputTensorCount}")
            Log.d(TAG, "Zero-DCE output tensor count: ${interp.outputTensorCount}")
            
            // Log input/output tensor info
            for (i in 0 until interp.inputTensorCount) {
                val inputTensor = interp.getInputTensor(i)
                Log.d(TAG, "Zero-DCE Input $i: name='${inputTensor.name()}', shape=${inputTensor.shape().contentToString()}, dataType=${inputTensor.dataType()}")
            }
            
            for (i in 0 until interp.outputTensorCount) {
                val outputTensor = interp.getOutputTensor(i)
                Log.d(TAG, "Zero-DCE Output $i: name='${outputTensor.name()}', shape=${outputTensor.shape().contentToString()}, dataType=${outputTensor.dataType()}")
            }
            
            interp
        } catch (e: Exception) {
            Log.e(TAG, "Error loading Zero-DCE model", e)
            throw e
        }
    }

    // Reusable buffers (avoid GC) - pre-allocated for efficiency
    private var inputBuffer: ByteBuffer? = null

    // FPS tracking
    private val lastTs = AtomicLong(System.nanoTime())

    fun onCameraPermission(granted: Boolean) {
        Log.d(TAG, "Zero-DCE Camera permission: $granted")
        _ui.value = _ui.value.copy(cameraGranted = granted)
    }

    fun onFrame(image: ImageProxy) {
        // Skip frame if already processing one
        if (isProcessingFrame) {
            Log.d(TAG, "Zero-DCE: Skipping frame - already processing")
            image.close()
            return
        }
        
        frameCount++
        val frameStartTime = System.currentTimeMillis()
        Log.d(TAG, "🎬 Zero-DCE Frame $frameCount START at ${java.text.SimpleDateFormat("HH:mm:ss.SSS").format(java.util.Date(frameStartTime))}")
        Log.d(TAG, "Zero-DCE Processing frame $frameCount, size: ${image.width}x${image.height}")
        
        isProcessingFrame = true
        
        // Run off main thread
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val step1 = System.currentTimeMillis()
                Log.d(TAG, "⏱️ Zero-DCE Frame $frameCount: Step 1 - Image conversion started")
                
                // Convert YUV -> RGB Bitmap
                val originalBmp = imageProxyToBitmap(image)
                image.close()
                
                val step2 = System.currentTimeMillis()
                Log.d(TAG, "⏱️ Zero-DCE Frame $frameCount: Step 2 - Image conversion done (${step2 - step1}ms)")
                
                // Scale to Zero-DCE input size (400x600) - maintaining aspect ratio
                val scaledBmp = Bitmap.createScaledBitmap(originalBmp, TARGET_WIDTH, TARGET_HEIGHT, true)
                originalBmp.recycle() // Free original bitmap
                Log.d(TAG, "Zero-DCE Frame $frameCount: Scaled to ${scaledBmp.width}x${scaledBmp.height}")
                
                val step3 = System.currentTimeMillis()
                Log.d(TAG, "⏱️ Zero-DCE Frame $frameCount: Step 3 - Scaling done (${step3 - step2}ms)")

                // Convert bitmap to ByteBuffer with Zero-DCE normalization
                val buffer = convertBitmapToByteBuffer(scaledBmp)
                
                val step4 = System.currentTimeMillis()
                Log.d(TAG, "⏱️ Zero-DCE Frame $frameCount: Step 4 - Input buffer prep (${step4 - step3}ms)")

                val t0 = System.nanoTime()
                val inferenceStartTime = System.currentTimeMillis()
                Log.d(TAG, "🚀 Zero-DCE Frame $frameCount: INFERENCE STARTING at ${java.text.SimpleDateFormat("HH:mm:ss.SSS").format(java.util.Date(inferenceStartTime))}")
                
                try {
                    // Run inference using standard TensorFlow Lite API
                    val inferenceStart = System.nanoTime()
                    
                    // Prepare input and output tensors
                    interpreter.resizeInput(0, intArrayOf(BATCH_SIZE, TARGET_WIDTH, TARGET_HEIGHT, PIXEL_SIZE))
                    interpreter.allocateTensors()
                    
                    // Prepare output array (NHWC format)
                    val output = Array(BATCH_SIZE) { Array(TARGET_WIDTH) { Array(TARGET_HEIGHT) { FloatArray(PIXEL_SIZE) } } }
                    
                    // Run inference with ByteBuffer input and Array output
                    interpreter.run(buffer, output)
                    
                    val inferenceEnd = System.nanoTime()
                    
                    val t1 = System.nanoTime()
                    val totalMs = (t1 - t0) / 1e6
                    val inferenceMs = (inferenceEnd - inferenceStart) / 1e6
                    val inferenceEndTime = System.currentTimeMillis()
                    Log.d(TAG, "✅ Zero-DCE Frame $frameCount: INFERENCE COMPLETED at ${java.text.SimpleDateFormat("HH:mm:ss.SSS").format(java.util.Date(inferenceEndTime))}")
                    Log.d(TAG, "Zero-DCE Frame $frameCount: ⚡ INFERENCE TIME: ${inferenceMs}ms (Total: ${totalMs}ms)")

                    // Convert output array to bitmap
                    val enhancedBitmap = outputArrayToBitmap(output)
                    Log.d(TAG, "Zero-DCE Frame $frameCount: Enhanced bitmap created")
                    
                    // FPS update
                    val now = System.nanoTime()
                    val fps = 1e9 / (now - lastTs.getAndSet(now)).coerceAtLeast(1L).toDouble()

                    _ui.value = _ui.value.copy(
                        enhancedBitmap = enhancedBitmap,
                        lastMs = inferenceMs,
                        fps = fps
                    )
                    Log.d(TAG, "Zero-DCE Frame $frameCount: UI updated, FPS: ${"%.1f".format(fps)}, Inference: ${"%.1f".format(inferenceMs)}ms")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Zero-DCE Frame $frameCount: Inference failed", e)
                    throw e
                }
                
            } catch (e: Throwable) {
                Log.e(TAG, "Error processing Zero-DCE frame $frameCount", e)
                // In production, report/log
            } finally {
                isProcessingFrame = false
                val frameEndTime = System.currentTimeMillis()
                val totalFrameTime = frameEndTime - frameStartTime
                Log.d(TAG, "🏁 Zero-DCE Frame $frameCount COMPLETED at ${java.text.SimpleDateFormat("HH:mm:ss.SSS").format(java.util.Date(frameEndTime))} (Total: ${totalFrameTime}ms)")
            }
        }
    }

    // ---------- Helpers (adapted from original Zero-DCE implementation) ----------

    // YUV420 -> RGB Bitmap (same as PairLIE implementation)
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

    // Convert bitmap to ByteBuffer with Zero-DCE normalization
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * BATCH_SIZE * TARGET_WIDTH * TARGET_HEIGHT * PIXEL_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(TARGET_WIDTH * TARGET_HEIGHT)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until TARGET_WIDTH) {
            for (j in 0 until TARGET_HEIGHT) {
                val `val` = intValues[pixel++]
                // Zero-DCE normalization: (pixel - 128) / 128 to get [-1, 1] range
                byteBuffer.putFloat(((`val` shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD) // R
                byteBuffer.putFloat(((`val` shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)  // G
                byteBuffer.putFloat(((`val` and 0xFF) - IMAGE_MEAN) / IMAGE_STD)        // B
            }
        }
        return byteBuffer
    }

    // Convert output array to bitmap (replacing the ByteBuffer version)
    private fun outputArrayToBitmap(output: Array<Array<Array<FloatArray>>>): Bitmap {
        val bitmap = Bitmap.createBitmap(TARGET_WIDTH, TARGET_HEIGHT, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(TARGET_WIDTH * TARGET_HEIGHT)
        
        var pixelIndex = 0
        for (i in 0 until TARGET_WIDTH) {
            for (j in 0 until TARGET_HEIGHT) {
                val a = 0xFF
                // Zero-DCE outputs are typically in [0, 1] range, so multiply by 255
                val r = (output[0][i][j][0] * 255.0f).coerceIn(0f, 255f).toInt()
                val g = (output[0][i][j][1] * 255.0f).coerceIn(0f, 255f).toInt()
                val b = (output[0][i][j][2] * 255.0f).coerceIn(0f, 255f).toInt()
                
                pixels[pixelIndex++] = a shl 24 or (r shl 16) or (g shl 8) or b
            }
        }
        bitmap.setPixels(pixels, 0, TARGET_WIDTH, 0, 0, TARGET_WIDTH, TARGET_HEIGHT)
        return bitmap
    }

    // Convert output buffer back to bitmap (keeping for reference)
    private fun getOutputImage(output: ByteBuffer): Bitmap {
        output.rewind() // Rewind the output buffer after running

        val bitmap = Bitmap.createBitmap(TARGET_WIDTH, TARGET_HEIGHT, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(TARGET_WIDTH * TARGET_HEIGHT)
        for (i in 0 until TARGET_WIDTH * TARGET_HEIGHT) {
            val a = 0xFF
            // Zero-DCE outputs are typically in [0, 1] range, so multiply by 255
            val r: Float = output.float * 255.0f
            val g: Float = output.float * 255.0f
            val b: Float = output.float * 255.0f
            
            // Clamp values to [0, 255] range
            val rInt = r.coerceIn(0f, 255f).toInt()
            val gInt = g.coerceIn(0f, 255f).toInt()
            val bInt = b.coerceIn(0f, 255f).toInt()
            
            pixels[i] = a shl 24 or (rInt shl 16) or (gInt shl 8) or bInt
        }
        bitmap.setPixels(pixels, 0, TARGET_WIDTH, 0, 0, TARGET_WIDTH, TARGET_HEIGHT)

        return bitmap
    }

    companion object {
        private const val TAG = "ZeroDCEViewModel"
    }
} 