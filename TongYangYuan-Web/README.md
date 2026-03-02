# 童养园咨询师端 - 网页端项目

## 项目说明

这是童养园儿童心理咨询应用的咨询师端网页版本，用于咨询师登录、查看预约、与客户进行文字和视频咨询。

## 技术栈

- HTML5
- CSS3
- JavaScript (ES6+)
- LocalStorage (本地数据存储)
- WebRTC (视频通话，待集成)

## 项目结构

```
TongYangYuan-Web/
├── index.html              # 登录页面
├── dashboard.html          # 咨询师工作台
├── chat.html              # 聊天对话页面
├── video-call.html        # 视频通话页面
├── css/
│   ├── common.css         # 公共样式
│   ├── login.css          # 登录页面样式
│   ├── dashboard.css      # 工作台样式
│   ├── chat.css           # 聊天样式
│   └── video-call.css     # 视频通话样式
├── js/
│   ├── config.js          # 配置文件
│   ├── storage.js         # 本地存储管理
│   ├── auth.js            # 认证管理
│   ├── mock-data.js       # 模拟数据
│   ├── dashboard.js       # 工作台逻辑
│   ├── chat.js            # 聊天逻辑
│   └── video-call.js      # 视频通话逻辑
├── assets/
│   ├── images/            # 图片资源
│   └── icons/             # 图标资源
└── README.md              # 项目说明
```

## 功能模块

### 1. 咨询师登录
- 手机号 + 密码登录
- 记住登录状态
- 自动登录

### 2. 工作台
- 查看今日预约
- 查看待处理咨询
- 查看历史记录
- 个人信息管理

### 3. 文字咨询
- 实时文字聊天
- 发送图片
- 查看聊天历史
- 结束咨询

### 4. 视频咨询
- 视频通话
- 语音通话
- 屏幕共享（可选）
- 通话录制（可选）

## 数据同步

当前版本使用LocalStorage进行本地数据存储，模拟与手机端的数据同步。
后续可以通过WebSocket或HTTP API实现真实的数据同步。

## 开发说明

1. 直接在浏览器中打开index.html即可运行
2. 使用Chrome/Firefox等现代浏览器
3. 建议使用本地服务器（如Live Server）以避免跨域问题

## 默认测试账号

- 手机号: 13800000001
- 密码: 123456
- 咨询师: 张医生

## 版本信息

- 版本: 1.0
- 最后更新: 2025-12-28
