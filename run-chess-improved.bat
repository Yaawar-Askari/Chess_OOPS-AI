@echo off
rem Improved Chess Game Launcher Script for Windows
rem This script ensures all dependencies are properly loaded

cd /d "%~dp0"

echo ================================================
echo      Chess Game with Hint System
echo ================================================
echo.

echo [1/3] Building Chess Game...
call mvn clean compile -q

if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    echo Please check your Maven installation and try again.
    pause
    exit /b 1
)

echo [2/3] Copying dependencies...
call mvn dependency:copy-dependencies -DoutputDirectory=target/dependency -q

if %errorlevel% neq 0 (
    echo ERROR: Failed to copy dependencies!
    pause
    exit /b 1
)

echo [3/3] Starting Chess Game...
echo.
echo The game window should appear shortly...
echo (If you see any "WARNING" messages, you can ignore them)
echo.

rem Use the simple classpath approach with wildcard
java -cp "target\classes;target\dependency\*" com.chess.Main

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Failed to start the game (Error code: %errorlevel%)
    echo.
    echo Possible causes:
    echo - Java is not installed or not in PATH
    echo - Missing dependencies
    echo - Antivirus blocking the application
    echo.
    echo Please check your Java installation:
    java -version
    echo.
    pause
    exit /b %errorlevel%
)

echo.
echo Chess Game finished successfully.
pause
