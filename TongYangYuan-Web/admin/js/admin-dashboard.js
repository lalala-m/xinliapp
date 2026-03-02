// 管理后台首页功能
let dashboardData = {
    totalUsers: 0,
    totalConsultants: 0,
    todayAppointments: 0,
    monthlyRevenue: 0,
    activities: []
};

// 加载仪表板数据
async function loadDashboardData() {
    try {
        // 获取统计数据
        const statsResponse = await AdminCommon.request('/api/admin/statistics/overview');
        if (statsResponse && statsResponse.code === 200) {
            const data = statsResponse.data || {};
            // 更新数据对象，而不是替换它
            dashboardData.totalUsers = data.totalUsers || 0;
            dashboardData.totalConsultants = data.totalConsultants || 0;
            dashboardData.todayAppointments = data.todayAppointments || 0;
            dashboardData.monthlyRevenue = data.monthlyRevenue || 0;
            
            updateDashboard();
        }

        // 获取最近活动
        const logsResponse = await AdminCommon.request('/api/admin/logs?limit=10');
        if (logsResponse && logsResponse.code === 200) {
            dashboardData.activities = Array.isArray(logsResponse.data) ? logsResponse.data : [];
            updateActivities();
        }
    } catch (error) {
        console.error('加载仪表板数据失败:', error);
        // 显示具体的错误信息
        AdminCommon.showError('加载数据失败: ' + (error.message || '未知错误'));
    }
}

// 更新仪表板显示
function updateDashboard() {
    // 更新数字
    document.getElementById('totalUsers').textContent = dashboardData.totalUsers || 0;
    document.getElementById('totalConsultants').textContent = dashboardData.totalConsultants || 0;
    document.getElementById('todayAppointments').textContent = dashboardData.todayAppointments || 0;
    document.getElementById('monthlyRevenue').textContent = `¥${(dashboardData.monthlyRevenue || 0).toFixed(2)}`;
}

// 更新活动列表
function updateActivities() {
    const activityList = document.getElementById('activityList');
    if (!activityList) return;

    if (!dashboardData.activities || dashboardData.activities.length === 0) {
        activityList.innerHTML = '<div class="activity-item"><span class="content">暂无活动记录</span></div>';
        return;
    }

    activityList.innerHTML = dashboardData.activities.map(activity => `
        <div class="activity-item">
            <span class="time">${getTimeAgo(activity.createdAt || activity.timestamp)}</span>
            <span class="content">${activity.action} - ${activity.details || ''}</span>
        </div>
    `).join('');
}

// 获取相对时间
function getTimeAgo(timestamp) {
    if (!timestamp) return '未知';
    
    // Handle Spring Boot LocalDateTime array [year, month, day, hour, minute, second]
    let time;
    if (Array.isArray(timestamp)) {
        time = new Date(timestamp[0], timestamp[1] - 1, timestamp[2], timestamp[3], timestamp[4], timestamp[5]);
    } else {
        time = new Date(timestamp);
    }
    
    const now = new Date();
    const diff = Math.floor((now - time) / 1000); // 秒

    if (diff < 60) return '刚刚';
    if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`;
    if (diff < 604800) return `${Math.floor(diff / 86400)}天前`;
    
    return AdminCommon.formatDateOnly(timestamp);
}

// 刷新数据
function refreshData() {
    AdminCommon.showSuccess('正在刷新数据...');
    loadDashboardData();
}

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', () => {
    loadDashboardData();
    
    // 每30秒自动刷新一次
    setInterval(loadDashboardData, 30000);
});
