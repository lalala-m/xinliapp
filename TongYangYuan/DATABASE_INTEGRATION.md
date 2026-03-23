# 数据库集成说明

## 概述

应用已从使用本地假数据改为使用MySQL数据库，并将数据缓存到本地Room数据库中。

## 架构

### 1. MySQL数据库配置
- **数据库名**: xin_psychology
- **用户名**: root
- **密码**: 123456
- **主机**: localhost
- **端口**: 3306

配置文件位置: `com.example.tongyangyuan.database.DatabaseConfig`

### 2. 数据流程

```
MySQL数据库 → DataSyncService → Room本地数据库 → ConsultantRepository → UI
```

1. **应用启动时**: `TongYangYuanApp` 自动从MySQL同步数据到本地Room数据库
2. **数据读取**: `ConsultantRepository` 从本地Room数据库读取数据（快速，离线可用）
3. **数据更新**: 可以手动调用 `DataSyncService.syncConsultants()` 来刷新数据

### 3. 核心类说明

#### DatabaseConfig
存储MySQL数据库连接配置信息。

#### MySQLManager
管理MySQL数据库连接，提供异步连接和查询功能。

#### DataSyncService
负责从MySQL同步数据到本地Room数据库。

#### AppDatabase (Room)
本地SQLite数据库，使用Room框架管理。

#### ConsultantRepository
数据仓库类，提供统一的数据访问接口。

## MySQL数据库表结构

### consultants 表

```sql
CREATE TABLE consultants (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    title VARCHAR(200),
    specialty VARCHAR(500),
    rating DECIMAL(3,2),
    served_count VARCHAR(50),
    avatar_color VARCHAR(50),
    intro TEXT,
    reviews TEXT,
    identity_tags TEXT
);
```

**字段说明**:
- `id`: 咨询师ID（主键）
- `name`: 姓名
- `title`: 职位/头衔
- `specialty`: 专业领域（用 / 分隔）
- `rating`: 评分（0-5）
- `served_count`: 服务次数
- `avatar_color`: 头像颜色
- `intro`: 个人介绍
- `reviews`: 用户评价（逗号分隔）
- `identity_tags`: 身份标签（逗号分隔）

### 示例数据

```sql
INSERT INTO consultants (name, title, specialty, rating, served_count, avatar_color, intro, reviews, identity_tags) VALUES
('林姝', '高级心理治疗师·儿童发展方向', '饮食调理 / 情绪陪伴 / 睡眠习惯养成', 4.9, '680', 'green',
 '善于用温柔的叙事疗法陪伴孩子与家长，帮助建立可持续的家庭互动节奏。',
 '孩子愿意主动表达感受,方案贴近日常，家长也能坚持,两周内看到了饮食和睡眠的改善',
 '童康源内部人员,童康源指导师（蓝V认证）,心理健康指导师（黄V认证）');
```

## 使用方法

### 1. 获取所有咨询师（异步）

```java
ConsultantRepository repository = ConsultantRepository.getInstance(context);
repository.getAll(new ConsultantRepository.ConsultantCallback() {
    @Override
    public void onSuccess(List<Consultant> consultants) {
        // 处理咨询师列表
    }

    @Override
    public void onError(Exception e) {
        // 处理错误
    }
});
```

### 2. 获取所有咨询师（同步，用于已缓存数据）

```java
ConsultantRepository repository = ConsultantRepository.getInstance(context);
List<Consultant> consultants = repository.getAllSync();
```

### 3. 按姓名查找咨询师

```java
repository.findByName("林姝", new ConsultantRepository.SingleConsultantCallback() {
    @Override
    public void onSuccess(Consultant consultant) {
        // 处理咨询师信息
    }

    @Override
    public void onError(Exception e) {
        // 处理错误
    }
});
```

### 4. 手动同步数据

```java
DataSyncService syncService = DataSyncService.getInstance(context);
syncService.syncConsultants(new DataSyncService.SyncCallback() {
    @Override
    public void onSuccess(int count) {
        Log.d(TAG, "同步了 " + count + " 条数据");
    }

    @Override
    public void onError(Exception e) {
        Log.e(TAG, "同步失败", e);
    }
});
```

## 注意事项

1. **网络权限**: 已在 `AndroidManifest.xml` 中添加 `INTERNET` 和 `ACCESS_NETWORK_STATE` 权限

2. **数据库连接**: MySQL连接在后台线程执行，不会阻塞UI

3. **本地缓存**: 数据会缓存在本地Room数据库中，即使没有网络也能访问

4. **数据同步**: 应用启动时自动同步一次，也可以手动触发同步

5. **线程安全**: 所有数据库操作都在后台线程执行，回调在主线程

## 依赖库

- **Room**: 2.6.1 - 本地数据库
- **MySQL Connector**: 8.0.33 - MySQL JDBC驱动
- **Gson**: 2.10.1 - JSON序列化

## 配置修改

如需修改数据库连接信息，请编辑 `DatabaseConfig.java`:

```java
public static final String DB_HOST = "localhost";  // 数据库主机
public static final int DB_PORT = 3306;            // 数据库端口
public static final String DB_NAME = "xin_psychology";  // 数据库名
public static final String DB_USER = "root";       // 用户名
public static final String DB_PASSWORD = "123456"; // 密码
```

## 已删除的内容

- ✅ 删除了 `ConsultantRepository` 中的硬编码假数据
- ✅ 所有咨询师数据现在从MySQL数据库读取
- ✅ 数据保存到手机本地Room数据库
