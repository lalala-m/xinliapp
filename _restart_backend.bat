@echo off
chcp 65001 >nul
title 童养缘后端服务
echo [1/3] 设置 JAVA_HOME...
set "JAVA_HOME=D:\Android-studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo JAVA_HOME=%JAVA_HOME%
java -version

echo.
echo [2/3] 编译项目...
cd /d D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Server
call mvn clean package -DskipTests -q
if errorlevel 1 (
    echo 编译失败!
    pause
    exit /b 1
)
echo 编译成功!

echo.
echo [3/3] 启动 Spring Boot...
java -jar target\mental-health-server-1.0.0.jar
pause
