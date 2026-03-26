# 同阳缘心理健康咨询系统 - 本地开发环境配置

## 一、环境概览

| 服务 | 位置 | 连接地址 | 状态 |
|------|------|----------|------|
| MySQL | Windows 本地 | localhost:3306 | ✅ 运行中 |
| Redis | WSL (Ubuntu) | localhost:6379 | ✅ 运行中 |
| Docker | Docker Desktop | - | ✅ 运行中 |
| PostgreSQL | Docker | localhost:5433 | ✅ 运行中（其他项目用） |

---

## 二、MySQL 配置

### 信息
- **路径**: `D:\MySQL\mysql-8.0.44-winx64`
- **版本**: MySQL 8.0.44
- **服务名**: `MySQL80`
- **端口**: 3306
- **密码**: 123456
- **数据库**: `mental_health_db`

### 命令行连接
```powershell
mysql -u root -p123456
mysql -u root -p123456 mental_health_db
```

### 常用命令
```powershell
# 启动服务
net start MySQL80

# 停止服务
net stop MySQL80

# 重启服务
net stop MySQL80 && net start MySQL80
```

---

## 三、Redis 配置

### 信息
- **位置**: WSL (Ubuntu) 内部
- **连接地址**: localhost:6379
- **密码**: Redis@2026

### WSL 内命令
```bash
# 进入 WSL
wsl

# 启动 Redis（如果没运行）
sudo service redis-server start

# 连接 Redis
redis-cli
redis-cli -a Redis@2026

# 测试连接
ping
```

### Windows 侧连接
```powershell
# 如果 Windows 装了 redis-cli
redis-cli -h localhost -p 6379 -a Redis@2026
```

---

## 四、Docker 配置

### 状态
- **版本**: Docker 29.0.1
- **运行模式**: Linux 容器模式

### 镜像源（已配置）
```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn/",
    "https://mirror.baidubce.com/",
    "https://docker.nju.edu.cn/"
  ]
}
```

> ⚠️ 注意: docker.nju.edu.cn 有时返回 403，如遇拉取失败可临时移除

### 常用命令
```powershell
# 查看运行中的容器
docker ps

# 查看所有容器
docker ps -a

# 停止容器
docker stop <容器名>

# 删除容器
docker rm <容器名>

# 查看日志
docker logs <容器名>
```

### 项目 Docker Compose
项目目录: `D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Server\docker-compose.yml`

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: tyy-mysql
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: mental_health_db
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    command: --default-authentication-plugin=mysql_native_password --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    restart: unless-stopped

  redis:
    image: redis:latest
    container_name: tyy-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    restart: unless-stopped

  livekit:
    image: livekit/livekit-server:latest
    container_name: tyy-livekit
    command: --dev --bind 0.0.0.0
    ports:
      - "7880:7880"
      - "7881:7881"
      - "7882:7882/udp"
    restart: unless-stopped

volumes:
  mysql-data:
  redis-data:
```

> 注意: 由于本地已有 MySQL 和 Redis（WSL），这个 compose 文件主要用于 LiveKit

### 启动/停止服务
```powershell
cd D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Server

# 启动 LiveKit（主要用途）
docker compose up -d livekit

# 停止
docker compose down

# 查看日志
docker logs tyy-livekit -f
```

---

## 五、后端配置 (application.properties)

路径: `D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Server\src\main\resources\application.properties`

### 本地开发配置（当前）
```properties
# MySQL - Windows 本地
spring.datasource.url=jdbc:mysql://localhost:3306/mental_health_db
spring.datasource.username=root
spring.datasource.password=123456

# Redis - WSL
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=Redis@2026

# LiveKit - Docker 本地
livekit.url=ws://127.0.0.1:7880
livekit.api-key=devkey
livekit.api-secret=secret

# 服务地址
app.server-url=http://localhost:8080
```

### 部署到云端时配置
```properties
# MySQL - AutoDL
spring.datasource.url=jdbc:mysql://<实例IP>:3306/mental_health_db
spring.datasource.password=<你的密码>

# Redis - AutoDL
spring.data.redis.host=<实例IP>
spring.data.redis.password=<你的密码>

# LiveKit - LiveKit Cloud
livekit.url=wss://your-server.livekit.cloud
livekit.api-key=<你的API Key>
livekit.api-secret=<你的API Secret>

# 服务地址
app.server-url=http://<实例IP>:8080
```

---

## 六、启动顺序

### 本地开发启动
```powershell
# 1. 确保 MySQL 运行
net start MySQL80

# 2. 确保 WSL Redis 运行
wsl -e sudo service redis-server start

# 3. 启动后端 (IntelliJ 或命令行)
cd D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Server
mvn spring-boot:run

# 4. 如果需要 LiveKit
docker compose up -d livekit
```

### 常用检查命令
```powershell
# 检查 MySQL
mysql -u root -p123456 -e "SELECT 1;"

# 检查 Redis (WSL)
wsl -e redis-cli -a Redis@2026 ping

# 检查后端是否启动
curl http://localhost:8080/api/health

# 检查端口占用
netstat -ano | findstr "8080"
netstat -ano | findstr "3306"
netstat -ano | findstr "6379"
netstat -ano | findstr "7880"
```

---

## 七、测试账号

| 账号类型 | 用户名/手机 | 密码 | 用途 |
|---------|------------|------|------|
| 咨询师 | 13800000001 | password123 | 电脑端咨询师登录 |
| 家长 | 13900000001 | password123 | 手机端家长登录 |
| 管理员 | admin | password123 | 后台管理 |

---

## 八、常见问题

### Q1: MySQL 连接失败
```powershell
# 检查服务状态
net start MySQL80

# 检查端口
netstat -ano | findstr "3306"
```

### Q2: Redis 连接失败
```bash
# WSL 内检查
sudo service redis-server start
redis-cli -a Redis@2026 ping
```

### Q3: Docker 拉取镜像失败 (403)
修改 Docker Desktop → Settings → Docker Engine，移除 docker.nju.edu.cn

### Q4: 端口被占用
```powershell
# 查找占用端口的进程
netstat -ano | findstr "8080"

# 结束进程
taskkill /PID <进程ID> /F
```

### Q5: 后端启动失败
检查 MySQL 和 Redis 是否正常运行，确认数据库 mental_health_db 存在

---

## 九、项目结构

```
D:\AllProject\AndroidStudioProjects\TYY\
├── TongYangYuan-Server/          # Java Spring Boot 后端
│   ├── src/main/java/
│   ├── src/main/resources/
│   │   ├── application.properties  # 主配置
│   │   └── data.sql                # 测试数据
│   └── docker-compose.yml
│
├── TongYangYuan-Web/              # Web 前端（管理后台）
│   └── admin/
│
└── TongYangYuan/                  # Android App
    └── app/src/main/assets/
```

---

## 十、相关文档链接

- MySQL: https://dev.mysql.com/doc/
- Redis: https://redis.io/docs/
- LiveKit: https://docs.livekit.io/
- Spring Boot: https://spring.io/projects/spring-boot
