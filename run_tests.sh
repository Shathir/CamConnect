#!/bin/bash

# CamConnect Test Runner Script
# This script provides easy commands to run different types of tests

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to run unit tests
run_unit_tests() {
    print_status "Running unit tests..."
    ./gradlew test
    if [ $? -eq 0 ]; then
        print_success "Unit tests passed!"
    else
        print_error "Unit tests failed!"
        exit 1
    fi
}

# Function to run instrumented tests
run_instrumented_tests() {
    print_status "Running instrumented tests..."
    ./gradlew connectedAndroidTest
    if [ $? -eq 0 ]; then
        print_success "Instrumented tests passed!"
    else
        print_error "Instrumented tests failed!"
        exit 1
    fi
}

# Function to run all tests
run_all_tests() {
    print_status "Running all tests..."
    ./gradlew check
    if [ $? -eq 0 ]; then
        print_success "All tests passed!"
    else
        print_error "Some tests failed!"
        exit 1
    fi
}

# Function to generate test coverage report
generate_coverage() {
    print_status "Generating test coverage report..."
    ./gradlew jacocoTestReport
    if [ $? -eq 0 ]; then
        print_success "Coverage report generated!"
        print_status "Coverage report available at: app/build/reports/jacoco/test/html/index.html"
    else
        print_error "Failed to generate coverage report!"
        exit 1
    fi
}

# Function to run lint checks
run_lint() {
    print_status "Running lint checks..."
    ./gradlew lint
    if [ $? -eq 0 ]; then
        print_success "Lint checks passed!"
    else
        print_warning "Lint issues found. Check app/build/reports/lint-results.html"
    fi
}

# Function to run specific test class
run_specific_test() {
    if [ -z "$1" ]; then
        print_error "Please provide a test class name"
        echo "Usage: $0 specific <TestClassName>"
        exit 1
    fi
    
    print_status "Running specific test: $1"
    ./gradlew test --tests "$1"
    if [ $? -eq 0 ]; then
        print_success "Test $1 passed!"
    else
        print_error "Test $1 failed!"
        exit 1
    fi
}

# Function to clean and rebuild
clean_rebuild() {
    print_status "Cleaning and rebuilding..."
    ./gradlew clean build
    if [ $? -eq 0 ]; then
        print_success "Clean rebuild completed!"
    else
        print_error "Clean rebuild failed!"
        exit 1
    fi
}

# Function to show help
show_help() {
    echo "CamConnect Test Runner"
    echo ""
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  unit          Run unit tests only"
    echo "  instrumented  Run instrumented tests only"
    echo "  all           Run all tests (unit + instrumented)"
    echo "  coverage      Generate test coverage report"
    echo "  lint          Run lint checks"
    echo "  specific      Run specific test class"
    echo "  clean         Clean and rebuild project"
    echo "  help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 unit"
    echo "  $0 specific com.outdu.camconnect.viewmodels.RecordingViewModelTest"
    echo "  $0 coverage"
}

# Main script logic
case "$1" in
    "unit")
        run_unit_tests
        ;;
    "instrumented")
        run_instrumented_tests
        ;;
    "all")
        run_all_tests
        ;;
    "coverage")
        generate_coverage
        ;;
    "lint")
        run_lint
        ;;
    "specific")
        run_specific_test "$2"
        ;;
    "clean")
        clean_rebuild
        ;;
    "help"|"-h"|"--help")
        show_help
        ;;
    "")
        print_warning "No command specified. Use 'help' to see available commands."
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
