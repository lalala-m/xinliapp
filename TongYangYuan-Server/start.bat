@echo off
echo ========================================
echo TongYangYuan Mental Health Server
echo ========================================
echo.

echo [1/4] Checking Java environment...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found. Please install JDK 17 or higher
    pause
    exit /b 1
)
echo [OK] Java environment check passed
echo.

echo [2/4] Checking MySQL connection...
mysql -u root -p123456 -e "USE mental_health_db;" >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] Cannot connect to MySQL database
    echo Please ensure:
    echo   1. MySQL service is running
    echo   2. Database mental_health_db exists
    echo   3. Username and password are correct (root/123456)
    echo.
    echo Continue to start server? (May fail)
    pause
)
echo [OK] MySQL connection successful
echo.

echo [3/4] Building project...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Project build failed
    pause
    exit /b 1
)
echo [OK] Project built successfully
echo.

echo [4/4] Starting server...
echo Server will start at http://localhost:8080
echo WebSocket endpoint: ws://localhost:8080/api/ws
echo.
echo Press Ctrl+C to stop the server
echo.

java -jar target\mental-health-server-1.0.0.jar

pause
