@echo off
echo ================================================
echo      Chess Game with Hint System
echo ================================================
echo.

echo Building Chess Game...
call mvn clean compile -q

if errorlevel 1 (
    echo Build failed. Press any key to exit.
    pause
    exit /b 1
)

echo Copying dependencies...
call mvn dependency:copy-dependencies -DoutputDirectory=target/dependency -q

if errorlevel 1 (
    echo Failed to copy dependencies. Press any key to exit.
    pause
    exit /b 1
)

echo Starting Chess Game...
echo.
echo The game window should appear shortly...
echo (You can minimize this window - it will stay open until you close the game)
echo.

java -cp "target\classes;target\dependency\*" com.chess.Main

echo.
echo Chess Game finished. Press any key to exit.
pause
