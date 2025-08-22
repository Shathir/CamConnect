# TensorFlow Lite Image Enhancement Module

This module provides real-time image enhancement capabilities using TensorFlow Lite models. It supports two different enhancement algorithms that can be switched between dynamically.

## Supported Models

### 1. PairLIE (Pair Learning for Image Enhancement)
- **File**: `PairLIEViewModel.kt`
- **Model**: `pairlie_float32.tflite`
- **Algorithm**: Decomposes images into illumination (L), reflectance (R), and noise (X) components
- **Enhancement Formula**: `I = L^0.2 * R`
- **Input**: Dynamic size, normalized to [0, 1]
- **Outputs**: 3 tensors (L, R, X) with mixed channel support
- **Performance**: Optimized for real-time processing with NNAPI acceleration

### 2. Zero-DCE (Zero-Reference Deep Curve Estimation)
- **File**: `ZeroDCEViewModel.kt`  
- **Model**: `zero_dce.tflite` (⚠️ **Required** - not included)
- **Algorithm**: Direct curve estimation for low-light enhancement
- **Input**: Fixed 400x600 size, normalized to [-1, 1] range
- **Output**: Single enhanced image tensor
- **Performance**: Advanced enhancement with hardware acceleration support

## Architecture

### ViewModels
Both ViewModels follow the same architectural pattern:

```kotlin
// UI State Management
data class UIState(
    val cameraGranted: Boolean = false,
    val enhancedBitmap: Bitmap? = null,
    val lastMs: Double = 0.0,
    val fps: Double = 0.0
)

// Core Methods
fun onCameraPermission(granted: Boolean)
fun onFrame(image: ImageProxy) // Real-time processing
```

### Key Features
- **Frame Skipping**: Prevents processing backlog by skipping frames when busy
- **Memory Management**: Reusable buffers and proper bitmap recycling
- **Hardware Acceleration**: NNAPI delegate with CPU fallback
- **Performance Monitoring**: Real-time FPS and inference time tracking
- **Detailed Logging**: Comprehensive debug information

## UI Components

### Main Screen (`tfliteScreen.kt`)
- **Model Toggle**: Switch between PairLIE and Zero-DCE
- **Live Preview**: Real-time camera feed with enhancement overlay
- **Performance Metrics**: FPS and inference time display
- **Status Indicators**: Model loading and processing status

### Individual Screens
- `PairLIEScreen.kt` - Dedicated PairLIE interface
- `ZeroDCEScreen.kt` - Dedicated Zero-DCE interface

### AI Configuration (`AiLayout.kt`)
- Model selection buttons in the AI settings panel
- Descriptive text for each model
- Integration with existing AI configuration system

## Usage

### Switching Models
```kotlin
// In UI
var selectedModel by remember { mutableStateOf("PairLIE") }

// Toggle between models
when (selectedModel) {
    "PairLIE" -> pairLIEViewModel.onFrame(imageProxy)
    "ZeroDCE" -> zeroDCEViewModel.onFrame(imageProxy)
}
```

### Adding Zero-DCE Model
1. Obtain a Zero-DCE TensorFlow Lite model
2. Name it `zero_dce.tflite`
3. Place in `app/src/main/assets/`
4. Ensure input shape: [1, 400, 600, 3]
5. Ensure output shape: [1, 400, 600, 3]

## Technical Details

### Image Processing Pipeline
1. **YUV → RGB Conversion**: Camera ImageProxy to Bitmap
2. **Scaling**: Resize to model requirements
3. **Normalization**: Convert to model input range
4. **Inference**: TensorFlow Lite model execution
5. **Post-processing**: Convert output to displayable Bitmap

### Performance Optimizations
- **NNAPI Acceleration**: Hardware NPU/DSP when available
- **Multi-threading**: CPU thread optimization
- **Memory Reuse**: Pre-allocated buffers
- **Frame Management**: Skip processing when overloaded

### Error Handling
- Graceful NNAPI fallback to CPU
- Model loading error recovery
- Frame processing error isolation
- Comprehensive logging for debugging

## Integration Points

The module integrates with:
- **Camera System**: Real-time frame processing
- **UI System**: Compose-based interfaces
- **AI Configuration**: Settings panel integration
- **Performance Monitoring**: FPS and timing metrics
- **Permission System**: Camera access management

## Future Enhancements

Potential improvements:
- Model download and caching
- Multiple model support
- Custom model loading
- Performance profiling tools
- Batch processing capabilities 