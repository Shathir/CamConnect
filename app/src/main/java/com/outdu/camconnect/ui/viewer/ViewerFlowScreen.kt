package com.outdu.camconnect.ui.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import com.outdu.camconnect.R
import com.outdu.camconnect.profiler.classifyPerformance
import com.outdu.camconnect.profiler.estimatePerformanceScore
import com.outdu.camconnect.profiler.getDeviceSpecs
import com.outdu.camconnect.profiler.getPerformanceMessage
import com.outdu.camconnect.profiler.PerformanceTier
import com.outdu.camconnect.services.OnvifDevice
import com.outdu.camconnect.viewmodels.ViewerFlowViewModel
import com.outdu.camconnect.ui.theme.*
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType
import android.util.Log


/**
 * Main screen for the Viewer Flow
 */
@Composable
fun ViewerFlowScreen(
    viewModel: ViewerFlowViewModel,
    onStartStreaming: () -> Unit,
    onCameraSelected: (OnvifDevice) -> Unit,
    onGoToWifiSettings: () -> Unit,
    onAuthenticationSuccess: (OnvifDevice) -> Unit,
    onBack: () -> Unit,
    onScanQRCode: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCamera by viewModel.selectedCamera.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    {
        // Header
        ViewerFlowHeader(onBack = onBack)

        Spacer(modifier = Modifier.height(32.dp))


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
        ) {

            when {
                // Initial state - show action buttons
                !uiState.isDiscovering && uiState.discoveredCameras.isEmpty() && !uiState.showNoCamerasDialog && !uiState.isConnectingToWifi && !uiState.wifiConnectionSuccess -> {
                    StartStreamingSection(
                        onStartStreaming = onStartStreaming,
                        onScanQRCode = onScanQRCode,
                        errorMessage = uiState.errorMessage,
                        wifiConnectionError = uiState.wifiConnectionError,
                        onClearError = viewModel::clearError,
                        onClearWifiError = viewModel::clearWifiConnectionError
                    )
                }
                
                // WiFi connection successful
                uiState.wifiConnectionSuccess -> {
                    WifiConnectionSuccessSection()
                }
                
                // Connecting to WiFi
                uiState.isConnectingToWifi -> {
                    ConnectingToWifiSection()
                }

                // Discovering cameras
                uiState.isDiscovering -> {
                    DiscoveringSection()
                }

                // Cameras found - show camera list
                uiState.discoveredCameras.isNotEmpty() -> {
                    CameraListSection(
                        cameras = uiState.discoveredCameras,
                        onCameraSelected = onCameraSelected,
                        onRetryDiscovery = viewModel::retryDiscovery
                    )
                }

                // No cameras found - show WiFi connection UI
                uiState.showNoCamerasDialog -> {
                    NoCamerasFoundScreen(
                        onGoToWifiSettings = onGoToWifiSettings,
                        onRetry = viewModel::retryDiscovery,
                        onManualIpEntry = viewModel::showManualIpDialog
                    )
                }
            }
        }
    }
    
    // PIN authentication bottom modal
    if (uiState.showPinDialog && selectedCamera != null) {
        PinAuthBottomModal(
            camera = selectedCamera!!,
            isAuthenticating = uiState.isAuthenticating,
            authError = uiState.authError,
            onPinEntered = { pin ->
                viewModel.authenticateWithPin(pin) { camera ->
                    onAuthenticationSuccess(camera)
                }
            },
            onDismiss = viewModel::dismissPinDialog,
            onClearAuthError = viewModel::clearAuthError
        )
    }
    
    // Manual IP entry dialog
    if (uiState.showManualIpDialog) {
        ManualIpDialog(
            onIpEntered = { ip ->
                viewModel.connectWithManualIp(ip)
            },
            onDismiss = viewModel::dismissManualIpDialog
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewerFlowHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stravion logo at top left

        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = StravionBlue
            )
        }

//        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            painter = painterResource(id = R.drawable.stravion_logo),
            contentDescription = "Stravion",
            modifier = Modifier.size(120.dp, 40.dp),
            tint = StravionBlue
        )
    }
}

