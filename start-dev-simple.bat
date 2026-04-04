@echo off
setlocal EnableDelayedExpansion

set "PROJECT_DIR=D:\AllProject\AndroidStudioProjects\TYY"
set "SERVER_DIR=%PROJECT_DIR%\TongYangYuan-Server"
set "DEPLOY_DIR=%SERVER_DIR%\deploy"
set "CONSULTANT_DIR=%PROJECT_DIR%\TongYangYuan-Web"
set "WEB_DIR=%SERVER_DIR%\web"

echo.
echo ========================================
echo  TongYangYuan Start Script v8 (Simple)
echo ========================================
echo.

:: Step 1: Check Docker
echo [1] Checking Docker...
where docker >nul 2>&1
if errorlevel 1 (
    echo [FAIL] Docker not found.
    pause
    exit /b 1
)
echo [OK] Docker running.

:: Step 2: Clean containers
echo.
echo [2] Cleaning old containers...
docker stop -t 3 tongyangyuan-redis 2>nul
docker rm -f tongyangyuan-redis 2>nul
docker stop -t 3 tongyangyuan-mysql 2>nul
docker rm -f tongyangyuan-mysql 2>nul
docker stop -t 3 tongyangyuan-livekit 2>nul
docker rm -f tongyangyuan-livekit 2>nul
docker stop -t 3 tongyangyuan-openim 2>nul
docker rm -f tongyangyuan-openim 2>nul
echo [OK] Done.

:: Step 3: Start Redis
echo.
echo [3] Starting Redis...
docker run -d --name tongyangyuan-redis -p 6379:6379 -v "%DEPLOY_DIR%\redis\data:/data" redis:7-alpine redis-server --appendonly yes --requirepass Redis@2026
echo [OK] Redis started.

:: Step 4: MySQL info
echo.
echo [4] MySQL: Using Windows MySQL on port 3306
echo     Database: mental_health_db
echo     User: root, Password: 123456

:: Step 5: Create directories
echo.
echo [5] Creating directories...
for %%d in (kafka\data mongodb\data etcd\data minio\data livekit openim\data) do (
    if not exist "%DEPLOY_DIR%\%%d" mkdir "%DEPLOY_DIR%\%%d" 2>nul
)
echo [OK] Done.

:: Step 6: ADB setup
echo.
echo [6] Setting up ADB...
set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if exist "%ADB%" (
    for /f "tokens=1" %%d in ('"%ADB%" devices 2^>nul ^| findstr /r "^[0-9]" 2^>nul') do (
        echo   Forwarding ports for %%d...
        "%ADB%" -s %%d reverse tcp:8080 tcp:8080 >nul 2>&1
        "%ADB%" -s %%d reverse tcp:7880 tcp:7880 >nul 2>&1
        "%ADB%" -s %%d reverse tcp:10001 tcp:10001 >nul 2>&1
        "%ADB%" -s %%d reverse tcp:10002 tcp:10002 >nul 2>&1
    )
    echo [OK] ADB configured.
) else (
    echo [SKIP] ADB not found.
)

:: Step 7: Start Backend
echo.
echo [7] Starting Spring Boot backend...
if not defined JAVA_HOME (
    if exist "D:\Android-studio\jbr\bin\java.exe" set "JAVA_HOME=D:\Android-studio\jbr"
)
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"
echo   JAVA_HOME: !JAVA_HOME!
start "TongYangYuan-Backend" cmd /k "title TongYangYuan-Backend && cd /d "%SERVER_DIR%" && mvn spring-boot:run"

:: Step 8: Start Consultant Web
echo.
echo [8] Starting Consultant Web...
start "TongYangYuan-Consultant" cmd /k "title TongYangYuan-Consultant && cd /d "%CONSULTANT_DIR%" && python -m http.server 5500"
echo [OK] Consultant: http://localhost:5500

:: Step 9: Start Admin Web
echo.
echo [9] Starting Admin Web...
if exist "%WEB_DIR%\package.json" (
    start "TongYangYuan-Admin" cmd /k "title TongYangYuan-Admin && cd /d "%WEB_DIR%" && npm run dev"
    echo [OK] Admin: http://localhost:8002
) else (
    echo [SKIP] Admin Web not installed.
)

:: Done
echo.
echo ========================================
echo [DONE] All services started!
echo ========================================
echo.
echo Service URLs:
echo   Backend:  http://localhost:8080/api
echo   Consultant: http://localhost:5500
echo   Admin:    http://localhost:8002
echo   MySQL:    localhost:3306 (Windows MySQL)
echo   Redis:    localhost:6379
echo.
echo Press any key to open consultant page...
pause >nul
start http://localhost:5500
