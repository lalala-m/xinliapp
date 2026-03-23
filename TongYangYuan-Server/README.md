# 同阳缘心理健康咨询系统 - 服务器端

## 项目简介

这是同阳缘心理健康咨询系统的服务器端项目，基于Spring Boot 3.2.1开发，提供完整的RESTful API和WebSocket实时通信功能，支持手机端（Android）和Web端的数据同步、实时聊天和视频通信。

## 技术栈

- **框架**: Spring Boot 3.2.1
- **数据库**: MySQL 8.0+
- **ORM**: Spring Data JPA / Hibernate
- **安全**: Spring Security + JWT
- **实时通信**: WebSocket (STOMP)
- **视频通信**: WebRTC信令服务器
- **构建工具**: Maven

## 功能特性

### 1. 用户管理
- 用户注册和登录（支持咨询师和家长两种角色）
- JWT Token认证
- 用户信息同步

### 2. 咨询师管理
- 咨询师信息CRUD
- 咨询师列表查询
- 评分和服务统计

### 3. 预约管理
- 创建、查询、更新、删除预约
- 按咨询师/家长查询预约
- 预约状态管理

### 4. 实时聊天
- WebSocket实时消息推送
- 支持文本、图片、视频、音频消息
- 消息已读/未读状态
- 在线状态管理
- 输入状态提示

### 5. 视频通信
- WebRTC信令服务器
- 支持一对一视频通话
- 通话状态管理（呼叫、接受、拒绝、结束）
- SDP和ICE候选交换

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置

1. 创建数据库：
```bash
mysql -u root -p
```

```sql
CREATE DATABASE mental_health_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 导入数据库表结构：
```bash
mysql -u root -p mental_health_db < database/schema.sql
```

### 3. 配置文件

编辑 `src/main/resources/application.properties`，修改数据库连接信息：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mental_health_db?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=123456
```

### 4. 编译和运行

```bash
# 编译项目
mvn clean package

# 运行项目
mvn spring-boot:run

# 或者运行打包后的jar
java -jar target/mental-health-server-1.0.0.jar
```

服务器将在 `http://localhost:8080` 启动。

## API文档

### 基础URL

```
http://localhost:8080/api
```

### 认证接口

#### 1. 用户登录
```http
POST /auth/login
Content-Type: application/json

{
  "phone": "13800000001",
  "password": "123456"
}
```

响应：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "phone": "13800000001",
    "userType": "CONSULTANT",
    "nickname": "张医生"
  }
}
```

#### 2. 用户注册
```http
POST /auth/register?phone=13900000003&password=123456&userType=PARENT&nickname=测试用户
```

### 咨询师接口

#### 1. 获取所有咨询师
```http
GET /consultants
Authorization: Bearer {token}
```

#### 2. 获取咨询师详情
```http
GET /consultants/{id}
Authorization: Bearer {token}
```

#### 3. 根据用户ID获取咨询师信息
```http
GET /consultants/user/{userId}
Authorization: Bearer {token}
```

### 预约接口

#### 1. 获取咨询师的预约列表
```http
GET /appointments/consultant/{consultantId}
Authorization: Bearer {token}
```

#### 2. 获取家长的预约列表
```http
GET /appointments/parent/{parentUserId}
Authorization: Bearer {token}
```

#### 3. 创建预约
```http
POST /appointments
Authorization: Bearer {token}
Content-Type: application/json

{
  "appointmentNo": "APT20260104003",
  "consultantId": 1,
  "parentUserId": 4,
  "childId": 1,
  "childName": "小明",
  "childAge": 8,
  "appointmentDate": "2026-01-10",
  "timeSlot": "14:00-15:00",
  "description": "孩子学习压力大",
  "status": "PENDING"
}
```

#### 4. 更新预约
```http
PUT /appointments/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "ACCEPTED"
}
```

### 消息接口

#### 1. 获取预约的聊天记录
```http
GET /messages/appointment/{appointmentId}
Authorization: Bearer {token}
```

#### 2. 获取未读消息
```http
GET /messages/unread/{userId}
Authorization: Bearer {token}
```

#### 3. 标记消息已读
```http
PUT /messages/{messageId}/read
Authorization: Bearer {token}
```

## WebSocket连接

### 连接地址

```
ws://localhost:8080/api/ws
```

### 使用SockJS和STOMP

#### JavaScript示例（Web端）

```javascript
// 1. 连接WebSocket
const socket = new SockJS('http://localhost:8080/api/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);

    // 2. 订阅消息队列
    stompClient.subscribe('/user/queue/messages', function(message) {
        const chatMessage = JSON.parse(message.body);
        console.log('收到消息:', chatMessage);
        // 处理接收到的消息
    });

    // 3. 订阅WebRTC信令
    stompClient.subscribe('/user/queue/webrtc', function(message) {
        const signal = JSON.parse(message.body);
        console.log('收到WebRTC信令:', signal);
        // 处理WebRTC信令
    });

    // 4. 通知服务器用户上线
    stompClient.send('/app/chat.online', {}, userId);
});

// 5. 发送聊天消息
function sendMessage(messageData) {
    stompClient.send('/app/chat.send', {}, JSON.stringify(messageData));
}

