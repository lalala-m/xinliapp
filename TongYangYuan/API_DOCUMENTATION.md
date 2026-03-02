# 童养园 (TongYangYuan) - Android与Web端接口文档

## 概述

本文档描述了童养园儿童心理咨询应用的Android端与Web端（HTML页面）之间的JavaScript接口。这些接口通过Android的WebView JavascriptInterface实现，允许Web页面调用Android原生功能。

**版本**: 1.0
**最后更新**: 2025-12-28

---

## 目录

1. [基础接口](#基础接口)
2. [用户认证接口](#用户认证接口)
3. [用户信息接口](#用户信息接口)
4. [咨询师相关接口](#咨询师相关接口)
5. [预约管理接口](#预约管理接口)
6. [聊天消息接口](#聊天消息接口)
7. [多媒体接口](#多媒体接口)
8. [支付接口](#支付接口)
9. [儿童信息接口](#儿童信息接口)
10. [导航接口](#导航接口)
11. [通话接口](#通话接口)

---

## 1. 基础接口

### 1.1 showToast

显示Android Toast消息提示。

**调用方式**:
```javascript
Android.showToast(message)
```

**参数**:
- `message` (String): 要显示的消息内容

**示例**:
```javascript
Android.showToast('操作成功');
```

---

### 1.2 goBack

返回上一页或关闭当前Activity。

**调用方式**:
```javascript
Android.goBack()
```

**示例**:
```javascript
Android.goBack();
```

---

## 2. 用户认证接口

### 2.1 loginWithPhone

使用手机号和验证码登录。

**调用方式**:
```javascript
Android.loginWithPhone(phone, code)
```

**参数**:
- `phone` (String): 11位手机号
- `code` (String): 6位验证码

**示例**:
```javascript
Android.loginWithPhone('13800138000', '123456');
```

**说明**:
- 登录成功后会自动跳转到首页或儿童信息页面
- 如果已有儿童信息则跳转到首页，否则跳转到儿童信息页面

---

### 2.2 loginWithWechat

使用微信授权登录。

**调用方式**:
```javascript
Android.loginWithWechat()
```

**示例**:
```javascript
Android.loginWithWechat();
```

---

### 2.3 logoutAccount

退出登录。

**调用方式**:
```javascript
Android.logoutAccount()
```

**示例**:
```javascript
Android.logoutAccount();
```

---

## 3. 用户信息接口

### 3.1 getUserProfile

获取当前用户信息。

**调用方式**:
```javascript
const profile = Android.getUserProfile()
```

**返回值** (JSON String):
```json
{
  "phone": "13800138000",
  "isPaid": true,
  "hasChildProfile": true,
  "isLoggedIn": true
}
```

**字段说明**:
- `phone`: 用户手机号
- `isPaid`: 是否为付费用户
- `hasChildProfile`: 是否已填写儿童信息
- `isLoggedIn`: 是否已登录

**示例**:
```javascript
const profileJson = Android.getUserProfile();
const profile = JSON.parse(profileJson);
if (profile.isPaid) {
    console.log('用户是付费会员');
}
```

---

### 3.2 isPaidUser

检查用户是否为付费用户。

**调用方式**:
```javascript
const isPaid = Android.isPaidUser()
```

**返回值**: Boolean

**示例**:
```javascript
if (Android.isPaidUser()) {
    // 显示付费功能
}
```

---

## 4. 咨询师相关接口

### 4.1 getConsultants

获取所有咨询师列表。

**调用方式**:
```javascript
const consultants = Android.getConsultants()
```

**返回值** (JSON String):
```json
[
  {
    "name": "张医生",
    "title": "儿童心理咨询师",
    "specialty": "儿童焦虑、学习障碍",
    "rating": 4.8,
    "servedCount": 1200,
    "avatarColor": "#6FA6F8",
    "intro": "10年儿童心理咨询经验...",
    "identityTier": "GOLD",
    "displayIdentityTag": "金牌咨询师",
    "identityTags": ["认证咨询师", "专业资质"],
    "reviews": ["非常专业", "孩子很喜欢"]
  }
]
```

**示例**:
```javascript
const consultantsJson = Android.getConsultants();
const consultants = JSON.parse(consultantsJson);
consultants.forEach(consultant => {
    console.log(consultant.name);
});
```

---

### 4.2 getConsultantDetail

获取指定咨询师的详细信息。

**调用方式**:
```javascript
const detail = Android.getConsultantDetail(name)
```

**参数**:
- `name` (String): 咨询师姓名

**返回值**: JSON String (格式同getConsultants中的单个对象)

**示例**:
```javascript
const detailJson = Android.getConsultantDetail('张医生');
const detail = JSON.parse(detailJson);
```

---

### 4.3 navigateToConsultantDetail

跳转到咨询师详情页面。

**调用方式**:
```javascript
Android.navigateToConsultantDetail(name)
```

**参数**:
- `name` (String): 咨询师姓名

**示例**:
```javascript
Android.navigateToConsultantDetail('张医生');
```

---

### 4.4 navigateToConsultantList

跳转到咨询师列表页面。

**调用方式**:
```javascript
Android.navigateToConsultantList()
```

**示例**:
```javascript
Android.navigateToConsultantList();
```

---

## 5. 预约管理接口

### 5.1 getAppointments

获取所有预约记录。

**调用方式**:
```javascript
const appointments = Android.getAppointments()
```

**返回值** (JSON String):
```json
[
  {
    "id": "apt_123456",
    "date": "2025-12-28",
    "timeSlot": "14:00-15:00",
    "description": "孩子学习焦虑问题",
    "hasChatted": true,
    "childName": "小明",
    "childId": "child_001",
    "pinned": false,
    "consultant": {
      "name": "张医生",
      "title": "儿童心理咨询师"
    }
  }
]
```

**示例**:
```javascript
const appointmentsJson = Android.getAppointments();
const appointments = JSON.parse(appointmentsJson);
```

---

### 5.2 navigateToAppointment

跳转到预约页面。

**调用方式**:
```javascript
Android.navigateToAppointment(consultantName)
```

**参数**:
- `consultantName` (String): 咨询师姓名（可选）

**示例**:
```javascript
Android.navigateToAppointment('张医生');
```

---

### 5.3 submitAppointment

提交预约信息。

**调用方式**:
```javascript
Android.submitAppointment(date, timeSlot, description, childId, childName)
```

**参数**:
- `date` (String): 预约日期，格式：YYYY-MM-DD
- `timeSlot` (String): 时间段，如"14:00-15:00"
- `description` (String): 咨询问题描述
- `childId` (String): 儿童ID
- `childName` (String): 儿童姓名

**示例**:
```javascript
Android.submitAppointment(
    '2025-12-28',
    '14:00-15:00',
    '孩子学习焦虑问题',
    'child_001',
    '小明'
);
```

**说明**:
- 提交成功后会自动跳转到聊天页面

---

### 5.4 pinAppointment

置顶或取消置顶预约。

**调用方式**:
```javascript
Android.pinAppointment(appointmentId, pinned)
```

**参数**:
- `appointmentId` (String): 预约ID
- `pinned` (Boolean): true为置顶，false为取消置顶

**示例**:
```javascript
Android.pinAppointment('apt_123456', true);
```

---

### 5.5 deleteAppointment

删除预约记录。

**调用方式**:
```javascript
Android.deleteAppointment(appointmentId)
```

**参数**:
- `appointmentId` (String): 预约ID

**示例**:
```javascript
Android.deleteAppointment('apt_123456');
```

---

### 5.6 markAppointmentChatted

标记预约已进行过聊天。

**调用方式**:
```javascript
Android.markAppointmentChatted(appointmentId)
```

**参数**:
- `appointmentId` (String): 预约ID

**示例**:
```javascript
Android.markAppointmentChatted('apt_123456');
```

---

### 5.7 openDatePicker

打开日期选择器。

**调用方式**:
```javascript
Android.openDatePicker()
```

**回调**:
选择日期后会调用JavaScript回调函数：
```javascript
window.onDateSelected = function(dateString) {
    // dateString格式: YYYY-MM-DD
    console.log('选择的日期:', dateString);
}
```

**示例**:
```javascript
window.onDateSelected = function(date) {
    document.getElementById('dateInput').value = date;
};
Android.openDatePicker();
```

---

## 6. 聊天消息接口

### 6.1 navigateToChat

跳转到聊天页面。

**调用方式**:
```javascript
Android.navigateToChat(appointmentId)
```

**参数**:
- `appointmentId` (String): 预约ID

**示例**:
```javascript
Android.navigateToChat('apt_123456');
```

---

### 6.2 getChatHistory

获取聊天历史记录。

**调用方式**:
```javascript
const history = Android.getChatHistory(appointmentId)
```

**参数**:
- `appointmentId` (String): 预约ID

**返回值** (JSON String):
```json
[
  {
    "fromConsultant": false,
    "type": "TEXT",
    "content": "您好，我想咨询一下",
    "mediaUri": "",
    "timestamp": 1703750400000
  },
  {
    "fromConsultant": true,
    "type": "TEXT",
    "content": "您好，请问有什么问题？",
    "mediaUri": "",
    "timestamp": 1703750460000
  }
]
```

**字段说明**:
- `fromConsultant`: 是否来自咨询师
- `type`: 消息类型（TEXT/IMAGE/VIDEO/AUDIO）
- `content`: 消息内容
- `mediaUri`: 媒体文件URI（图片/视频/音频）
- `timestamp`: 时间戳

**示例**:
```javascript
const historyJson = Android.getChatHistory('apt_123456');
const history = JSON.parse(historyJson);
```

---

### 6.3 saveChatMessage

保存聊天消息。

**调用方式**:
```javascript
Android.saveChatMessage(appointmentId, fromConsultant, type, content, mediaUri)
```

**参数**:
- `appointmentId` (String): 预约ID
- `fromConsultant` (Boolean): 是否来自咨询师
- `type` (String): 消息类型（TEXT/IMAGE/VIDEO/AUDIO）
- `content` (String): 消息内容
- `mediaUri` (String): 媒体文件URI（可选）

**示例**:
```javascript
Android.saveChatMessage(
    'apt_123456',
    false,
    'TEXT',
    '您好，我想咨询一下',
    ''
);
```

---

## 7. 多媒体接口

### 7.1 openImagePicker

打开图片选择器。

**调用方式**:
```javascript
Android.openImagePicker()
```

**回调**:
选择图片后会调用JavaScript回调函数：
```javascript
window.onMediaSelected = function(type, content, mediaUri) {
    // type: 'image'
    // content: 描述文本
    // mediaUri: 图片的Base64数据URL或文件URI
}
```

**示例**:
```javascript
window.onMediaSelected = function(type, content, mediaUri) {
    if (type === 'image') {
        console.log('选择了图片:', mediaUri);
    }
};
Android.openImagePicker();
```

---

### 7.2 captureImage

使用相机拍照。

**调用方式**:
```javascript
Android.captureImage()
```

**回调**: 同openImagePicker

---

### 7.3 openVideoPicker

打开视频选择器。

**调用方式**:
```javascript
Android.openVideoPicker()
```

**回调**:
```javascript
window.onMediaSelected = function(type, content, mediaUri) {
    // type: 'video'
    // content: 描述文本
    // mediaUri: 视频文件URI
}
```

---

### 7.4 captureVideo

使用相机录制视频。

**调用方式**:
```javascript
Android.captureVideo()
```

**回调**: 同openVideoPicker

---

### 7.5 startVoiceRecord

开始录音。

**调用方式**:
```javascript
Android.startVoiceRecord()
```

**回调**:
```javascript
window.onVoiceRecordState = function(recording) {
    // recording: true表示正在录音，false表示录音结束
    if (recording) {
        console.log('开始录音');
    } else {
        console.log('录音结束');
    }
}
```

---

### 7.6 stopVoiceRecord

停止录音。

**调用方式**:
```javascript
Android.stopVoiceRecord()
```

**回调**:
录音完成后会调用onMediaSelected：
```javascript
window.onMediaSelected = function(type, content, mediaUri) {
    // type: 'audio'
    // content: '语音消息'
    // mediaUri: 音频文件URI
}
```

---

### 7.7 playMedia

播放媒体文件（视频或音频）。

**调用方式**:
```javascript
Android.playMedia(type, uri)
```

**参数**:
- `type` (String): 媒体类型（'video' 或 'audio'）
- `uri` (String): 媒体文件URI

**示例**:
```javascript
Android.playMedia('audio', 'content://...');
```

---

## 8. 支付接口

### 8.1 navigateToRecharge

跳转到充值页面。

**调用方式**:
```javascript
Android.navigateToRecharge()
```

**示例**:
```javascript
Android.navigateToRecharge();
```

---

### 8.2 confirmPayment

确认支付。

**调用方式**:
```javascript
Android.confirmPayment(plan, payment)
```

**参数**:
- `plan` (String): 套餐类型（'month' 或 'quarter'）
- `payment` (String): 支付方式（'alipay' 或 'wechat'）

**示例**:
```javascript
Android.confirmPayment('month', 'alipay');
```

**说明**:
- 支付成功后会自动更新用户的付费状态
- 页面会自动关闭

---

## 9. 儿童信息接口

### 9.1 openChildManagement

打开儿童信息管理页面。

**调用方式**:
```javascript
Android.openChildManagement()
```

**示例**:
```javascript
Android.openChildManagement();
```

---

### 9.2 getChildProfiles

获取所有儿童信息。

**调用方式**:
```javascript
const profiles = Android.getChildProfiles()
```

**返回值** (JSON String):
```json
[
  {
    "id": "child_001",
    "name": "小明",
    "age": 8,
    "gender": "male",
    "grade": "二年级",
    "concerns": "学习焦虑"
  }
]
```

**示例**:
```javascript
const profilesJson = Android.getChildProfiles();
const profiles = JSON.parse(profilesJson);
```

---

### 9.3 saveChildInfo

保存儿童信息。

**调用方式**:
```javascript
Android.saveChildInfo(json)
```

**参数**:
- `json` (String): 儿童信息JSON字符串（数组格式）

**示例**:
```javascript
const children = [
    {
        id: 'child_001',
        name: '小明',
        age: 8,
        gender: 'male',
        grade: '二年级',
        concerns: '学习焦虑'
    }
];
Android.saveChildInfo(JSON.stringify(children));
```

---

## 10. 导航接口

### 10.1 navigateToConsult

跳转到咨询页面。

**调用方式**:
```javascript
Android.navigateToConsult()
```

---

### 10.2 openAccountManagement

打开账户管理页面。

**调用方式**:
```javascript
Android.openAccountManagement()
```

---

### 10.3 navigateToWechatLogin

跳转到微信登录页面。

**调用方式**:
```javascript
Android.navigateToWechatLogin()
```

---

## 11. 通话接口

### 11.1 startVideoCall

发起视频通话。

**调用方式**:
```javascript
Android.startVideoCall(consultantName, appointmentId)
```

**参数**:
- `consultantName` (String): 咨询师姓名
- `appointmentId` (String): 预约ID

**示例**:
```javascript
Android.startVideoCall('张医生', 'apt_123456');
```

**说明**:
- 会自动请求相机和麦克风权限
- 打开视频通话界面

---

### 11.2 startVoiceCall

发起语音通话。

**调用方式**:
```javascript
Android.startVoiceCall(consultantName, appointmentId)
```

**参数**:
- `consultantName` (String): 咨询师姓名
- `appointmentId` (String): 预约ID

**示例**:
```javascript
Android.startVoiceCall('张医生', 'apt_123456');
```

---

## 附录

### A. 数据类型说明

#### 咨询师身份等级 (IdentityTier)
- `BRONZE`: 铜牌咨询师
- `SILVER`: 银牌咨询师
- `GOLD`: 金牌咨询师
- `PLATINUM`: 白金咨询师

#### 消息类型 (MessageType)
- `TEXT`: 文本消息
- `IMAGE`: 图片消息
- `VIDEO`: 视频消息
- `AUDIO`: 语音消息

#### 套餐类型 (PlanType)
- `month`: 月度会员（30天）
- `quarter`: 季度会员（90天）

#### 支付方式 (PaymentMethod)
- `alipay`: 支付宝
- `wechat`: 微信支付

---

### B. 错误处理

所有接口调用前应检查`window.Android`是否存在：

```javascript
if (window.Android && typeof Android.methodName === 'function') {
    Android.methodName();
} else {
    console.log('Android接口不可用');
}
```

---

### C. 网页端开发注意事项

1. **接口可用性检查**: 始终检查Android对象和方法是否存在
2. **JSON解析**: 从Android返回的字符串需要使用`JSON.parse()`解析
3. **回调函数**: 某些接口需要定义全局回调函数（如`window.onMediaSelected`）
4. **权限处理**: 涉及相机、麦克风等功能时，Android端会自动处理权限请求
5. **错误提示**: 使用`showToast`显示用户友好的错误信息

---

### D. 示例：完整的聊天消息发送流程

```javascript
// 1. 发送文本消息
function sendTextMessage(text) {
    const appointmentId = getAppointmentId();

    // 保存到本地
    if (window.Android) {
        Android.saveChatMessage(
            appointmentId,
            false,  // 来自用户
            'TEXT',
            text,
            ''
        );
    }

    // 显示在界面上
    displayMessage(false, 'TEXT', text);
}

// 2. 发送图片
function sendImage() {
    // 定义回调
    window.onMediaSelected = function(type, content, mediaUri) {
        if (type === 'image') {
            const appointmentId = getAppointmentId();

            // 保存到本地
            if (window.Android) {
                Android.saveChatMessage(
                    appointmentId,
                    false,
                    'IMAGE',
                    content,
                    mediaUri
                );
            }

            // 显示在界面上
            displayMessage(false, 'IMAGE', content, mediaUri);
        }
    };

    // 打开图片选择器
    if (window.Android) {
        Android.openImagePicker();
    }
}

// 3. 加载历史消息
function loadHistory() {
    const appointmentId = getAppointmentId();

    if (window.Android) {
        const historyJson = Android.getChatHistory(appointmentId);
        const history = JSON.parse(historyJson);

        history.forEach(msg => {
            displayMessage(
                msg.fromConsultant,
                msg.type,
                msg.content,
                msg.mediaUri
            );
        });
    }
}
```

---

## 版本历史

- **v1.0** (2025-12-28): 初始版本，包含所有核心功能接口

---

## 联系方式

如有问题或建议，请联系开发团队。