@Composable
private fun StartStreamingSection(
    onStartStreaming: () -> Unit,
    onScanQRCode: () -> Unit,
    errorMessage: String?,
    wifiConnectionError: String?,
    onClearError: () -> Unit,
    onClearWifiError: () -> Unit
) {

    val deviceType = rememberDeviceType()

    Column(
        modifier = Modifier
//            .fillMaxWidth(if(deviceType == DeviceType.TABLET) 0.5f else 0.7f),
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome card
        Box(
            modifier = Modifier.fillMaxWidth()
        )
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(
//                containerColor = DarkBackground2
//            ),
//            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Camera",
                    modifier = Modifier.size(if(deviceType == DeviceType.TABLET)48.dp else 36.dp),
                    tint = StravionBlue
                )
                
                Text(
                    text = "Welcome Viewer,",
                    style = TextStyle(
                        fontSize = if(deviceType == DeviceType.TABLET) 24.sp else 18.sp,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF1A1A1C)
                    ),
                    textAlign = TextAlign.Center
                )
                
//                Text(
//                    text = "Ready to Stream",
//                    style = MaterialTheme.typography.titleLarge.copy(
//                        color = Color(0xFF1A1A1C)
//                    ),
//                    textAlign = TextAlign.Center
//                )
                
                Text(
                    text = "Click 'Start Streaming' to discover available cameras on your network.",
                    style = TextStyle(
                        fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
                        color = Color(0xFF9097A0)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Action buttons
        Column(
            modifier = Modifier.fillMaxWidth(if(deviceType == DeviceType.TABLET) 0.5f else 0.6f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Discover Devices button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(StravionBlue)
                    .clickable { onStartStreaming() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Discover Devices",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
            
            // Scan QR Code button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, StravionBlue, RoundedCornerShape(16.dp))
                    .clickable { onScanQRCode() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = StravionBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan QR Code",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StravionBlue
                        )
                    )
                }
            }
        }


        val specs = getDeviceSpecs(LocalContext.current)
        val score = estimatePerformanceScore(specs)
        val tier = classifyPerformance(score)
        val message = getPerformanceMessage(tier)

        Log.d("ViewerFlow1", "Performance score: $score")
        Log.d("ViewerFlow1", "Performance tier: $tier")
        Log.d("ViewerFlow1", "Performance message: $message")

        var showMessage by remember { mutableStateOf(false) }
        LaunchedEffect(message) {
            showMessage = true
        }

        if(showMessage) {
            val textColor = when (tier) {
                PerformanceTier.LOW, PerformanceTier.CRITICAL -> Color(0xFFFF3B30) // Red
                else -> Color(0xFF1A1A1C) // Black
            }
            
            Text(
                text = message,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.arial_regular)),
                    fontWeight = FontWeight(700),
                    color = textColor
                ),
                modifier = Modifier.fillMaxWidth(0.8f),
                textAlign = TextAlign.Center
            )
        }

        
        // Error messages
        if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = RedVariant.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = RedVariant
                    )
                    
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = RedVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(onClick = onClearError) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear error",
                            tint = RedVariant
                        )
                    }
                }
            }
        }
        
        if (wifiConnectionError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = RedVariant.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "WiFi Error",
                        tint = RedVariant
                    )
                    
                    Text(
                        text = wifiConnectionError,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = RedVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(onClick = onClearWifiError) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear WiFi error",
                            tint = RedVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WifiConnectionSuccessSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            modifier = Modifier.size(48.dp),
            tint = Color(0xFF4CAF50)
        )
        
        Text(
            text = "WiFi Connected Successfully!",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = White
            ),
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Starting camera discovery...",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MediumLightGray
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ConnectingToWifiSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(36.dp),
            color = StravionBlue,
            strokeWidth = 4.dp
        )
        
        Text(
            text = "Connecting to WiFi",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = White
            ),
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Connecting to the network... If this is your first time connecting, a system dialog may appear. After approval, Android will remember your choice and connect automatically.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MediumLightGray
            ),
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Once connected, camera discovery will start automatically.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MediumGray
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DiscoveringSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(
//                containerColor = DarkBackground2
//            ),
//            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//        )
        Box(
            modifier = Modifier.fillMaxWidth()
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = StravionBlue,
                    strokeWidth = 4.dp
                )
                
                Text(
                    text = "Discovering Cameras",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = White
                    ),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Scanning your network for available cameras...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MediumLightGray
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CameraListSection(
    cameras: List<OnvifDevice>,
    onCameraSelected: (OnvifDevice) -> Unit,
    onRetryDiscovery: () -> Unit
) {

    val deviceType = rememberDeviceType()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with retry button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Available Cameras",
                    style = TextStyle(
                        fontSize = if(deviceType == DeviceType.TABLET) 24.sp else 18.sp,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF1A1A1C)
                    )
                )
                Text(
                    text = "${cameras.size} camera${if (cameras.size != 1) "s" else ""} found",
                    style = TextStyle(
                        fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
                        fontWeight =FontWeight(500),
                        color = Color(0xFF9097A0)
                    )
                )
            }
            
            OutlinedButton(
                onClick = onRetryDiscovery,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = StravionBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFF1A1A1C)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retry",
                     style = TextStyle(
                         color = Color(0xFF1A1A1C)
                     ))
            }
        }
        
        // Camera list
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cameras) { camera ->
                CameraCard(
                    camera = camera,
                    onSelected = { onCameraSelected(camera) }
                )
            }
        }
    }
}

