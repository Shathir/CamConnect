# CamConnect UI Layouts Documentation

## Overview

CamConnect is a sophisticated Android camera streaming application that implements an adaptive UI system with three distinct layout modes. The application provides real-time camera streaming, recording capabilities, and comprehensive device control through a responsive interface that adapts to different device types and user preferences.

## Architecture Overview

The UI architecture follows a modular design pattern with clear separation of concerns:

```
ui/
├── layouts/           # Main layout containers
├── components/        # Reusable UI components
├── models/           # Data models and state management
├── theme/            # Material Design theming
└── viewmodels/       # ViewModels for state management
```

## Layout System

### Adaptive Layout Container

The `AdaptiveStreamLayout` serves as the main container that manages transitions between three distinct layout modes:

#### Layout Modes

1. **Minimal Control Layout (90/10 split)**
   - **Left Pane (90%)**: Live camera stream display
   - **Right Pane (10%)**: Compact controls and status indicators
   - **Purpose**: Maximum viewing area for immersive camera streaming

2. **Expanded Control Layout (60/40 split)**
   - **Left Pane (60%)**: Live camera stream display
   - **Right Pane (40%)**: Dynamic and scrollable control options
   - **Purpose**: Intermediate control layout with quick access to common features

3. **Full Control Layout (45/55 split)**
   - **Left Pane (45%)**: Live camera stream display
   - **Right Pane (55%)**: In-depth device/camera configuration
   - **Purpose**: Comprehensive settings and configuration interface

### Implementation Details

```kotlin
@Composable
fun AdaptiveStreamLayout(
    modifier: Modifier = Modifier,
    context: Context
) {
    var layoutMode by rememberSaveable { mutableStateOf(LayoutMode.MINIMAL_CONTROL) }
    
    // Animated weights for smooth transitions
    val leftPaneWeight by animateFloatAsState(
        targetValue = when (layoutMode) {
            LayoutMode.MINIMAL_CONTROL -> 0.9f
            LayoutMode.EXPANDED_CONTROL -> 0.6f
            LayoutMode.FULL_CONTROL -> 0.3f
        },
        animationSpec = tween(durationMillis = 300)
    )
}
```

## Layout Components

### 1. Minimal Control Layout (`MinimalControlLayout.kt`)

The minimal control layout provides essential controls in a compact vertical arrangement.

#### Key Features:
- **Compact Button Design**: Square-shaped buttons optimized for minimal space
- **Essential Controls**: Settings, camera switch, recording toggle
- **Status Indicators**: Battery, WiFi, AI status in compact format
- **Screen Recording**: Integrated screen recording functionality

#### Implementation Example:

```kotlin
@Composable
fun MinimalControlContent(
    cameraState: CameraState,
    customButtons: List<ButtonConfig>,
    systemStatus: SystemStatus,
    onSettingsClick: () -> Unit,
    onCameraSwitch: () -> Unit,
    onRecordingToggle: () -> Unit,
    onExpandClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Settings button
        CustomizableButton(
            config = ButtonConfig(
                id = "settings",
                iconPlaceholder = R.drawable.sliders_horizontal.toString(),
                color = MediumGray,
                text = "Settings",
                backgroundColor = MediumDarkBackground,
                onClick = onSettingsClick
            ),
            isCompact = true,
            showText = false
        )
        
        // Additional controls...
    }
}
```

### 2. Expanded Control Layout (`ExpandedControlLayout.kt`)

The expanded control layout provides comprehensive control options with scrollable content.

#### Key Features:
- **Scout Header**: Branded header with logo
- **Control Button Rows**: Organized button groups for different functions
- **Recording Interface**: Advanced recording controls with state management
- **Zoom Selector**: Interactive zoom level selection
- **Toggle Icons**: Quick access to camera features (EIS, HDR, Auto Day/Night)
- **Status Indicators**: Comprehensive system status display

#### Component Structure:

```kotlin
@Composable
fun ExpandedControlContent(
    cameraState: CameraState,
    systemStatus: SystemStatus,
    customButtons: List<ButtonConfig>,
    toggleableIcons: List<ToggleableIcon>,
    buttonStates: MutableMap<String, Boolean>,
    // ... other parameters
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth > 600.dp
        val padding = if (deviceType == DeviceType.TABLET) 32.dp else 12.dp
        
        // Adaptive layout based on device type
        val layoutDirection: @Composable (@Composable () -> Unit) -> Unit =
            if (deviceType == DeviceType.TABLET) { 
                content -> Row(modifier = layoutModifier) { content() } 
            } else { 
                content -> Column(modifier = layoutModifier.verticalScroll(rememberScrollState())) { content() } 
            }
    }
}
```

### 3. Full Control Layout (`FullControlLayout.kt`)

The full control layout provides comprehensive device and camera configuration options.

