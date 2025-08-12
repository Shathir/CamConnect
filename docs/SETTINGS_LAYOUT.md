# Settings Layout Documentation

![Full Layout](images/layout-full.png)

## Overview
The Settings Layout (`FullControlLayout.kt`) provides comprehensive configuration and control capabilities through a tabbed interface. This layout is designed for in-depth device and camera configuration, offering full access to all system settings and advanced features.

## Layout Structure

### Main Container
- **Background**: Transparent background allowing camera feed visibility
- **Padding**: Device-specific spacing (24dp for tablets, 12dp for phones)
- **Arrangement**: Vertical spacing between sections
- **Scrolling**: Managed scroll state with proper cleanup

## Header Section

### Navigation Controls

#### Settings Screen Button
- **ID**: `settings-screen`
- **Function**: Returns to the camera control interface
- **Visual Styling**:
  - Background: Selected button background (active state)
  - Icon Color: Selected icon color
  - Border: Standard button border color
- **Click Action**: Triggers `onCollapseClick()` callback
- **Compact Mode**: Yes (icon only, no text)
- **Always Enabled**: Yes
- **API Used**: No direct API - UI navigation only

#### Tab Switcher
- **Function**: Central navigation between different settings categories
- **Weight**: Takes remaining space in header row (`weight(1f)`)
- **Integration**: Uses `ControlTabSwitcher` component
- **State Management**: Controlled by `selectedTab` and `onTabSelected`
- **API Used**: No direct API - UI state management only

#### Logout Button
- **ID**: `logout`
- **Function**: User logout functionality (currently disabled)
- **Visual Styling**:
  - Background: Standard button background
  - Icon Color: Red
  - Border: Standard button border color
  - **Enabled**: False (feature not implemented)
- **Compact Mode**: Yes (icon only, no text)
- **API Used**: Not implemented - placeholder for future logout functionality

## Tab Navigation System

### Control Tab Switcher
The layout implements a sophisticated tab system for organizing different categories of settings:

#### Available Tabs
1. **Camera Control Tab** (`ControlTab.CAMERA_CONTROL`)
2. **AI Control Tab** (`ControlTab.AI_CONTROL`)
3. **License Control Tab** (`ControlTab.LICENSE_CONTROL`)

### Tab Content Areas

#### Camera Control Tab (`ControlTab.CAMERA_CONTROL`)
- **Content**: Advanced camera configuration settings
- **Component**: `CameraLayout()`
- **Features**: 
  - Camera parameter adjustments
  - Image quality settings
  - Exposure and focus controls
  - Advanced camera features
- **State Management**: Integrated with camera ViewModels
- **API Used**:
  - **Configuration APIs**: `MotocamAPIAndroidHelper` suite of camera control functions
  - **Primary APIs**:
    - `setResolutionAsync()` - Image resolution settings
    - `setRotationAsync()` - Image rotation settings
    - `setTiltAsync()` - Image tilt adjustments
    - `setMirrorAsync()` - Image mirroring controls
    - `setFlipAsync()` - Image flip controls
    - `setWdrAsync()` - Wide Dynamic Range settings
    - `setEisAsync()` - Electronic Image Stabilization
  - **Command Protocol**: Uses standard MotocamAPI command structure
  - **HTTP Communication**: HTTP POST to `http://192.168.2.1:80/api/motocam_api`
  - **Configuration Management**:
    - `getConfigAsync("Current")` - Current device configuration
    - `getConfigAsync("Default")` - Default configuration values
    - `getConfigAsync("Factory")` - Factory reset values

#### AI Control Tab (`ControlTab.AI_CONTROL`)
- **Content**: AI processing and system configuration
- **Component**: `AiLayout(systemStatus, onSystemStatusChange)`
- **Features**:
  - AI processing toggles
  - System performance settings
  - Device control options
- **State Management**: 
  - Receives current `systemStatus`
  - Updates through `onSystemStatusChange` callback
