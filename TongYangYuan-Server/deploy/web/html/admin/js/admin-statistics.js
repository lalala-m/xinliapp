// 数据统计功能
let charts = {};
let statisticsData = {};

// 初始化日期范围
function initDateRange() {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 30); // 默认最近30天

    document.getElementById('startDate').valueAsDate = startDate;
    document.getElementById('endDate').valueAsDate = endDate;
}

// 选择快捷时间范围
function selectRange(range) {
    const endDate = new Date();
    const startDate = new Date();

    switch (range) {
        case 'today':
            startDate.setHours(0, 0, 0, 0);
            break;
        case 'week':
            startDate.setDate(startDate.getDate() - 7);
            break;
        case 'month':
            startDate.setMonth(startDate.getMonth() - 1);
            break;
        case 'year':
            startDate.setFullYear(startDate.getFullYear() - 1);
            break;
    }

    document.getElementById('startDate').valueAsDate = startDate;
    document.getElementById('endDate').valueAsDate = endDate;
    
    updateStatistics();
}

// 更新统计数据
async function updateStatistics() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!startDate || !endDate) {
        AdminCommon.showError('请选择日期范围');
        return;
    }

    try {
        const params = new URLSearchParams({ startDate, endDate });
        const response = await AdminCommon.request(`/admin/statistics?${params}`);
        
        if (response && response.code === 200) {
            statisticsData = response.data || {};
            updateMetrics();
            updateCharts();
            updateSummaryTable();
        }
    } catch (error) {
        console.error('加载统计数据失败:', error);
        AdminCommon.showError('加载统计数据失败');
    }
}

// 更新关键指标
function updateMetrics() {
    document.getElementById('totalUsers').textContent = statisticsData.totalUsers || 0;
    document.getElementById('activeUsers').textContent = statisticsData.activeUsers || 0;
    document.getElementById('totalAppointments').textContent = statisticsData.totalAppointments || 0;
    document.getElementById('totalRevenue').textContent = `¥${(statisticsData.totalRevenue || 0).toFixed(2)}`;
}

// 更新图表
function updateCharts() {
    updateUserGrowthChart();
    updateAppointmentChart();
    updateRevenueChart();
    updateConsultantRankChart();
}

// 用户增长趋势图
function updateUserGrowthChart() {
    const ctx = document.getElementById('userGrowthChart');
    if (!ctx) return;

    if (charts.userGrowth) {
        charts.userGrowth.destroy();
    }

    const data = statisticsData.userGrowth || [];
    
    charts.userGrowth = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.map(d => d.date),
            datasets: [{
                label: '新增用户',
                data: data.map(d => d.count),
                borderColor: '#4CAF50',
                backgroundColor: 'rgba(76, 175, 80, 0.1)',
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

// 预约统计图
function updateAppointmentChart() {
    const ctx = document.getElementById('appointmentChart');
    if (!ctx) return;

    if (charts.appointment) {
        charts.appointment.destroy();
    }

    const data = statisticsData.appointmentStats || {};
    
    charts.appointment = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['已完成', '待确认', '已取消'],
            datasets: [{
                data: [
                    data.completed || 0,
                    data.pending || 0,
                    data.cancelled || 0
                ],
                backgroundColor: [
                    '#4CAF50',
                    '#FF9800',
                    '#f44336'
                ]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

// 收入统计图
function updateRevenueChart() {
    const ctx = document.getElementById('revenueChart');
    if (!ctx) return;

    if (charts.revenue) {
        charts.revenue.destroy();
    }

    const data = statisticsData.revenueData || [];
    
    charts.revenue = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.map(d => d.date),
            datasets: [{
                label: '收入（元）',
                data: data.map(d => d.amount),
                backgroundColor: '#2196F3'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

// 咨询师排行图
function updateConsultantRankChart() {
    const ctx = document.getElementById('consultantRankChart');
    if (!ctx) return;

    if (charts.consultantRank) {
        charts.consultantRank.destroy();
    }

    const data = statisticsData.consultantRank || [];
    
    charts.consultantRank = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.map(d => d.name),
            datasets: [{
                label: '预约数',
                data: data.map(d => d.count),
                backgroundColor: '#9C27B0'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            indexAxis: 'y',
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                x: {
                    beginAtZero: true
                }
            }
        }
    });
}

// 更新汇总表格
function updateSummaryTable() {
    const summary = statisticsData.summary || {};
    
    // 今日数据
    document.getElementById('newUsersToday').textContent = summary.today?.newUsers || 0;
    document.getElementById('newAppointmentsToday').textContent = summary.today?.newAppointments || 0;
    document.getElementById('completedToday').textContent = summary.today?.completed || 0;
    document.getElementById('revenueToday').textContent = `¥${(summary.today?.revenue || 0).toFixed(2)}`;
    
    // 本周数据
    document.getElementById('newUsersWeek').textContent = summary.week?.newUsers || 0;
    document.getElementById('newAppointmentsWeek').textContent = summary.week?.newAppointments || 0;
    document.getElementById('completedWeek').textContent = summary.week?.completed || 0;
    document.getElementById('revenueWeek').textContent = `¥${(summary.week?.revenue || 0).toFixed(2)}`;
    
    // 本月数据
    document.getElementById('newUsersMonth').textContent = summary.month?.newUsers || 0;
    document.getElementById('newAppointmentsMonth').textContent = summary.month?.newAppointments || 0;
    document.getElementById('completedMonth').textContent = summary.month?.completed || 0;
    document.getElementById('revenueMonth').textContent = `¥${(summary.month?.revenue || 0).toFixed(2)}`;
    
    // 总计数据
    document.getElementById('newUsersTotal').textContent = summary.total?.newUsers || 0;
    document.getElementById('newAppointmentsTotal').textContent = summary.total?.newAppointments || 0;
    document.getElementById('completedTotal').textContent = summary.total?.completed || 0;
    document.getElementById('revenueTotal').textContent = `¥${(summary.total?.revenue || 0).toFixed(2)}`;
}

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', () => {
    initDateRange();
    updateStatistics();
});
