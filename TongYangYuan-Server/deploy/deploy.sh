#!/bin/bash
# 童康源生产环境部署脚本
# 在服务器上执行: bash deploy.sh

set -e

SERVER_IP="139.196.5.153"
DEPLOY_DIR="/data/tongyangyuan"

echo "========================================"
echo "  童康源生产环境部署脚本"
echo "  服务器: $SERVER_IP"
echo "========================================"

# 检查Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker未安装，请先安装Docker"
    echo "   安装命令: curl -fsSL https://get.docker.com | sh"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose未安装，请先安装"
    echo "   安装命令: pip install docker-compose 或下载二进制"
    exit 1
fi

echo "✅ Docker环境检查通过"

# 创建部署目录
mkdir -p $DEPLOY_DIR
cd $DEPLOY_DIR

echo "📁 部署目录: $DEPLOY_DIR"

# 检查必要文件
if [ ! -f "backend/mental-health-server-1.0.0.jar" ]; then
    echo "❌ 缺少后端JAR文件: backend/mental-health-server-1.0.0.jar"
    echo "   请先将部署文件上传到 $DEPLOY_DIR"
    exit 1
fi

if [ ! -f "docker-compose.yml" ]; then
    echo "❌ 缺少 docker-compose.yml"
    exit 1
fi

echo "✅ 部署文件检查通过"

# 创建必要目录
mkdir -p uploads/images uploads/videos uploads/audios logs mysql/data redis/data

# 设置权限
chmod -R 755 uploads logs

echo "📂 目录结构创建完成"

# 停止旧服务（如果存在）
if docker-compose ps &> /dev/null; then
    echo "🛑 停止旧服务..."
    docker-compose down || true
fi

# 启动服务
echo "🚀 启动服务..."
docker-compose up -d

echo ""
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo ""
echo "📊 服务状态:"
docker-compose ps

echo ""
echo "🔍 健康检查..."

# 检查后端API
if curl -s http://localhost:8080/api/health &> /dev/null; then
    echo "✅ 后端API正常: http://$SERVER_IP:8080/api"
else
    echo "⚠️ 后端API可能未就绪，查看日志:"
    docker-compose logs --tail=20 backend
fi

# 检查Web
if curl -s -o /dev/null -w "%{http_code}" http://localhost/admin/login.html | grep -q "200\|301\|302"; then
    echo "✅ Web前端正常: http://$SERVER_IP/admin/login.html"
else
    echo "⚠️ Web前端可能未就绪"
fi

echo ""
echo "========================================"
echo "  部署完成!"
echo "========================================"
echo ""
echo "📱 Android App API地址:"
echo "   http://$SERVER_IP:8080/api"
echo ""
echo "🌐 管理后台地址:"
echo "   http://$SERVER_IP/admin/login.html"
echo ""
echo "👨‍⚕️ 咨询师端地址:"
echo "   http://$SERVER_IP/login.html"
echo ""
echo "📊 查看实时日志:"
echo "   docker-compose logs -f backend"
echo ""
echo "🔄 重启服务:"
echo "   docker-compose restart"
echo ""
echo "🛑 停止服务:"
echo "   docker-compose down"
echo ""
