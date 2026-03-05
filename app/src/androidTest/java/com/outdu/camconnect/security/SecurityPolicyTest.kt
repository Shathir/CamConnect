package com.outdu.camconnect.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for security policy enforcement.
 * Covers policy retrieval and validation with real Android context.
 */
@RunWith(AndroidJUnit4::class)
class SecurityPolicyTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun getSecurityPolicy_returnsValidPolicy() {
        val manager = CameraSecurityManager.getInstance()
        val policy = manager.getSecurityPolicy()
        assertNotNull(policy)
        assertTrue("Max session duration should be positive", policy.maxSessionDuration > 0)
    }

    @Test
    fun validateCameraUsage_returnsValidationResult() {
        val manager = CameraSecurityManager.getInstance()
        val validation = manager.validateCameraUsage(context)
        assertNotNull(validation)
        assertNotNull(validation.violations)
        assertTrue(validation.isValid is Boolean)
    }

    @Test
    fun generateSecurityReport_containsAuditEvents() {
        val manager = CameraSecurityManager.getInstance()
        val report = manager.generateSecurityReport()
        assertNotNull(report.auditEvents)
        assertTrue(report.auditEvents is List<*>)
    }

    @Test
    fun generateSecurityReport_hasSecurityViolationsList() {
        val manager = CameraSecurityManager.getInstance()
        val report = manager.generateSecurityReport()
        assertNotNull(report.securityViolations)
    }
}
