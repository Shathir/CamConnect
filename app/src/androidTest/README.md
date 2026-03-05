# Instrumented Tests with Real Camera Authentication

## Overview

The instrumented tests in this project can connect to a **real camera** to test actual functionality instead of mocking. This provides more realistic test coverage and validates the entire authentication and API flow.

## Test Configuration

### Camera Credentials

The tests are configured to connect to a camera at:
- **IP Address**: `192.168.2.1`
- **PIN**: `1111`

These values are defined in `helpers/TestAuthHelper.kt`:

```kotlin
const val TEST_CAMERA_IP = "192.168.2.1"
const val TEST_CAMERA_PIN = "1111"
```

### Changing Camera Credentials

To test with a different camera, edit `TestAuthHelper.kt` and update:

```kotlin
const val TEST_CAMERA_IP = "your.camera.ip"
const val TEST_CAMERA_PIN = "your_pin"
```

## How It Works

### Automatic Authentication

Tests that require camera access use `TestAuthHelper.authenticateIfNeeded()` in their `@Before` setup:

```kotlin
@Before
fun setup() {
    TestAuthHelper.authenticateIfNeeded()
}
```

This helper:
1. **Authenticates once** per test session (reuses session token across tests)
2. **Skips tests** if the camera is not available (instead of failing)
3. **Logs authentication status** for debugging

### Test Behavior

**When camera is available:**
- Tests authenticate and run with real API calls
- All API methods receive actual responses from the camera
- Tests verify real functionality

**When camera is NOT available:**
- Tests are automatically **SKIPPED** (not failed)
- Log message: `"Camera not available at 192.168.2.1 (skip test)"`
- This allows tests to pass in CI/CD without a camera

## Running Tests

### With Camera Connected

1. Ensure your camera is powered on and accessible at `192.168.2.1`
2. Verify network connectivity: `ping 192.168.2.1`
3. Run instrumented tests:

```bash
./gradlew connectedDebugAndroidTest
```

### Without Camera (CI/CD)

Tests will automatically skip when camera is not available:

```bash
./gradlew connectedDebugAndroidTest
# Tests requiring camera will be SKIPPED
```

## Test Classes Using Real Authentication

### Communication Tests
- `MotocamAPIAndroidHelperInstrumentedTest` - Tests all camera API methods with real session

### E2E Tests
- `CompleteSetupFlowTest` - Setup flow with authenticated session
- `RecordingWorkflowTest` - Recording workflow with real camera
- `ViewerDiscoveryFlowTest` - Viewer discovery with authentication
- `UserJourneyTest` - Complete user journeys including logout

## Session Management

### Session Reuse
The `TestAuthHelper` reuses the session token across tests for efficiency:
- First test authenticates and gets session token
- Subsequent tests reuse the same token
- Session is valid for 24 hours (as per `SessionManager`)

### Force Re-authentication
To force re-authentication in a specific test:

```kotlin
@Test
fun myTest() {
    TestAuthHelper.forceReauthentication()
    TestAuthHelper.authenticateIfNeeded()
    // Test runs with fresh authentication
}
```

### Clear Authentication
To test logout flows:

```kotlin
@Test
fun logoutTest() {
    TestAuthHelper.clearAuthentication()
    // Verify session is cleared
    assertFalse(SessionManager.isAuthenticated())
}
```

## Debugging

### Enable Verbose Logging

Check logcat for authentication status:

```bash
adb logcat | grep TestAuthHelper
```

Example output:
```
TestAuthHelper: Authenticating with camera at 192.168.2.1...
TestAuthHelper: ✓ Authentication successful! Token length: 32
TestAuthHelper: Session status: Authenticated: true, PIN Attempts: 0/3, Session Valid: true
```

### Common Issues

**Issue**: Tests skip with "Camera not available"
- **Solution**: Verify camera is powered on and accessible at `192.168.2.1`
- Check: `ping 192.168.2.1`

**Issue**: Authentication fails with "Invalid PIN"
- **Solution**: Verify PIN is correct in `TestAuthHelper.kt`
- Default PIN: `1111`

**Issue**: Tests fail with "Session token expired"
- **Solution**: Re-run tests to get fresh session token
- Or: Call `TestAuthHelper.forceReauthentication()` in test

## Benefits of Real Camera Testing

✅ **Real API Validation** - Tests actual camera responses, not mocks  
✅ **Session Token Flow** - Validates authentication and session management  
✅ **Network Layer** - Tests real HTTP/binary protocol communication  
✅ **Error Handling** - Discovers real-world error scenarios  
✅ **Integration Coverage** - End-to-end validation of entire stack  

## CI/CD Integration

The test suite is designed to work in both environments:

**Local Development** (with camera):
- Tests run with real camera
- Full API validation
- Comprehensive coverage

**CI/CD Pipeline** (without camera):
- Tests automatically skip
- No false failures
- Build passes successfully

This approach provides maximum coverage when possible, while maintaining CI/CD reliability.

## Example Test Output

### With Camera Available
```
MotocamAPIAndroidHelperInstrumentedTest
  ✓ getConfigAsync_invokesCallback (2.1s)
  ✓ setZoomAsync_invokesCallback (2.3s)
  ✓ getHealthStatusAsync_invokesCallback (2.2s)
  
9 tests passed
```

### Without Camera Available
```
MotocamAPIAndroidHelperInstrumentedTest
  ⊘ getConfigAsync_invokesCallback (SKIPPED)
  ⊘ setZoomAsync_invokesCallback (SKIPPED)
  ⊘ getHealthStatusAsync_invokesCallback (SKIPPED)
  
9 tests skipped
```

## Security Note

⚠️ **Do not commit real camera credentials to version control**

If testing with production cameras:
1. Use environment variables for credentials
2. Or: Create a local `test.properties` file (add to `.gitignore`)
3. Or: Use test-specific credentials that are safe to commit

Current default credentials (`192.168.2.1` / `1111`) are assumed to be test/development camera credentials.
