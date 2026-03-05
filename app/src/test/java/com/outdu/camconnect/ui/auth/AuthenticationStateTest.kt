package com.outdu.camconnect.ui.auth

import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for AuthenticationState - PinAuthState data class.
 */
class AuthenticationStateTest {

    @Test
    fun pinAuthState_defaultValues() {
        val state = PinAuthState()
        Assert.assertEquals("", state.pin)
        Assert.assertFalse(state.isLoading)
        Assert.assertNull(state.errorMessage)
        Assert.assertFalse(state.isSuccess)
        Assert.assertEquals(3, state.attemptsRemaining)
        Assert.assertTrue(state.canAttempt)
    }

    @Test
    fun pinAuthState_copy() {
        val state = PinAuthState(pin = "1234", isLoading = true)
        val copied = state.copy(isLoading = false)
        Assert.assertEquals("1234", copied.pin)
        Assert.assertFalse(copied.isLoading)
    }
}
