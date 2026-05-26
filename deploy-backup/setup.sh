#!/bin/bash
# 童康源服务器快速部署脚本
# 重置服务器后执行: bash /data/setup.sh

set -e

echo "========== 童康源服务器部署 =========="

# 1. 更新系统
apt update && apt upgrade -y

# 2. 安装必要软件
apt install -y mysql-server redis-server nginx openjdk-21-jdk curl

# 3. 配置MySQL
mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '123456';"
mysql -e "CREATE DATABASE IF NOT EXISTS mental_health_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -e "GRANT ALL PRIVILEGES ON mental_health_db.* TO 'root'@'%';"
mysql -e "FLUSH PRIVILEGES;"

# 4. 配置Redis
sed -i 's/^# requirepass/requirepass/' /etc/redis/redis.conf || true
echo "requirepass Redis@2026" >> /etc/redis/redis.conf
systemctl restart redis-server

# 5. 创建部署目录
mkdir -p /data/tongyangyuan/{backend,web/html,android,uploads,logs}

# 6. 配置Nginx
cat > /etc/nginx/sites-available/tongyangyuan << 'EOF'
server {
    listen 80;
    server_name 139.196.5.153;
    root /data/tongyangyuan/web/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /uploads/ {
        alias /data/tongyangyuan/uploads/;
        expires 30d;
    }

    location /download/ {
        alias /data/tongyangyuan/android/;
        autoindex on;
    }
}
EOF

ln -sf /etc/nginx/sites-available/tongyangyuan /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl restart nginx

# 7. 创建启动脚本
cat > /data/tongyangyuan/start.sh << 'EOF'
#!/bin/bash
cd /data/tongyangyuan/backend
nohup java -Xms512m -Xmx1024m -XX:+UseG1GC -jar mental-health-server-1.0.0.jar \
  --spring.profiles.active=prod \
  > /data/tongyangyuan/logs/backend.log 2>&1 &
echo $! > /data/tongyangyuan/backend.pid
echo "Backend started"
EOF
chmod +x /data/tongyangyuan/start.sh

cat > /data/tongyangyuan/stop.sh << 'EOF'
#!/bin/bash
if [ -f /data/tongyangyuan/backend.pid ]; then
    kill $(cat /data/tongyangyuan/backend.pid) 2>/dev/null
fi
killall -9 java 2>/dev/null
echo "Backend stopped"
EOF
chmod +x /data/tongyangyuan/stop.sh

echo ""
echo "========== 部署完成 =========="
echo "请上传以下文件到对应目录："
echo "  - JAR文件 → /data/tongyangyuan/backend/mental-health-server-1.0.0.jar"
echo "  - Web文件 → /data/tongyangyuan/web/html/"
echo "  - APK文件 → /data/tongyangyuan/android/tongyangyuan-release.apk"
echo "  - SQL数据 → 导入到 mental_health_db"
echo ""
echo "启动命令: bash /data/tongyangyuan/start.sh"
echo "停止命令: bash /data/tongyangyuan/stop.sh"
