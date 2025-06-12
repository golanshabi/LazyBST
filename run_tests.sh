#!/bin/bash

# BST Test Runner Script
# This script compiles and runs all BST tests automatically

set -e  # Exit on any error (except where explicitly handled)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test results tracking
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Create log file with timestamp
LOG_FILE="test_results_$(date +%Y%m%d_%H%M%S).log"

echo -e "${BLUE}BST Test Suite Runner${NC} - Detailed log: ${LOG_FILE}"
echo "========================================"

# Log header
{
    echo "========================================="
    echo "BST Test Suite - $(date)"
    echo "========================================="
    echo
} > "$LOG_FILE"

# Function to run a test and track results
run_test() {
    local test_name="$1"
    local test_class="$2"
    local description="$3"
    
    echo -n "Running ${test_name}... "
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Log detailed info to file
    {
        echo "----------------------------------------"
        echo "Running: ${test_name}"
        echo "Description: ${description}"
        echo "Class: ${test_class}"
        echo "Time: $(date)"
        echo "----------------------------------------"
    } >> "$LOG_FILE"
    
    # Run test and capture output
    if java -ea -cp src/algorithms "$test_class" >> "$LOG_FILE" 2>&1; then
        echo -e "${GREEN}PASSED${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo "✅ PASSED: ${test_name}" >> "$LOG_FILE"
    else
        echo -e "${RED}FAILED${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo "❌ FAILED: ${test_name}" >> "$LOG_FILE"
    fi
    
    echo "" >> "$LOG_FILE"
}

# Compilation step
echo -n "Compiling... "
{
    echo "Step 1: Compiling Java files..."
    echo "Command: javac src/algorithms/*.java"
    echo "Time: $(date)"
} >> "$LOG_FILE"

if javac src/algorithms/*.java >> "$LOG_FILE" 2>&1; then
    echo -e "${GREEN}DONE${NC}"
    echo "✅ Compilation successful" >> "$LOG_FILE"
else
    echo -e "${RED}FAILED${NC}"
    echo "❌ Compilation failed" >> "$LOG_FILE"
    echo -e "${RED}Compilation failed. Check ${LOG_FILE} for details.${NC}"
    exit 1
fi
echo "" >> "$LOG_FILE"

# Handle command line arguments for individual test runs
if [ $# -eq 1 ]; then
    case "$1" in
        "basic")
            echo "Running basic tests only..."
            echo "Step 2: Running basic tests only..." >> "$LOG_FILE"
            run_test "Basic BST Tests" "BSTTest" "Core functionality: add, duplicates, multiple insertions, edge cases"
            ;;
        "timeout")
            echo "Running timeout tests only..."
            echo "Step 2: Running timeout tests only..." >> "$LOG_FILE"
            run_test "Timeout-Protected Tests" "BSTTestWithTimeout" "Detects infinite loops and performance issues"
            ;;
        "advanced")
            echo "Running advanced tests only..."
            echo "Step 2: Running advanced tests only..." >> "$LOG_FILE"
            run_test "Advanced Concurrent Tests" "BSTJUnitTest" "Concurrent operations, large datasets, performance benchmarks"
            ;;
        "multiprocess")
            echo "Running multi-process tests only..."
            echo "Step 2: Running multi-process tests only..." >> "$LOG_FILE"
            run_test "Multi-Process Tests" "BSTMultiProcessTest" "Multiple JVM processes with BST property verification"
            ;;
        *)
            echo -e "${RED}Unknown test option: $1${NC}"
            echo "Valid options: basic, timeout, advanced, multiprocess"
            exit 1
            ;;
    esac
else
    # Run all test suites
    echo "Running all test suites..."
    echo "Step 2: Running all test suites..." >> "$LOG_FILE"

    # Basic functionality tests
    run_test "Basic BST Tests" "BSTTest" "Core functionality: add, duplicates, multiple insertions, edge cases"

    # Timeout-protected tests
    run_test "Timeout-Protected Tests" "BSTTestWithTimeout" "Detects infinite loops and performance issues"

    # Advanced concurrent tests
    run_test "Advanced Concurrent Tests" "BSTJUnitTest" "Concurrent operations, large datasets, performance benchmarks"

    # Multi-process tests
    run_test "Multi-Process Tests" "BSTMultiProcessTest" "Multiple JVM processes with BST property verification"
fi

# Summary
echo "========================================"
echo -e "RESULTS: ${GREEN}${PASSED_TESTS} passed${NC}, ${RED}${FAILED_TESTS} failed${NC} (${TOTAL_TESTS} total)"

# Log detailed summary
{
    echo "========================================="
    echo "           TEST SUMMARY"
    echo "========================================="
    echo "Total Tests: ${TOTAL_TESTS}"
    echo "Passed: ${PASSED_TESTS}"
    echo "Failed: ${FAILED_TESTS}"
    echo "Completion Time: $(date)"
    echo "========================================="
} >> "$LOG_FILE"

if [ $FAILED_TESTS -gt 0 ]; then
    echo -e "${RED}❌ Some tests failed. Check ${LOG_FILE} for details.${NC}"
    exit 1
else
    echo -e "${GREEN}🎉 All tests passed!${NC}"
fi

echo "Detailed log saved to: ${LOG_FILE}"
echo
echo "Quick commands:"
echo "  ./run_tests.sh basic|timeout|advanced|multiprocess"
echo "  java -ea -cp src/algorithms [TestClass]" 