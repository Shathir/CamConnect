# CamConnect Testing Guide

## Overview

This guide provides comprehensive instructions for writing, running, and maintaining tests for the CamConnect Android application. The testing strategy covers unit tests, integration tests, and UI tests to ensure production readiness.

## Testing Architecture

### Test Structure

```
app/src/
├── test/                           # Unit tests (run on JVM)
│   └── java/com/outdu/camconnect/
│       ├── TestBase.kt            # Base class for all unit tests
│       ├── viewmodels/            # ViewModel tests
│       ├── communication/        # Communication layer tests
│       ├── services/              # Service tests
│       ├── auth/                  # Authentication tests
│       └── utils/                # Utility tests
└── androidTest/                   # Instrumented tests (run on device/emulator)
    └── java/com/outdu/camconnect/
        ├── integration/           # Integration tests
        ├── ui/                    # UI tests
        └── network/               # Network tests
```

## Test Types

### 1. Unit Tests

**Location**: `app/src/test/`
**Purpose**: Test individual components in isolation
**Run Command**: `./gradlew test`

#### Key Components to Test:
- **ViewModels**: Business logic and state management
- **Communication Layer**: Network operations and data handling
- **Services**: Background operations
- **Authentication**: Security and session management
- **Utilities**: Helper functions and data processing

#### Example Unit Test Structure:
```kotlin
@ExperimentalCoroutinesApi
class ComponentTest : TestBase() {
    
    @Before
    fun setup() {
        super.setup()
        // Initialize test objects
    }
    
    @Test
    fun `test specific functionality`() = runTest {
        // Given
        // When
        // Then
    }
}
```

### 2. Integration Tests

**Location**: `app/src/androidTest/java/com/outdu/camconnect/integration/`
**Purpose**: Test component interactions and real device communication
**Run Command**: `./gradlew connectedAndroidTest`

#### Key Integration Tests:
- **ONVIF Communication**: Device discovery and authentication
- **Network Operations**: HTTP client and socket communication
- **Database Operations**: Data persistence and retrieval
- **File Operations**: Recording and file management

### 3. UI Tests

**Location**: `app/src/androidTest/java/com/outdu/camconnect/ui/`
**Purpose**: Test user interface interactions and Compose components
**Run Command**: `./gradlew connectedAndroidTest`

#### Key UI Tests:
- **Screen Navigation**: User flow testing
- **Button Interactions**: Recording controls and settings
- **State Updates**: UI state changes and animations
- **Accessibility**: Screen reader and accessibility features

## Running Tests

### Using the Test Script

The `run_tests.sh` script provides easy commands for different testing scenarios:

```bash
# Run unit tests only
./run_tests.sh unit

# Run instrumented tests only
./run_tests.sh instrumented

# Run all tests
./run_tests.sh all

# Generate coverage report
./run_tests.sh coverage

# Run specific test class
./run_tests.sh specific com.outdu.camconnect.viewmodels.RecordingViewModelTest

# Run lint checks
./run_tests.sh lint

# Clean and rebuild
./run_tests.sh clean
```

### Using Gradle Directly

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# All tests
./gradlew check

# Coverage report
./gradlew jacocoTestReport

# Lint checks
./gradlew lint

# Specific test class
./gradlew test --tests "com.outdu.camconnect.viewmodels.RecordingViewModelTest"

# Specific test method
./gradlew test --tests "com.outdu.camconnect.viewmodels.RecordingViewModelTest.testRecordingState"
```

## Test Coverage

### Coverage Goals
- **Minimum**: 70% overall coverage
- **Target**: 80% overall coverage
- **Critical Components**: 90% coverage (ViewModels, Communication layer)

### Generating Coverage Reports

```bash
./gradlew jacocoTestReport
```

Coverage reports are generated in:
- HTML: `app/build/reports/jacoco/test/html/index.html`
- XML: `app/build/reports/jacoco/test/testReport.xml`

### Coverage Analysis

The coverage report shows:
- **Line Coverage**: Percentage of lines executed
- **Branch Coverage**: Percentage of branches taken
- **Method Coverage**: Percentage of methods called
- **Class Coverage**: Percentage of classes used

## Writing Tests

### Test Naming Convention

Use descriptive test names that explain the scenario:

```kotlin
@Test
fun `recording button clicked should start recording`() = runTest {
    // Test implementation
}

@Test
fun `recording state should handle error scenarios gracefully`() = runTest {
    // Test implementation
}
```

### Test Structure (Given-When-Then)

```kotlin
@Test
fun `test specific functionality`() = runTest {
    // Given - Setup test data and conditions
    val input = "test input"
    val expectedOutput = "expected output"
    
    // When - Execute the functionality being tested
    val actualOutput = componentUnderTest.process(input)
    
    // Then - Verify the results
    assertEquals(expectedOutput, actualOutput)
}
```

### Mocking Guidelines

Use MockK for Kotlin mocking:

```kotlin
// Create mock
val mockDependency = mockk<Dependency>()

