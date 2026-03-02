package com.outdu.camconnect.security

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Comprehensive security manager for camera permissions and usage monitoring.
 * 
 * Key Features:
 * - Runtime permission management with user consent tracking
 * - Camera access monitoring and logging
 * - Security policy enforcement
 * - Data protection and privacy controls
 * - Audit trail for compliance
 */
class CameraSecurityManager private constructor() : DefaultLifecycleObserver {
    
    companion object {
        private const val TAG = "CameraSecurityManager"
        private const val MAX_CAMERA_SESSION_DURATION = 30 * 60 * 1000L // 30 minutes
        private const val PERMISSION_RATIONALE_SHOWN_KEY = "camera_rationale_shown"
        
        @Volatile
        private var instance: CameraSecurityManager? = null
        
        fun getInstance(): CameraSecurityManager {
            return instance ?: synchronized(this) {
                instance ?: CameraSecurityManager().also { instance = it }
            }
        }
    }
    
    // Security state tracking
    private val _cameraPermissionGranted = MutableStateFlow(false)
    val cameraPermissionGranted: StateFlow<Boolean> = _cameraPermissionGranted.asStateFlow()
    
    private val _cameraInUse = MutableStateFlow(false)
    val cameraInUse: StateFlow<Boolean> = _cameraInUse.asStateFlow()
    
    private val _securityViolations = MutableStateFlow<List<SecurityViolation>>(emptyList())
    val securityViolations: StateFlow<List<SecurityViolation>> = _securityViolations.asStateFlow()
    
    // Session tracking
    private val cameraSessionStartTime = AtomicLong(0)
    private val permissionRequestCount = AtomicLong(0)
    private val isMonitoring = AtomicBoolean(false)
    
    // Security audit trail
    private val auditEvents = mutableListOf<AuditEvent>()
    private val auditLock = Any()
    
    // Coroutine scope for background operations
    private val securityScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Security violation data class
     */
    data class SecurityViolation(
        val type: ViolationType,
        val timestamp: Long,
        val details: String,
        val severity: Severity
    )
    
    /**
     * Types of security violations
     */
    enum class ViolationType {
        UNAUTHORIZED_ACCESS_ATTEMPT,
        EXCESSIVE_PERMISSION_REQUESTS,
        CAMERA_SESSION_TIMEOUT,
        PERMISSION_DENIED_MULTIPLE_TIMES,
        SUSPICIOUS_ACTIVITY
    }
    
    /**
     * Severity levels for violations
     */
    enum class Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    /**
     * Audit event for compliance tracking
     */
    data class AuditEvent(
        val action: String,
        val timestamp: Long,
        val userId: String? = null,
        val sessionId: String,
        val details: Map<String, Any> = emptyMap()
    )
    
    /**
     * Initialize security manager with lifecycle awareness
     */
    fun initialize(activity: ComponentActivity) {
        activity.lifecycle.addObserver(this)
        logAuditEvent("SECURITY_MANAGER_INITIALIZED", details = mapOf(
            "activity" to activity.javaClass.simpleName,
            "android_version" to Build.VERSION.SDK_INT
        ))
        
        // Check current permission state
        updatePermissionState(activity)
        
        Log.i(TAG, "CameraSecurityManager initialized for ${activity.javaClass.simpleName}")
    }
    
    /**
     * Request camera permission with security controls
     */
    fun requestCameraPermission(
        activity: ComponentActivity,
        rationale: String? = null,
        onResult: (granted: Boolean, showRationale: Boolean) -> Unit
    ) {
        val requestCount = permissionRequestCount.incrementAndGet()
        
        // Security check: Too many permission requests
        if (requestCount > 5) {
            logSecurityViolation(
                ViolationType.EXCESSIVE_PERMISSION_REQUESTS,
                "Too many permission requests: $requestCount",
                Severity.HIGH
            )
        }
        
        logAuditEvent("CAMERA_PERMISSION_REQUESTED", details = mapOf(
            "request_count" to requestCount,
            "has_rationale" to (rationale != null)
        ))
        
        val currentPermission = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.CAMERA
        )
        
        if (currentPermission == PackageManager.PERMISSION_GRANTED) {
            _cameraPermissionGranted.value = true
            onResult(true, false)
            logAuditEvent("CAMERA_PERMISSION_ALREADY_GRANTED")
            return
        }
        
