package com.outdu.camconnect.ui.setupflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outdu.camconnect.auth.UserStateManager
import com.outdu.camconnect.data.CameraRepository
import com.outdu.camconnect.data.StoredCamera
import com.outdu.camconnect.data.CameraWithStatus
import com.outdu.camconnect.services.discoverOnvifDevices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Example ViewModel showing how to integrate camera storage with ONVIF discovery
 */
class CameraManagementViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(CameraManagementUiState())
    val uiState: StateFlow<CameraManagementUiState> = _uiState.asStateFlow()
    
    private var cameraRepository: CameraRepository? = null
    
    fun initialize(repository: CameraRepository) {
        cameraRepository = repository
        
        // Observe camera changes
        viewModelScope.launch {
            repository.cameras.collect { cameras ->
                _uiState.value = _uiState.value.copy(
                    storedCameras = cameras,
                    camerasWithStatus = repository.getCamerasWithStatus()
                )
                
                // Update user state with camera count
                UserStateManager.updateRegisteredCamerasCount(cameras.size)
            }
        }
    }
    
    /**
     * Run ONVIF discovery and update camera online status
     */
    fun runDiscovery() {
        _uiState.value = _uiState.value.copy(isDiscovering = true)
        
        discoverOnvifDevices { discoveredDevices ->
            viewModelScope.launch {
                val onlineIPs = discoveredDevices.map { it.ipAddress }
                cameraRepository?.updateCameraOnlineStatus(onlineIPs)
                
                _uiState.value = _uiState.value.copy(
                    isDiscovering = false,
                    discoveredDevices = discoveredDevices.size,
                    camerasWithStatus = cameraRepository?.getCamerasWithStatus() ?: emptyList()
                )
            }
        }
    }
    
    /**
     * Add camera from QR scan
     */
    fun addCameraFromQR(qrData: String) {
        viewModelScope.launch {
            try {
                // Parse QR code data (format depends on your QR implementation)
                val camera = parseQRCodeToCamera(qrData)
                
                cameraRepository?.addCamera(camera)?.let { result ->
                    if (result.isSuccess) {
                        _uiState.value = _uiState.value.copy(
                            message = "Camera added successfully"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = result.exceptionOrNull()?.message ?: "Failed to add camera"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Invalid QR code: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Remove a camera
     */
    fun removeCamera(cameraId: String) {
        viewModelScope.launch {
            cameraRepository?.removeCamera(cameraId)?.let { result ->
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        message = "Camera removed successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to remove camera"
                    )
                }
            }
        }
    }
    
    /**
     * Sync with server
     */
    fun syncWithServer() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            
            cameraRepository?.syncWithServer()?.let { result ->
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    message = if (result.isSuccess) "Sync completed" else "Sync failed"
                )
            }
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
    
    private fun parseQRCodeToCamera(qrData: String): StoredCamera {
        // Example QR format: "camera:ip=192.168.1.100:port=80:name=Camera1:serial=ABC123"
        val parts = qrData.split(":")
        val params = parts.drop(1).associate { part ->
            val (key, value) = part.split("=", limit = 2)
            key to value
        }
        
        return StoredCamera(
            name = params["name"] ?: "Unknown Camera",
            ipAddress = params["ip"] ?: throw IllegalArgumentException("No IP address in QR code"),
            port = params["port"]?.toIntOrNull() ?: 80,
            serialNumber = params["serial"] ?: "",
            qrCodeData = qrData
        )
    }
}

data class CameraManagementUiState(
    val storedCameras: List<StoredCamera> = emptyList(),
    val camerasWithStatus: List<CameraWithStatus> = emptyList(),
    val isDiscovering: Boolean = false,
    val isSyncing: Boolean = false,
    val discoveredDevices: Int = 0,
    val message: String? = null,
    val error: String? = null
)

/**
 * Example Composable showing camera management UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraManagementScreen(
    viewModel: CameraManagementViewModel,
    onCameraSelected: (StoredCamera) -> Unit,
    onAddCamera: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize with camera repository
    LaunchedEffect(Unit) {
        UserStateManager.initialize(context)
        UserStateManager.getCameraRepository()?.let { repository ->
            viewModel.initialize(repository)
        }
    }
    
    // Show messages
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            viewModel.clearMessage()
        }
    }
    
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearMessage()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with actions
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "My Cameras (${uiState.storedCameras.size})",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.runDiscovery() },
                        enabled = !uiState.isDiscovering,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (uiState.isDiscovering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Discover")
                    }
                    
                    OutlinedButton(
                        onClick = onAddCamera,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Camera")
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.syncWithServer() },
                        enabled = !uiState.isSyncing,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sync")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Camera list
        if (uiState.camerasWithStatus.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No cameras added yet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add cameras by scanning QR codes or sync from server",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onAddCamera) {
                        Text("Add Your First Camera")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.camerasWithStatus, key = { it.camera.id }) { cameraWithStatus ->
                    CameraCard(
                        cameraWithStatus = cameraWithStatus,
                        onSelect = { onCameraSelected(cameraWithStatus.camera) },
                        onRemove = { viewModel.removeCamera(cameraWithStatus.camera.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraCard(
    cameraWithStatus: CameraWithStatus,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    val camera = cameraWithStatus.camera
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = camera.name.ifEmpty { "Camera ${camera.ipAddress}" },
                    style = MaterialTheme.typography.titleMedium
                )
                
                Surface(
                    color = if (cameraWithStatus.isOnline) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.error,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (cameraWithStatus.isOnline) "Online" else "Offline",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "IP: ${camera.ipAddress}:${camera.port}",
                style = MaterialTheme.typography.bodySmall
            )
            
            if (camera.manufacturer.isNotEmpty()) {
                Text(
                    text = "Manufacturer: ${camera.manufacturer}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (camera.model.isNotEmpty()) {
                Text(
                    text = "Model: ${camera.model}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Text(
                text = "Last seen: ${if (cameraWithStatus.isOnline) "Now" else "${cameraWithStatus.lastSeenMinutesAgo} minutes ago"}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSelect,
                    enabled = cameraWithStatus.isOnline,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (cameraWithStatus.isOnline) "Connect" else "Offline")
                }
                
                OutlinedButton(
                    onClick = onRemove,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Remove")
                }
            }
        }
    }
}
