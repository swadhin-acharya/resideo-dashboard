@echo off
setlocal enabledelayedexpansion

echo === OpenQA Dashboard Local Edition ===
echo.

if not exist data mkdir data

set JAR_PATH=openqa-dashboard-standalone\target\openqa-dashboard-standalone-1.0.0-SNAPSHOT.jar

if exist "%JAR_PATH%" (
    echo Starting OpenQA Dashboard from JAR...
    where /q java
    if errorlevel 1 (
        echo ERROR: Java not found. Install a JDK 21+ from https://adoptium.net/
        pause
        exit /b 1
    )
    java -jar "%JAR_PATH%"
    goto :end
)

echo JAR not found - building from source...
call mvn clean install -DskipTests -q

echo.
echo Starting OpenQA Dashboard...
java -jar "%JAR_PATH%"

:end
endlocal
