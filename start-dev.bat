@echo off
setlocal EnableDelayedExpansion

set "PROJECT_DIR=D:\AllProject\AndroidStudioProjects\TYY"
set "SERVER_DIR=%PROJECT_DIR%\TongYangYuan-Server"
set "DEPLOY_DIR=%SERVER_DIR%\deploy"
set "CONSULTANT_DIR=%PROJECT_DIR%\TongYangYuan-Web"
set "WEB_DIR=%SERVER_DIR%\web"

echo.
echo =============================================
echo   TongYangYuan 一键启动脚本 v9 (完整版)
echo =============================================
echo.

:: =============================================
:: Step 1: 检查 Docker
:: =============================================
echo [1/9] 检查 Docker...
where docker >nul 2>&1
if errorlevel 1 (
    echo [FAIL] Docker 未安装，请先安装 Docker Desktop
    pause
    exit /b 1
)
docker info >nul 2>&1
if errorlevel 1 (
    echo [FAIL] Docker 未运行，请先启动 Docker Desktop
    pause
    exit /b 1
)
echo [OK] Docker 运行正常

where docker-compose >nul 2>&1
if errorlevel 1 (
    docker compose version >nul 2>&1
    if errorlevel 1 (
        echo [FAIL] docker-compose 未安装
        pause
        exit /b 1
    )
    set "DC=docker compose"
    echo [OK] 使用 docker compose (v2)
) else (
    set "DC=docker-compose"
    echo [OK] 使用 docker-compose
)

:: =============================================
:: Step 2: 检查 Windows MySQL 和端口 3306
:: =============================================
echo.
echo [2/9] 检查 MySQL (端口 3306)...
set "USE_WINDOWS_MYSQL=0"
set "MYSQL_PORT=3306"
for /f "tokens=*" %%a in ('powershell -NoProfile -Command "try { (Get-NetTCPConnection -LocalPort 3306 -ErrorAction Stop).OwningProcess -join ',' } catch { }" 2^>nul') do (
    if not "%%a"=="" (
        set "PID_3306=%%a"
    )
)
if defined PID_3306 (
    for /f "tokens=*" %%n in ('powershell -NoProfile -Command "try { Get-Process -Id !PID_3306! -ErrorAction Stop ^| Select-Object -ExpandProperty ProcessName } catch { }" 2^>nul') do (
        echo %%n | findstr /i "mysql" >nul 2>&1
        if not errorlevel 1 (
            echo [OK] Windows MySQL 正在运行 (PID: !PID_3306!)
            echo [INFO] Spring Boot 将连接 Windows MySQL: localhost:3306
            set "USE_WINDOWS_MYSQL=1"
        ) else (
            echo [INFO] 端口 3306 被其他进程占用 (PID: !PID_3306!)
            echo [INFO] 将启动 Docker MySQL 替代，映射到端口 3307
            set "USE_WINDOWS_MYSQL=0"
            set "MYSQL_PORT=3307"
        )
    )
) else (
    echo [OK] 端口 3306 空闲，将启动 Docker MySQL
    set "USE_WINDOWS_MYSQL=0"
    set "MYSQL_PORT=3306"
)

:: =============================================
:: Step 3: 创建目录
:: =============================================
echo.
echo [3/9] 创建数据目录...
for %%d in (redis\data mysql\data etcd\data mongodb\data kafka\data minio\data livekit openim\data) do (
    if not exist "%DEPLOY_DIR%\%%d" mkdir "%DEPLOY_DIR%\%%d" 2>nul
)
echo [OK] 目录就绪

:: =============================================
:: Step 4: 停止旧的 Docker 容器（避免名称冲突）
:: =============================================
echo.
echo [4/9] 停止旧容器...
:: 先尝试用 docker-compose 停止
%DC% -f "%DEPLOY_DIR%\docker-compose-openim.yml" down >nul 2>&1
:: 单独停止可能残留的容器（兼容不同命名）
docker stop -t 3 tongyangyuan-openim 2>nul
docker rm -f tongyangyuan-openim 2>nul
docker stop -t 3 tongyuan-openim 2>nul
docker rm -f tongyuan-openim 2>nul
docker stop -t 3 tongyangyuan-redis 2>nul
docker rm -f tongyangyuan-redis 2>nul
docker stop -t 3 tongyuan-redis 2>nul
docker rm -f tongyuan-redis 2>nul
docker stop -t 3 tongyangyuan-livekit 2>nul
docker rm -f tongyangyuan-livekit 2>nul
docker stop -t 3 tongyuan-kafka 2>nul
docker rm -f tongyuan-kafka 2>nul
docker stop -t 3 tongyuan-etcd 2>nul
docker rm -f tongyuan-etcd 2>nul
docker stop -t 3 tongyuan-mongodb 2>nul
docker rm -f tongyuan-mongodb 2>nul
docker stop -t 3 tongyuan-minio 2>nul
docker rm -f tongyuan-minio 2>nul
docker stop -t 3 tongyangyuan-mysql 2>nul
docker rm -f tongyangyuan-mysql 2>nul
echo [OK] 旧容器已清理

