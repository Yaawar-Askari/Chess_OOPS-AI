# Chess Game Launcher Script for Windows PowerShell
# This script provides better error handling and cross-platform compatibility

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "      Chess Game with Hint System" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Change to script directory
Set-Location $PSScriptRoot

try {
    Write-Host "[1/3] Building Chess Game..." -ForegroundColor Yellow
    $buildResult = & mvn clean compile -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Build failed!" -ForegroundColor Red
        Write-Host "Please check your Maven installation and try again." -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }

    Write-Host "[2/3] Copying dependencies..." -ForegroundColor Yellow
    $depResult = & mvn dependency:copy-dependencies -DoutputDirectory=target/dependency -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Failed to copy dependencies!" -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }

    Write-Host "[3/3] Starting Chess Game..." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "The game window should appear shortly..." -ForegroundColor Green
    Write-Host "(If you see any WARNING messages, you can ignore them)" -ForegroundColor Gray
    Write-Host ""

    # Start the Java application
    $classpath = "target\classes;target\dependency\*"
    & java -cp $classpath com.chess.Main

    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "ERROR: Failed to start the game (Error code: $LASTEXITCODE)" -ForegroundColor Red
        Write-Host ""
        Write-Host "Possible causes:" -ForegroundColor Yellow
        Write-Host "- Java is not installed or not in PATH" -ForegroundColor Yellow
        Write-Host "- Missing dependencies" -ForegroundColor Yellow
        Write-Host "- Antivirus blocking the application" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Checking Java installation:" -ForegroundColor Yellow
        & java -version
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit $LASTEXITCODE
    }

    Write-Host ""
    Write-Host "Chess Game finished successfully." -ForegroundColor Green
}
catch {
    Write-Host "An unexpected error occurred: $($_.Exception.Message)" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Read-Host "Press Enter to exit"