- **API Used**:
  - **System Status API**: `MotocamAPIAndroidHelper.getHealthStatusAsync()`
  - **Health Monitoring**:
    - CPU usage monitoring
    - Memory usage tracking
    - Temperature monitoring (ISP, IR, Sensor)
    - Service status (RTSPS, Streamer, Portable RTC)
  - **AI Processing Control**: Device-specific AI pipeline management
  - **Performance Monitoring**: Real-time system performance metrics
  - **Update Frequency**: Configurable polling interval for health status

#### License Control Tab (`ControlTab.LICENSE_CONTROL`)
- **Content**: Device information and licensing
- **Component**: `LicenseLayout()`
- **Features**:
  - Device information display
  - License management
  - System information
  - Version details
- **API Used**:
  - **Device Information APIs**:
    - Android system properties (`Build.MODEL`, `Build.VERSION`, etc.)
    - Device hardware information
    - Network interface details
  - **Configuration APIs**:
    - `getCurrentConfig()` - Device configuration details
    - System version and build information
  - **License Validation**: Device-specific licensing information
  - **Serial Number**: Hardware identification

## Helper Components

### Custom Toggle Button
```kotlin
CustomToggleButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

#### Functionality
- **Purpose**: Binary toggle controls throughout settings
- **Visual States**:
  - **Selected**: Blue variant background
  - **Unselected**: Dark gray background
- **Styling**: Rounded corners (8dp), centered text
- **Typography**: Body medium style with white text
- **API Integration**: Can trigger camera API calls based on implementation

### Custom Selectable Button
```kotlin
CustomSelectableButton(
    label: String,
    isSelected: Boolean,
    selectedColor: Color = Color.Red,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
)
```

#### Functionality
- **Purpose**: Single-selection from multiple options
- **Visual States**:
  - **Selected**: Custom color background (default red)
  - **Unselected**: Dark gray background
- **Customization**: Configurable selected color
- **Styling**: Rounded corners (8dp), centered text
- **API Integration**: Commonly used for camera setting options (resolution, quality, etc.)

### Setting Row
```kotlin
SettingRow(
    label: String, 
    content: @Composable RowScope.() -> Unit
)
```

#### Functionality
- **Purpose**: Consistent layout for settings items
- **Structure**:
  - Label: White text, 14sp
  - Content: Flexible content area
- **Spacing**: 4dp between label and content, 8dp vertical padding
- **Usage**: Container for camera API controls and system settings

## Communication Architecture

### Camera Configuration APIs
- **Primary Interface**: `MotocamAPIAndroidHelper` object with async methods
- **Protocol**: HTTP REST API over WiFi
- **IP Address**: `192.168.2.1` (default camera device)
- **Main API Port**: `80` (HTTP)
- **Health Check Port**: `8080` (ping endpoint)
- **API Endpoint**: `http://192.168.2.1:80/api/motocam_api`
- **Client Library**: Ktor HTTP client with CIO engine
- **Connection Management**: Automatic device discovery and connection handling

### Configuration Management
- **Factory Config**: Default manufacturer settings
  - API: `getConfigAsync("Factory")`
  - Command: `MotocamAPIHelper.getFactoryConfigCmd()`
- **Default Config**: User-defined default settings
  - API: `getConfigAsync("Default")`
  - Command: `MotocamAPIHelper.getDefaultConfigCmd()`
- **Current Config**: Active device configuration
  - API: `getConfigAsync("Current")`
  - Command: `MotocamAPIHelper.getCurrentConfigCmd()`

### System Monitoring APIs
- **Health Status**: Comprehensive device health information
  - API: `getHealthStatusAsync()`
  - Returns: `HealthStatus` data class with system metrics
  - Monitoring: Real-time CPU, memory, temperature tracking
- **Network Status**: WiFi and connectivity monitoring
  - API: `getWifiStateCmd()`
  - Returns: Current network configuration and status

### Error Handling and Logging
- **Exception Management**: Comprehensive try-catch blocks with logging
- **API Timeout**: Connection timeout handling (10 seconds default)
- **Retry Logic**: Automatic retry for failed API calls
- **Error Propagation**: Proper error callback handling to UI layer

