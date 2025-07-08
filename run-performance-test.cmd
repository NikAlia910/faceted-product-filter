@echo off
setlocal
set USERS=%1
set RAMP=%2
if "%USERS%"=="" set USERS=1
if "%RAMP%"=="" set RAMP=1

REM Run the Gatling test with configurable users and ramp duration
mvn gatling:test -Dusers=%USERS% -DrampDuration=%RAMP%
endlocal 