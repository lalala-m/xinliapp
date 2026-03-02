@echo off
echo ========================================
echo Database Initialization Script
echo ========================================
echo.

echo Connecting to MySQL and initializing database...
echo.

mysql -u root -p123456 < database\schema.sql

if %errorlevel% equ 0 (
    echo [SUCCESS] Database initialized successfully!
    echo.
    echo Test accounts created:
    echo.
    echo Consultant Account:
    echo   Phone: 13800000001
    echo   Password: 123456
    echo.
    echo Parent Account:
    echo   Phone: 13900000001
    echo   Password: 123456
    echo.
) else (
    echo [ERROR] Database initialization failed
    echo Please check:
    echo   1. MySQL service is running
    echo   2. Username and password are correct (root/123456)
    echo   3. database\schema.sql file exists
)

pause
