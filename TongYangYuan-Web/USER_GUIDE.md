# 童养园咨询师端 - 完整使用指南

## 项目完成情况 ✅

### 已完成的功能

#### 1. 登录系统 ✅
- 手机号 + 密码登录
- 记住登录状态
- 自动登录检查
- 退出登录功能

#### 2. 工作台 ✅
- 统计数据展示（今日预约、待处理、已完成、累计服务）
- 预约列表管理
- 标签切换（今日/待处理/已完成/全部）
- 接受/拒绝预约
- 开始咨询
- 查看历史记录
- 添加测试预约

#### 3. 聊天功能 ✅
- 实时文字聊天
- 消息历史记录
- 消息发送和接收
- 系统消息提示
- 结束咨询功能
- 只读模式（查看历史）
- 发起视频通话入口

#### 4. 视频通话 ✅
- 本地视频预览
- 远程视频显示
- 静音/取消静音
- 切换摄像头
- 通话时长显示
- 挂断通话
- 通话记录保存

#### 5. 数据管理 ✅
- LocalStorage本地存储
- 咨询师数据管理
- 预约数据管理
- 消息数据管理
- 设置数据管理
- 模拟数据生成

#### 6. UI/UX ✅
- Material Design 3设计
- 与手机端风格一致
- 响应式布局
- 流畅的动画效果
- 友好的交互提示

## 快速开始

### 方法一：直接打开（推荐用于开发测试）

1. 找到项目文件夹：
   ```
   D:\AllProject\AndroidStudioProjects\TongYangYuan-Web
   ```

2. 双击打开 `index.html`

3. 使用测试账号登录：
   - 手机号: 13800000001
   - 密码: 123456

### 方法二：使用Tomcat部署（推荐用于生产环境）

#### 步骤1：安装Tomcat
1. 下载Tomcat 9.x: https://tomcat.apache.org/download-90.cgi
2. 解压到任意目录，如：`C:\apache-tomcat-9.0.xx`

#### 步骤2：部署应用
```bash
# 复制整个项目文件夹到Tomcat的webapps目录
复制: D:\AllProject\AndroidStudioProjects\TongYangYuan-Web
到: C:\apache-tomcat-9.0.xx\webapps\
```

#### 步骤3：启动Tomcat
```bash
# Windows
cd C:\apache-tomcat-9.0.xx\bin
startup.bat

# Linux/Mac
cd /path/to/tomcat/bin
./startup.sh
```

#### 步骤4：访问应用
打开浏览器访问：
```
http://localhost:8080/TongYangYuan-Web/
```

### 方法三：使用本地服务器

#### 使用Python
```bash
cd D:\AllProject\AndroidStudioProjects\TongYangYuan-Web
python -m http.server 8000
```
访问: http://localhost:8000

#### 使用Node.js
```bash
cd D:\AllProject\AndroidStudioProjects\TongYangYuan-Web
npx http-server -p 8000
```
访问: http://localhost:8000

#### 使用VS Code Live Server
1. 安装Live Server插件
2. 右键 `index.html`
3. 选择 "Open with Live Server"

## 功能使用说明

### 1. 登录

1. 打开登录页面
2. 输入测试账号：
   - 手机号: 13800000001
   - 密码: 123456
3. 勾选"记住我"（可选）
4. 点击"登录"按钮

**提示**: 点击测试账号区域可自动填充

### 2. 工作台

登录后自动进入工作台，可以：

#### 查看统计数据
- 今日预约数量
- 待处理预约数量
- 已完成咨询数量
- 累计服务人数

#### 管理预约
- **今日预约**: 查看今天的所有预约
- **待处理**: 查看需要处理的预约
- **已完成**: 查看历史完成记录
- **全部**: 查看所有预约

#### 处理预约
- **接受**: 接受客户的预约请求
- **拒绝**: 拒绝预约请求
- **开始咨询**: 进入聊天页面开始咨询
- **查看记录**: 查看已完成的咨询记录

#### 测试功能
- 点击"添加测试预约"可生成模拟预约数据
- 点击"刷新"可更新数据显示

### 3. 聊天咨询

点击"开始咨询"进入聊天页面：

#### 发送消息
1. 在底部输入框输入文字
2. 按Enter发送（Shift+Enter换行）
3. 或点击"发送"按钮

#### 视频通话
- 点击"📹 视频通话"按钮发起视频通话
- 需要允许浏览器访问摄像头和麦克风

#### 结束咨询
1. 点击"结束咨询"按钮
2. 确认结束
3. 系统自动保存咨询记录
4. 返回工作台

### 4. 视频通话

进入视频通话页面后：

#### 通话控制
- **🎤 静音**: 切换麦克风静音状态
- **📞 挂断**: 结束视频通话
- **🔄 切换摄像头**: 切换前后摄像头

#### 通话信息
- 左上角显示客户姓名
- 显示通话时长
- 右上角显示本地视频预览

#### 结束通话
1. 点击挂断按钮
2. 确认结束
3. 通话记录自动保存到聊天历史
4. 返回聊天页面

### 5. 退出登录

点击右上角"退出登录"按钮即可退出

## 数据说明

### 本地存储
所有数据存储在浏览器的LocalStorage中：
- 登录信息
- 预约记录
- 聊天消息
- 用户设置

### 清除数据
如需清除所有数据：
1. 打开浏览器开发者工具（F12）
2. 进入Application/存储标签
3. 选择Local Storage
4. 删除相关数据

