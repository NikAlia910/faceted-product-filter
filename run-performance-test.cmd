@echo off
echo ================================================
echo   Comprehensive API Performance Test Runner
echo ================================================

:: Default values
set USERS=10
set RAMP_DURATION=10
set BASE_URL=http://localhost:8080

:: Parse command line arguments
:parse
if "%~1"=="" goto :run
if "%~1"=="--users" (
    set USERS=%~2
    shift
    shift
    goto :parse
)
if "%~1"=="--ramp" (
    set RAMP_DURATION=%~2
    shift
    shift
    goto :parse
)
if "%~1"=="--url" (
    set BASE_URL=%~2
    shift
    shift
    goto :parse
)
if "%~1"=="--help" goto :help
shift
goto :parse

:run
echo Configuration:
echo   Users: %USERS%
echo   Ramp Duration: %RAMP_DURATION% seconds
echo   Base URL: %BASE_URL%
echo.
echo Starting performance test...
echo.

mvn gatling:test ^
    -Dgatling.simulationClass=gatling.simulations.ComprehensiveApiPerformanceTest ^
    -Dusers=%USERS% ^
    -DrampDuration=%RAMP_DURATION% ^
    -DbaseUrl=%BASE_URL%

echo.
echo Performance test completed!
echo Check target/gatling/ for detailed reports
goto :end

:help
echo Usage: run-performance-test.cmd [OPTIONS]
echo.
echo Options:
echo   --users NUM        Number of concurrent users (default: 10)
echo   --ramp NUM         Ramp up duration in seconds (default: 10)
echo   --url URL          Base URL for the application (default: http://localhost:8080)
echo   --help             Show this help message
echo.
echo Examples:
echo   run-performance-test.cmd
echo   run-performance-test.cmd --users 50 --ramp 30
echo   run-performance-test.cmd --url https://myapp.com --users 25
echo.

:end 