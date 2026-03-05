package com.outdu.camconnect.auth

import android.content.Context
import com.outdu.camconnect.auth.SetupFlowDetector.AuthenticationMethod
import com.outdu.camconnect.auth.UserStateManager.UserFlowType
import com.outdu.camconnect.auth.UserStateManager.UserType
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SetupFlowDetector and SetupFlowConfig.
 * Mocks UserStateManager and SessionManager to test flow logic.
 * Tests use no coroutines; detector methods are synchronous.
 */
class SetupFlowDetectorTest {

    private lateinit var context: Context
    private lateinit var detector: SetupFlowDetector
    private lateinit var userStateFlow: MutableStateFlow<UserStateManager.UserState>

    @Before
    fun setup() {
        context = io.mockk.mockk(relaxed = true)
        userStateFlow = MutableStateFlow(UserStateManager.UserState(userType = UserType.UNKNOWN))
        mockkObject(UserStateManager)
        every { UserStateManager.initialize(any()) } returns Unit
        every { UserStateManager.getUserFlowType() } returns UserFlowType.FIRST_TIME
        every { UserStateManager.userState } returns userStateFlow
        every { UserStateManager.getUserStatus() } returns "test"
        every { UserStateManager.markAppLaunched() } returns Unit
        every { UserStateManager.hasRegisteredCameras() } returns false
        mockkObject(SessionManager)
        every { SessionManager.initialize(any()) } returns Unit
        every { SessionManager.isAuthenticated() } returns false
        every { SessionManager.getSessionStatus() } returns "test"
        detector = SetupFlowDetector(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getStartingDestination FIRST_TIME returns landing`() {
        every { UserStateManager.getUserFlowType() } returns UserFlowType.FIRST_TIME
        every { SessionManager.isAuthenticated() } returns false
        assertEquals("landing", detector.getStartingDestination())
    }

    @Test
    fun `getStartingDestination RETURNING_WITH_CAMERAS authenticated returns main_activity`() {
        every { UserStateManager.getUserFlowType() } returns UserFlowType.RETURNING_WITH_CAMERAS
        every { SessionManager.isAuthenticated() } returns true
        assertEquals("main_activity", detector.getStartingDestination())
    }

    @Test
    fun `getStartingDestination RETURNING_WITH_CAMERAS not authenticated returns viewer_camera_list`() {
        every { UserStateManager.getUserFlowType() } returns UserFlowType.RETURNING_WITH_CAMERAS
        every { SessionManager.isAuthenticated() } returns false
        assertEquals("viewer_camera_list", detector.getStartingDestination())
    }

    @Test
    fun `getStartingDestination RETURNING_NO_CAMERAS authenticated returns camera_add`() {
        every { UserStateManager.getUserFlowType() } returns UserFlowType.RETURNING_NO_CAMERAS
        every { SessionManager.isAuthenticated() } returns true
        assertEquals("camera_add", detector.getStartingDestination())
    }

    @Test
    fun `getStartingDestination RETURNING_NO_CAMERAS not authenticated returns login`() {
        every { UserStateManager.getUserFlowType() } returns UserFlowType.RETURNING_NO_CAMERAS
        every { SessionManager.isAuthenticated() } returns false
        assertEquals("login", detector.getStartingDestination())
    }

    @Test
    fun `getStartingDestination VIEWER returns viewer_camera_list`() {
        every { UserStateManager.getUserFlowType() } returns UserFlowType.VIEWER
        assertEquals("viewer_camera_list", detector.getStartingDestination())
    }

    @Test
    fun `getStartingDestination INCOMPLETE_SETUP returns login`() {
        every { UserStateManager.getUserFlowType() } returns UserFlowType.INCOMPLETE_SETUP
        assertEquals("login", detector.getStartingDestination())
    }

    @Test
    fun `shouldSkipSetup returns true when RETURNING_WITH_CAMERAS authenticated and has cameras`() {
        every { UserStateManager.getUserFlowType() } returns UserFlowType.RETURNING_WITH_CAMERAS
        every { SessionManager.isAuthenticated() } returns true
        every { UserStateManager.hasRegisteredCameras() } returns true
        assertTrue(detector.shouldSkipSetup())
    }

    @Test
    fun `shouldSkipSetup returns false when not authenticated`() {
        every { UserStateManager.getUserFlowType() } returns UserFlowType.RETURNING_WITH_CAMERAS
        every { SessionManager.isAuthenticated() } returns false
        every { UserStateManager.hasRegisteredCameras() } returns true
        assertFalse(detector.shouldSkipSetup())
    }

    @Test
    fun `shouldRunAutoDiscovery FIRST_TIME returns false`() {
        every { UserStateManager.getUserFlowType() } returns UserFlowType.FIRST_TIME
        assertFalse(detector.shouldRunAutoDiscovery())
    }

    @Test
    fun `shouldRunAutoDiscovery RETURNING_WITH_CAMERAS returns true`() {
        every { UserStateManager.getUserFlowType() } returns UserFlowType.RETURNING_WITH_CAMERAS
        assertTrue(detector.shouldRunAutoDiscovery())
    }

    @Test
    fun `getAuthenticationMethod OWNER returns PIN_WITH_SETUP`() {
        userStateFlow.value = UserStateManager.UserState(userType = UserType.OWNER)
        assertEquals(AuthenticationMethod.PIN_WITH_SETUP, detector.getAuthenticationMethod())
    }

    @Test
    fun `getAuthenticationMethod VIEWER returns PIN_ONLY`() {
        userStateFlow.value = UserStateManager.UserState(userType = UserType.VIEWER)
        assertEquals(AuthenticationMethod.PIN_ONLY, detector.getAuthenticationMethod())
    }

    @Test
    fun `getSetupFlowConfig FIRST_TIME has showLanding true`() {
        every { UserStateManager.getUserFlowType() } returns UserFlowType.FIRST_TIME
        val config = detector.getSetupFlowConfig()
        assertTrue(config.showLanding)
        assertTrue(config.showLicenseFetch)
        assertTrue(config.requireAuthentication)
        assertEquals(UserType.UNKNOWN, config.userType)
    }

    @Test
    fun `SetupFlowConfig data class holds values`() {
        val config = SetupFlowConfig(
            showLanding = true,
            showLicenseFetch = false,
            showCameraDiscovery = true,
            showQRScanner = true,
            requireAuthentication = true,
            userType = UserType.VIEWER
        )
        assertTrue(config.showLanding)
        assertFalse(config.showLicenseFetch)
        assertEquals(UserType.VIEWER, config.userType)
    }
}
