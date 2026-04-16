@echo off
chcp 65001 >nul
title ADB 端口映射

echo ========================================
echo ADB 端口映射脚本
echo ========================================
echo.

REM 检查ADB是否可用
adb devices >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 无法找到ADB，请确保：
    echo   1. Android SDK已安装
    echo   2. ADB已添加到系统PATH
    echo   3. 模拟器或手机已连接
    pause
    exit /b 1
)

echo 检测到连接的设备：
adb devices
echo.

echo 正在设置端口映射...
echo.

REM 后端API
adb reverse tcp:8080 tcp:8080
if %errorlevel% equ 0 (
    echo [OK] 后端API: tcp:8080
) else (
    echo [失败] 后端API: tcp:8080
)

REM LiveKit WebSocket
adb reverse tcp:7880 tcp:7880
if %errorlevel% equ 0 (
    echo [OK] LiveKit WS: tcp:7880
) else (
    echo [失败] LiveKit WS: tcp:7880
)

REM LiveKit HTTP API
adb reverse tcp:7881 tcp:7881
if %errorlevel% equ 0 (
    echo [OK] LiveKit API: tcp:7881
) else (
    echo [失败] LiveKit API: tcp:7881
)

echo.
echo ========================================
echo 端口映射完成！
echo.
echo 现在你可以在模拟器中测试视频通话了。
echo.
echo 注意：如果模拟器重启，需要重新运行此脚本
echo ========================================
pause