## Device Adaptation

### Tablet Layout
- **Spacing**: Increased vertical spacing (24dp between sections)
- **Content Padding**: Larger content padding (20dp)
- **Visual Hierarchy**: Enhanced spacing for larger screens
- **Touch Targets**: Optimized for tablet interaction

### Phone Layout
- **Spacing**: Compact vertical spacing (12dp between sections)
- **Content Padding**: Standard content padding (12dp)
- **Scrolling**: Vertical scrolling for content that exceeds screen height
- **Touch Targets**: Optimized for phone interaction

## Memory Management

### Cleanup Implementation
- **Component Lifecycle**: `DisposableEffect` for proper cleanup
- **Memory References**: `MemoryManager.cleanupWeakReferences()` on disposal
- **Error Handling**: Comprehensive error catching and logging
- **State Cleanup**: Proper cleanup of scroll states and view models

### Logging
- **Creation**: "SettingsControlLayout Component created"
- **Disposal**: "SettingsControlLayout Component disposed - cleaning up"
- **Error Logging**: Detailed error information for debugging

## State Management

### Tab State
- **Current Tab**: Managed by parent component through `selectedTab`
- **Tab Changes**: Handled through `onTabSelected` callback
- **Content Keying**: Each tab content is keyed for proper memory management

### System Status Integration
- **Status Input**: Receives current system status from parent
- **Status Updates**: Propagates changes through `onSystemStatusChange`
- **Real-time Updates**: Immediate reflection of system changes
- **API Polling**: Background polling for health status updates

## Scrolling and Navigation

### Scroll State Management
- **State Creation**: `rememberScrollState()` for content scrolling
- **Cleanup**: Proper disposal of scroll state resources
- **Memory Efficiency**: Managed scroll state to prevent memory leaks

### Content Organization
- **Weighted Layout**: Tab switcher takes available space
- **Fixed Elements**: Navigation buttons maintain consistent size
- **Flexible Content**: Tab content adapts to available space

## Integration Points

### Service Integration
- **System Services**: Integration with device system services
- **Camera Services**: Direct integration with camera API
- **AI Services**: Connection to AI processing systems
- **Health Monitoring**: Continuous system health monitoring

### Callback System
- **Navigation Callbacks**: Proper handling of navigation events
- **Settings Callbacks**: Real-time propagation of settings changes
- **State Callbacks**: Immediate reflection of state changes
- **API Callbacks**: Async callback handling for all API operations

## User Experience Features

### Visual Consistency
- **Theme Integration**: Consistent with app's dark theme
- **Color Scheme**: Unified color palette across all tabs
- **Typography**: Consistent text styling and hierarchy
- **Button Styling**: Unified button appearance and behavior

### Accessibility
- **Navigation**: Clear navigation between sections
- **Content Descriptions**: Proper labeling for screen readers
- **Touch Targets**: Appropriately sized interactive elements
- **Visual Feedback**: Clear indication of selected states

### Performance
- **Lazy Loading**: Tab content loaded only when accessed
- **Memory Efficiency**: Proper cleanup prevents memory leaks
- **Smooth Scrolling**: Optimized scrolling performance
- **State Preservation**: Tab states preserved during navigation
- **API Optimization**: Efficient API calling with caching where appropriate

## Error Handling

### Exception Management
- **Component Errors**: Graceful handling of component failures
- **Memory Errors**: Protection against memory-related issues
- **State Errors**: Recovery from invalid state conditions
- **Logging**: Comprehensive error logging for debugging
- **API Errors**: Specific handling for camera API communication failures

### Fallback Behavior
- **Failed Tab Loading**: Graceful degradation for failed tab content
- **Memory Issues**: Automatic cleanup on memory pressure
- **State Recovery**: Restoration of valid states after errors
- **API Timeouts**: Retry logic and user feedback for API failures
- **Network Issues**: Handling of camera device disconnection scenarios

---
*This comprehensive documentation covers the full Settings Layout with advanced configuration capabilities, complete API integration, and extensive visual component references for professional camera device management.* 