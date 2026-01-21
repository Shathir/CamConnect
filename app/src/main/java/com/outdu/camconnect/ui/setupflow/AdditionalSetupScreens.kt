package com.outdu.camconnect.ui.setupflow

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import com.outdu.camconnect.ui.theme.SpyBlue
import com.outdu.camconnect.utils.rememberDeviceType
import android.content.Intent
import android.provider.Settings
import android.os.Build
import android.util.Log
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import android.provider.Settings.Panel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import com.outdu.camconnect.utils.WifiConnectionManager
import com.outdu.camconnect.utils.WifiConnectionResult
import com.outdu.camconnect.utils.WifiCredentials
import com.outdu.camconnect.utils.WifiPersistResult

@Composable
fun PermissionScreen(
    onPermissionsGranted: () -> Unit
) {
    val deviceType = rememberDeviceType()
    val context = LocalContext.current

    // Required permissions
    val requiredPermissions = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.POST_NOTIFICATIONS
    )

    var permissionStates by remember { mutableStateOf(
        requiredPermissions.associateWith { false }
    ) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionStates = permissions
        // Check if all permissions are granted
        if (permissions.values.all { it }) {
            onPermissionsGranted()
        }
    }

    // Check current permissions on composition
    LaunchedEffect(Unit) {
        permissionStates = requiredPermissions.associateWith { permission ->
            context.checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        // If all permissions are already granted, proceed
        if (permissionStates.values.all { it }) {
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
                .padding(horizontal = 16.dp, vertical = 40.dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.stravion_logo),
                    contentDescription = "Scout Logo",
                    tint = SpyBlue,
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
                    text = "Permissions Required",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF1A1A1C)
                    )
                )

                Text(
                    text = "To provide the best experience, we need access to these device features:",
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
                PermissionItem(
                    title = "Camera",
                    description = "For QR code scanning and video streaming",
                    isGranted = permissionStates[Manifest.permission.CAMERA] ?: false,
                    icon = R.drawable.camera_line
                )

                PermissionItem(
                    title = "Location",
                    description = "For GPS tracking and navigation features",
                    isGranted = (permissionStates[Manifest.permission.ACCESS_FINE_LOCATION] ?: false) ||
                               (permissionStates[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false),
                    icon = R.drawable.earth_line
                )

                PermissionItem(
                    title = "Microphone",
                    description = "For audio recording during video capture",
                    isGranted = permissionStates[Manifest.permission.RECORD_AUDIO] ?: false,
                    icon = R.drawable.record_circle_line
                )

                PermissionItem(
                    title = "Notifications",
                    description = "For important alerts and updates",
                    isGranted = permissionStates[Manifest.permission.POST_NOTIFICATIONS] ?: false,
                    icon = R.drawable.settings_line
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
                        permissionLauncher.launch(requiredPermissions.toTypedArray())
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Grant Permissions",
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
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: Int
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
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.arial_regular)),
                    fontWeight = FontWeight(500),
                    color = Color(0xFF1A1A1C)
                )
            )

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

@Composable
fun CameraAddScreen(
    onAddCamera: () -> Unit,
    username: String = "James"
) {
    val deviceType = rememberDeviceType()

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
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.stravion_logo),
                    contentDescription = "Stravion Logo",
                    tint = StravionBlue,
                    modifier = Modifier
                        .width(120.dp)
                        .height(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Welcome message
            Text(
                text = "Welcome $username !",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontFamily = FontFamily(Font(R.font.arial_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF1A1A1C),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Empty state illustration
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        Color(0xFFF0F0F0),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Blue pill/capsule shape
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(20.dp)
                        .background(
                            StravionBlue,
                            RoundedCornerShape(10.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main message
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Looks like you are\nnew here",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF1A1A1C),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                )

                Text(
                    text = "You have no active cameras\nlinked to this account",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF9097A0),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Add camera button
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .clip(RoundedCornerShape(25.dp))
                    .background(StravionBlue)
                    .clickable { onAddCamera() }
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.camera_line),
                        contentDescription = "Add Camera",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = "Add Camera",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.arial_regular)),
                            fontWeight = FontWeight(500),
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // AI Licenses notification
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF4E6)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // AI icon with yellow background
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Color(0xFFFFB800),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ai_line),
                            contentDescription = "AI",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "AI Licenses found",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.arial_regular)),
                                fontWeight = FontWeight(500),
                                color = Color(0xFF1A1A1C)
                            )
                        )

                        Text(
                            text = "You have 3 AI licenses ready to be activated.",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(R.font.arial_regular)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF9097A0)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login with other account
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Login",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(500),
                        color = StravionBlue,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable {
                        // Handle login with other account
                    }
                )

                Text(
                    text = " with other account.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF9097A0)
                    )
                )
            }
        }
    }
}

