// 预约管理功能
let currentPage = 1;
let pageSize = 10;
let totalPages = 1;
let appointments = [];
let currentAppointment = null;

// 加载预约列表
async function loadAppointments() {
    const tableBody = document.getElementById('appointmentTableBody');
    AdminCommon.showLoading(tableBody);

    try {
        const params = new URLSearchParams({
            page: currentPage,
            size: pageSize
        });

        // 添加筛选条件
        const statusFilter = document.getElementById('statusFilter')?.value;
        const dateFilter = document.getElementById('dateFilter')?.value;
        const searchInput = document.getElementById('searchInput')?.value;

        if (statusFilter) params.append('status', statusFilter);
        if (dateFilter) params.append('date', dateFilter);
        if (searchInput) params.append('search', searchInput);

        const response = await AdminCommon.request(`/api/admin/appointments?${params}`);
        
        if (response && response.code === 200) {
            // 后端如果是分页返回，data 可能包含 content
            // 如果是列表，data 就是数组
            const data = response.data;
            let allAppointments = [];
            
            if (Array.isArray(data)) {
                allAppointments = data;
            } else if (data && Array.isArray(data.content)) {
                allAppointments = data.content;
                // 如果后端做了分页，这里可能需要调整
            }
            
            // 前端筛选 (如果是全部返回的话)
            let filteredAppointments = allAppointments;
            
            if (statusFilter) {
                 filteredAppointments = filteredAppointments.filter(a => a.status === statusFilter);
            }
            
             // date filter logic (simplified)
            if (dateFilter) {
                 filteredAppointments = filteredAppointments.filter(a => a.appointmentDate === dateFilter);
            }

            if (searchInput) {
                 const search = searchInput.toLowerCase();
                 filteredAppointments = filteredAppointments.filter(a => 
                     (a.parentName && a.parentName.toLowerCase().includes(search)) ||
                     (a.consultantName && a.consultantName.toLowerCase().includes(search))
                 );
            }
            
            // 计算分页
            totalPages = Math.ceil(filteredAppointments.length / pageSize);
            if (totalPages === 0) totalPages = 1;
            
            if (currentPage > totalPages) currentPage = totalPages;
            
            const start = (currentPage - 1) * pageSize;
            const end = start + pageSize;
            appointments = filteredAppointments.slice(start, end);

            renderAppointments();
            updatePagination();
            updateStats();
        } else {
             AdminCommon.showEmpty(tableBody, '获取数据失败');
        }
    } catch (error) {
        console.error('加载预约列表失败:', error);
        AdminCommon.showEmpty(tableBody, '加载失败');
    }
}

// 渲染预约列表
function renderAppointments() {
    const tableBody = document.getElementById('appointmentTableBody');
    
    if (!appointments || appointments.length === 0) {
        AdminCommon.showEmpty(tableBody);
        return;
    }

    tableBody.innerHTML = appointments.map(appointment => `
        <tr>
            <td>${appointment.id}</td>
            <td>${appointment.parentName || '-'}</td>
            <td>${appointment.consultantName || '-'}</td>
            <td>${AdminCommon.formatDate(appointment.appointmentTime)}</td>
            <td>${appointment.consultationType || appointment.domain || '-'}</td>
            <td><span class="status-badge ${AdminCommon.getStatusClass(appointment.status)}">${AdminCommon.getStatusName(appointment.status)}</span></td>
            <td>${AdminCommon.formatDate(appointment.createdAt)}</td>
            <td>
                <div class="action-btns">
                    <button class="btn-sm btn-view" onclick="viewAppointment(${appointment.id})">查看</button>
                    ${appointment.status === 'PENDING' ? `<button class="btn-sm btn-edit" onclick="confirmAppointmentDirect(${appointment.id})">确认</button>` : ''}
                    ${appointment.status !== 'CANCELLED' ? `<button class="btn-sm btn-delete" onclick="cancelAppointmentDirect(${appointment.id})">取消</button>` : ''}
                </div>
            </td>
        </tr>
    `).join('');
}

// 更新统计数据
async function updateStats() {
    try {
        const statsResponse = await AdminCommon.request('/api/admin/statistics/appointments');
        if (statsResponse && statsResponse.code === 200) {
            const stats = statsResponse.data || {};
            document.getElementById('todayCount').textContent = stats.today || 0;
            document.getElementById('pendingCount').textContent = stats.pending || 0;
            document.getElementById('completedCount').textContent = stats.completed || 0;
            document.getElementById('cancelledCount').textContent = stats.cancelled || 0;
        }
    } catch (error) {
        console.error('加载统计数据失败:', error);
    }
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
        loadAppointments();
    }
}

