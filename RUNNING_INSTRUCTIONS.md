# Chess Game - Windows Command Prompt Instructions

## Quick Start (Copy and paste these commands into Windows Command Prompt):

```cmd
cd "c:\Users\yaawa\Chess_OOP\Chess_OOP"
mvn clean compile -q
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency -q
java -cp "target\classes;target\dependency\*" com.chess.Main
```

## Alternative: Use PowerShell

If Command Prompt doesn't work, try PowerShell:

1. Open PowerShell (Windows + X, then select "Windows PowerShell")
2. Run:
```powershell
cd "c:\Users\yaawa\Chess_OOP\Chess_OOP"
.\run-chess-improved.ps1
```

## Alternative: Use the existing simple batch file

```cmd
cd "c:\Users\yaawa\Chess_OOP\Chess_OOP"
run-simple.bat
```

## Troubleshooting

If you still get errors:

1. **Check Java installation:**
   ```cmd
   java -version
   ```

2. **Check Maven installation:**
   ```cmd
   mvn -version
   ```

3. **Try running with explicit Java path:**
   ```cmd
   "C:\Program Files\Java\jdk-11\bin\java" -cp "target\classes;target\dependency\*" com.chess.Main
   ```

4. **If antivirus is blocking, try temporarily disabling it or adding an exception**

## Why Git Bash doesn't work well:

- Git Bash has issues with Java GUI applications on Windows
- Terminal process termination (exit code 256) is common with Git Bash + Java GUI
- Windows Command Prompt or PowerShell work much better for Java applications

## What the Chess Game includes:

✅ Game Teacher Hint System with Stockfish AI
✅ Local multiplayer (Hot-seat)
✅ AI opponent with multiple difficulty levels  
✅ Online multiplayer support
✅ Modern FlatLaf UI with multiple themes
✅ Save/Load game functionality
✅ Comprehensive move validation and game rules
