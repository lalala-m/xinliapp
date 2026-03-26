// 童养园咨询师端 - 工作台逻辑

/** 将后端 appointmentDate 统一为 YYYY-MM-DD，避免与「今日」比较失败 */
function normalizeAppointmentDate(apt) {
    const v = apt && (apt.appointmentDate != null ? apt.appointmentDate : apt.date);
    if (v == null || v === '') return '';
    if (typeof v === 'string') {
        return v.length >= 10 ? v.slice(0, 10) : v;
    }
    if (Array.isArray(v) && v.length >= 3) {
        const y = v[0];
        const m = v[1];
        const d = v[2];
        return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    }
    return '';
}

const Dashboard = {
    // 默认「全部」，咨询师登录后第一时间看到所有预约（包括历史）
    currentTab: 'all',
    consultant: null,
    appointments: [], // 本地缓存当前的预约列表
    records: [],

    // 初始化
    async init() {
        // 检查登录状态
        this.consultant = Auth.checkAuth();
        if (!this.consultant) return;

        // 刷新咨询师信息以确保 ID 与数据库一致 (解决本地缓存 stale 导致的外键错误)
        try {
            const freshInfo = await API.get(`/consultants/user/${this.consultant.userId}`);
            if (freshInfo && freshInfo.id) {
                this.consultant = { ...this.consultant, ...freshInfo };
                // 更新本地存储
                const stored = ConsultantStorage.getCurrentConsultant();
                ConsultantStorage.setCurrentConsultant({ ...stored, ...freshInfo });
            } else {
                // 如果后端返回空(说明该用户ID没有对应的咨询师档案)，则当前登录状态无效
                console.warn("未找到咨询师档案，强制退出");
                Utils.showToast('账号信息已过期，请重新登录', 'error');
                Auth.logout();
                return;
            }
        } catch (e) {
            console.error('刷新咨询师信息失败', e);
            // 遇到任何错误都建议重新登录，确保数据一致性
            Utils.showToast('同步用户信息失败，请重新登录', 'error');
            Auth.logout();
            return;
        }

        // 显示咨询师信息
        this.displayConsultantInfo();

        // 初始加载数据
        this.refreshData();

        this.requestNotificationPermission();
        this.startPolling();

        // 确保如果有 Mock 数据也尝试加载
        if (window.MockData && !this.appointments.length) {
            console.log('尝试加载 Mock 数据作为兜底...');
            // 这里可以添加加载 Mock 数据的逻辑，但目前先主要依赖后端接口
        }
    },

    // 每30秒自动轮询预约数据
    startPolling() {
        if (this._pollTimer) clearInterval(this._pollTimer);
        this._pollTimer = setInterval(async () => {
            try {
                const appointments = await API.get(`/consultants/me/appointments`);
                if (appointments) {
                    const oldPendingCount = this.appointments.filter(apt => apt.status === 'pending').length;
                    this.appointments = appointments.map(apt => ({
                        ...apt,
                        date: normalizeAppointmentDate(apt),
                        status: (apt.status || '').toLowerCase(),
                        clientName: `家长#${apt.parentUserId}`,
                        createdAt: new Date(apt.createdAt).getTime()
                    }));
                    const newPendingCount = this.appointments.filter(apt => apt.status === 'pending').length;

                    // 如果有新的待处理预约，弹通知
                    if (newPendingCount > oldPendingCount) {
                        const newApts = this.appointments.filter(apt => apt.status === 'pending');
                        Utils.showToast(`您有 ${newPendingCount} 个新预约待处理！`, 'info');
                        if (Notification.permission === 'granted') {
                            new Notification('童养园 - 新预约通知', {
                                body: `您有 ${newPendingCount} 个新预约待确认`,
                                icon: 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><text y=".9em" font-size="90">👨‍⚕️</text></svg>'
                            });
                        }
                    }

                    this.updateStats();
                    this.filterAndDisplayAppointments();
                }
            } catch (e) {
                console.error('轮询预约失败', e);
            }
        }, 30000); // 30秒轮询一次
    },

    // 请求浏览器通知权限
    requestNotificationPermission() {
        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission();
        }
    },

    // 显示咨询师信息
    displayConsultantInfo() {
        const nameEl = document.getElementById('consultantName');
        const titleEl = document.getElementById('consultantTitle');
        const avatarEl = document.getElementById('consultantAvatar');

        if (nameEl) nameEl.textContent = this.consultant.name;
        if (titleEl) titleEl.textContent = this.consultant.title;
        if (avatarEl) {
            avatarEl.textContent = Utils.getInitials(this.consultant.name);
            avatarEl.style.background = this.consultant.avatarColor || Utils.getAvatarColor(this.consultant.name);
        }
    },

    // 刷新数据（同时加载统计和列表）
    async refreshData() {
        this.showLoading();
        try {
            const [appointments, recordPage] = await Promise.all([
                API.get(`/consultants/me/appointments`),
                API.get('/consultation-records/consultant?page=0&size=20')
            ]);

            if (appointments) {
                this.appointments = appointments.map(apt => ({
                    ...apt,
                    date: normalizeAppointmentDate(apt),
                    status: (apt.status || '').toLowerCase(),
                    clientName: `家长#${apt.parentUserId}`,
                    createdAt: new Date(apt.createdAt).getTime()
                }));

                this.updateStats();
                this.filterAndDisplayAppointments();
            }

            if (recordPage) {
                const recordList = Array.isArray(recordPage) ? recordPage : (recordPage.content || []);
                this.records = recordList.map(record => ({
                    ...record,
                    clientName: `家长#${record.parentUserId}`,
                    createdAt: record.createdAt ? new Date(record.createdAt).getTime() : Date.now()
                }));
                this.displayRecords(this.records);
            }
        } catch (error) {
            console.error('加载数据失败', error);
            Utils.showToast('加载数据失败', 'error');
        } finally {
            this.hideLoading();
        }
    },

    async refreshRecords() {
        this.showLoading();
        try {
            const recordPage = await API.get('/consultation-records/consultant?page=0&size=20');
            if (recordPage) {
                const recordList = Array.isArray(recordPage) ? recordPage : (recordPage.content || []);
                this.records = recordList.map(record => ({
                    ...record,
                    clientName: `家长#${record.parentUserId}`,
                    createdAt: record.createdAt ? new Date(record.createdAt).getTime() : Date.now()
                }));
                this.displayRecords(this.records);
            }
        } catch (error) {
            console.error('加载咨询记录失败', error);
            Utils.showToast('加载咨询记录失败', 'error');
        } finally {
            this.hideLoading();
        }
    },

    // 更新统计数据
    updateStats() {
        const todayStr = Utils.formatDate(new Date());
        
        const todayCount = this.appointments.filter(apt => apt.date === todayStr).length;
        const pendingCount = this.appointments.filter(apt => apt.status === 'pending').length;
        const completedCount = this.appointments.filter(apt => apt.status === 'completed').length;
        
        this.updateStat('todayCount', todayCount);
        this.updateStat('pendingCount', pendingCount);
        this.updateStat('completedCount', completedCount);
        this.updateStat('totalServed', this.consultant.servedCount || completedCount);
    },

    // 更新统计数字
    updateStat(id, value) {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = value;
        }
    },

    // 切换标签
    switchTab(tab) {
        this.currentTab = tab;

        // 更新标签样式
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.remove('active');
            if (btn.dataset.tab === tab) {
                btn.classList.add('active');
            }
        });

        // 重新过滤显示
        this.filterAndDisplayAppointments();
    },

    // 过滤并显示预约
    filterAndDisplayAppointments() {
        let filtered = [];
        const todayStr = Utils.formatDate(new Date());

        switch (this.currentTab) {
            case 'today':
                filtered = this.appointments.filter(apt => apt.date === todayStr);
                break;
            case 'pending':
                filtered = this.appointments.filter(apt => apt.status === 'pending');
                break;
            case 'completed':
                filtered = this.appointments.filter(apt => apt.status === 'completed');
                break;
            case 'all':
                filtered = [...this.appointments];
                break;
        }

        // 按创建时间倒序排列
        filtered.sort((a, b) => b.createdAt - a.createdAt);

        this.displayAppointments(filtered);
    },

    // 显示预约列表
    displayAppointments(appointments) {
        const listEl = document.getElementById('appointmentList');
        if (!listEl) return;

        const fragment = document.createDocumentFragment();

        if (appointments.length === 0) {
            const emptyDiv = document.createElement('div');
            emptyDiv.className = 'empty-state';
            const hint = this.currentTab === 'today'
                ? '<div class="empty-hint" style="margin-top:10px;font-size:13px;color:#888;line-height:1.5;">今日没有排期？待确认的预约若约在未来日期，请点击上方「待处理」查看。</div>'
                : '';
            emptyDiv.innerHTML = `
                <div class="empty-icon">📭</div>
                <div class="empty-text">暂无预约</div>
                ${hint}
            `;
            fragment.appendChild(emptyDiv);
        } else {
            appointments.forEach(apt => {
                const itemDiv = document.createElement('div');
                itemDiv.className = 'appointment-item';
                itemDiv.dataset.id = apt.id;
                itemDiv.innerHTML = this.createAppointmentItemHTML(apt);
                fragment.appendChild(itemDiv);
            });
        }

        listEl.innerHTML = '';
        listEl.appendChild(fragment);
    },

    displayRecords(records) {
        const listEl = document.getElementById('consultationRecordList');
        if (!listEl) return;

        const fragment = document.createDocumentFragment();

        if (records.length === 0) {
            const emptyDiv = document.createElement('div');
            emptyDiv.className = 'empty-state';
            emptyDiv.innerHTML = `
                <div class="empty-icon">📭</div>
                <div class="empty-text">暂无咨询记录</div>
            `;
            fragment.appendChild(emptyDiv);
        } else {
            records.forEach(record => {
                const itemDiv = document.createElement('div');
                itemDiv.className = 'consultation-record-item';
                itemDiv.dataset.id = record.id;
                itemDiv.innerHTML = this.createRecordItemHTML(record);
                fragment.appendChild(itemDiv);
            });
        }

        listEl.innerHTML = '';
        listEl.appendChild(fragment);
    },

    // 创建预约项HTML
    createAppointmentItemHTML(apt) {
        const statusBadge = this.getStatusBadge(apt.status);
        const actions = this.getAppointmentActions(apt);
        const avatarColor = Utils.getAvatarColor(apt.clientName);

        return `
            <div class="appointment-avatar" style="background: ${avatarColor}">
                ${Utils.getInitials(apt.clientName)}
            </div>
            <div class="appointment-info">
                <div class="appointment-client">
                    ${apt.clientName} ${statusBadge}
                </div>
                <div class="appointment-child">
                    孩子：${apt.childName || '未填写'}（${apt.childAge || '?'}岁）
                </div>
                <div class="appointment-time">
                    📅 ${apt.date} ${apt.timeSlot}
                </div>
                <div class="appointment-description">
                    ${apt.description || '无描述'}
                </div>
            </div>
            <div class="appointment-actions">
                ${actions}
            </div>
        `;
    },

    createRecordItemHTML(record) {
        const avatarColor = Utils.getAvatarColor(record.clientName);
        const createdAtText = record.createdAt ? Utils.formatDate(record.createdAt) : '-';
        const durationText = record.duration ? `${record.duration} 分钟` : '未记录';
        const typeText = this.getConsultationTypeLabel(record.consultationType);
        const summaryText = record.summary || record.consultantFeedback || '无摘要';
        const actions = record.appointmentId
            ? `<button class="btn btn-secondary btn-sm" onclick="Dashboard.viewHistory('${record.appointmentId}')">查看记录</button>`
            : '';

        return `
            <div class="record-avatar" style="background: ${avatarColor}">
                ${Utils.getInitials(record.clientName)}
            </div>
            <div class="record-info">
                <div class="record-title">
                    ${record.clientName} · ${typeText}
                </div>
                <div class="record-meta">
                    📅 ${createdAtText} · ⏱ ${durationText}
                </div>
                <div class="record-summary">
                    ${summaryText}
                </div>
            </div>
            <div class="record-actions">
                ${actions}
            </div>
        `;
    },

    getConsultationTypeLabel(type) {
        const map = {
            ONLINE: '在线咨询',
            OFFLINE: '线下咨询',
            VIDEO: '视频咨询',
            AUDIO: '语音咨询'
        };
        return map[type] || '在线咨询';
    },

    // 获取状态徽章
    getStatusBadge(status) {
        const badges = {
            'pending': '<span class="badge badge-warning">待处理</span>',
            'accepted': '<span class="badge badge-primary">已接受</span>',
            'in_progress': '<span class="badge badge-primary">进行中</span>',
            'completed': '<span class="badge badge-success">已完成</span>',
            'cancelled': '<span class="badge badge-error">已取消</span>'
        };
        return badges[status] || `<span class="badge">${status}</span>`;
    },

    // 获取预约操作按钮
    getAppointmentActions(apt) {
        const actions = [];

        if (apt.status === 'pending') {
            actions.push(`
                <button class="btn btn-success btn-sm" onclick="Dashboard.updateStatus('${apt.id}', 'ACCEPTED')">
                    接受
                </button>
            `);
            actions.push(`
                <button class="btn btn-danger btn-sm" onclick="Dashboard.updateStatus('${apt.id}', 'CANCELLED')">
                    拒绝
                </button>
            `);
        }

        if (apt.status === 'accepted' || apt.status === 'in_progress') {
            actions.push(`
                <button class="btn btn-primary btn-sm" onclick="Dashboard.startChat('${apt.id}')">
                    开始咨询
                </button>
            `);
        }

        if (apt.status === 'completed') {
            actions.push(`
                <button class="btn btn-secondary btn-sm" onclick="Dashboard.viewHistory('${apt.id}')">
                    查看记录
                </button>
            `);
        }

        // 任何状态的预约都可删除（尤其是家长那边同步失败产生的垃圾记录）
        actions.push(`
            <button class="btn btn-sm" style="color:#999;background:#f5f5f5;border:1px solid #ddd;"
                onclick="Dashboard.deleteAppointment('${apt.id}')">
                删除
            </button>
        `);

        return actions.join('');
    },

    // 删除预约
    async deleteAppointment(id) {
        if (!Utils.confirm('确定要删除这条预约记录吗？删除后家长端也会同步消失。')) {
            return;
        }
        this.showLoading();
        try {
            await API.delete(`/appointments/${id}`);
            Utils.showToast('已删除', 'success');
            this.refreshData();
        } catch (error) {
            console.error('删除预约失败', error);
            Utils.showToast('删除失败', 'error');
            this.hideLoading();
        }
    },

    // 更新预约状态
    async updateStatus(id, newStatus) {
        const confirmMsg = newStatus === 'ACCEPTED' ? '确定接受这个预约吗？' : '确定拒绝这个预约吗？';

        if (Utils.confirm(confirmMsg)) {
            this.showLoading();
            try {
                const endpoint = newStatus === 'ACCEPTED'
                    ? `/consultants/me/appointments/${id}/accept`
                    : `/consultants/me/appointments/${id}/reject`;
                await API.put(endpoint);

                Utils.showToast('操作成功', 'success');
                this.refreshData(); // 重新加载数据
            } catch (error) {
                console.error('更新状态失败', error);
                Utils.showToast('操作失败', 'error');
                this.hideLoading();
            }
        }
    },

    // 接受预约 (为了兼容旧代码调用，实际上已经统一用 updateStatus)
    acceptAppointment(id) {
        this.updateStatus(id, 'ACCEPTED');
    },

    // 拒绝预约
    rejectAppointment(id) {
        this.updateStatus(id, 'CANCELLED');
    },

    // 开始咨询
    async startChat(id) {
        // 先更新状态为进行中
        try {
            await API.put(`/appointments/${id}`, {
                status: 'IN_PROGRESS'
            });
            window.location.href = `chat.html?appointmentId=${id}`;
        } catch (error) {
            console.error('开始咨询失败', error);
            // 即使失败也跳转尝试
            window.location.href = `chat.html?appointmentId=${id}`;
        }
    },

    // 查看历史记录
    viewHistory(id) {
        window.location.href = `chat.html?appointmentId=${id}&readonly=true`;
    },

    // 添加测试预约 (仅开发用)
    addMockAppointment() {
        this.showLoading();

        if (!this.consultant || !this.consultant.id) {
            Utils.showToast('咨询师信息无效，请重新登录', 'error');
            this.hideLoading();
            return;
        }

        // 调用创建预约 API
        const mockApt = {
            consultantId: this.consultant.id,
            parentUserId: 1, // 假设家长ID (请确保数据库中存在 ID 为 1 的用户)
            childName: "测试儿童",
            childAge: 6,
            appointmentDate: Utils.formatDate(new Date()),
            timeSlot: "10:00-11:00",
            description: "这是一个测试预约",
            status: "PENDING"
        };
        
        API.post('/appointments', mockApt).then(() => {
            Utils.showToast('已添加测试预约', 'success');
            this.refreshData();
        }).catch(() => {
            this.hideLoading();
        });
    },

    // 显示加载状态
    showLoading() {
        const listEl = document.getElementById('appointmentList');
        if (listEl) {
            listEl.style.opacity = '0.5';
            listEl.style.pointerEvents = 'none';
        }
        const recordListEl = document.getElementById('consultationRecordList');
        if (recordListEl) {
            recordListEl.style.opacity = '0.5';
            recordListEl.style.pointerEvents = 'none';
        }
    },

    // 隐藏加载状态
    hideLoading() {
        const listEl = document.getElementById('appointmentList');
        if (listEl) {
            listEl.style.opacity = '1';
            listEl.style.pointerEvents = 'auto';
        }
        const recordListEl = document.getElementById('consultationRecordList');
        if (recordListEl) {
            recordListEl.style.opacity = '1';
            recordListEl.style.pointerEvents = 'auto';
        }
    }
};

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', () => {
    Dashboard.init();
});
