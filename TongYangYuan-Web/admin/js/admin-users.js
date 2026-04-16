// 用户管理功能
let currentPage = 1;
let pageSize = 10;
let totalPages = 1;
let users = [];
let currentUser = null;

// 加载用户列表
async function loadUsers() {
    const tableBody = document.getElementById('userTableBody');
    AdminCommon.showLoading(tableBody);

    try {
        const params = new URLSearchParams({
            page: currentPage,
            size: pageSize,
            userType: 'PARENT' // 只加载家长
        });

        // 添加筛选条件
        // const roleFilter = document.getElementById('roleFilter')?.value;
        const statusFilter = document.getElementById('statusFilter')?.value;
        const searchInput = document.getElementById('searchInput')?.value;

        // if (roleFilter) params.append('role', roleFilter);
        if (statusFilter) params.append('status', statusFilter);
        if (searchInput) params.append('search', searchInput);

        // 请求后端获取所有用户
        const response = await AdminCommon.request(`/admin/users?${params.toString()}`);
        
        if (response && response.code === 200) {
            const allUsers = response.data || [];
            window.allUsers = allUsers; // Store all users for client-side pagination if needed
            
            // 前端实现筛选
            let filteredUsers = allUsers;
            
            // roleFilter is hardcoded to PARENT
            
            if (statusFilter) {
                 // Assuming statusFilter values match backend enum strings (ACTIVE, INACTIVE, BANNED)
                filteredUsers = filteredUsers.filter(u => u.status === statusFilter);
            }
            
            if (searchInput) {
                const search = searchInput.toLowerCase();
                filteredUsers = filteredUsers.filter(u => 
                    (u.phone && u.phone.includes(search)) || 
                    (u.nickname && u.nickname.toLowerCase().includes(search))
                );
            }
            
            // 计算分页
            totalPages = Math.ceil(filteredUsers.length / pageSize);
            if (totalPages === 0) totalPages = 1;
            
            // 确保当前页不越界
            if (currentPage > totalPages) currentPage = totalPages;
            
            // 切片获取当前页数据
            const start = (currentPage - 1) * pageSize;
            const end = start + pageSize;
            users = filteredUsers.slice(start, end);
            
            renderUsers();
            updatePagination();
        } else {
             AdminCommon.showEmpty(tableBody, '获取数据失败');
        }
    } catch (error) {
        console.error('加载用户列表失败:', error);
        AdminCommon.showEmpty(tableBody, '加载失败');
    }
}

// 渲染用户列表
function renderUsers() {
    const tableBody = document.getElementById('userTableBody');
    
    if (!users || users.length === 0) {
        AdminCommon.showEmpty(tableBody);
        return;
    }

    tableBody.innerHTML = users.map(user => {
        // Format children info
        let childrenStr = '-';
        if (user.children && user.children.length > 0) {
            childrenStr = user.children.map(c => 
                `<div>${c.name} <small class="text-muted">(${c.gender === 'BOY' ? '男' : '女'}, ${c.age}岁)</small></div>`
            ).join('');
        }

        return `
        <tr>
            <td>${user.id}</td>
            <td>${user.phone}</td>
            <td>${user.nickname || '-'}</td>
            <td>${childrenStr}</td>
            <td><div class="ellipsis-text" title="${user.keyIssues || ''}" style="max-width: 150px;">${user.keyIssues || '-'}</div></td>
            <td>${user.currentConsultant || '-'}</td>
            <td><span class="status-badge ${AdminCommon.getStatusClass(user.status)}">${user.status === 'ACTIVE' ? '启用' : '禁用'}</span></td>
            <td>${AdminCommon.formatDate(user.gmtCreate)}</td>
            <td>
                <div class="action-btns">
                    <button class="btn-sm btn-view" onclick="viewUser(${user.id})">查看</button>
                    <button class="btn-sm btn-edit" onclick="editUser(${user.id})">编辑</button>
                    <button class="btn-sm btn-delete" onclick="deleteUser(${user.id})">删除</button>
                </div>
            </td>
        </tr>
    `}).join('');
}

// 更新分页
function updatePagination() {
    document.getElementById('pageInfo').textContent = `第 ${currentPage} 页 / 共 ${totalPages} 页`;
    document.getElementById('prevBtn').disabled = currentPage <= 1;
    document.getElementById('nextBtn').disabled = currentPage >= totalPages;
}

