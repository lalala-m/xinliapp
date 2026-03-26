@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

:: =============================================
::  TongYangYuan 一键启动脚本（增强版）
:: =============================================
title TongYangYuan - All Services Starter

set "PROJECT_DIR=D:\AllProject\AndroidStudioProjects\TYY"
set "SERVER_DIR=%PROJECT_DIR%\TongYangYuan-Server"
set "WEB_DIR=%PROJECT_DIR%\TongYangYuan-Server\web"
set "CONSULTANT_DIR=%PROJECT_DIR%\TongYangYuan-Web"

:: 颜色定义
set "C_OK=[OK]"
set "C_FAIL=[FAIL]"
set "C_INFO=[INFO]"
set "C_WARN=[WARN]"
set "C_END=[  DONE  ]"

goto :main

:: =============================================
:: 检查工具
:: =============================================
:check_tools
echo.
echo [Step 0/6] 检查必要工具...
echo.

set "TOOL_ERRORS="

:: Java
where java >nul 2>&1
if errorlevel 1 (
    echo %C_FAIL% 未找到 Java，请安装 JDK 17+
    set "TOOL_ERRORS=!TOOL_ERRORS! Java"
) else (
    java -version 2>&1 | findstr /i "version" >nul
    echo %C_OK% Java 已找到
)

:: Maven
where mvn >nul 2>&1
if errorlevel 1 (
    echo %C_FAIL% 未找到 Maven，请安装并加入 PATH
    set "TOOL_ERRORS=!TOOL_ERRORS! Maven"
) else (
    echo %C_OK% Maven 已找到
)

:: Node / npm
where node >nul 2>&1
if errorlevel 1 (
    echo %C_FAIL% 未找到 Node.js，请安装
    set "TOOL_ERRORS=!TOOL_ERRORS! Node.js"
) else (
    where npm >nul 2>&1
    if not errorlevel 1 (
        echo %C_OK% Node.js / npm 已找到
    ) else (
        echo %C_WARN% Node.js 已找到但 npm 未找到
        set "TOOL_ERRORS=!TOOL_ERRORS! npm"
    )
)

:: Python
where python >nul 2>&1
if errorlevel 1 (
    where python3 >nul 2>&1
    if not errorlevel 1 (
        set "PYTHON=python3"
        echo %C_OK% Python3 已找到
    ) else (
        echo %C_FAIL% 未找到 Python，请安装
        set "TOOL_ERRORS=!TOOL_ERRORS! Python"
    )
) else (
    set "PYTHON=python"
    echo %C_OK% Python 已找到
)

:: Docker
where docker >nul 2>&1
if errorlevel 1 (
    echo %C_WARN% 未找到 Docker（或未加入 PATH），Redis/LiveKit 将通过 WSL 启动
    set "DOCKER_FOUND=0"
) else (
    docker info >nul 2>&1
    if errorlevel 1 (
        echo %C_WARN% Docker 已找到但未运行（请打开 Docker Desktop）
        set "DOCKER_FOUND=0"
    ) else (
        set "DOCKER_FOUND=1"
        echo %C_OK% Docker 已找到并运行中
    )
)

:: ADB
set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if exist "%ADB%" (
    echo %C_OK% ADB 已找到
    set "ADB_FOUND=1"
) else (
    echo %C_WARN% 未找到 ADB（%%LOCALAPPDATA%%\Android\Sdk\platform-tools\adb.exe）
    echo         模拟器调试需要手动设置 ADB 路径
    set "ADB_FOUND=0"
)

:: 工具检查结论
if defined TOOL_ERRORS (
    echo.
    echo =============================================
    echo %C_FAIL% 缺少以下必要工具: %TOOL_ERRORS%
    echo =============================================
    echo 请先安装缺失工具后再运行本脚本
    pause
    exit /b 1
)

echo.
echo %C_OK% 所有必要工具检查通过
goto :eof

:: =============================================
:: 主流程
:: =============================================
:main

