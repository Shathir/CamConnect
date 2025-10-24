# Authentication Testing Guide

This guide demonstrates comprehensive testing patterns for the login functionality in CamConnect, covering all scenarios including success, error, edge cases, security, and integration testing.

## Test Structure

### 1. SessionManagerTest.kt
**Main test class for SessionManager authentication functionality**

#### Test Categories:

**✅ Success Scenarios:**
- Valid PIN authentication
- Custom camera IP authentication  
- Session token storage
- Attempt counter reset on success

**❌ Error Scenarios:**
- Invalid PIN format validation
- Wrong PIN authentication
- Network errors (timeout, connection failure)
- Server errors (500, 502, etc.)
- Empty responses
- Malformed JSON responses
- Missing session tokens

**🔒 Security Scenarios:**
- Attempt counter increment
- Lockout mechanism after max attempts
- Lockout period enforcement
- Session token validation

**🔍 Edge Cases:**
- Null/empty PINs
- Very long PINs
- Special character PINs
- Unexpected server messages
- Malformed cookies
- Concurrent authentication attempts

**🔗 Integration Scenarios:**
- HTTP timeout handling
- Real HTTP client interaction
- Session persistence
- Cross-component communication

### 2. AuthenticationExceptionTest.kt
**Tests for all authentication exception types**

- Exception hierarchy validation
- Default and custom messages
- Exception chaining (cause)
- toString() behavior
- Stack trace availability

### 3. AuthenticationTestSuite.kt
**Test suite configuration and utilities**

- Centralized test data constants
- Mock response configurations
- Test timeout settings
- Suite execution configuration

## Testing Patterns Demonstrated

### 1. **Mocking Dependencies**
```kotlin
@Mock
private lateinit var mockContext: Context

@Mock
private lateinit var mockSharedPreferences: SharedPreferences

@Before
fun setup() {
    MockitoAnnotations.openMocks(this)
    // Setup mock behavior
}
```

### 2. **Coroutine Testing**
```kotlin
@Test
fun `test async authentication`() = runTest {
    // Test coroutine-based authentication
    val result = SessionManager.authenticateWithPin("1234")
    assertTrue(result.isSuccess)
}
```

### 3. **HTTP Client Mocking**
```kotlin
private fun setupMockHttpClient(
    responseBody: String,
    statusCode: HttpStatusCode = HttpStatusCode.OK,
    headers: Headers = Headers.Empty
) {
    mockEngine = MockEngine { request ->
        respond(
            content = ByteReadChannel(responseBody.toByteArray()),
            status = statusCode,
            headers = headers
        )
    }
}
```

### 4. **Exception Testing**
```kotlin
@Test
fun `test specific exception type`() = runTest {
    val result = SessionManager.authenticateWithPin("invalid")
    
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is InvalidPinException)
    assertEquals("Expected error message", result.exceptionOrNull()?.message)
}
```

### 5. **State Verification**
```kotlin
// Verify SharedPreferences interactions
verify(mockEditor).putString("session_token", sessionToken)
verify(mockEditor).putLong("last_auth_time", any())
verify(mockEditor).apply()
```

## Running the Tests

### Run All Authentication Tests
```bash
./gradlew test --tests "com.outdu.camconnect.auth.*"
```

### Run Specific Test Class
```bash
./gradlew test --tests "com.outdu.camconnect.auth.SessionManagerTest"
```

### Run Test Suite
```bash
./gradlew test --tests "com.outdu.camconnect.auth.AuthenticationTestSuite"
```

### Run with Coverage
```bash
./gradlew testDebugUnitTestCoverage
```

## Test Coverage Areas

### **Functional Coverage:**
- ✅ Valid authentication flow
- ✅ Invalid authentication handling
- ✅ Session management
- ✅ Error handling and recovery

### **Exception Coverage:**
- ✅ All exception types tested
- ✅ Exception chaining
- ✅ Error message validation
- ✅ Exception hierarchy

### **Security Coverage:**
- ✅ PIN validation
- ✅ Attempt limiting
- ✅ Lockout mechanism
- ✅ Session token handling

### **Integration Coverage:**
- ✅ HTTP client interaction
- ✅ SharedPreferences integration
- ✅ Coroutine handling
- ✅ Network error scenarios

### **Edge Case Coverage:**
- ✅ Boundary conditions
- ✅ Malformed input
- ✅ Concurrent operations
- ✅ Resource cleanup

## Key Testing Principles Applied

1. **Arrange-Act-Assert Pattern**: Clear test structure
2. **Single Responsibility**: Each test focuses on one scenario
3. **Descriptive Test Names**: Clear intent with backticks
4. **Comprehensive Mocking**: Isolate units under test
5. **Exception Testing**: Verify all error paths
6. **State Verification**: Check side effects
7. **Integration Testing**: Test component interactions
8. **Coverage Testing**: Ensure all code paths are tested

## Extending the Pattern

To apply this testing pattern to other functionality:

1. **Identify the component** (e.g., MotocamSocketClient)
2. **List all scenarios** (success, error, edge cases)
3. **Mock dependencies** (HTTP client, SharedPreferences)
4. **Write test cases** for each scenario
5. **Verify behavior** (return values, side effects)
6. **Test exceptions** (all error paths)
7. **Add integration tests** (real dependencies)

This pattern ensures robust, maintainable, and reliable code with comprehensive test coverage.
