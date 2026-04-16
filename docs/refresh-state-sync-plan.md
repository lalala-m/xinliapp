# 手机端状态刷新和同步功能修复计划

> 文档版本：v1.0  
> 创建日期：2026-04-07  
> 项目：同阳缘心理健康咨询系统  

---

## 一、问题分析

### 1.1 当前存在的问题

| # | 问题 | 优先级 | 状态 |
|---|------|--------|------|
| 1 | 头像编辑按钮点击无响应 | P0 | ❌ 未修复 |
| 2 | 用户名修改显示失败 | P0 | ❌ 未修复 |
| 3 | 登录状态改变后"我的"页面不更新 | P1 | ❌ 未修复 |
| 4 | 刷新功能不完整 | P1 | ❌ 未修复 |

### 1.2 根本原因分析

1. **头像编辑问题**：可能是点击事件未绑定或绑定到了错误的选择器
2. **用户名修改失败**：后端 API 可能返回了错误或前端调用路径不对
3. **状态不更新**：缺少状态变化监听机制

---

## 二、修复计划

### 阶段一：问题诊断（预计 30 分钟）

#### 任务 1.1：检查 profile.html 页面结构

**文件：** `TongYangYuan/app/src/main/assets/profile.html`

检查内容：
- 头像编辑按钮是否存在
- 点击事件绑定是否正确
- 表单元素ID是否正确

#### 任务 1.2：检查 app.js 中的头像编辑逻辑

**文件：** `TongYangYuan/app/src/main/assets/js/app.js`

检查内容：
- `showEditProfileModal()` 函数是否存在
- `updateUserProfile()` 函数是否存在
- API 调用路径是否正确

#### 任务 1.3：检查后端 UserController 接口

**文件：** `TongYangYuan-Server/src/main/java/com/tongyangyuan/mentalhealth/controller/UserController.java`

检查内容：
- `/user/profile` PUT 接口是否存在
- 返回的响应格式是否正确

---

### 阶段二：修复头像编辑功能（预计 1 小时）

#### 任务 2.1：修复 profile.html 头像编辑按钮

```html
<!-- 确保按钮存在且绑定正确事件 -->
<div class="profile-avatar-container">
    <img id="avatarImg" src="" alt="头像">
    <button class="edit-avatar-btn" onclick="showEditProfileModal()">
        <span>编辑</span>
    </button>
</div>
```

#### 任务 2.2：新增头像编辑模态框

```html
<!-- 头像编辑模态框 -->
<div id="editProfileModal" class="modal">
    <div class="modal-content">
        <span class="close" onclick="closeEditProfileModal()">&times;</span>
        <h2>编辑资料</h2>
        <form id="editProfileForm">
            <div class="form-group">
                <label>头像</label>
                <div class="avatar-edit-area">
                    <img id="editAvatarPreview" src="" alt="头像预览">
                    <input type="file" id="avatarInput" accept="image/*" 
                           onchange="previewAvatar(this)">
                    <button type="button" onclick="document.getElementById('avatarInput').click()">
                        选择图片
                    </button>
                </div>
            </div>
            <div class="form-group">
                <label for="editNickname">昵称</label>
                <input type="text" id="editNickname" maxlength="50">
            </div>
            <div class="modal-actions">
                <button type="button" class="btn-secondary" onclick="closeEditProfileModal()">取消</button>
                <button type="submit" class="btn-primary" onclick="saveProfile(event)">保存</button>
            </div>
        </form>
    </div>
</div>
```

#### 任务 2.3：实现头像上传和资料保存

```javascript
/**
 * 显示编辑资料模态框
 */
function showEditProfileModal() {
    const modal = document.getElementById('editProfileModal');
    const user = getCurrentUser();
    
    if (user) {
        document.getElementById('editNickname').value = user.nickname || '';
        document.getElementById('editAvatarPreview').src = user.avatarUrl || '';
    }
    
    modal.classList.add('show');
}

/**
 * 关闭编辑资料模态框
 */
function closeEditProfileModal() {
    const modal = document.getElementById('editProfileModal');
    modal.classList.remove('show');
}

/**
 * 预览选择的图片
 */
function previewAvatar(input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('editAvatarPreview').src = e.target.result;
        };
        reader.readAsDataURL(input.files[0]);
    }
}

/**
 * 保存用户资料
 */
async function saveProfile(event) {
    event.preventDefault();
    
    const nickname = document.getElementById('editNickname').value.trim();
    const avatarInput = document.getElementById('avatarInput');
    
    try {
        // 如果选择了新头像，先上传
        let avatarUrl = getCurrentUser()?.avatarUrl || '';
        
        if (avatarInput.files && avatarInput.files[0]) {
            avatarUrl = await uploadAvatar(avatarInput.files[0]);
        }
        
        // 保存资料
        const response = await fetch(`${API_BASE}/user/profile`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${getToken()}`
            },
            body: JSON.stringify({ nickname, avatarUrl })
        });
        
        const result = await response.json();
        
        if (result.code === 200) {
            // 更新本地存储的用户信息
            updateLocalUser(result.data);
            // 刷新页面显示
            loadUserProfile();
            closeEditProfileModal();
            showToast('保存成功');
        } else {
            showToast(result.message || '保存失败');
        }
    } catch (error) {
        console.error('保存失败:', error);
        showToast('保存失败，请重试');
    }
}

