@echo off
chcp 65001 >nul
title 同阳缘一键启动

echo ============================================
echo    同阳缘心理健康咨询系统 - 一键启动
echo ============================================
echo.

:: 检查MySQL
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul
if %errorlevel% neq 0 (
    echo [ERROR] MySQL 未运行！请先启动MySQL服务
    pause
    exit /b 1
)
echo [OK] MySQL 运行正常

:: 检查后端是否已启动
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul
if %errorlevel%==0 (
    echo [INFO] 后端服务已在运行 (端口8080)
    goto :check_frontend
) else (
    echo [1/3] 启动后端服务...
    start "TongYangYuan-Backend" cmd /k "title TongYangYuan-Backend && cd /d D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Server && mvn spring-boot:run"
    echo [INFO] 后端启动中，首次编译约需1-2分钟，请耐心等待...
)

:check_frontend
:: 检查咨询师端是否已启动
netstat -ano | findstr ":5500" | findstr "LISTENING" >nul
if %errorlevel%==0 (
    echo [INFO] 咨询师端已在运行 (端口5500)
    goto :finish
) else (
    echo [2/3] 启动咨询师端页面...
    start "TongYangYuan-Consultant" cmd /k "title TongYangYuan-Consultant && cd /d D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Web && python -m http.server 5500"
)

:finish
echo.
echo ============================================
echo    所有服务已启动！
echo ============================================
echo.
echo 访问地址：
echo   - 后端API:      http://localhost:8080/api
echo   - 咨询师端:     http://localhost:5500
echo.
echo 测试账号：
echo   - 咨询师: 13800000001 / 123456
echo   - 家  长: 13900000001 / 123456
echo   - 管理员: 13800000000 / admin123
echo.
echo 功能说明：
echo   - 预约功能: 正常可用
echo   - 聊天咨询: 正常可用  
echo   - 视频通话: 暂不启用(需配置LiveKit)
echo.
echo ============================================
echo 按任意键打开浏览器测试...
pause >nul
start http://localhost:5500