echo.
echo =============================================
echo       TongYangYuan 一键启动脚本（增强版）
echo =============================================
echo.

call :check_tools
if errorlevel 1 goto :end

:: -------------------------------------------------
:: Step 1: 启动 Docker 服务（Redis + LiveKit）
:: -------------------------------------------------
echo.
echo [Step 1/6] 启动 Docker 服务（Redis + LiveKit）...
echo.

if "!DOCKER_FOUND!"=="1" (
    echo %C_INFO% 检查 docker compose 服务状态...
    cd /d "%SERVER_DIR%"
    docker compose ps >nul 2>&1

    docker compose up -d redis livekit 2>&1 | findstr /i /v "Downloading\|Pulling\|Extracting\|Verifying"

    :: 等待服务就绪
    echo %C_INFO% 等待 Redis 就绪...
    for /L %%i in (1,1,10) do (
        docker exec redis-server ping >nul 2>&1
        if not errorlevel 1 goto :redis_ok
        timeout /t 2 >nul
    )
    :redis_ok
    echo   %C_OK% Redis 已启动

    echo %C_INFO% 等待 LiveKit 就绪...
    timeout /t 5 >nul
    docker ps --filter "name=livekit-server" --filter "status=running" | findstr livekit >nul 2>&1
    if not errorlevel 1 (
        echo   %C_OK% LiveKit 已启动
    ) else (
        echo   %C_WARN% LiveKit 启动中（可能需要 10-30 秒）
    )
) else (
    :: WSL Redis
    echo %C_WARN% Docker 未运行，尝试通过 WSL 启动 Redis...
    wsl -d Ubuntu redis-cli -a Redis@2026 ping >nul 2>&1
    if errorlevel 1 (
        echo %C_WARN% WSL Redis 未运行，尝试启动...
        wsl -d Ubuntu redis-server --daemonize yes >nul 2>&1
        timeout /t 2 >nul
        wsl -d Ubuntu redis-cli -a Redis@2026 ping >nul 2>&1
        if not errorlevel 1 (
            echo   %C_OK% WSL Redis 已启动
        ) else (
            echo   %C_FAIL% Redis 启动失败，请确保：
            echo         1. WSL Ubuntu 已安装
            echo         2. Redis 已通过 sudo apt install redis-server 安装
            echo         3. 已执行：sudo redis-server --daemonize yes
        )
    ) else (
        echo   %C_OK% WSL Redis 已运行
    )
    echo %C_WARN% LiveKit 无法启动（需要 Docker），视频通话将不可用
)

:: -------------------------------------------------
:: Step 2: 启动 Spring Boot 后端
:: -------------------------------------------------
echo.
echo [Step 2/6] 启动 Spring Boot 后端...
echo   (后端窗口将单独打开，需约 20-60 秒启动)
echo.

:: 设置 JAVA_HOME（如果未设置）
if not defined JAVA_HOME (
    set "JAVA_HOME=D:\Android-studio\jbr"
    if not exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_HOME=D:\Android Studio\jbr"
        if not exist "%JAVA_HOME%\bin\java.exe" (
            echo %C_WARN% JAVA_HOME 未设置且未找到默认路径，尝试使用系统 PATH 中的 java
            set "JAVA_HOME="
        )
    )
)
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    echo   JAVA_HOME=%JAVA_HOME%
)

:: 杀掉旧的后端进程（如果还在跑）
wmic process where "name='java.exe' and commandline like '%%mental-health%%'" call terminate >nul 2>&1
wmic process where "name='java.exe' and commandline like '%%tongyangyuan%%'" call terminate >nul 2>&1

start "TongYangYuan-Backend" cmd /k "title TongYangYuan-Backend && cd /d "%SERVER_DIR%" && mvn spring-boot:run"

:: -------------------------------------------------
:: Step 3: 等待后端启动并检测健康
:: -------------------------------------------------
echo.
echo [Step 3/6] 等待后端启动...
echo.

