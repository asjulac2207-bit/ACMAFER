@REM Maven Wrapper for Windows
@echo off
where mvn >nul 2>&1
if %ERRORLEVEL% equ 0 (
    mvn %*
    exit /b %ERRORLEVEL%
) else (
    echo Error: Maven not found. Please install from https://maven.apache.org
    exit /b 1
)
