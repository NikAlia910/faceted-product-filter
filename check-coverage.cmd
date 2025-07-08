@echo off
echo ================================================
echo         API Coverage Quick Check
echo ================================================
echo.
echo Current Performance Test Coverage:
echo.
echo ✅ TESTED ENDPOINTS (18/32):
echo    - Categories: 6/6 (100%%)
echo    - Products: 7/7 (100%%)  
echo    - Authentication: 2/2 (100%%)
echo    - Administration: 3/10 (30%%)
echo.
echo ❌ MISSING ENDPOINTS (14/32):
echo    - Account Management: 0/6 (0%%)
echo    - User Administration: 1/5 (20%%)
echo    - Authorities Management: 1/4 (25%%)
echo.
echo ================================================
echo    TOTAL COVERAGE: 56.25%% (18/32 endpoints)
echo ================================================
echo.
echo For detailed analysis, see: api-coverage-analysis.md
echo.
echo To run performance test:
echo   run-performance-test.cmd
echo.
pause 