:: =============================================
:: Step 5: 启动 LiveKit (Docker)
:: =============================================
echo.
echo [5/9] 启动 LiveKit (视频通话)...

:: 获取 Docker 网关 IP
set "LIVEKIT_NODE_IP=127.0.0.1"
for /f "tokens=*" %%a in ('powershell -NoProfile -Command "try { (docker network inspect bridge --format \"%%(Gateway)\" 2>^$null) } catch { }" 2^>nul') do (
    if not "%%a"=="" (
        echo %%a | findstr /r "^[0-9]" >nul 2>&1
        if not errorlevel 1 set "LIVEKIT_NODE_IP=%%a"
    )
)
echo   节点 IP: %LIVEKIT_NODE_IP%

:: 更新 LiveKit 配置
powershell -NoProfile -Command "$f='%DEPLOY_DIR%\livekit\config.yaml'; $c=Get-Content $f -Raw; $c -replace '(?m)^(node-ip:\s*).*','$1%LIVEKIT_NODE_IP%' | Set-Content $f -NoNewline -Encoding UTF8"

docker run -d --name tongyangyuan-livekit ^
    -p 7880:7880 ^
    -p 7881:7881 ^
    -p 7882:7882/udp ^
    -p 61000-61999:61000-61999/udp ^
    -v "%DEPLOY_DIR%\livekit\config.yaml:/etc/livekit.yaml" ^
    livekit/livekit-server:latest ^
    --config /etc/livekit.yaml ^
    --node-ip %LIVEKIT_NODE_IP%