@Composable
private fun NoCamerasFoundScreen(
    onGoToWifiSettings: () -> Unit,
    onRetry: () -> Unit,
    onManualIpEntry: () -> Unit
) {

    val deviceType = rememberDeviceType()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if(deviceType == DeviceType.TABLET) 32.dp else 16.dp)
    ) {
        // Welcome message
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Welcome Viewer,",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = White
                ),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Connect to camera network\nto start streaming",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = White,
                    textAlign = TextAlign.Center
                )
            )
        }
        
        // WiFi screen image
        Image(
            painter = painterResource(id = R.drawable.wifiscreen),
            contentDescription = "WiFi Connection Screen",
            modifier = Modifier.size(300.dp),
            contentScale = ContentScale.Fit
        )
        
        // Instructions
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "With your camera switched ON",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MediumLightGray
                )
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Wi-fi menu > Connect with",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MediumLightGray
                    )
                )
                Text(
                    text = "Stravion",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        
        // Show Wi-Fi Networks button
        Box(

            modifier = Modifier
                .fillMaxWidth(if(deviceType == DeviceType.TABLET) 0.5f else 1f)
                .padding(16.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(StravionBlue)
                .clickable {onGoToWifiSettings()},
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Show Wi-fi Networks",
                style = TextStyle(
                    color = Color(0xFFF8F6F5),
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400),
                    fontFamily = FontFamily(Font(R.font.arial_regular))
                ),
                textAlign = TextAlign.Center
            )
        }

        OutlinedButton(
            onClick = onRetry,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
             Text("Retry")
         }
         
         // Manual IP Entry button
         OutlinedButton(
            onClick = onManualIpEntry,
            modifier = Modifier.fillMaxWidth(if(deviceType == DeviceType.TABLET) 0.5f else 1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = StravionBlue
            ),
            shape = RoundedCornerShape(12.dp)
         ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Enter IP Manually")
         }
         
         Spacer(modifier = Modifier.height(32.dp))
         
         // Warning message
         Column(
             horizontalAlignment = Alignment.CenterHorizontally,
             verticalArrangement = Arrangement.spacedBy(4.dp)
         ) {
             Text(
                 text = "To avoid errors,",
                 style = MaterialTheme.typography.bodySmall.copy(
                     color = MediumGray
                 ),
                 textAlign = TextAlign.Center
             )
             Text(
                 text = "DO not switch on multiple cameras at once",
                 style = MaterialTheme.typography.bodySmall.copy(
                     color = MediumGray,
                     fontWeight = FontWeight.Bold
                 ),
                 textAlign = TextAlign.Center
             )
         }
     }
 }

