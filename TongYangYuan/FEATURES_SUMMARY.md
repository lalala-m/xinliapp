# 童养园 (TongYangYuan) - 功能总结文档

## 项目概述

童养园是一款儿童心理咨询服务应用，为家长提供专业的儿童心理咨询服务。应用采用混合开发模式（Android原生 + WebView），支持在线预约、实时聊天、视频通话等核心功能。

**技术栈**:
- Android原生开发（Java）
- WebView + HTML/CSS/JavaScript
- Material Design 3设计规范

---

## 核心功能模块

### 1. 用户认证模块

#### 1.1 登录功能
- **手机号验证码登录**: 支持11位手机号 + 6位验证码登录
- **微信授权登录**: 支持微信快捷登录
- **自动登录**: 记住登录状态，下次自动登录

#### 1.2 用户状态管理
- 登录状态持久化
- 付费会员状态管理
- 用户信息本地存储

**相关文件**:
- `AuthActivity.java` - 认证Activity
- `auth.html` - 登录页面
- `PreferenceStore.java` - 用户偏好存储

---

### 2. 咨询师管理模块

#### 2.1 咨询师列表
- 展示所有可用咨询师
- 显示咨询师基本信息（姓名、职称、专长）
- 显示评分和服务人数
- 支持身份标签（金牌、银牌等）

#### 2.2 咨询师详情
- 详细介绍咨询师背景
- 展示用户评价
- 显示专业资质
- 提供预约入口

**相关文件**:
- `ConsultantListActivity.java` - 咨询师列表
- `ConsultantDetailActivity.java` - 咨询师详情
- `ConsultantRepository.java` - 咨询师数据仓库
- `consultant_list.html` - 咨询师列表页面
- `consultant_detail.html` - 咨询师详情页面

---

### 3. 预约管理模块

#### 3.1 创建预约
- 选择咨询师
- 选择日期和时间段
- 填写咨询问题描述
- 选择需要咨询的孩子

#### 3.2 预约列表
- 查看所有预约记录
- 支持置顶重要预约
- 显示预约状态（已聊天/未聊天）
- 快速进入聊天

#### 3.3 预约操作
- 置顶/取消置顶
- 删除预约
- 进入聊天

**相关文件**:
- `AppointmentActivity.java` - 预约Activity
- `AppointmentStore.java` - 预约数据存储
- `appointment.html` - 预约页面

---

### 4. 聊天通讯模块

#### 4.1 文本聊天
- 实时文本消息发送
- 消息历史记录
- 消息持久化存储
- 自动保存聊天记录

#### 4.2 多媒体消息
- **图片消息**:
  - 从相册选择图片
  - 拍照发送
  - 图片预览
  - Base64编码传输

- **视频消息**:
  - 从相册选择视频
  - 录制视频
  - 视频播放

- **语音消息**:
  - 录音功能
  - 语音播放
  - 录音状态提示

#### 4.3 聊天界面
- 消息气泡样式
- 区分用户和咨询师消息
- 消息时间显示
- 输入框自动调整高度
- 底部输入工具栏

**相关文件**:
- `ChatActivity.java` - 聊天Activity
- `ChatStore.java` - 聊天消息存储
- `chat.html` - 聊天页面

---

### 5. 视频/语音通话模块

#### 5.1 视频通话
- 视频通话界面
- 本地视频预览（小窗口）
- 远程视频显示（全屏）
- 通话控制按钮：
  - 静音/取消静音
  - 挂断
  - 切换摄像头
- 通话时长显示

#### 5.2 语音通话
- 语音通话功能
- 通话状态提示

#### 5.3 权限管理
- 自动请求相机权限
- 自动请求麦克风权限
- 权限拒绝提示

**相关文件**:
- `VideoCallActivity.java` - 视频通话Activity
- `activity_video_call.xml` - 视频通话布局

**注意**: 当前为基础框架，实际的音视频通话需要集成WebRTC或其他音视频SDK。

---

### 6. 支付充值模块

#### 6.1 会员套餐
- **月度会员**: ¥99，30天无限次咨询
- **季度会员**: ¥269，90天无限次咨询（省30元）

#### 6.2 支付方式
- 支付宝支付
- 微信支付

#### 6.3 支付流程
1. 选择会员套餐
2. 选择支付方式
3. 生成支付二维码
4. 扫码支付
5. 支付成功后自动开通会员

#### 6.4 UI特色
- 渐变色背景
- 卡片式套餐展示
- 动画效果
- 二维码展示
- 支付状态反馈

**相关文件**:
- `RechargeActivity.java` - 充值Activity
- `recharge.html` - 充值页面（已美化）
- `qr_alipay_sample.svg` - 支付宝二维码示例
- `qr_wechat_sample.svg` - 微信二维码示例

---

### 7. 儿童信息管理模块

