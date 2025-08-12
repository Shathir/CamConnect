# Minimal Control Layout Documentation

![Minimal Layout](images/layout-minimal.png)

## Overview
The Minimal Control Layout (`MinimalControlLayout.kt`) provides a streamlined, space-efficient interface designed for immersive camera operation. This layout displays essential controls in a vertical arrangement, maximizing screen real estate for the camera feed while maintaining access to critical functionality.

## Layout Structure

### Vertical Control Column
All controls are arranged in a single vertical column with space-around distribution, ensuring optimal use of screen space while maintaining easy access to essential functions.

## Control Elements

### Settings Button
- **ID**: `settings`
- **Function**: Opens the full settings/configuration panel
- **Visual Styling**:
  - Background: Medium dark background
  - Icon Color: Medium gray
  - Border: Standard button border color
- **Click Action**: Triggers `onSettingsClick()` callback
- **Compact Mode**: Yes (icon only, no text)
- **Always Enabled**: Yes
- **API Used**: No direct API - UI navigation only

### Expand Control Button
- **ID**: `camera_switch`
- **Function**: Expands the interface to the full expanded control layout
- **Visual Styling**:
  - Background: Dark gray
  - Icon Color: White
  - Border: Dark gray
- **Click Action**: Triggers `onExpandClick()` callback
- **Compact Mode**: Yes (icon only, no text)
- **Always Enabled**: Yes
- **API Used**: No direct API - UI state management only

### Screen Recording Button
- **ID**: `screen-record`
- **Function**: Starts/stops screen recording functionality
- **Visual States**:
  - **Not Recording**:
    - Icon: Record icon
    - Icon Color: Record red
    - Text: "Start Recording"
  - **Recording**:
    - Icon: Record circle line
    - Icon Color: Record red
    - Text: "Stop Recording"
- **Background**: Medium dark background
- **Border**: Standard button border color
- **Click Action**: `recordingViewModel.toggleRecording(context)`
- **Compact Mode**: Yes (icon only, no text)
- **Permissions Required**:
  - Media Projection permission for screen capture
  - Notification permission (Android 13+)
- **API Used**:
  - **Primary Service**: `ScreenRecorderService` (Android Foreground Service)
  - **Android APIs**:
    - `MediaProjectionManager.createScreenCaptureIntent()` - Screen capture permission
    - `MediaProjectionManager.getMediaProjection()` - Screen projection instance
    - `MediaRecorder` - Video recording engine with H.264 encoding
    - `VirtualDisplay.createVirtualDisplay()` - Screen mirroring surface
    - `MediaStore.Video.Media` - Gallery integration for saving videos
    - `NotificationManager` - Recording status notifications
  - **Permission APIs**:
    - `POST_NOTIFICATIONS` permission check (Android 13+)
    - `ActivityResultContracts.RequestPermission()` - Permission launcher
    - `ActivityResultContracts.StartActivityForResult()` - Media projection launcher
  - **Service Configuration**:
    - Foreground service with persistent notification
    - Format: MPEG-4 container
    - Video: H.264 encoding, 30 FPS, 8192 kbps bit rate
    - Resolution: Native device resolution with scaling
    - Audio: Optional (currently disabled)

