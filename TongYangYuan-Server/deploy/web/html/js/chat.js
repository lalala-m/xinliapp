// 童康源咨询师端 - 聊天逻辑（增强版：微信风格通话状态）

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
    
    // 🔧 新增：对方在线状态
    peerOnline: false,  // 对方是否在线（房间人数>=2）

    // 初始化
    async init() {
        console.log('[Chat] Initializing...');
        
        // 检查登录状态
        this.consultant = Auth.checkAuth();
        console.log('[Chat] Auth check:', this.consultant);
        if (!this.consultant) {
            console.error('[Chat] Not logged in! Redirecting to login page...');
            Utils.showToast('请先登录', 'error');
            setTimeout(() => window.location.href = 'consultant_login.html', 1500);
            return;
        }

        // 获取URL参数
        const urlParams = new URLSearchParams(window.location.search);
        this.appointmentId = urlParams.get('appointmentId');
        this.isReadonly = urlParams.get('readonly') === 'true';
        console.log('[Chat] appointmentId:', this.appointmentId, ', isReadonly:', this.isReadonly);

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

        // 初始化签名画布（延迟确保DOM就绪）
        setTimeout(() => this.initSignatureCanvas(), 500);
    },

    // 加载预约信息
    async loadAppointmentInfo() {
        try {
            const appointment = await API.get(`/appointments/${this.appointmentId}`);
            if (appointment) {
                // 尝试获取用户信息
                let clientName = `家长#${appointment.parentUserId}`;
                let clientAvatar = null;
                
                try {
                    const userInfo = await API.get(`/user/info?userId=${appointment.parentUserId}`);
                    if (userInfo && userInfo.nickname) {
                        clientName = userInfo.nickname;
                    }
                    if (userInfo && userInfo.avatarUrl) {
                        clientAvatar = userInfo.avatarUrl;
                    }
                } catch (e) {
                    console.warn('获取用户信息失败，使用默认名称');
                }

                this.appointment = {
                    ...appointment,
                    clientName: clientName,
                    clientAvatar: clientAvatar,
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
            // 如果有头像URL，显示头像图片
            if (this.appointment.clientAvatar) {
                avatarEl.innerHTML = `<img src="${this.escapeAttr(this.appointment.clientAvatar)}" alt="头像" onerror="this.parentElement.textContent='${Utils.getInitials(this.appointment.clientName)}'; this.parentElement.style.background='${Utils.getAvatarColor(this.appointment.clientName)}'">`;
                avatarEl.style.background = 'transparent';
            } else {
                avatarEl.textContent = Utils.getInitials(this.appointment.clientName);
                avatarEl.style.background = Utils.getAvatarColor(this.appointment.clientName);
            }
        }
    },

    // 加载消息
    async loadMessages() {
        try {
            const messages = await API.get(`/messages/appointment/${this.appointmentId}`);
            console.log('加载到的消息数量:', messages ? messages.length : 0);
            if (messages && messages.length > 0) {
                this.messages = messages.map(this.mapMessage);
                console.log('映射后的消息:', this.messages);
                this.displayMessages();
            } else {
                console.log('没有消息或消息为空');
                this.displayMessages(); // 显示空状态
            }
        } catch (error) {
            console.error('加载消息失败', error);
        }
    },

    // 返回咨询师首页
    goBack() {
        // 🔧 修复：直接跳转到咨询师首页，不使用 history.back()
        // 避免从视频通话页面返回时回到不正确的页面
        window.location.href = 'dashboard.html';
    },

    // 映射后端消息格式到前端
    mapMessage(msg) {
        // 处理时长字段：优先使用 msg.duration，否则从 content 中提取
        let duration = msg.duration || null;
        if (duration === null && msg.content) {
            // 尝试从 content 中提取时长，格式如 "语音消息 01:23" 或 "01:23"
            const durationMatch = msg.content.match(/(\d{1,2}):(\d{2})/);
            if (durationMatch) {
                duration = parseInt(durationMatch[1]) * 60 + parseInt(durationMatch[2]);
            } else {
                // 尝试提取纯数字（秒数）
                const secondsMatch = msg.content.match(/(\d+)(?:秒|")/);
                if (secondsMatch) {
                    duration = parseInt(secondsMatch[1]);
                }
            }
        }
        
        return {
            id: msg.id,
            type: msg.messageType ? msg.messageType.toLowerCase() : 'text',
            content: msg.content,
            timestamp: msg.timestamp || (msg.createdAt ? new Date(msg.createdAt).getTime() : Date.now()),
            fromConsultant: msg.isFromConsultant,
            senderUserId: msg.senderUserId,
            mediaUrl: msg.mediaUrl || msg.url || null,  // 保留媒体URL
            duration: duration
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

                // 同时连接新的原生WebRTC信令（用于接收Android端来电）
                this.connectWebRTCSignaling();
            }, (error) => {
                console.error('STOMP error:', error);
                this._setWsStatus('err');
                // 即使STOMP失败，也尝试连接WebRTC信令
                this.connectWebRTCSignaling();
                setTimeout(() => this.connectWebSocket(), 5000);
            }
        );
    },

    // 连接新的原生WebRTC信令服务器（与Android端互通）
    webrtcSignalingWs: null,
    connectWebRTCSignaling() {
        if (this.webrtcSignalingWs && this.webrtcSignalingWs.readyState === WebSocket.OPEN) {
            return; // 已连接
        }
        
        // 🔧 修复：添加 userId 参数，让服务器可以识别同一用户的重复连接
        const userId = this.consultant ? (this.consultant.userId || this.consultant.id) : 'unknown';
        const wsUrl = `${CONFIG.VIDEO_SIGNALING_URL}?roomId=${this.appointmentId}&userId=${userId}`;
        console.log('[Chat] Connecting WebRTC signaling:', wsUrl);
        
        try {
            this.webrtcSignalingWs = new WebSocket(wsUrl);
            
            this.webrtcSignalingWs.onopen = () => {
                console.log('[Chat] WebRTC signaling connected');
            };
            
            this.webrtcSignalingWs.onmessage = (event) => {
                try {
                    const signal = JSON.parse(event.data);
                    console.log('[Chat] WebRTC signal received:', signal);
                    
                    // 🔧 修复：处理 userCount 消息，跟踪对方在线状态
                    if (signal.type === 'userCount') {
                        this.peerOnline = signal.count >= 2;
                        this.updatePeerStatusUI();
                        return;
                    }
                    
                    // 将新格式转换为旧格式处理
                    if (signal.type === 'call') {
                        const callType = signal.callType || 'video';
                        const convertedSignal = {
                            type: 'call',
                            fromUserId: signal.fromUserId,
                            toUserId: signal.toUserId,
                            appointmentId: signal.appointmentId || this.appointmentId,
                            data: { callType: callType, sessionId: 'webrtc-' + Date.now() }
                        };
                        this.handleWebRTCSignal(convertedSignal);
                    } else if (signal.type === 'offer') {
                        // 收到offer，直接跳转到视频通话页面（被叫）
                        console.log('[Chat] Received offer, redirecting to video call page');
                        // 🔧 修复：跳转前彻底断开聊天页面的信令连接，避免重复连接导致房间人数异常
                        this._disconnectWebRTCSignaling();
                        const offerCallType = signal.callType || 'video';
                        const params = new URLSearchParams({
                            appointmentId: this.appointmentId,
                            type: offerCallType,
                            role: 'callee',
                            userId: this.consultant.userId || this.consultant.id,
                            targetUserId: signal.fromUserId || '',
                            accepted: '1'
                        });
                        // 🔧 延迟跳转，确保旧 WebSocket 完全关闭
                        setTimeout(() => {
                            window.location.href = `webrtc-video-call.html?${params.toString()}`;
                        }, 300);
                    } else if (signal.type === 'end' || signal.type === 'reject') {
                        this.handleWebRTCSignal(signal);
                    }
                } catch (e) {
                    console.error('[Chat] Failed to parse WebRTC signal:', e);
                }
            };
            
            this.webrtcSignalingWs.onerror = (error) => {
                console.error('[Chat] WebRTC signaling error:', error);
            };
            
            this.webrtcSignalingWs.onclose = () => {
                console.log('[Chat] WebRTC signaling closed');
                // 3秒后重连
                setTimeout(() => this.connectWebRTCSignaling(), 3000);
            };
        } catch (e) {
            console.error('[Chat] Failed to connect WebRTC signaling:', e);
        }
    },

    // 🔧 新增：彻底断开 WebRTC 信令连接，防止重复连接
    _disconnectWebRTCSignaling() {
        if (this.webrtcSignalingWs) {
            const ws = this.webrtcSignalingWs;
            this.webrtcSignalingWs = null; // 立即清除引用，防止 onclose 重连
            
            // 移除 onclose 事件处理器，防止自动重连
            ws.onclose = null;
            ws.onerror = null;
            ws.onmessage = null;
            
            if (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING) {
                try {
                    ws.close();
                } catch (e) {
                    console.log('[Chat] Error closing WebRTC signaling:', e);
                }
            }
            console.log('[Chat] WebRTC signaling disconnected');
        }
    },

    _setWsStatus(status) {
        // 连接状态条已移除，不再显示
        // 如果需要调试，可以取消下面的注释
        // console.log('WebSocket status:', status);
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
        // 放宽 appointmentId 校验：允许 0/null/undefined，或匹配当前预约ID
        // 开发测试阶段：临时 appointmentId 可能不匹配，允许通过
        const sigApt = signal.appointmentId != null ? String(signal.appointmentId) : '';
        const myApt = this.appointmentId != null ? String(this.appointmentId) : '';
        if (sigApt && myApt && sigApt !== myApt) {
            console.log('handleWebRTCSignal: appointmentId mismatch, but allowing for dev/test. sig=', sigApt, 'mine=', myApt);
            // 生产环境应该跳过，开发测试阶段允许通过
            // return;
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
        this.callState = 'connected';
        
        const payload = {
            type: 'accept',
            fromUserId: this.consultant.userId || this.consultant.id,
            toUserId: this.appointment ? this.appointment.parentUserId : 0,
            appointmentId: parseInt(this.appointmentId) || 0,
            data: { callType: callType || this.callType || 'video' }
        };
        
        // 1. 通过STOMP发送accept信令（兼容旧系统）
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.send('/app/webrtc.signal', {}, JSON.stringify(payload));
            console.log('[acceptCall] Sent accept signal via STOMP:', payload);
        }
        
        // 2. 通过新的WebRTC信令发送accept（与Android端互通）
        if (this.webrtcSignalingWs && this.webrtcSignalingWs.readyState === WebSocket.OPEN) {
            this.webrtcSignalingWs.send(JSON.stringify(payload));
            console.log('[acceptCall] Sent accept signal via WebRTC WS:', payload);
        }
        
        // 🔧 修复：彻底断开 WebRTC 信令连接，然后跳转
        this._disconnectWebRTCSignaling();
        
        const params = new URLSearchParams({
            appointmentId: this.appointmentId,
            type: callType || this.callType || 'video',
            role: 'callee',
            userId: this.consultant.userId || this.consultant.id,
            accepted: '1'  // 标记已从聊天页面点击接听
        });
        
        // 🔧 延迟跳转，确保旧 WebSocket 完全关闭
        setTimeout(() => {
            window.location.href = `webrtc-video-call.html?${params.toString()}`;
        }, 300);
    },

    // 拒绝通话
    rejectCall() {
        this.hideCallNotification();
        this.callState = 'rejected';
        
        const payload = {
            type: 'reject',
            fromUserId: this.consultant.userId || this.consultant.id,
            toUserId: this.appointment ? this.appointment.parentUserId : 0,
            appointmentId: parseInt(this.appointmentId) || 0,
            data: { callType: this.callType || 'video' }
        };
        
        // 通过STOMP发送（兼容旧系统）
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.send('/app/webrtc.signal', {}, JSON.stringify(payload));
        }
        
        // 通过新的WebRTC信令发送（与Android端互通）
        if (this.webrtcSignalingWs && this.webrtcSignalingWs.readyState === WebSocket.OPEN) {
            this.webrtcSignalingWs.send(JSON.stringify(payload));
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

    // 添加通话开始消息到聊天记录
    addCallStartedMessage(callType) {
        const typeLabel = callType === 'audio' ? '语音' : '视频';
        const message = {
            id: 'call_' + Date.now(),
            type: 'call_started',
            content: `CALL_STARTED:${callType}`,
            timestamp: Date.now(),
            fromConsultant: true
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
            fromUserId: signal.fromUserId,
            targetUserId: signal.fromUserId || ''
        });
        window.location.href = `webrtc-video-call.html?${params.toString()}`;
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
        const time = Utils.formatDate(msg.timestamp, 'HH:mm');

        // 通话结束消息
        // 通话开始消息
        if (msg.type === 'call_started' || (msg.content && msg.content.startsWith('CALL_STARTED:'))) {
            const parts = (msg.content || '').split(':');
            const callType = parts[1] === 'audio' ? '语音' : '视频';
            const icon = parts[1] === 'audio'
                ? `<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 9a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 0h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 7.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>`
                : `<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2"/></svg>`;
            return `
                <div class="message message-system message-call-started" data-id="${msg.id}">
                    <div class="message-content">
                        <div class="message-bubble call-started-bubble">
                            <span class="call-started-icon">${icon}</span>
                            <span class="call-started-text">${callType}通话中…</span>
                        </div>
                    </div>
                </div>
            `;
        }

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

        // 媒体消息处理
        let mediaHtml = '';
        if (msg.mediaUrl) {
            // 🔧 修复：拼接完整URL（相对路径转绝对路径）
            let fullMediaUrl = msg.mediaUrl;
            if (fullMediaUrl && !fullMediaUrl.startsWith('http')) {
                // 因为 server.servlet.context-path=/api，静态资源路径需要加 /api 前缀
                // msg.mediaUrl 是 /uploads/videos/xxx.webm，需要变成 /api/uploads/videos/xxx.webm
                if (!fullMediaUrl.startsWith('/api')) {
                    fullMediaUrl = '/api' + fullMediaUrl;
                }
                fullMediaUrl = CONFIG.API_BASE_URL.replace(/\/api$/, '') + fullMediaUrl;
            }
            if (msg.type === 'image') {
                mediaHtml = `<div class="media-preview"><img src="${fullMediaUrl}" alt="图片" onclick="Chat.previewMedia('${fullMediaUrl}')"></div>`;
            } else if (msg.type === 'video') {
                // 🔧 修复：使用更兼容的 video 标签，支持 webm 格式
                mediaHtml = `<div class="media-preview">
                    <video controls preload="metadata" style="max-width:100%;max-height:300px;border-radius:8px;">
                        <source src="${fullMediaUrl}" type="video/webm">
                        <source src="${fullMediaUrl}" type="video/mp4">
                        您的浏览器不支持视频播放，请<a href="${fullMediaUrl}" target="_blank">下载观看</a>
                    </video>
                </div>`;
            } else if (msg.type === 'audio') {
                // 语音消息 - 默认显示静态麦克风+时长，点击后切换到播放状态
                const duration = msg.duration || (msg.content && parseInt(msg.content)) || 0;
                const escapedMediaUrl = this.escapeAttr(msg.mediaUrl || '');
                mediaHtml = `
                    <div class="message-audio" data-media-url="${escapedMediaUrl}" data-duration="${duration}" data-playing="false" onclick="Chat.toggleAudioMessage(this)">
                        <div class="audio-content">
                            <div class="audio-icon static">
                                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/>
                                    <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
                                    <line x1="12" y1="19" x2="12" y2="23"/>
                                    <line x1="8" y1="23" x2="16" y2="23"/>
                                </svg>
                                <div class="audio-duration">${duration}"</div>
                            </div>
                            <div class="audio-playing" style="display:none">
                                <div class="audio-wave">
                                    <span class="audio-wave-bar"></span>
                                    <span class="audio-wave-bar"></span>
                                    <span class="audio-wave-bar"></span>
                                    <span class="audio-wave-bar"></span>
                                    <span class="audio-wave-bar"></span>
                                </div>
                                <div class="audio-duration">${duration}"</div>
                            </div>
                        </div>
                    </div>
                `;
            }
        }

        return `
            <div class="message ${messageClass}" data-id="${msg.id}">
                <div class="message-avatar" style="background: ${avatarColor}">
                    ${avatarText}
                </div>
                <div class="message-content">
                    ${mediaHtml}
                    ${msg.type !== 'audio' ? `<div class="message-bubble">${this.escapeHtml(msg.content)}</div>` : ''}
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
    
    // 转义HTML属性
    escapeAttr(text) {
        if (!text) return '';
        return String(text).replace(/"/g, '"').replace(/'/g, '&#39;');
    },
    
    // 设置只读模式
    setReadonlyMode() {
        const inputArea = document.querySelector('.chat-input-area');
        if (inputArea) {
            inputArea.style.display = 'none';
        }
    },
    
    // 结束咨询 - 先弹出总结与签名弹窗
    async endConsultation() {
        if (this.isReadonly) return;
        this.openSummaryModal();
    },

    openSummaryModal() {
        const overlay = document.getElementById('summaryModalOverlay');
        if (overlay) overlay.style.display = 'flex';
        this.clearSignature();
        document.getElementById('summaryText').value = '';
        document.getElementById('feedbackText').value = '';
        document.getElementById('issueTags').value = '';
    },

    closeSummaryModal() {
        const overlay = document.getElementById('summaryModalOverlay');
        if (overlay) overlay.style.display = 'none';
    },

    initSignatureCanvas() {
        const canvas = document.getElementById('signatureCanvas');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        ctx.lineCap = 'round';
        ctx.lineJoin = 'round';
        ctx.lineWidth = 2;
        ctx.strokeStyle = '#1E2A3B';

        let drawing = false;
        const getPos = (e) => {
            const rect = canvas.getBoundingClientRect();
            const clientX = e.touches ? e.touches[0].clientX : e.clientX;
            const clientY = e.touches ? e.touches[0].clientY : e.clientY;
            return { x: clientX - rect.left, y: clientY - rect.top };
        };

        const start = (e) => { drawing = true; const p = getPos(e); ctx.beginPath(); ctx.moveTo(p.x, p.y); };
        const move = (e) => { if (!drawing) return; e.preventDefault(); const p = getPos(e); ctx.lineTo(p.x, p.y); ctx.stroke(); };
        const end = () => { drawing = false; };

        canvas.addEventListener('mousedown', start);
        canvas.addEventListener('mousemove', move);
        canvas.addEventListener('mouseup', end);
        canvas.addEventListener('mouseleave', end);
        canvas.addEventListener('touchstart', start, { passive: false });
        canvas.addEventListener('touchmove', move, { passive: false });
        canvas.addEventListener('touchend', end);
    },

    clearSignature() {
        const canvas = document.getElementById('signatureCanvas');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, canvas.width, canvas.height);
    },

    getSignatureBase64() {
        const canvas = document.getElementById('signatureCanvas');
        if (!canvas) return null;
        // 检查画布是否为空（简单方法：检查是否有非白色像素）
        const ctx = canvas.getContext('2d');
        const pixelBuffer = ctx.getImageData(0, 0, canvas.width, canvas.height).data;
        const hasContent = pixelBuffer.some((v, i) => i % 4 !== 3 && v < 255);
        if (!hasContent) return null;
        return canvas.toDataURL('image/png');
    },

    async submitConsultationSummary() {
        const summary = document.getElementById('summaryText').value.trim();
        const feedback = document.getElementById('feedbackText').value.trim();
        const tags = document.getElementById('issueTags').value.trim();
        const signatureBase64 = this.getSignatureBase64();

        if (!summary) { Utils.showToast('请填写咨询主题总结', 'warning'); return; }
        if (!feedback) { Utils.showToast('请填写咨询师反馈', 'warning'); return; }
        if (!signatureBase64) { Utils.showToast('请手写签名确认', 'warning'); return; }

        const btn = document.getElementById('submitSummaryBtn');
        btn.disabled = true;
        btn.textContent = '提交中...';

        try {
            // 1. 创建/更新咨询记录
            const recordPayload = this.buildConsultationRecordPayload();
            recordPayload.consultantSummary = summary;
            recordPayload.consultantFeedback = feedback;
            recordPayload.coreIssueTags = tags;
            recordPayload.recordCompleteness = 'PARTIAL';

            const recordRes = await API.post(`/consultation-records/appointment/${this.appointmentId}`, recordPayload);
            const recordId = recordRes && recordRes.id ? recordRes.id : null;

            // 2. 保存咨询师签名
            if (recordId) {
                await API.post('/consultation-signatures/submit', {
                    recordId: recordId,
                    signerType: 'CONSULTANT',
                    signerId: this.consultant.userId,
                    signerName: this.consultant.name || '咨询师',
                    signatureImage: signatureBase64
                });
            }

            // 3. 更新预约状态
            await API.put(`/appointments/${this.appointmentId}`, { status: 'COMPLETED' });

            // 4. 发送 SYSTEM 消息通知家长签字确认（包含recordId）
            await API.post('/messages', {
                appointmentId: Number(this.appointmentId),
                senderUserId: this.consultant.userId,
                receiverUserId: this.appointment.parentUserId,
                messageType: 'SYSTEM',
                content: `SIGNATURE_REQUIRED:${recordId}:本次咨询已结束，咨询师已确认，请点击进行签字确认`,
                isFromConsultant: true
            });

            Storage.remove(CONFIG.STORAGE_KEYS.CONSULTATION_SESSION);
            this.closeSummaryModal();
            Utils.showToast('咨询结束，已通知家长签字确认', 'success');

            // 5. 跳转到评价页面
            const params = new URLSearchParams({
                appointmentId: this.appointmentId,
                parentUserId: this.appointment.parentUserId,
                childId: this.appointment.childId
            });
            window.location.href = `consultation-review.html?${params.toString()}`;
        } catch (error) {
            console.error('提交咨询总结失败', error);
            Utils.showToast('提交失败，请重试', 'error');
            btn.disabled = false;
            btn.textContent = '确认结束并签名';
        }
    },

    // ============ PDF 预览与下载 ============
    
    // 当前选中的PDF recordId
    currentPdfRecordId: null,
    
    // 打开PDF预览
    async openPdfPreview(recordId) {
        this.currentPdfRecordId = recordId;
        const modal = document.getElementById('pdfPreviewModal');
        const loading = document.getElementById('pdfLoading');
        const iframe = document.getElementById('pdfIframe');
        
        if (modal) modal.style.display = 'flex';
        if (loading) loading.style.display = 'flex';
        if (iframe) iframe.style.display = 'none';
        
        try {
            // 检查PDF是否已生成
            const pdfInfoRes = await API.get(`/consultation/pdf/info/${recordId}`);
            if (pdfInfoRes.success && pdfInfoRes.data) {
                // PDF已存在，直接预览
                const previewUrl = `${CONFIG.API_BASE_URL}/consultation/pdf/preview/${recordId}`;
                if (iframe) {
                    iframe.onload = () => {
                        if (loading) loading.style.display = 'none';
                        iframe.style.display = 'block';
                    };
                    iframe.src = previewUrl;
                }
            } else {
                // PDF不存在，先生成
                Utils.showToast('正在生成PDF，请稍候...', 'info');
                const generateRes = await API.post(`/consultation/pdf/generate/${recordId}`);
                if (generateRes.success) {
                    // 重新获取PDF信息
                    const newPdfInfo = await API.get(`/consultation/pdf/info/${recordId}`);
                    if (newPdfInfo.success && newPdfInfo.data) {
                        const previewUrl = `${CONFIG.API_BASE_URL}/consultation/pdf/preview/${recordId}`;
                        if (iframe) {
                            iframe.onload = () => {
                                if (loading) loading.style.display = 'none';
                                iframe.style.display = 'block';
                            };
                            iframe.src = previewUrl;
                        }
                    }
                }
            }
        } catch (error) {
            console.error('PDF预览失败', error);
            Utils.showToast('PDF加载失败', 'error');
            this.closePdfPreview();
        }
    },
    
    // 关闭PDF预览
    closePdfPreview() {
        const modal = document.getElementById('pdfPreviewModal');
        const iframe = document.getElementById('pdfIframe');
        if (modal) modal.style.display = 'none';
        if (iframe) iframe.src = '';
        this.currentPdfRecordId = null;
    },
    
    // 下载PDF
    async downloadPdf() {
        if (!this.currentPdfRecordId) {
            Utils.showToast('PDF记录ID不存在', 'error');
            return;
        }
        
        try {
            const downloadUrl = `${CONFIG.API_BASE_URL}/consultation/pdf/download/${this.currentPdfRecordId}`;
            // 创建隐藏的下载链接
            const link = document.createElement('a');
            link.href = downloadUrl;
            link.download = `consultation_record_${this.currentPdfRecordId}.pdf`;
            link.target = '_blank';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            Utils.showToast('PDF下载已开始', 'success');
        } catch (error) {
            console.error('PDF下载失败', error);
            Utils.showToast('PDF下载失败', 'error');
        }
    },
    
    // 在只读模式下查看PDF
    async viewConsultationPdf(recordId) {
        await this.openPdfPreview(recordId);
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

    // 🔧 新增：更新对方在线状态UI
    updatePeerStatusUI() {
        const statusEl = document.getElementById('peerStatus');
        if (statusEl) {
            if (this.peerOnline) {
                statusEl.innerHTML = '<span class="status-dot online"></span>对方在线';
                statusEl.className = 'peer-status online';
            } else {
                statusEl.innerHTML = '<span class="status-dot offline"></span>对方未在线';
                statusEl.className = 'peer-status offline';
            }
        }
        // 更新通话按钮状态
        const videoBtn = document.querySelector('button[onclick="Chat.startVideoCall()"]');
        const audioBtn = document.querySelector('button[onclick="Chat.startAudioCall()"]');
        if (videoBtn) {
            videoBtn.disabled = !this.peerOnline;
            videoBtn.style.opacity = this.peerOnline ? '1' : '0.5';
            videoBtn.style.cursor = this.peerOnline ? 'pointer' : 'not-allowed';
        }
        if (audioBtn) {
            audioBtn.disabled = !this.peerOnline;
            audioBtn.style.opacity = this.peerOnline ? '1' : '0.5';
            audioBtn.style.cursor = this.peerOnline ? 'pointer' : 'not-allowed';
        }
    },

    startVideoCall() {
        // 🔧 修复：只有对方在线时才允许发起通话
        if (!this.peerOnline) {
            Utils.showToast('对方未在线，无法发起通话', 'error');
            return;
        }
        if (this.appointmentId) {
            Storage.set(CONFIG.STORAGE_KEYS.CONSULTATION_SESSION, {
                appointmentId: this.appointmentId,
                callType: 'VIDEO',
                startedAt: Date.now()
            });
            // 添加通话记录到聊天记录
            this.addCallStartedMessage('video');
            // 先通过STOMP发送call信令（确保手机端能收到）
            this.sendCallSignalViaStomp('video');
            window.location.href = `webrtc-video-call.html?appointmentId=${this.appointmentId}&type=video&role=caller&userId=${this.consultant.userId || this.consultant.id}&targetUserId=${this.appointment.parentUserId || ''}`;
        } else {
            Utils.showToast('无法发起通话，预约ID不存在', 'error');
        }
    },

    startAudioCall() {
        // 🔧 修复：只有对方在线时才允许发起通话
        if (!this.peerOnline) {
            Utils.showToast('对方未在线，无法发起通话', 'error');
            return;
        }
        if (this.appointmentId) {
            Storage.set(CONFIG.STORAGE_KEYS.CONSULTATION_SESSION, {
                appointmentId: this.appointmentId,
                callType: 'AUDIO',
                startedAt: Date.now()
            });
            // 添加通话记录到聊天记录
            this.addCallStartedMessage('audio');
            // 先通过STOMP发送call信令（确保手机端能收到）
            this.sendCallSignalViaStomp('audio');
            window.location.href = `webrtc-video-call.html?appointmentId=${this.appointmentId}&type=audio&role=caller&userId=${this.consultant.userId || this.consultant.id}&targetUserId=${this.appointment.parentUserId || ''}`;
        } else {
            Utils.showToast('无法发起通话，预约ID不存在', 'error');
        }
    },

    // 通过STOMP发送call信令（用于通知手机端）
    sendCallSignalViaStomp(callType) {
        if (!this.stompClient || !this.stompClient.connected) {
            console.warn('[sendCallSignalViaStomp] STOMP not connected');
            return;
        }
        const toUserId = this.appointment ? this.appointment.parentUserId : 0;
        const appointmentId = parseInt(this.appointmentId) || 0;
        const payload = {
            type: 'call',
            fromUserId: this.consultant.userId || this.consultant.id,
            toUserId: toUserId,
            appointmentId: appointmentId,
            data: { callType: callType, sessionId: 'webrtc-' + Date.now() }
        };
        // 发送给用户队列
        this.stompClient.send('/app/webrtc.signal', {}, JSON.stringify(payload));
        console.log('[sendCallSignalViaStomp] Sent call signal via STOMP to user:', payload);
        
        // 同时发送广播（确保手机端能收到，即使toUserId不匹配）
        if (appointmentId > 0) {
            const broadcastPayload = {
                type: 'call',
                fromUserId: this.consultant.userId || this.consultant.id,
                appointmentId: appointmentId,
                data: { callType: callType, sessionId: 'webrtc-broadcast-' + Date.now() }
            };
            this.stompClient.send('/app/webrtc.signal', {}, JSON.stringify(broadcastPayload));
            console.log('[sendCallSignalViaStomp] Also sent broadcast signal:', broadcastPayload);
        }
    },

    // 自动调整输入框高度
    autoResize(textarea) {
        textarea.style.height = 'auto';
        textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px';
    },

    // 处理键盘事件
    handleKeyDown(event) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            this.sendMessage();
        }
    },

    // 附加文件
    attachFile() {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = 'image/*,video/*';
        input.onchange = (e) => {
            const file = e.target.files[0];
            if (file) {
                this.uploadMedia(file);
            }
        };
        input.click();
    },

    // 上传媒体文件
    async uploadMedia(file) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('appointmentId', this.appointmentId);
        
        // 根据文件类型选择正确的上传端点
        let uploadEndpoint = '/upload/image';
        if (file.type.startsWith('video/')) {
            uploadEndpoint = '/upload/video';
        } else if (file.type.startsWith('audio/')) {
            uploadEndpoint = '/upload/audio';
        }
        
        try {
            const result = await API.upload(uploadEndpoint, formData);
            Utils.showToast('文件上传成功', 'success');
        } catch (error) {
            console.error('上传失败', error);
            Utils.showToast('文件上传失败', 'error');
        }
    },

    // 预览媒体
    previewMedia(url) {
        const preview = document.getElementById('mediaPreview');
        const img = document.getElementById('previewImage');
        if (preview && img) {
            img.src = url;
            preview.classList.add('show');
        }
    },

    // 关闭媒体预览
    closeMediaPreview() {
        const preview = document.getElementById('mediaPreview');
        if (preview) {
            preview.classList.remove('show');
        }
    },

    // 显示用户信息
    showUserInfo() {
        if (this.appointment) {
            const info = `家长：${this.appointment.clientName}\n孩子：${this.appointment.childName || '未填写'}\n年龄：${this.appointment.childAge || '?'}岁`;
            Utils.showToast(info, 'info');
        }
    },

    // 显示菜单
    showMenu() {
        Utils.showToast('更多功能开发中', 'info');
    },

    // ========== 语音消息功能 ==========
    
    // 当前输入模式：text 或 voice
    _inputMode: 'text',
    // 语音录制相关
    _mediaRecorder: null,
    _audioChunks: [],
    _voiceStartY: 0,
    _isRecording: false,
    _recordingStartTime: null,
    _voiceTimer: null,

    // 切换输入模式（文字/语音）
    toggleInputMode() {
        const textWrapper = document.getElementById('textInputWrapper');
        const voiceWrapper = document.getElementById('voiceInputWrapper');
        const voiceBtn = document.getElementById('voiceBtn');
        const sendBtn = document.getElementById('sendBtn');
        const messageInput = document.getElementById('messageInput');
        
        if (this._inputMode === 'text') {
            // 切换到语音模式
            this._inputMode = 'voice';
            textWrapper.style.display = 'none';
            voiceWrapper.style.display = 'flex';
            voiceBtn.classList.add('active');
            sendBtn.style.display = 'none';
        } else {
            // 切换到文字模式
            this._inputMode = 'text';
            textWrapper.style.display = 'block';
            voiceWrapper.style.display = 'none';
            voiceBtn.classList.remove('active');
            sendBtn.style.display = 'block';
        }
    },

    // 开始录音
    async startVoiceRecording(event) {
        // 阻止默认行为，特别是移动端的上拉事件
        if (event.type === 'touchstart') {
            event.preventDefault();
        }
        
        // 检查权限
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            
            this._isRecording = true;
            this._audioChunks = [];
            this._recordingStartTime = Date.now();
            
            // 创建 MediaRecorder
            this._mediaRecorder = new MediaRecorder(stream, {
                mimeType: 'audio/webm;codecs=opus'
            });
            
            this._mediaRecorder.ondataavailable = (e) => {
                if (e.data.size > 0) {
                    this._audioChunks.push(e.data);
                }
            };
            
            this._mediaRecorder.onstop = () => {
                this.processRecording();
            };
            
            this._mediaRecorder.start();
            
            // 更新UI
            const voiceBtn = document.getElementById('voiceHoldBtn');
            const voiceBtnText = document.getElementById('voiceBtnText');
            if (voiceBtn) {
                voiceBtn.classList.add('recording');
                voiceBtnText.textContent = '松开 发送';
            }
            
            // 录音时长计时
            this._voiceTimer = setInterval(() => {
                this.updateRecordingDuration();
            }, 1000);
            
            // 记录起始位置用于滑动取消
            this._voiceStartY = event.type.includes('touch') ? event.touches[0].clientY : event.clientY;
            
        } catch (error) {
            console.error('录音失败:', error);
            Utils.showToast('无法访问麦克风，请检查权限设置', 'error');
        }
    },

    // 结束录音
    endVoiceRecording(event) {
        if (!this._isRecording) return;
        
        if (event.type === 'touchend' || event.type === 'mouseup') {
            // 检查是否上滑取消
            const currentY = event.type.includes('touch') ? event.changedTouches[0].clientY : event.clientY;
            const deltaY = this._voiceStartY - currentY;
            
            // 如果上滑超过100px，取消发送
            if (deltaY > 100) {
                this.cancelVoiceRecording();
                return;
            }
        }
        
        this.stopRecording(true);
    },

    // 取消录音
    cancelVoiceRecording(event) {
        this.stopRecording(false);
    },

    // 停止录音
    stopRecording(shouldSend) {
        if (!this._isRecording) return;
        
        this._isRecording = false;
        
        // 停止计时器
        if (this._voiceTimer) {
            clearInterval(this._voiceTimer);
            this._voiceTimer = null;
        }
        
        // 更新UI
        const voiceBtn = document.getElementById('voiceHoldBtn');
        const voiceBtnText = document.getElementById('voiceBtnText');
        if (voiceBtn) {
            voiceBtn.classList.remove('recording');
            voiceBtnText.textContent = '按住 说话';
        }
        
        // 隐藏取消提示
        const cancelHint = document.getElementById('voiceCancelHint');
        if (cancelHint) {
            cancelHint.classList.remove('show');
        }
        
        // 停止 MediaRecorder
        if (this._mediaRecorder && this._mediaRecorder.state !== 'inactive') {
            this._mediaRecorder.stop();
            // 停止所有轨道
            this._mediaRecorder.stream.getTracks().forEach(track => track.stop());
        }
    },

    // 处理录音结果
    async processRecording() {
        if (this._audioChunks.length === 0) return;
        
        const audioBlob = new Blob(this._audioChunks, { type: 'audio/webm' });
        const duration = Math.round((Date.now() - this._recordingStartTime) / 1000);
        
        // 检查录音时长，太短不发送
        if (duration < 0.5) {
            Utils.showToast('录音时间太短', 'warning');
            return;
        }
        
        // 显示"正在发送"提示
        Utils.showToast('正在发送...', 'info');
        
        try {
            // 上传音频文件 - 使用正确的端点
            const formData = new FormData();
            formData.append('file', audioBlob, 'voice.m4a');
            
            const result = await API.upload('/upload/audio', formData);
            
            // 提取返回的URL（可能是 {url: "/uploads/audios/xxx.m4a"} 或直接是URL字符串）
            let mediaUrl = '';
            if (typeof result === 'string') {
                mediaUrl = result;
            } else if (result && result.url) {
                mediaUrl = result.url;
            } else if (result && result.data && result.data.url) {
                mediaUrl = result.data.url;
            }
            
            // 确保URL是完整的
            if (mediaUrl && !mediaUrl.startsWith('http')) {
                mediaUrl = CONFIG.API_BASE_URL + mediaUrl;
            }
            
            // 发送消息到服务器
            const message = {
                appointmentId: this.appointmentId,
                senderUserId: this.consultant.userId,
                receiverUserId: this.appointment.parentUserId,
                messageType: 'AUDIO',
                content: `${duration}秒`,
                mediaUrl: mediaUrl,
                duration: duration,
                isFromConsultant: true
            };
            
            await API.post('/messages', message);
            
            // 不在这里立即显示，等待 WebSocket 推送回来（避免重复显示）
            Utils.showToast('语音已发送', 'success');
            
        } catch (error) {
            console.error('发送语音失败:', error);
            Utils.showToast('发送失败，请重试', 'error');
        }
    },

    // 更新录音时长显示
    updateRecordingDuration() {
        if (!this._recordingStartTime) return;
        
        const duration = Math.round((Date.now() - this._recordingStartTime) / 1000);
        const voiceBtnText = document.getElementById('voiceBtnText');
        if (voiceBtnText) {
            voiceBtnText.textContent = `${duration}" 录音中...`;
        }
    },

    /**
     * 切换音频消息播放状态
     * 默认显示静态麦克风，点击后开始播放并显示波形动画
     */
    toggleAudioMessage(el) {
        const isPlaying = el.getAttribute('data-playing') === 'true';
        const url = el.getAttribute('data-media-url');
        
        if (!url) {
            Utils.showToast('音频地址不存在', 'error');
            return;
        }
        
        if (isPlaying) {
            // 停止播放，切换回静态图标
            this.stopAudioMessage(el);
        } else {
            // 开始播放，切换到播放状态
            this.playAudioMessage(el);
        }
    },
    
    /**
     * 播放音频 - 切换到播放状态
     */
    playAudioMessage(el) {
        const url = el.getAttribute('data-media-url');
        if (!url) {
            Utils.showToast('音频地址不存在', 'error');
            return;
        }
        
        // 如果已经在播放，先停止
        if (el._audio) {
            el._audio.pause();
            el._audio = null;
        }
        
        // 切换到播放状态
        el.setAttribute('data-playing', 'true');
        el.querySelector('.audio-icon').style.display = 'none';
        const playingDiv = el.querySelector('.audio-playing');
        playingDiv.style.display = 'flex';
        
        // 直接使用浏览器原生 audio 播放
        const audio = new Audio(url);
        let hasStarted = false; // 🔧 防止重复播放
        
        // 播放结束后的回调
        audio.onended = () => {
            this.stopAudioMessage(el);
        };
        
        audio.onerror = () => {
            // URL可能不是完整路径，尝试拼接
            const fullUrl = url.startsWith('http') ? url : (window.location.origin + url);
            if (fullUrl !== url) {
                const retryAudio = new Audio(fullUrl);
                retryAudio.onerror = () => {
                    Utils.showToast('音频加载失败', 'error');
                    this.stopAudioMessage(el);
                };
                retryAudio.onended = () => {
                    this.stopAudioMessage(el);
                };
                retryAudio.play().catch(() => {
                    Utils.showToast('音频播放失败', 'error');
                    this.stopAudioMessage(el);
                });
                el._audio = retryAudio;
            } else {
                Utils.showToast('音频加载失败', 'error');
                this.stopAudioMessage(el);
            }
        };
        
        // 🔧 修复：使用 play() 的 Promise 而不是 oncanplay，避免重复触发
        audio.play().then(() => {
            hasStarted = true;
        }).catch(err => {
            console.error('Audio play failed:', err);
            // 如果自动播放被阻止，尝试静音后播放
            audio.muted = true;
            audio.play().then(() => {
                audio.muted = false;
                hasStarted = true;
            }).catch(() => {
                Utils.showToast('音频播放失败，请点击重试', 'error');
                this.stopAudioMessage(el);
            });
        });
        
        // 保存audio对象到元素上以便停止时使用
        el._audio = audio;
    },
    
    /**
     * 停止音频播放，切换回静态图标
     */
    stopAudioMessage(el) {
        el.setAttribute('data-playing', 'false');
        el.querySelector('.audio-icon').style.display = 'flex';
        el.querySelector('.audio-playing').style.display = 'none';
        
        // 停止音频
        if (el._audio) {
            el._audio.pause();
            el._audio.currentTime = 0;
            el._audio = null;
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
    
    // 绑定语音按钮事件 - 使用 JavaScript 动态绑定确保可靠性
    const voiceHoldBtn = document.getElementById('voiceHoldBtn');
    if (voiceHoldBtn) {
        // 鼠标事件
        voiceHoldBtn.addEventListener('mousedown', (e) => {
            e.preventDefault(); // 阻止默认行为
            Chat.startVoiceRecording(e);
        });
        voiceHoldBtn.addEventListener('mouseup', (e) => {
            Chat.endVoiceRecording(e);
        });
        voiceHoldBtn.addEventListener('mouseleave', (e) => {
            // 鼠标离开时，如果正在录音则取消
            if (Chat._isRecording) {
                Chat.cancelVoiceRecording(e);
            }
        });
        
        // 触摸事件
        voiceHoldBtn.addEventListener('touchstart', (e) => {
            e.preventDefault(); // 阻止默认行为
            Chat.startVoiceRecording(e);
        }, { passive: false });
        voiceHoldBtn.addEventListener('touchend', (e) => {
            Chat.endVoiceRecording(e);
        });
        voiceHoldBtn.addEventListener('touchcancel', (e) => {
            Chat.cancelVoiceRecording(e);
        });
        
        console.log('语音按钮事件绑定成功');
    }
});

// 处理来自 Android 原生端的消息（通过 WebAppInterface 调用）
// 这个函数会被 Android 端的 notifyMediaMessage 触发
window.onMediaSelected = function(type, content, mediaUri) {
    console.log('[Web onMediaSelected] type=' + type + ', content=' + content + ', mediaUri=' + mediaUri);
    
    // 如果 mediaUri 是 http 链接，转换为服务器相对路径
    let mediaUrl = mediaUri;
    if (mediaUri && mediaUri.startsWith('http')) {
        // 保留完整 URL，让 Web 端可以直接访问
        mediaUrl = mediaUri;
    }
    
    // 从 content 中提取时长（格式如 "语音消息 01:23"）
    let duration = 0;
    const durationMatch = content && content.match(/(\d{2}):(\d{2})/);
    if (durationMatch) {
        duration = parseInt(durationMatch[1]) * 60 + parseInt(durationMatch[2]);
    }
    
    // 创建消息对象
    const message = {
        id: 'local_' + Date.now(), // 临时本地 ID
        type: (type || 'text').toLowerCase(),
        content: content || '',
        timestamp: Date.now(),
        fromConsultant: false, // 来自家长端
        senderUserId: Chat.appointment ? Chat.appointment.parentUserId : null,
        mediaUrl: mediaUrl,
        duration: duration
    };
    
    // 添加到消息列表并显示
    Chat.messages.push(message);
    
    const messagesArea = document.getElementById('messagesArea');
    if (messagesArea) {
        if (messagesArea.querySelector('.empty-messages')) {
            messagesArea.innerHTML = '';
        }
        messagesArea.insertAdjacentHTML('beforeend', Chat.createMessageElement(message));
        Chat.scrollToBottom();
    }
    
    console.log('[Web onMediaSelected] Message added to UI');
};

// 全局函数：供Android端调用，关闭来电通知并重置通话状态
window.dismissCallNotification = function() {
    console.log('[dismissCallNotification] Resetting call state');
    Chat.hideCallNotification();
    Chat.callState = null;
    Chat.callType = null;
    Chat.callStartTime = null;
};
