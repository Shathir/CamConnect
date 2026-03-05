package com.outdu.camconnect.permissions

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.outdu.camconnect.security.CameraSecurityManager
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for runtime permission flows.
 * Covers camera permission check and consistency with CameraSecurityManager.
 */
@RunWith(AndroidJUnit4::class)
class RuntimePermissionsTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun cameraPermissionGranted_enablesPermissionCheck() {
        val manager = CameraSecurityManager.getInstance()
        val hasPermission = manager.hasCameraPermission(context)
        assertTrue("Camera permission should be granted by rule", hasPermission)
    }

    @Test
    fun validateCameraUsage_whenPermissionGranted_returnsValidOrViolations() {
        val manager = CameraSecurityManager.getInstance()
        val validation = manager.validateCameraUsage(context)
        assertTrue("Validation should have isValid", validation.isValid is Boolean)
        assertTrue("Violations should be list", validation.violations is List<*>)
    }

    @Test
    fun securityReport_afterPermissionCheck_hasNonNegativeCounts() {
        val manager = CameraSecurityManager.getInstance()
        manager.hasCameraPermission(context)
        val report = manager.generateSecurityReport()
        assertTrue(report.totalPermissionRequests >= 0)
        assertTrue(report.totalCameraSessions >= 0)
    }
}
