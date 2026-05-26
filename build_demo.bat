@echo off
echo ==============================================
echo [1/2] Building optimized JRE image using jlink
echo ==============================================
call mvn clean javafx:jlink

if %ERRORLEVEL% NEQ 0 (
    echo [Error] Maven jlink build failed!
    exit /b %ERRORLEVEL%
)

echo.
echo ==============================================
echo [2/2] Packaging application using jpackage
echo ==============================================
REM Remove previous build if exists
if exist "Release" rd /s /q "Release"

jpackage --type app-image --name DinoRun --module com.dino/com.dino.DinoMain --runtime-image target/image --dest Release

if %ERRORLEVEL% NEQ 0 (
    echo [Error] jpackage failed!
    exit /b %ERRORLEVEL%
)

echo.
echo ==============================================
echo Build Completed Successfully!
echo You can find the executable at: Release\DinoRun\DinoRun.exe
echo ==============================================
pause
