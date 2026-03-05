package com.outdu.camconnect.ui.components.camera

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.models.CameraMode
import com.outdu.camconnect.ui.models.VisionMode
import com.outdu.camconnect.ui.theme.CamConnectTheme
import com.outdu.camconnect.ui.viewmodels.IrIntensityLevel
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CameraDataFetcher - LiveCameraData and rememberLiveCameraData.
 */
@RunWith(AndroidJUnit4::class)
class CameraDataFetcherTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun liveCameraData_holdsDefaultValues() {
        val data = LiveCameraData(
            irIntensityLevel = IrIntensityLevel.OFF,
            visionMode = VisionMode.VISION,
            cameraMode = CameraMode.OFF,
            isLoading = false,
            error = null
        )
        Assert.assertEquals(IrIntensityLevel.OFF, data.irIntensityLevel)
        Assert.assertEquals(VisionMode.VISION, data.visionMode)
        Assert.assertEquals(CameraMode.OFF, data.cameraMode)
        Assert.assertFalse(data.isLoading)
        Assert.assertNull(data.error)
    }

    @Test
    fun rememberLiveCameraData_rendersWhenDisabled() {
        composeTestRule.setContent {
            CamConnectTheme {
                val data = rememberLiveCameraData(pollIntervalMs = 999999L, enabled = false)
                androidx.compose.material3.Text("Loading: ${data.isLoading}")
            }
        }
        Thread.sleep(500) // Allow composition
        composeTestRule.onNodeWithText("Loading: true").assertExists()
    }
}
