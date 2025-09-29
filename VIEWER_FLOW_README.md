# Viewer Flow Implementation

This document describes the implementation of the Viewer Flow feature in the CamConnect camera streaming application.

## Overview

The Viewer Flow allows users with limited permissions to connect to and stream from ONVIF-compatible cameras without the need for full account setup. This provides a streamlined experience for viewers who only need streaming access.

## Features Implemented

### 1. Viewer Role Authentication System
- **Location**: `ViewerFlowActivity.kt`, `ViewerFlowViewModel.kt`
- **Description**: Dedicated activity and view model for handling viewer-specific authentication flow
- **Key Features**:
  - Separate from owner authentication
  - Uses existing SessionManager for PIN-based authentication
  - Handles authentication errors and lockout scenarios

### 2. ONVIF Discovery Service Integration
- **Location**: `ViewerFlowViewModel.kt`
- **Description**: Integrates with existing ONVIF discovery service to find available cameras
- **Key Features**:
  - Automatic network scanning for ONVIF devices
  - Asynchronous discovery with callback handling
  - Error handling for discovery failures

### 3. Camera Cards UI
- **Location**: `CameraCard.kt`
- **Description**: Beautiful card-based UI for displaying discovered cameras
- **Key Features**:
  - Shows IP Address, MAC Address (if available), and Camera Name
  - Extracts camera information from ONVIF scopes
  - "Connect and Stream" button for each camera
  - Status indicators and device type information

### 4. PIN Authentication Dialog
- **Location**: `ViewerPinAuthDialog.kt`
- **Description**: Custom dialog for PIN authentication when connecting to cameras
- **Key Features**:
  - 4-digit PIN input with validation
  - Camera information display
  - Real-time authentication status
  - Error handling with user-friendly messages
  - Loading states during authentication

### 5. No Cameras Discovery Flow
- **Location**: `NoCamerasDialog.kt`
- **Description**: Handles scenarios when no cameras are discovered
- **Key Features**:
  - User-friendly explanation of why no cameras were found
  - Direct link to WiFi settings
  - Retry functionality
  - Helpful instructions for connecting to camera networks

### 6. Navigation Integration
- **Location**: `LoginComponents.kt` (updated)
- **Description**: Updated existing login screen to launch Viewer Flow
- **Key Features**:
  - "Start Streaming" button launches ViewerFlowActivity
  - Maintains existing owner login functionality
  - Seamless integration with existing UI

## Application Flow

### Viewer Flow Sequence

1. **Start Streaming**: User clicks "Start Streaming" button on login screen
2. **ONVIF Discovery**: App runs ONVIF discovery service to find cameras
3. **Camera Selection**: 
   - If cameras found: Display camera cards with details
   - If no cameras: Show WiFi connection prompt
4. **WiFi Connection Loop**: 
   - If no cameras found, prompt user to connect to camera WiFi
   - Return from WiFi settings triggers discovery retry
   - Loop continues until cameras are found
5. **Camera Authentication**: User selects camera and enters PIN
6. **Stream Consumption**: On successful authentication, navigate to MainActivity

### Technical Flow

```
ViewerFlowActivity
├── ViewerFlowViewModel (manages state)
├── ViewerFlowScreen (main UI)
├── CameraCard (displays found cameras)
├── NoCamerasDialog (no cameras scenario)
├── ViewerPinAuthDialog (PIN authentication)
└── MainActivity (stream consumption)
```

## Key Components

### ViewerFlowActivity
- Main activity for viewer flow
- Handles system-level operations (permissions, WiFi settings)
- Manages navigation to MainActivity on success

### ViewerFlowViewModel
- Manages UI state and business logic
- Handles ONVIF discovery operations
- Manages PIN authentication flow
- Provides reactive state updates

### ViewerFlowScreen
- Main composable UI screen
- Displays different states (start, discovering, camera list)
- Handles user interactions

### Camera Cards
- Beautiful material design cards for each discovered camera
- Extracts and displays camera information from ONVIF data
- Provides clear call-to-action buttons

### Authentication Integration
- Uses existing SessionManager for PIN authentication
- Maintains security features like attempt limits and lockouts
- Provides user-friendly error messages

## Configuration

### Manifest Updates
The `AndroidManifest.xml` has been updated to include:
```xml
<activity
    android:name=".ViewerFlowActivity"
    android:exported="false"
    android:screenOrientation="user"
    android:theme="@style/Theme.CamConnect" />
```

### Permissions
The implementation uses existing permissions:
- `ACCESS_FINE_LOCATION` (optional, for WiFi scanning)
- `ACCESS_WIFI_STATE`
- `INTERNET`
- `ACCESS_NETWORK_STATE`

## Integration Points

### MainActivity Integration
- Handles viewer flow parameters passed from ViewerFlowActivity
- Displays camera connection confirmation
- Maintains existing functionality for owner users

### SessionManager Integration
- Uses existing PIN authentication system
- Maintains security features and lockout mechanisms
- Preserves authentication state management

### ONVIF Service Integration
- Uses existing `discoverOnvifDevices()` function
- Maintains existing device parsing logic
- Preserves all existing ONVIF functionality

## User Experience

### For Viewers
1. Simple "Start Streaming" button on login screen
2. Automatic camera discovery with visual feedback
3. Clear camera selection with detailed information
4. Streamlined PIN authentication
5. Helpful guidance when no cameras are found
6. Direct access to stream consumption

### Error Handling
- Network discovery failures
- Authentication errors with retry options
- No cameras found with helpful instructions
- WiFi connection guidance
- Permission handling

## Security Considerations

- PIN authentication maintains existing security measures
- Lockout mechanisms prevent brute force attacks
- Session management follows existing patterns
- No persistent storage of camera credentials
- Secure navigation between activities

## Future Enhancements

Potential improvements for future releases:
1. Camera favorites/bookmarks
2. QR code scanning for direct camera connection
3. Network diagnostics and troubleshooting
4. Advanced camera filtering and search
5. Multi-camera selection support
6. Offline camera list caching

## Testing

To test the Viewer Flow:
1. Launch the app and go to the login screen
2. Click "Start streaming as viewer" card
3. Click "Start Streaming" button
4. Verify ONVIF discovery works on your network
5. Test camera selection and PIN authentication
6. Verify WiFi settings flow when no cameras are found
7. Confirm successful navigation to MainActivity

## Dependencies

The implementation uses existing dependencies:
- Jetpack Compose for UI
- Material3 design system
- Kotlin Coroutines for async operations
- Existing ONVIF discovery service
- Existing SessionManager authentication

No new external dependencies were added.
