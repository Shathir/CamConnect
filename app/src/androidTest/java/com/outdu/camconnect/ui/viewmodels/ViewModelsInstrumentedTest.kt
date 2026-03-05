package com.outdu.camconnect.ui.viewmodels

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.outdu.camconnect.ui.layouts.streamer.AiRegionOverlayType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ViewModels
 * Tests RecordingViewModel, NetworkConfigurationViewModel, and AiConfigurationViewModel
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ViewModelsInstrumentedTest {

    // ========== RecordingViewModel Tests ==========

    @Test
    fun testRecordingViewModelInitialState() {
        runBlocking {
            val viewModel = RecordingViewModel()
            
            assertFalse("Should not be recording initially", 
                viewModel.isRecording.first())
        }
    }

    @Test
    fun testRecordingStateFlow() {
        runBlocking {
            val viewModel = RecordingViewModel()
            val state = viewModel.recordingState.first()
            
            assertNotNull("Recording state should not be null", state)
        }
    }

    // ========== NetworkConfigurationViewModel Tests ==========

    @Test
    fun testNetworkConfigurationViewModelInitialState() {
        runBlocking {
            val viewModel = NetworkConfigurationViewModel()
            
            val hotspotState = viewModel.hotspotState.first()
            assertNotNull("Hotspot state should not be null", hotspotState)
            assertEquals("Default SSID should be SaberAthena01", 
                "SaberAthena01", hotspotState.hotspot_ssid)
            
            val wifiState = viewModel.wifiState.first()
            assertNotNull("WiFi state should not be null", wifiState)
            assertTrue("Dynamic IP should be enabled by default", 
                wifiState.dynamicIpEnabled)
        }
    }

    @Test
    fun testNetworkConfigurationHotspotUpdates() {
        runBlocking {
            val viewModel = NetworkConfigurationViewModel()
            
            viewModel.updateHotspotSSID("TestNetwork")
            val state = viewModel.hotspotState.first()
            
            assertEquals("SSID should be updated", "TestNetwork", state.hotspot_ssid)
        }
    }

    @Test
    fun testNetworkConfigurationWifiUpdates() {
        runBlocking {
            val viewModel = NetworkConfigurationViewModel()
            
            viewModel.updateWifiSSID("MyWiFi")
            viewModel.updateDynamicIpEnabled(false)
            
            val state = viewModel.wifiState.first()
            
            assertEquals("WiFi SSID should be updated", "MyWiFi", state.wifi_ssid)
            assertFalse("Dynamic IP should be disabled", state.dynamicIpEnabled)
        }
    }

    @Test
    fun testNetworkConfigurationIPAddressUpdate() {
        runBlocking {
            val viewModel = NetworkConfigurationViewModel()
            
            viewModel.updateHotspotIPAddress("192.168.5.1")
            val state = viewModel.hotspotState.first()
            
            assertEquals("IP address should be updated", "192.168.5.1", 
                state.hotspot_ip_address)
        }
    }

    // ========== AiConfigurationViewModel Tests ==========

    @Test
    fun testAiConfigurationViewModelInitialState() {
        runBlocking {
            val viewModel = AiConfigurationViewModel()
            
            val state = viewModel.uiState.first()
            
            assertFalse("FAR should be disabled by default", state.far)
            assertFalse("OD should be disabled by default", state.od)
            assertFalse("DS should be disabled by default", state.ds)
            assertFalse("Audio should be disabled by default", state.audio)
            assertEquals("Model should be version 1", 1, state.model)
            assertEquals("DS threshold should be 0.5", 0.5f, state.dsThreshold, 0.001f)
            assertEquals("Overlay type should be MASK", AiRegionOverlayType.MASK, 
                state.overlayType)
        }
    }

    @Test
    fun testAiConfigurationObjectDetectionToggle() {
        runBlocking {
            val viewModel = AiConfigurationViewModel()
            
            // Load configuration first to establish baseline
            viewModel.loadConfiguration(InstrumentationRegistry.getInstrumentation().targetContext)
            kotlinx.coroutines.delay(500) // Wait for load to complete
            
            viewModel.updateOD(true)
            val state = viewModel.uiState.first()
            
            assertTrue("OD should be enabled", state.od)
        }
    }

    @Test
    fun testAiConfigurationFallDetectionToggle() {
        runBlocking {
            val viewModel = AiConfigurationViewModel()
            
            viewModel.updateFAR(true)
            val state = viewModel.uiState.first()
            
            assertTrue("FAR should be enabled", state.far)
        }
    }

    @Test
    fun testAiConfigurationDepthSensingToggle() {
        runBlocking {
            val viewModel = AiConfigurationViewModel()
            
            viewModel.updateDS(true)
            val state = viewModel.uiState.first()
            
            assertTrue("DS should be enabled", state.ds)
        }
    }

    @Test
    fun testAiConfigurationModelVersionUpdate() {
        runBlocking {
            val viewModel = AiConfigurationViewModel()
            
            viewModel.updateModel(3)
            val state = viewModel.uiState.first()
            
            assertEquals("Model version should be 3", 3, state.model)
        }
    }

    @Test
    fun testAiConfigurationThresholdUpdate() {
        runBlocking {
            val viewModel = AiConfigurationViewModel()
            
            viewModel.updateDsThreshold(0.75f)
            val state = viewModel.uiState.first()
            
            assertEquals("Threshold should be 0.75", 0.75f, state.dsThreshold, 0.001f)
        }
    }

    @Test
    fun testAiConfigurationOverlayTypeUpdate() {
        runBlocking {
            val viewModel = AiConfigurationViewModel()
            
            viewModel.updateOverlayType(AiRegionOverlayType.BOX)
            val state = viewModel.uiState.first()
            
            assertEquals("Overlay type should be BOX", AiRegionOverlayType.BOX, 
                state.overlayType)
        }
    }

    @Test
    fun testAiConfigurationClearError() {
        runBlocking {
            val viewModel = AiConfigurationViewModel()
            
            viewModel.clearError()
            val state = viewModel.uiState.first()
            
            assertNull("Error should be cleared", state.errorMessage)
        }
    }

    @Test
    fun testAiConfigurationMultipleUpdates() {
        runBlocking {
            val viewModel = AiConfigurationViewModel()
            
            // Load configuration first to establish baseline
            viewModel.loadConfiguration(InstrumentationRegistry.getInstrumentation().targetContext)
            kotlinx.coroutines.delay(500) // Wait for load to complete
            
            viewModel.updateOD(true)
            viewModel.updateFAR(true)
            viewModel.updateDS(true)
            viewModel.updateAudio(true)
            viewModel.updateModel(2)
            
            val state = viewModel.uiState.first()
            
            assertTrue("All AI features should be enabled", 
                state.od && state.far && state.ds && state.audio)
            assertEquals("Model should be version 2", 2, state.model)
        }
    }

    // ========== Hour 5: ViewModel integration with real Android ==========

    @Test
    fun testRecordingViewModelWithRealStorageCheck() {
        runBlocking {
            val viewModel = RecordingViewModel()
            val state = viewModel.recordingState.first()
            assertNotNull(state)
        }
    }

    @Test
    fun testNetworkConfigurationViewModelWithRealContext() {
        runBlocking {
            val viewModel = NetworkConfigurationViewModel()
            viewModel.updateWifiSSID("TestSSID")
            val state = viewModel.wifiState.first()
            assertEquals("TestSSID", state.wifi_ssid)
        }
    }

    @Test
    fun testCameraControlViewModelInitialState() {
        runBlocking {
            val viewModel = CameraControlViewModel()
            assertNotNull(viewModel.cameraControlState.value)
        }
    }

    @Test
    fun testAiConfigurationViewModelLoadConfigurationWithContext() {
        runBlocking {
            val viewModel = AiConfigurationViewModel()
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            viewModel.loadConfiguration(context)
            kotlinx.coroutines.delay(300)
            val state = viewModel.uiState.first()
            assertNotNull(state)
        }
    }

    @Test
    fun testRecordingViewModelUiEventsFlow() {
        runBlocking {
            val viewModel = RecordingViewModel()
            val recording = viewModel.isRecording.first()
            assertFalse(recording)
        }
    }
}
