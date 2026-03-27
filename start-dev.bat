@echo off
chcp 65001 >nul 2>&1
setlocal EnableDelayedExpansion

:: =============================================
::  TongYangYuan 一键启动脚本
::  包含：MySQL + Redis + LiveKit + OpenIM 全套 + Spring Boot + 前端
:: =============================================
title TongYangYuan - 一键启动

set "PROJECT_DIR=D:\AllProject\AndroidStudioProjects\TYY"
set "SERVER_DIR=%PROJECT_DIR%\TongYangYuan-Server"
set "WEB_DIR=%SERVER_DIR%\web"
set "CONSULTANT_DIR=%PROJECT_DIR%\TongYangYuan-Web"
set "DEPLOY_DIR=%SERVER_DIR%\deploy"

:: 颜色定义
set "C_OK=[OK]"
set "C_FAIL=[FAIL]"
set "C_INFO=[INFO]"
set "C_WARN=[WARN]"
set "C_END=[DONE]"

:: =============================================
::  启动标题
:: =============================================
echo.
echo  ========================================
echo    TongYangYuan 一键启动脚本
echo  ========================================
echo.

:: =============================================
::  检查 Docker
:: =============================================
echo [1/8] 检查 Docker Desktop...
where docker >nul 2>&1
if errorlevel 1 (
    echo %C_FAIL% 未找到 Docker，请安装 Docker Desktop
    echo.
    echo 按任意键退出...
    pause >nul
    exit
)

docker info >nul 2>&1
if errorlevel 1 (
    echo %C_FAIL% Docker 未运行
    echo   请先启动 Docker Desktop，然后重新运行此脚本
    echo.
    echo 按任意键退出...
    pause >nul
    exit
)
echo %C_OK% Docker 已运行

:: =============================================
::  确保必要目录存在
:: =============================================
echo.
echo [2/8] 准备目录...
if not exist "%DEPLOY_DIR%\redis\data"      mkdir "%DEPLOY_DIR%\redis\data"
if not exist "%DEPLOY_DIR%\mysql\data"      mkdir "%DEPLOY_DIR%\mysql\data"
if not exist "%DEPLOY_DIR%\mysql\conf"      mkdir "%DEPLOY_DIR%\mysql\conf"
if not exist "%DEPLOY_DIR%\openim\data"     mkdir "%DEPLOY_DIR%\openim\data"
if not exist "%DEPLOY_DIR%\etcd\data"       mkdir "%DEPLOY_DIR%\etcd\data"
if not exist "%DEPLOY_DIR%\mongodb\data"    mkdir "%DEPLOY_DIR%\mongodb\data"
if not exist "%DEPLOY_DIR%\kafka\data"      mkdir "%DEPLOY_DIR%\kafka\data"
if not exist "%DEPLOY_DIR%\minio\data"      mkdir "%DEPLOY_DIR%\minio\data"
echo %C_OK% 目录已就绪

:: =============================================
::  启动 MySQL 和 Redis（基础服务）
:: =============================================
echo.
echo [3/8] 启动 MySQL + Redis...

cd /d "%DEPLOY_DIR%"

docker compose up -d mysql redis

if errorlevel 1 (
    echo %C_WARN% docker compose 启动 MySQL/Redis 失败，尝试手动启动...

    docker stop tongyangyuan-mysql 2>nul
    docker rm   tongyangyuan-mysql 2>nul
    docker run -d --name tongyangyuan-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=TongYuan@2026 -e MYSQL_DATABASE=mental_health_db -v "%DEPLOY_DIR%\mysql\data:/var/lib/mysql" mysql:8.0 --default-authentication-plugin=mysql_native_password

    docker stop tongyangyuan-redis 2>nul
    docker rm   tongyangyuan-redis 2>nul
    docker run -d --name tongyangyuan-redis -p 6379:6379 -v "%DEPLOY_DIR%\redis\data:/data" redis:7-alpine redis-server --appendonly yes --requirepass Redis@2026
)

echo.
echo   等待 MySQL + Redis 启动...
timeout /t 5 /nobreak >nul

:: 检查容器状态
docker ps --filter "name=tongyangyuan-mysql" --filter "name=tongyangyuan-redis" --format "%%{.Names}: %%.{Status}"

:: =============================================
::  等待 MySQL 完全就绪
:: =============================================
echo.
echo [4/8] 等待 MySQL 就绪（最多 60 秒）...

set "MYSQL_UP=0"
for /L %%i in (1,1,60) do (
    docker exec tongyangyuan-mysql mysqladmin ping -h localhost -uroot -pTongYuan@2026 >nul 2>&1
    if !errorlevel!==0 (
        set "MYSQL_UP=1"
        goto :mysql_ready
    )
    echo   等待 MySQL 初始化... %%i/60
    timeout /t 2 /nobreak >nul
)
:mysql_ready
if "!MYSQL_UP!"=="1" (
    echo %C_OK% MySQL 已就绪
) else (
    echo %C_WARN% MySQL 启动可能超时，但仍会继续...
)

:: =============================================
::  启动 LiveKit
:: =============================================
echo.
echo [5/8] 启动 LiveKit 视频服务...

docker compose up -d livekit
if errorlevel 1 (
    echo %C_WARN% docker compose 启动 LiveKit 失败，尝试手动启动...

    docker stop tongyangyuan-livekit 2>nul
    docker rm   tongyangyuan-livekit 2>nul
    docker run -d --name tongyangyuan-livekit -p 7880:7880 -p 7881:7881 -p 7882:7882/udp -v "%DEPLOY_DIR%\livekit\config.yaml:/etc/livekit.yaml" livekit/livekit-server:latest --config /etc/livekit.yaml --node-ip 127.0.0.1
)

