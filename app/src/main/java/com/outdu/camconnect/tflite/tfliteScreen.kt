package com.outdu.camconnect.tflite

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember
import android.Manifest
import androidx.compose.runtime.mutableStateOf
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

// Original PairLIE MainScreen
@Composable
fun MainScreen(vm: PairLIEViewModel = viewModel()) {
    val ctx = LocalContext.current
    val ui by vm.uiState.collectAsState()
    var hasPermission by remember { mutableStateOf(false) }

    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        vm.onCameraPermission(isGranted)
    }

    // Check camera permission on first composition
    LaunchedEffect(Unit) {
        val permission = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        if (permission) {
            hasPermission = true
            vm.onCameraPermission(true)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Title
        Text(
            text = "PairLIE Image Enhancement",
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp
        )
        
        Box(Modifier.weight(1f)) {
            if (ui.cameraGranted) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onImage = { imageProxy -> vm.onFrame(imageProxy) }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera permission required")
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            // Show enhanced frame
            ui.enhancedBitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "PairLIE Enhanced Output",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Performance metrics
        Text(
            text = "FPS: ${"%.1f".format(ui.fps)}   Inference: ${"%.1f".format(ui.lastMs)} ms",
            color = androidx.compose.ui.graphics.Color.Red,
            modifier = Modifier.padding(12.dp)
        )
        
        // Debug info
        if (ui.fps > 0 || ui.lastMs > 0) {
            Text(
                text = "✅ PairLIE Model loaded and running inference",
                color = androidx.compose.ui.graphics.Color.Green,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        } else {
            Text(
                text = "⚠️ PairLIE Model not running - check logs",
                color = androidx.compose.ui.graphics.Color.Red,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
        
        // Show if enhanced image is available
        if (ui.enhancedBitmap != null) {
            Text(
                text = "🖼️ PairLIE Enhanced image displayed",
                color = androidx.compose.ui.graphics.Color.Blue,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

// Zero-DCE MainScreen
@Composable
fun MainScreenZeroDCE(vm: ZeroDCEViewModel = viewModel()) {
    val ctx = LocalContext.current
    val ui by vm.uiState.collectAsState()
    var hasPermission by remember { mutableStateOf(false) }

    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        vm.onCameraPermission(isGranted)
    }

    // Check camera permission on first composition
    LaunchedEffect(Unit) {
        val permission = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        if (permission) {
            hasPermission = true
            vm.onCameraPermission(true)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Title
        Text(
            text = "Zero-DCE Image Enhancement",
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp
        )
        
        Box(Modifier.weight(1f)) {
            if (ui.cameraGranted) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onImage = { imageProxy -> vm.onFrame(imageProxy) }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera permission required")
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            // Show enhanced frame
            ui.enhancedBitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Zero-DCE Enhanced Output",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Performance metrics
        Text(
            text = "FPS: ${"%.1f".format(ui.fps)}   Inference: ${"%.1f".format(ui.lastMs)} ms",
            color = androidx.compose.ui.graphics.Color.Red,
            modifier = Modifier.padding(12.dp)
        )
        
        // Debug info
        if (ui.fps > 0 || ui.lastMs > 0) {
            Text(
                text = "✅ Zero-DCE Model loaded and running inference",
                color = androidx.compose.ui.graphics.Color.Green,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        } else {
            Text(
                text = "⚠️ Zero-DCE Model not running - check logs",
                color = androidx.compose.ui.graphics.Color.Red,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
        
        // Show if enhanced image is available
        if (ui.enhancedBitmap != null) {
            Text(
                text = "🖼️ Zero-DCE Enhanced image displayed",
                color = androidx.compose.ui.graphics.Color.Blue,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

// Combined screen with toggle (keeping for reference if needed)
@Composable
fun MainScreenCombined() {
    val ctx = LocalContext.current
    
    // State for model selection
    var selectedModel by remember { mutableStateOf("PairLIE") }
    
    // ViewModels for both models
    val pairLIEViewModel: PairLIEViewModel = viewModel { PairLIEViewModel(ctx.applicationContext as android.app.Application) }
    val zeroDCEViewModel: ZeroDCEViewModel = viewModel { ZeroDCEViewModel(ctx.applicationContext as android.app.Application) }
    
    // Get UI state from the selected model
    val pairLIEUI by pairLIEViewModel.uiState.collectAsState()
    val zeroDCEUI by zeroDCEViewModel.uiState.collectAsState()
    
    var hasPermission by remember { mutableStateOf(false) }

    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        // Grant permission to both ViewModels
        pairLIEViewModel.onCameraPermission(isGranted)
        zeroDCEViewModel.onCameraPermission(isGranted)
    }

    // Check camera permission on first composition
    LaunchedEffect(Unit) {
        val permission = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        if (permission) {
            hasPermission = true
            pairLIEViewModel.onCameraPermission(true)
            zeroDCEViewModel.onCameraPermission(true)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Model Selection Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { selectedModel = "PairLIE" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedModel == "PairLIE") MaterialTheme.colorScheme.primary else Color.Gray
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("PairLIE")
            }
            
            Button(
                onClick = { selectedModel = "ZeroDCE" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedModel == "ZeroDCE") MaterialTheme.colorScheme.primary else Color.Gray
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Zero-DCE")
            }
        }
        
        // Model Description
        Text(
            text = when (selectedModel) {
                "PairLIE" -> "PairLIE: Pair Learning for Image Enhancement - Real-time processing"
                "ZeroDCE" -> "Zero-DCE: Zero-Reference Deep Curve Estimation - Advanced enhancement"
                else -> ""
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 12.sp,
            color = Color.Gray
        )
        
        Box(Modifier.weight(1f)) {
            // Show camera preview based on selected model
            if (hasPermission) {
                when (selectedModel) {
                    "PairLIE" -> {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            onImage = { imageProxy -> pairLIEViewModel.onFrame(imageProxy) }
                        )
                        
                        // Show PairLIE enhanced frame
                        pairLIEUI.enhancedBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "PairLIE Enhanced Output",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    "ZeroDCE" -> {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            onImage = { imageProxy -> zeroDCEViewModel.onFrame(imageProxy) }
                        )
                        
                        // Show Zero-DCE enhanced frame
                        zeroDCEUI.enhancedBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Zero-DCE Enhanced Output",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera permission required")
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
        
        // Performance metrics based on selected model
        when (selectedModel) {
            "PairLIE" -> {
                Text(
                    text = "[PairLIE] FPS: ${"%.1f".format(pairLIEUI.fps)}   Inference: ${"%.1f".format(pairLIEUI.lastMs)} ms",
                    color = androidx.compose.ui.graphics.Color.Red,
                    modifier = Modifier.padding(12.dp)
                )
                
                // Debug info
                if (pairLIEUI.fps > 0 || pairLIEUI.lastMs > 0) {
                    Text(
                        text = "✅ PairLIE Model loaded and running inference",
                        color = androidx.compose.ui.graphics.Color.Green,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                } else {
                    Text(
                        text = "⚠️ PairLIE Model not running - check logs",
                        color = androidx.compose.ui.graphics.Color.Red,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                
                // Show if enhanced image is available
                if (pairLIEUI.enhancedBitmap != null) {
                    Text(
                        text = "🖼️ PairLIE Enhanced image displayed",
                        color = androidx.compose.ui.graphics.Color.Blue,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
            "ZeroDCE" -> {
                Text(
                    text = "[Zero-DCE] FPS: ${"%.1f".format(zeroDCEUI.fps)}   Inference: ${"%.1f".format(zeroDCEUI.lastMs)} ms",
                    color = androidx.compose.ui.graphics.Color.Red,
                    modifier = Modifier.padding(12.dp)
                )
                
                // Debug info
                if (zeroDCEUI.fps > 0 || zeroDCEUI.lastMs > 0) {
                    Text(
                        text = "✅ Zero-DCE Model loaded and running inference",
                        color = androidx.compose.ui.graphics.Color.Green,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                } else {
                    Text(
                        text = "⚠️ Zero-DCE Model not running - check logs",
                        color = androidx.compose.ui.graphics.Color.Red,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                
                // Show if enhanced image is available
                if (zeroDCEUI.enhancedBitmap != null) {
                    Text(
                        text = "🖼️ Zero-DCE Enhanced image displayed",
                        color = androidx.compose.ui.graphics.Color.Blue,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    }
}