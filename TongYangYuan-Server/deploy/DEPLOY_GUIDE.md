# 童康源生产环境部署指南

## 服务器信息
- **IP**: 139.196.5.153
- **端口**: 8080 (API), 80 (Web)
- **部署方式**: Docker Compose

## 一、上传到服务器

```bash
# 在本地执行（Windows PowerShell）
scp -r deploy/* root@139.196.5.153:/data/tongyangyuan/

# 或者使用压缩包上传
cd deploy && tar czvf ../tongyangyuan-deploy.tar.gz .
scp ../tongyangyuan-deploy.tar.gz root@139.196.5.153:/data/
ssh root@139.196.5.153 "cd /data && mkdir -p tongyangyuan && tar xzvf tongyangyuan-deploy.tar.gz -C tongyangyuan"
```

## 二、服务器端部署步骤

### 1. 连接服务器
```bash
ssh root@139.196.5.153
cd /data/tongyangyuan
```

### 2. 启动服务
```bash
# 启动所有服务（MySQL、Redis、Backend、Nginx）
docker-compose up -d

# 查看日志
docker-compose logs -f backend

# 查看状态
docker-compose ps
```

### 3. 数据库初始化（首次部署）
```bash
# 等待MySQL启动完成
docker-compose exec mysql mysql -uroot -pTongYuan@2026 mental_health_db < database/init.sql
```

### 4. 检查服务
```bash
# 测试后端API
curl http://139.196.5.153:8080/api/health

# 测试Web访问
curl http://139.196.5.153/admin/login.html
```

## 三、Android App 配置

App已配置为生产模式（ENV_MODE=0），会自动连接 `http://139.196.5.153:8080/api`。

需要重新构建并发布APK：
```bash
# 在Android Studio中执行
./gradlew assembleRelease
```

## 四、更新部署

### 更新后端
```bash
# 本地构建后上传
scp target/mental-health-server-1.0.0.jar root@139.196.5.153:/data/tongyangyuan/backend/

# 服务器端重启
ssh root@139.196.5.153 "cd /data/tongyangyuan && docker-compose restart backend"
```

### 更新Web前端
```bash
# 上传新的前端文件
scp -r TongYangYuan-Web/* root@139.196.5.153:/data/tongyangyuan/web/html/

# 或者重新构建Nginx镜像
ssh root@139.196.5.153 "cd /data/tongyangyuan && docker-compose build web && docker-compose up -d web"
```

## 五、常见问题

### 1. 图片上传后无法访问
检查 uploads 目录权限：
```bash
docker-compose exec backend ls -la /app/uploads/
```

### 2. 数据库连接失败
检查MySQL容器状态：
```bash
docker-compose logs mysql
```

### 3. CORS跨域问题
修改 `application-prod.properties` 中的 `cors.allowed-origins`，添加新的域名。

## 六、服务架构

```
Nginx (80/443)
  ├── /api/* → Spring Boot (8080)
  ├── /uploads/* → 静态文件
  └── /* → Web前端

MySQL (3306) ← 数据持久化
Redis (6379) ← 缓存/会话
```
