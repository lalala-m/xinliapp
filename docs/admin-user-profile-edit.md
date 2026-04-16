# 管理后台用户昵称和头像修改功能开发文档

> 文档版本：v1.0  
> 创建日期：2026-04-07  
> 项目：同阳缘心理健康咨询系统  

---

## 一、功能概述

### 1.1 需求描述

在管理后台（`TongYangYuan-Web/admin/users.html`）的用户管理页面中，管理员可以查看和编辑用户信息。目前已实现用户列表查看、新增用户、启用/禁用用户、删除用户功能，但缺少编辑用户昵称和头像的功能。

### 1.2 现状分析

| 功能 | 状态 | 说明 |
|------|------|------|
| 用户列表展示 | ✅ 已完成 | 显示用户ID、手机号、姓名、孩子信息等 |
| 新增用户 | ✅ 已完成 | 支持手机号、姓名、密码、角色 |
| 查看用户详情 | ✅ 已完成 | 模态框展示用户完整信息 |
| 编辑用户信息 | ❌ 未完成 | `editUser()` 函数仅提示"开发中" |
| 修改昵称 | ❌ 未完成 | 需要新增编辑表单 |
| 修改头像 | ❌ 未完成 | 需要头像上传功能 |

---

## 二、技术架构

### 2.1 现有接口

**后端已有接口：**

```java
// UserController.java - 用户端更新自己资料（需JWT认证）
PUT /user/profile
Request: { "nickname": "xxx", "avatarUrl": "xxx" }
Response: { "code": 200, "data": { "nickname": "xxx", "avatarUrl": "xxx" } }

// AdminController.java - 管理员更新用户资料（需管理员认证）
PUT /admin/users/{id}
Request: User 对象
Response: { "code": 200, "data": User 对象 }
```

### 2.2 数据模型

**User 实体关键字段：**

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String phone;          // 手机号（登录账号）
    private String nickname;        // 昵称
    private String avatarUrl;      // 头像URL
    private String password;       // 密码（加密存储）
    private UserType userType;     // 用户类型 PARENT/CONSULTANT/ADMIN
    private UserStatus status;     // 状态 ACTIVE/INACTIVE/BANNED
    
    // 其他字段...
}
```

---

## 三、开发计划

### 阶段一：后端接口完善

#### 任务 1.1：新增管理员修改用户资料接口

**文件：** `TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/controller/AdminController.java`

```java
/**
 * 更新用户昵称和头像（管理员专用）
 * PUT /admin/users/{id}/profile
 */