if errorlevel 1 (
    docker start tongyangyuan-livekit >nul 2>&1
)
echo [OK] LiveKit 已启动 (ws://localhost:7880)

:: 等待 LiveKit 启动
for /L %%i in (1,1,20) do (
    curl.exe -s --max-time 3 -o nul http://localhost:7880/ 2>nul
    if not errorlevel 1 (
        echo [OK] LiveKit 就绪
        goto :livekit_ready
    )
    timeout /t 2 /nobreak >nul
)
echo [WARN] LiveKit 启动超时，继续...
:livekit_ready

:: =============================================
:: Step 6: 启动 OpenIM 全家桶 (docker-compose)
:: =============================================
echo.
echo [6/9] 启动 OpenIM 全家桶 (Redis/Kafka/etcd/MinIO/MongoDB/OpenIM)...

:: 创建 Docker 网络（如果不存在）
docker network inspect tongyuan-openim-net >nul 2>&1
if errorlevel 1 (
    docker network create tongyuan-openim-net >nul 2>&1
    echo [OK] 创建网络: tongyuan-openim-net
)

:: 启动所有 OpenIM 相关容器
%DC% -f "%DEPLOY_DIR%\docker-compose-openim.yml" up -d
if errorlevel 1 (
    echo [FAIL] docker-compose 启动失败，查看日志:
    echo   docker-compose -f "%DEPLOY_DIR%\docker-compose-openim.yml" logs
    pause
    exit /b 1
)
echo [OK] docker-compose 启动完成

:: 等待各服务就绪
echo.
echo   等待 etcd...
for /L %%i in (1,1,15) do (
    curl.exe -s --max-time 3 -o nul http://localhost:12379/health 2>nul
    if not errorlevel 1 (
        echo [OK] etcd 就绪
        goto :etcd_ok
    )
    timeout /t 2 /nobreak >nul
)
:etcd_ok

echo   等待 Kafka...
for /L %%i in (1,1,20) do (
    curl.exe -s --max-time 3 -o nul http://localhost:19092/ 2>nul
    if not errorlevel 1 (
        echo [OK] Kafka 就绪
        goto :kafka_ok
    )
    timeout /t 2 /nobreak >nul
)
:kafka_ok

echo   等待 MongoDB...
for /L %%i in (1,1,15) do (
    curl.exe -s --max-time 3 -o nul http://localhost:27017/ 2>nul
    if not errorlevel 1 (
        echo [OK] MongoDB 就绪
        goto :mongo_ok
    )
    timeout /t 2 /nobreak >nul
)
:mongo_ok

echo   等待 MinIO...
for /L %%i in (1,1,15) do (
    curl.exe -s --max-time 3 -o nul http://localhost:10005/minio/health/live 2>nul
    if not errorlevel 1 (
        echo [OK] MinIO 就绪
        goto :minio_ok
    )
    timeout /t 2 /nobreak >nul
)
:minio_ok

echo   等待 OpenIM Server...
for /L %%i in (1,1,20) do (
    curl.exe -s --max-time 3 -o nul http://localhost:10002/ 2>nul
    if not errorlevel 1 (
        echo [OK] OpenIM Server 就绪
        goto :openim_ok
    )
    timeout /t 3 /nobreak >nul
)
:openim_ok

:: =============================================
:: Step 7: 验证 Docker 容器状态
:: =============================================
echo.
echo [7/9] Docker 容器状态:
docker ps --filter "name=tongyangyuan" --filter "name=tongyuan" --format "  %%names%% : %%status%%"

:: =============================================
:: Step 8: 配置并启动 Spring Boot 后端
:: =============================================
echo.
echo [8/9] 启动 Spring Boot 后端...

:: 设置 JAVA_HOME
if not defined JAVA_HOME (
    if exist "D:\Android-studio\jbr\bin\java.exe" set "JAVA_HOME=D:\Android-studio\jbr"
    if exist "D:\Android Studio\jbr\bin\java.exe" set "JAVA_HOME=D:\Android Studio\jbr"
)
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"
echo   JAVA_HOME: !JAVA_HOME!

:: 杀死旧的后端进程
for /f "tokens=*" %%a in ('powershell -NoProfile -Command "try { (Get-NetTCPConnection -LocalPort 8080 -ErrorAction Stop).OwningProcess -join ',' } catch { }" 2^>nul') do (
    for %%p in (%%a) do (
        echo   停止旧后端 (PID: %%p)
        taskkill /F /PID %%p >nul 2>&1
    )
)
timeout /t 2 /nobreak >nul

:: 更新 application.properties 中的 MySQL 端口（如果用了 Docker MySQL）
if "!MYSQL_PORT!"=="3307" (
    echo   更新 MySQL 端口为 3307...
    powershell -NoProfile -Command "$f='%SERVER_DIR%\src\main\resources\application.properties'; $c=Get-Content $f -Raw; $c -replace 'localhost:3306','localhost:3307' | Set-Content $f -NoNewline -Encoding UTF8"
)

:: 启动后端
start "TongYangYuan-Backend" cmd /k "title TongYangYuan-Backend && cd /d "%SERVER_DIR%" && mvn spring-boot:run"
echo [INFO] 后端已在新窗口启动（首次启动需编译 30-90 秒）
echo   请查看 "TongYangYuan-Backend" 窗口等待启动完成

:: =============================================
:: Step 9: 启动前端服务
:: =============================================
echo.
echo [9/9] 启动前端服务...

:: 清理旧端口进程
for /f "tokens=*" %%a in ('powershell -NoProfile -Command "try { (Get-NetTCPConnection -LocalPort 5500 -ErrorAction Stop).OwningProcess -join ',' } catch { }" 2^>nul') do (
    for %%p in (%%a) do taskkill /F /PID %%p >nul 2>&1
)
start "TongYangYuan-Consultant" cmd /k "title TongYangYuan-Consultant && cd /d "%CONSULTANT_DIR%" && python -m http.server 5500"
echo [OK] 咨询师前端: http://localhost:5500

for /f "tokens=*" %%a in ('powershell -NoProfile -Command "try { (Get-NetTCPConnection -LocalPort 8002 -ErrorAction Stop).OwningProcess -join ',' } catch { }" 2^>nul') do (
    for %%p in (%%a) do taskkill /F /PID %%p >nul 2>&1
)
if exist "%WEB_DIR%\package.json" (
    if exist "%WEB_DIR%\node_modules" (
        start "TongYangYuan-Admin" cmd /k "title TongYangYuan-Admin && cd /d "%WEB_DIR%" && npm run dev"
        echo [OK] 管理后台: http://localhost:8002
    ) else (
        echo [SKIP] Admin Web 未安装 (npm install 未运行)
    )
)

:: =============================================
:: 完成总结
:: =============================================
echo.
echo =============================================
echo   所有服务已启动！
echo =============================================
echo.
echo 端口映射：
echo   后端 API    - http://localhost:8080
echo   STOMP WS   - ws://localhost:8080/api/stomp
echo   OpenIM API  - http://localhost:10002
echo   OpenIM WS  - ws://localhost:10001
echo   LiveKit     - ws://localhost:7880
echo   Redis      - localhost:6379
echo   MongoDB    - localhost:27017
echo   etcd       - localhost:12379
echo   MinIO API  - http://localhost:10005
echo   MinIO Console - http://localhost:10006
echo   Kafka      - localhost:19092
echo   MySQL      - localhost:%MYSQL_PORT%  ^(Windows MySQL^)
echo.
echo   咨询师前端  - http://localhost:5500
echo   管理后台   - http://localhost:8002
echo.
echo =============================================
echo 提示：等待 "TongYangYuan-Backend" 窗口出现
echo       "Started TongYangYuanApplication" 后表示后端就绪
echo =============================================
pause >nul
start http://localhost:5500
