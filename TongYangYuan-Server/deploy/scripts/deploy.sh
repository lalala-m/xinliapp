#!/bin/bash
# TongYangYuan 一键部署脚本

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$(dirname "$SCRIPT_DIR")"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo_step() {
    echo -e "${GREEN}[STEP]${NC} $1"
}

echo_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

echo_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 Docker
check_docker() {
    echo_step "检查 Docker 环境..."
    if ! command -v docker &> /dev/null; then
        echo_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    if ! command -v docker-compose &> /dev/null; then
        echo_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
    echo "  Docker 版本: $(docker --version)"
    echo "  Docker Compose 版本: $(docker-compose --version)"
}

# 创建目录结构
create_dirs() {
    echo_step "创建目录结构..."
    cd "$DEPLOY_DIR"
    mkdir -p mysql/{data,conf} redis/data livekit openim/config backend web web/dist logs uploads
    chmod +x scripts/*.sh
    echo "  目录创建完成"
}

# 检查文件
check_files() {
    echo_step "检查必要文件..."
    
    # 检查后端 JAR
    if [ ! -f "backend/mental-health-server-1.0.0.jar" ]; then
        echo_warn "未找到后端 JAR 文件: backend/mental-health-server-1.0.0.jar"
        echo_warn "请将 JAR 文件放入 deploy/backend/ 目录"
        # 不退出，继续执行
    else
        echo "  [OK] 后端 JAR 文件存在"
    fi
    
    # 检查前端构建
    if [ ! -d "web/dist" ]; then
        echo_warn "未找到前端构建文件: web/dist/"
        echo_warn "请将前端构建文件放入 deploy/web/dist/ 目录"
    else
        echo "  [OK] 前端构建文件存在"
    fi
}

# 停止现有服务
stop_services() {
    echo_step "停止现有服务..."
    cd "$DEPLOY_DIR"
    docker-compose down 2>/dev/null || true
    echo "  服务已停止"
}

# 启动服务
start_services() {
    echo_step "启动 Docker 服务..."
    cd "$DEPLOY_DIR"
    docker-compose up -d
    echo "  服务启动中，请等待..."
}

# 等待服务就绪
wait_services() {
    echo_step "等待服务就绪..."
    echo "  等待 MySQL (约30秒)..."
    sleep 15
    
    echo "  等待 Redis..."
    for i in {1..10}; do
        if docker exec tongyangyuan-redis redis-cli -a Redis@2026 ping > /dev/null 2>&1; then
            echo "  Redis 已就绪"
            break
        fi
        sleep 3
    done
    
    echo "  等待后端启动 (约60秒)..."
    for i in {1..20}; do
        if curl -sf http://localhost:8080/api/consultants > /dev/null 2>&1; then
            echo "  后端已就绪"
            break
        fi
        sleep 3
    done
}

# 显示状态
show_status() {
    echo ""
    echo_step "服务状态:"
    docker-compose ps
    echo ""
    echo_step "端口映射:"
    echo "  MySQL:    3306"
    echo "  Redis:    6379"
    echo "  后端 API: 8080"
    echo "  LiveKit:  7880/7881/7882"
    echo "  OpenIM:   10001/10002"
    echo "  Nginx:    80"
}

# 主函数
main() {
    echo "=========================================="
    echo "  TongYangYuan 一键部署脚本"
    echo "=========================================="
    echo ""
    
    check_docker
    create_dirs
    check_files
    stop_services
    start_services
    wait_services
    show_status
    
    echo ""
    echo_step "部署完成!"
    echo "  请运行: bash scripts/health_check.sh 进行详细检查"
    echo "  查看日志: bash scripts/deploy.sh logs"
}

case "$1" in
    start)
        cd "$DEPLOY_DIR"
        docker-compose start
        ;;
    stop)
        cd "$DEPLOY_DIR"
        docker-compose stop
        ;;
    restart)
        cd "$DEPLOY_DIR"
        docker-compose restart
        ;;
    logs)
        cd "$DEPLOY_DIR"
        shift
        if [ -n "$1" ]; then
            docker-compose logs -f "$1"
        else
            docker-compose logs -f
        fi
        ;;
    status)
        cd "$DEPLOY_DIR"
        docker-compose ps
        ;;
    health)
        bash "$SCRIPT_DIR/health_check.sh"
        ;;
    rebuild)
        echo_step "重新构建镜像..."
        cd "$DEPLOY_DIR"
        docker-compose build --no-cache backend web
        docker-compose up -d
        ;;
    *)
        main
        ;;
esac
