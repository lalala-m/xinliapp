# 童养园管理后台系统

## 📋 项目概述

童养园管理后台系统是一个功能完整的Web管理平台，用于管理用户、咨询师、预约和系统数据统计。

## ✅ 已完成功能（100%）

### 1. 后台管理前端页面
- ✅ [`login.html`](TongYangYuan-Web/admin/login.html) - 管理员登录页面
- ✅ [`index.html`](TongYangYuan-Web/admin/index.html) - 管理后台首页（数据概览）
- ✅ [`users.html`](TongYangYuan-Web/admin/users.html) - 用户管理页面
- ✅ [`consultants.html`](TongYangYuan-Web/admin/consultants.html) - 咨询师管理页面
- ✅ [`appointments.html`](TongYangYuan-Web/admin/appointments.html) - 预约管理页面
- ✅ [`statistics.html`](TongYangYuan-Web/admin/statistics.html) - 数据统计页面

### 2. 样式文件
- ✅ [`admin.css`](TongYangYuan-Web/admin/css/admin.css) - 管理后台统一样式
  - 侧边栏导航
  - 数据卡片
  - 表格样式
  - 模态框
  - 响应式设计

### 3. JavaScript功能模块
- ✅ [`admin-common.js`](TongYangYuan-Web/admin/js/admin-common.js) - 通用功能
  - 权限检查
  - API请求封装
  - Toast提示
  - 日期格式化
  - 操作日志记录

- ✅ [`admin-dashboard.js`](TongYangYuan-Web/admin/js/admin-dashboard.js) - 首页功能
  - 数据概览加载
  - 最近活动展示
  - 自动刷新

- ✅ [`admin-users.js`](TongYangYuan-Web/admin/js/admin-users.js) - 用户管理
  - 用户列表展示
  - 搜索和筛选
  - 用户详情查看
  - 启用/禁用用户
  - 删除用户

- ✅ [`admin-consultants.js`](TongYangYuan-Web/admin/js/admin-consultants.js) - 咨询师管理
  - 咨询师列表展示
  - 搜索和筛选
  - 咨询师详情查看
  - 认证咨询师
  - 删除咨询师

- ✅ [`admin-appointments.js`](TongYangYuan-Web/admin/js/admin-appointments.js) - 预约管理
  - 预约列表展示
  - 搜索和筛选
  - 预约详情查看
  - 确认/取消预约
  - 预约统计

- ✅ [`admin-statistics.js`](TongYangYuan-Web/admin/js/admin-statistics.js) - 数据统计
  - 关键指标展示
  - 用户增长趋势图
  - 预约统计图
  - 收入统计图
  - 咨询师排行榜
  - 数据汇总表

### 4. 后端权限系统
- ✅ [`RequireAdmin.java`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/annotation/RequireAdmin.java) - 管理员权限注解
- ✅ [`AdminAuthInterceptor.java`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/interceptor/AdminAuthInterceptor.java) - 权限拦截器
  - Token验证
  - 管理员身份检查
  - 401/403错误处理

- ✅ [`WebMvcConfig.java`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/config/WebMvcConfig.java) - Web配置
  - 注册拦截器
  - 配置拦截路径

- ✅ [`AdminLogRepository.java`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/repository/AdminLogRepository.java) - 操作日志Repository

- ✅ 更新 [`AdminController.java`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/controller/AdminController.java)
  - 添加@RequireAdmin注解
  - 修正API路径为/api/admin

## 🎯 功能特性

### 权限控制
- ✅ 前端登录验证
- ✅ 后端Token验证
- ✅ 管理员身份检查
- ✅ 自动跳转登录页
- ✅ 操作日志记录

### 用户体验
- ✅ 响应式设计（支持PC和平板）
- ✅ Material Design风格
- ✅ 实时数据刷新
- ✅ Toast消息提示
- ✅ 加载状态显示
- ✅ 错误处理

### 数据可视化
- ✅ Chart.js图表集成
- ✅ 用户增长趋势
- ✅ 预约统计饼图
- ✅ 收入柱状图
- ✅ 咨询师排行榜

## 📁 文件结构