@PutMapping("/users/{id}/profile")
public ApiResponse<Map<String, Object>> updateUserProfile(
        @PathVariable Long id,
        @RequestBody UpdateUserProfileRequest request) {
    try {
        User user = adminService.updateUserProfile(id, request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("nickname", user.getNickname());
        result.put("avatarUrl", user.getAvatarUrl());
        
        return ApiResponse.success("用户资料已更新", result);
    } catch (Exception e) {
        return ApiResponse.error(e.getMessage());
    }
}
```

#### 任务 1.2：新增请求 DTO 类

**文件：** `TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/dto/UpdateUserProfileRequest.java`

```java
package com.tongyangyuan.mentalhealth.dto;

public class UpdateUserProfileRequest {
    private String nickname;
    private String avatarUrl;
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
```

#### 任务 1.3：实现 Service 层方法

**文件：** `TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/service/AdminService.java`

```java
/**
 * 更新用户资料（昵称和头像）
 */
public User updateUserProfile(Long userId, UpdateUserProfileRequest request) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
    
    // 更新昵称
    if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
        if (request.getNickname().length() > 50) {
            throw new RuntimeException("昵称不能超过50个字符");
        }
        user.setNickname(request.getNickname().trim());
    }
    
    // 更新头像
    if (request.getAvatarUrl() != null) {
        // 验证头像URL格式
        if (request.getAvatarUrl().isEmpty() || isValidUrl(request.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
        } else {
            throw new RuntimeException("头像URL格式不正确");
        }
    }
    
    return userRepository.save(user);
}

private boolean isValidUrl(String url) {
    if (url == null || url.isEmpty()) return true; // 空值允许（清除头像）
    return url.matches("^(https?://|/).*");
}
```

---

### 阶段二：头像上传功能

#### 任务 2.1：检查现有头像上传接口

**文件：** `TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/controller/FileUploadController.java`

查看现有接口：

```java
@PostMapping("/avatar")
public ApiResponse<Map<String, String>> uploadAvatar(
        @RequestParam("file") MultipartFile file,
        @RequestHeader("Authorization") String token) {
    // 现有头像上传接口（需要JWT认证）
}
```

**问题：** 管理员上传用户头像时，需要使用管理员Token或者新增公开上传接口。

#### 任务 2.2：新增管理员头像上传接口

在 `AdminController.java` 中新增：

```java
/**
 * 管理员上传用户头像
 * POST /admin/users/{id}/avatar
 */
@PostMapping("/users/{id}/avatar")
public ApiResponse<Map<String, Object>> uploadUserAvatar(
        @PathVariable Long id,
        @RequestParam("file") MultipartFile file) {
    try {
        // 1. 验证用户存在
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 2. 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ApiResponse.error("只能上传图片文件");
        }
        
        // 3. 验证文件大小（最大2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return ApiResponse.error("图片大小不能超过2MB");
        }
        
        // 4. 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = "avatar_" + id + "_" + System.currentTimeMillis() + extension;
        
        // 5. 保存文件
        String uploadDir = uploadPath + "/avatars/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        File destFile = new File(uploadDir + newFilename);
        file.transferTo(destFile);
        
        // 6. 更新用户头像URL
        String avatarUrl = "/uploads/avatars/" + newFilename;
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        
        // 7. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("avatarUrl", avatarUrl);
        result.put("filename", newFilename);
        
        return ApiResponse.success("头像上传成功", result);
    } catch (Exception e) {
        return ApiResponse.error("头像上传失败: " + e.getMessage());
    }
}
```

---

### 阶段三：前端页面开发

#### 任务 3.1：新增用户编辑模态框

**文件：** `TongYangYuan-Web/admin/users.html`

在 `userModal` 后面添加编辑模态框：

```html
<!-- 用户编辑模态框 -->
<div id="editUserModal" class="modal">
    <div class="modal-content" style="max-width: 500px;">
        <span class="close" onclick="closeEditModal()">&times;</span>
        <h2>编辑用户资料</h2>
        <form id="editUserForm" onsubmit="event.preventDefault(); saveUserProfile();">
            <input type="hidden" id="editUserId">
            
            <!-- 用户基本信息（只读）-->
            <div class="form-group">
                <label>用户ID</label>
                <input type="text" id="editDisplayId" class="form-control" readonly>
            </div>
            <div class="form-group">
                <label>手机号</label>
                <input type="text" id="editPhone" class="form-control" readonly>
            </div>
            
            <!-- 可编辑字段 -->
            <div class="form-group">
                <label for="editNickname">昵称</label>
                <input type="text" id="editNickname" class="form-control" 
                       placeholder="请输入昵称" maxlength="50">
                <small class="text-muted">最多50个字符</small>
            </div>
            
            <div class="form-group">
                <label>头像</label>
                <div class="avatar-edit-container">
                    <div class="current-avatar" id="currentAvatarPreview">
                        <img id="avatarPreviewImg" src="" alt="当前头像">
                    </div>
                    <div class="avatar-actions">
                        <label class="btn-upload">
                            <input type="file" id="avatarFileInput" accept="image/*" 
                                   style="display: none;" onchange="handleAvatarSelect(event)">
                            📷 选择图片
                        </label>
                        <button type="button" class="btn-secondary" onclick="clearAvatar()">
                            🗑️ 清除头像
                        </button>
                    </div>
                </div>
                <div class="avatar-upload-area" id="avatarUploadArea" style="display: none;">
                    <div class="upload-preview">
                        <img id="newAvatarPreview" alt="预览">
                    </div>
                    <div class="upload-progress" id="uploadProgress" style="display: none;">
                        <div class="progress-bar">
                            <div class="progress-fill" id="progressFill"></div>
                        </div>
                        <span id="progressText">0%</span>
                    </div>
                    <button type="button" class="btn-primary" id="uploadAvatarBtn" onclick="uploadAvatar()">
                        上传头像
                    </button>
                </div>
                <input type="hidden" id="newAvatarUrl">
                <small class="text-muted">支持 JPG、PNG 格式，最大 2MB</small>
            </div>
            
            <div class="modal-actions" style="margin-top: 20px;">
                <button type="button" class="btn-secondary" onclick="closeEditModal()">取消</button>
                <button type="submit" class="btn-primary" id="saveProfileBtn">保存</button>
            </div>
        </form>
    </div>
