# 童养园心理健康平台 - 账户信息

## 数据库配置

**数据库名称**: `mental_health_db`
**MySQL用户名**: `root`
**MySQL密码**: `123456`
**服务器地址**: `localhost:3306`

## 账户信息

### 咨询师账户（电脑端登录）
- **手机号**: 13800000001
- **密码**: password123
- **用户ID**: 1
- **姓名**: 张医生
- **职称**: 儿童心理咨询师
- **专长**: 儿童焦虑、学习障碍
- **等级**: GOLD（金牌咨询师）

### 家长账户（手机端登录）
- **手机号**: 13900000001
- **密码**: password123
- **用户ID**: 2
- **姓名**: 王女士
- **用户类型**: PARENT（家长）

## 数据存储说明

### 1. 用户数据
所有用户信息存储在 `users` 表中：
- 基本信息：手机号、密码（加密）、昵称、状态
- 用户类型：CONSULTANT（咨询师）或 PARENT（家长）

### 2. 咨询师数据
咨询师详细信息存储在 `consultants` 表中：
- 个人资料：姓名、职称、专长、简介
- 业务数据：评分、服务人数、等级

### 3. 聊天记录备份
聊天记录同时保存在两个地方：

#### 客户端（手机端）
- 位置：Android本地数据库（SQLite）
- 表名：`chat_messages`
- 用途：离线访问、快速加载

#### 服务端（MySQL数据库）
- 位置：`mental_health_db.chat_messages` 表
- 字段：
  - `id` - 消息ID
  - `appointment_id` - 预约ID
  - `sender_user_id` - 发送者用户ID
  - `receiver_user_id` - 接收者用户ID
  - `message_type` - 消息类型（TEXT/IMAGE/VIDEO/AUDIO）
  - `content` - 消息内容
  - `media_url` - 媒体文件URL
  - `is_from_consultant` - 是否来自咨询师
  - `is_read` - 是否已读
  - `created_at` - 创建时间

### 4. 预约数据
预约信息存储在 `appointments` 表中：
- 预约详情：日期、时间段、描述
- 关联信息：咨询师ID、家长ID、孩子信息
- 状态：PENDING/ACCEPTED/IN_PROGRESS/COMPLETED/CANCELLED

### 5. 在线状态
用户在线状态存储在 `user_online_status` 表中：
- 在线状态：是否在线
- Socket ID：WebSocket连接ID
- 最后在线时间

## 数据备份机制

### 聊天消息备份流程

1. **消息发送**
   - 用户发送消息 → WebSocket
   - 服务器接收 → [`ChatWebSocketController.sendMessage()`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/ChatWebSocketController.java:36)

2. **服务器保存**
   - 调用 [`ChatMessageService.saveMessage()`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/service/ChatMessageService.java)
   - 保存到MySQL数据库
   - 返回保存后的消息对象（包含ID和时间戳）

3. **消息推送**
   - 推送给接收者：[`messagingTemplate.convertAndSendToUser()`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/ChatWebSocketController.java:58)
   - 推送给发送者（确认）：[`messagingTemplate.convertAndSendToUser()`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/ChatWebSocketController.java:65)

4. **客户端保存**
   - 接收到消息后调用 [`Android.saveChatMessage()`](TongYangYuan/app/src/main/assets/chat.html:608)
   - 保存到本地SQLite数据库

### 数据同步

- **上行同步**：客户端发送 → 服务器保存 → 推送给对方
- **下行同步**：服务器推送 → 客户端接收 → 本地保存
- **历史同步**：通过 [`/messages/appointment/{appointmentId}`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/controller/ChatMessageController.java) 获取历史记录

## 数据查询

### 查看所有聊天记录
```sql
SELECT 
    cm.*,
    u1.nickname as sender_name,
    u2.nickname as receiver_name
FROM chat_messages cm
LEFT JOIN users u1 ON cm.sender_user_id = u1.id
LEFT JOIN users u2 ON cm.receiver_user_id = u2.id
ORDER BY cm.created_at DESC;
```

### 查看特定预约的聊天记录
```sql
SELECT * FROM chat_messages 
WHERE appointment_id = ? 
ORDER BY created_at ASC;
```

### 查看所有预约
```sql
SELECT 
    a.*,
    c.name as consultant_name,
    u.nickname as parent_name
FROM appointments a
LEFT JOIN consultants c ON a.consultant_id = c.user_id
LEFT JOIN users u ON a.parent_user_id = u.id
ORDER BY a.created_at DESC;
```

## 数据清理说明

已执行的清理操作：
1. 删除所有测试用户（除了张医生和王女士）
2. 删除所有测试咨询师（除了张医生）
3. 清空所有聊天记录
4. 清空所有预约记录
5. 清空在线状态记录

## 重要提示

1. **密码安全**：所有密码使用BCrypt加密存储
2. **数据备份**：建议定期备份MySQL数据库
3. **日志记录**：所有操作都有日志记录，便于追踪
4. **数据恢复**：如需恢复数据，重启服务器会自动执行 [`data.sql`](TongYangYuan-Server/src/main/resources/data.sql)

## 启动服务器

```bash
cd TongYangYuan-Server
mvn spring-boot:run
```

或使用启动脚本：
```bash
start-debug.bat  # Windows
```

服务器启动后会自动：
1. 连接到 `mental_health_db` 数据库
2. 创建所需的表结构
3. 执行 `data.sql` 初始化数据
4. 启动WebSocket服务

---
最后更新：2026-01-30
