# CamConnect Component Architecture

## Overview

The CamConnect application implements a comprehensive component architecture that promotes reusability, maintainability, and consistency across the UI. This document outlines the component structure, design patterns, and implementation details.

## Component Hierarchy

```
ui/components/
├── buttons/           # Button components and configurations
├── camera/           # Camera-related UI components
├── controls/         # Control elements (recording, zoom, etc.)
├── indicators/       # Status indicators and monitoring
├── recording/        # Recording-specific components
├── settings/         # Settings and configuration components
└── login/           # Authentication components
```

## Button Components

### CustomizableButton

The `CustomizableButton` is the foundation of the application's interactive elements, providing a flexible and configurable button implementation.

#### Design Philosophy

- **Configurability**: Support for various button states and appearances
- **Responsiveness**: Automatic adaptation to device types (tablet/phone)
- **Theme Awareness**: Seamless integration with Material Design 3
- **Accessibility**: Built-in support for content descriptions and focus management

#### Implementation Details

```kotlin
@Composable
fun CustomizableButton(
    config: ButtonConfig,
    modifier: Modifier = Modifier,
    showText: Boolean = true,
    layout: String = "Row",
    isCompact: Boolean = false
) {
    val deviceType = rememberDeviceType()
    val isDarkTheme = isSystemInDarkTheme()
    
    // Adaptive sizing based on device type
    val buttonSize = if (isCompact) {
        if(deviceType == DeviceType.TABLET) 76.dp else 48.dp
    } else {
        if(deviceType == DeviceType.TABLET) 112.dp else 56.dp
    }
    
    Box(
        modifier = modifier.size(buttonSize)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(if(deviceType == DeviceType.TABLET) 20.dp else 14.dp))
                .background(if (config.enabled) config.backgroundColor else config.backgroundColor.copy(alpha = 0.5f))
                .border(
                    width = if (isDarkTheme) 0.dp else 1.dp,
                    color = config.BorderColor,
                    shape = RoundedCornerShape(if(deviceType == DeviceType.TABLET) 20.dp else 14.dp)
                )
                .clickable(enabled = config.enabled) { config.onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Content rendering logic
        }
    }
}
```

#### Configuration Model

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

#### Usage Patterns

**Compact Icon-Only Button:**
```kotlin
CustomizableButton(
    config = ButtonConfig(
        id = "settings",
        iconPlaceholder = R.drawable.sliders_horizontal.toString(),
        color = MediumGray,
        backgroundColor = MediumDarkBackground,
        onClick = onSettingsClick
    ),
    isCompact = true,
    showText = false
)
```

**Full Button with Text:**
```kotlin
CustomizableButton(
    config = ButtonConfig(
        id = "record",
        iconPlaceholder = R.drawable.record_icon.toString(),
        text = "RECORD",
        color = Color.White,
        backgroundColor = RecordRed,
        onClick = onRecordingToggle
    ),
    isCompact = false,
    showText = true
)
```

### Zoom Selector

The `ZoomSelector` component provides interactive zoom level selection with smooth animations and state management.

#### Features

- **Multiple Zoom Levels**: Support for 1x, 2x, 4x zoom levels
- **Smooth Animations**: Animated transitions between zoom states
- **State Persistence**: Maintains zoom state across layout changes
- **API Integration**: Direct integration with camera control APIs

#### Implementation

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

## Status Indicators

### BatteryIndicator

Real-time battery monitoring with visual feedback and automatic updates.

#### Implementation Features

- **BroadcastReceiver Integration**: Real-time battery level updates
- **Visual Feedback**: Color-coded battery levels (red, orange, green)
- **Icon Adaptation**: Different icons for battery states
- **Memory Management**: Proper cleanup of system resources

```kotlin
@Composable
fun BatteryIndicator(
    batteryLevel: Int = -1,
    modifier: Modifier = Modifier,
    showPercentage: Boolean = true
) {
    val context = LocalContext.current
    var currentBatteryLevel by remember { mutableStateOf(batteryLevel) }
    
    DisposableEffect(context) {
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        currentBatteryLevel = (level * 100 / scale)
                    }
                }
            }
        }
        
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
        
        onDispose {
            context.unregisterReceiver(batteryReceiver)
        }
    }
    
    // Visual rendering with color-coded feedback
    val batteryColor = when {
        displayBatteryLevel <= 20 -> BatteryRed
        displayBatteryLevel <= 70 -> Color(0xFFD08101)
        else -> BatteryGreen
    }
}
```