### IR (Infrared) Button
- **ID**: `ir`
- **Function**: Toggles infrared illumination for night vision
- **Visual States**:
  - **Disabled State** (Vision Mode active):
    - Background: Dark gray (#272727)
    - Icon: Darker gray (#363636)
    - Border: Standard
  - **IR Off**:
    - Background: Standard button background
    - Icon: Standard icon color
    - Border: Standard button border color
  - **IR On**:
    - Background: Record red
    - Icon: White (dark theme) or standard selected color
    - Border: Record red
- **Click Action**: `cameraControlViewModel.toggleIR()`
- **Enabled When**: `currentVisionMode != VisionMode.VISION`
- **Compact Mode**: Yes (icon only, no text)
- **API Used**:
  - **Primary API**: `MotocamAPIAndroidHelper.setIrBrightnessAsync()`
  - **Underlying Protocol**: `MotocamAPIHelper.setImgIRBrightnessCmd(value)`
  - **Command Structure**:
    - Header: `Header.SET` (1)
    - Command: `Commands.IMAGE` (4)
    - SubCommand: `ImageSubCommands.IRBRIGHTNESS`
    - Value: 0 (disabled) or 15 (low intensity when enabling)
  - **HTTP Communication**: HTTP POST to `http://192.168.2.1:80/api/motocam_api`
  - **Brightness Range**: 0-255 (0=off, 15=low intensity, 5=high intensity)
  - **Default Behavior**: When enabling IR, starts with low intensity (value 15)

### IR Intensity Button
- **ID**: `ir-cut-filter`
- **Function**: Toggles between high and low intensity IR illumination
- **Visual States**:
  - **Disabled State** (IR not enabled):
    - Background: Dark gray (#272727)
    - Icon: Darker gray (#363636)
    - Enabled: False
  - **Low Intensity** (when IR enabled):
    - Background: Standard button background
    - Icon: Standard icon color
  - **High Intensity** (when IR enabled):
    - Background: Selected button background
    - Icon: Selected icon color
- **Click Action**: `cameraControlViewModel.toggleIrIntensity()`
- **Enabled When**: `cameraControlState.isIrEnabled` is true
- **Compact Mode**: Yes (icon only, no text)
- **Dependency**: Only functional when IR is enabled
- **API Used**:
  - **Primary API**: `MotocamAPIAndroidHelper.setIrBrightnessAsync()`
  - **Underlying Protocol**: `MotocamAPIHelper.setImgIRBrightnessCmd(value)`
  - **Command Structure**: Same as IR button
  - **Value Logic**: 
    - Low Intensity: 15
    - High Intensity: 5
    - Toggle between these two values only
  - **HTTP Communication**: HTTP POST to `http://192.168.2.1:80/api/motocam_api`
  - **Prerequisite**: IR must be enabled (brightness > 0) for this function to work

## Status Indicators

### WiFi Indicator
- **Function**: Shows current WiFi connection status
- **States**:
  - Connected: Active WiFi icon with full opacity
  - Disconnected: Inactive/grayed WiFi icon
- **Data Source**: `systemStatus.isWifiConnected`
- **Position**: Below control buttons in vertical arrangement

### AI Status Indicator
- **Function**: Shows AI processing status
- **States**:
  - Enabled: Active AI icon
  - Disabled: Inactive/grayed AI icon
- **Data Source**: `systemStatus.isAiEnabled`
- **Position**: Below WiFi indicator

### Battery Indicator
- **Function**: Shows current battery level with visual representation
- **Display**: Battery icon with level indication
- **Data Source**: `systemStatus.batteryLevel`
- **Position**: Bottom of the control column
- **API Used**:
  - **Android API**: `BatteryManager` system service
  - **Intent Filter**: `Intent.ACTION_BATTERY_CHANGED` broadcasts
  - **Battery Properties**:
    - `BATTERY_PROPERTY_CAPACITY` - Current battery percentage
    - `BATTERY_PROPERTY_STATUS` - Charging/discharging status
  - **Update Source**: System battery level broadcasts

## Permission Management

### Screen Recording Permissions
The layout handles complex permission flows for screen recording:

#### Android 13+ (API 33+)
- **Notification Permission**: Required for foreground service
- **Permission Flow**:
  1. Check notification permission
  2. Request if not granted
  3. Launch screen capture intent
  4. Start foreground service
- **API Used**:
  - `ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)`
  - `ActivityResultContracts.RequestPermission()` launcher

#### Pre-Android 13
- **Media Projection**: Automatically available
- **Permission Flow**:
  1. Launch screen capture intent directly
  2. Start foreground service
- **API Used**:
  - Direct media projection without notification permission

### Permission Launchers
- **Screen Record Launcher**: Handles media projection result
  - Type: `ActivityResultContracts.StartActivityForResult()`
  - Intent: `MediaProjectionManager.createScreenCaptureIntent()`
  - Result: `RecordConfig` with result code and intent data
- **Permission Launcher**: Handles notification permission result
  - Type: `ActivityResultContracts.RequestPermission()`
  - Permission: `Manifest.permission.POST_NOTIFICATIONS`
  - Flow: Continues to media projection if granted
- **Service Integration**: Starts `ScreenRecorderService` with proper configuration
  - Action: `ScreenRecorderService.ACTION_START`
  - Extra: `ScreenRecorderService.RECORD_CONFIG` with projection data

## Communication Architecture

### Camera Control APIs
- **Protocol**: HTTP REST API over WiFi
- **Primary IP**: `192.168.2.1` (default camera device IP)
- **Main API Port**: `80` (HTTP)
- **Health Check Port**: `8080` (ping endpoint)
- **API Endpoint**: `http://192.168.2.1:80/api/motocam_api`
- **Client Library**: Ktor HTTP client with CIO engine
- **Discovery**: Automatic device discovery via ARP table scanning
- **Connection**: `MotocamSocketClient` handles HTTP communication
- **Wrapper**: `MotocamAPIHelperWrapper` provides async coroutine interface

### Command Protocol Structure
```
[Header][Command][SubCommand][DataLength][Data][CRC]
```
- **Transport**: HTTP POST with hex-encoded command data
- **Content-Type**: `application/octet-stream`
- **Authentication**: Session cookie authentication
- **Header Values**: SET(1), GET(2), ACK(3), RESPONSE(4)
- **Command Categories**: STREAMING(1), NETWORK(2), CONFIG(3), IMAGE(4), AUDIO(5), SYSTEM(6)
- **Error Handling**: CRC checksum validation and exception handling
- **Timeout**: Connection timeout of 10 seconds for HTTP requests

### Android System Integration
- **Media Projection**: System-level screen capture API
- **MediaRecorder**: Hardware-accelerated video encoding
- **Foreground Service**: Background recording with user notification
- **File System**: Temporary cache files with MediaStore integration
- **Notifications**: Persistent recording status with system integration

## Layout Characteristics

### Spacing and Arrangement
- **Main Container**: Column with `Arrangement.SpaceAround`
- **Padding**: 8dp on all sides
- **Vertical Alignment**: Center-aligned horizontally
- **Button Spacing**: Automatic distribution for optimal touch targets

### Device Adaptations
- **Universal Layout**: Same layout for both tablets and phones
- **Compact Design**: Prioritizes screen real estate for camera feed
- **Touch-Optimized**: All buttons sized for easy finger access

### Visual Styling
- **Compact Buttons**: All buttons use `isCompact = true`
- **Icon-Only Display**: `showText = false` for all controls
- **Consistent Theming**: Follows app's dark theme design
- **Status Integration**: System indicators seamlessly integrated

## State Management

### ViewModels Used
- **RecordingViewModel**: Manages recording state and operations
  - State: `RecordingState` (NotRecording, Recording, StoppingRecording, SavedToGallery)
  - Duration tracking with coroutine-based timer
  - Service lifecycle management
- **CameraControlViewModel**: Handles camera control states (IR, intensity)
  - State: `CameraControlState` with IR settings
  - API communication for IR control
  - Vision mode dependency management
- **CameraLayoutViewModel**: Manages layout and vision mode states
  - Vision mode restrictions for IR controls
  - Layout state transitions

### State Collection
- **Recording State**: `isRecording.collectAsStateWithLifecycle()`
- **Camera Control State**: `cameraControlState.collectAsStateWithLifecycle()`
- **Theme State**: `isSystemInDarkTheme()`

## Memory Management
- **Cleanup Implementation**: `DisposableEffect` for proper cleanup
- **Memory References**: `MemoryManager.cleanupWeakReferences()` on disposal
- **Error Handling**: Catches and logs cleanup errors
- **Lifecycle Awareness**: Proper logging for component lifecycle

## Service Integration

### Screen Recorder Service
- **Service Class**: `ScreenRecorderService`
- **Type**: Foreground service with persistent notification
- **Actions**: 
  - `ACTION_START`: Begins screen recording
  - `ACTION_STOP`: Ends screen recording
- **Configuration**: `RecordConfig` with result code and intent data
- **Lifecycle**: 
  - Automatic start/stop based on media projection callbacks
  - Resource cleanup on service destruction
  - Gallery integration for saved videos

### Intent Handling
- **Media Projection Manager**: System service for screen capture permissions
- **Activity Result Handling**: Proper handling of permission results
- **Service Starting**: Uses `ContextCompat.startForegroundService()` for compatibility
- **Error Recovery**: Graceful handling of permission denials and errors

## User Experience Features

### Minimal Distraction
- **Clean Interface**: Only essential controls visible
- **Quick Access**: Single-tap access to most common functions
- **Status Awareness**: Critical system status always visible
- **Expansion Option**: Easy upgrade to full controls when needed

### Accessibility
- **Content Descriptions**: All buttons have proper content descriptions
- **Touch Targets**: Appropriately sized for accessibility guidelines
- **Visual Feedback**: Clear visual states for all interactive elements
- **System Integration**: Respects system accessibility settings

---
*This documentation covers the streamlined Minimal Control Layout optimized for immersive camera operation with essential controls and comprehensive API integration.* 