@echo off
chcp 65001 >nul 2>&1
cd /d %~dp0
title TongYangYuan Start

echo ========================================
echo TongYangYuan - All-in-One Start
echo ========================================
echo.

REM Step 1: Check Docker
echo [1/6] Checking Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running!
    echo Please start Docker Desktop first
    pause
    exit
) else (
    echo [OK] Docker is running
)

REM Step 2: Start Docker services
echo [2/6] Starting Docker services...

docker start tyy-redis >nul 2>&1
docker start tongyuan-redis >nul 2>&1
echo [OK] Redis started

docker start tyy-livekit >nul 2>&1
docker start tongyuan-livekit >nul 2>&1
echo [OK] LiveKit started

docker start tyy-mysql >nul 2>&1
docker start tongyuan-mysql >nul 2>&1
echo [OK] MySQL containers started

echo.

REM Step 3: Start OpenIM
echo [3/6] Starting OpenIM...

docker ps -a --format "{{.Names}}" | findstr /i "tongyangyuan-openim" >nul 2>&1
if errorlevel 1 (
    echo [INFO] Creating OpenIM container...
    docker run -d --name tongyangyuan-openim -p 10001:10001 -p 10002:10002 -e TZ=Asia/Shanghai ghcr.io/openimsdk/openim-server:latest
    echo [INFO] OpenIM container created (first start may take 1-2 min)
) else (
    docker start tongyangyuan-openim >nul 2>&1
    echo [OK] OpenIM started (ports 10001, 10002)
)

echo.

REM Step 4: Check MySQL
echo [4/6] Checking MySQL...
mysql -u root -p123456 -e "SELECT 1;" >nul 2>&1
if errorlevel 1 (
    echo [WARNING] MySQL may not be running
    echo [TIP] Make sure MySQL80 service is started
) else (
    echo [OK] MySQL connected
)
echo.

REM Step 5: Start Backend
echo [5/6] Starting Backend...
echo.

cd /d %~dp0TongYangYuan-Server

netstat -ano | findstr ":8080" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [WARNING] Port 8080 is occupied, backend may already be running
) else (
    echo Starting Spring Boot backend...
    echo [TIP] First start takes 3-5 minutes for build
    echo.
    start "TYY-Backend" cmd /k "title TYY Backend && mvn spring-boot:run"
)
echo.

REM Step 5.5: Setup ADB Reverse for mobile/ emulator connectivity
echo [5.5/6] Setting up ADB reverse for mobile connection...
echo.

set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if exist "%ADB%" (
    "%ADB%" devices | findstr "device$" | findstr /v "List" >nul 2>&1
    if not errorlevel 1 (
        echo [INFO] Device/Emulator detected, setting up port forwarding...
        "%ADB%" reverse tcp:8080 tcp:8080 2>&1 | findstr /v "error" >nul 2>&1
        if %errorlevel% equ 0 (
            echo [OK] ADB reverse 8080 established (mobile can connect to backend)
        ) else (
            REM Try again without filtering
            "%ADB%" reverse tcp:8080 tcp:8080 >nul 2>&1
            if %errorlevel% equ 0 (
                echo [OK] ADB reverse 8080 established
            ) else (
                echo [SKIP] ADB reverse 8080 failed (no device connected or USB debugging off)
            )
        )
        
        "%ADB%" reverse tcp:7880 tcp:7880 >nul 2>&1
        if %errorlevel% equ 0 (
            echo [OK] ADB reverse 7880 established (LiveKit)
        )
    ) else (
        echo [SKIP] No device/emulator detected
        echo [TIP] If using physical phone, enable USB debugging and connect via USB
    )
) else (
    echo [SKIP] ADB not found (SDK may not be installed)
    echo [TIP] If developing for mobile, install Android SDK Platform-Tools
)
echo.

REM Step 6: Start Frontend
echo [6/6] Starting Frontend...
cd /d %~dp0TongYangYuan-Web

netstat -ano | findstr ":5500" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [WARNING] Port 5500 is occupied, frontend may already be running
) else (
    echo Starting frontend...
    start "TYY-Frontend" cmd /k "title TYY Frontend && npx live-server --port=5500 --host=127.0.0.1"
)
echo.

cd /d %~dp0

echo ========================================
echo  Startup Complete!
echo ========================================
echo.
echo Service URLs:
echo   Backend API:  http://localhost:8080/api
echo   Frontend:     http://localhost:5500
echo   LiveKit:      ws://localhost:7880
echo   OpenIM WS:   ws://localhost:10001
echo   OpenIM HTTP:  http://localhost:10002
echo.
echo Tips:
echo   1. Backend first start: 3-5 minutes
echo   2. OpenIM first start: 1-2 minutes (pull image)
echo   3. Make sure MySQL80 service is running
echo   4. Keep Docker Desktop running
echo.
echo Check backend logs in new window
echo.
pause
