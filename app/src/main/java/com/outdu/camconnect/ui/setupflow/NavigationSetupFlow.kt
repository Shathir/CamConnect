package com.outdu.camconnect.ui.setupflow

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.outdu.camconnect.auth.SetupFlowDetector
import com.outdu.camconnect.auth.UserStateManager
import com.outdu.camconnect.viewmodels.SetupState
import com.outdu.camconnect.auth.SessionManager
import com.outdu.camconnect.security.MandatoryPermissionScreen

/**
 * Navigation-based setup flow that determines the starting point
 * based on user state (first-time vs returning user)
 */
@Composable
fun NavigationSetupFlow(
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val setupFlowDetector = remember { SetupFlowDetector(context) }
    
    // Get the starting destination based on user state
    val startDestination = remember { setupFlowDetector.getStartingDestination() }
    val flowConfig = remember { setupFlowDetector.getSetupFlowConfig() }
    
    // Observe user state changes
    val userState by UserStateManager.userState.collectAsState()
    
    // Handle navigation based on state changes
    LaunchedEffect(userState) {
        // Auto-navigate based on user state changes
        when {
            userState.isAuthenticated && userState.hasCompletedSetup && userState.registeredCamerasCount > 0 -> {
                // User is fully set up - go to main activity
                onNavigateToMain()
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        
        // Landing screen for first-time users
        composable("landing") {
            if (flowConfig.showLanding) {
                LandingScreen(
                    onGetStarted = {
                        UserStateManager.markAppLaunched()
                        navController.navigate("login")
                    }
                )
            } else {
                // Skip to next step
                LaunchedEffect(Unit) {
                    navController.navigate("login")
                }
            }
        }
        
        // Login screen - using existing LoginScreen with both viewer and owner options
        composable("login") {
            val setupState = remember { SetupState() }
            LoginScreen(
                setupState = setupState,
                onNext = {
                    // This is for owner registration flow
                    UserStateManager.setUserType(UserStateManager.UserType.OWNER)
                    navController.navigate("camera_add")
                },
                onUpdateDetails = { username, email, password, confirmPassword ->
                    // Handle owner registration details
                    UserStateManager.setUserType(UserStateManager.UserType.OWNER)
                },
                onAuthenticate = { isAuthenticated ->
                    if (isAuthenticated) {
                        UserStateManager.updateAuthenticationStatus(true)
                        // For viewers, check if cameras are available
                        UserStateManager.setUserType(UserStateManager.UserType.VIEWER)
                        navController.navigate("viewer_camera_list")
                    }
                },
                onOwnerLogin = {
                    // Owner login clicked - set user type and navigate to owner flow
                    UserStateManager.setUserType(UserStateManager.UserType.OWNER)
                    UserStateManager.updateAuthenticationStatus(true)
                    navController.navigate("camera_add")
                }
            )
        }
        
        // Camera add screen - shows when user has no cameras
        composable("camera_add") {
            CameraAddScreen(
                onAddCamera = {
                    navController.navigate("qr_scanner")
                }
            )
        }
        
        // Permission screen - for first-time setup
        composable("permissions") {
            MandatoryPermissionScreen(
                onPermissionsGranted = {
                    navController.navigate("camera_add")
                }
            )
        }
        
        // QR Scanner for adding cameras - using your existing QRScannerScreen
        composable("qr_scanner") {
            if (flowConfig.showQRScanner) {
                QRScannerScreen(
                    onQRScanned = { qrData ->
                        // Process QR data and add camera to repository
                        UserStateManager.getCameraRepository()?.let { repo ->
                            // Parse QR data to extract camera info
                            // For now, simulate camera addition
                            val newCount = userState.registeredCamerasCount + 1
                            UserStateManager.updateRegisteredCamerasCount(newCount)
                        }
                        
                        // Navigate to WiFi connection with camera details
                        navController.navigate("wifi_connection/$qrData") {
                            popUpTo("qr_scanner") { inclusive = true }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // WiFi connection screen with QR data parameter
        composable("wifi_connection/{qrData}") { backStackEntry ->
            val qrData = backStackEntry.arguments?.getString("qrData") ?: ""
            // Parse QR data to extract camera details
            val cameraDetails = parseQRData(qrData)
            
            WifiConnectionScreen(
                cameraHostname = cameraDetails.hostname,
                macId = cameraDetails.macId,
                serialNumber = cameraDetails.serialNumber,
                manufacturedDate = cameraDetails.manufacturedDate,
                onWifiConnected = {
                    navController.navigate("camera_pin_entry/$qrData")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Camera PIN entry with QR data parameter
        composable("camera_pin_entry/{qrData}") { backStackEntry ->
            val qrData = backStackEntry.arguments?.getString("qrData") ?: ""
            val cameraDetails = parseQRData(qrData)
            
            CameraPinEntryScreen(
                cameraHostname = cameraDetails.hostname,
                onPinEntered = { pin ->
                    // Authenticate with camera using the PIN
                    // TODO: Implement actual camera authentication
                    // For now, simulate successful authentication
                    UserStateManager.markSetupCompleted()
                    navController.navigate("setup_complete")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Setup complete screen - using existing SetupCompleteScreen
        composable("setup_complete") {
            SetupCompleteScreen(
                onStartStreaming = {
                    // User has completed setup and wants to start streaming
                    onNavigateToMain()
                }
            )
        }
        
        // Camera connection screen - using existing CameraConnectionScreen
        composable("camera_connection") {
            CameraConnectionScreen(
                onConnectCamera = {
                    // Camera connected, proceed to main app
                    UserStateManager.markSetupCompleted()
                    onNavigateToMain()
                }
            )
        }
        
        // Email verification screen - using existing EmailVerificationScreen
        composable("email_verification") {
            val setupState = remember { SetupState() }
            EmailVerificationScreen(
                setupState = setupState,
                onVerify = {
                    // Email verified, proceed to next step
                    navController.navigate("camera_add")
                },
                onUpdateCode = { code ->
                    // Handle verification code update
                }
            )
        }
        
        // Viewer camera list - shows available cameras for viewers
        composable("viewer_camera_list") {
            ViewerCameraListScreen(
                onCameraSelected = { camera: CameraWithOnlineStatus ->
                    // Check if camera is online
                    if (camera.isOnline) {
                        navController.navigate("viewer_pin_entry/${camera.id}")
                    } else {
                        navController.navigate("viewer_wifi_connection/${camera.id}")
                    }
                },
                onNoCameras = {
                    // No cameras available, prompt to connect
                    navController.navigate("viewer_wifi_connection/discover")
                }
            )
        }
        
        // Viewer WiFi connection - for connecting to cameras directly
        composable("viewer_wifi_connection/{cameraId}") { backStackEntry ->
            val cameraId = backStackEntry.arguments?.getString("cameraId") ?: ""
            
            ViewerWifiConnectionScreen(
                cameraId = cameraId,
                onWifiConnected = {
                    navController.navigate("viewer_pin_entry/$cameraId")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Viewer PIN entry - for viewer authentication
        composable("viewer_pin_entry/{cameraId}") { backStackEntry ->
            val cameraId = backStackEntry.arguments?.getString("cameraId") ?: ""
            
            ViewerPinEntryScreen(
                cameraId = cameraId,
                onAuthenticated = {
                    UserStateManager.markSetupCompleted(UserStateManager.UserType.VIEWER)
                    onNavigateToMain()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Main activity destination (handled by parent)
        composable("main_activity") {
            LaunchedEffect(Unit) {
                onNavigateToMain()
            }
        }
    }
}

// Helper data class for parsing QR code data
data class CameraDetails(
    val hostname: String,
    val macId: String?,
    val serialNumber: String?,
    val manufacturedDate: String?
)

// Helper data class for camera with online status
data class CameraWithOnlineStatus(
    val id: String,
    val name: String,
    val isOnline: Boolean
)

// Helper function to parse QR code data
fun parseQRData(qrData: String): CameraDetails {
    // Parse QR data format: "hostname:CameraWiFi_12345;mac:AA:BB:CC:DD:EE:FF;serial:SN123456789;date:2024-01-15"
    val parts = qrData.split(";")
    val dataMap = parts.associate { part ->
        val (key, value) = part.split(":", limit = 2)
        key to value
    }
    
    return CameraDetails(
        hostname = dataMap["hostname"] ?: "CameraWiFi_Unknown",
        macId = dataMap["mac"],
        serialNumber = dataMap["serial"],
        manufacturedDate = dataMap["date"]
    )
}

// Viewer-specific screens (these would need to be implemented)
@Composable
fun ViewerCameraListScreen(
    onCameraSelected: (CameraWithOnlineStatus) -> Unit,
    onNoCameras: () -> Unit
) {
    // TODO: Implement viewer camera list screen
    // This should show discovered cameras and their online status
    // If no cameras are found, show option to connect to camera WiFi
    
    // For now, simulate with CameraAddScreen
    CameraAddScreen(
        onAddCamera = onNoCameras
    )
}

@Composable
fun ViewerWifiConnectionScreen(
    cameraId: String,
    onWifiConnected: () -> Unit,
    onBack: () -> Unit
) {
    // TODO: Implement viewer-specific WiFi connection
    // This should help viewer connect to camera's WiFi network
    
    // For now, use existing WifiConnectionScreen
    WifiConnectionScreen(
        cameraHostname = "CameraWiFi_$cameraId",
        macId = null,
        serialNumber = null,
        manufacturedDate = null,
        onWifiConnected = onWifiConnected,
        onBack = onBack
    )
}

@Composable
fun ViewerPinEntryScreen(
    cameraId: String,
    onAuthenticated: () -> Unit,
    onBack: () -> Unit
) {
    // TODO: Implement viewer-specific PIN entry
    // This should authenticate viewer with camera
    
    // For now, use existing CameraPinEntryScreen
    CameraPinEntryScreen(
        cameraHostname = "Camera_$cameraId",
        onPinEntered = { pin ->
            // TODO: Implement actual viewer authentication
            onAuthenticated()
        },
        onBack = onBack
    )
}

/**
 * Updated NavigationSetupFlow that uses the existing screens from:
 * - SetupScreens.kt: LandingScreen, LoginScreen, EmailVerificationScreen, CameraConnectionScreen, SetupCompleteScreen
 * - AdditionalSetupScreens.kt: PermissionScreen, CameraAddScreen, WifiConnectionScreen, CameraPinEntryScreen
 * - QRScannerScreen.kt: QRScannerScreen
 * 
 * Flow:
 * OWNER: LandingScreen -> LoginScreen -> CameraAddScreen -> QRScanner -> WifiConnectionScreen -> CameraPinEntryScreen -> SetupCompleteScreen -> MainActivity
 * VIEWER: LandingScreen -> LoginScreen -> ViewerCameraListScreen -> ViewerWifiConnectionScreen -> ViewerPinEntryScreen -> MainActivity
 */
