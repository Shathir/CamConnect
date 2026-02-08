package com.outdu.camconnect.testutils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit Test Rule that sets up a TestDispatcher for Kotlin Coroutines.
 * 
 * This rule replaces the Main dispatcher with a TestDispatcher, which is essential
 * for testing ViewModels and other components that use `viewModelScope` or 
 * `Dispatchers.Main`.
 *
 * Usage:
 * ```kotlin
 * @OptIn(ExperimentalCoroutinesApi::class)
 * class MyViewModelTest {
 *     @get:Rule
 *     val mainDispatcherRule = MainDispatcherRule()
 *
 *     @Test
 *     fun `test something`() = runTest {
 *         // Your test code
 *     }
 * }
 * ```
 *
 * By default, uses UnconfinedTestDispatcher which executes tasks immediately.
 * You can pass a custom TestDispatcher if you need more control over execution.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    
    /**
     * Called before each test starts.
     * Sets the Main dispatcher to our test dispatcher.
     */
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    /**
     * Called after each test finishes.
     * Resets the Main dispatcher to its original state.
     */
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
