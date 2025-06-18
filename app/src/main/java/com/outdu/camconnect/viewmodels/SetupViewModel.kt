package com.outdu.camconnect.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SetupState(
    val isNetworkConfigured: Boolean = false,
    val isCameraConfigured: Boolean = false,
    val arePermissionsGranted: Boolean = false,
    val currentStep: Int = 0,
    val error: String? = null,
    val isSetupComplete: Boolean = false,
    // Registration related states
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isEmailVerified: Boolean = false,
    val verificationCode: String = "",
    val isRegistrationComplete: Boolean = false
)

class SetupViewModel : ViewModel() {
    private val _setupState = MutableStateFlow(SetupState())
    val setupState: StateFlow<SetupState> = _setupState.asStateFlow()

    fun updateNetworkConfig(isConfigured: Boolean) {
        _setupState.value = _setupState.value.copy(
            isNetworkConfigured = isConfigured,
            error = null
        )
        checkSetupCompletion()
    }

    fun updateCameraConfig(isConfigured: Boolean) {
        _setupState.value = _setupState.value.copy(
            isCameraConfigured = isConfigured,
            error = null
        )
        checkSetupCompletion()
    }

    fun updatePermissionsStatus(isGranted: Boolean) {
        _setupState.value = _setupState.value.copy(
            arePermissionsGranted = isGranted,
            error = null
        )
        checkSetupCompletion()
    }

    fun updateRegistrationDetails(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (password != confirmPassword) {
            setError("Passwords do not match")
            return
        }
        _setupState.value = _setupState.value.copy(
            username = username,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            error = null
        )
    }

    fun updateVerificationCode(code: String) {
        _setupState.value = _setupState.value.copy(
            verificationCode = code
        )
    }

    fun verifyEmail() {
        // Here you would typically make an API call to verify the code
        // For now, we'll just simulate successful verification
        _setupState.value = _setupState.value.copy(
            isEmailVerified = true,
            error = null
        )
    }

    private fun checkSetupCompletion() {
        val currentState = _setupState.value
        val isComplete = currentState.isNetworkConfigured &&
                currentState.isCameraConfigured &&
                currentState.arePermissionsGranted &&
                currentState.isEmailVerified

        _setupState.value = currentState.copy(isSetupComplete = isComplete)
    }

    fun setError(error: String) {
        _setupState.value = _setupState.value.copy(error = error)
    }

    fun clearError() {
        _setupState.value = _setupState.value.copy(error = null)
    }

    fun isSetupComplete(): Boolean {
        return _setupState.value.isSetupComplete
    }
} 