/**
 * 上传头像
 */
async function uploadAvatar(file) {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await fetch(`${API_BASE}/upload/avatar`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${getToken()}`
        },
        body: formData
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
        return result.data.url;
    } else {
        throw new Error(result.message || '头像上传失败');
    }
}
```

---

### 阶段三：实现登录状态变化监听（预计 1 小时）

#### 任务 3.1：创建全局状态管理

**文件：** `TongYangYuan/app/src/main/assets/js/state-manager.js`

```javascript
/**
 * 全局状态管理器
 */
class StateManager {
    constructor() {
        this.listeners = [];
        this.currentState = {
            isLoggedIn: false,
            user: null
        };
        
        // 初始化状态
        this.init();
    }
    
    init() {
        // 从 localStorage 恢复状态
        const token = localStorage.getItem('token');
        const userStr = localStorage.getItem('user');
        
        if (token && userStr) {
            this.currentState.isLoggedIn = true;
            this.currentState.user = JSON.parse(userStr);
        }
        
        // 监听 storage 变化（跨页面）
        window.addEventListener('storage', (e) => {
            if (e.key === 'token' || e.key === 'user') {
                this.refreshState();
            }
        });
    }
    
    refreshState() {
        const token = localStorage.getItem('token');
        const userStr = localStorage.getItem('user');
        
        const newState = {
            isLoggedIn: !!token,
            user: userStr ? JSON.parse(userStr) : null
        };
        
        // 检查状态是否变化
        if (JSON.stringify(newState) !== JSON.stringify(this.currentState)) {
            this.currentState = newState;
            this.notify();
        }
    }
    
    subscribe(listener) {
        this.listeners.push(listener);
        return () => {
            this.listeners = this.listeners.filter(l => l !== listener);
        };
    }
    
    notify() {
        this.listeners.forEach(listener => listener(this.currentState));
    }
    
    getState() {
        return this.currentState;
    }
}

// 创建全局实例
window.stateManager = new StateManager();
```

#### 任务 3.2：在 profile.html 中使用状态管理器

```javascript
// 在 profile.html 的 JS 中添加

// 订阅状态变化
stateManager.subscribe((state) => {
    if (!state.isLoggedIn) {
        // 用户已退出登录，跳转到首页
        window.location.href = 'index.html';
    } else {
        // 用户登录状态变化，刷新页面
        loadUserProfile();
        updateProfileDisplay();
    }
});

// 页面加载时检查状态
document.addEventListener('DOMContentLoaded', () => {
    const state = stateManager.getState();
    if (!state.isLoggedIn) {
        showLoginPrompt();
    } else {
        loadUserProfile();
    }
});
```

#### 任务 3.3：实现退出登录功能

```javascript
/**
 * 退出登录
 */
function logout() {
    // 清除本地存储
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('consultantToken');
    
    // 通知状态变化
    stateManager.refreshState();
    
    // 跳转到首页
    window.location.href = 'index.html';
}
```

---

### 阶段四：完善刷新功能（预计 30 分钟）

#### 任务 4.1：实现完整的页面刷新

```javascript
/**
 * 完整刷新用户资料页面
 */
async function refreshProfilePage() {
    try {
        // 1. 重新获取用户信息
        const userInfo = await fetchUserInfo();
        
        // 2. 更新本地存储
        updateLocalUser(userInfo);
        
        // 3. 更新页面显示
        updateProfileDisplay();
        
        // 4. 更新头像
        updateAvatarDisplay();
        
        // 5. 更新统计数据（如有）
        await updateStatistics();
        
        showToast('刷新成功');
    } catch (error) {
        console.error('刷新失败:', error);
        showToast('刷新失败，请重试');
    }
}

/**
 * 获取用户信息
 */
async function fetchUserInfo() {
    const response = await fetch(`${API_BASE}/user/profile`, {
        headers: {
            'Authorization': `Bearer ${getToken()}`
        }
    });
    
    const result = await response.json();
    
    if (result.code === 200) {
        return result.data;
    } else {
        throw new Error(result.message || '获取用户信息失败');
    }
}

/**
 * 更新本地存储的用户信息
 */
function updateLocalUser(user) {
    if (user) {
        localStorage.setItem('user', JSON.stringify(user));
    }
}

/**
 * 更新页面显示
 */
function updateProfileDisplay() {
    const user = getCurrentUser();
    
    if (user) {
        // 更新昵称
        const nicknameEl = document.getElementById('nicknameDisplay');
        if (nicknameEl) {
            nicknameEl.textContent = user.nickname || '未设置昵称';
        }
        
        // 更新手机号
        const phoneEl = document.getElementById('phoneDisplay');
        if (phoneEl) {
            phoneEl.textContent = user.phone || '';
        }
        
        // 更新用户类型
        const userTypeEl = document.getElementById('userTypeDisplay');
        if (userTypeEl) {
            userTypeEl.textContent = getUserTypeText(user.userType);
        }
    }
}

/**
 * 更新头像显示
 */
function updateAvatarDisplay() {
    const user = getCurrentUser();
    
    if (user && user.avatarUrl) {
        const avatarEls = document.querySelectorAll('.user-avatar');
        avatarEls.forEach(el => {
            el.src = user.avatarUrl;
        });
    }
}
```

---

## 三、任务清单

### 阶段一：问题诊断

| # | 任务 | 优先级 | 状态 |
|---|------|--------|------|
| 1.1 | 检查 profile.html 页面结构 | P0 | ⬜ |
| 1.2 | 检查 app.js 中的头像编辑逻辑 | P0 | ⬜ |
| 1.3 | 检查后端 UserController 接口 | P0 | ⬜ |

### 阶段二：修复头像编辑功能

| # | 任务 | 优先级 | 状态 |
|---|------|--------|------|
| 2.1 | 修复 profile.html 头像编辑按钮 | P0 | ⬜ |
| 2.2 | 新增头像编辑模态框 | P0 | ⬜ |
| 2.3 | 实现头像上传功能 | P0 | ⬜ |
| 2.4 | 实现资料保存功能 | P0 | ⬜ |

### 阶段三：实现登录状态变化监听

| # | 任务 | 优先级 | 状态 |
|---|------|--------|------|
| 3.1 | 创建全局状态管理器 | P1 | ⬜ |
| 3.2 | 在 profile.html 中使用状态管理器 | P1 | ⬜ |
| 3.3 | 实现退出登录功能 | P1 | ⬜ |

### 阶段四：完善刷新功能

| # | 任务 | 优先级 | 状态 |
|---|------|--------|------|
| 4.1 | 实现完整的页面刷新 | P1 | ⬜ |
| 4.2 | 添加下拉刷新功能 | P2 | ⬜ |
| 4.3 | 添加自动刷新机制 | P2 | ⬜ |

---

## 四、预计工时

| 阶段 | 任务 | 预计工时 |
|------|------|----------|
| 问题诊断 | 分析现有代码 | 30 分钟 |
| 修复头像编辑 | 修复按钮、模态框、上传 | 1 小时 |
| 状态监听 | 全局状态管理器 | 1 小时 |
| 刷新功能 | 完整刷新逻辑 | 30 分钟 |
| **合计** | | **3 小时** |

---

## 五、测试计划

### 5.1 功能测试

| # | 测试项 | 预期结果 |
|---|--------|----------|
| 1 | 点击头像编辑按钮 | 弹出编辑模态框 |
| 2 | 选择新头像 | 显示预览 |
| 3 | 修改昵称并保存 | 保存成功，页面更新 |
| 4 | 上传新头像 | 上传成功，显示新头像 |
| 5 | 退出登录 | 跳转到首页 |
| 6 | 重新登录 | 页面刷新，显示新用户信息 |
| 7 | 点击刷新按钮 | 页面完全刷新 |

### 5.2 边界测试

| # | 测试项 | 预期结果 |
|---|--------|----------|
| 1 | 不选择头像直接保存 | 仅保存昵称 |
| 2 | 昵称为空 | 提示不能为空 |
| 3 | 上传超大图片 | 提示图片过大 |
| 4 | Token 过期 | 跳转登录页 |

---

## 六、注意事项

1. **安全性**：
   - 所有 API 调用需要携带 Token
   - 上传文件需要验证类型和大小
   - 敏感操作需要二次确认

2. **用户体验**：
   - 操作过程中显示 loading 状态
   - 错误提示清晰明确
   - 成功/失败都有反馈

3. **兼容性**：
   - 兼容 Android 5.0+
   - 兼容各种屏幕尺寸
   - 兼容离线状态（显示缓存数据）

4. **性能优化**：
   - 头像图片压缩后再上传
   - 使用懒加载
   - 减少不必要的网络请求

---

*文档版本：v1.0*
*最后更新：2026-04-07*