#### Key Features:
- **Tab Navigation**: Camera Control, AI Control, License Control
- **Settings Sections**: Display, detection, and image settings
- **Advanced Configuration**: Detailed camera and device settings
- **Responsive Design**: Adapts to tablet and phone layouts

#### Implementation:

```kotlin
@Composable
fun SettingsControlLayout(
    selectedTab: ControlTab,
    onTabSelected: (ControlTab) -> Unit,
    onSystemStatusChange: (SystemStatus) -> Unit,
    systemStatus: SystemStatus,
    modifier: Modifier = Modifier,
    onCollapseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxSize()
            .background(Color.Transparent),
        verticalArrangement = Arrangement.spacedBy(if(deviceType == DeviceType.TABLET) 24.dp else 12.dp)
    ) {
        // Tab switcher
        ControlTabSwitcher(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.weight(1f)
        )
        
        // Tab content
        when (selectedTab) {
            ControlTab.CAMERA_CONTROL -> CameraLayout()
            ControlTab.AI_CONTROL -> AiLayout(systemStatus, onSystemStatusChange)
            ControlTab.LICENSE_CONTROL -> LicenseLayout()
        }
    }
}
```

## Core Components

### CustomizableButton

A flexible button component that supports various configurations and layouts.

#### Features:
- **Icon and Text Support**: Configurable icon and text display
- **Theme Awareness**: Automatic dark/light theme adaptation
- **Device Responsive**: Different sizes for tablet and phone
- **State Management**: Support for enabled/disabled states
- **Layout Options**: Row and column layout support

#### Configuration:

```kotlin
data class ButtonConfig(
    val id: String,
    val iconPlaceholder: String = "", // Drawable resource ID as string
    val text: String = "",
    val color: Color = Color.White,
    val backgroundColor: Color = DefaultColors.BluePrimary,
    val enabled: Boolean = true,
    val onClick: () -> Unit = {},
    val BorderColor: Color = Color.White
)
```

#### Usage Example:

```kotlin
CustomizableButton(
    config = ButtonConfig(
        id = "settings",
        iconPlaceholder = R.drawable.sliders_horizontal.toString(),
        color = MediumGray,
        text = "Settings",
        backgroundColor = MediumDarkBackground,
        onClick = onSettingsClick
    ),
    isCompact = true,
    showText = false
)
```

### Status Indicators

Comprehensive status monitoring components for system information.

#### BatteryIndicator

Real-time battery level monitoring with visual feedback.

```kotlin
@Composable
fun BatteryIndicator(
    batteryLevel: Int = -1,
    modifier: Modifier = Modifier,
    showPercentage: Boolean = true
) {
    val context = LocalContext.current
    var currentBatteryLevel by remember { mutableStateOf(batteryLevel) }
    
    // BroadcastReceiver for real-time battery updates
    DisposableEffect(context) {
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Battery level calculation logic
            }
        }
        
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
        
        onDispose {
            context.unregisterReceiver(batteryReceiver)
        }
    }
}
```

#### WifiIndicator

WiFi connectivity status with real-time updates.

```kotlin
@Composable
fun WifiIndicator(
    isConnected: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var wifiStatus by remember { mutableStateOf(isConnected) }
    
    // WiFi status monitoring implementation
    DisposableEffect(context) {
        val wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // WiFi status update logic
            }
        }
        
        val filter = IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        context.registerReceiver(wifiReceiver, filter)
        
        onDispose {
            context.unregisterReceiver(wifiReceiver)
        }
    }
}
```

### Recording Components

Advanced recording functionality with state management.

#### RecordingButton

Animated recording button with state transitions.

```kotlin
@Composable
fun RecordingButton(
    isRecording: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier
            .scale(if (isRecording) scale else 1f)
            .clickable { onToggle() }
    ) {
        // Recording button content
    }
}
```

### Zoom Selector

Interactive zoom level selection with smooth animations.

```kotlin
@Composable
fun ZoomSelector(
    initialZoom: Float,
    onZoomChanged: (Float) -> Unit
) {
    var currentZoom by remember { mutableFloatStateOf(initialZoom) }
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(zoomLevels) { zoomLevel ->
            ZoomLevelButton(
                zoom = zoomLevel,
                isSelected = currentZoom == zoomLevel,
                onClick = {
                    currentZoom = zoomLevel
                    onZoomChanged(zoomLevel)
                }
            )
        }
    }
}
```

## State Management

### ViewModels

The application uses multiple ViewModels for state management:

#### RecordingViewModel

Manages recording state and functionality.

```kotlin
class RecordingViewModel : ViewModel() {
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.NotRecording)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    fun toggleRecording(context: Context) {
        // Recording toggle logic
    }
}
```

#### CameraControlViewModel

Manages camera control states and operations.

```kotlin
class CameraControlViewModel : ViewModel() {
    private val _cameraControlState = MutableStateFlow(CameraControlState())
    val cameraControlState: StateFlow<CameraControlState> = _cameraControlState.asStateFlow()
    
    fun setZoom(zoom: Float) {
        // Zoom setting logic
    }
    
    fun toggleIR() {
        // IR toggle logic
    }
}
```

