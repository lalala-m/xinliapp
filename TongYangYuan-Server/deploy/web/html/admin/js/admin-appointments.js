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

        const response = await AdminCommon.request(`/admin/appointments?${params}`);
        
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
                    ${appointment.status === 'COMPLETED' ? `<button class="btn-sm btn-edit" onclick="viewTrace(${appointment.id})">查看留痕</button>` : ''}
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
        const statsResponse = await AdminCommon.request('/admin/statistics/appointments');
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
        const appointment = await AdminCommon.request(`/admin/appointments/${appointmentId}`);
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
        await AdminCommon.request(`/admin/appointments/${currentAppointment.id}/confirm`, {
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
        await AdminCommon.request(`/admin/appointments/${appointmentId}/confirm`, {
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
        await AdminCommon.request(`/admin/appointments/${currentAppointment.id}/cancel`, {
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
        await AdminCommon.request(`/admin/appointments/${appointmentId}/cancel`, {
            method: 'PUT'
        });
        
        AdminCommon.showSuccess('取消成功');
        AdminCommon.logAction('CANCEL_APPOINTMENT', `取消预约 ID: ${appointmentId}`);
        loadAppointments();
    } catch (error) {
        AdminCommon.showError('取消失败');
    }
}

// 查看咨询留痕
async function viewTrace(appointmentId) {
    try {
        const recordRes = await AdminCommon.request(`/consultation-records/appointment/${appointmentId}`);
        if (!recordRes || recordRes.code !== 200 || !recordRes.data) {
            AdminCommon.showError('该预约暂无留痕记录');
            return;
        }
        const data = recordRes.data;
        const record = data.record || {};
        const signatures = data.signatures || [];
        const chatMessages = data.chatMessages || [];

        const consultantSig = signatures.find(s => s.signerType === 'CONSULTANT');
        const parentSig = signatures.find(s => s.signerType === 'PARENT');

        const modal = document.getElementById('appointmentModal');
        const details = document.getElementById('appointmentDetails');
        
        // 🔧 渲染聊天记录
        let chatHtml = '';
        if (chatMessages.length > 0) {
            chatHtml = chatMessages.map(msg => {
                const isConsultant = msg.fromConsultant || msg.isFromConsultant;
                const sender = isConsultant ? '👨‍⚕️ 咨询师' : '👤 家长';
                const type = (msg.messageType || msg.type || 'TEXT').toUpperCase();
                const time = msg.createdAt ? new Date(msg.createdAt).toLocaleString('zh-CN') : '';
                
                let contentHtml = '';
                if (type === 'TEXT' || type === 'SYSTEM') {
                    contentHtml = `<div style="color:#333;">${escapeHtml(msg.content || '')}</div>`;
                } else if (type === 'IMAGE') {
                    const imgUrl = msg.mediaUrl || msg.url || '';
                    contentHtml = imgUrl ? `<img src="${imgUrl}" style="max-width:200px;border-radius:6px;cursor:pointer;" onclick="window.open('${imgUrl}')">` : '<span style="color:#999;">[图片]</span>';
                } else if (type === 'AUDIO') {
                    const audioUrl = msg.mediaUrl || msg.url || '';
                    contentHtml = audioUrl ? `<audio controls src="${audioUrl}" style="max-width:250px;"></audio>` : '<span style="color:#999;">[语音]</span>';
                } else if (type === 'VIDEO') {
                    const videoUrl = msg.mediaUrl || msg.url || '';
                    contentHtml = videoUrl ? `<video controls src="${videoUrl}" style="max-width:300px;max-height:200px;border-radius:6px;"></video>` : '<span style="color:#999;">[视频]</span>';
                } else {
                    contentHtml = `<div style="color:#333;">${escapeHtml(msg.content || '')}</div>`;
                    if (msg.mediaUrl || msg.url) {
                        contentHtml += `<div style="margin-top:4px;"><a href="${msg.mediaUrl || msg.url}" target="_blank" style="color:#2196F3;font-size:12px;">查看附件</a></div>`;
                    }
                }
                
                return `
                    <div style="margin-bottom:10px;padding:8px 12px;background:${isConsultant ? '#E3F2FD' : '#F5F5F5'};border-radius:8px;">
                        <div style="font-size:12px;color:#666;margin-bottom:4px;">${sender} · ${time}</div>
                        <div style="font-size:13px;">${contentHtml}</div>
                    </div>
                `;
            }).join('');
        } else {
            chatHtml = '<div style="color:#999;text-align:center;padding:20px;">暂无聊天记录</div>';
        }
        
        details.innerHTML = `
            <div style="line-height: 1.8;max-height:70vh;overflow-y:auto;">
                <h4 style="margin:0 0 10px;">📋 咨询记录 #${record.id || '-'}</h4>
                <p><strong>咨询主题总结：</strong> ${escapeHtml(record.consultantSummary || '未填写')}</p>
                <p><strong>咨询师反馈：</strong> ${escapeHtml(record.consultantFeedback || '未填写')}</p>
                <p><strong>核心困扰标签：</strong> ${escapeHtml(record.coreIssueTags || '未填写')}</p>
                <p><strong>咨询时长：</strong> ${record.duration || 0} 分钟</p>
                <p><strong>家长已确认：</strong> ${record.parentAcknowledged ? '✅ 已确认' : '⏳ 待确认'}</p>
                
                <hr style="margin:16px 0;border:none;border-top:1px solid #eee;">
                <h4 style="margin:0 0 10px;">💬 聊天记录 (${chatMessages.length}条)</h4>
                <div style="max-height:300px;overflow-y:auto;border:1px solid #eee;border-radius:8px;padding:8px;">
                    ${chatHtml}
                </div>
                
                <hr style="margin:16px 0;border:none;border-top:1px solid #eee;">
                <h4 style="margin:0 0 10px;">✍️ 电子签名</h4>
                <div style="display:flex;gap:20px;flex-wrap:wrap;">
                    <div>
                        <p style="font-size:13px;color:#666;margin-bottom:6px;">咨询师签名</p>
                        ${consultantSig ? `<img src="${consultantSig.signatureData || consultantSig.signatureImage}" style="max-width:200px;border:1px solid #ddd;border-radius:6px;background:white;">` : '<div style="color:#999;font-size:13px;">无签名</div>'}
                    </div>
                    <div>
                        <p style="font-size:13px;color:#666;margin-bottom:6px;">家长签名</p>
                        ${parentSig ? `<img src="${parentSig.signatureData || parentSig.signatureImage}" style="max-width:200px;border:1px solid #ddd;border-radius:6px;background:white;">` : '<div style="color:#999;font-size:13px;">无签名</div>'}
                    </div>
                </div>
                ${parentSig && parentSig.verificationMediaUrl ? `
                    <div style="margin-top:12px;">
                        <p style="font-size:13px;color:#666;margin-bottom:6px;">家长验证照片</p>
                        <img src="${parentSig.verificationMediaUrl}" style="max-width:240px;border:1px solid #ddd;border-radius:6px;">
                    </div>
                ` : ''}
            </div>
        `;
        modal.classList.add('show');
    } catch (error) {
        console.error(error);
        AdminCommon.showError('加载留痕记录失败');
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
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