#### 7.1 儿童档案
- 添加多个孩子信息
- 记录孩子基本信息：
  - 姓名
  - 年龄
  - 性别
  - 年级
  - 关注问题

#### 7.2 信息管理
- 编辑儿童信息
- 删除儿童档案
- 信息持久化存储

**相关文件**:
- `ChildInfoActivity.java` - 儿童信息Activity
- `ChildProfile.java` - 儿童档案数据模型
- `child_info.html` - 儿童信息页面

---

### 8. 个人中心模块

#### 8.1 用户信息
- 显示手机号
- 显示会员状态
- 显示儿童档案状态

#### 8.2 功能入口
- 账户管理
- 儿童信息管理
- 会员中心
- 咨询计划
- 退出登录

**相关文件**:
- `ProfileFragment.java` - 个人中心Fragment
- `fragment_profile.xml` - 个人中心布局

---

### 9. 首页模块

#### 9.1 功能入口
- 快速预约
- 咨询师列表
- 我的预约
- 会员充值

#### 9.2 信息展示
- 欢迎信息
- 功能卡片
- 快捷操作

**相关文件**:
- `HomeFragment.java` - 首页Fragment
- `fragment_home.xml` - 首页布局

---

### 10. 消息中心模块

#### 10.1 消息列表
- 显示所有预约对话
- 显示最后一条消息
- 显示未读状态
- 置顶功能

#### 10.2 消息操作
- 进入聊天
- 删除对话
- 置顶对话

**相关文件**:
- `MessageFragment.java` - 消息Fragment
- `fragment_message.xml` - 消息布局

---

## 数据存储

### 本地存储方案

#### 1. SharedPreferences
用于存储用户偏好和简单数据：
- 登录状态
- 用户手机号
- 付费状态
- 儿童信息JSON

**实现类**: `PreferenceStore.java`

#### 2. 文件存储
用于存储结构化数据：
- 聊天消息记录
- 预约记录

**实现类**:
- `ChatStore.java` - 聊天消息存储
- `AppointmentStore.java` - 预约记录存储

#### 3. 媒体文件存储
- 图片：MediaStore.Images
- 视频：MediaStore.Video
- 语音：应用私有目录

---

## UI/UX设计

### 设计风格

#### 1. Material Design 3
- 遵循Material Design 3设计规范
- 使用Material组件
- 统一的视觉语言

#### 2. 色彩方案
- **主色**: #6FA6F8（蓝色）
- **辅助色**: #B7BCC3（灰色）
- **背景色**: #F9FAFB（浅灰）
- **文本色**: #2C3345（深灰）
- **渐变色**: #667eea → #764ba2（紫色渐变，用于支付页面）

#### 3. 图标系统
- Material Design图标
- 矢量图标（SVG/XML）
- 统一的图标风格

**图标文件**:
- `ic_home.xml` - 首页图标
- `ic_message.xml` - 消息图标
- `ic_profile.xml` - 个人中心图标
- `ic_phone.xml` - 电话图标
- `ic_video_call.xml` - 视频通话图标
- `ic_camera.xml` - 相机图标
- `ic_mic.xml` - 麦克风图标
- `ic_send.xml` - 发送图标
- `ic_qr_code.xml` - 二维码图标
- 等等...

#### 4. 动画效果
- 页面切换动画
- 按钮点击反馈
- 卡片展开/收起
- 加载动画

---

## 权限管理

### 必需权限

1. **CAMERA**: 拍照、视频通话
2. **RECORD_AUDIO**: 录音、语音通话
3. **READ_MEDIA_IMAGES**: 读取图片（Android 13+）
4. **READ_MEDIA_VIDEO**: 读取视频（Android 13+）
5. **READ_EXTERNAL_STORAGE**: 读取外部存储（Android 12及以下）

### 权限请求策略
- 运行时动态请求
- 权限说明提示
- 权限拒绝处理

---

## 网页端开发指南

### 1. 接口调用

所有Android接口通过`window.Android`对象调用：

```javascript
if (window.Android && typeof Android.methodName === 'function') {
    Android.methodName(params);
} else {
    // 降级处理或提示
}
```

### 2. 回调函数

某些接口需要定义全局回调函数：

```javascript
// 媒体选择回调
window.onMediaSelected = function(type, content, mediaUri) {
    // 处理选择的媒体
};

// 日期选择回调
window.onDateSelected = function(dateString) {
    // 处理选择的日期
};

// 录音状态回调
window.onVoiceRecordState = function(recording) {
    // 处理录音状态变化
};
```

### 3. 数据格式

- 从Android返回的数据为JSON字符串，需要`JSON.parse()`解析
- 传递给Android的复杂数据需要`JSON.stringify()`序列化

### 4. 样式规范

- 使用CSS变量定义主题色
- 响应式设计，适配不同屏幕
- 最大宽度520px，居中显示
- 使用毛玻璃效果（backdrop-filter）

---

## 项目结构

