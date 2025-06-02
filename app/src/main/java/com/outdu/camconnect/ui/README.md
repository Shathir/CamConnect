# CamConnect UI Architecture

## Overview
This UI implementation follows the specifications from the Android prompt to create an adaptive camera streaming and control interface with three distinct layout modes.

## Directory Structure

```
ui/
├── components/          # Reusable UI components
│   ├── buttons/        # Customizable buttons and toggles
│   ├── camera/         # Camera-related views
│   ├── controls/       # Control elements (recording, zoom, etc.)
│   ├── indicators/     # Status indicators (battery, connectivity, etc.)
│   └── settings/       # Settings controls and sections
├── layouts/            # Layout implementations
│   ├── MinimalControlLayout.kt    # Layout 1 (90/10 split)
│   ├── ExpandedControlLayout.kt   # Layout 2 (60/40 split)
│   ├── FullControlLayout.kt       # Layout 3 (45/55 split)
│   └── AdaptiveStreamLayout.kt    # Main adaptive container
├── models/             # Data models and states
│   ├── LayoutState.kt  # Layout mode management
│   └── CameraData.kt   # Camera and system states
└── theme/              # Material theme configuration

```

## Layout Modes

### Layout 1: Minimal Control Panel (Stream Focused)
- **Left Pane (90%)**: Live camera stream display
- **Right Pane (10%)**: Compact controls and status indicators
- **Components**: Settings button, camera switch, recording toggle, compass, battery indicator, connectivity indicators, AI status, speed indicator
- **Purpose**: Maximum viewing area for immersive camera streaming

### Layout 2: Expanded Control Panel (Interactive Controls)
- **Left Pane (60%)**: Live camera stream display
- **Right Pane (40%)**: Dynamic and scrollable control options
- **Components**:
  - Row 1: Customizable 5-button row
  - Row 2: Recording toggle + Scrollable zoom selector
  - Row 3: 6 toggleable icons
  - Row 4: Compass component
  - Row 5: Video feed slot + Snapshot image slot
  - Row 6: Speed indicator + 4 Status indicators
- **Purpose**: Intermediate control layout with quick access to common features

### Layout 3: Full Control Panel (Settings and Configuration)
- **Left Pane (45%)**: Live camera stream display
- **Right Pane (55%)**: In-depth device/camera configuration
- **Components**:
  - Row 1: Customizable 5-button row
  - Row 2: Tab switcher (Camera Control / Device Control)
  - Row 3: Display Settings (Auto Day/Night, Vision Mode)
  - Row 4: Detection Toggles (Object, Far Object, Motion)
  - Row 5: Image Settings (Camera Mode, Orientation)
- **Purpose**: Comprehensive settings and configuration interface

## Key Components

### CustomizableButton
- Configurable icon, text, color, and function
- Supports compact and full display modes
- Used throughout all layouts for consistent interaction

### Status Indicators
- **BatteryIndicator**: Shows battery level with appropriate icon
- **WifiIndicator**: WiFi connection status
- **LteIndicator**: Mobile data connection status
- **OnlineIndicator**: Online/Offline status
- **AiStatusIndicator**: AI enabled/disabled status

### Camera Controls
- **RecordingToggle**: Animated recording button
- **ZoomSelector**: Scrollable zoom level selection (1x, 2x, 4x, etc.)
- **CameraSwitchButton**: Switch between multiple cameras
- **ExpandButton**: Transition between layout modes

### Compass & Speed
- **CompassIndicator**: Real-time directional compass with degree display
- **SpeedIndicator**: Shows speed from phone sensors
- **CompactSpeedIndicator**: Minimal speed display for Layout 1

### Settings Components
- **DisplaySettingsSection**: Auto day/night mode, vision mode selection
- **DetectionSettingsSection**: Object, far object, and motion detection toggles
- **ImageSettingsSection**: Camera mode (HDR/EIS) and orientation settings
- **ControlTabSwitcher**: Switch between Camera and Device control tabs

## State Management

### CameraState
- Recording status
- Current camera index
- Zoom level
- Camera mode (HDR/EIS/Both)
- Vision mode (Vision/Infrared/Both)
- Orientation mode
- Auto day/night setting

### SystemStatus
- Battery level
- WiFi/LTE connectivity
- Online status
- AI enabled status
- Current speed
- Compass direction

### DetectionSettings
- Object detection
- Far object detection
- Motion detection

## Layout Transitions
The `AdaptiveStreamLayout` manages smooth animated transitions between layout modes using `AnimatedContent` with horizontal slide animations.

## Customization
- Custom buttons can be configured with unique icons, colors, and actions
- Toggleable icons in Layout 2 can be customized
- All colors follow Material Design 3 theming

## Usage
Simply add `AdaptiveStreamLayout()` to your activity or screen:

```kotlin
setContent {
    CamConnectTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AdaptiveStreamLayout()
        }
    }
}
```

## Future Enhancements
- Actual camera integration
- Real sensor data for compass and speed
- Persistence of user preferences
- Custom button configuration UI
- Gesture controls for layout transitions 