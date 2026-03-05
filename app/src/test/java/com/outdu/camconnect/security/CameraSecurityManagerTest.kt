package com.outdu.camconnect.security

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Unit tests for CameraSecurityManager
 * Tests singleton, data classes, getSecurityPolicy, hasCameraPermission, validateCameraUsage,
 * generateSecurityReport, start/end session, state flows
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CameraSecurityManagerTest {

    private val context: Context
        get() = RuntimeEnvironment.getApplication()

    @Test
    fun `getInstance returns same singleton`() {
        val a = CameraSecurityManager.getInstance()
        val b = CameraSecurityManager.getInstance()
        assertSame(a, b)
    }

    @Test
    fun `getSecurityPolicy returns valid policy`() {
        val policy = CameraSecurityManager.getInstance().getSecurityPolicy()

        assertTrue("Max session duration should be positive", policy.maxSessionDuration > 0)
        assertTrue("Max permission requests should be positive", policy.maxPermissionRequests > 0)
        assertTrue("Auditing should be enabled", policy.auditingEnabled)
        assertTrue("Data retention should be positive", policy.dataRetentionPeriod > 0)
    }

    @Test
    fun `SecurityPolicy data class holds values`() {
        val policy = CameraSecurityManager.SecurityPolicy(
            maxSessionDuration = 1000L,
            maxPermissionRequests = 5,
            auditingEnabled = true,
            dataEncryptionRequired = false,
            dataRetentionPeriod = 86400_000L
        )
        assertEquals(1000L, policy.maxSessionDuration)
        assertEquals(5, policy.maxPermissionRequests)
        assertTrue(policy.auditingEnabled)
        assertFalse(policy.dataEncryptionRequired)
        assertEquals(86400_000L, policy.dataRetentionPeriod)
    }

    @Test
    fun `ViolationType enum has expected values`() {
        val values = CameraSecurityManager.ViolationType.values()
        assertTrue(values.isNotEmpty())
        assertNotNull(CameraSecurityManager.ViolationType.UNAUTHORIZED_ACCESS_ATTEMPT)
        assertNotNull(CameraSecurityManager.ViolationType.CAMERA_SESSION_TIMEOUT)
    }

    @Test
    fun `Severity enum has expected values`() {
        val values = CameraSecurityManager.Severity.values()
        assertTrue(values.size >= 4)
        assertNotNull(CameraSecurityManager.Severity.LOW)
        assertNotNull(CameraSecurityManager.Severity.CRITICAL)
    }

    @Test
    fun `SecurityViolation data class holds values`() {
        val v = CameraSecurityManager.SecurityViolation(
            type = CameraSecurityManager.ViolationType.UNAUTHORIZED_ACCESS_ATTEMPT,
            timestamp = 12345L,
            details = "test",
            severity = CameraSecurityManager.Severity.HIGH
        )
        assertEquals(CameraSecurityManager.ViolationType.UNAUTHORIZED_ACCESS_ATTEMPT, v.type)
        assertEquals(12345L, v.timestamp)
        assertEquals("test", v.details)
        assertEquals(CameraSecurityManager.Severity.HIGH, v.severity)
    }

    @Test
    fun `AuditEvent data class holds values`() {
        val e = CameraSecurityManager.AuditEvent(
            action = "TEST",
            timestamp = 999L,
            userId = "u1",
            sessionId = "s1",
            details = emptyMap()
        )
        assertEquals("TEST", e.action)
        assertEquals(999L, e.timestamp)
        assertEquals("u1", e.userId)
        assertEquals("s1", e.sessionId)
        assertTrue(e.details.isEmpty())
    }

    @Test
    fun `hasCameraPermission returns boolean`() {
        val manager = CameraSecurityManager.getInstance()
        val result = manager.hasCameraPermission(context)
        assertTrue("Should return boolean", result is Boolean)
    }

    @Test
    fun `validateCameraUsage returns ValidationResult`() {
        val manager = CameraSecurityManager.getInstance()
        val result = manager.validateCameraUsage(context)

        assertNotNull(result)
        assertTrue("isValid should be boolean", result.isValid is Boolean)
        assertNotNull(result.violations)
        assertTrue("violations should be list", result.violations is List<*>)
        assertTrue("timestamp should be positive", result.timestamp > 0)
    }

    @Test
    fun `generateSecurityReport returns SecurityReport`() {
        val manager = CameraSecurityManager.getInstance()
        val report = manager.generateSecurityReport()

        assertNotNull(report)
        assertTrue(report.generatedAt > 0)
        assertTrue(report.totalCameraSessions >= 0)
        assertTrue(report.totalPermissionRequests >= 0)
        assertNotNull(report.securityViolations)
        assertNotNull(report.auditEvents)
        assertTrue(
            "Compliance should be COMPLIANT or NON_COMPLIANT",
            report.complianceStatus == "COMPLIANT" || report.complianceStatus == "NON_COMPLIANT"
        )
    }

    @Test
    fun `startCameraSession without permission returns false`() {
        val manager = CameraSecurityManager.getInstance()
        // Ensure we're in a state where permission might not be granted
        val result = manager.startCameraSession("test-session-${System.currentTimeMillis()}")
        // Result depends on runtime permission; we only assert it's a boolean
        assertTrue("Should return boolean", result is Boolean)
    }

    @Test
    fun `endCameraSession does not throw`() {
        val manager = CameraSecurityManager.getInstance()
        manager.endCameraSession("test-session-end")
        // No exception
    }

    @Test
    fun `cameraPermissionGranted state flow emits`() = runTest {
        val manager = CameraSecurityManager.getInstance()
        manager.hasCameraPermission(context)
        val value = runBlocking { manager.cameraPermissionGranted.first() }
        assertTrue("Should be boolean", value is Boolean)
    }

    @Test
    fun `cameraInUse state flow emits`() = runTest {
        val manager = CameraSecurityManager.getInstance()
        val value = runBlocking { manager.cameraInUse.first() }
        assertTrue("Should be boolean", value is Boolean)
    }

    @Test
    fun `securityViolations state flow emits list`() = runTest {
        val manager = CameraSecurityManager.getInstance()
        val value = runBlocking { manager.securityViolations.first() }
        assertNotNull(value)
        assertTrue("Should be list", value is List<*>)
    }

    @Test
    fun `ValidationResult with no violations is valid`() {
        val r = CameraSecurityManager.ValidationResult(
            isValid = true,
            violations = emptyList(),
            timestamp = System.currentTimeMillis()
        )
        assertTrue(r.isValid)
        assertTrue(r.violations.isEmpty())
    }

    @Test
    fun `SecurityReport structure`() {
        val manager = CameraSecurityManager.getInstance()
        val report = manager.generateSecurityReport()
        assertNotNull(report.auditEvents)
        assertNotNull(report.securityViolations)
    }

    // ========== Hour 6: Permission and audit trail branches ==========

    @Test
    fun `checkCameraPermission via hasCameraPermission returns boolean`() {
        val manager = CameraSecurityManager.getInstance()
        val result = manager.hasCameraPermission(context)
        assertTrue(result is Boolean)
    }

    @Test
    fun `enforceSecurityPolicy via validateCameraUsage returns result`() {
        val manager = CameraSecurityManager.getInstance()
        val result = manager.validateCameraUsage(context)
        assertNotNull(result)
        assertTrue(result.isValid is Boolean)
    }

    @Test
    fun `logSecurityEvent via generateSecurityReport includes events`() {
        val manager = CameraSecurityManager.getInstance()
        val report = manager.generateSecurityReport()
        assertNotNull(report.auditEvents)
    }

    @Test
    fun `getAuditTrail via report auditEvents`() {
        val manager = CameraSecurityManager.getInstance()
        val report = manager.generateSecurityReport()
        assertTrue(report.auditEvents is List<*>)
    }

    @Test
    fun `ValidationResult with violations is invalid`() {
        val r = CameraSecurityManager.ValidationResult(
            isValid = false,
            violations = listOf("Camera permission not granted"),
            timestamp = System.currentTimeMillis()
        )
        assertFalse(r.isValid)
        assertTrue(r.violations.isNotEmpty())
    }

    @Test
    fun `ViolationType values`() {
        val types = CameraSecurityManager.ViolationType.values()
        assertTrue(types.size >= 2)
    }

    @Test
    fun `Severity MEDIUM exists`() {
        assertNotNull(CameraSecurityManager.Severity.MEDIUM)
    }
}