set "BACKEND_UP=0"
for /L %%i in (1,1,30) do (
    curl.exe -s -o nul -w "%%{http_code}" http://localhost:8080/api/home/config >nul 2>&1
    if not errorlevel 1 (
        for /f %%c in ('curl.exe -s -o nul -w "%%{http_code}" http://localhost:8080/api/home/config') do (
            if "%%c"=="200" (
                set "BACKEND_UP=1"
                goto :backend_ready
            )
        )
    )
    echo   等待中... %%i/30  (约需 20-60 秒，请耐心)
    timeout /t 3 >nul
)

:backend_ready
if "!BACKEND_UP!"=="1" (
    echo   %C_OK% Spring Boot 后端已启动（http://localhost:8080/api）
) else (
    echo   %C_WARN% 后端可能还在启动，请查看 TongYangYuan-Backend 窗口
)

:: -------------------------------------------------
:: Step 4: 启动前端 Vue 管理后台
:: -------------------------------------------------
echo.
echo [Step 4/6] 启动前端 Vue 管理后台...
echo   (窗口将单独打开)
echo.

if exist "%WEB_DIR%\node_modules" (
    start "TongYangYuan-Frontend" cmd /k "title TongYangYuan-Frontend && cd /d "%WEB_DIR%" && npm run dev"
) else (
    echo   %C_INFO% 首次运行，正在安装依赖（仅一次，需等待）...
    start "TongYangYuan-Frontend-Install" cmd /k "title TongYangYuan-Frontend-Install && cd /d "%WEB_DIR%" && npm install && npm run dev"
)

:: -------------------------------------------------
:: Step 5: 启动咨询师静态页面
:: -------------------------------------------------
echo.
echo [Step 5/6] 启动咨询师端页面...
echo.

:: 杀掉旧的 python http.server
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5500" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
)
start "TongYangYuan-Consultant" cmd /k "title TongYangYuan-Consultant && cd /d "%CONSULTANT_DIR%" && %PYTHON% -m http.server 5500"

:: -------------------------------------------------
:: Step 6: ADB Reverse（模拟器调试）
:: -------------------------------------------------
echo.
echo [Step 6/6] 配置模拟器反向代理（adb reverse）...
echo.

if "!ADB_FOUND!"=="1" (
    "%ADB%" devices | findstr "device$" | findstr /v "List" >nul 2>&1
    if not errorlevel 1 (
        "%ADB%" reverse tcp:8080 tcp:8080 >nul 2>&1
        "%ADB%" reverse tcp:7880 tcp:7880 >nul 2>&1
        echo   %C_OK% adb reverse tcp:8080 tcp:8080 已生效
        echo   %C_OK% adb reverse tcp:7880 tcp:7880 已生效
    ) else (
        echo   %C_WARN% 未检测到运行中的模拟器
        echo         请先启动模拟器，再手动执行：
        echo         "%ADB%" reverse tcp:8080 tcp:8080
        echo         "%ADB%" reverse tcp:7880 tcp:7880
    )
) else (
    echo   %C_WARN% ADB 未找到，跳过
)

:: -------------------------------------------------
:: 完成
:: -------------------------------------------------
echo.
echo.
echo =============================================
echo %C_END%  所有服务已启动！
echo =============================================
echo.
echo   后端 API:    http://localhost:8080/api
echo   管理后台:    http://localhost:8002
echo   咨询师端:    http://localhost:5500
echo.
echo   模拟器（Android）访问：
echo     后端:    http://127.0.0.1:8080/api
echo     LiveKit: ws://127.0.0.1:7880
echo     ^(已通过 adb reverse 映射^)
echo.
echo   视频通话测试（需预约为 ACCEPTED 状态）：
echo     1. 咨询师在 http://localhost:5500/dashboard.html 登录
echo     2. 家长在 Android 模拟器 App 中向该咨询师发起视频
echo     3. 咨询师在聊天页面点击接听，进入 video-call.html
echo     4. 双方应看到对方画面
echo.
echo =============================================
echo   如有问题，查看对应服务窗口的控制台输出
echo =============================================
echo.
pause

:end
