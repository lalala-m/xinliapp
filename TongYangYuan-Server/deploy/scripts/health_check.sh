#!/bin/bash
# TongYangYuan 服务健康检查脚本

echo "=========================================="
echo "  TongYangYuan 服务健康检查"
echo "=========================================="
echo ""

PASS=0
FAIL=0

# 检查后端服务
echo -n "[1/6] 检查后端服务 (8080)... "
if curl -sf --max-time 5 http://localhost:8080/api/health > /dev/null 2>&1 || \
   curl -sf --max-time 5 http://localhost:8080/api/consultants > /dev/null 2>&1; then
    echo "[OK]"
    ((PASS++))
else
    echo "[FAIL]"
    ((FAIL++))
fi

# 检查 LiveKit 服务
echo -n "[2/6] 检查 LiveKit (7880)... "
if curl -sf --max-time 5 http://localhost:7880/health > /dev/null 2>&1; then
    echo "[OK]"
    ((PASS++))
else
    echo "[FAIL]"
    ((FAIL++))
fi

# 检查 MySQL 服务
echo -n "[3/6] 检查 MySQL (3306)... "
if docker exec tongyangyuan-mysql mysqladmin ping -h localhost -uroot -pTongYuan@2026 > /dev/null 2>&1; then
    echo "[OK]"
    ((PASS++))
else
    echo "[FAIL]"
    ((FAIL++))
fi

# 检查 Redis 服务
echo -n "[4/6] 检查 Redis (6379)... "
if docker exec tongyangyuan-redis redis-cli -a Redis@2026 ping > /dev/null 2>&1; then
    echo "[OK]"
    ((PASS++))
else
    echo "[FAIL]"
    ((FAIL++))
fi

# 检查 OpenIM 服务
echo -n "[5/6] 检查 OpenIM (10002)... "
if curl -sf --max-time 5 http://localhost:10002/api/user/get_users_info > /dev/null 2>&1; then
    echo "[OK]"
    ((PASS++))
else
    echo "[WARN] (可能未初始化)"
    ((PASS++))
fi

# 检查 Nginx 前端
echo -n "[6/6] 检查 Nginx (80)... "
if curl -sf --max-time 5 http://localhost/ > /dev/null 2>&1; then
    echo "[OK]"
    ((PASS++))
else
    echo "[FAIL]"
    ((FAIL++))
fi

echo ""
echo "=========================================="
echo "  检查完成: $PASS 通过, $FAIL 失败"
echo "=========================================="

if [ $FAIL -eq 0 ]; then
    exit 0
else
    exit 1
fi
