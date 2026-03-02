package com.outdu.camconnect.security

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue

/**
 * Manager for handling mandatory permissions (Camera and Location)
 * Blocks app usage until these permissions are granted
 */
class MandatoryPermissionManager private constructor() {
    
    companion object {
        private const val TAG = "MandatoryPermissionManager"
        
        // Mandatory permissions that must be granted for app to function
        val MANDATORY_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        @Volatile
        private var instance: MandatoryPermissionManager? = null
        
        fun getInstance(): MandatoryPermissionManager {
            return instance ?: synchronized(this) {
                instance ?: MandatoryPermissionManager().also { instance = it }
            }
        }
    }
    
    /**
     * Check if all mandatory permissions are granted
     */
    fun hasAllMandatoryPermissions(context: Context): Boolean {
        return MANDATORY_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Check if camera permission is granted
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if location permission is granted (either fine or coarse)
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }
    
    /**
     * Get list of missing mandatory permissions
     */
    fun getMissingMandatoryPermissions(context: Context): List<String> {
        return MANDATORY_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Open app settings for permission management
     */
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open app settings", e)
        }
    }
}

/**
 * Composable for mandatory permission checking screen
 * Blocks the app until mandatory permissions are granted
 */
@Composable
fun MandatoryPermissionScreen(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val permissionManager = remember { MandatoryPermissionManager.getInstance() }
    
    var permissionStates by remember { mutableStateOf(
        MandatoryPermissionManager.MANDATORY_PERMISSIONS.associateWith { false }
    ) }
    
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionStates = permissions
        
        // Check if all mandatory permissions are granted
        val allGranted = MandatoryPermissionManager.MANDATORY_PERMISSIONS.all { permission ->
            permissions[permission] == true
        }
        
        if (allGranted) {
            onPermissionsGranted()
        } else {
            // Check if any permissions are permanently denied
            val activity = context as? ComponentActivity
            val hasPermanentlyDenied = MandatoryPermissionManager.MANDATORY_PERMISSIONS.any { permission ->
                permissions[permission] == false && 
                activity?.shouldShowRequestPermissionRationale(permission) == false
            }
            
            if (hasPermanentlyDenied) {
                showSettingsDialog = true
            }
        }
    }
    
    // Check current permissions on composition
    LaunchedEffect(Unit) {
        permissionStates = MandatoryPermissionManager.MANDATORY_PERMISSIONS.associateWith { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        // If all permissions are already granted, proceed
        if (permissionManager.hasAllMandatoryPermissions(context)) {
            onPermissionsGranted()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.stravion_logo),
                    contentDescription = "Stravion Logo",
                    tint = StravionBlue,
                    modifier = Modifier
                        .width(88.dp)
                        .height(12.dp)
                )
            }
            
            // Title and description
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Required Permissions",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF1A1A1C)
                    )
                )
                
                Text(
                    text = "These permissions are required for the app to function properly. Please grant them to continue.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF9097A0)
                    )
                )
            }
            
            // Permission list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MandatoryPermissionItem(
                    title = "Camera",
                    description = "Required for QR code scanning and camera access",
                    isGranted = permissionStates[Manifest.permission.CAMERA] ?: false,
                    icon = R.drawable.camera_line,
                    isMandatory = true
                )
                
                MandatoryPermissionItem(
                    title = "Location",
                    description = "Required for GPS tracking and device location",
                    isGranted = (permissionStates[Manifest.permission.ACCESS_FINE_LOCATION] ?: false) ||
                               (permissionStates[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false),
                    icon = R.drawable.earth_line,
                    isMandatory = true
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Grant permissions button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(StravionBlue)
                    .clickable {
                        permissionLauncher.launch(MandatoryPermissionManager.MANDATORY_PERMISSIONS)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Grant Required Permissions",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.space_grotesk)),
                        fontWeight = FontWeight(700),
                        color = Color.White
                    ),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
    
    // Settings dialog for permanently denied permissions
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = "Permissions Required",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF1A1A1C)
                    )
                )
            },
            text = {
                Text(
                    text = "Some permissions have been permanently denied. Please enable them in the app settings to continue using the application.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF9097A0)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSettingsDialog = false
                        permissionManager.openAppSettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StravionBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSettingsDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MandatoryPermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: Int,
    isMandatory: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            tint = if (isGranted) Color(0xFF4CAF50) else Color(0xFF9097A0),
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFF1A1A1C)
                    )
                )
                
                if (isMandatory) {
                    Text(
                        text = "(Required)",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(R.font.arial_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFFFF5722)
                        )
                    )
                }
            }
            
            Text(
                text = description,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.arial_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF9097A0)
                )
            )
        }
        
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = if (isGranted) "Granted" else "Not granted",
            tint = if (isGranted) Color(0xFF4CAF50) else Color(0xFFFF9800),
            modifier = Modifier.size(20.dp)
        )
    }
}
