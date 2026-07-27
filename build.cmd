@echo off
REM build.cmd - match Unix ./build
REM   build.cmd              - package with tests
REM   build.cmd skip-tests   - package without tests
REM   build.cmd release      - clean package -Prelease

if "%~1"=="release" (
    call mvnw.cmd clean package -Prelease
) else if "%~1"=="skip-tests" (
    call mvnw.cmd package -DskipTests
) else (
    call mvnw.cmd package
)