</div>
```

#### 任务 3.2：添加编辑模态框样式

**文件：** `TongYangYuan-Web/admin/css/admin.css`

```css
/* 用户编辑模态框样式 */
.avatar-edit-container {
    display: flex;
    align-items: flex-start;
    gap: 20px;
}

.current-avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    overflow: hidden;
    background: #f0f0f0;
    border: 2px solid #ddd;
}

.current-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.avatar-actions {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.btn-upload {
    display: inline-block;
    padding: 8px 16px;
    background: #2196F3;
    color: white;
    border-radius: 4px;
    cursor: pointer;
    transition: background 0.3s;
}

.btn-upload:hover {
    background: #1976D2;
}

.avatar-upload-area {
    margin-top: 15px;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 8px;
    border: 1px dashed #ccc;
}

.upload-preview {
    width: 100px;
    height: 100px;
    margin: 0 auto 10px;
    border-radius: 50%;
    overflow: hidden;
    border: 2px solid #2196F3;
}

.upload-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.upload-progress {
    margin: 10px 0;
}

.progress-bar {
    height: 8px;
    background: #e0e0e0;
    border-radius: 4px;
    overflow: hidden;
}

.progress-fill {
    height: 100%;
    background: #2196F3;
    transition: width 0.3s;
}

.progress-text {
    text-align: center;
    font-size: 12px;
    color: #666;
    margin-top: 5px;
}
```

#### 任务 3.3：实现前端编辑功能

**文件：** `TongYangYuan-Web/admin/js/admin-users.js`

```javascript
// ========== 用户编辑功能 ==========

let editAvatarChanged = false;

/**
 * 编辑用户 - 打开编辑模态框
 */
async function editUser(userId) {
    try {
        // 获取用户详情
        const user = users.find(u => u.id === userId) || {};
        
        // 填充表单
        document.getElementById('editUserId').value = user.id;
        document.getElementById('editDisplayId').value = user.id;
        document.getElementById('editPhone').value = user.phone || '';
        document.getElementById('editNickname').value = user.nickname || '';
        document.getElementById('newAvatarUrl').value = user.avatarUrl || '';
        
        // 显示当前头像
        const previewImg = document.getElementById('avatarPreviewImg');
        if (user.avatarUrl) {
            previewImg.src = getFullImageUrl(user.avatarUrl);
        } else {
            previewImg.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%23999"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>';
        }
        
        // 重置编辑状态
        editAvatarChanged = false;
        document.getElementById('avatarUploadArea').style.display = 'none';
        document.getElementById('newAvatarPreview').src = '';
        
        // 显示模态框
        const modal = document.getElementById('editUserModal');
        modal.classList.add('show');
        
    } catch (error) {
        console.error('获取用户详情失败:', error);
        AdminCommon.showError('获取用户详情失败');
    }
}

/**
 * 关闭编辑模态框
 */
function closeEditModal() {
    const modal = document.getElementById('editUserModal');
    modal.classList.remove('show');
    editAvatarChanged = false;
}

/**
 * 处理头像选择
 */
function handleAvatarSelect(event) {
    const file = event.target.files[0];
    if (!file) return;
    
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
        AdminCommon.showError('请选择图片文件');
        return;
    }
    
    // 验证文件大小
    if (file.size > 2 * 1024 * 1024) {
        AdminCommon.showError('图片大小不能超过2MB');
        return;
    }
    
    // 显示预览
    const reader = new FileReader();
    reader.onload = function(e) {
        document.getElementById('newAvatarPreview').src = e.target.result;
        document.getElementById('avatarUploadArea').style.display = 'block';
        editAvatarChanged = true;
    };
    reader.readAsDataURL(file);
}

/**
 * 上传头像
 */
