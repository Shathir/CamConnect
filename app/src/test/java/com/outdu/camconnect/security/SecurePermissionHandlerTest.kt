package com.outdu.camconnect.security

import android.content.Context
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SecurePermissionHandler: CameraPermissionState and Context.hasSecureCameraPermission().
 * Composable and Activity-dependent logic are covered in instrumented tests.
 */
class SecurePermissionHandlerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = io.mockk.mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== CameraPermissionState data class ==========

    @Test
    fun `CameraPermissionState has correct defaults`() {
        val state = CameraPermissionState()
        assertFalse(state.isGranted)
        assertFalse(state.shouldShowRationale)
        assertFalse(state.isPermanentlyDenied)
        assertFalse(state.hasRequestedBefore)
    }

    @Test
    fun `CameraPermissionState copy updates isGranted`() {
        val state = CameraPermissionState().copy(isGranted = true)
        assertTrue(state.isGranted)
    }

    @Test
    fun `CameraPermissionState copy updates shouldShowRationale`() {
        val state = CameraPermissionState().copy(shouldShowRationale = true)
        assertTrue(state.shouldShowRationale)
    }

    @Test
    fun `CameraPermissionState copy updates isPermanentlyDenied`() {
        val state = CameraPermissionState().copy(isPermanentlyDenied = true)
        assertTrue(state.isPermanentlyDenied)
    }

    @Test
    fun `CameraPermissionState copy updates hasRequestedBefore`() {
        val state = CameraPermissionState().copy(hasRequestedBefore = true)
        assertTrue(state.hasRequestedBefore)
    }

    @Test
    fun `CameraPermissionState with all true`() {
        val state = CameraPermissionState(
            isGranted = true,
            shouldShowRationale = true,
            isPermanentlyDenied = true,
            hasRequestedBefore = true
        )
        assertTrue(state.isGranted)
        assertTrue(state.shouldShowRationale)
        assertTrue(state.isPermanentlyDenied)
        assertTrue(state.hasRequestedBefore)
    }

    // ========== Context.hasSecureCameraPermission() ==========

    @Test
    fun `hasSecureCameraPermission returns true when CameraSecurityManager has permission`() {
        val mockManager = io.mockk.mockk<CameraSecurityManager>(relaxed = true)
        every { mockManager.hasCameraPermission(context) } returns true
        mockkObject(CameraSecurityManager)
        every { CameraSecurityManager.getInstance() } returns mockManager
        assertTrue(context.hasSecureCameraPermission())
    }

    @Test
    fun `hasSecureCameraPermission returns false when CameraSecurityManager has no permission`() {
        val mockManager = io.mockk.mockk<CameraSecurityManager>(relaxed = true)
        every { mockManager.hasCameraPermission(context) } returns false
        mockkObject(CameraSecurityManager)
        every { CameraSecurityManager.getInstance() } returns mockManager
        assertFalse(context.hasSecureCameraPermission())
    }

    @Test
    fun `CameraPermissionState equals by value`() {
        val a = CameraPermissionState(isGranted = true)
        val b = CameraPermissionState(isGranted = true)
        assertTrue(a == b)
    }

    @Test
    fun `CameraPermissionState not equals when different`() {
        val a = CameraPermissionState(isGranted = true)
        val b = CameraPermissionState(isGranted = false)
        assertTrue(a != b)
    }
}