@Composable
private fun PinAuthBottomModal(
    camera: OnvifDevice,
    isAuthenticating: Boolean,
    authError: String?,
    onPinEntered: (String) -> Unit,
    onDismiss: () -> Unit,
    onClearAuthError: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    
    // Background overlay with fade
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        // Bottom modal content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .clickable { } // Prevent clicks from passing through
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Color(0xFFE0E0E0),
                        RoundedCornerShape(2.dp)
                    )
            )
            
            // Camera info section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Camera icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color(0xFFF0F0F0),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Camera",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF666666)
                    )
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = extractCameraName(camera),
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1C)
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
//                        Text(
//                            text = "MAC:",
//                            style = TextStyle(
//                                fontSize = 12.sp,
//                                color = Color(0xFF666666)
//                            )
//                        )
//                        Text(
//                            text = camera.ipAddress ?: "Unknown",
//                            style = TextStyle(
//                                fontSize = 12.sp,
//                                color = Color(0xFF666666)
//                            )
//                        )
                        Text(
                            text = "IP:",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        )
                        Text(
                            text = camera.ipAddress,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Online status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                StravionBlue,
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Text(
                        text = "Online",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = StravionBlue
                        )
                    )
                }
            }
            
            // PIN entry title
            Text(
                text = "Enter PIN to Connect",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1C)
                ),
                textAlign = TextAlign.Center
            )
            
            // PIN input section with clickable overlay
            val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
            
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // PIN input boxes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { focusRequester.requestFocus() },
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    repeat(4) { index ->
                        PinInputBox(
                            digit = pin.getOrNull(index)?.toString() ?: "",
                            isFocused = pin.length == index,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                
                // Invisible text field for input
                androidx.compose.foundation.text.BasicTextField(
                    value = pin,
                    onValueChange = { newPin ->
                        if (newPin.length <= 4 && newPin.all { it.isDigit() }) {
                            pin = newPin
                            if (authError != null) {
                                onClearAuthError()
                            }
                        }

                        if(newPin.length == 4 && newPin.all { it.isDigit() } && !isAuthenticating) {
                            onPinEntered(pin)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .focusRequester(focusRequester)
                        .alpha(0f), // Make it invisible
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )
            }
            
            // Auto-focus when modal appears
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
            
            // Error message
            if (authError != null) {
                Text(
                    text = authError,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFFFF3B30)
                    ),
                    textAlign = TextAlign.Center
                )
            }
            
            // Connect button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (pin.length == 4 && !isAuthenticating) StravionBlue 
                        else Color(0xFFE0E0E0)
                    )
                    .clickable(enabled = pin.length == 4 && !isAuthenticating) {
                        onPinEntered(pin)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isAuthenticating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Text(
                            text = "Connecting...",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        )
                    }
                } else {
                    Text(
                        text = "Connect",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (pin.length == 4) Color.White else Color(0xFF999999)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PinInputBox(
    digit: String,
    isFocused: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Color(0xFFF8F8F8),
                RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = if (isFocused) StravionBlue else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1C)
            )
        )
    }
}

@Composable
private fun ManualIpDialog(
    onIpEntered: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var ipAddress by remember { mutableStateOf("") }
    var ipError by remember { mutableStateOf<String?>(null) }
    
    // Background overlay with fade
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        // Center modal content
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .clickable { } // Prevent clicks from passing through
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title
            Text(
                text = "Enter Camera IP Address",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1C)
                )
            )
            
            // Description
            Text(
                text = "Enter the IP address of your camera to connect directly",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF9097A0),
                    textAlign = TextAlign.Center
                )
            )
            
            // IP Address Input Field
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { 
                        ipAddress = it
                        ipError = null
                    },
                    label = { Text("IP Address") },
                    placeholder = { Text("192.168.1.100") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    isError = ipError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StravionBlue,
                        focusedLabelColor = StravionBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Error message
                if (ipError != null) {
                    Text(
                        text = ipError!!,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
            
            // Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF9097A0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
                
                // Connect button
                Button(
                    onClick = {
                        val trimmedIp = ipAddress.trim()
                        if (trimmedIp.isEmpty()) {
                            ipError = "Please enter an IP address"
                        } else if (!isValidIpFormat(trimmedIp)) {
                            ipError = "Invalid IP address format"
                        } else {
                            onIpEntered(trimmedIp)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StravionBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Connect")
                }
            }
        }
    }
}

/**
 * Validate IP address format
 */
private fun isValidIpFormat(ip: String): Boolean {
    val ipPattern = Regex(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    )
    return ipPattern.matches(ip)
}

/**
 * Extract camera name from ONVIF device scopes or generate a default name
 */
private fun extractCameraName(camera: OnvifDevice): String {
    // Try to extract name from scopes
    camera.scopes.forEach { scope ->
        when {
            scope.contains("name/", ignoreCase = true) -> {
                val name = scope.substringAfterLast("/")
                if (name.isNotBlank()) return name
            }
            scope.contains("device_name/", ignoreCase = true) -> {
                val name = scope.substringAfterLast("/")
                if (name.isNotBlank()) return name
            }
            scope.contains("model/", ignoreCase = true) -> {
                val model = scope.substringAfterLast("/")
                if (model.isNotBlank()) return model
            }
        }
    }
    
    // Generate default name based on IP or use "NVeyetech_cam" as shown in image
    return "NVeyetech_cam"
}