:: 等待 LiveKit
set "LIVEKIT_UP=0"
for /L %%i in (1,1,30) do (
    curl.exe -s -o nul http://localhost:7880/ 2>nul
    if !errorlevel!==0 (
        set "LIVEKIT_UP=1"
        goto :livekit_ready
    )
    echo   等待 LiveKit 初始化... %%i/30
    timeout /t 2 /nobreak >nul
)
:livekit_ready
if "!LIVEKIT_UP!"=="1" (
    echo %C_OK% LiveKit 已就绪
) else (
    echo %C_WARN% LiveKit 启动可能超时，但仍会继续...
)

:: =============================================
::  启动 OpenIM（即时通讯 - etcd + MongoDB + Kafka + MinIO + OpenIM Server）
:: =============================================
echo.
echo [6/8] OpenIM 即时通讯已改用 Spring Boot 内置 STOMP WebSocket（端口 8080）
echo   Android 客户端无需独立 OpenIM 服务器，直接连接后端 ws://.../stomp
echo %C_OK% 无需额外配置

:: =============================================
::  配置 ADB 端口转发（模拟器用）
:: =============================================
echo.
echo [ADB] 配置模拟器端口转发（让模拟器连到本机服务）...
set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if exist "%ADB%" (
    echo   检测到 ADB，开始配置端口转发...
    :: 先检查是否有模拟器连接
    "%ADB%" devices >"%TEMP%\adb_devices.txt" 2>&1
    findstr /C:"device$" "%TEMP%\adb_devices.txt" | findstr /v "List" >nul 2>&1
    if not errorlevel 1 (
        "%ADB%" reverse tcp:8080   tcp:8080   >nul 2>&1
        "%ADB%" reverse tcp:7880   tcp:7880   >nul 2>&1
        echo %C_OK% ADB 端口转发已配置（模拟器可正常访问本机服务）
    ) else (
        echo %C_WARN% 未检测到模拟器，跳过 ADB 配置
        echo   启动模拟器后，请手动运行以下命令：
        echo   "%ADB%" reverse tcp:8080   tcp:8080
        echo   "%ADB%" reverse tcp:7880   tcp:7880
    )
    del "%TEMP%\adb_devices.txt" 2>nul
) else (
    echo %C_WARN% ADB 未找到，跳过端口转发
    echo   如需配置，请安装 Android SDK 或手动转发端口
)

:: =============================================
::  启动 Spring Boot 后端
:: =============================================
echo.
echo [7/8] 启动 Spring Boot 后端...

:: 设置 JAVA_HOME
if not defined JAVA_HOME (
    if exist "D:\Android-studio\jbr\bin\java.exe" (
        set "JAVA_HOME=D:\Android-studio\jbr"
    ) else if exist "D:\Android Studio\jbr\bin\java.exe" (
        set "JAVA_HOME=D:\Android Studio\jbr"
    )
)
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

:: 杀掉旧的 Spring Boot 进程
wmic process where "name='java.exe' and commandline like '%%mental-health%%'" call terminate >nul 2>&1
wmic process where "name='java.exe' and commandline like '%%tongyangyuan%%'" call terminate >nul 2>&1

:: 启动后端（在新窗口中运行）
start "TongYangYuan-Backend" cmd /k "title TongYangYuan-Backend && cd /d "%SERVER_DIR%" && mvn spring-boot:run"

echo %C_INFO% 后端已在新窗口启动（需要 20-60 秒编译启动）
echo   请查看 "TongYangYuan-Backend" 窗口查看日志

:: =============================================
::  启动前端服务
:: =============================================
echo.
echo [8/8] 启动前端服务...

:: 启动咨询师端（Python HTTP 服务器）
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5500 " ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
)
start "TongYangYuan-Consultant" cmd /k "title TongYangYuan-Consultant && cd /d "%CONSULTANT_DIR%" && python -m http.server 5500"
echo %C_OK% 咨询师端已启动 (http://localhost:5500)

:: 启动管理后台（如果 node_modules 存在）
if exist "%WEB_DIR%\node_modules" (
    start "TongYangYuan-Admin" cmd /k "title TongYangYuan-Admin && cd /d "%WEB_DIR%" && npm run dev"
    echo %C_OK% 管理后台已启动 (http://localhost:8002)
) else (
    echo %C_WARN% 管理后台依赖未安装，跳过
    echo   如需启动，请先: cd "%WEB_DIR%" ^&^& npm install
)

:: =============================================
::  完成汇总
:: =============================================
echo.
echo  ========================================
echo %C_END%  所有服务已启动！
echo  ========================================
echo.
echo   服务地址（本机浏览器访问电脑）:
echo   ----------------------------------------
docker ps --filter "name=tongyangyuan" --format "  %%.{Names}: %%.{Status}"
echo.
echo   咨询师端:     http://localhost:5500
echo   管理后台:     http://localhost:8002 ^(如已安装^)
echo   后端 API:     http://localhost:8080/api
echo   STOMP WS:     ws://localhost:8080/stomp（Android 实时消息）
echo   LiveKit:      ws://localhost:7880（视频通话）
echo.
echo   模拟器访问地址（App 通过 adb reverse 连接本机）:
echo   ----------------------------------------
echo   后端:    http://127.0.0.1:8080/api
echo   即时通讯: ws://127.0.0.1:8080/stomp（STOMP WebSocket，集成在后端）
echo   视频通话: ws://127.0.0.1:7880（LiveKit）
echo.
echo  ========================================
echo   按任意键打开咨询师端测试页面...
echo  ========================================
pause >nul

:: 打开浏览器
start http://localhost:5500