async function uploadAvatar() {
    const fileInput = document.getElementById('avatarFileInput');
    const file = fileInput.files[0];
    
    if (!file) {
        AdminCommon.showError('请先选择图片');
        return;
    }
    
    const userId = document.getElementById('editUserId').value;
    const formData = new FormData();
    formData.append('file', file);
    
    const progressArea = document.getElementById('uploadProgress');
    const progressFill = document.getElementById('progressFill');
    const progressText = document.getElementById('progressText');
    const uploadBtn = document.getElementById('uploadAvatarBtn');
    
    try {
        uploadBtn.disabled = true;
        uploadBtn.textContent = '上传中...';
        progressArea.style.display = 'block';
        progressFill.style.width = '0%';
        progressText.textContent = '0%';
        
        // 使用 XMLHttpRequest 以支持进度显示
        await new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            
            xhr.upload.addEventListener('progress', (e) => {
                if (e.lengthComputable) {
                    const percent = Math.round((e.loaded / e.total) * 100);
                    progressFill.style.width = percent + '%';
                    progressText.textContent = percent + '%';
                }
            });
            
            xhr.addEventListener('load', () => {
                if (xhr.status >= 200 && xhr.status < 300) {
                    try {
                        const response = JSON.parse(xhr.responseText);
                        if (response.code === 200) {
                            document.getElementById('newAvatarUrl').value = response.data.avatarUrl;
                            document.getElementById('avatarPreviewImg').src = getFullImageUrl(response.data.avatarUrl);
                            AdminCommon.showSuccess('头像上传成功');
                            progressArea.style.display = 'none';
                            document.getElementById('avatarUploadArea').style.display = 'none';
                        } else {
                            reject(new Error(response.message || '上传失败'));
                        }
                    } catch (e) {
                        reject(new Error('解析响应失败'));
                    }
                } else {
                    reject(new Error('上传失败: ' + xhr.status));
                }
                resolve();
            });
            
            xhr.addEventListener('error', () => {
                reject(new Error('网络错误'));
                resolve();
            });
            
            xhr.open('POST', `${API_BASE_URL}/admin/users/${userId}/avatar`);
            xhr.setRequestHeader('Authorization', `Bearer ${getAdminToken()}`);
            xhr.send(formData);
        });
        
    } catch (error) {
        console.error('上传失败:', error);
        AdminCommon.showError(error.message || '上传失败');
        progressArea.style.display = 'none';
    } finally {
        uploadBtn.disabled = false;
        uploadBtn.textContent = '上传头像';
    }
}

/**
 * 清除头像
 */
function clearAvatar() {
    if (!confirm('确定要清除头像吗？')) return;
    document.getElementById('newAvatarUrl').value = '';
    document.getElementById('avatarPreviewImg').src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%23999"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>';
    editAvatarChanged = true;
}

/**
 * 保存用户资料
 */
async function saveUserProfile() {
    const userId = document.getElementById('editUserId').value;
    const nickname = document.getElementById('editNickname').value.trim();
    const avatarUrl = document.getElementById('newAvatarUrl').value;
    
    // 验证昵称
    if (nickname && nickname.length > 50) {
        AdminCommon.showError('昵称不能超过50个字符');
        return;
    }
    
    const saveBtn = document.getElementById('saveProfileBtn');
    saveBtn.disabled = true;
    saveBtn.textContent = '保存中...';
    
    try {
        const requestData = {};
        if (nickname) {
            requestData.nickname = nickname;
        }
        if (avatarUrl !== undefined) {
            requestData.avatarUrl = avatarUrl;
        }
        
        const response = await AdminCommon.request(
            `/admin/users/${userId}/profile`,
            {
                method: 'PUT',
                body: JSON.stringify(requestData)
            }
        );
        
        if (response && response.code === 200) {
            AdminCommon.showSuccess('保存成功');
            AdminCommon.logAction('UPDATE_USER_PROFILE', `更新用户 ${userId} 的资料`);
            closeEditModal();
            loadUsers(); // 刷新列表
        } else {
            AdminCommon.showError(response.message || '保存失败');
        }
    } catch (error) {
        console.error('保存失败:', error);
        AdminCommon.showError('保存失败: ' + error.message);
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = '保存';
    }
}

/**
 * 获取完整的图片URL
 */
function getFullImageUrl(relativeUrl) {
    if (!relativeUrl) return '';
    if (relativeUrl.startsWith('http://') || relativeUrl.startsWith('https://')) {
        return relativeUrl;
    }
    return API_BASE_URL + relativeUrl;
}

