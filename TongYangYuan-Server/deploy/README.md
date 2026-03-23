# TongYangYuan 视频咨询功能部署指南

## 一、项目结构

```
deploy/
├── docker-compose.yml          # Docker 编排配置
├── .env.example               # 环境变量示例
├── backend/
│   ├── Dockerfile             # 后端镜像构建
│   └── mental-health-server-1.0.0.jar  # JAR包(需自行构建)
├── web/
│   ├── Dockerfile             # 前端镜像构建
│   ├── nginx.conf             # Nginx 配置
│   └── dist/                  # 前端构建文件(需自行构建)
├── livekit/
│   └── config.yaml            # LiveKit 配置
├── openim/
│   └── config.yaml            # OpenIM 配置
└── scripts/
    ├── deploy.sh              # 部署脚本
    ├── health_check.sh        # 健康检查脚本
    └── init_video_tables.sql  # 数据库初始化脚本
```

## 二、部署前准备

### 2.1 服务器要求

- **操作系统**: Ubuntu 20.04+ / Debian 11+
- **CPU**: 2核+
- **内存**: 4GB+
- **磁盘**: 20GB+
- **网络**: 公网IP 或 域名解析

### 2.2 安装 Docker

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com | sh

# 启动 Docker
sudo systemctl start docker
sudo systemctl enable docker

# 添加当前用户到 docker 组
sudo usermod -aG docker $USER
newgrp docker
```

### 2.3 安装 Docker Compose

```bash
sudo curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

## 三、部署步骤

### 3.1 上传文件到服务器

```bash
# 在服务器上创建部署目录
ssh user@your-server-ip
mkdir -p /data/tongyangyuan

# 本地执行(Windows PowerShell 或 CMD)
scp -r deploy/* user@your-server-ip:/data/tongyangyuan/
```

### 3.2 构建后端

**方式一：在本地构建后上传**

```bash
# Windows (在项目目录执行)
cd TongYangYuan-Server
mvn clean package -DskipTests

# 上传 JAR
scp target/mental-health-server-1.0.0.jar user@your-server:/data/tongyangyuan/backend/
```

**方式二：在服务器上构建**

```bash
ssh user@your-server-ip
cd /data/tongyangyuan
git clone https://your-repo/TongYangYuan-Server.git
cd TongYangYuan-Server
mvn clean package -DskipTests
cp target/mental-health-server-1.0.0.jar ../backend/
```

### 3.3 构建前端

```bash
# 在项目目录执行
cd TongYangYuan-Server/web
npm install
npm run build:prod

# 上传到服务器
scp -r dist/* user@your-server:/data/tongyangyuan/web/
```

### 3.4 配置修改

#### 4.1 修改后端配置

编辑 `/data/tongyangyuan/backend/application.properties` 或创建环境变量:

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://mysql:3306/mental_health_db?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=TongYuan@2026

# Redis 配置
spring.data.redis.host=redis
spring.data.redis.port=6379
spring.data.redis.password=Redis@2026

# LiveKit 配置
livekit.url=http://livekit:7880
livekit.api-key=devkey
livekit.api-secret=your-api-key-secret-here

# OpenIM 配置
openim.api-url=http://openim:10002
openim.ws-url=ws://openim:10001
openim.admin-user-id=openIMAdmin
openim.secret=OpenIMAdmin@2026

# CORS 配置
cors.allowed-origins=http://your-server-ip
```

#### 4.2 修改 LiveKit 配置

编辑 `/data/tongyangyuan/livekit/config.yaml`:

```yaml
keys:
  devkey: your-api-key-secret-here  # 替换为安全的密钥
```

#### 4.3 修改 Nginx 配置

编辑 `/data/tongyangyuan/web/nginx.conf`:

```nginx
server_name your-server-ip;  # 替换为你的服务器IP或域名
```

### 3.5 初始化数据库

```bash
# 启动基础服务
cd /data/tongyangyuan
docker-compose up -d mysql redis

# 等待 MySQL 启动
sleep 20

# 初始化视频通话表
docker exec -i tongyangyuan-mysql mysql -uroot -pTongYuan@2026 < scripts/init_video_tables.sql
```

### 3.6 启动所有服务

```bash
cd /data/tongyangyuan

# 设置脚本执行权限
chmod +x scripts/*.sh

# 一键部署
bash scripts/deploy.sh

# 或分步执行
docker-compose up -d
```

### 3.7 验证部署

```bash
# 运行健康检查
bash scripts/health_check.sh

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
```

## 四、端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| 后端 API | 8080 | Spring Boot API |
| LiveKit HTTP | 7880 | 视频服务 HTTP |
| LiveKit WS | 7881 | 视频服务 WebSocket |
| LiveKit UDP | 7882 | 视频媒体流 |
| OpenIM WS | 10001 | IM WebSocket |
| OpenIM HTTP | 10002 | IM HTTP API |
| Nginx | 80 | 前端网站 |

## 五、常用命令

```bash
# 进入部署目录
cd /data/tongyangyuan

# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 查看日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f livekit

# 重新构建镜像
docker-compose build --no-cache backend web
docker-compose up -d

# 健康检查
bash scripts/health_check.sh
```

## 六、常见问题

### 6.1 视频通话无法连接

1. 检查 LiveKit 是否正常运行: `curl http://localhost:7880/health`
2. 检查防火墙是否开放 7880-7882 端口
3. 检查 LiveKit 配置中的 `use_external_ip` 是否为 `true`

### 6.2 WebSocket 连接失败

1. 检查 Nginx 配置中的 WebSocket 代理设置
2. 检查后端 WebSocket 配置是否正确
3. 查看后端日志: `docker-compose logs -f backend`

### 6.3 数据库连接失败

1. 检查 MySQL 是否启动: `docker ps | grep mysql`
2. 检查网络连通性: `docker exec tongyangyuan-backend ping mysql`
3. 检查密码是否正确

### 6.4 前端无法访问

1. 检查 Nginx 是否启动: `docker ps | grep web`
2. 检查端口是否冲突: `netstat -tlnp | grep 80`
3. 查看 Nginx 日志: `docker-compose logs -f web`

## 七、安全建议

1. **修改默认密码**: 将 `.env.example` 复制为 `.env` 并修改所有密码
2. **配置 SSL**: 生产环境建议使用 HTTPS
3. **防火墙**: 只开放必要端口 (80, 443, 8080)
4. **定期备份**: 定期备份 MySQL 数据和 Redis 数据

## 八、技术支持

如有问题，请检查:
1. Docker 日志: `docker-compose logs`
2. 服务状态: `docker-compose ps`
3. 端口占用: `netstat -tlnp`