// 上一页
function prevPage() {
    if (currentPage > 1) {
        currentPage--;
        // loadUsers();
        refreshPageData();
    }
}

// 下一页
function nextPage() {
    if (currentPage < totalPages) {
        currentPage++;
        // loadUsers();
        refreshPageData();
    }
}

// 刷新当前页数据（前端分页）
function refreshPageData() {
    if (window.allUsers) {
        const start = (currentPage - 1) * pageSize;
        const end = start + pageSize;
        users = window.allUsers.slice(start, end);
        renderUsers();
        updatePagination();
    } else {
        loadUsers();
    }
}

// 搜索用户
function searchUsers() {
    currentPage = 1;
    loadUsers();
}

// 筛选用户
function filterUsers() {
    currentPage = 1;
    loadUsers();
}

// 刷新用户列表
function refreshUsers() {
    currentPage = 1;
    loadUsers();
    AdminCommon.showSuccess('已刷新');
}

// 查看用户详情
function viewUser(userId) {
    // Try to find in current list first since we have enriched data
    const user = users.find(u => u.id === userId);
    if (user) {
        currentUser = user;
        showUserModal(user);
    } else {
        // Fallback to API
        AdminCommon.request(`/admin/users/${userId}`).then(u => {
            if (u) {
                currentUser = u;
                showUserModal(u);
            }
        }).catch(err => AdminCommon.showError('获取用户详情失败'));
    }
}

// 显示用户详情模态框
function showUserModal(user) {
    const modal = document.getElementById('userModal');
    const details = document.getElementById('userDetails');
    
    // Format children for modal
    let childrenHtml = '无';
    if (user.children && user.children.length > 0) {
        childrenHtml = user.children.map(c => 
            `<div class="child-info-card" style="background:#f5f7fa; padding:10px; margin-bottom:5px; border-radius:4px;">
                <strong>${c.name}</strong> (${c.gender === 'BOY' ? '男' : '女'}, ${c.age}岁)<br>
                <small>身体状况: ${c.bodyStatus || '无'}</small>
            </div>`
        ).join('');
    }

    details.innerHTML = `
        <div style="line-height: 2;">
            <p><strong>用户ID:</strong> ${user.id}</p>
            <p><strong>手机号:</strong> ${user.phone}</p>
            <p><strong>姓名:</strong> ${user.nickname || '-'}</p>
            <p><strong>角色:</strong> 家长</p>
            <p><strong>状态:</strong> ${user.status === 'ACTIVE' ? '启用' : '禁用'}</p>
            <p><strong>注册时间:</strong> ${AdminCommon.formatDate(user.gmtCreate)}</p>
            <p><strong>最后登录:</strong> ${AdminCommon.formatDate(user.lastLoginAt) || '-'}</p>
            <hr>
            <p><strong>当前咨询师:</strong> ${user.currentConsultant || '无'}</p>
            <p><strong>重点问题:</strong></p>
            <div style="background:#fff3cd; padding:8px; border-radius:4px; margin-bottom:10px;">${user.keyIssues || '无'}</div>
            <p><strong>孩子信息:</strong></p>
            ${childrenHtml}
        </div>
    `;
    
    modal.classList.add('show');
}

// 关闭模态框
function closeModal() {
    const modal = document.getElementById('userModal');
    modal.classList.remove('show');
    currentUser = null;
}

// ========== 用户编辑功能 ==========

let editAvatarChanged = false;

/**
 * 编辑用户 - 打开编辑模态框
 */