// 下一页
function nextPage() {
    if (currentPage < totalPages) {
        currentPage++;
        loadAppointments();
    }
}

// 搜索预约
function searchAppointments() {
    currentPage = 1;
    loadAppointments();
}

// 筛选预约
function filterAppointments() {
    currentPage = 1;
    loadAppointments();
}

// 刷新预约列表
function refreshAppointments() {
    currentPage = 1;
    loadAppointments();
    AdminCommon.showSuccess('已刷新');
}

// 查看预约详情
async function viewAppointment(appointmentId) {
    try {
        const appointment = await AdminCommon.request(`/api/admin/appointments/${appointmentId}`);
        if (appointment) {
            currentAppointment = appointment;
            showAppointmentModal(appointment);
        }
    } catch (error) {
        AdminCommon.showError('获取预约详情失败');
    }
}

// 显示预约详情模态框
function showAppointmentModal(appointment) {
    const modal = document.getElementById('appointmentModal');
    const details = document.getElementById('appointmentDetails');
    
    details.innerHTML = `
        <div style="line-height: 2;">
            <p><strong>预约ID:</strong> ${appointment.id}</p>
            <p><strong>用户:</strong> ${appointment.parentName || '-'}</p>
            <p><strong>咨询师:</strong> ${appointment.consultantName || '-'}</p>
            <p><strong>预约时间:</strong> ${AdminCommon.formatDate(appointment.appointmentTime)}</p>
            <p><strong>咨询类型:</strong> ${appointment.consultationType || appointment.domain || '-'}</p>
            <p><strong>状态:</strong> ${AdminCommon.getStatusName(appointment.status)}</p>
            <p><strong>备注:</strong> ${appointment.notes || '-'}</p>
            <p><strong>创建时间:</strong> ${AdminCommon.formatDate(appointment.createdAt)}</p>
            <p><strong>更新时间:</strong> ${AdminCommon.formatDate(appointment.updatedAt)}</p>
        </div>
    `;
    
    modal.classList.add('show');
}

// 关闭模态框
function closeModal() {
    const modal = document.getElementById('appointmentModal');
    modal.classList.remove('show');
    currentAppointment = null;
}

// 确认预约
async function confirmAppointment() {
    if (!currentAppointment) return;
    
    if (!AdminCommon.confirm('确定要确认该预约吗？')) return;

    try {
        await AdminCommon.request(`/api/admin/appointments/${currentAppointment.id}/confirm`, {
            method: 'PUT'
        });
        
        AdminCommon.showSuccess('确认成功');
        AdminCommon.logAction('CONFIRM_APPOINTMENT', `确认预约 ID: ${currentAppointment.id}`);
        closeModal();
        loadAppointments();
    } catch (error) {
        AdminCommon.showError('确认失败');
    }
}

// 直接确认预约
async function confirmAppointmentDirect(appointmentId) {
    if (!AdminCommon.confirm('确定要确认该预约吗？')) return;

    try {
        await AdminCommon.request(`/api/admin/appointments/${appointmentId}/confirm`, {
            method: 'PUT'
        });
        
        AdminCommon.showSuccess('确认成功');
        AdminCommon.logAction('CONFIRM_APPOINTMENT', `确认预约 ID: ${appointmentId}`);
        loadAppointments();
    } catch (error) {
        AdminCommon.showError('确认失败');
    }
}

// 取消预约
async function cancelAppointment() {
    if (!currentAppointment) return;
    
    if (!AdminCommon.confirm('确定要取消该预约吗？')) return;

    try {
        await AdminCommon.request(`/api/admin/appointments/${currentAppointment.id}/cancel`, {
            method: 'PUT'
        });
        
        AdminCommon.showSuccess('取消成功');
        AdminCommon.logAction('CANCEL_APPOINTMENT', `取消预约 ID: ${currentAppointment.id}`);
        closeModal();
        loadAppointments();
    } catch (error) {
        AdminCommon.showError('取消失败');
    }
}

// 直接取消预约
async function cancelAppointmentDirect(appointmentId) {
    if (!AdminCommon.confirm('确定要取消该预约吗？')) return;

    try {
        await AdminCommon.request(`/api/admin/appointments/${appointmentId}/cancel`, {
            method: 'PUT'
        });
        
        AdminCommon.showSuccess('取消成功');
        AdminCommon.logAction('CANCEL_APPOINTMENT', `取消预约 ID: ${appointmentId}`);
        loadAppointments();
    } catch (error) {
        AdminCommon.showError('取消失败');
    }
}

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', () => {
    loadAppointments();
    
    // 搜索框回车事件
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchAppointments();
            }
        });
    }
});