/**
 * 获取管理员Token
 */
function getAdminToken() {
    return localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken') || '';
}

// 导出函数到全局作用域
window.editUser = editUser;
window.closeEditModal = closeEditModal;
window.handleAvatarSelect = handleAvatarSelect;
window.uploadAvatar = uploadAvatar;
window.clearAvatar = clearAvatar;
window.saveUserProfile = saveUserProfile;
```

---

## 四、API 接口汇总

### 4.1 新增接口

| 方法 | 路径 | 说明 | 认证 | 参数 |
|------|------|------|------|------|
| PUT | `/admin/users/{id}/profile` | 更新用户昵称头像 | 管理员 | `{ nickname, avatarUrl }` |
| POST | `/admin/users/{id}/avatar` | 上传用户头像 | 管理员 | `multipart/form-data` |

### 4.2 响应格式

**PUT /admin/users/{id}/profile**

```json
// 成功响应
{
    "code": 200,
    "message": "用户资料已更新",
    "data": {
        "id": 123,
        "nickname": "新昵称",
        "avatarUrl": "/uploads/avatars/avatar_123_1234567890.jpg"
    }
}

// 失败响应
{
    "code": 500,
    "message": "用户不存在"
}
```

**POST /admin/users/{id}/avatar**

```json
// 成功响应
{
    "code": 200,
    "message": "头像上传成功",
    "data": {
        "id": 123,
        "avatarUrl": "/uploads/avatars/avatar_123_1234567890.jpg",
        "filename": "avatar_123_1234567890.jpg"
    }
}

// 失败响应
{
    "code": 500,
    "message": "只能上传图片文件"
}
```

---

## 五、开发任务清单

### 阶段一：后端开发

| # | 任务 | 文件 | 优先级 | 状态 |
|---|------|------|--------|------|
| 1.1 | 新增 UpdateUserProfileRequest DTO | `dto/UpdateUserProfileRequest.java` | P0 | ⬜ |
| 1.2 | AdminController 添加 profile 接口 | `controller/AdminController.java` | P0 | ⬜ |
| 1.3 | AdminService 实现 updateUserProfile | `service/AdminService.java` | P0 | ⬜ |
| 1.4 | AdminController 添加头像上传接口 | `controller/AdminController.java` | P0 | ⬜ |

### 阶段二：前端开发

| # | 任务 | 文件 | 优先级 | 状态 |
|---|------|------|--------|------|
| 2.1 | 添加编辑模态框 HTML | `admin/users.html` | P0 | ⬜ |
| 2.2 | 添加编辑相关 CSS | `admin/css/admin.css` | P1 | ⬜ |
| 2.3 | 实现编辑功能 JS | `admin/js/admin-users.js` | P0 | ⬜ |
| 2.4 | 集成头像上传功能 | `admin/js/admin-users.js` | P0 | ⬜ |

### 阶段三：测试验证

| # | 任务 | 说明 | 状态 |
|---|------|------|------|
| 3.1 | 测试昵称修改 | 修改用户昵称并验证 | ⬜ |
| 3.2 | 测试头像上传 | 上传新头像并验证显示 | ⬜ |
| 3.3 | 测试头像清除 | 清除头像并验证 | ⬜ |
| 3.4 | 测试权限控制 | 非管理员不能访问接口 | ⬜ |

---

## 六、注意事项

1. **安全性**：
   - 所有接口都需要管理员认证
   - 上传文件需要验证类型和大小
   - 昵称长度限制在50字符以内

2. **用户体验**：
   - 头像上传显示进度条
   - 操作后自动刷新列表
   - 错误提示清晰明确

3. **兼容性**：
   - 图片预览使用 FileReader API
   - 进度条使用 XMLHttpRequest 的 progress 事件

4. **性能优化**：
   - 头像文件压缩存储
   - 使用 CDN 加速图片访问

---

## 七、预计工时

| 阶段 | 任务 | 预计工时 |
|------|------|----------|
| 后端开发 | DTO、Controller、Service | 2 小时 |
| 前端开发 | HTML、CSS、JS | 3 小时 |
| 测试验证 | 功能测试 | 1 小时 |
| **合计** | | **6 小时** |

---

*文档版本：v1.0*
*最后更新：2026-04-07*
