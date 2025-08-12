package com.outdu.camconnect.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outdu.camconnect.communication.CameraConfigurationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AiConfigurationUiState(
    val far: Boolean = false,           // FAR in Data.java
    val od: Boolean = false,            // OD in Data.java  
    val ds: Boolean = false,            // DS in Data.java (Depth Sensing)
    val audio: Boolean = false,         // AUDIO in Data.java
    val model: Int = 1,                 // MODEL in Data.java
    val dsThreshold: Float = 0.5f,      // DS_THRESHOLD in Data.java (Depth Sensing Threshold)
    val isLoading: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val errorMessage: String? = null
)

class AiConfigurationViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(AiConfigurationUiState())
    val uiState: StateFlow<AiConfigurationUiState> = _uiState.asStateFlow()
    
    fun loadConfiguration(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = CameraConfigurationManager.loadConfigurationAsync(context)
            result.fold(
                onSuccess = { config ->
                    _uiState.value = AiConfigurationUiState(
                        far = config.farDetectionEnabled,
                        od = config.objectDetectionEnabled,
                        ds = config.depthSensingEnabled,
                        audio = config.audioEnabled,
                        model = config.modelVersion,
                        dsThreshold = config.depthSensingThreshold,
                        isLoading = false,
                        hasUnsavedChanges = false
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load configuration: ${exception.message}"
                    )
                }
            )
        }
    }
    
    fun updateOD(enabled: Boolean) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            od = enabled,
            hasUnsavedChanges = true
        )
    }
    
    fun updateFAR(enabled: Boolean) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            far = enabled,
            hasUnsavedChanges = true
        )
    }
    
    fun updateDS(enabled: Boolean) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            ds = enabled,
            hasUnsavedChanges = true
        )
    }
    
    fun updateAudio(enabled: Boolean) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            audio = enabled,
            hasUnsavedChanges = true
        )
    }
    
    fun updateModel(version: Int) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            model = version,
            hasUnsavedChanges = true
        )
    }
    
    fun updateDsThreshold(threshold: Float) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            dsThreshold = threshold,
            hasUnsavedChanges = true
        )
    }
    
    fun saveConfiguration(context: Context, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
            
            val config = CameraConfigurationManager.CameraConfig(
                farDetectionEnabled = currentState.far,
                objectDetectionEnabled = currentState.od,
                depthSensingEnabled = currentState.ds,
                audioEnabled = currentState.audio,
                modelVersion = currentState.model,
                depthSensingThreshold = currentState.dsThreshold
            )
            
            val result = CameraConfigurationManager.updateConfiguration(context, config)
            result.fold(
                onSuccess = {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        hasUnsavedChanges = false,
                        errorMessage = null
                    )
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "Failed to save configuration: ${exception.message}"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun resetToDefaults(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = CameraConfigurationManager.resetToDefaults(context)
            result.fold(
                onSuccess = {
                    loadConfiguration(context)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to reset configuration: ${exception.message}"
                    )
                }
            )
        }
    }
} 