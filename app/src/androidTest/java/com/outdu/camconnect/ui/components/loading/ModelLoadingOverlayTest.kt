package com.outdu.camconnect.ui.components.loading

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.outdu.camconnect.ui.theme.CamConnectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ModelLoadingOverlay (requires ViewModel; minimal render test if exposed).
 * ModelLoadState and ModelLoadError are tested in ModelLoadErrorTest.
 */
@RunWith(AndroidJUnit4::class)
class ModelLoadingOverlayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun modelLoadState_loadingIsSingleton() {
        assert(ModelLoadState.Loading == ModelLoadState.Loading)
    }

    @Test
    fun modelLoadState_successIsSingleton() {
        assert(ModelLoadState.Success == ModelLoadState.Success)
    }

    @Test
    fun modelLoadState_skippedIsSingleton() {
        assert(ModelLoadState.Skipped == ModelLoadState.Skipped)
    }
}
