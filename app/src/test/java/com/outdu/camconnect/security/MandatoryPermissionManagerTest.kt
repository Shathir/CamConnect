package com.outdu.camconnect.security

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MandatoryPermissionManager.
 */
class MandatoryPermissionManagerTest {

    private lateinit var context: Context
    private lateinit var manager: MandatoryPermissionManager

    @Before
    fun setup() {
        context = io.mockk.mockk(relaxed = true)
        every { context.packageName } returns "com.outdu.camconnect"
        mockkStatic(ContextCompat::class)
        manager = MandatoryPermissionManager.getInstance()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `MANDATORY_PERMISSIONS contains CAMERA and location`() {
        assertTrue(MandatoryPermissionManager.MANDATORY_PERMISSIONS.contains(Manifest.permission.CAMERA))
        assertTrue(MandatoryPermissionManager.MANDATORY_PERMISSIONS.contains(Manifest.permission.ACCESS_FINE_LOCATION))
        assertTrue(MandatoryPermissionManager.MANDATORY_PERMISSIONS.contains(Manifest.permission.ACCESS_COARSE_LOCATION))
        assertEquals(3, MandatoryPermissionManager.MANDATORY_PERMISSIONS.size)
    }

    @Test
    fun `getInstance returns same instance`() {
        val a = MandatoryPermissionManager.getInstance()
        val b = MandatoryPermissionManager.getInstance()
        assertTrue(a === b)
    }

    @Test
    fun `hasAllMandatoryPermissions returns true when all granted`() {
        every {
            ContextCompat.checkSelfPermission(context, any())
        } returns PackageManager.PERMISSION_GRANTED
        assertTrue(manager.hasAllMandatoryPermissions(context))
    }

    @Test
    fun `hasAllMandatoryPermissions returns false when one denied`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_GRANTED
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        assertFalse(manager.hasAllMandatoryPermissions(context))
    }

    @Test
    fun `hasCameraPermission returns true when granted`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_GRANTED
        assertTrue(manager.hasCameraPermission(context))
    }

    @Test
    fun `hasCameraPermission returns false when denied`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_DENIED
        assertFalse(manager.hasCameraPermission(context))
    }

    @Test
    fun `hasLocationPermission returns true when fine location granted`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED
        assertTrue(manager.hasLocationPermission(context))
    }

    @Test
    fun `hasLocationPermission returns true when coarse location granted`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        assertTrue(manager.hasLocationPermission(context))
    }

    @Test
    fun `hasLocationPermission returns false when both denied`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED
        assertFalse(manager.hasLocationPermission(context))
    }

    @Test
    fun `getMissingMandatoryPermissions returns empty when all granted`() {
        every {
            ContextCompat.checkSelfPermission(context, any())
        } returns PackageManager.PERMISSION_GRANTED
        assertTrue(manager.getMissingMandatoryPermissions(context).isEmpty())
    }

    @Test
    fun `getMissingMandatoryPermissions returns missing permissions`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_DENIED
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        val missing = manager.getMissingMandatoryPermissions(context)
        assertEquals(1, missing.size)
        assertEquals(Manifest.permission.CAMERA, missing[0])
    }

    @Test
    fun `getMissingMandatoryPermissions returns all when all denied`() {
        every {
            ContextCompat.checkSelfPermission(context, any())
        } returns PackageManager.PERMISSION_DENIED
        val missing = manager.getMissingMandatoryPermissions(context)
        assertEquals(3, missing.size)
    }

    @Test
    fun `openAppSettings does not throw`() {
        manager.openAppSettings(context)
    }
}
