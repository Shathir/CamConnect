package com.outdu.camconnect.ui.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
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
import com.outdu.camconnect.R
import com.outdu.camconnect.services.OnvifDevice
import com.outdu.camconnect.viewmodels.ViewerFlowViewModel
import com.outdu.camconnect.ui.theme.*
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue

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
    onBack: () -> Unit
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
                // Initial state - show start streaming button
                !uiState.isDiscovering && uiState.discoveredCameras.isEmpty() && !uiState.showNoCamerasDialog -> {
                    StartStreamingSection(
                        onStartStreaming = onStartStreaming,
                        errorMessage = uiState.errorMessage,
                        onClearError = viewModel::clearError
                    )
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
                        onRetry = viewModel::retryDiscovery
                    )
                }
            }
        }
    }
    
    // PIN authentication dialog
    if (uiState.showPinDialog && selectedCamera != null) {
        ViewerPinAuthDialog(
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
    errorMessage: String?,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.5f),
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
                    modifier = Modifier.size(48.dp),
                    tint = StravionBlue
                )
                
                Text(
                    text = "Welcome Viewer,",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
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
                    text = "Click 'Start Streaming' to discover available cameras on your network. You'll be able to connect and view streams from cameras that you have access to.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF9097A0)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Start Streaming button
//        Button(
//            onClick = onStartStreaming,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = MaterialTheme.colorScheme.primary,
//                contentColor = White
//            )
//        )
        Box(
            modifier = Modifier.fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(StravionBlue)
                .clickable { onStartStreaming() },
            contentAlignment = Alignment.Center
        )
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Streaming",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
        
        // Error message
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
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                )
                Text(
                    text = "${cameras.size} camera${if (cameras.size != 1) "s" else ""} found",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MediumLightGray
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
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
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
            modifier = Modifier,
            contentScale = ContentScale.FillBounds
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
                    text = "NVE_Stravion",
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
                .wrapContentWidth()
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
