// 童养园咨询师端 - 聊天逻辑（增强版：微信风格通话状态）

const Chat = {
    appointmentId: null,
    appointment: null,
    consultant: null,
    messages: [],
    stompClient: null,
    isReadonly: false,
    consultationStartAt: null,
    lastCallInfo: null,
    // 通话状态
    callState: null, // 'calling' | 'connected' | 'ended' | 'rejected' | 'failed' | null
    callType: null,   // 'video' | 'audio'
    callStartTime: null,
    /** 同一通来电可能经 /queue/webrtc 与 /queue/messages 各推一次，按 sessionId 去重 */
    _recentCallSessionIds: null,

    // 初始化
    async init() {
        // 检查登录状态
        this.consultant = Auth.checkAuth();
        if (!this.consultant) return;

        // 获取URL参数
        const urlParams = new URLSearchParams(window.location.search);
        this.appointmentId = urlParams.get('appointmentId');
        this.isReadonly = urlParams.get('readonly') === 'true';

        if (!this.appointmentId) {
            Utils.showToast('预约ID不存在', 'error');
            setTimeout(() => window.location.href = 'dashboard.html', 1500);
            return;
        }

        // 加载预约信息
        await this.loadAppointmentInfo();

        this.consultationStartAt = Date.now();
        const session = Storage.get(CONFIG.STORAGE_KEYS.CONSULTATION_SESSION);
        if (session && String(session.appointmentId) === String(this.appointmentId)) {
            this.lastCallInfo = session;
        } else {
            this.lastCallInfo = null;
        }

        // 加载消息历史
        await this.loadMessages();

        // 连接 WebSocket
        if (!this.isReadonly) {
            this.connectWebSocket();
        }

        // 设置只读模式
        if (this.isReadonly) {
            this.setReadonlyMode();
        }

        // 自动滚动到底部
        this.scrollToBottom();
    },

    // 加载预约信息
    async loadAppointmentInfo() {
        try {
            const appointment = await API.get(`/appointments/${this.appointmentId}`);
            if (appointment) {
                this.appointment = {
                    ...appointment,
                    clientName: `家长#${appointment.parentUserId}`, // 暂无名字
                    childName: appointment.childName,
                    childAge: appointment.childAge
                };
                this.displayAppointmentInfo();
            } else {
                Utils.showToast('预约不存在', 'error');
                setTimeout(() => window.location.href = 'dashboard.html', 1500);
            }
        } catch (error) {
            console.error('加载预约失败', error);
            Utils.showToast('加载预约失败', 'error');
        }
    },

    // 显示预约信息
    displayAppointmentInfo() {
        const nameEl = document.getElementById('clientName');
        const childEl = document.getElementById('clientChild');
        const avatarEl = document.getElementById('clientAvatar');

        if (nameEl) nameEl.textContent = this.appointment.clientName;
        if (childEl) childEl.textContent = `孩子：${this.appointment.childName || '未填写'}（${this.appointment.childAge || '?'}岁）`;
        if (avatarEl) {
            avatarEl.textContent = Utils.getInitials(this.appointment.clientName);
            avatarEl.style.background = Utils.getAvatarColor(this.appointment.clientName);
        }
    },

    // 加载消息
    async loadMessages() {
        try {
            const messages = await API.get(`/messages/appointment/${this.appointmentId}`);
            if (messages) {
                this.messages = messages.map(this.mapMessage);
                this.displayMessages();
            }
        } catch (error) {
            console.error('加载消息失败', error);
        }
    },

    // 返回上一页
    goBack() {
        if (window.history.length > 1) {
            window.history.back();
        } else {
            window.location.href = 'dashboard.html';
        }
    },

    // 映射后端消息格式到前端
    mapMessage(msg) {
        return {
            id: msg.id,
            type: msg.messageType ? msg.messageType.toLowerCase() : 'text',
            content: msg.content,
            timestamp: msg.timestamp || (msg.createdAt ? new Date(msg.createdAt).getTime() : Date.now()),
            fromConsultant: msg.isFromConsultant,
            senderUserId: msg.senderUserId
        };
    },

    // 连接 WebSocket
    connectWebSocket() {
        const token = API.getToken();
        const socket = new SockJS(CONFIG.WS_BASE_URL, null, {
            transports: ['websocket']
        });
        this.stompClient = Stomp.over(socket);
        this.stompClient.debug = null; // 关闭调试日志
        this._setWsStatus('conn');

        // Token 放入 STOMP CONNECT 帧的 Authorization 头，供 WebSocketChannelInterceptor 验证
        this.stompClient.connect(
            { 'Authorization': 'Bearer ' + token },
            (frame) => {
                console.log('STOMP Connected: ' + frame + ' (userId=' + this.consultant.userId + ')');
                this._setWsStatus('ok');

                // 订阅个人消息队列
                this.stompClient.subscribe('/user/queue/messages', (messageOutput) => {
                    const message = JSON.parse(messageOutput.body);
                    this.handleIncomingMessage(message);
                });

                this.stompClient.subscribe('/user/queue/webrtc', (messageOutput) => {
                    const signal = JSON.parse(messageOutput.body);
                    this.handleWebRTCSignal(signal);
                });
            }, (error) => {
                console.error('STOMP error:', error);
                this._setWsStatus('err');
                setTimeout(() => this.connectWebSocket(), 5000);
            }
        );
    },

    _setWsStatus(status) {
        const el = document.getElementById('wsStatusBar');
        if (!el) return;
        el.className = 'ws-status ' + status;
        const labels = { ok: '已连接', conn: '连接中…', err: '连接失败', disc: '已断开' };
        el.innerHTML = `<span class="ws-status-dot"></span>${labels[status] || status}`;
    },

    // 处理收到的消息
    handleIncomingMessage(rawMsg) {
        // 过滤非当前预约的消息（虽然通常后端会定向推送，但多重保险）
        if (rawMsg.appointmentId && String(rawMsg.appointmentId) !== String(this.appointmentId)) {
            return;
        }

        const mt = (rawMsg.messageType || '').toString().toUpperCase();
        const content = rawMsg.content || '';
        if (mt === 'SYSTEM' && content.startsWith('CALL:call:')) {
            this.handleCallSignalFromChatMessage(rawMsg);
        }

        const message = this.mapMessage(rawMsg);
        this.messages.push(message);
        
        // 只有当消息不是自己刚刚发送的（即避免重复显示，虽然这里是全量推送）
        // 实际上，我们发送时会先上屏，或者等待推送。
        // 为了简单，我们每次收到都重新渲染或追加。
        // 更好的做法是去重。
        
        const existingMsg = document.querySelector(`.message[data-id="${message.id}"]`);
        if (!existingMsg) {
             const messagesArea = document.getElementById('messagesArea');
             // 如果当前显示"暂无消息"，清空
             if (messagesArea.querySelector('.empty-messages')) {
                 messagesArea.innerHTML = '';
             }
             messagesArea.insertAdjacentHTML('beforeend', this.createMessageElement(message));
             this.scrollToBottom();
        }
    },

    /**
     * 与 handleWebRTCSignal 一致：从已持久化的 SYSTEM 消息解析来电（兜底 /queue/messages）
     */
    handleCallSignalFromChatMessage(rawMsg) {
        const content = rawMsg.content || '';
        const parts = content.split(':', 4);
        if (parts.length < 4 || parts[1] !== 'call') return;
        const callTypePart = parts[2] || 'video';
        const sessionId = parts[3];
        const signal = {
            type: 'call',
            fromUserId: rawMsg.senderUserId,
            toUserId: rawMsg.receiverUserId,
            appointmentId: rawMsg.appointmentId,
            data: { callType: callTypePart, sessionId }
        };
        this.handleWebRTCSignal(signal);
    },

    consumeCallSessionIfNew(signal) {
        if (!signal || signal.type !== 'call') return true;
        const sid = signal.data && signal.data.sessionId;
        if (!sid) return true;
        if (!this._recentCallSessionIds) this._recentCallSessionIds = new Set();
        if (this._recentCallSessionIds.has(sid)) return false;
        this._recentCallSessionIds.add(sid);
        setTimeout(() => {
            if (this._recentCallSessionIds) this._recentCallSessionIds.delete(sid);
        }, 120000);
        return true;
    },

    handleWebRTCSignal(signal) {
        if (!signal || !signal.type) return;
        if (signal.appointmentId && String(signal.appointmentId) !== String(this.appointmentId)) {
            return;
        }

        const callType = signal.data && signal.data.callType ? signal.data.callType : 'video';
        const typeLabel = callType === 'audio' ? '语音' : '视频';

        if (signal.type === 'call') {
            if (!this.consumeCallSessionIfNew(signal)) return;
            // 收到通话请求
            this.callType = callType;
            this.callState = 'calling';
            this.showCallNotification({
                type: 'calling',
                title: `收到${typeLabel}通话请求`,
                subtitle: '来自家长',
                showActions: true,
                callType: callType
            });
        } else if (signal.type === 'accept') {
            // 对方已接听
            this.callState = 'connected';
            this.callStartTime = Date.now();
            this.showCallNotification({
                type: 'connected',
                title: '对方已接听',
                subtitle: '通话已连通'
            });
            // 3秒后移除通知
            setTimeout(() => this.hideCallNotification(), 3000);
        } else if (signal.type === 'end') {
            // 通话结束
            this.handleCallEnded('ended');
        } else if (signal.type === 'reject') {
            // 对方拒绝
            this.callState = 'rejected';
            this.showCallNotification({
                type: 'rejected',
                title: '家长拒绝了通话',
                subtitle: typeLabel + '通话未接通'
            });
            setTimeout(() => this.hideCallNotification(), 4000);
        }
    },

    // 显示通话状态通知（像微信那样）
    showCallNotification(options) {
        const container = document.getElementById('callNotificationContainer');
        if (!container) return;

        // 移除之前的通知
        this.hideCallNotification();

        const notification = document.createElement('div');
        notification.className = 'call-notification';
        notification.id = 'currentCallNotification';

        // 图标 SVG
        const iconSvg = this.getCallIconSvg(options.type);
        // 振铃动画点
        const ringingDots = options.type === 'calling'
            ? '<span class="call-ringing-dots"><span></span><span></span><span></span></span>'
            : '';

        let actionsHtml = '';
        if (options.showActions) {
            actionsHtml = `
                <div class="call-notification-actions">
                    <button class="call-action-btn accept" onclick="Chat.acceptCall('${options.callType}')">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 9a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 0h3a2 2 0 0 1 2 1.72"/>
                        </svg>
                        接听
                    </button>
                    <button class="call-action-btn reject" onclick="Chat.rejectCall()">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                            <line x1="18" y1="6" x2="6" y2="18"/>
                            <line x1="6" y1="6" x2="18" y2="18"/>
                        </svg>
                        拒绝
                    </button>
                </div>
            `;
        }

        notification.innerHTML = `
            <div class="call-notification-icon ${options.type}">
                ${iconSvg}
            </div>
            <div class="call-notification-content">
                <div class="call-notification-title">${options.title}${ringingDots}</div>
                <div class="call-notification-subtitle">${options.subtitle}</div>
                ${actionsHtml}
            </div>
        `;

        container.appendChild(notification);

        // 3D 动画效果
        notification.style.animation = 'callNotificationSlide 0.4s cubic-bezier(0.34, 1.56, 0.64, 1)';
    },

    // 获取通话图标 SVG
    getCallIconSvg(type) {
        const videoIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="23 7 16 12 23 17 23 7"/>
            <rect x="1" y="5" width="15" height="14" rx="2"/>
        </svg>`;
        const phoneIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 9a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 0h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 7.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
        </svg>`;
        const checkIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="20 6 9 17 4 12"/>
        </svg>`;
        const endIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
        </svg>`;

        if (type === 'calling') {
            return this.callType === 'audio' ? phoneIcon : videoIcon;
        } else if (type === 'connected') {
            return checkIcon;
        } else if (type === 'rejected' || type === 'ended') {
            return endIcon;
        }
        return videoIcon;
    },

    // 隐藏通话通知
    hideCallNotification() {
        const notification = document.getElementById('currentCallNotification');
        if (notification) {
            notification.classList.add('fade-out');
            setTimeout(() => notification.remove(), 300);
        }
    },

    // 接听通话
    acceptCall(callType) {
        this.hideCallNotification();
        const params = new URLSearchParams({
            appointmentId: this.appointmentId,
            type: callType || this.callType || 'video',
            role: 'callee'
        });
        window.location.href = `video-call.html?${params.toString()}`;
    },

    // 拒绝通话
    rejectCall() {
        this.hideCallNotification();
        this.callState = 'rejected';
        if (this.stompClient) {
            this.sendWebRTCSignal('reject', String(this.appointment.parentUserId), this.appointmentId);
        }
        Utils.showToast('已拒绝通话', 'info');
    },

    // 处理通话结束
    handleCallEnded(type) {
        const duration = this.callStartTime ? Math.floor((Date.now() - this.callStartTime) / 1000) : 0;
        const minutes = Math.floor(duration / 60);
        const seconds = duration % 60;
        const durationStr = minutes > 0 ? `${minutes}分${seconds}秒` : `${seconds}秒`;

        const typeLabel = this.callType === 'audio' ? '语音' : '视频';

        if (type === 'ended') {
            this.showCallNotification({
                type: 'ended',
                title: `${typeLabel}通话已结束`,
                subtitle: `通话时长 ${durationStr}`
            });
            // 添加通话结束系统消息到聊天记录
            this.addCallEndedMessage(typeLabel, duration);
        }
        setTimeout(() => this.hideCallNotification(), 5000);
    },

    // 添加通话结束消息到聊天记录
    addCallEndedMessage(callType, durationSeconds) {
        const minutes = Math.floor(durationSeconds / 60);
        const secs = durationSeconds % 60;
        const durationStr = `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;

        const message = {
            id: 'call_' + Date.now(),
            type: 'call_ended',
            content: `CALL_ENDED:${callType === '语音' ? 'audio' : 'video'}:${durationSeconds}:${durationStr}`,
            timestamp: Date.now(),
            fromConsultant: null
        };

        this.messages.push(message);
        const messagesArea = document.getElementById('messagesArea');
        if (messagesArea) {
            if (messagesArea.querySelector('.empty-messages')) {
                messagesArea.innerHTML = '';
            }
            messagesArea.insertAdjacentHTML('beforeend', this.createMessageElement(message));
            this.scrollToBottom();
        }

        this.callState = null;
        this.callStartTime = null;
    },

    promptIncomingCall(signal) {
        const callType = signal.data && signal.data.callType ? signal.data.callType : 'video';
        const label = callType === 'audio' ? '语音' : '视频';
        const accept = Utils.confirm(`收到${label}通话请求，是否接听？`);
        const appointmentId = signal.appointmentId || this.appointmentId;

        if (!accept) {
            this.sendWebRTCSignal('reject', signal.fromUserId, appointmentId);
            return;
        }

        const params = new URLSearchParams({
            appointmentId: appointmentId,
            type: callType,
            incoming: '1',
            fromUserId: signal.fromUserId
        });
        window.location.href = `video-call.html?${params.toString()}`;
    },

    sendWebRTCSignal(type, toUserId, appointmentId, data = null) {
        if (!this.stompClient) return;
        const payload = {
            type: type,
            fromUserId: this.consultant.userId,
            toUserId: toUserId,
            appointmentId: appointmentId,
            data: data
        };
        this.stompClient.send('/app/webrtc.signal', {}, JSON.stringify(payload));
    },

    // 显示消息
    displayMessages() {
        const messagesArea = document.getElementById('messagesArea');
        if (!messagesArea) return;

        if (this.messages.length === 0) {
            messagesArea.innerHTML = `
                <div class="empty-messages">
                    <div class="empty-messages-icon">💬</div>
                    <div class="empty-messages-text">暂无消息，开始对话吧</div>
                </div>
            `;
            return;
        }

        messagesArea.innerHTML = this.messages.map(msg => this.createMessageElement(msg)).join('');
        this.scrollToBottom();
    },

    // 创建消息元素
    createMessageElement(msg) {
        const isSystem = msg.type === 'system';
        const fromConsultant = msg.fromConsultant;
        const time = Utils.formatDate(msg.timestamp, 'HH:mm'); // 仅显示时间

        // 通话结束消息
        if (msg.type === 'call_ended' || (msg.content && msg.content.startsWith('CALL_ENDED:'))) {
            let content = msg.content || '';
            if (!content.startsWith('CALL_ENDED:')) {
                content = msg.content;
            }
            const parts = content.split(':');
            const callType = parts[1] === 'audio' ? '语音' : '视频';
            const durationStr = parts[3] || parts[2] || '0秒';
            const icon = parts[1] === 'audio'
                ? `<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 9a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 0h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 7.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>`
                : `<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2"/></svg>`;
            return `
                <div class="message message-system message-call-ended" data-id="${msg.id}">
                    <div class="message-content">
                        <div class="message-bubble call-ended-bubble">
                            <span class="call-ended-icon">${icon}</span>
                            <span class="call-ended-text">${callType}通话已结束，时长 ${durationStr}</span>
                        </div>
                    </div>
                </div>
            `;
        }

        if (isSystem) {
            if (msg.content && String(msg.content).startsWith('CALL:')) {
                return '';
            }
            return `
                <div class="message message-system" data-id="${msg.id}">
                    <div class="message-content">
                        <div class="message-bubble">${msg.content}</div>
                    </div>
                </div>
            `;
        }

        const avatarColor = fromConsultant
            ? (this.consultant.avatarColor || Utils.getAvatarColor(this.consultant.name))
            : Utils.getAvatarColor(this.appointment.clientName);

        const avatarText = fromConsultant
            ? Utils.getInitials(this.consultant.name)
            : Utils.getInitials(this.appointment.clientName);

        const messageClass = fromConsultant ? 'from-consultant' : 'from-client';

        return `
            <div class="message ${messageClass}" data-id="${msg.id}">
                <div class="message-avatar" style="background: ${avatarColor}">
                    ${avatarText}
                </div>
                <div class="message-content">
                    <div class="message-bubble">${this.escapeHtml(msg.content)}</div>
                    <div class="message-time">${time}</div>
                </div>
            </div>
        `;
    },

    // 发送消息
    async sendMessage() {
        if (this.isReadonly) {
            Utils.showToast('只读模式，无法发送消息', 'warning');
            return;
        }

        const input = document.getElementById('messageInput');
        if (!input) return;

        const content = input.value.trim();
        if (!content) {
            Utils.showToast('请输入消息内容', 'warning');
            return;
        }

        // 构造消息对象
        const message = {
            appointmentId: this.appointmentId,
            senderUserId: this.consultant.userId, // 使用 consultant.userId (关联的用户ID) 而不是 consultant.id
            receiverUserId: this.appointment.parentUserId,
            messageType: 'TEXT',
            content: content,
            isFromConsultant: true
        };

        try {
            // 通过 API 发送消息
            await API.post('/messages', message);
            
            // 清空输入框
            input.value = '';
            input.style.height = 'auto';
            
            // 注意：不手动添加到 messages 数组，等待 WebSocket 推送回来
            
        } catch (error) {
            console.error('发送消息失败', error);
            Utils.showToast('发送消息失败', 'error');
        }
    },

    // 滚动到底部
    scrollToBottom() {
        const messagesArea = document.getElementById('messagesArea');
        if (messagesArea) {
            messagesArea.scrollTop = messagesArea.scrollHeight;
        }
    },

    // 转义HTML
    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    },
    
    // 设置只读模式
    setReadonlyMode() {
        const inputArea = document.querySelector('.chat-input-area');
        if (inputArea) {
            inputArea.style.display = 'none';
        }
    },
    
    // 结束咨询
    async endConsultation() {
        if (this.isReadonly) return;
        
        if (Utils.confirm('确定要结束本次咨询吗？')) {
            let recordError = null;
            try {
                const payload = this.buildConsultationRecordPayload();
                await API.post(`/consultation-records/appointment/${this.appointmentId}`, payload);
                Storage.remove(CONFIG.STORAGE_KEYS.CONSULTATION_SESSION);
            } catch (error) {
                recordError = error;
            }
            try {
                await API.put(`/appointments/${this.appointmentId}`, {
                    status: 'COMPLETED'
                });
                if (recordError) {
                    Utils.showToast('咨询已结束，但记录保存失败', 'warning');
                } else {
                    Utils.showToast('咨询已结束', 'success');
                }
                setTimeout(() => window.location.href = 'dashboard.html', 1500);
            } catch (error) {
                console.error('结束咨询失败', error);
                Utils.showToast('操作失败', 'error');
            }
        }
    },
    
    buildConsultationRecordPayload() {
        const consultationType = this.getConsultationType();
        const duration = this.getConsultationDurationMinutes();
        const payload = {
            appointmentId: Number(this.appointmentId),
            consultantId: this.appointment.consultantId,
            parentUserId: this.appointment.parentUserId,
            childId: this.appointment.childId,
            consultationType
        };
        if (duration !== null) {
            payload.duration = duration;
        }
        return payload;
    },

    getConsultationType() {
        const callType = this.lastCallInfo && this.lastCallInfo.callType
            ? this.lastCallInfo.callType
            : null;
        if (callType === 'VIDEO') return 'VIDEO';
        if (callType === 'AUDIO') return 'AUDIO';
        return 'ONLINE';
    },

    getConsultationDurationMinutes() {
        if (this.lastCallInfo && typeof this.lastCallInfo.durationSeconds === 'number') {
            return Math.max(0, Math.ceil(this.lastCallInfo.durationSeconds / 60));
        }
        if (!this.consultationStartAt) return null;
        const seconds = Math.floor((Date.now() - this.consultationStartAt) / 1000);
        return Math.max(0, Math.ceil(seconds / 60));
    },

    startVideoCall() {
        if (this.appointmentId) {
            Storage.set(CONFIG.STORAGE_KEYS.CONSULTATION_SESSION, {
                appointmentId: this.appointmentId,
                callType: 'VIDEO',
                startedAt: Date.now()
            });
            window.location.href = `video-call.html?appointmentId=${this.appointmentId}&type=video&role=caller`;
        } else {
            Utils.showToast('无法发起通话，预约ID不存在', 'error');
        }
    },

    startAudioCall() {
        if (this.appointmentId) {
            Storage.set(CONFIG.STORAGE_KEYS.CONSULTATION_SESSION, {
                appointmentId: this.appointmentId,
                callType: 'AUDIO',
                startedAt: Date.now()
            });
            window.location.href = `video-call.html?appointmentId=${this.appointmentId}&type=audio&role=caller`;
        } else {
            Utils.showToast('无法发起通话，预约ID不存在', 'error');
        }
    }
};

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', () => {
    Chat.init();
    
    // 绑定发送按钮事件
    const sendBtn = document.getElementById('sendMessageBtn');
    if (sendBtn) {
        sendBtn.addEventListener('click', () => Chat.sendMessage());
    }
    
    // 绑定回车发送
    const input = document.getElementById('messageInput');
    if (input) {
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                Chat.sendMessage();
            }
        });
    }
});
