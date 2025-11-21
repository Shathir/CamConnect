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
    
    // Store the original loaded state to compare against
    private var originalState: AiConfigurationUiState? = null
    
    // Helper function to check if current state differs from original
    private fun hasChanges(currentState: AiConfigurationUiState): Boolean {
        val original = originalState ?: return false
        return currentState.far != original.far ||
               currentState.od != original.od ||
               currentState.ds != original.ds ||
               currentState.audio != original.audio ||
               currentState.model != original.model ||
               currentState.dsThreshold != original.dsThreshold
    }
    
    fun loadConfiguration(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = CameraConfigurationManager.loadConfigurationAsync(context)
            result.fold(
                onSuccess = { config ->
                    val loadedState = AiConfigurationUiState(
                        far = config.farDetectionEnabled,
                        od = config.objectDetectionEnabled,
                        ds = config.depthSensingEnabled,
                        audio = config.audioEnabled,
                        model = config.modelVersion,
                        dsThreshold = config.depthSensingThreshold,
                        isLoading = false,
                        hasUnsavedChanges = false
                    )
                    originalState = loadedState
                    _uiState.value = loadedState
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
        val newState = currentState.copy(od = enabled)
        _uiState.value = newState.copy(hasUnsavedChanges = hasChanges(newState))
    }
    
    fun updateFAR(enabled: Boolean) {
        val currentState = _uiState.value
        val newState = currentState.copy(far = enabled)
        _uiState.value = newState.copy(hasUnsavedChanges = hasChanges(newState))
    }
    
    fun updateDS(enabled: Boolean) {
        val currentState = _uiState.value
        val newState = currentState.copy(ds = enabled)
        _uiState.value = newState.copy(hasUnsavedChanges = hasChanges(newState))
    }
    
    fun updateAudio(enabled: Boolean) {
        val currentState = _uiState.value
        val newState = currentState.copy(audio = enabled)
        _uiState.value = newState.copy(hasUnsavedChanges = hasChanges(newState))
    }
    
    fun updateModel(version: Int) {
        val currentState = _uiState.value
        val newState = currentState.copy(model = version)
        _uiState.value = newState.copy(hasUnsavedChanges = hasChanges(newState))
    }
    
    fun updateDsThreshold(threshold: Float) {
        val currentState = _uiState.value
        val newState = currentState.copy(dsThreshold = threshold)
        _uiState.value = newState.copy(hasUnsavedChanges = hasChanges(newState))
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
                    val savedState = currentState.copy(
                        isLoading = false,
                        hasUnsavedChanges = false,
                        errorMessage = null
                    )
                    originalState = savedState
                    _uiState.value = savedState
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