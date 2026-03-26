@echo off
REM ============================================================
REM  模拟器连本机后端 + LiveKit：adb reverse
REM  请先启动 Android 模拟器，再双击本文件
REM ============================================================
@echo off
chcp 65001 >nul

set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if not exist "%ADB%" (
    echo [FAIL] 未找到 adb.exe
    echo 请把路径改成你的 SDK：%%LOCALAPPDATA%%\Android\Sdk\platform-tools\adb.exe
    echo 或在 Android Studio > SDK Manager > SDK Tools 里安装 Android SDK Platform-Tools
    pause
    exit /b 1
)

echo [INFO] 检测模拟器...
"%ADB%" devices | findstr "device$" | findstr /v "List" >nul 2>&1
if errorlevel 1 (
    echo [FAIL] 未检测到运行中的模拟器或设备
    echo 请先启动 Android Studio > Run App 或 AVD Manager > Start
    echo.
    echo 已连接的设备：
    "%ADB%" devices
    pause
    exit /b 1
)

echo [OK]  找到运行中的模拟器/设备
echo.

echo [1/2] 反向代理后端（8080 -> 127.0.0.1:8080）...
"%ADB%" reverse tcp:8080 tcp:8080 2>&1
if %errorlevel% equ 0 (
    echo   [OK] 8080 已映射
) else (
    echo   [FAIL] 8080 映射失败
)

echo.
echo [2/2] 反向代理 LiveKit（7880 -> 127.0.0.1:7880）...
"%ADB%" reverse tcp:7880 tcp:7880 2>&1
if %errorlevel% equ 0 (
    echo   [OK] 7880 已映射
) else (
    echo   [WARN] 7880 映射失败（LiveKit 未启动或端口占用）
    echo   提示：确保已在 start-dev.bat 中成功启动了 LiveKit 服务
)

echo.
echo [DONE] 模拟器调试环境已就绪
echo.
echo 当前映射列表：
"%ADB%" reverse --list
echo.
pause