        // Check if we should show rationale
        val shouldShowRationale = activity.shouldShowRequestPermissionRationale(
            Manifest.permission.CAMERA
        )
        
        if (shouldShowRationale && rationale != null) {
            // User has denied before, show rationale
            onResult(false, true)
            logAuditEvent("CAMERA_PERMISSION_RATIONALE_SHOWN")
            return
        }
        
        // Create permission launcher
        val permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            _cameraPermissionGranted.value = granted
            
            if (granted) {
                logAuditEvent("CAMERA_PERMISSION_GRANTED")
                startCameraSessionMonitoring()
            } else {
                logAuditEvent("CAMERA_PERMISSION_DENIED")
                
                // Check for repeated denials
                if (requestCount > 2) {
                    logSecurityViolation(
                        ViolationType.PERMISSION_DENIED_MULTIPLE_TIMES,
                        "Permission denied $requestCount times",
                        Severity.MEDIUM
                    )
                }
            }
            
            onResult(granted, false)
        }
        
        // Launch permission request
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    /**
     * Check if camera permission is granted
     */
    fun hasCameraPermission(context: Context): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        _cameraPermissionGranted.value = granted
        return granted
    }
    
    /**
     * Start camera usage session with security monitoring
     */
    fun startCameraSession(sessionId: String): Boolean {
        if (!_cameraPermissionGranted.value) {
            logSecurityViolation(
                ViolationType.UNAUTHORIZED_ACCESS_ATTEMPT,
                "Camera session started without permission",
                Severity.CRITICAL
            )
            return false
        }
        
        val currentTime = System.currentTimeMillis()
        cameraSessionStartTime.set(currentTime)
        _cameraInUse.value = true
        
        logAuditEvent("CAMERA_SESSION_STARTED", details = mapOf(
            "session_id" to sessionId,
            "start_time" to currentTime
        ))
        
        startCameraSessionMonitoring()
        
        Log.i(TAG, "Camera session started: $sessionId")
        return true
    }
    
    /**
     * End camera usage session
     */
    fun endCameraSession(sessionId: String) {
        val sessionDuration = System.currentTimeMillis() - cameraSessionStartTime.get()
        _cameraInUse.value = false
        
        logAuditEvent("CAMERA_SESSION_ENDED", details = mapOf(
            "session_id" to sessionId,
            "duration_ms" to sessionDuration
        ))
        
        stopCameraSessionMonitoring()
        
        Log.i(TAG, "Camera session ended: $sessionId, duration: ${sessionDuration}ms")
    }
    
    /**
     * Monitor camera session for security compliance
     */
    private fun startCameraSessionMonitoring() {
        if (isMonitoring.compareAndSet(false, true)) {
            securityScope.launch {
                while (isMonitoring.get() && _cameraInUse.value) {
                    delay(60000) // Check every minute
                    
                    val sessionDuration = System.currentTimeMillis() - cameraSessionStartTime.get()
                    
                    // Check for session timeout
                    if (sessionDuration > MAX_CAMERA_SESSION_DURATION) {
                        logSecurityViolation(
                            ViolationType.CAMERA_SESSION_TIMEOUT,
                            "Camera session exceeded maximum duration: ${sessionDuration}ms",
                            Severity.MEDIUM
                        )
                        
                        // Force end session for security
                        withContext(Dispatchers.Main) {
                            _cameraInUse.value = false
                        }
                        break
                    }
                }
                
                isMonitoring.set(false)
            }
        }
    }
    
    /**
     * Stop camera session monitoring
     */
    private fun stopCameraSessionMonitoring() {
        isMonitoring.set(false)
    }
    
    /**
     * Get security policy for camera usage
     */
    fun getSecurityPolicy(): SecurityPolicy {
        return SecurityPolicy(
            maxSessionDuration = MAX_CAMERA_SESSION_DURATION,
            maxPermissionRequests = 5,
            auditingEnabled = true,
            dataEncryptionRequired = true,
            dataRetentionPeriod = 30 * 24 * 60 * 60 * 1000L // 30 days
        )
    }
    
    /**
     * Security policy data class
     */
    data class SecurityPolicy(
        val maxSessionDuration: Long,
        val maxPermissionRequests: Int,
        val auditingEnabled: Boolean,
        val dataEncryptionRequired: Boolean,
        val dataRetentionPeriod: Long
    )
    
    /**
     * Validate camera usage against security policy
     */
    fun validateCameraUsage(context: Context): ValidationResult {
        val violations = mutableListOf<String>()
        
        // Check permission status
        if (!hasCameraPermission(context)) {
            violations.add("Camera permission not granted")
        }
        
        // Check session duration if active
        if (_cameraInUse.value) {
            val sessionDuration = System.currentTimeMillis() - cameraSessionStartTime.get()
            if (sessionDuration > MAX_CAMERA_SESSION_DURATION) {
                violations.add("Camera session duration exceeded policy limit")
            }
        }
        
        // Check permission request count
        if (permissionRequestCount.get() > 5) {
            violations.add("Excessive permission requests detected")
        }
        
        return ValidationResult(
            isValid = violations.isEmpty(),
            violations = violations,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Validation result data class
     */
    data class ValidationResult(
        val isValid: Boolean,
        val violations: List<String>,
        val timestamp: Long
    )
    
    /**
     * Generate security report for compliance
     */
    fun generateSecurityReport(): SecurityReport {
        synchronized(auditLock) {
            val totalSessions = auditEvents.count { it.action == "CAMERA_SESSION_STARTED" }
            val permissionRequests = auditEvents.count { it.action == "CAMERA_PERMISSION_REQUESTED" }
            val violations = _securityViolations.value
            
            return SecurityReport(
                generatedAt = System.currentTimeMillis(),
                totalCameraSessions = totalSessions,
                totalPermissionRequests = permissionRequests,
                securityViolations = violations,
                auditEvents = auditEvents.toList(),
                complianceStatus = if (violations.any { it.severity == Severity.CRITICAL }) 
                    "NON_COMPLIANT" else "COMPLIANT"
            )
        }
    }
    
    /**
     * Security report data class
     */
    data class SecurityReport(
        val generatedAt: Long,
        val totalCameraSessions: Int,
        val totalPermissionRequests: Int,
        val securityViolations: List<SecurityViolation>,
        val auditEvents: List<AuditEvent>,
        val complianceStatus: String
    )
    
    /**
     * Log security violation
     */
    private fun logSecurityViolation(
        type: ViolationType,
        details: String,
        severity: Severity
    ) {
        val violation = SecurityViolation(
            type = type,
            timestamp = System.currentTimeMillis(),
            details = details,
            severity = severity
        )
        
        val currentViolations = _securityViolations.value.toMutableList()
        currentViolations.add(violation)
        _securityViolations.value = currentViolations
        
        logAuditEvent("SECURITY_VIOLATION", details = mapOf(
            "type" to type.name,
            "severity" to severity.name,
            "details" to details
        ))
        
        Log.w(TAG, "Security violation: $type - $details (Severity: $severity)")
    }
    
    /**
     * Log audit event for compliance
     */
    private fun logAuditEvent(
        action: String,
        userId: String? = null,
        details: Map<String, Any> = emptyMap()
    ) {
        synchronized(auditLock) {
            val event = AuditEvent(
                action = action,
                timestamp = System.currentTimeMillis(),
                userId = userId,
                sessionId = generateSessionId(),
                details = details
            )
            
            auditEvents.add(event)
            
            // Cleanup old audit events (keep last 1000)
            if (auditEvents.size > 1000) {
                auditEvents.removeAt(0)
            }
        }
        
        Log.d(TAG, "Audit event: $action")
    }
    
    /**
     * Generate unique session ID
     */
    private fun generateSessionId(): String {
        val timestamp = System.currentTimeMillis().toString()
        val digest = MessageDigest.getInstance("MD5")
        val hash = digest.digest(timestamp.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }.take(8)
    }
    
    /**
     * Update permission state
     */
    private fun updatePermissionState(context: Context) {
        _cameraPermissionGranted.value = hasCameraPermission(context)
    }
    
    /**
     * Cleanup resources
     */
    private fun cleanup() {
        securityScope.cancel()
        isMonitoring.set(false)
        
        logAuditEvent("SECURITY_MANAGER_CLEANUP")
        Log.i(TAG, "CameraSecurityManager cleanup completed")
    }
    
    // Lifecycle callbacks
    override fun onPause(owner: LifecycleOwner) {
        // Automatically end camera session when app goes to background
        if (_cameraInUse.value) {
            logAuditEvent("CAMERA_SESSION_PAUSED_BY_LIFECYCLE")
            endCameraSession("lifecycle_pause")
        }
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        cleanup()
    }
}