async function editUser(userId) {
    // 先关闭详情模态框
    closeModal();
    
    try {
        // 获取用户详情
        const user = users.find(u => u.id === userId) || window.allUsers?.find(u => u.id === userId);
        
        if (!user) {
            AdminCommon.showError('用户不存在');
            return;
        }
        
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
        modal.style.display = 'flex';
        modal.style.opacity = '1';
        modal.style.pointerEvents = 'auto';
        modal.classList.add('show', 'active');
        
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
    if (modal) {
        modal.classList.remove('show', 'active');
        modal.style.display = 'none';
        modal.style.opacity = '0';
        modal.style.pointerEvents = 'none';
    }
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
        // 如果头像URL有变化（即使是空字符串），也发送
        const currentUser = users.find(u => u.id === parseInt(userId)) || window.allUsers?.find(u => u.id === parseInt(userId));
        if (currentUser) {
            if (avatarUrl !== (currentUser.avatarUrl || '')) {
                requestData.avatarUrl = avatarUrl;
            }
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

// 切换用户状态
async function toggleUserStatus() {
    if (!currentUser) return;
    
    const action = currentUser.enabled ? '禁用' : '启用';
    if (!AdminCommon.confirm(`确定要${action}该用户吗？`)) return;

    try {
        await AdminCommon.request(`/admin/users/${currentUser.id}/toggle`, {
            method: 'PUT'
        });
        
        AdminCommon.showSuccess(`${action}成功`);
        AdminCommon.logAction('TOGGLE_USER_STATUS', `${action}用户 ${currentUser.phone}`);
        closeModal();
        loadUsers();
    } catch (error) {
        AdminCommon.showError(`${action}失败`);
    }
}

// 删除用户
async function deleteUser(userId) {
    if (!AdminCommon.confirm('确定要删除该用户吗？此操作不可恢复！')) return;

    try {
        await AdminCommon.request(`/admin/users/${userId}`, {
            method: 'DELETE'
        });
        
        AdminCommon.showSuccess('删除成功');
        AdminCommon.logAction('DELETE_USER', `删除用户 ID: ${userId}`);
        loadUsers();
    } catch (error) {
        AdminCommon.showError('删除失败');
    }
}

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', () => {
    loadUsers();
    
    // 搜索框回车事件
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchUsers();
            }
        });
    }
});

// 显示新增用户弹窗
function showAddUserModal() {
    console.log('showAddUserModal called');
    const modal = document.getElementById('addUserModal');
    if (modal) {
        document.getElementById('newPhone').value = '';
        document.getElementById('newNickname').value = '';
        document.getElementById('newPassword').value = '';
        document.getElementById('newRole').value = 'PARENT';
        modal.style.display = 'flex';
        modal.style.opacity = '1';
        modal.style.pointerEvents = 'auto';
        modal.classList.add('show', 'active');
    } else {
        console.error('addUserModal not found');
        alert('错误：找不到弹窗元素');
    }
}

// 关闭新增用户弹窗
function closeAddUserModal() {
    const modal = document.getElementById('addUserModal');
    if (modal) {
        modal.classList.remove('show', 'active');
        modal.style.display = 'none';
        modal.style.opacity = '0';
        modal.style.pointerEvents = 'none';
    }
}

// 保存新用户
async function saveUser() {
    const phone = document.getElementById('newPhone').value;
    const nickname = document.getElementById('newNickname').value;
    const password = document.getElementById('newPassword').value;
    const userType = document.getElementById('newRole').value;

    if (!phone) {
        alert('请输入手机号');
        return;
    }

    const userData = {
        phone,
        nickname,
        password: password || '123456', // 默认密码
        userType,
        status: 'ACTIVE'
    };

    try {
        const response = await AdminCommon.request('/admin/users', {
            method: 'POST',
            body: JSON.stringify(userData)
        });

        if (response && response.code === 200) {
            AdminCommon.showSuccess('创建用户成功');
            closeAddUserModal();
            refreshUsers();
        } else {
            // 如果 response.code 不是 200，AdminCommon.request 可能会抛出异常或返回 null/error
            // 这里处理非 200 的情况，如果 request 没有抛出异常
            AdminCommon.showError(response.message || '创建失败');
        }
    } catch (error) {
        console.error('创建用户失败:', error);
        AdminCommon.showError('创建用户失败: ' + error.message);
    }
}

// Expose functions to global scope
window.showAddUserModal = showAddUserModal;
window.closeAddUserModal = closeAddUserModal;
window.saveUser = saveUser;
window.editUser = editUser;
window.closeEditModal = closeEditModal;
window.handleAvatarSelect = handleAvatarSelect;
window.uploadAvatar = uploadAvatar;
window.clearAvatar = clearAvatar;
window.saveUserProfile = saveUserProfile;
window.getFullImageUrl = getFullImageUrl;
window.getAdminToken = getAdminToken;