```
TongYangYuan/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/tongyangyuan/
│   │   │   │   ├── MainActivity.java                 # 主Activity
│   │   │   │   ├── LauncherActivity.java            # 启动页
│   │   │   │   ├── AuthActivity.java                # 认证
│   │   │   │   ├── ChatActivity.java                # 聊天
│   │   │   │   ├── VideoCallActivity.java           # 视频通话
│   │   │   │   ├── ConsultActivity.java             # 咨询
│   │   │   │   ├── ConsultantListActivity.java      # 咨询师列表
│   │   │   │   ├── ConsultantDetailActivity.java    # 咨询师详情
│   │   │   │   ├── AppointmentActivity.java         # 预约
│   │   │   │   ├── RechargeActivity.java            # 充值
│   │   │   │   ├── ChildInfoActivity.java           # 儿童信息
│   │   │   │   ├── fragment/
│   │   │   │   │   ├── HomeFragment.java            # 首页
│   │   │   │   │   ├── MessageFragment.java         # 消息
│   │   │   │   │   └── ProfileFragment.java         # 个人中心
│   │   │   │   ├── webview/
│   │   │   │   │   ├── WebViewActivity.java         # WebView容器
│   │   │   │   │   └── WebAppInterface.java         # JS接口
│   │   │   │   ├── data/
│   │   │   │   │   ├── PreferenceStore.java         # 偏好存储
│   │   │   │   │   ├── ChatStore.java               # 聊天存储
│   │   │   │   │   └── AppointmentStore.java        # 预约存储
│   │   │   │   ├── consult/
│   │   │   │   │   ├── Consultant.java              # 咨询师模型
│   │   │   │   │   └── ConsultantRepository.java    # 咨询师仓库
│   │   │   │   └── child/
│   │   │   │       └── ChildProfile.java            # 儿童档案
│   │   │   ├── res/
│   │   │   │   ├── layout/                          # 布局文件
│   │   │   │   ├── drawable/                        # 图标资源
│   │   │   │   ├── values/                          # 值资源
│   │   │   │   └── mipmap/                          # 应用图标
│   │   │   ├── assets/
│   │   │   │   ├── *.html                           # HTML页面
│   │   │   │   ├── css/
│   │   │   │   │   └── style.css                    # 样式文件
│   │   │   │   ├── js/
│   │   │   │   │   └── app.js                       # JavaScript
│   │   │   │   ├── images/
│   │   │   │   │   ├── qr_alipay_sample.svg         # 支付宝二维码
│   │   │   │   │   └── qr_wechat_sample.svg         # 微信二维码
│   │   │   │   └── icons/                           # SVG图标
│   │   │   └── AndroidManifest.xml                  # 清单文件
│   │   └── build.gradle                             # 构建配置
│   └── API_DOCUMENTATION.md                         # API文档
└── README.md                                        # 项目说明
```

---

## 后续开发建议

### 1. 音视频通话
- 集成WebRTC实现实时音视频通话
- 或集成第三方SDK（如声网Agora、腾讯云TRTC）

### 2. 支付集成
- 集成支付宝SDK
- 集成微信支付SDK
- 实现真实的支付流程

### 3. 后端服务
- 开发RESTful API
- 实现用户认证
- 实现消息推送
- 实现数据同步

### 4. 数据库
- 使用Room数据库替代文件存储
- 实现数据迁移
- 优化查询性能

### 5. 网络通信
- 使用Retrofit进行网络请求
- 实现WebSocket实时通信
- 添加网络状态监听

### 6. 推送通知
- 集成Firebase Cloud Messaging
- 或集成极光推送
- 实现消息提醒

### 7. 性能优化
- 图片加载优化（Glide/Coil）
- 内存优化
- 启动速度优化
- 电池优化

### 8. 安全性
- 数据加密
- 通信加密（HTTPS）
- 防止截屏（敏感页面）
- 代码混淆

### 9. 测试
- 单元测试
- UI测试
- 集成测试
- 性能测试

### 10. 国际化
- 多语言支持
- 本地化资源

---

## 技术亮点

1. **混合开发架构**: Android原生 + WebView，兼顾性能和开发效率
2. **Material Design 3**: 现代化的UI设计
3. **模块化设计**: 清晰的模块划分，易于维护
4. **数据持久化**: 完善的本地存储方案
5. **权限管理**: 规范的运行时权限处理
6. **多媒体支持**: 图片、视频、语音全面支持
7. **视频通话框架**: 预留视频通话接口
8. **美观的支付页面**: 渐变色设计，动画效果

---

## 开发团队

- Android开发
- Web前端开发
- UI/UX设计
- 后端开发（待开发）

---

## 版本信息

- **当前版本**: 1.0
- **最后更新**: 2025-12-28
- **Android最低版本**: API 21 (Android 5.0)
- **目标版本**: API 34 (Android 14)

---

## 许可证

[待定]

---

## 联系方式

如有问题或建议，请联系开发团队。
