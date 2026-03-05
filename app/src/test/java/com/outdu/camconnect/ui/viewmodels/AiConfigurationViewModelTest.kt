package com.outdu.camconnect.ui.viewmodels

import app.cash.turbine.test
import com.outdu.camconnect.testutils.MainDispatcherRule
import com.outdu.camconnect.ui.layouts.streamer.AiRegionOverlayType
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for AiConfigurationViewModel.
 * Tests state updates, hasUnsavedChanges logic, and clearError.
 * loadConfiguration, saveConfiguration, resetToDefaults require Context and are
 * covered in ViewModelsInstrumentedTest.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AiConfigurationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AiConfigurationViewModel

    @Before
    fun setup() {
        viewModel = AiConfigurationViewModel()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========== Initial State Tests ==========

    @Test
    fun `initial uiState has default values`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.far)
            assertFalse(state.od)
            assertFalse(state.ds)
            assertFalse(state.audio)
            assertEquals(1, state.model)
            assertEquals(0.5f, state.dsThreshold, 0.001f)
            assertEquals(AiRegionOverlayType.MASK, state.overlayType)
            assertFalse(state.isLoading)
            assertFalse(state.hasUnsavedChanges)
            assertNull(state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== Update OD (Object Detection) Tests ==========

    @Test
    fun `updateOD true sets od`() = runTest {
        viewModel.updateOD(true)
        viewModel.uiState.test {
            assertTrue(awaitItem().od)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateOD false sets od`() = runTest {
        viewModel.updateOD(false)
        viewModel.uiState.test {
            assertFalse(awaitItem().od)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== Update FAR Tests ==========

    @Test
    fun `updateFAR true sets far`() = runTest {
        viewModel.updateFAR(true)
        viewModel.uiState.test {
            assertTrue(awaitItem().far)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateFAR false sets far`() = runTest {
        viewModel.updateFAR(false)
        viewModel.uiState.test {
            assertFalse(awaitItem().far)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== Update DS (Depth Sensing) Tests ==========

    @Test
    fun `updateDS true sets ds`() = runTest {
        viewModel.updateDS(true)
        viewModel.uiState.test {
            assertTrue(awaitItem().ds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateDS false sets ds`() = runTest {
        viewModel.updateDS(false)
        viewModel.uiState.test {
            assertFalse(awaitItem().ds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== Update Audio Tests ==========

    @Test
    fun `updateAudio true sets audio`() = runTest {
        viewModel.updateAudio(true)
        viewModel.uiState.test {
            assertTrue(awaitItem().audio)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateAudio false sets audio`() = runTest {
        viewModel.updateAudio(false)
        viewModel.uiState.test {
            assertFalse(awaitItem().audio)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== Update Model Tests ==========

    @Test
    fun `updateModel sets model version`() = runTest {
        viewModel.updateModel(2)
        viewModel.uiState.test {
            assertEquals(2, awaitItem().model)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateModel sets model and emits state`() = runTest {
        viewModel.updateModel(1)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.model)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== Update DS Threshold Tests ==========

    @Test
    fun `updateDsThreshold sets threshold`() = runTest {
        viewModel.updateDsThreshold(0.75f)
        viewModel.uiState.test {
            assertEquals(0.75f, awaitItem().dsThreshold, 0.001f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== Update Overlay Type Tests ==========

    @Test
    fun `updateOverlayType sets overlay type to BOX`() = runTest {
        viewModel.updateOverlayType(AiRegionOverlayType.BOX)
        viewModel.uiState.test {
            assertEquals(AiRegionOverlayType.BOX, awaitItem().overlayType)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateOverlayType sets overlay type to NONE`() = runTest {
        viewModel.updateOverlayType(AiRegionOverlayType.NONE)
        viewModel.uiState.test {
            assertEquals(AiRegionOverlayType.NONE, awaitItem().overlayType)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateOverlayType sets overlay type to MASK`() = runTest {
        viewModel.updateOverlayType(AiRegionOverlayType.BOX)
        viewModel.updateOverlayType(AiRegionOverlayType.MASK)
        viewModel.uiState.test {
            assertEquals(AiRegionOverlayType.MASK, awaitItem().overlayType)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== clearError Test ==========

    @Test
    fun `clearError clears error message`() = runTest {
        viewModel.clearError()
        viewModel.uiState.test {
            assertNull(awaitItem().errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== loadConfiguration with mocked CameraConfigurationManager ==========

    @Test
    fun `loadConfiguration on success updates state from config`() = runTest {
        val config = com.outdu.camconnect.communication.CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = true,
            objectDetectionEnabled = true,
            depthSensingEnabled = true,
            audioEnabled = true,
            modelVersion = 2,
            depthSensingThreshold = 0.6f
        )
        val context = io.mockk.mockk<android.content.Context>(relaxed = true)
        val prefs = io.mockk.mockk<android.content.SharedPreferences>(relaxed = true)
        every { context.getSharedPreferences(any(), any()) } returns prefs
        every { prefs.getString(any(), any()) } returns AiRegionOverlayType.MASK.name
        every { prefs.edit() } returns io.mockk.mockk<android.content.SharedPreferences.Editor>(relaxed = true).apply {
            every { putString(any(), any()) } returns this
            every { apply() } just Runs
        }

        mockkObject(com.outdu.camconnect.communication.CameraConfigurationManager)
        coEvery {
            com.outdu.camconnect.communication.CameraConfigurationManager.loadConfigurationAsync(context)
        } returns kotlin.Result.success(config)

        viewModel.loadConfiguration(context)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.far)
            assertTrue(state.od)
            assertTrue(state.ds)
            assertTrue(state.audio)
            assertEquals(2, state.model)
            assertEquals(0.6f, state.dsThreshold, 0.001f)
            assertFalse(state.isLoading)
            assertFalse(state.hasUnsavedChanges)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadConfiguration on failure sets error message`() = runTest {
        val context = io.mockk.mockk<android.content.Context>(relaxed = true)
        mockkObject(com.outdu.camconnect.communication.CameraConfigurationManager)
        coEvery {
            com.outdu.camconnect.communication.CameraConfigurationManager.loadConfigurationAsync(context)
        } returns kotlin.Result.failure(RuntimeException("Load failed"))

        viewModel.loadConfiguration(context)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.errorMessage!!.contains("Load failed"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== saveConfiguration with mock ==========

    @Test
    fun `saveConfiguration on success clears hasUnsavedChanges and calls onSuccess`() = runTest {
        val context = io.mockk.mockk<android.content.Context>(relaxed = true)
        val prefs = io.mockk.mockk<android.content.SharedPreferences>(relaxed = true)
        every { context.getSharedPreferences(any(), any()) } returns prefs
        val editor = io.mockk.mockk<android.content.SharedPreferences.Editor>(relaxed = true)
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } just Runs

        mockkObject(com.outdu.camconnect.communication.CameraConfigurationManager)
        coEvery {
            com.outdu.camconnect.communication.CameraConfigurationManager.updateConfiguration(context, any())
        } returns kotlin.Result.success(Unit)

        viewModel.updateOD(true)
        var successCalled = false
        viewModel.saveConfiguration(context, onSuccess = { successCalled = true })
        advanceUntilIdle()

        assertTrue(successCalled)
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.hasUnsavedChanges)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveConfiguration on failure sets error message`() = runTest {
        val context = io.mockk.mockk<android.content.Context>(relaxed = true)
        mockkObject(com.outdu.camconnect.communication.CameraConfigurationManager)
        coEvery {
            com.outdu.camconnect.communication.CameraConfigurationManager.updateConfiguration(context, any())
        } returns kotlin.Result.failure(RuntimeException("Save failed"))

        viewModel.updateOD(true)
        viewModel.saveConfiguration(context)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.errorMessage!!.contains("Save failed"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== hasUnsavedChanges logic ==========

    @Test
    fun `hasUnsavedChanges true when state differs from loaded original`() = runTest {
        val config = com.outdu.camconnect.communication.CameraConfigurationManager.CameraConfig(
            farDetectionEnabled = false,
            objectDetectionEnabled = true,
            depthSensingEnabled = false,
            audioEnabled = false,
            modelVersion = 1,
            depthSensingThreshold = 0.5f
        )
        val context = io.mockk.mockk<android.content.Context>(relaxed = true)
        val prefs = io.mockk.mockk<android.content.SharedPreferences>(relaxed = true)
        every { context.getSharedPreferences(any(), any()) } returns prefs
        every { prefs.getString(any(), any()) } returns AiRegionOverlayType.MASK.name
        every { prefs.edit() } returns io.mockk.mockk<android.content.SharedPreferences.Editor>(relaxed = true).apply {
            every { putString(any(), any()) } returns this
            every { apply() } just Runs
        }
        mockkObject(com.outdu.camconnect.communication.CameraConfigurationManager)
        coEvery {
            com.outdu.camconnect.communication.CameraConfigurationManager.loadConfigurationAsync(context)
        } returns kotlin.Result.success(config)
        viewModel.loadConfiguration(context)
        advanceUntilIdle()
        viewModel.updateOD(false)
        viewModel.uiState.test {
            assertTrue(awaitItem().hasUnsavedChanges)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
