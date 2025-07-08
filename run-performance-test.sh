#!/bin/bash

echo "================================================"
echo "  Comprehensive API Performance Test Runner"
echo "================================================"

# Default values
USERS=10
RAMP_DURATION=10
BASE_URL="http://localhost:8080"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --users)
            USERS="$2"
            shift 2
            ;;
        --ramp)
            RAMP_DURATION="$2"
            shift 2
            ;;
        --url)
            BASE_URL="$2"
            shift 2
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --users NUM        Number of concurrent users (default: 10)"
            echo "  --ramp NUM         Ramp up duration in seconds (default: 10)"
            echo "  --url URL          Base URL for the application (default: http://localhost:8080)"
            echo "  --help             Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0"
            echo "  $0 --users 50 --ramp 30"
            echo "  $0 --url https://myapp.com --users 25"
            echo ""
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

echo "Configuration:"
echo "  Users: $USERS"
echo "  Ramp Duration: $RAMP_DURATION seconds"
echo "  Base URL: $BASE_URL"
echo ""
echo "Starting performance test..."
echo ""

mvn gatling:test \
    -Dgatling.simulationClass=gatling.simulations.ComprehensiveApiPerformanceTest \
    -Dusers=$USERS \
    -DrampDuration=$RAMP_DURATION \
    -DbaseUrl=$BASE_URL

echo ""
echo "Performance test completed!"
echo "Check target/gatling/ for detailed reports" 