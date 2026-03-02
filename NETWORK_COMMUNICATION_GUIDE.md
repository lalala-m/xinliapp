# 童养园心理健康平台 - 网络通信与UI优化文档

## 项目概述
本文档记录了童养园心理健康平台的网络通信功能实现和UI交互优化。

## 一、实时通信功能

### 1.1 WebSocket架构

#### 服务端配置
- **框架**: Spring Boot + WebSocket + STOMP
- **端点**: `/ws`
- **消息队列**:
  - `/user/queue/messages` - 个人聊天消息
  - `/user/queue/webrtc` - WebRTC信令
  - `/topic/online-status` - 在线状态广播

#### 客户端实现

**移动端 (Android WebView)**
- 使用 SockJS + STOMP.js
- 连接地址: 动态获取自 [`Android.getBaseUrl()`](TongYangYuan/app/src/main/assets/chat.html:479)
- 自动重连机制
- 消息持久化到本地数据库

**Web端 (咨询师端)**
- 使用 SockJS + STOMP.js
- 连接地址: [`CONFIG.WS_BASE_URL`](TongYangYuan-Web/js/chat.js:111)
- 断线重连: 5秒后自动重连

### 1.2 聊天功能

#### 消息类型
- TEXT - 文本消息
- IMAGE - 图片消息
- VIDEO - 视频消息
- AUDIO - 语音消息

#### 消息流程
1. 用户发送消息 → [`sendMessage()`](TongYangYuan/app/src/main/assets/chat.html:548)
2. 通过WebSocket发送到服务器 → [`/app/chat.send`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/ChatWebSocketController.java:36)
3. 服务器保存到数据库 → [`ChatMessageService.saveMessage()`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/ChatWebSocketController.java:52)
4. 推送给接收者和发送者 → [`messagingTemplate.convertAndSendToUser()`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/ChatWebSocketController.java:58)
5. 客户端接收并显示 → [`handleIncomingMessage()`](TongYangYuan/app/src/main/assets/chat.html:514)

#### 在线状态管理
- 用户上线: [`/app/chat.online`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/ChatWebSocketController.java:77)
- 用户下线: [`/app/chat.offline`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/ChatWebSocketController.java:99)
- 正在输入: [`/app/chat.typing`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/ChatWebSocketController.java:117)

### 1.3 视频通话功能

#### WebRTC架构
- **信令服务器**: WebSocket (STOMP)
- **STUN服务器**: 
  - `stun:stun.l.google.com:19302`
  - `stun:stun1.l.google.com:19302`

#### 信令流程
1. 咨询师发起呼叫 → [`/app/webrtc.call`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/WebRTCSignalingController.java:40)
2. 用户接受/拒绝 → [`/app/webrtc.accept`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/WebRTCSignalingController.java:61) / [`/app/webrtc.reject`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/WebRTCSignalingController.java:81)
3. 交换SDP → [`/app/webrtc.signal`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/WebRTCSignalingController.java:21)
4. 交换ICE候选 → [`/app/webrtc.signal`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/WebRTCSignalingController.java:21)
5. 建立P2P连接
6. 结束通话 → [`/app/webrtc.end`](TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/websocket/WebRTCSignalingController.java:101)

#### Web端实现
- 文件: [`TongYangYuan-Web/js/video-call.js`](TongYangYuan-Web/js/video-call.js)
- 功能:
  - 本地视频预览
  - 远程视频显示
  - 静音/取消静音
  - 通话时长计时
  - 切换摄像头

#### 移动端实现
- 使用Android原生WebRTC库
- 通过WebView与JavaScript交互
- 支持前后摄像头切换

## 二、支付功能统一

### 2.1 统一支付页面
创建了统一的支付页面 [`payment.html`](TongYangYuan/app/src/main/assets/payment.html)，用于处理所有支付场景。

#### 功能特性
- 支持微信支付和支付宝
- 美观的支付方式选择界面
- 订单信息展示
- 加载动画
- 支付结果回调

#### 使用方式
```javascript
// 从充值页面跳转
const params = new URLSearchParams({
    amount: '99.00',
    desc: '月度会员',
    productName: '月度会员',
    planId: 'month',
    orderNumber: 'TYY1234567890'
});
window.location.href = `payment.html?${params.toString()}`;
```

#### Android接口
- [`Android.launchWeChatPay(amount, orderNumber, productName)`](TongYangYuan/app/src/main/assets/payment.html:323)
- [`Android.launchAlipay(amount, orderNumber, productName)`](TongYangYuan/app/src/main/assets/payment.html:330)

