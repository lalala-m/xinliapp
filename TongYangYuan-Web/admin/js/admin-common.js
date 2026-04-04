// 管理后台通用功能
const AdminCommon = {
    scrollLockCount: 0,
    // 获取token
    getToken() {
        return localStorage.getItem('token');
    },

    // 检查登录状态
    checkAuth() {
        const token = this.getToken();
        if (!token) {
            window.location.href = 'login.html';
            return false;
        }
        
        // 检查是否是管理员
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        if (userInfo.role !== 'ADMIN') {
            alert('权限不足，需要管理员权限');
            window.location.href = '../dashboard.html';
            return false;
        }
        
        return true;
    },

    // 设置管理员信息
    setAdminInfo() {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        const adminNameEl = document.getElementById('adminName');
        if (adminNameEl && userInfo.name) {
            adminNameEl.textContent = userInfo.name;
        }
    },

    // API请求封装
    async request(url, options = {}) {
        const token = this.getToken();
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        };

        const finalOptions = {
            ...defaultOptions,
            ...options,
            headers: {
                ...defaultOptions.headers,
                ...options.headers
            }
        };

        try {
            // Use CONFIG.API_BASE_URL
            // 修改：后端接口没有 /api 前缀，直接使用 http://localhost:8080
            // 或者如果 CONFIG.API_BASE_URL 是 http://localhost:8080/api，我们需要去除 /api
            
            let baseUrl = (typeof CONFIG !== 'undefined' && CONFIG.API_BASE_URL) ? CONFIG.API_BASE_URL : 'http://localhost:8080';
            
            // 确保 baseUrl 不以 / 结尾
            if (baseUrl.endsWith('/')) {
                baseUrl = baseUrl.substring(0, baseUrl.length - 1);
            }

            // 临时修复：如果 base url 包含 /api 但请求 url 以 /admin 开头，则去除 /api
            // if (baseUrl.endsWith('/api') && url.startsWith('/admin')) {
            //    baseUrl = baseUrl.substring(0, baseUrl.length - 4);
            // }
            
            // Handle url that already has /api prefix if base url also has it
            let requestUrl = url;
            // 移除开头的 / 防止双斜杠
            if (requestUrl.startsWith('/')) {
                requestUrl = requestUrl.substring(1);
            }

            // 如果 baseUrl 不包含 /api 但 url 以 api/ 开头，需要添加
            const needsApiPrefix = !baseUrl.endsWith('/api') && requestUrl.startsWith('api/');
            if (needsApiPrefix) {
                baseUrl = baseUrl + '/api';
            }
            
            const fullUrl = baseUrl + '/' + requestUrl;
            console.log('Full Request URL:', fullUrl);
            
            const response = await fetch(fullUrl, finalOptions);
            
            if (response.status === 401) {
                console.error("Authentication failed: 401 Unauthorized");
                // Don't alert immediately, check if it's login page
                if (!window.location.href.includes('login.html')) {
                    alert('登录已过期，请重新登录');
                    localStorage.clear();
                    window.location.href = 'login.html';
                }
                return null;
            }

            if (response.status === 403) {
                console.error("Access forbidden: 403 Forbidden");
                alert('权限不足');
                return null;
            }
            
            if (response.status === 500) {
                console.error("Server error: 500 Internal Server Error");
                const errorText = await response.text();
                console.error("Error details:", errorText);
                throw new Error("服务器内部错误");
            }

            const data = await response.json();
            
            if (!response.ok) {
                throw new Error(data.message || '请求失败');
            }

            return data;
        } catch (error) {
            console.error('API请求错误:', error);
            this.showError(error.message);
            return null;
        }
    },

    // 显示成功消息
    showSuccess(message) {
        this.showToast(message, 'success');
    },

    // 显示错误消息
    showError(message) {
        this.showToast(message, 'error');
    },

    // 显示提示消息
    showToast(message, type = 'info') {
        // 创建toast元素
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            background: ${type === 'success' ? '#4CAF50' : type === 'error' ? '#f44336' : '#2196F3'};
            color: white;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.2);
            z-index: 10000;
            animation: slideInRight 0.3s;
        `;

        document.body.appendChild(toast);

        // 3秒后自动移除
        setTimeout(() => {
            toast.style.animation = 'slideOutRight 0.3s';
            setTimeout(() => {
                document.body.removeChild(toast);
            }, 300);
        }, 3000);
    },

    // 格式化日期
    formatDate(dateString) {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    // 格式化日期（仅日期）
    formatDateOnly(dateString) {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('zh-CN');
    },

    // 确认对话框
    confirm(message) {
        return window.confirm(message);
    },

    lockBodyScroll() {
        const body = document.body;
        if (this.scrollLockCount === 0) {
            body.dataset.prevOverflow = body.style.overflow || '';
            body.dataset.prevPaddingRight = body.style.paddingRight || '';
            const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth;
            body.style.overflow = 'hidden';
            body.style.paddingRight = scrollbarWidth > 0 ? `${scrollbarWidth}px` : '';
        }
        this.scrollLockCount += 1;
    },

    unlockBodyScroll() {
        if (this.scrollLockCount === 0) {
            return;
        }
        this.scrollLockCount -= 1;
        if (this.scrollLockCount === 0) {
            const body = document.body;
            body.style.overflow = body.dataset.prevOverflow || '';
            body.style.paddingRight = body.dataset.prevPaddingRight || '';
            delete body.dataset.prevOverflow;
            delete body.dataset.prevPaddingRight;
        }
    },

    // 显示加载状态
    showLoading(element) {
        if (element) {
            element.innerHTML = '<td colspan="100" class="loading">加载中...</td>';
        }
    },

    // 显示空状态
    showEmpty(element, message = '暂无数据') {
        if (element) {
            element.innerHTML = `<td colspan="100" class="loading">${message}</td>`;
        }
    },

    // 获取角色显示名称
    getRoleName(role) {
        const roleMap = {
            'ADMIN': '管理员',
            'PARENT': '家长',
            'CONSULTANT': '咨询师'
        };
        return roleMap[role] || role;
    },

    // 获取状态显示名称
    getStatusName(status) {
        const statusMap = {
            'PENDING': '待确认',
            'ACCEPTED': '已确认',
            'IN_PROGRESS': '进行中',
            'COMPLETED': '已完成',
            'CANCELLED': '已取消',
            'ACTIVE': '在线',
            'BUSY': '忙碌',
            'OFFLINE': '离线'
        };
        return statusMap[status] || status;
    },

    // 获取状态样式类
    getStatusClass(status) {
        const classMap = {
            'PENDING': 'pending',
            'CONFIRMED': 'active',
            'COMPLETED': 'active',
            'CANCELLED': 'inactive',
            'ACTIVE': 'active',
            'BUSY': 'pending',
            'OFFLINE': 'inactive',
            'true': 'active',
            'false': 'inactive'
        };
        return classMap[status] || '';
    },

    // 记录管理员操作
    async logAction(action, details) {
        try {
            await this.request('/api/admin/logs', {
                method: 'POST',
                body: JSON.stringify({
                    action,
                    details,
                    timestamp: new Date().toISOString()
                })
            });
        } catch (error) {
            console.error('记录操作日志失败:', error);
        }
    }
};

// 页面加载时检查权限
document.addEventListener('DOMContentLoaded', () => {
    if (!AdminCommon.checkAuth()) {
        return;
    }
    
    AdminCommon.setAdminInfo();

    // 退出登录
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (AdminCommon.confirm('确定要退出登录吗？')) {
                AdminCommon.logAction('LOGOUT', '管理员退出登录');
                localStorage.clear();
                window.location.href = 'login.html';
            }
        });
    }

    const modals = document.querySelectorAll('.modal');
    modals.forEach((modal) => {
        const content = modal.querySelector('.modal-content');
        if (!content) {
            return;
        }
        content.addEventListener('wheel', (e) => {
            if (!modal.classList.contains('show') && !modal.classList.contains('active')) {
                return;
            }
            if (content.scrollHeight <= content.clientHeight) {
                return;
            }
            content.scrollTop += e.deltaY;
            e.preventDefault();
        }, { passive: false });
    });
});

// 添加CSS动画
(function() {
    const style = document.createElement('style');
    style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
    document.head.appendChild(style);
})();
