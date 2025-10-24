package com.outdu.camconnect.auth

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test suite for all authentication-related tests
 * 
 * Run this suite to execute all authentication tests:
 * ./gradlew test --tests "com.outdu.camconnect.auth.AuthenticationTestSuite"
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    SimpleSessionManagerTest::class,
    AuthenticationExceptionTest::class
)
class AuthenticationTestSuite {
    // This class remains empty, it is used only as a holder for the above annotations
}

/**
 * Test configuration and utilities for authentication tests
 */
object AuthenticationTestConfig {
    
    // Test data constants
    const val VALID_PIN = "1234"
    const val INVALID_PIN = "9999"
    const val MALFORMED_PIN = "12a4"
    const val EMPTY_PIN = ""
    const val LONG_PIN = "1234567890"
    
    const val VALID_SESSION_TOKEN = "test_session_token_12345"
    const val CUSTOM_CAMERA_IP = "192.168.1.100"
    const val DEFAULT_CAMERA_IP = "192.168.2.1"
    
    // Mock response bodies
    const val SUCCESS_RESPONSE = """{"message": "Login successful"}"""
    const val ERROR_RESPONSE = """{"error": "Invalid PIN provided"}"""
    const val MALFORMED_RESPONSE = """{"invalid": json}"""
    const val EMPTY_RESPONSE = ""
    
    // Test timeouts and delays
    const val TEST_TIMEOUT_MS = 5000L
    const val MOCK_DELAY_MS = 100L
    
    // Lockout test data
    const val LOCKOUT_DURATION_MS = 30000L
    const val MAX_ATTEMPTS = 3
}
