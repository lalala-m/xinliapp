// 童养园咨询师端 - 配置文件

const CONFIG = {
    // 应用信息
    APP_NAME: '童养园咨询师端',
    VERSION: '1.0.0',

    // API 配置
    API_BASE_URL: 'http://localhost:8080/api',
    WS_BASE_URL: 'http://localhost:8080/api/ws',

    // 存储键名
    STORAGE_KEYS: {
        CONSULTANT: 'tyy_consultant',
        APPOINTMENTS: 'tyy_appointments',
        MESSAGES: 'tyy_messages',
        SETTINGS: 'tyy_settings',
        CONSULTATION_SESSION: 'tyy_consultation_session'
    },

    // 咨询师身份等级
    IDENTITY_TIER: {
        BRONZE: { name: '铜牌咨询师', color: '#CD7F32' },
        SILVER: { name: '银牌咨询师', color: '#C0C0C0' },
        GOLD: { name: '金牌咨询师', color: '#FFD700' },
        PLATINUM: { name: '白金咨询师', color: '#E5E4E2' }
    },

    // 消息类型
    MESSAGE_TYPE: {
        TEXT: 'text',
        IMAGE: 'image',
        VIDEO: 'video',
        AUDIO: 'audio',
        SYSTEM: 'system'
    },

    // 预约状态
    APPOINTMENT_STATUS: {
        PENDING: 'pending',      // 待处理
        ACCEPTED: 'accepted',    // 已接受
        IN_PROGRESS: 'in_progress', // 进行中
        COMPLETED: 'completed',  // 已完成
        CANCELLED: 'cancelled'   // 已取消
    },

    // 时间格式
    DATE_FORMAT: 'YYYY-MM-DD',
    TIME_FORMAT: 'HH:mm',
    DATETIME_FORMAT: 'YYYY-MM-DD HH:mm:ss',

    // 默认头像颜色
    AVATAR_COLORS: [
        '#6FA6F8', '#FF6B6B', '#4ECDC4', '#45B7D1',
        '#FFA07A', '#98D8C8', '#F7DC6F', '#BB8FCE'
    ]
};

// 工具函数
const Utils = {
    // 格式化日期
    formatDate(date, format = CONFIG.DATE_FORMAT) {
        const d = new Date(date);
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        const hours = String(d.getHours()).padStart(2, '0');
        const minutes = String(d.getMinutes()).padStart(2, '0');
        const seconds = String(d.getSeconds()).padStart(2, '0');

        return format
            .replace('YYYY', year)
            .replace('MM', month)
            .replace('DD', day)
            .replace('HH', hours)
            .replace('mm', minutes)
            .replace('ss', seconds);
    },

    // 获取相对时间
    getRelativeTime(timestamp) {
        const now = Date.now();
        const diff = now - timestamp;
        const seconds = Math.floor(diff / 1000);
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);
        const days = Math.floor(hours / 24);

        if (days > 0) return `${days}天前`;
        if (hours > 0) return `${hours}小时前`;
        if (minutes > 0) return `${minutes}分钟前`;
        return '刚刚';
    },

    // 生成唯一ID
    generateId(prefix = '') {
        return `${prefix}${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    },

    // 获取头像颜色
    getAvatarColor(name) {
        const index = name.charCodeAt(0) % CONFIG.AVATAR_COLORS.length;
        return CONFIG.AVATAR_COLORS[index];
    },

    // 获取姓名首字母
    getInitials(name) {
        if (!name) return '?';
        return name.charAt(0).toUpperCase();
    },

    // 显示Toast
    showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'slideUp 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    },

    // 确认对话框
    confirm(message) {
        return window.confirm(message);
    },

    // 防抖
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    // 节流
    throttle(func, limit) {
        let inThrottle;
        return function(...args) {
            if (!inThrottle) {
                func.apply(this, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }
};

// 添加slideUp动画
const style = document.createElement('style');
style.textContent = `
    @keyframes slideUp {
        to {
            opacity: 0;
            transform: translateX(-50%) translateY(-20px);
        }
    }
`;
document.head.appendChild(style);
