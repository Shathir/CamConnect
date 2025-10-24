package com.outdu.camconnect.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.viewmodels.RecordingViewModel
import com.outdu.camconnect.viewmodels.RecordingState
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for CamConnect Compose screens
 * 
 * Tests cover:
 * - Recording screen interactions
 * - Button states and transitions
 * - UI state updates
 * - User interactions
 */
@RunWith(AndroidJUnit4::class)
class RecordingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun recordingButton_clicked_startsRecording() {
        // Given
        val mockViewModel = createMockRecordingViewModel()
        composeTestRule.setContent {
            RecordingScreen(viewModel = mockViewModel)
        }

        // When
        composeTestRule.onNodeWithText("Start Recording").performClick()

        // Then
        composeTestRule.onNodeWithText("Stop Recording").assertIsDisplayed()
        composeTestRule.onNodeWithText("00:00").assertIsDisplayed()
    }

    @Test
    fun recordingButton_whenRecording_stopsRecording() {
        // Given
        val mockViewModel = createMockRecordingViewModel()
        composeTestRule.setContent {
            RecordingScreen(viewModel = mockViewModel)
        }
        composeTestRule.onNodeWithText("Start Recording").performClick()

        // When
        composeTestRule.onNodeWithText("Stop Recording").performClick()

        // Then
        composeTestRule.onNodeWithText("Start Recording").assertIsDisplayed()
        composeTestRule.onNodeWithText("Saved to Gallery").assertIsDisplayed()
    }

    @Test
    fun recordingTimer_displaysCorrectFormat() {
        // Given
        val mockViewModel = createMockRecordingViewModel()
        composeTestRule.setContent {
            RecordingScreen(viewModel = mockViewModel)
        }

        // When
        composeTestRule.onNodeWithText("Start Recording").performClick()

        // Then
        composeTestRule.onNodeWithText("00:00").assertIsDisplayed()
        // Timer should update to show elapsed time
    }

    @Test
    fun recordingState_error_displaysErrorMessage() {
        // Given
        val mockViewModel = createMockRecordingViewModelWithError()
        composeTestRule.setContent {
            RecordingScreen(viewModel = mockViewModel)
        }

        // When
        composeTestRule.onNodeWithText("Start Recording").performClick()

        // Then
        composeTestRule.onNodeWithText("Recording failed").assertIsDisplayed()
    }

    @Test
    fun recordingControls_areAccessible() {
        // Given
        val mockViewModel = createMockRecordingViewModel()
        composeTestRule.setContent {
            RecordingScreen(viewModel = mockViewModel)
        }

        // When & Then
        composeTestRule.onNodeWithText("Start Recording")
            .assertIsEnabled()
            .assertIsDisplayed()
    }

    @Test
    fun recordingScreen_displaysCorrectInitialState() {
        // Given
        val mockViewModel = createMockRecordingViewModel()
        composeTestRule.setContent {
            RecordingScreen(viewModel = mockViewModel)
        }

        // Then
        composeTestRule.onNodeWithText("Start Recording").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stop Recording").assertDoesNotExist()
    }

    @Test
    fun recordingScreen_handlesRapidClicks() {
        // Given
        val mockViewModel = createMockRecordingViewModel()
        composeTestRule.setContent {
            RecordingScreen(viewModel = mockViewModel)
        }

        // When
        repeat(5) {
            composeTestRule.onNodeWithText("Start Recording").performClick()
        }

        // Then
        // Should handle rapid clicks gracefully
        composeTestRule.onNodeWithText("Stop Recording").assertIsDisplayed()
    }

    @Test
    fun recordingScreen_displaysBatteryIndicator() {
        // Given
        val mockViewModel = createMockRecordingViewModel()
        composeTestRule.setContent {
            RecordingScreen(viewModel = mockViewModel)
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Battery indicator").assertIsDisplayed()
    }

    @Test
    fun recordingScreen_displaysSignalStrength() {
        // Given
        val mockViewModel = createMockRecordingViewModel()
        composeTestRule.setContent {
            RecordingScreen(viewModel = mockViewModel)
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Signal strength").assertIsDisplayed()
    }

    @Test
    fun recordingScreen_displaysAiIndicator() {
        // Given
        val mockViewModel = createMockRecordingViewModel()
        composeTestRule.setContent {
            RecordingScreen(viewModel = mockViewModel)
        }

        // Then
        composeTestRule.onNodeWithContentDescription("AI processing indicator").assertIsDisplayed()
    }

    private fun createMockRecordingViewModel(): RecordingViewModel {
        val mockViewModel = mockk<RecordingViewModel>(relaxed = true)
        
        every { mockViewModel.isRecording } returns MutableStateFlow(false)
        every { mockViewModel.recordingState } returns MutableStateFlow(RecordingState.NotRecording)
        every { mockViewModel.recordingDuration } returns MutableStateFlow("00:00")
        
        return mockViewModel
    }

    private fun createMockRecordingViewModelWithError(): RecordingViewModel {
        val mockViewModel = mockk<RecordingViewModel>(relaxed = true)
        
        every { mockViewModel.isRecording } returns MutableStateFlow(false)
        every { mockViewModel.recordingState } returns MutableStateFlow(RecordingState.Error("Recording failed"))
        every { mockViewModel.recordingDuration } returns MutableStateFlow("00:00")
        
        return mockViewModel
    }
}

/**
 * Mock RecordingScreen composable for testing
 * This would be replaced with your actual RecordingScreen implementation
 */
@Composable
fun RecordingScreen(viewModel: RecordingViewModel) {
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingState by viewModel.recordingState.collectAsState()
    val recordingDuration by viewModel.recordingDuration.collectAsState()

    Column {
        when (recordingState) {
            is RecordingState.NotRecording -> {
                Button(onClick = { viewModel.toggleRecording(mockk()) }) {
                    Text("Start Recording")
                }
            }
            is RecordingState.Recording -> {
                Button(onClick = { viewModel.toggleRecording(mockk()) }) {
                    Text("Stop Recording")
                }
                Text(recordingDuration)
            }
            is RecordingState.SavedToGallery -> {
                Text("Saved to Gallery")
                Button(onClick = { viewModel.toggleRecording(mockk()) }) {
                    Text("Start Recording")
                }
            }
            is RecordingState.Error -> {
                Text(recordingState.message)
            }
        }
        
        // Status indicators
        Icon(
            imageVector = Icons.Default.Battery,
            contentDescription = "Battery indicator"
        )
        Icon(
            imageVector = Icons.Default.SignalWifi,
            contentDescription = "Signal strength"
        )
        Icon(
            imageVector = Icons.Default.Android,
            contentDescription = "AI processing indicator"
        )
    }
}