### 2.2 充值页面改造
修改了 [`recharge.html`](TongYangYuan/app/src/main/assets/recharge.html)，移除了二维码支付，改为跳转到统一支付页面。

## 三、UI交互优化

### 3.1 移除蓝色高亮
在 [`style.css`](TongYangYuan/app/src/main/assets/css/style.css) 中添加全局样式：

```css
* {
    -webkit-tap-highlight-color: transparent;
    -webkit-touch-callout: none;
    -webkit-user-select: none;
    user-select: none;
}
```

### 3.2 卡片动画效果
优化了卡片点击动画，使用径向渐变波纹效果：

```css
.card::before {
    content: '';
    position: absolute;
    inset: 0;
    background: radial-gradient(circle at center, rgba(111, 166, 248, 0.15), transparent);
    opacity: 0;
    transform: scale(0);
    transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1),
                transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.card:active::before {
    opacity: 1;
    transform: scale(1);
}
```

### 3.3 按钮动画效果
优化了按钮点击反馈：

```css
.btn::before {
    content: '';
    position: absolute;
    inset: 0;
    background: rgba(255, 255, 255, 0.2);
    opacity: 0;
    transform: scale(0);
    transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1),
                transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn:active::before {
    opacity: 1;
    transform: scale(1);
}
```

### 3.4 动画特性
- 使用 `cubic-bezier(0.4, 0, 0.2, 1)` 缓动函数，提供流畅的动画效果
- 点击时缩放到 0.96-0.98，提供触觉反馈
- 波纹效果从中心扩散，符合Material Design规范

## 四、网络配置

### 4.1 服务器配置
- **开发环境**: `http://10.0.2.2:8080` (Android模拟器)
- **生产环境**: 通过 [`Android.getBaseUrl()`](TongYangYuan/app/src/main/assets/chat.html:479) 动态获取

### 4.2 跨域配置
服务器需要配置CORS以支持Web端访问：

```java
@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
```

## 五、测试建议

### 5.1 聊天功能测试
1. 测试文本消息发送和接收
2. 测试图片、视频、语音消息
3. 测试消息持久化
4. 测试断线重连
5. 测试多设备同步

### 5.2 视频通话测试
1. 测试呼叫发起和接受
2. 测试视频和音频质量
3. 测试网络切换场景
4. 测试通话中断处理
5. 测试多人通话（如需要）

### 5.3 支付功能测试
1. 测试微信支付跳转
2. 测试支付宝支付跳转
3. 测试支付成功回调
4. 测试支付失败处理
5. 测试订单状态同步

### 5.4 UI交互测试
1. 测试所有按钮和卡片的点击反馈
2. 测试动画流畅度
3. 测试不同设备的兼容性
4. 测试暗色模式（如支持）

## 六、已知问题和改进建议

### 6.1 已知问题
1. WebRTC在某些网络环境下可能连接失败，需要配置TURN服务器
2. 消息推送可能存在延迟，需要优化服务器性能
3. 支付功能依赖Android原生实现，需要完善

### 6.2 改进建议
1. 添加消息已读/未读状态
2. 添加消息撤回功能
3. 添加文件传输功能
4. 优化视频通话质量
5. 添加群聊功能
6. 添加消息搜索功能
7. 添加聊天记录导出功能

## 七、部署说明

### 7.1 服务器部署
1. 确保WebSocket端口开放
2. 配置HTTPS（生产环境必需）
3. 配置TURN服务器（用于NAT穿透）
4. 配置数据库连接池
5. 配置日志系统

### 7.2 移动端部署
1. 更新网络配置
2. 配置支付SDK
3. 配置推送服务
4. 测试网络连接
5. 发布到应用商店

### 7.3 Web端部署
1. 配置Nginx反向代理
2. 配置WebSocket升级
3. 配置SSL证书
4. 优化静态资源
5. 配置CDN

## 八、技术栈总结

### 后端
- Spring Boot 2.x
- Spring WebSocket
- STOMP协议
- MySQL数据库
- JWT认证

### 前端
- HTML5 + CSS3 + JavaScript
- SockJS + STOMP.js
- WebRTC API
- Material Design 3

### 移动端
- Android WebView
- WebRTC Android SDK
- 微信支付SDK
- 支付宝SDK

## 九、联系方式
如有问题，请联系开发团队。

---
文档版本: 1.0
最后更新: 2026-01-30
