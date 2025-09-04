package com.outdu.camconnect.security

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Secure permission handler for camera access with comprehensive security controls.
 * 
 * This component provides:
 * - Runtime permission management with user education
 * - Security policy enforcement
 * - Privacy protection and transparency
 * - Audit trail for compliance
 */

/**
 * Secure camera permission state
 */
data class CameraPermissionState(
    val isGranted: Boolean = false,
    val shouldShowRationale: Boolean = false,
    val isPermanentlyDenied: Boolean = false,
    val hasRequestedBefore: Boolean = false
)

/**
 * Composable for secure camera permission handling
 */
@Composable
fun SecureCameraPermission(
    onPermissionResult: (granted: Boolean) -> Unit,
    showPermissionDialog: Boolean = true,
    customRationale: String? = null
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val securityManager = remember { CameraSecurityManager.getInstance() }
    
    var permissionState by remember { 
        mutableStateOf(CameraPermissionState()) 
    }
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    // Monitor permission state
    val isPermissionGranted by securityManager.cameraPermissionGranted.collectAsStateWithLifecycle()
    
    // Permission launcher with security logging
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionState = permissionState.copy(
            isGranted = granted,
            hasRequestedBefore = true,
            isPermanentlyDenied = !granted && activity?.shouldShowRequestPermissionRationale(
                Manifest.permission.CAMERA
            ) == false
        )
        
        onPermissionResult(granted)
        
        if (!granted && permissionState.isPermanentlyDenied) {
            showSettingsDialog = true
        }
    }
    
    // Check initial permission state
    LaunchedEffect(Unit) {
        if (activity != null) {
            securityManager.initialize(activity)
            
            val currentPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            )
            
            permissionState = permissionState.copy(
                isGranted = currentPermission == PackageManager.PERMISSION_GRANTED,
                shouldShowRationale = activity.shouldShowRequestPermissionRationale(
                    Manifest.permission.CAMERA
                )
            )
            
            if (permissionState.isGranted) {
                onPermissionResult(true)
            }
        }
    }
    
    // Request permission function
    fun requestPermission() {
        activity?.let { act ->
            securityManager.requestCameraPermission(
                activity = act,
                rationale = customRationale ?: getDefaultRationale()
            ) { granted, showRationale ->
                if (showRationale) {
                    showRationaleDialog = true
                } else if (granted) {
                    onPermissionResult(true)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
    
    // Auto-request permission if needed and allowed
    LaunchedEffect(showPermissionDialog) {
        if (showPermissionDialog && !permissionState.isGranted && !permissionState.hasRequestedBefore) {
            requestPermission()
        }
    }
    
    // Permission rationale dialog
    if (showRationaleDialog) {
        CameraPermissionRationaleDialog(
            rationale = customRationale ?: getDefaultRationale(),
            onAccept = {
                showRationaleDialog = false
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onDeny = {
                showRationaleDialog = false
                onPermissionResult(false)
            }
        )
    }
    
    // Settings dialog for permanently denied permission
    if (showSettingsDialog) {
        CameraPermissionSettingsDialog(
            onOpenSettings = {
                showSettingsDialog = false
                // Open app settings
                try {
                    val intent = android.content.Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("SecurePermissionHandler", "Failed to open settings", e)
                }
            },
            onDismiss = {
                showSettingsDialog = false
                onPermissionResult(false)
            }
        )
    }
}

/**
 * Camera permission rationale dialog
 */
@Composable
private fun CameraPermissionRationaleDialog(
    rationale: String,
    onAccept: () -> Unit,
    onDeny: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDeny,
        title = { Text("Camera Permission Required") },
        text = { 
            Text(rationale)
        },
        confirmButton = {
            Button(onClick = onAccept) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            Button(onClick = onDeny) {
                Text("Not Now")
            }
        }
    )
}

/**
 * Settings dialog for permanently denied permission
 */
@Composable
private fun CameraPermissionSettingsDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Camera Access Required") },
        text = { 
            Text(
                "Camera permission is required for this app to function. " +
                "Please enable it in the app settings to continue."
            )
        },
        confirmButton = {
            Button(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Get default rationale text for camera permission
 */
private fun getDefaultRationale(): String {
    return """
        CamConnect requires camera access to provide the following features:
        
        • Live camera streaming and monitoring
        • Object detection and AI analysis
        • Recording and capturing footage
        • Remote camera control
        
        Your privacy is protected:
        • Camera access is only used when you're actively using the app
        • No images or videos are stored without your consent
        • All data transmission is encrypted
        • You can revoke this permission at any time in settings
        
        Camera access will only be used for the stated purposes and will never be accessed in the background without your knowledge.
    """.trimIndent()
}

/**
 * Hook for secure camera permission management
 */
@Composable
fun rememberSecureCameraPermission(): SecureCameraPermissionState {
    val context = LocalContext.current
    val securityManager = remember { CameraSecurityManager.getInstance() }
    
    return remember {
        SecureCameraPermissionState(context, securityManager)
    }
}

/**
 * Secure camera permission state manager
 */
class SecureCameraPermissionState(
    private val context: Context,
    private val securityManager: CameraSecurityManager
) {
    private val _permissionGranted = mutableStateOf(false)
    val permissionGranted: State<Boolean> = _permissionGranted
    
    private val _sessionActive = mutableStateOf(false)
    val sessionActive: State<Boolean> = _sessionActive
    
    init {
        // Initialize permission state
        updatePermissionState()
    }
    
    /**
     * Check if camera permission is granted
     */
    fun hasPermission(): Boolean {
        return securityManager.hasCameraPermission(context)
    }
    
    /**
     * Start secure camera session
     */
    fun startCameraSession(sessionId: String = generateSessionId()): Boolean {
        if (!hasPermission()) {
            Log.w("SecureCameraPermission", "Attempted to start camera session without permission")
            return false
        }
        
        val started = securityManager.startCameraSession(sessionId)
        _sessionActive.value = started
        return started
    }
    
    /**
     * End camera session
     */
    fun endCameraSession(sessionId: String = "default") {
        securityManager.endCameraSession(sessionId)
        _sessionActive.value = false
    }
    
    /**
     * Get security validation result
     */
    fun validateSecurity(): CameraSecurityManager.ValidationResult {
        return securityManager.validateCameraUsage(context)
    }
    
    /**
     * Update permission state
     */
    private fun updatePermissionState() {
        _permissionGranted.value = hasPermission()
    }
    
    /**
     * Generate session ID
     */
    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}"
    }
}

/**
 * Utility function to check camera permission with security logging
 */
fun Context.hasSecureCameraPermission(): Boolean {
    val securityManager = CameraSecurityManager.getInstance()
    return securityManager.hasCameraPermission(this)
}

/**
 * Extension function for secure camera permission request
 */
fun ComponentActivity.requestSecureCameraPermission(
    rationale: String? = null,
    onResult: (granted: Boolean) -> Unit
) {
    val securityManager = CameraSecurityManager.getInstance()
    securityManager.initialize(this)
    securityManager.requestCameraPermission(this, rationale) { granted, showRationale ->
        // Handle the rationale case internally and just pass the granted status
        if (showRationale) {
            // If rationale should be shown, we treat it as not granted for now
            // The SecureCameraPermission composable handles rationale display better
            onResult(false)
        } else {
            onResult(granted)
        }
    }
}
