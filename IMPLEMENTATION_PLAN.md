# 童养园系统完善计划

## 当前状态分析

### 已完成
- ✅ 基础通信功能（WebSocket聊天、WebRTC视频）
- ✅ UI优化（动画效果、支付页面）
- ✅ 数据库配置（mental_health_db）
- ✅ 基础账户系统（登录功能）

### 待完成
- ❌ 用户注册功能
- ❌ 移除硬编码数据
- ❌ 后台管理系统
- ❌ 管理员权限系统

## 实施计划

### 阶段一：用户注册功能（优先级：高）

#### 1.1 后端API
**文件**: `TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/controller/AuthController.java`

需要添加的接口：
```java
@PostMapping("/register/parent")
public ApiResponse<User> registerParent(@RequestBody RegisterRequest request)

@PostMapping("/register/consultant")
public ApiResponse<User> registerConsultant(@RequestBody RegisterRequest request)
```

**RegisterRequest DTO**:
- phone (手机号)
- password (密码)
- nickname (昵称)
- verificationCode (验证码，可选)

#### 1.2 前端页面

**手机端注册页面**:
- 文件: `TongYangYuan/app/src/main/assets/register.html`
- 功能: 家长注册表单
- 字段: 手机号、密码、确认密码、昵称

**Web端注册页面**:
- 文件: `TongYangYuan-Web/register.html`
- 功能: 咨询师注册表单
- 字段: 手机号、密码、姓名、职称、专长、简介

### 阶段二：移除硬编码数据（优先级：高）

#### 2.1 需要修改的文件

**移动端**:
1. `TongYangYuan/app/src/main/java/com/example/tongyangyuan/consult/ConsultantRepository.java`
   - 移除 `getDefaultConsultants()` 中的硬编码数据
   - 改为从服务器API获取

2. `TongYangYuan/app/src/main/assets/js/consultants_fallback.js`
   - 移除或标记为仅用于离线模式

**Web端**:
1. `TongYangYuan-Web/js/mock-data.js`
   - 移除所有mock数据
   - 或标记为开发模式专用

#### 2.2 API集成

需要确保所有页面都通过以下API获取数据：
- `GET /consultants` - 获取所有咨询师
- `GET /consultants/{id}` - 获取咨询师详情
- `GET /users/{id}` - 获取用户信息
- `GET /appointments/consultant/{id}` - 获取咨询师的预约
- `GET /appointments/parent/{id}` - 获取家长的预约

### 阶段三：后台管理系统（优先级：中）

#### 3.1 管理员账号

**数据库添加**:
```sql
INSERT INTO users (id, phone, password, user_type, nickname, status) VALUES
(999, 'admin', '$2a$10$...', 'ADMIN', '系统管理员', 'ACTIVE');
```

**User实体扩展**:
- 添加 `ADMIN` 用户类型
- 添加权限字段

#### 3.2 后台管理页面结构

参考 `TongYangYuan-Server/backend` 和 `TongYangYuan-Server/web`

**页面列表**:
1. `admin/index.html` - 管理后台首页
2. `admin/users.html` - 用户管理
3. `admin/consultants.html` - 咨询师管理
4. `admin/appointments.html` - 预约管理
5. `admin/messages.html` - 消息管理
6. `admin/statistics.html` - 数据统计

#### 3.3 后台API

**文件**: `TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/controller/AdminController.java`

需要的接口：
```java
// 用户管理
GET /admin/users - 获取所有用户
PUT /admin/users/{id} - 更新用户信息
DELETE /admin/users/{id} - 删除用户
PUT /admin/users/{id}/status - 更新用户状态

// 咨询师管理
GET /admin/consultants - 获取所有咨询师
PUT /admin/consultants/{id} - 更新咨询师信息
POST /admin/consultants/{id}/approve - 审核通过
POST /admin/consultants/{id}/reject - 审核拒绝

// 数据统计
GET /admin/statistics/overview - 总览数据
GET /admin/statistics/users - 用户统计
GET /admin/statistics/appointments - 预约统计
GET /admin/statistics/messages - 消息统计
```

### 阶段四：权限系统（优先级：中）

#### 4.1 权限拦截器

**文件**: `TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/config/AdminAuthInterceptor.java`

功能：
- 检查JWT token
- 验证用户类型是否为ADMIN
- 拦截所有 `/admin/**` 请求

#### 4.2 前端权限控制

**文件**: `TongYangYuan-Web/js/admin-auth.js`

功能：
- 检查登录状态
- 验证管理员权限
- 未授权时跳转到登录页

## 实施优先级

### 第一优先级（立即实施）
1. 添加用户注册功能
2. 移除硬编码的咨询师数据

### 第二优先级（本周完成）
3. 创建基础后台管理系统
4. 添加管理员账号

### 第三优先级（下周完成）
5. 完善权限系统
6. 添加数据统计功能

## 技术栈

### 后端
- Spring Boot 2.x
- Spring Security（权限控制）
- JWT（身份验证）
- MySQL（数据存储）

### 前端
- HTML5 + CSS3 + JavaScript
- Bootstrap 5（管理后台UI）
- Chart.js（数据可视化）
- Axios（HTTP请求）

## 数据库变更

### 新增表

**admin_logs** - 管理员操作日志
```sql
CREATE TABLE admin_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 修改表

**users** - 添加字段
```sql
ALTER TABLE users ADD COLUMN is_approved BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN approved_at TIMESTAMP NULL;
ALTER TABLE users ADD COLUMN approved_by BIGINT NULL;
```

## 测试计划

### 功能测试
1. 注册功能测试（家长、咨询师）
2. 数据获取测试（确保从数据库读取）
3. 管理后台测试（CRUD操作）
4. 权限测试（非管理员无法访问）

### 性能测试
1. 并发注册测试
2. 大量数据加载测试
3. 管理后台响应速度测试

## 风险评估

### 高风险
- 数据迁移可能导致现有功能异常
- 权限系统配置错误可能导致安全问题

### 中风险
- 注册功能可能被恶意利用
- 管理后台性能问题

### 低风险
- UI兼容性问题
- 小bug修复

## 建议

由于这是一个大型重构项目，建议：

1. **分阶段实施**：不要一次性完成所有功能
2. **充分测试**：每个阶段完成后进行测试
3. **备份数据**：在修改前备份数据库
4. **版本控制**：使用Git管理代码变更
5. **文档更新**：及时更新技术文档

## 预计工作量

- 注册功能：2-3天
- 移除硬编码：1-2天
- 后台管理系统：5-7天
- 权限系统：2-3天
- 测试和优化：3-5天

**总计**：约15-20个工作日

---
创建时间：2026-01-30
