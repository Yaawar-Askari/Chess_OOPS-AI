@echo off
rem Chess Game Launcher Script with FlatLaf Support (Windows)

cd /d "%~dp0"

echo Building Chess Game...
mvn compile -q

if %errorlevel% neq 0 (
    echo Build failed. Exiting.
    exit /b 1
)

rem Set up classpath with all dependencies
set CLASSPATH=target\classes
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\ch\qos\logback\logback-classic\1.2.11\logback-classic-1.2.11.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\ch\qos\logback\logback-core\1.2.11\logback-core-1.2.11.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\com\formdev\flatlaf\3.2.5\flatlaf-3.2.5.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\com\formdev\flatlaf-extras\3.2.5\flatlaf-extras-3.2.5.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\com\github\weisj\jsvg\1.2.0\jsvg-1.2.0.jar

echo Starting Chess Game with FlatLaf...
java -cp "%CLASSPATH%" com.chess.Main %*