@Composable
fun WifiConnectionScreen(
    cameraHostname: String,
    macId: String?,
    serialNumber: String?,
    manufacturedDate: String?,
    wifiCredentials: WifiCredentials? = null,
    onWifiConnected: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val tag = "WifiConnectionScreen"
    var wifiConnectError by remember { mutableStateOf<String?>(null) }
    var isAutoConnecting by remember { mutableStateOf(false) }

    // Activity result launcher for WiFi settings
    val wifiSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // When user returns from WiFi settings, proceed to PIN entry
        onWifiConnected()
    }

    // Activity result launcher for "Add/save WiFi network" dialog (Android 11+)
    val saveNetworkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // After the system dialog, user may be connected; proceed and let next steps validate as needed.
        onWifiConnected()
    }

    // Location permission launcher (required for WiFi connect APIs / SSID visibility on Android 10+)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d(tag, "Location permission result: $granted")
        if (!granted) {
            wifiConnectError = "Location permission is required to connect via QR. Please grant it and try again."
            isAutoConnecting = false
        }
    }

    // Auto-connect if WiFi credentials were provided via QR (this mirrors native camera behavior,
    // but Android still shows a system confirmation dialog).
    LaunchedEffect(wifiCredentials) {
        val creds = wifiCredentials ?: return@LaunchedEffect
        val manager = WifiConnectionManager(context)

        // Ensure location permission (required by Android for WiFi scanning/connection APIs)
        val hasLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasLocation) {
            Log.w(tag, "Missing ACCESS_FINE_LOCATION; requesting before attempting auto-connect")
            wifiConnectError = "Grant location permission to connect to WiFi automatically."
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return@LaunchedEffect
        }

        // Best-effort "native camera" behavior depends on Android version:
        // - Android 11+ (API 30+): use the system "Add/Save network" dialog (persists + can become active)
        // - Android 10 (API 29): use WifiNetworkSuggestion to persist, then open WiFi panel for user to connect/approve
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            isAutoConnecting = true
            wifiConnectError = null
            Log.i(tag, "Launching save-network system dialog for SSID='${creds.ssid}'")
            val saveIntent = manager.createSaveNetworkIntent(creds)
            if (saveIntent != null) {
                isAutoConnecting = false
                saveNetworkLauncher.launch(saveIntent)
            } else {
                isAutoConnecting = false
                wifiConnectError = "Save network dialog not available. Please connect to '${creds.ssid}' in WiFi settings."
                wifiSettingsLauncher.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
            return@LaunchedEffect
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            isAutoConnecting = true
            wifiConnectError = null
            Log.i(tag, "Android 10 detected; adding WifiNetworkSuggestion for SSID='${creds.ssid}'")
            when (val res = manager.addNetworkSuggestion(creds)) {
                is WifiPersistResult.Success,
                is WifiPersistResult.AlreadyExists -> {
                    isAutoConnecting = false
                    // Encourage user approval/connect via WiFi panel (quick settings-style)
                    try {
                        wifiSettingsLauncher.launch(Intent(Panel.ACTION_WIFI))
                    } catch (_: Exception) {
                        wifiSettingsLauncher.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
                    }
                }
                is WifiPersistResult.Failed -> {
                    isAutoConnecting = false
                    wifiConnectError = res.error
                    wifiSettingsLauncher.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
                }
            }
            return@LaunchedEffect
        }

        // Android 9 and below (or any other fallback)
        isAutoConnecting = false
        wifiConnectError = "Please connect to '${creds.ssid}' in WiFi settings to continue."
        wifiSettingsLauncher.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 40.dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.stravion_logo),
                    contentDescription = "Scout Logo",
                    tint = SpyBlue,
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
                    text = "Connect to Camera WiFi",
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF1A1A1C)
                    )
                )

                Text(
                    text = "Please connect to your camera's WiFi network to complete the setup.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF9097A0)
                    )
                )
            }

            // Camera details card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F9FA)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Camera Details",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.arial_regular)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFF1A1A1C)
                        )
                    )

                    if (macId != null) {
                        DetailRow("MAC ID:", macId)
                    }
                    if (serialNumber != null) {
                        DetailRow("Serial Number:", serialNumber)
                    }
                    if (manufacturedDate != null) {
                        DetailRow("Manufactured:", manufacturedDate)
                    }
                }
            }

            // WiFi connection instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = StravionBlue.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.wifi_line),
                            contentDescription = "WiFi",
                            tint = StravionBlue,
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = "WiFi Network Name:",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.arial_regular)),
                                fontWeight = FontWeight(500),
                                color = Color(0xFF1A1A1C)
                            )
                        )
                    }

                    Text(
                        text = cameraHostname,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.space_grotesk)),
                            fontWeight = FontWeight(700),
                            color = StravionBlue
                        ),
                        modifier = Modifier
                            .background(
                                Color.White,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )

                    if (isAutoConnecting) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = StravionBlue,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Waiting for WiFi connection confirmation…",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily(Font(R.font.arial_regular)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFF1A1A1C)
                                )
                            )
                        }
                    }

                    if (wifiConnectError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = wifiConnectError!!,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(R.font.arial_regular)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFFF3B30)
                            )
                        )
                    }
                }
            }

            // Instructions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Instructions:",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFF1A1A1C)
                    )
                )

                val instructions = listOf(
                    "1. Click 'Open WiFi Settings' below",
                    "2. Look for the network name: $cameraHostname",
                    "3. Connect to this network",
                    "4. Return to this app to continue"
                )

                instructions.forEach { instruction ->
                    Text(
                        text = instruction,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.arial_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF1A1A1C)
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Open WiFi Settings button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(StravionBlue)
                        .clickable {
                            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                            wifiSettingsLauncher.launch(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.wifi_line),
                            contentDescription = "WiFi Settings",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = "Open WiFi Settings",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.space_grotesk)),
                                fontWeight = FontWeight(700),
                                color = Color.White
                            )
                        )
                    }
                }

                // Skip button (for testing or if already connected)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = 1.dp,
                            color = StravionBlue,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onWifiConnected() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Already Connected",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.space_grotesk)),
                            fontWeight = FontWeight(700),
                            color = StravionBlue
                        ),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.arial_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF9097A0)
            )
        )

        Text(
            text = value,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.arial_regular)),
                fontWeight = FontWeight(500),
                color = Color(0xFF1A1A1C)
            )
        )
    }
}

