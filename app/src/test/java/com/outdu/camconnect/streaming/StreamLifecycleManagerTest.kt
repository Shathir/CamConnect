package com.outdu.camconnect.streaming

import com.outdu.camconnect.Viewmodels.AppViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for StreamLifecycleManager
 * Tests bind/unbind and lifecycle callbacks with main looper
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class StreamLifecycleManagerTest {

    @Test
    fun `bind does not throw`() {
        val viewModel = AppViewModel()
        StreamLifecycleManager.bind(viewModel)
        // No exception
    }

    @Test
    fun `unbind with same viewModel does not throw`() {
        val viewModel = AppViewModel()
        StreamLifecycleManager.bind(viewModel)
        StreamLifecycleManager.unbind(viewModel)
        // No exception
    }

    @Test
    fun `unbind with different viewModel instance does not throw`() {
        val vm1 = AppViewModel()
        val vm2 = AppViewModel()
        StreamLifecycleManager.bind(vm1)
        StreamLifecycleManager.unbind(vm2)
        // Binding still holds vm1
        StreamLifecycleManager.unbind(vm1)
    }

    @Test
    fun `multiple bind calls with same viewModel do not throw`() {
        val viewModel = AppViewModel()
        StreamLifecycleManager.bind(viewModel)
        StreamLifecycleManager.bind(viewModel)
        StreamLifecycleManager.unbind(viewModel)
    }

    @Test
    fun `onAppBackgrounded does not throw`() {
        val viewModel = AppViewModel()
        StreamLifecycleManager.bind(viewModel)
        StreamLifecycleManager.onAppBackgrounded()
        // No exception
    }

    @Test
    fun `onAppForegrounded does not throw`() {
        val viewModel = AppViewModel()
        StreamLifecycleManager.bind(viewModel)
        StreamLifecycleManager.onAppForegrounded()
        // No exception
    }

    @Test
    fun `onAppForegrounded after onAppBackgrounded does not throw`() {
        val viewModel = AppViewModel()
        StreamLifecycleManager.bind(viewModel)
        StreamLifecycleManager.onAppBackgrounded()
        StreamLifecycleManager.onAppForegrounded()
        // No exception
    }

    @Test
    fun `onAppBackgrounded without bind does not throw`() {
        StreamLifecycleManager.onAppBackgrounded()
        StreamLifecycleManager.onAppBackgrounded()
    }

    @Test
    fun `full lifecycle bind background foreground unbind`() {
        val viewModel = AppViewModel()
        assertTrue(viewModel.isPlaying.value)
        StreamLifecycleManager.bind(viewModel)
        StreamLifecycleManager.onAppBackgrounded()
        StreamLifecycleManager.onAppForegrounded()
        StreamLifecycleManager.unbind(viewModel)
        // No exception
    }
}