### 初始数据
首次使用时会自动初始化：
- 2个咨询师账号
- 3条测试预约
- 部分测试消息

## 测试账号

### 咨询师账号1（推荐）
- 手机号: 13800000001
- 密码: 123456
- 姓名: 张医生
- 职称: 儿童心理咨询师

### 咨询师账号2
- 手机号: 13800000002
- 密码: 123456
- 姓名: 李医生
- 职称: 青少年心理咨询师

## 浏览器兼容性

### 推荐浏览器
- Chrome 90+
- Firefox 88+
- Edge 90+
- Safari 14+

### 功能支持
- **基础功能**: 所有现代浏览器
- **视频通话**: 需要支持WebRTC的浏览器
- **LocalStorage**: 所有现代浏览器

### 注意事项
- 视频通话需要HTTPS或localhost
- 需要允许浏览器访问摄像头和麦克风
- 建议使用Chrome浏览器以获得最佳体验

## 常见问题

### 1. 登录后跳转失败
- 检查浏览器是否允许LocalStorage
- 清除浏览器缓存后重试
- 检查浏览器控制台是否有错误

### 2. 数据丢失
- LocalStorage数据在清除浏览器数据时会丢失
- 建议定期导出重要数据
- 生产环境应使用后端数据库

### 3. 视频通话无法使用
- 确保使用HTTPS或localhost访问
- 检查浏览器权限设置
- 确认摄像头和麦克风正常工作

### 4. 页面样式错误
- 清除浏览器缓存
- 检查CSS文件是否正确加载
- 使用开发者工具查看网络请求

### 5. Tomcat部署404错误
- 检查项目路径是否正确
- 确认Tomcat已正常启动
- 检查URL拼写是否正确

## 开发说明

### 项目结构
```
TongYangYuan-Web/
├── index.html              # 登录页面
├── dashboard.html          # 工作台
├── chat.html              # 聊天页面
├── video-call.html        # 视频通话
├── css/                   # 样式文件
│   ├── common.css         # 公共样式
│   ├── login.css          # 登录样式
│   ├── dashboard.css      # 工作台样式
│   ├── chat.css           # 聊天样式
│   └── video-call.css     # 视频通话样式
├── js/                    # JavaScript文件
│   ├── config.js          # 配置
│   ├── storage.js         # 存储管理
│   ├── auth.js            # 认证
│   ├── mock-data.js       # 模拟数据
│   ├── dashboard.js       # 工作台逻辑
│   ├── chat.js            # 聊天逻辑
│   └── video-call.js      # 视频通话逻辑
├── WEB-INF/               # Tomcat配置
│   └── web.xml            # Web应用配置
├── README.md              # 项目说明
├── DEVELOPMENT_PROGRESS.md # 开发进度
├── TOMCAT_DEPLOYMENT.md   # Tomcat部署指南
└── USER_GUIDE.md          # 本文件
```

### 技术栈
- HTML5
- CSS3 (Material Design 3)
- JavaScript (ES6+)
- LocalStorage
- WebRTC (视频通话)

### 扩展开发

#### 添加新功能
1. 在对应的HTML文件中添加UI
2. 在对应的CSS文件中添加样式
3. 在对应的JS文件中添加逻辑
4. 更新mock-data.js添加测试数据

#### 集成后端API
1. 修改storage.js，将LocalStorage替换为API调用
2. 添加网络请求库（如axios）
3. 实现数据同步逻辑
4. 处理网络错误和重试

#### 集成WebRTC
1. 选择WebRTC库（如simple-peer）
2. 实现信令服务器
3. 修改video-call.js实现真实通话
4. 添加STUN/TURN服务器配置

## 与手机端配合使用

### 数据同步方案

#### 方案一：共享后端API
- 手机端和网页端都连接同一个后端
- 通过API实现数据同步
- 使用WebSocket实现实时通信

#### 方案二：二维码扫码
- 网页端生成二维码
- 手机端扫码获取会话信息
- 建立点对点连接

#### 方案三：云端数据库
- 使用Firebase或其他云服务
- 实时数据同步
- 离线支持

### 推荐架构
```
手机端 (Android) ←→ 后端API ←→ 网页端 (Web)
                      ↓
                  数据库
                      ↓
                  WebSocket服务
```

## 后续开发建议

### 短期目标
1. 集成真实的后端API
2. 实现WebRTC视频通话
3. 添加文件上传功能
4. 实现消息推送

### 中期目标
1. 添加数据统计和报表
2. 实现预约日历视图
3. 添加咨询记录导出
4. 实现多人会话

### 长期目标
1. 移动端适配优化
2. 离线功能支持
3. 国际化支持
4. 性能优化

## 技术支持

### 文档
- README.md - 项目概述
- DEVELOPMENT_PROGRESS.md - 开发进度
- TOMCAT_DEPLOYMENT.md - 部署指南
- USER_GUIDE.md - 使用指南（本文件）

### 相关资源
- Tomcat官方文档: https://tomcat.apache.org/
- WebRTC文档: https://webrtc.org/
- Material Design: https://m3.material.io/

## 版本信息

- **当前版本**: 1.0.0
- **发布日期**: 2025-12-28
- **最后更新**: 2025-12-28

## 更新日志

### v1.0.0 (2025-12-28)
- ✅ 完成登录功能
- ✅ 完成工作台功能
- ✅ 完成聊天功能
- ✅ 完成视频通话功能
- ✅ 完成Tomcat部署配置
- ✅ 完成用户文档

---

**祝使用愉快！** 🎉
