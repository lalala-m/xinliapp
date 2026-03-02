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
        AdminCommon.request(`/api/admin/users/${userId}`).then(u => {
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

// 编辑用户
function editUser(userId) {
    AdminCommon.showSuccess('编辑功能开发中...');
    // TODO: 实现编辑功能
}

// 切换用户状态
async function toggleUserStatus() {
    if (!currentUser) return;
    
    const action = currentUser.enabled ? '禁用' : '启用';
    if (!AdminCommon.confirm(`确定要${action}该用户吗？`)) return;

    try {
        await AdminCommon.request(`/api/admin/users/${currentUser.id}/toggle`, {
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
        await AdminCommon.request(`/api/admin/users/${userId}`, {
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
        const response = await AdminCommon.request('/api/admin/users', {
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
