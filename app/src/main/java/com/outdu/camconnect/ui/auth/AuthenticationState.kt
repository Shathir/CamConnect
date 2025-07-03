package com.outdu.camconnect.ui.auth

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.outdu.camconnect.auth.SessionManager
import com.outdu.camconnect.auth.AuthenticationException
import com.outdu.camconnect.auth.InvalidPinException
import com.outdu.camconnect.auth.MaxAttemptsExceededException
import com.outdu.camconnect.auth.AuthenticationNetworkException
import com.outdu.camconnect.auth.LockoutInfo

/**
 * Represents the current state of PIN authentication
 */
data class PinAuthState(
    val pin: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val attemptsRemaining: Int = 3,
    val canAttempt: Boolean = true,
    val lockoutInfo: LockoutInfo = LockoutInfo(false, 0L, 0)
)

/**
 * ViewModel for managing PIN authentication state and operations
 */
class PinAuthViewModel : ViewModel() {
    
    private val _authState = MutableStateFlow(
        PinAuthState(
            attemptsRemaining = 3 - SessionManager.getPinAttempts(),
            lockoutInfo = SessionManager.getCurrentLockoutInfo()
        )
    )
    val authState: StateFlow<PinAuthState> = _authState.asStateFlow()
    
    init {
        // Start periodic lockout info updates
        startLockoutTimer()
    }
    
    /**
     * Start a coroutine that periodically updates lockout information
     */
    private fun startLockoutTimer() {
        viewModelScope.launch {
            while (true) {
                val lockoutInfo = SessionManager.getCurrentLockoutInfo()
                val currentState = _authState.value
                
                // Update state if lockout info has changed
                if (lockoutInfo != currentState.lockoutInfo) {
                    _authState.value = currentState.copy(
                        lockoutInfo = lockoutInfo,
                        canAttempt = !lockoutInfo.isLockedOut && SessionManager.canAttemptPin(),
                        attemptsRemaining = maxOf(0, 3 - SessionManager.getPinAttempts()),
                        errorMessage = if (lockoutInfo.isLockedOut) {
                            // Keep existing lockout error message if present
                            if (currentState.errorMessage?.contains("Account locked") == true) {
                                "Account locked. Retry in ${formatLockoutTime(lockoutInfo.remainingTime)}"
                            } else {
                                currentState.errorMessage
                            }
                        } else if (currentState.errorMessage?.contains("Account locked") == true) {
                            null // Clear lockout message when lockout expires
                        } else {
                            currentState.errorMessage
                        }
                    )
                }
                
                // Update every second if locked out, otherwise every 5 seconds
                delay(if (lockoutInfo.isLockedOut) 1000L else 5000L)
            }
        }
    }
    
    /**
     * Format lockout time for user display
     */
    private fun formatLockoutTime(timeMs: Long): String {
        val seconds = timeMs / 1000
        return when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
        }
    }

    /**
     * Update PIN input
     */
    fun updatePin(newPin: String) {
        // Only allow input if not locked out
        if (_authState.value.lockoutInfo.isLockedOut) return
        
        // Only allow numeric input up to 4 digits
        if (newPin.length <= 4 && newPin.all { it.isDigit() }) {
            _authState.value = _authState.value.copy(
                pin = newPin,
                errorMessage = null // Clear error when user starts typing
            )
            
            // Auto-submit when 4 digits are entered
            if (newPin.length == 4) {
                authenticateWithPin()
            }
        }
    }
    
    /**
     * Clear current PIN input
     */
    fun clearPin() {
        _authState.value = _authState.value.copy(
            pin = "",
            errorMessage = null
        )
    }
    
    /**
     * Add digit to current PIN
     */
    fun addDigit(digit: String) {
        val currentPin = _authState.value.pin
        if (currentPin.length < 4 && digit.length == 1 && digit.first().isDigit()) {
            updatePin(currentPin + digit)
        }
    }
    
    /**
     * Remove last digit from PIN
     */
    fun removeLastDigit() {
        val currentPin = _authState.value.pin
        if (currentPin.isNotEmpty()) {
            updatePin(currentPin.dropLast(1))
        }
    }
    
    /**
     * Authenticate with current PIN
     */
    fun authenticateWithPin() {
        val currentState = _authState.value
        
        if (currentState.pin.length != 4) {
            _authState.value = currentState.copy(
                errorMessage = "Please enter a 4-digit PIN"
            )
            return
        }
        
        if (!SessionManager.canAttemptPin()) {
            val lockoutInfo = SessionManager.getCurrentLockoutInfo()
            _authState.value = currentState.copy(
                errorMessage = if (lockoutInfo.isLockedOut) {
                    "Account locked. Retry in ${formatLockoutTime(lockoutInfo.remainingTime)}"
                } else {
                    "Maximum attempts exceeded. Please try again later."
                },
                canAttempt = false,
                lockoutInfo = lockoutInfo
            )
            return
        }
        
        // Set loading state
        _authState.value = currentState.copy(
            isLoading = true,
            errorMessage = null
        )
        
        // Perform authentication
        viewModelScope.launch {
            try {
                val result = SessionManager.authenticateWithPin(currentState.pin)
                
                if (result.isSuccess) {
                    _authState.value = PinAuthState(
                        pin = currentState.pin,
                        isLoading = false,
                        isSuccess = true,
                        attemptsRemaining = 3,
                        canAttempt = true,
                        lockoutInfo = LockoutInfo(false, 0L, 0)
                    )
                } else {
                    val exception = result.exceptionOrNull()
                    val updatedLockoutInfo = SessionManager.getCurrentLockoutInfo()
                    
                    val errorMessage = when (exception) {
                        is InvalidPinException -> "Invalid PIN. Please try again."
                        is MaxAttemptsExceededException -> {
                            if (updatedLockoutInfo.isLockedOut) {
                                "Account locked. Retry in ${formatLockoutTime(updatedLockoutInfo.remainingTime)}"
                            } else {
                                exception.message ?: "Too many attempts."
                            }
                        }
                        is AuthenticationNetworkException -> "Network error. Please check connection."
                        else -> "Authentication failed. Please try again."
                    }
                    
                    _authState.value = currentState.copy(
                        pin = "", // Clear PIN on failure for security
                        isLoading = false,
                        errorMessage = errorMessage,
                        attemptsRemaining = maxOf(0, 3 - SessionManager.getPinAttempts()),
                        canAttempt = SessionManager.canAttemptPin(),
                        lockoutInfo = updatedLockoutInfo
                    )
                }
            } catch (e: Exception) {
                val updatedLockoutInfo = SessionManager.getCurrentLockoutInfo()
                _authState.value = currentState.copy(
                    pin = "",
                    isLoading = false,
                    errorMessage = "Unexpected error occurred",
                    attemptsRemaining = maxOf(0, 3 - SessionManager.getPinAttempts()),
                    canAttempt = SessionManager.canAttemptPin(),
                    lockoutInfo = updatedLockoutInfo
                )
            }
        }
    }
    
    /**
     * Reset authentication state
     */
    fun resetState() {
        _authState.value = PinAuthState(
            attemptsRemaining = 3 - SessionManager.getPinAttempts(),
            lockoutInfo = SessionManager.getCurrentLockoutInfo()
        )
    }
    
    /**
     * Dismiss error message
     */
    fun dismissError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }
}

/**
 * Composable state holder for PIN authentication
 */
@Composable
fun rememberPinAuthState(): PinAuthState {
    val viewModel = remember { PinAuthViewModel() }
    return viewModel.authState.collectAsState().value
} 