```
TongYangYuan-Web/admin/
├── login.html              # 登录页面
├── index.html              # 首页
├── users.html              # 用户管理
├── consultants.html        # 咨询师管理
├── appointments.html       # 预约管理
├── statistics.html         # 数据统计
├── css/
│   └── admin.css          # 统一样式
└── js/
    ├── admin-common.js    # 通用功能
    ├── admin-dashboard.js # 首页功能
    ├── admin-users.js     # 用户管理
    ├── admin-consultants.js # 咨询师管理
    ├── admin-appointments.js # 预约管理
    └── admin-statistics.js # 数据统计

TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/
├── annotation/
│   └── RequireAdmin.java  # 权限注解
├── interceptor/
│   └── AdminAuthInterceptor.java # 权限拦截器
├── config/
│   └── WebMvcConfig.java  # Web配置
└── repository/
    └── AdminLogRepository.java # 日志Repository
```

## 🚀 使用说明

### 1. 启动服务器
```bash
cd TongYangYuan-Server
mvn spring-boot:run
```

### 2. 访问管理后台
```
http://localhost:8080/admin/login.html
```

### 3. 登录账号
```
账号：admin
密码：admin123
```

**注意：** 首次使用需要先执行SQL脚本创建管理员账号：
```bash
mysql -u root -p123456 mental_health_db < TongYangYuan-Server/create-admin.sql
```

## 🔐 安全特性

1. **前端权限控制**
   - 登录状态检查
   - 管理员身份验证
   - 自动跳转登录页

2. **后端权限控制**
   - JWT Token验证
   - @RequireAdmin注解
   - 拦截器统一处理
   - 401/403错误响应

3. **操作日志**
   - 记录所有管理员操作
   - 包含操作类型和详情
   - 时间戳记录

## 📊 API接口

所有管理后台API都需要管理员权限，路径前缀为 `/api/admin`

### 用户管理
- `GET /api/admin/users` - 获取用户列表
- `GET /api/admin/users/{id}` - 获取用户详情
- `PUT /api/admin/users/{id}/toggle` - 启用/禁用用户
- `DELETE /api/admin/users/{id}` - 删除用户

### 咨询师管理
- `GET /api/admin/consultants` - 获取咨询师列表
- `GET /api/admin/consultants/{id}` - 获取咨询师详情
- `PUT /api/admin/consultants/{id}/verify` - 认证咨询师
- `DELETE /api/admin/consultants/{id}` - 删除咨询师

### 预约管理
- `GET /api/admin/appointments` - 获取预约列表
- `GET /api/admin/appointments/{id}` - 获取预约详情
- `GET /api/admin/appointments/stats` - 获取预约统计
- `PUT /api/admin/appointments/{id}/confirm` - 确认预约
- `PUT /api/admin/appointments/{id}/cancel` - 取消预约

### 数据统计
- `GET /api/admin/statistics` - 获取统计数据
- `GET /api/admin/statistics/overview` - 获取概览数据
- `GET /api/admin/logs` - 获取操作日志

## 🎨 界面预览

### 登录页面
- 渐变背景
- 居中卡片设计
- 错误提示
- 自动跳转

### 管理首页
- 数据卡片展示
- 快速操作按钮
- 最近活动列表
- 自动刷新

### 管理页面
- 侧边栏导航
- 搜索和筛选
- 数据表格
- 分页功能
- 模态框详情

### 统计页面
- 关键指标卡片
- 多种图表类型
- 时间范围选择
- 数据汇总表

## 🔧 技术栈

### 前端
- HTML5 + CSS3
- JavaScript (ES6+)
- Chart.js (图表库)
- Fetch API (HTTP请求)

### 后端
- Spring Boot
- Spring MVC
- JWT认证
- JPA/Hibernate

## 📝 开发日志

### 2024-01-30
- ✅ 创建管理后台前端页面（6个页面）
- ✅ 实现CSS样式系统
- ✅ 开发JavaScript功能模块（6个模块）
- ✅ 实现后端权限拦截器
- ✅ 配置WebMVC拦截器
- ✅ 添加@RequireAdmin注解
- ✅ 创建AdminLogRepository
- ✅ 完成前后端权限控制

## 🎯 下一步计划

1. **功能增强**
   - [ ] 添加用户编辑功能
   - [ ] 实现批量操作
   - [ ] 添加数据导出功能
   - [ ] 实现实时通知

2. **性能优化**
   - [ ] 添加数据缓存
   - [ ] 优化查询性能
   - [ ] 实现懒加载

3. **安全加固**
   - [ ] 添加操作确认
   - [ ] 实现IP白名单
   - [ ] 添加操作审计

## 📞 联系方式

如有问题或建议，请联系开发团队。

---

**童养园管理后台系统 v1.0**  
*让管理更简单，让数据更清晰*