// Configure mock behavior
every { mockDependency.method() } returns "expected result"

// Verify interactions
verify { mockDependency.method() }
```

### Testing Coroutines

Use `runTest` for coroutine testing:

```kotlin
@Test
fun `test coroutine functionality`() = runTest {
    // Test coroutine-based code
    val result = viewModel.asyncOperation()
    assertEquals(expectedResult, result)
}
```

### Testing StateFlow

Use Turbine for StateFlow testing:

```kotlin
@Test
fun `test state flow emissions`() = runTest {
    viewModel.stateFlow.test {
        // Initial state
        assertEquals(InitialState, awaitItem())
        
        // State after action
        viewModel.performAction()
        assertEquals(UpdatedState, awaitItem())
    }
}
```

## Test Data Management

### Test Fixtures

Create test data fixtures for consistent testing:

```kotlin
object TestFixtures {
    val sampleCamera = CameraData(
        id = "test-camera-1",
        name = "Test Camera",
        ipAddress = "192.168.2.1",
        isOnline = true
    )
    
    val sampleRecordingState = RecordingState.Recording("01:30")
}
```

### Test Database

Use in-memory database for testing:

```kotlin
@Before
fun setupDatabase() {
    database = Room.inMemoryDatabaseBuilder(
        context,
        AppDatabase::class.java
    ).build()
}
```

## Continuous Integration

### GitHub Actions Workflow

Create `.github/workflows/ci.yml`:

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
    
    - name: Run unit tests
      run: ./gradlew test
    
    - name: Run lint
      run: ./gradlew lint
    
    - name: Generate test report
      run: ./gradlew jacocoTestReport
    
    - name: Upload test results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: test-results
        path: app/build/reports/
```

## Debugging Tests

### Common Issues and Solutions

1. **Test Timeouts**
   - Increase timeout in test configuration
   - Check for infinite loops or blocking operations

2. **Mock Issues**
   - Verify mock configuration
   - Check for strict mocking requirements

3. **Coroutine Issues**
   - Use `runTest` for coroutine testing
   - Check dispatcher configuration

4. **StateFlow Issues**
   - Use Turbine for StateFlow testing
   - Verify state emissions

### Debug Commands

```bash
# Run tests with debug output
./gradlew test --info

# Run specific test with debug
./gradlew test --tests "TestClass" --debug

# Run tests in parallel
./gradlew test --parallel
```

## Best Practices

### 1. Test Organization
- Group related tests in the same class
- Use descriptive test names
- Keep tests focused and simple

### 2. Test Data
- Use consistent test fixtures
- Avoid hardcoded values
- Create reusable test utilities

### 3. Test Maintenance
- Update tests when code changes
- Remove obsolete tests
- Keep tests up to date with requirements

### 4. Performance
- Use `runTest` for coroutine testing
- Mock external dependencies
- Avoid slow operations in tests

### 5. Coverage
- Aim for high coverage on critical paths
- Don't sacrifice quality for coverage numbers
- Focus on meaningful test scenarios

## Production Readiness Checklist

### Testing Requirements
- [ ] Unit test coverage > 70%
- [ ] Integration tests for critical flows
- [ ] UI tests for main user journeys
- [ ] Performance tests for critical operations
- [ ] Security tests for authentication
- [ ] Error handling tests
- [ ] Edge case testing

### Quality Gates
- [ ] All tests pass
- [ ] No lint errors
- [ ] Coverage targets met
- [ ] Performance benchmarks met
- [ ] Security scan passed

### Documentation
- [ ] Test documentation updated
- [ ] Coverage reports generated
- [ ] Test results archived
- [ ] Performance metrics recorded

## Troubleshooting

### Common Test Failures

1. **Build Failures**
   - Check dependencies in `build.gradle.kts`
   - Verify test configuration
   - Check for missing imports

2. **Test Failures**
   - Review test logic
   - Check mock configurations
   - Verify test data

3. **Coverage Issues**
   - Check Jacoco configuration
   - Verify test execution
   - Review coverage exclusions

### Getting Help

- Check test logs for detailed error messages
- Review test documentation
- Consult team members for complex issues
- Use debugging tools for investigation

## Conclusion

This testing guide provides a comprehensive framework for testing the CamConnect application. By following these guidelines, you can ensure that your application is robust, maintainable, and ready for production deployment.

Remember to:
- Write tests early and often
- Maintain high test coverage
- Keep tests simple and focused
- Update tests when code changes
- Use appropriate testing tools and frameworks
