// 全局应用JavaScript
(function() {
    'use strict';

    const TAG_PRIORITY_PATTERNS = [
        /内部人员/,
        /蓝V认证/,
        /黄V认证/
    ];

    function pickTopIdentityTagFromList(tags = []) {
        if (!Array.isArray(tags) || tags.length === 0) {
            return '';
        }
        for (const pattern of TAG_PRIORITY_PATTERNS) {
            const matched = tags.find(tag => typeof tag === 'string' && pattern.test(tag));
            if (matched) {
                return matched;
            }
        }
        return tags.find(tag => typeof tag === 'string' && tag.trim().length) || '';
    }

    window.resolveIdentityTag = function(target) {
        if (!target) {
            return '';
        }
        if (typeof target === 'object' && target.displayIdentityTag) {
            return target.displayIdentityTag;
        }
        if (Array.isArray(target)) {
            return pickTopIdentityTagFromList(target);
        }
        if (Array.isArray(target.identityTags)) {
            return pickTopIdentityTagFromList(target.identityTags);
        }
        return '';
    };

    window.isPaidUser = function() {
        if (window.Android && Android.isPaidUser) {
            try {
                return !!Android.isPaidUser();
            } catch (e) {
                console.warn('读取付费状态失败', e);
            }
        }
        return false;
    };

    window.openRechargePage = function() {
        if (window.Android && Android.navigateToRecharge) {
            Android.navigateToRecharge();
        } else {
            window.location.href = 'upgrade_service.html';
        }
    };

    window.getUserProfile = function() {
        if (window.Android && Android.getUserProfile) {
            try {
                const payload = Android.getUserProfile();
                const parsed = typeof payload === 'string' ? JSON.parse(payload) : payload;
                return {
                    phone: parsed.phone || '',
                    nickname: parsed.nickname || '',
                    avatarUrl: parsed.avatarUrl || '',
                    isPaid: !!parsed.isPaid,
                    hasChildProfile: !!parsed.hasChildProfile,
                    isLoggedIn: !!parsed.isLoggedIn
                };
            } catch (e) {
                console.warn('读取用户信息失败', e);
            }
        }
        return {
            phone: '',
            nickname: '',
            avatarUrl: '',
            isPaid: false,
            hasChildProfile: false,
            isLoggedIn: false
        };
    };

    const PAGE_TAB_MAP = {
        'index.html': 'home',
        'consult.html': 'home',
        'consultant_list.html': 'home',
        'consultant_detail.html': 'home',
        'appointment.html': 'home',
        'message.html': 'message',
        'chat.html': 'message',
        'rating.html': 'message',
        'consultant_training.html': 'profile',
        'consultant_exam.html': 'profile',
        'upgrade_service.html': 'profile',
        'profile.html': 'profile',
        'child_info.html': 'profile',
        'auth.html': 'profile',
        'recharge.html': 'profile',
        'account_manage.html': 'profile'
    };
    const PRIMARY_PAGES = ['index.html', 'message.html', 'profile.html'];

    function getCurrentPageName() {
        const page = window.location.pathname.split('/').pop();
        return page && page.length ? page : 'index.html';
    }

    // 获取Token
    window.getToken = function() {
        if (window.Android && Android.getToken) {
            return Android.getToken();
        }
        return localStorage.getItem('token') || '';
    };

    // API Base URL - 优先从 Android 接口获取（运行时返回正确的主机地址）
    window.API_BASE_URL = (function() {
        try {
            if (window.Android && typeof Android.getBaseUrl === 'function') {
                var url = Android.getBaseUrl();
                // Android 端返回的 URL 已经是 http://host:port/api 格式，直接使用
                if (url && typeof url === 'string') {
                    return url;
                }
            }
        } catch (e) { /* ignore */ }
        // 模拟器 fallback：adb reverse tcp:8080 tcp:8080
        // 注意：后端配置 context-path 为 /api，所以这里要包含 /api
        return 'http://127.0.0.1:8080/api';
    })();

    // 带认证的Fetch
    window.fetchWithAuth = async function(url, options = {}) {
        const token = window.getToken();
        console.log('fetchWithAuth - Token:', token ? token.substring(0, 20) + '...' : 'empty');
        
        // 确保 headers 是对象
        const headers = {};
        if (options.headers) {
            Object.keys(options.headers).forEach(key => {
                headers[key] = options.headers[key];
            });
        }

        // 添加 Authorization header
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }

        // 如果是相对路径，拼接 Base URL
        // 注意：API_BASE_URL 已经是 http://host:port/api 格式，所以直接拼接
        let fullUrl = url;
        if (url.startsWith('/')) {
            fullUrl = window.API_BASE_URL + url;
        }
        
        console.log('fetchWithAuth - URL:', fullUrl);

        const newOptions = {
            method: options.method || 'GET',
            headers: headers,
            body: options.body,
            mode: 'cors'
        };

        return fetch(fullUrl, newOptions);
    };

    function resolveActiveTab() {
        const currentPage = getCurrentPageName();
        if (PAGE_TAB_MAP[currentPage]) {
            return PAGE_TAB_MAP[currentPage];
        }
        return PAGE_TAB_MAP['index.html'];
    }

    // 页面导航
    window.navigateTo = function(page) {
        if (window.Android) {
            switch(page) {
                case 'consult':
                    // 直接跳转到咨询师列表，跳过问题选择页面
                    Android.navigateToConsultantList();
                    break;
                case 'chat':
                    // 需要传递appointmentId
                    break;
                default:
                    console.log('Navigate to:', page);
            }
        }
    };

    window.navigateToConsultantDetail = function(name) {
        if (window.Android && Android.navigateToConsultantDetail) {
            Android.navigateToConsultantDetail(name);
        } else {
            window.location.href = 'consultant_detail.html?name=' + encodeURIComponent(name);
        }
    };

    // 显示Toast
    window.showToast = function(message) {
        if (window.Android) {
            Android.showToast(message);
        } else {
            console.log('Toast:', message);
        }
    };

    // 更新用户头像和昵称
    window.updateUserProfile = async function(nickname, avatarUrl) {
        try {
            const response = await window.fetchWithAuth('/user/profile', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ nickname, avatarUrl })
            });
            const result = await response.json();
            if (result.code === 200) {
                return result.data;
            } else {
                throw new Error(result.message || '更新失败');
            }
        } catch (e) {
            console.error('更新用户信息失败:', e);
            throw e;
        }
    };

    // 获取预约列表
    window.getAppointments = function() {
        if (window.Android) {
            try {
                const json = Android.getAppointments();
                return JSON.parse(json);
            } catch (e) {
                console.error('Error getting appointments:', e);
                return [];
            }
        }
        return [];
    };

    // 点击反馈动画
    document.addEventListener('DOMContentLoaded', function() {
        // 为所有可点击元素添加点击反馈
        const clickableElements = document.querySelectorAll('.card:not(.static-card), .btn, .list-item, .nav-item, .consultant-item');
        clickableElements.forEach(el => {
            el.addEventListener('touchstart', function() {
                this.style.transition = 'transform 0.1s';
                this.style.transform = 'scale(0.98)';
            });
            el.addEventListener('touchend', function() {
                this.style.transform = 'scale(1)';
            });
        });
    });
    window.addEventListener('pageshow', setupBackButton);

    // 返回按钮
    window.goBack = function() {
        let handled = false;
        try {
            if (window.Android && typeof Android.goBack === 'function') {
                Android.goBack();
                handled = true;
            }
        } catch (err) {
            console.warn('Android goBack failed', err);
            handled = false;
        }
        if (!handled) {
            if (window.history.length > 1) {
                window.history.back();
            } else {
                window.location.href = 'index.html';
            }
        }
    };

    function isPrimaryPage() {
        const page = getCurrentPageName();
        return PRIMARY_PAGES.includes(page);
    }

    function setupBackButton() {
        const toolbar = document.querySelector('.top-app-bar');
        if (!toolbar) {
            return;
        }
        const existingStatic = toolbar.querySelectorAll('.back-btn');
        existingStatic.forEach(btn => {
            if (!btn.classList.contains('dynamic-back-btn')) {
                btn.remove();
            }
        });
        const existingDynamic = toolbar.querySelector('.dynamic-back-btn');
        if (existingDynamic) {
            existingDynamic.remove();
        }
        if (isPrimaryPage()) {
            return;
        }
        const button = document.createElement('button');
        button.className = 'back-btn dynamic-back-btn';
        button.innerHTML = `
            <svg class="nav-icon" viewBox="0 0 24 24">
                <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
            </svg>
        `;
        button.addEventListener('click', goBack);
        toolbar.insertBefore(button, toolbar.firstChild);
    }

    const NAV_TABS = [
        {
            id: 'home',
            label: '首页',
            href: 'index.html',
            icon: '<path d="M3 10.5L12 3l9 7.5V21a1 1 0 0 1-1 1h-6v-6H10v6H4a1 1 0 0 1-1-1z"/>'
        },
        {
            id: 'message',
            label: '消息',
            href: 'message.html',
            icon: '<path d="M21 6H3a1 1 0 0 0-1 1v12l4-4h15a1 1 0 0 0 1-1V7a1 1 0 0 0-1-1z"/>'
        },
        {
            id: 'profile',
            label: '我的',
            href: 'profile.html',
            icon: '<circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 4-7 8-7s8 3 8 7"/>'
        }
    ];

    window.renderBottomNav = function(activeTab) {
        // 原底部导航栏代码已移除，使用 Android 原生底部导航栏
    };
})();