// 6. 发送WebRTC信令
function sendWebRTCSignal(signal) {
    stompClient.send('/app/webrtc.signal', {}, JSON.stringify(signal));
}
```

#### Android示例

添加依赖（build.gradle）：
```gradle
implementation 'org.java-websocket:Java-WebSocket:1.5.3'
implementation 'com.google.code.gson:gson:2.10.1'
```

Java代码：
```java
// 使用OkHttp + STOMP或者原生WebSocket实现
// 详细实现请参考客户端集成文档
```

### WebSocket消息格式

#### 聊天消息
```json
{
  "appointmentId": 1,
  "senderUserId": 1,
  "receiverUserId": 4,
  "messageType": "TEXT",
  "content": "您好，请问有什么可以帮助您的？",
  "isFromConsultant": true
}
```

#### WebRTC信令
```json
{
  "type": "offer",
  "fromUserId": 1,
  "toUserId": 4,
  "appointmentId": 1,
  "data": {
    "sdp": "v=0\r\no=- ...",
    "type": "offer"
  }
}
```

## WebRTC视频通话流程

### 1. 发起通话
```javascript
// 呼叫方发送通话请求
stompClient.send('/app/webrtc.call', {}, JSON.stringify({
    fromUserId: 1,
    toUserId: 4,
    appointmentId: 1
}));
```

### 2. 接受/拒绝通话
```javascript
// 接受通话
stompClient.send('/app/webrtc.accept', {}, JSON.stringify({
    fromUserId: 4,
    toUserId: 1,
    appointmentId: 1
}));

// 拒绝通话
stompClient.send('/app/webrtc.reject', {}, JSON.stringify({
    fromUserId: 4,
    toUserId: 1,
    appointmentId: 1
}));
```

### 3. 交换SDP
```javascript
// 发送Offer
stompClient.send('/app/webrtc.offer', {}, JSON.stringify({
    fromUserId: 1,
    toUserId: 4,
    data: offerSDP
}));

// 发送Answer
stompClient.send('/app/webrtc.answer', {}, JSON.stringify({
    fromUserId: 4,
    toUserId: 1,
    data: answerSDP
}));
```

### 4. 交换ICE候选
```javascript
stompClient.send('/app/webrtc.ice-candidate', {}, JSON.stringify({
    fromUserId: 1,
    toUserId: 4,
    data: iceCandidate
}));
```

### 5. 结束通话
```javascript
stompClient.send('/app/webrtc.end', {}, JSON.stringify({
    fromUserId: 1,
    toUserId: 4,
    appointmentId: 1
}));
```

## 测试账号

### 咨询师账号
- 手机号: 13800000001
- 密码: 123456
- 用户类型: CONSULTANT

### 家长账号
- 手机号: 13900000001
- 密码: 123456
- 用户类型: PARENT

注：所有测试账号的密码都是 `123456`（已使用BCrypt加密）

## 项目结构

```
TongYangYuan-Server/
├── database/
│   └── schema.sql                 # 数据库表结构
├── src/
│   └── main/
│       ├── java/
│       │   └── com/tongyangyuan/mentalhealth/
│       │       ├── MentalHealthApplication.java    # 主应用类
│       │       ├── config/                         # 配置类
│       │       │   ├── SecurityConfig.java         # 安全配置
│       │       │   └── WebSocketConfig.java        # WebSocket配置
│       │       ├── controller/                     # 控制器
│       │       │   ├── AuthController.java
│       │       │   ├── ConsultantController.java
│       │       │   ├── AppointmentController.java
│       │       │   └── ChatMessageController.java
│       │       ├── dto/                            # 数据传输对象
│       │       │   ├── ApiResponse.java
│       │       │   ├── LoginRequest.java
│       │       │   ├── ChatMessageDTO.java
│       │       │   └── WebRTCSignal.java
│       │       ├── entity/                         # 实体类
│       │       │   ├── User.java
│       │       │   ├── Consultant.java
│       │       │   ├── Child.java
│       │       │   ├── Appointment.java
│       │       │   ├── ChatMessage.java
│       │       │   └── UserOnlineStatus.java
│       │       ├── repository/                     # 数据访问层
│       │       │   ├── UserRepository.java
│       │       │   ├── ConsultantRepository.java
│       │       │   ├── ChildRepository.java
│       │       │   ├── AppointmentRepository.java
│       │       │   ├── ChatMessageRepository.java
│       │       │   └── UserOnlineStatusRepository.java
│       │       ├── service/                        # 业务逻辑层
│       │       │   ├── AuthService.java
│       │       │   ├── ConsultantService.java
│       │       │   ├── AppointmentService.java
│       │       │   └── ChatMessageService.java
│       │       ├── util/                           # 工具类
│       │       │   └── JwtUtil.java
│       │       └── websocket/                      # WebSocket处理
│       │           ├── ChatWebSocketController.java
│       │           └── WebRTCSignalingController.java
│       └── resources/
│           └── application.properties              # 应用配置
└── pom.xml                                         # Maven配置
```

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 确认数据库名称、用户名、密码是否正确
- 检查MySQL是否允许远程连接

### 2. WebSocket连接失败
- 确认服务器已启动
- 检查防火墙设置
- 确认客户端使用正确的WebSocket URL

### 3. JWT Token过期
- 默认过期时间为24小时
- 可在application.properties中修改 `jwt.expiration` 配置

## 后续开发建议

1. **文件上传功能**: 实现图片、视频、音频文件的上传和存储
2. **消息推送**: 集成Firebase Cloud Messaging或极光推送
3. **支付功能**: 集成支付宝、微信支付
4. **数据统计**: 添加咨询师工作统计、用户行为分析
5. **管理后台**: 开发管理员后台系统

## 许可证

本项目仅供学习和研究使用。

## 联系方式

如有问题，请联系开发团队。
