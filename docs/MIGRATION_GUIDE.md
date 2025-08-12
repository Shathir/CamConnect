# Migration Guide: From Data.java to CameraConfigurationManager

## Overview

This guide outlines the migration from the legacy `Data.java` class to the modern `CameraConfigurationManager.kt` for camera configuration management.

## Why Migrate?

### Problems with Data.java:
- ❌ **Thread Safety**: Static mutable state without synchronization
- ❌ **UI Blocking**: Synchronous file I/O operations
- ❌ **Error Handling**: Poor exception handling with generic printStackTrace()
- ❌ **Testing**: Difficult to unit test due to static state
- ❌ **Architecture**: Violates modern Android architecture principles

### Benefits of CameraConfigurationManager.kt:
- ✅ **Thread Safe**: Uses ReentrantReadWriteLock for safe concurrent access
- ✅ **Async Operations**: Non-blocking coroutine-based file operations
- ✅ **Error Handling**: Proper Result types with detailed error information
- ✅ **Testable**: Easier to mock and test
- ✅ **Modern**: Follows Android architecture best practices

## Migration Steps

### 1. Replace Direct Data.java Usage

**Old Code (Data.java):**
```java
// Getting values
boolean odEnabled = Data.isOD();
boolean farEnabled = Data.isFAR();
boolean dsEnabled = Data.isDS();

// Setting values
Data.setOD(context, true);
Data.setFAR(context, false);
Data.setDS(context, true);
```

**New Code (CameraConfigurationManager.kt):**
```kotlin
// Getting values
val odEnabled = CameraConfigurationManager.isObjectDetectionEnabled()
val farEnabled = CameraConfigurationManager.isFarDetectionEnabled()
val dsEnabled = CameraConfigurationManager.isDepthSensingEnabled()

// Setting values (async)
lifecycleScope.launch {
    CameraConfigurationManager.setObjectDetectionEnabled(context, true)
    CameraConfigurationManager.setFarDetectionEnabled(context, false)
    CameraConfigurationManager.setDepthSensingEnabled(context, true)
}
```

### 2. Update UI Components

**Old Pattern:**
```kotlin
@Composable
fun MyComponent() {
    var enabled by remember { mutableStateOf(Data.isOD()) }
    
    // Direct state management
    Switch(
        checked = enabled,
        onCheckedChange = { 
            enabled = it
            Data.setOD(LocalContext.current, it)
        }
    )
}
```

**New Pattern with ViewModel:**
```kotlin
@Composable
fun MyComponent(viewModel: AiConfigurationViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadConfiguration(LocalContext.current)
    }
    
    Switch(
        checked = uiState.od,
        onCheckedChange = { viewModel.updateOD(it) }
    )
}
```

### 3. Mapping Between Old and New Properties

| Data.java | CameraConfigurationManager.kt | Description |
|-----------|-------------------------------|-------------|
| `FAR` | `farDetectionEnabled` | Detect Far Away Objects |
| `OD` | `objectDetectionEnabled` | Object Detection |
| `DS` | `depthSensingEnabled` | Depth Sensing |
| `AUDIO` | `audioEnabled` | Audio Features |
| `MODEL` | `modelVersion` | AI Model Version |
| `DS_THRESHOLD` | `depthSensingThreshold` | Depth Sensing Threshold |

### 4. Update MainActivitySingleton.kt

**Before:**
```kotlin
fun getOD(): Boolean {
    return Data.isOD()
}

fun setOD(od: Boolean) {
    Data.setOD(mainActivity, od)
}
```

**After:**
```kotlin
fun getOD(): Boolean {
    return CameraConfigurationManager.isObjectDetectionEnabled()
}

suspend fun setOD(od: Boolean): Result<Unit> {
    return CameraConfigurationManager.setObjectDetectionEnabled(mainActivity, od)
}
```

### 5. Configuration Loading

**Old (Synchronous):**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Data.loadData(this) // Blocks UI thread
}
```

**New (Asynchronous):**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    lifecycleScope.launch {
        val result = CameraConfigurationManager.loadConfigurationAsync(this@MainActivity)
        result.fold(
            onSuccess = { config ->
                // Handle successful load
            },
            onFailure = { exception ->
                // Handle error
            }
        )
    }
}
```

## Implementation Checklist

- [ ] Create `AiConfigurationViewModel` for UI state management
- [ ] Update `AiLayout.kt` to use the new ViewModel
- [ ] Replace `Data.java` usage in `MainActivitySingleton.kt`
- [ ] Update any native code interfaces if needed
- [ ] Add error handling for configuration operations
- [ ] Test configuration persistence across app restarts
- [ ] Remove `Data.java` after all references are updated

## Testing Strategy

1. **Unit Tests**: Test ViewModel state management
2. **Integration Tests**: Test configuration persistence
3. **UI Tests**: Test user interactions with new UI
4. **Migration Tests**: Ensure old config files are properly migrated

## Backward Compatibility

The `CameraConfigurationManager` includes deprecated methods that map to the old `Data.java` API:

```kotlin
@Deprecated("Use isObjectDetectionEnabled() instead")
fun isOD(): Boolean = isObjectDetectionEnabled()

@Deprecated("Use isFarDetectionEnabled() instead") 
fun isFAR(): Boolean = isFarDetectionEnabled()
```

This allows for gradual migration without breaking existing code.

## Final Steps

1. **Phase 1**: Introduce new ViewModel and update UI components
2. **Phase 2**: Update business logic to use CameraConfigurationManager
3. **Phase 3**: Remove deprecated Data.java class
4. **Phase 4**: Clean up backward compatibility methods

## Benefits After Migration

- **Better UX**: Non-blocking UI operations
- **Reliability**: Thread-safe configuration management
- **Maintainability**: Clean, testable code architecture
- **Error Handling**: Proper error states and recovery
- **Modern Architecture**: Follows Android best practices 