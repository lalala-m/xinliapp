@echo off
setlocal EnableDelayedExpansion

set "PROJECT_DIR=D:\AllProject\AndroidStudioProjects\TYY"
set "SERVER_DIR=%PROJECT_DIR%\TongYangYuan-Server"
set "DEPLOY_DIR=%SERVER_DIR%\deploy"

echo.
echo ========================================
echo  OpenIM Chat Server Launcher
echo ========================================
echo.

:: Check if OpenIM is already running
docker ps --format "{{.Names}}" | findstr "tongyangyuan-openim" >nul 2>&1
if not errorlevel 1 (
    echo [INFO] OpenIM server is already running!
    echo   WebSocket: ws://localhost:10001
    echo   HTTP API:  http://localhost:10002
    pause
    exit /b 0
)

:: Check required services
echo [1] Checking dependencies...
echo   MongoDB...
netstat -ano | findstr ":27017 " | findstr "LISTENING" >nul 2>&1
if errorlevel 1 (
    echo   [FAIL] MongoDB is not running on port 27017
    echo   [INFO] Please start MongoDB first
    pause
    exit /b 1
)
echo   [OK] MongoDB ready

echo   Kafka...
netstat -ano | findstr ":19092 " | findstr "LISTENING" >nul 2>&1
if errorlevel 1 (
    echo   [FAIL] Kafka is not running on port 19092
    pause
    exit /b 1
)
echo   [OK] Kafka ready

echo   etcd...
netstat -ano | findstr ":12379 " | findstr "LISTENING" >nul 2>&1
if errorlevel 1 (
    echo   [FAIL] etcd is not running on port 12379
    pause
    exit /b 1
)
echo   [OK] etcd ready

echo   MinIO...
netstat -ano | findstr ":10005 " | findstr "LISTENING" >nul 2>&1
if errorlevel 1 (
    echo   [FAIL] MinIO is not running on port 10005
    pause
    exit /b 1
)
echo   [OK] MinIO ready

:: Update config to use localhost for external access
echo.
echo [2] Updating OpenIM config...

:: Create backup
copy /y "%DEPLOY_DIR%\openim\config.yaml" "%DEPLOY_DIR%\openim\config.yaml.bak" >nul 2>&1

:: Update config file - use localhost for external connections
powershell -NoProfile -Command ^
    "$f='%DEPLOY_DIR%\openim\config.yaml'; " ^ 
    "$c=Get-Content $f -Raw; " ^
    "$c=$c -replace 'mongodb://[^/]+:', 'mongodb://openIM:openIM123@localhost:'; " ^
    "$c=$c -replace 'mongodb://[^:]+:', 'mongodb://openIM:openIM123@localhost:'; " ^
    "$c=$c -replace ':3306', ':3306'; " ^
    "$c=$c -replace 'tongyuan-', 'localhost'; " ^
    "$c=$c -replace 'tongyangyuan-', 'localhost'; " ^
    "$c=$c -replace 'openImRedis', 'localhost'; " ^
    "Set-Content $f -Value $c -NoNewline -Encoding UTF8"

echo   [OK] Config updated

:: Stop old container if exists
echo.
echo [3] Starting OpenIM server...
docker stop tongyangyuan-openim 2>nul
docker rm tongyangyuan-openim 2>nul

:: Start OpenIM server with simpler config
docker run -d --name tongyangyuan-openim ^
    --add-host=host.docker.internal:host-gateway ^
    -p 10001:10001 ^
    -p 10002:10002 ^
    -e TZ=Asia/Shanghai ^
    -v "%DEPLOY_DIR%\openim\config:/etc/openim-server/config" ^
    ghcr.io/openimsdk/openim-server:latest

if errorlevel 1 (
    echo [FAIL] Failed to start OpenIM
    pause
    exit /b 1
)

echo   Container started, waiting for initialization...

:: Wait for OpenIM to be ready
set "RETRY=0"
:wait_openim
timeout /t 3 /nobreak >nul
curl -s http://localhost:10002/ping >nul 2>&1
if errorlevel 1 (
    set /a RETRY+=1
    if !RETRY! lss 20 (
        echo   Waiting... !RETRY!/20
        goto :wait_openim
    )
    echo [WARN] OpenIM may not be fully ready yet
) else (
    echo [OK] OpenIM server is ready!
)

echo.
echo ========================================
echo [DONE] OpenIM Chat is ready!
echo ========================================
echo.
echo   WebSocket: ws://localhost:10001
echo   HTTP API:  http://localhost:10002
echo.
echo   Admin credentials:
echo   - UserID:   openIMAdmin
echo   - Secret:   openIMAdmin@2026
echo.
pause
