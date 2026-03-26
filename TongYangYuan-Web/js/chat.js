// 童养园咨询师端 - 聊天逻辑

const Chat = {
    appointmentId: null,
    appointment: null,
    consultant: null,
    messages: [],
    stompClient: null,
    isReadonly: false,
    consultationStartAt: null,
    lastCallInfo: null,

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

        // Token 放入 STOMP CONNECT 帧的 Authorization 头，供 WebSocketChannelInterceptor 验证
        this.stompClient.connect(
            { 'Authorization': 'Bearer ' + token },
            (frame) => {
                console.log('STOMP Connected: ' + frame);

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
                // 断线重连
                setTimeout(() => this.connectWebSocket(), 5000);
            }
        );
    },

    // 处理收到的消息
    handleIncomingMessage(rawMsg) {
        // 过滤非当前预约的消息（虽然通常后端会定向推送，但多重保险）
        if (rawMsg.appointmentId && String(rawMsg.appointmentId) !== String(this.appointmentId)) {
            return;
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

    handleWebRTCSignal(signal) {
        if (!signal || !signal.type) return;
        if (signal.appointmentId && String(signal.appointmentId) !== String(this.appointmentId)) {
            return;
        }
        if (signal.type === 'call') {
            this.promptIncomingCall(signal);
        } else if (signal.type === 'end') {
            Utils.showToast('对方已挂断', 'info');
        }
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

        if (isSystem) {
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
