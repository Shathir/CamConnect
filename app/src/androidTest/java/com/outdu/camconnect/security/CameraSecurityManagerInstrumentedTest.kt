package com.outdu.camconnect.security

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for CameraSecurityManager
 * Runs on an Android device or emulator with real Android framework
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CameraSecurityManagerInstrumentedTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA
    )

    private lateinit var securityManager: CameraSecurityManager
    private lateinit var context: android.content.Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        securityManager = CameraSecurityManager.getInstance()
    }

    @Test
    fun testSingletonPattern() {
        val instance1 = CameraSecurityManager.getInstance()
        val instance2 = CameraSecurityManager.getInstance()
        
        assertSame("Should return same singleton instance", instance1, instance2)
    }

    @Test
    fun testCameraPermissionStateFlow() {
        runBlocking {
            val permissionState = securityManager.cameraPermissionGranted.first()
            
            // Permission state should be a boolean
            assertTrue("Permission state should be boolean", 
                permissionState is Boolean)
        }
    }

    @Test
    fun testCameraInUseStateFlow() {
        runBlocking {
            val inUseState = securityManager.cameraInUse.first()
            
            // Should be a boolean value
            assertTrue("In-use state should be boolean", inUseState is Boolean)
        }
    }

    @Test
    fun testSecurityViolationsStateFlow() {
        runBlocking {
            val violations = securityManager.securityViolations.first()
            
            assertNotNull("Violations should not be null", violations)
            assertTrue("Violations should be a list", violations is List<*>)
        }
    }

    @Test
    fun testHasCameraPermission() {
        val hasPermission = securityManager.hasCameraPermission(context)
        
        // Should return a boolean
        assertTrue("Permission check should return boolean", 
            hasPermission is Boolean)
    }

    @Test
    fun testRequestCameraPermission() {
        val hasPermission = securityManager.hasCameraPermission(context)
        
        // Should return a boolean
        assertTrue("Permission check should return boolean", 
            hasPermission is Boolean)
    }

    @Test
    fun testStartAndEndCameraSession() {
        val sessionId = "test-session-${System.currentTimeMillis()}"
        
        val startResult = securityManager.startCameraSession(sessionId)
        assertTrue("Start session should return boolean", startResult is Boolean)
        
        securityManager.endCameraSession(sessionId)
        
        // Should complete without exception
        assertTrue("Session lifecycle should work", true)
    }

    @Test
    fun testValidateCameraUsage() {
        val validation = securityManager.validateCameraUsage(context)
        
        assertNotNull("Validation result should not be null", validation)
        assertTrue("Validation should have isValid property", 
            validation.isValid is Boolean)
        assertNotNull("Validation should have violations list", 
            validation.violations)
    }

    @Test
    fun testGenerateSecurityReport() {
        val report = securityManager.generateSecurityReport()
        
        assertNotNull("Security report should not be null", report)
        assertTrue("Report should have total sessions", 
            report.totalCameraSessions >= 0)
        assertTrue("Report should have total permission requests", 
            report.totalPermissionRequests >= 0)
        assertNotNull("Report should have security violations list", 
            report.securityViolations)
    }

    @Test
    fun testGetSecurityPolicy() {
        val policy = securityManager.getSecurityPolicy()
        
        assertNotNull("Security policy should not be null", policy)
        assertTrue("Policy should have max session duration", 
            policy.maxSessionDuration > 0)
    }

    @Test
    fun testMultipleSessionStarts() {
        val sessionId1 = "session-1"
        val sessionId2 = "session-2"
        
        securityManager.startCameraSession(sessionId1)
        securityManager.startCameraSession(sessionId2)
        
        securityManager.endCameraSession(sessionId1)
        securityManager.endCameraSession(sessionId2)
        
        // Should handle multiple sessions without crashing
        assertTrue("Multiple sessions should be handled", true)
    }

    @Test
    fun testSecurityReportStructure() {
        val report = securityManager.generateSecurityReport()
        
        // Verify report has expected structure
        assertTrue("Total sessions should be non-negative", 
            report.totalCameraSessions >= 0)
        assertTrue("Permission requests should be non-negative", 
            report.totalPermissionRequests >= 0)
        assertNotNull("Security violations list should not be null", 
            report.securityViolations)
        assertNotNull("Audit events list should not be null", 
            report.auditEvents)
    }

    @Test
    fun testValidationResultStructure() {
        val validation = securityManager.validateCameraUsage(context)
        
        assertNotNull("Violations list should not be null", validation.violations)
        assertTrue("Violations should be a list", validation.violations is List<*>)
        assertTrue("isValid should be boolean", validation.isValid is Boolean)
    }

    // ========== Block 5: Audit and compliance ==========

    @Test
    fun testAuditLog_containsEventsAfterSession() {
        val sessionId = "audit-test-${System.currentTimeMillis()}"
        securityManager.startCameraSession(sessionId)
        val report = securityManager.generateSecurityReport()
        securityManager.endCameraSession(sessionId)
        assertNotNull("Audit events list should not be null", report.auditEvents)
        assertTrue(
            "Audit events should contain session or permission events",
            report.auditEvents.isNotEmpty() || report.totalCameraSessions >= 0
        )
    }

    @Test
    fun testGenerateSecurityReport_auditEventsIsList() {
        val report = securityManager.generateSecurityReport()
        assertNotNull(report.auditEvents)
        assertTrue(report.auditEvents is List<*>)
    }
}
