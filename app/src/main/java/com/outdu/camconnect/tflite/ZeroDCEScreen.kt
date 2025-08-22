package com.outdu.camconnect.tflite

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ZeroDCEScreen() {
    val context = LocalContext.current
    val vm: ZeroDCEViewModel = viewModel { 
        ZeroDCEViewModel(context.applicationContext as android.app.Application) 
    }
    val ui by vm.uiState.collectAsState()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        vm.onCameraPermission(isGranted)
    }

    LaunchedEffect(Unit) {
        if (!ui.cameraGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Title
        Text(
            text = "Zero-DCE Image Enhancement",
            modifier = Modifier.padding(16.dp)
        )
        
        Box(Modifier.weight(1f)) {
            if (ui.cameraGranted) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onImage = { imageProxy -> vm.onFrame(imageProxy) }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Camera permission required")
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