@Composable
fun CameraPinEntryScreen(
    cameraHostname: String,
    onPinEntered: (String) -> Unit,
    onBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 40.dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.stravion_logo),
                    contentDescription = "Scout Logo",
                    tint = SpyBlue,
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
                    text = "Enter Camera PIN",
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF1A1A1C)
                    )
                )

                Text(
                    text = "Please enter the 4-digit PIN to connect to your camera.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF9097A0)
                    )
                )
            }

            // Connected network info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E8)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Connected",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )

                    Column {
                        Text(
                            text = "Connected to:",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(R.font.arial_regular)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF4CAF50)
                            )
                        )

                        Text(
                            text = cameraHostname,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.arial_regular)),
                                fontWeight = FontWeight(500),
                                color = Color(0xFF1A1A1C)
                            )
                        )
                    }
                }
            }

            // PIN input using the same component as ViewerLoginCard
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Camera Access Key",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFF1A1A1C)
                    )
                )

                CameraAccessKeyInput(
                    onPinEntered = { pin ->
                        // When PIN is exactly 4 digits, attempt connection
                        if (pin.length == 4 && !isLoading) {
                            isLoading = true
                            errorMessage = null
                            onPinEntered(pin)
                        }
                    },
                    isLoading = isLoading,
                    errorMessage = errorMessage
                )

                Text(
                    text = "Enter the 4-digit PIN found on your camera or documentation",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF9097A0)
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.dp,
                        color = StravionBlue,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Back to WiFi Settings",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.space_grotesk)),
                        fontWeight = FontWeight(700),
                        color = StravionBlue
                    ),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraAccessKeyInput(
    onPinEntered: (String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var accessKey by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isPinComplete = accessKey.length == 4

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = accessKey,
            onValueChange = { newValue ->
                if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                    accessKey = newValue
                    if (newValue.length == 4) {
                        onPinEntered(newValue)
                    }
                }
            },
            enabled = !isLoading,
            placeholder = {
                Text(
                    text = "Enter 4 digit Access Key",
                    color = Color(0xFFAAAAAA)
                )
            },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = {
                        isPasswordVisible = !isPasswordVisible
                    }) {
                        Icon(
                            painter = painterResource(
                                id = if (isPasswordVisible) R.drawable.eye else R.drawable.eye_slash
                            ),
                            contentDescription = if (isPasswordVisible) "Hide PIN" else "Show PIN",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF333333), shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.just_sans_regular))
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isPinComplete && !isLoading) {
                        onPinEntered(accessKey)
                    }
                }
            ),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White,
                disabledTextColor = Color.White.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Error message
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color(0xFFFF3B30),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.just_sans_regular))
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
