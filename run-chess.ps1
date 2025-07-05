# Chess Game Launcher Script (PowerShell)

Write-Host "Building Chess Game..." -ForegroundColor Green
mvn compile -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed. Exiting." -ForegroundColor Red
    exit 1
}

Write-Host "Copying dependencies..." -ForegroundColor Green
mvn dependency:copy-dependencies -q

Write-Host "Starting Chess Game..." -ForegroundColor Green

# Set up classpath
$classpath = "target\classes;target\dependency\*"

# Run the game
java -cp $classpath com.chess.Main

Write-Host "Chess Game finished." -ForegroundColor Green