### Data Models

#### CameraState

Represents the current state of the camera system.

```kotlin
data class CameraState(
    val isRecording: Boolean = false,
    val currentCamera: Int = 0,
    val zoomLevel: Float = 1f,
    val cameraMode: CameraMode = CameraMode.NORMAL,
    val visionMode: VisionMode = VisionMode.VISION,
    val orientation: Orientation = Orientation.PORTRAIT,
    val autoDayNight: Boolean = false
)
```

#### SystemStatus

Represents system-wide status information.

```kotlin
data class SystemStatus(
    val batteryLevel: Int = 100,
    val isWifiConnected: Boolean = false,
    val isLteConnected: Boolean = false,
    val isOnline: Boolean = false,
    val isAiEnabled: Boolean = false,
    val currentSpeed: Float = 0f,
    val compassDirection: Float = 0f
)
```

## Theme System

### Material Design 3 Integration

The application implements Material Design 3 theming with custom color schemes.

#### Color Definitions:

```kotlin
object AppColors {
    val ButtonBgColor = Color(0xFF1E1E1E)
    val ButtonBorderColor = Color(0xFF333333)
    val ButtonIconColor = Color(0xFFC5C5C5)
    val ButtonSelectedBgColor = Color(0xFF007AFF)
    val ButtonSelectedIconColor = Color.White
    
    val RecordRed = Color(0xFFE74C3C)
    val BatteryGreen = Color(0xFF2ECC71)
    val BatteryRed = Color(0xFFE74C3C)
}
```

#### Theme Implementation:

```kotlin
@Composable
fun CamConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

## Device Adaptation

### Responsive Design

The application adapts to different device types and screen sizes.

#### Device Type Detection:

```kotlin
@Composable
fun rememberDeviceType(): DeviceType {
    val configuration = LocalConfiguration.current
    return if (configuration.screenWidthDp >= 600) {
        DeviceType.TABLET
    } else {
        DeviceType.PHONE
    }
}
```

#### Adaptive Layouts:

```kotlin
val layoutModifier = Modifier
    .fillMaxSize()
    .padding(horizontal = if (deviceType == DeviceType.TABLET) 32.dp else 12.dp)

val buttonSize = if (deviceType == DeviceType.TABLET) 76.dp else 48.dp
```

## Animation System

### Smooth Transitions

The application implements smooth animated transitions between layout modes.

#### Layout Transitions:

```kotlin
val leftPaneWeight by animateFloatAsState(
    targetValue = when (layoutMode) {
        LayoutMode.MINIMAL_CONTROL -> 0.9f
        LayoutMode.EXPANDED_CONTROL -> 0.6f
        LayoutMode.FULL_CONTROL -> 0.3f
    },
    animationSpec = tween(durationMillis = 300)
)
```

#### Content Transitions:

```kotlin
AnimatedContent(
    targetState = recordingState,
    transitionSpec = {
        fadeIn(animationSpec = tween(300)) with
        fadeOut(animationSpec = tween(300))
    }
) { state ->
    // Content based on recording state
}
```

## Performance Considerations

### Memory Management

The application implements proper memory management practices:

```kotlin
DisposableEffect(Unit) {
    Log.d("Component", "Component created")
    onDispose {
        Log.d("Component", "Component disposed - cleaning up")
        try {
            MemoryManager.cleanupWeakReferences()
        } catch (e: Exception) {
            Log.e("Component", "Error during cleanup", e)
        }
    }
}
```

### State Optimization

- Use of `rememberSaveable` for persistent state
- Efficient state flow management
- Proper disposal of resources and listeners

## Usage Examples

### Basic Implementation

```kotlin
@Composable
fun MainScreen() {
    CamConnectTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AdaptiveStreamLayout(context = LocalContext.current)
        }
    }
}
```

### Custom Button Configuration

```kotlin
val customButtons = listOf(
    ButtonConfig(
        id = "custom-action",
        iconPlaceholder = R.drawable.custom_icon.toString(),
        text = "Custom Action",
        backgroundColor = Color(0xFF007AFF),
        onClick = { /* Custom action */ }
    )
)
```

## Best Practices

1. **State Management**: Use ViewModels for complex state management
2. **Memory Management**: Implement proper cleanup in DisposableEffect
3. **Responsive Design**: Always consider tablet and phone layouts
4. **Theme Awareness**: Support both dark and light themes
5. **Animation**: Use smooth transitions for better UX
6. **Error Handling**: Implement proper error handling and logging

## Future Enhancements

- Gesture-based layout transitions
- Custom button configuration UI
- Advanced animation effects
- Accessibility improvements
- Performance optimizations
- Additional device type support

---

**Document Version**: 1.0  
**Last Updated**: 15-07-2025  
**Author**: Shathir
**Status**: Complete 
**License**: Apache 2.0 