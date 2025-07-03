package com.outdu.camconnect.auth

/**
 * Base exception class for all authentication-related errors
 */
sealed class AuthenticationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when PIN authentication fails
 */
class InvalidPinException(
    message: String = "Invalid PIN provided",
    cause: Throwable? = null
) : AuthenticationException(message, cause)

/**
 * Thrown when session token is invalid or expired
 */
class InvalidSessionException(
    message: String = "Session is invalid or expired",
    cause: Throwable? = null
) : AuthenticationException(message, cause)

/**
 * Thrown when authentication network request fails
 */
class AuthenticationNetworkException(
    message: String = "Network error during authentication",
    cause: Throwable? = null
) : AuthenticationException(message, cause)

/**
 * Thrown when authentication server is unavailable
 */
class AuthenticationServerException(
    message: String = "Authentication server unavailable",
    cause: Throwable? = null
) : AuthenticationException(message, cause)

/**
 * Thrown when maximum PIN attempts are exceeded
 */
class MaxAttemptsExceededException(
    message: String = "Maximum PIN attempts exceeded",
    cause: Throwable? = null
) : AuthenticationException(message, cause) 