### WifiIndicator

WiFi connectivity monitoring with real-time status updates.

#### Features

- **Network State Monitoring**: Real-time WiFi connection status
- **Visual Indicators**: Clear visual representation of connection state
- **Permission Handling**: Proper permission management for network access
- **Fallback States**: Graceful handling of unavailable network information

```kotlin
@Composable
fun WifiIndicator(
    isConnected: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var wifiStatus by remember { mutableStateOf(isConnected) }
    
    DisposableEffect(context) {
        val wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val wifiManager = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
                wifiStatus = wifiManager.isWifiEnabled
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

### AI Status Indicator

AI functionality status monitoring with visual feedback.

```kotlin
@Composable
fun AiStatusIndicator(
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (isEnabled) Color(0xFF4CAF50) else Color(0xFF666666)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ai_icon),
            contentDescription = "AI Status",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}
```

## Recording Components

### Recording State Management

The recording system implements a comprehensive state management approach with multiple recording states.

#### Recording States

```kotlin
sealed class RecordingState {
    object NotRecording : RecordingState()
    data class Recording(val duration: String) : RecordingState()
    object StoppingRecording : RecordingState()
    object SavedToGallery : RecordingState()
}
```

#### Recording Button Implementation

```kotlin
@Composable
fun RecordingButton(
    recordingState: RecordingState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (recordingState is RecordingState.Recording) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                when (recordingState) {
                    is RecordingState.Recording -> RecordRed
                    is RecordingState.StoppingRecording -> RecordRed.copy(alpha = 0.7f)
                    is RecordingState.SavedToGallery -> Color(0xFF4CAF50)
                    RecordingState.NotRecording -> MediumDarkBackground
                }
            )
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Animated record indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(if (recordingState is RecordingState.Recording) scale else 1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (recordingState) {
                            is RecordingState.Recording -> White
                            is RecordingState.StoppingRecording -> White.copy(alpha = 0.7f)
                            is RecordingState.SavedToGallery -> Color(0xFF4CAF50)
                            RecordingState.NotRecording -> RedVariant
                        }
                    )
            )
            
            // Animated text content
            AnimatedContent(
                targetState = recordingState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with
                    fadeOut(animationSpec = tween(300))
                }
            ) { state ->
                Text(
                    text = when (state) {
                        is RecordingState.Recording -> "RECORDING ${state.duration}"
                        is RecordingState.StoppingRecording -> "STOPPING RECORDING..."
                        is RecordingState.SavedToGallery -> "SAVED TO GALLERY"
                        RecordingState.NotRecording -> "RECORD"
                    },
                    color = if (state is RecordingState.NotRecording) MediumLightGray else Color(0xFFFFFFFF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
```

## Camera Components

### Camera Stream View

The camera stream component handles real-time video display and camera integration.

#### Features

- **Real-time Streaming**: Live camera feed display
- **Zoom Support**: Integrated zoom functionality
- **Performance Optimization**: Efficient rendering and memory management
- **Error Handling**: Graceful handling of camera errors and unavailable states

```kotlin
@Composable
fun CameraStreamView(
    modifier: Modifier = Modifier,
    cameraState: CameraState,
    onCameraError: (String) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera stream implementation
        // Integration with camera APIs and video rendering
        
        // Error state handling
        if (cameraState.hasError) {
            ErrorOverlay(
                message = cameraState.errorMessage,
                onRetry = { /* Retry logic */ }
            )
        }
    }
}
```

## Settings Components

### Tab Navigation

The settings interface implements a tab-based navigation system for organizing different configuration sections.

#### Control Tab System

```kotlin
enum class ControlTab {
    CAMERA_CONTROL,
    AI_CONTROL,
    LICENSE_CONTROL
}
```

#### Tab Switcher Implementation

```kotlin
@Composable
fun ControlTabSwitcher(
    selectedTab: ControlTab,
    onTabSelected: (ControlTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ControlTab.values().forEach { tab ->
            CustomToggleButton(
                label = tab.displayName,
                isSelected = selectedTab == tab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}
```

### Settings Sections

#### Camera Settings

```kotlin
@Composable
fun CameraLayout() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Display Settings
        DisplaySettingsSection()
        
        // Detection Settings
        DetectionSettingsSection()
        
        // Image Settings
        ImageSettingsSection()
    }
}
```

#### AI Settings

```kotlin
@Composable
fun AiLayout(
    systemStatus: SystemStatus,
    onSystemStatusChange: (SystemStatus) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI configuration options
        AiConfigurationSection(
            isEnabled = systemStatus.isAiEnabled,
            onToggle = { enabled ->
                onSystemStatusChange(systemStatus.copy(isAiEnabled = enabled))
            }
        )
    }
}
```

## Component Design Patterns

### 1. State Hoisting

Components follow the state hoisting pattern to maintain predictable state management.

```kotlin
@Composable
fun StatefulComponent(
    state: ComponentState,
    onStateChange: (ComponentState) -> Unit
) {
    // Component implementation using passed state
}
```

### 2. Composition over Inheritance

The component system favors composition over inheritance, allowing flexible component combinations.

```kotlin
@Composable
fun ComplexComponent() {
    Column {
        CustomizableButton(/* config */)
        StatusIndicator(/* state */)
        RecordingButton(/* state */)
    }
}
```

### 3. Unidirectional Data Flow

Components implement unidirectional data flow for predictable state management.

```kotlin
// State flows from parent to child
@Composable
fun ParentComponent() {
    var state by remember { mutableStateOf(InitialState) }
    
    ChildComponent(
        state = state,
        onStateChange = { newState -> state = newState }
    )
}
```

## Performance Optimization

### 1. Lazy Loading

Components implement lazy loading for better performance.

```kotlin
@Composable
fun LazyComponentList(items: List<Item>) {
    LazyColumn {
        items(items) { item ->
            ItemComponent(item = item)
        }
    }
}
```

### 2. Memory Management

Proper memory management with DisposableEffect.

```kotlin
DisposableEffect(Unit) {
    // Setup
    onDispose {
        // Cleanup
    }
}
```

### 3. State Optimization

Efficient state management with remember and derived state.

```kotlin
val derivedState by remember(primaryState) {
    derivedStateOf { /* computation */ }
}
```

## Accessibility Features

### 1. Content Descriptions

All interactive components include proper content descriptions.

```kotlin
Image(
    painter = painterResource(id = iconId),
    contentDescription = "Button action description",
    modifier = Modifier.clickable { /* action */ }
)
```

### 2. Focus Management

Proper focus management for keyboard navigation.

```kotlin
Modifier
    .focusable()
    .onKeyEvent { /* handle key events */ }
```

### 3. Semantic Properties

Semantic properties for screen readers.

```kotlin
Modifier.semantics {
    contentDescription = "Accessible description"
    role = Role.Button
}
```

## Testing Considerations

### 1. Component Testing

Components are designed for easy testing with clear interfaces.

```kotlin
@Composable
fun TestableComponent(
    state: TestState,
    onAction: () -> Unit
) {
    // Implementation with clear state and action interfaces
}
```

### 2. Preview Support

All components include preview functions for development.

```kotlin
@Preview
@Composable
fun ComponentPreview() {
    CamConnectTheme {
        ComponentName(
            state = PreviewState,
            onAction = {}
        )
    }
}
```

## Best Practices

1. **Single Responsibility**: Each component has a single, well-defined responsibility
2. **Composability**: Components are designed to be easily composed together
3. **Reusability**: Components are reusable across different contexts
4. **Testability**: Components are designed with testing in mind
5. **Accessibility**: All components include proper accessibility support
6. **Performance**: Components are optimized for performance and memory usage
7. **Documentation**: All components include clear documentation and examples

## Future Enhancements

- **Component Library**: Create a comprehensive component library
- **Animation System**: Enhanced animation capabilities
- **Accessibility**: Improved accessibility features
- **Performance**: Further performance optimizations
- **Testing**: Enhanced testing infrastructure
- **Documentation**: Interactive component documentation

---

**Document Version**: 1.0  
**Last Updated**: 15-07-2025  
**Author**: Shathir  
**Status**: Complete 
**License**: Apache 2.0 