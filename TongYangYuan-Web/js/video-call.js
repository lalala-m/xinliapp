// 童养园咨询师端 - 视频通话 (支持来电等待 + 正确信令流程)
// 角色: caller(主动发起) / callee(被动等待)
// 信令: WebSocket STOMP /queue/webrtc
// 媒体: LiveKit

const VideoCall = {
    appointmentId: null,
    appointment: null,
    consultant: null,
    room: null,
    startTime: null,
    durationInterval: null,
    callType: 'video',
    isAudioCall: false,
    currentUserId: null,

    // 角色
    role: null,        // 'caller' | 'callee'
    stompClient: null,
    // 主叫信息（从 CALL 信令中提取，供 callee 接听/拒绝时使用）
    callerInfo: null,   // { fromUserId, sessionId }
    connectedFired: false,

    // 初始化
    async init() {
        this.consultant = Auth.checkAuth();
        if (!this.consultant) return;
        this.currentUserId = this.consultant.userId || this.consultant.id;

        const urlParams = new URLSearchParams(window.location.search);
        this.appointmentId = urlParams.get('appointmentId');
        this.role = urlParams.get('role') || 'callee';
        this.callType = (urlParams.get('type') || 'video').toLowerCase();
        this.isAudioCall = this.callType === 'audio';

        if (!this.appointmentId) {
            Utils.showToast('预约ID不存在', 'error');
            return this.goBackToChat();
        }

        // 加载预约信息
        await this.loadAppointmentInfo();

        // 建立 WebSocket 连接（用于收信令）- callee 必须先连接才能收到信令
        this.connectWebSocket();

        // 根据角色启动流程
        if (this.role === 'caller') {
            // 主叫：发送 call 信号，等待对方 accept
            this.startOutgoingCall();
        } else {
            // 被叫：显示来电等待界面，等待信令
            this.startIncomingWait();
        }
    },

    liveKitRoomName() {
        return `apt_${this.appointmentId}`;
    },

    // ---- WebSocket 信令 ----

    connectWebSocket() {
        const token = API.getToken();
        const socket = new SockJS(CONFIG.WS_BASE_URL, null, {
            transports: ['websocket']
        });
        this.stompClient = Stomp.over(socket);
        this.stompClient.debug = null;

        this.stompClient.connect(
            { 'Authorization': 'Bearer ' + token },
            () => {
                console.log('VideoCall WebSocket Connected');

                // 订阅 /queue/webrtc 收信令
                this.stompClient.subscribe('/user/queue/webrtc', (messageOutput) => {
                    const signal = JSON.parse(messageOutput.body);
                    this.handleWebRTCSignal(signal);
                });

                // 如果是被叫，检查 sessionStorage 是否有积压的来电信息
                if (this.role === 'callee') {
                    this.checkPendingIncomingCall();
                }
            },
            (error) => {
                console.error('VideoCall WebSocket error:', error);
                setTimeout(() => this.connectWebSocket(), 5000);
            }
        );
    },

    /**
     * 被叫页面加载时检查是否有积压的来电（上一条 CALL 信号已通过 chat.js 收到了但 tab 未打开）
     * chat.js 收到 CALL 时会把 callerInfo 存入 sessionStorage
     */
    checkPendingIncomingCall() {
        try {
            const pending = sessionStorage.getItem('pendingIncomingCall');
            if (pending) {
                const info = JSON.parse(pending);
                if (info.appointmentId == this.appointmentId) {
                    sessionStorage.removeItem('pendingIncomingCall');
                    this.callerInfo = { fromUserId: info.fromUserId, sessionId: info.sessionId };
                    this.showIncomingUI();
                }
            }
        } catch (e) {
            console.error('checkPendingIncomingCall error', e);
        }
    },

    handleWebRTCSignal(signal) {
        if (!signal || !signal.type) return;
        const type = signal.type;

        if (type === 'call') {
            // 收到来电
            if (signal.appointmentId && String(signal.appointmentId) !== String(this.appointmentId)) {
                return;
            }
            // 保存 caller 信息（用于接听/拒绝）
            this.callerInfo = {
                fromUserId: signal.fromUserId,
                sessionId: signal.data?.sessionId || signal.sessionId || '',
                callType: signal.data?.callType || 'video'
            };
            this.callType = (this.callerInfo.callType || 'video').toLowerCase();
            this.isAudioCall = this.callType === 'audio';

            // 立即显示来电 UI（callee 刚打开 tab 时）
            this.showIncomingUI();
        } else if (type === 'accept') {
            // 对方接听 → 开始 LiveKit
            if (this.role === 'caller' && !this.room) {
                console.log('Got accept signal, connecting to LiveKit...');
                this.connectToLiveKit();
            }
        } else if (type === 'reject') {
            Utils.showToast('家长拒绝了通话', 'warn');
            this.goBackToChat();
        } else if (type === 'end') {
            Utils.showToast('通话已结束', 'info');
            if (this.room) this.room.disconnect();
            this.goBackToChat();
        }
    },

    sendWebRTCSignal(type, data = null) {
        if (!this.stompClient) return;
        const payload = {
            type: type,
            fromUserId: this.currentUserId,
            toUserId: this.callerInfo ? this.callerInfo.fromUserId : (this.appointment ? this.appointment.parentUserId : null),
            appointmentId: parseInt(this.appointmentId),
            data: data
        };
        this.stompClient.send('/app/webrtc.signal', {}, JSON.stringify(payload));
        console.log('Sent signal:', type, payload);
    },

    // ---- 主叫流程 ----

    startOutgoingCall() {
        this._setStatus('正在发送通话请求…');
        this._showIncomingOverlay(false);
        this._showWaitingPeerOverlay(true);
        document.getElementById('waitingPeerName').textContent =
            (this.appointment && this.appointment.childName)
                ? this.appointment.childName
                : '家长';
        document.getElementById('waitingPeerState').textContent = '等待家长接听…';

        // 发送 call 信令
        this.sendWebRTCSignal('call', {
            callType: this.callType,
            sessionId: Date.now() + '_' + this.currentUserId
        });
    },

    // ---- 被叫流程 ----

    startIncomingWait() {
        // 显示等待界面（尚未收到来电时）
        this._showIncomingOverlay(false);
        document.getElementById('incomingSub').textContent = '等待来电…';
    },

    showIncomingUI() {
        this._showIncomingOverlay(true);
        const name = (this.appointment && this.appointment.childName)
            ? this.appointment.childName
            : '家长';
        document.getElementById('incomingName').textContent = name;
        document.getElementById('incomingSub').textContent =
            this.isAudioCall ? '语音来电…' : '视频来电…';
    },

    acceptIncomingCall() {
        this._showIncomingOverlay(false);
        this._setStatus('正在接听…');

        // 发送 accept 信令
        this.sendWebRTCSignal('accept', {
            callType: this.callType,
            sessionId: this.callerInfo ? this.callerInfo.sessionId : ''
        });

        // 连接 LiveKit
        this.connectToLiveKit();
    },

    rejectIncomingCall() {
        this._showIncomingOverlay(false);
        Utils.showToast('已拒绝通话', 'info');

        this.sendWebRTCSignal('reject', {
            callType: this.callType,
            sessionId: this.callerInfo ? this.callerInfo.sessionId : ''
        });

        this.goBackToChat();
    },

    // ---- LiveKit 核心连接 ----

    async connectToLiveKit() {
        try {
            this._setStatus('正在获取通话凭证…');
            this._showLoading(true);

            const room = encodeURIComponent(this.liveKitRoomName());
            const identity = encodeURIComponent(String(this.currentUserId));
            const response = await fetch(`${CONFIG.API_BASE_URL}/livekit/token?room=${room}&identity=${identity}`);
            const body = await response.json().catch(() => ({}));

            if (!response.ok) {
                const errMsg = body.error || body.message || `HTTP ${response.status}`;
                throw new Error(errMsg);
            }

            const token = body.token;
            const serverUrl = body.serverUrl;
            if (!token || !serverUrl) {
                throw new Error('服务器未返回有效的 LiveKit 凭证');
            }

            this._showWaitingPeerOverlay(false);
            this._setStatus('正在建立连接…');
            this.room = new LivekitClient.Room();

            // 远端视频轨道订阅
            this.room.on(LivekitClient.RoomEvent.TrackSubscribed, (track, publication, participant) => {
                console.log('TrackSubscribed:', track.kind, participant.identity);
                if (track.kind === 'video') {
                    const remote = document.getElementById('remoteVideo');
                    const waiting = document.getElementById('vcWaiting');
                    if (remote) {
                        track.attach(remote);
                        if (waiting) waiting.classList.add('gone');
                    }
                }
                if (track.kind === 'audio') {
                    track.attach(document.createElement('audio'));
                }
                // 通话正式开始
                if (!this.connectedFired) {
                    this.connectedFired = true;
                    this.startTimer();
                    this._enterInCallState();
                }
            });

            this.room.on(LivekitClient.RoomEvent.TrackUnsubscribed, (track) => {
                track.detach();
                const waiting = document.getElementById('vcWaiting');
                if (waiting) waiting.classList.remove('gone');
            });

            // 对方加入房间（处理晚加入：对方已在 room 中）
            this.room.on(LivekitClient.RoomEvent.ParticipantConnected, (participant) => {
                console.log('ParticipantConnected:', participant.identity);
                participant.tracks.forEach((pub) => {
                    if (pub.track) {
                        if (pub.track.kind === 'video') {
                            const remote = document.getElementById('remoteVideo');
                            if (remote) {
                                pub.track.attach(remote);
                                const waiting = document.getElementById('vcWaiting');
                                if (waiting) waiting.classList.add('gone');
                            }
                        }
                        if (pub.track.kind === 'audio') {
                            pub.track.attach(document.createElement('audio'));
                        }
                    }
                });
            });

            // 连接状态变化
            this.room.on(LivekitClient.RoomEvent.ConnectionStateChanged, (state) => {
                const net = document.getElementById('vcNetIndicator');
                if (!net) return;
                net.className = 'vc-net';
                if (state === 'connected') {
                    net.classList.add('connected');
                    net.querySelector('span').textContent = '已连接';
                } else if (state === 'disconnected' || state === 'failed') {
                    net.classList.add('error');
                    net.querySelector('span').textContent = '连接断开';
                } else {
                    net.querySelector('span').textContent = '连接中';
                }
            });

            this.room.on(LivekitClient.RoomEvent.Disconnected, () => {
                Utils.showToast('通话已结束', 'info');
                this.goBackToChat();
            });

            // 连接 LiveKit
            await this.room.connect(serverUrl, token);

            this._showLoading(false);
            this._setNetConnected(true);

            // 发布本地媒体
            await this.room.localParticipant.setMicrophoneEnabled(true);
            if (!this.isAudioCall) {
                await this.room.localParticipant.setCameraEnabled(true);
                const localVideo = document.getElementById('localVideo');
                const localPh = document.getElementById('localVideoPlaceholder');
                const pub = this.room.localParticipant.getTrackPublication(LivekitClient.Track.Source.Camera);
                if (pub && pub.videoTrack && localVideo) {
                    pub.videoTrack.attach(localVideo);
                    localVideo.classList.add('active');
                    if (localPh) localPh.classList.add('hidden');
                }
            }

            // 通话正式开始
            if (!this.connectedFired) {
                this.connectedFired = true;
                this.startTimer();
                this._enterInCallState();
            }

            // 晚加入：对方已在 room 中，立即订阅
            this.room.remoteParticipants.forEach((participant) => {
                console.log('Remote participant already in room:', participant.identity);
                participant.tracks.forEach((pub) => {
                    if (pub.track) {
                        if (pub.track.kind === 'video') {
                            const remote = document.getElementById('remoteVideo');
                            if (remote) {
                                pub.track.attach(remote);
                                const waiting = document.getElementById('vcWaiting');
                                if (waiting) waiting.classList.add('gone');
                            }
                        }
                        if (pub.track.kind === 'audio') {
                            pub.track.attach(document.createElement('audio'));
                        }
                    }
                });
            });

        } catch (error) {
            console.error('LiveKit connection failed:', error);
            this._showLoading(false);
            this._setNetConnected(false);
            Utils.showToast('连接失败: ' + error.message, 'error');
        }
    },

    // ---- UI 辅助 ----

    _showLoading(show) {
        const el = document.getElementById('connectionStatus');
        if (el) el.classList.toggle('show', show);
    },

    _setStatus(text) {
        const el = document.getElementById('connStatusText');
        if (el) el.textContent = text;
    },

    _showIncomingOverlay(show) {
        const el = document.getElementById('incomingCallOverlay');
        if (el) el.style.display = show ? 'flex' : 'none';
    },

    _showWaitingPeerOverlay(show) {
        const el = document.getElementById('waitingPeerOverlay');
        if (el) el.style.display = show ? 'flex' : 'none';
    },

    _setNetConnected(connected) {
        const net = document.getElementById('vcNetIndicator');
        if (!net) return;
        net.className = 'vc-net' + (connected ? ' connected' : ' error');
        const span = net.querySelector('span');
        if (span) span.textContent = connected ? '已连接' : '连接失败';
    },

    _enterInCallState() {
        const waiting = document.getElementById('vcWaiting');
        const stateEl = document.getElementById('callStateText');
        const timerEl = document.getElementById('callDuration');
        if (waiting) waiting.style.display = 'none';
        if (stateEl) {
            stateEl.textContent = '通话中';
            stateEl.className = 'vc-state active';
        }
        if (timerEl) timerEl.style.display = 'block';
    },

    // ---- 控制 ----

    async toggleMute() {
        if (!this.room) return;
        const enabled = !this.room.localParticipant.isMicrophoneEnabled;
        await this.room.localParticipant.setMicrophoneEnabled(enabled);
        const btn = document.getElementById('btnMute');
        const icOn = btn?.querySelector('.ic-mic-on');
        const icOff = btn?.querySelector('.ic-mic-off');
        const lbl = document.getElementById('lblMute');
        btn?.classList.toggle('muted', !enabled);
        if (icOn) icOn.style.display = enabled ? 'block' : 'none';
        if (icOff) icOff.style.display = enabled ? 'none' : 'block';
        if (lbl) lbl.textContent = enabled ? '静音' : '已静音';
    },

    async toggleVideo() {
        if (!this.room || this.isAudioCall) return;
        const enabled = !this.room.localParticipant.isCameraEnabled;
        await this.room.localParticipant.setCameraEnabled(enabled);

        const btn = document.getElementById('btnVideo');
        const icOn = btn?.querySelector('.ic-cam-on');
        const icOff = btn?.querySelector('.ic-cam-off');
        const lbl = document.getElementById('lblVideo');
        const localVideo = document.getElementById('localVideo');
        const localPh = document.getElementById('localVideoPlaceholder');
        btn?.classList.toggle('video-off', !enabled);
        if (icOn) icOn.style.display = enabled ? 'block' : 'none';
        if (icOff) icOff.style.display = enabled ? 'none' : 'block';
        if (lbl) lbl.textContent = enabled ? '关摄像头' : '开摄像头';
        if (localVideo) localVideo.classList.toggle('active', enabled);
        if (localPh) localPh.classList.toggle('hidden', enabled);
    },

    async endCall() {
        if (this.room) {
            this.room.disconnect();
        }
        this.sendWebRTCSignal('end', { callType: this.callType });
        await this.sendCallEndedMessage();
        this.goBackToChat();
    },

    async sendCallEndedMessage() {
        if (!this.appointmentId || !this.currentUserId) return;
        try {
            const durationSec = this.startTime
                ? Math.floor((Date.now() - this.startTime) / 1000)
                : 0;
            const m = String(Math.floor(durationSec / 60)).padStart(2, '0');
            const ss = String(durationSec % 60).padStart(2, '0');
            const durationStr = `${m}:${ss}`;
            const peerUserId = this.appointment ? this.appointment.parentUserId : null;
            if (!peerUserId) return;
            const content = `CALL_ENDED:${this.callType}:${durationSec}:${durationStr}`;
            await API.post('/messages', {
                appointmentId: parseInt(this.appointmentId),
                senderUserId: this.currentUserId,
                receiverUserId: peerUserId,
                messageType: 'SYSTEM',
                content: content
            });
        } catch (e) {
            console.error('Failed to send call ended message:', e);
        }
    },

    goBackToChat() {
        if (this.durationInterval) clearInterval(this.durationInterval);
        if (this.stompClient) {
            this.stompClient.disconnect();
            this.stompClient = null;
        }
        const url = this.appointmentId
            ? `chat.html?appointmentId=${this.appointmentId}`
            : 'dashboard.html';
        window.location.href = url;
    },

    async loadAppointmentInfo() {
        try {
            const appointment = await API.get(`/appointments/${this.appointmentId}`);
            if (appointment) {
                this.appointment = {
                    ...appointment,
                    clientName: `家长#${appointment.parentUserId}`,
                    childName: appointment.childName,
                    childAge: appointment.childAge
                };
                this.displayClientInfo();
            }
        } catch (error) {
            console.error('加载预约失败', error);
        }
    },

    displayClientInfo() {
        const nameEl = document.getElementById('callClientName');
        const waitingNameEl = document.getElementById('waitingPeerName');
        const incomingNameEl = document.getElementById('incomingName');
        const name = (this.appointment && this.appointment.childName)
            ? this.appointment.childName
            : '家长';
        if (nameEl) nameEl.textContent = name;
        if (waitingNameEl) waitingNameEl.textContent = name;
        if (incomingNameEl) incomingNameEl.textContent = name;
    },

    configureCallMode() {
        const badge = document.getElementById('callTypeBadge');
        const videoCtrl = document.getElementById('videoCtrlBtn');
        const localWrap = document.getElementById('vcLocalWrap');

        if (badge) {
            badge.innerHTML = this.isAudioCall
                ? `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 9a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 0h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 7.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg> 语音通话`
                : `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2"/></svg> 视频通话`;
        }
        if (this.isAudioCall) {
            document.title = '语音通话 - 童养园';
            if (videoCtrl) videoCtrl.style.display = 'none';
            if (localWrap) localWrap.style.display = 'none';
        }
    },

    startTimer() {
        this.startTime = Date.now();
        const durationEl = document.getElementById('callDuration');
        const stateEl = document.getElementById('callStateText');
        this.durationInterval = setInterval(() => {
            const s = Math.floor((Date.now() - this.startTime) / 1000);
            const m = String(Math.floor(s / 60)).padStart(2, '0');
            const ss = String(s % 60).padStart(2, '0');
            if (durationEl) durationEl.textContent = `${m}:${ss}`;
            if (stateEl) {
                stateEl.textContent = '通话中';
                stateEl.className = 'vc-state active';
            }
        }, 1000);
    }
};

// 页面加载时初始化
window.addEventListener('DOMContentLoaded', () => {
    VideoCall.configureCallMode();
    VideoCall.init();
});
