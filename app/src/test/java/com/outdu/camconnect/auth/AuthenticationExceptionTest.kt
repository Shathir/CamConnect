package com.outdu.camconnect.auth

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for AuthenticationException hierarchy
 * 
 * Tests all exception types and their properties
 */
class AuthenticationExceptionTest {

    @Test
    fun `test InvalidPinException default message`() {
        val exception = InvalidPinException()
        assertEquals("Invalid PIN provided", exception.message)
        assertTrue(exception is AuthenticationException)
    }

    @Test
    fun `test InvalidPinException custom message`() {
        val customMessage = "Custom PIN validation failed"
        val exception = InvalidPinException(customMessage)
        assertEquals(customMessage, exception.message)
        assertTrue(exception is AuthenticationException)
    }

    @Test
    fun `test InvalidPinException with cause`() {
        val cause = RuntimeException("Root cause")
        val exception = InvalidPinException("PIN error", cause)
        assertEquals("PIN error", exception.message)
        assertEquals(cause, exception.cause)
        assertTrue(exception is AuthenticationException)
    }

    @Test
    fun `test InvalidSessionException default message`() {
        val exception = InvalidSessionException()
        assertEquals("Session is invalid or expired", exception.message)
        assertTrue(exception is AuthenticationException)
    }

    @Test
    fun `test InvalidSessionException custom message`() {
        val customMessage = "Session token expired"
        val exception = InvalidSessionException(customMessage)
        assertEquals(customMessage, exception.message)
        assertTrue(exception is AuthenticationException)
    }

    @Test
    fun `test AuthenticationNetworkException default message`() {
        val exception = AuthenticationNetworkException()
        assertEquals("Network error during authentication", exception.message)
        assertTrue(exception is AuthenticationException)
    }

    @Test
    fun `test AuthenticationNetworkException custom message`() {
        val customMessage = "Connection timeout"
        val exception = AuthenticationNetworkException(customMessage)
        assertEquals(customMessage, exception.message)
        assertTrue(exception is AuthenticationException)
    }

    @Test
    fun `test AuthenticationNetworkException with cause`() {
        val cause = java.net.ConnectException("Connection refused")
        val exception = AuthenticationNetworkException("Network error", cause)
        assertEquals("Network error", exception.message)
        assertEquals(cause, exception.cause)
        assertTrue(exception is AuthenticationException)
    }

    @Test
    fun `test AuthenticationServerException default message`() {
        val exception = AuthenticationServerException()
        assertEquals("Authentication server unavailable", exception.message)
        assertTrue(exception is AuthenticationException)
    }

    @Test
    fun `test AuthenticationServerException custom message`() {
        val customMessage = "Server returned 500 error"
        val exception = AuthenticationServerException(customMessage)
        assertEquals(customMessage, exception.message)
        assertTrue(exception is AuthenticationException)
    }

    @Test
    fun `test AuthenticationServerException with cause`() {
        val cause = RuntimeException("Server error")
        val exception = AuthenticationServerException("Server unavailable", cause)
        assertEquals("Server unavailable", exception.message)
        assertEquals(cause, exception.cause)
        assertTrue(exception is AuthenticationException)
    }

    @Test
    fun `test MaxAttemptsExceededException default message`() {
        val exception = MaxAttemptsExceededException()
        assertEquals("Maximum PIN attempts exceeded", exception.message)
        assertTrue(exception is AuthenticationException)
    }

    @Test
    fun `test MaxAttemptsExceededException custom message`() {
        val customMessage = "Account locked for 30 minutes"
        val exception = MaxAttemptsExceededException(customMessage)
        assertEquals(customMessage, exception.message)
        assertTrue(exception is AuthenticationException)
    }

    @Test
    fun `test MaxAttemptsExceededException with cause`() {
        val cause = RuntimeException("Lockout triggered")
        val exception = MaxAttemptsExceededException("Max attempts", cause)
        assertEquals("Max attempts", exception.message)
        assertEquals(cause, exception.cause)
        assertTrue(exception is AuthenticationException)
    }

    @Test
    fun `test exception hierarchy inheritance`() {
        val exceptions = listOf(
            InvalidPinException(),
            InvalidSessionException(),
            AuthenticationNetworkException(),
            AuthenticationServerException(),
            MaxAttemptsExceededException()
        )

        exceptions.forEach { exception ->
            assertTrue("All exceptions should extend AuthenticationException", 
                exception is AuthenticationException)
            assertTrue("All exceptions should extend Exception", 
                exception is Exception)
        }
    }

    @Test
    fun `test exception toString behavior`() {
        val exception = InvalidPinException("Test PIN error")
        val toString = exception.toString()
        
        assertTrue("toString should contain exception class name", 
            toString.contains("InvalidPinException"))
        assertTrue("toString should contain message", 
            toString.contains("Test PIN error"))
    }

    @Test
    fun `test exception stack trace`() {
        val exception = AuthenticationNetworkException("Network test")
        
        // Verify stack trace is available
        val stackTrace = exception.stackTrace
        assertNotNull("Stack trace should not be null", stackTrace)
        assertTrue("Stack trace should have elements", stackTrace.isNotEmpty())
    }
}
