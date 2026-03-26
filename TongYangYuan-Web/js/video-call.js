// 童养园咨询师端 - 视频通话逻辑 (LiveKit 重构版 + 现代 UI)

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

    // 初始化
    async init() {
        this.consultant = Auth.checkAuth();
        if (!this.consultant) return;
        this.currentUserId = this.consultant.userId || this.consultant.id;

        const urlParams = new URLSearchParams(window.location.search);
        this.appointmentId = urlParams.get('appointmentId');
        this.callType = (urlParams.get('type') || 'video').toLowerCase();
        this.isAudioCall = this.callType === 'audio';

        if (!this.appointmentId) {
            Utils.showToast('预约ID不存在', 'error');
            return this.goBackToChat();
        }

        this.configureCallMode();
        this.displayClientInfo();
        await this.connectToLiveKit();
    },

    liveKitRoomName() {
        return `apt_${this.appointmentId}`;
    },

    // 连接到 LiveKit（房间名须与 Android 端 VideoCallActivity 一致：apt_{appointmentId}）
    async connectToLiveKit() {
        try {
            this.showStatus('正在获取通话凭证...');
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
                throw new Error('服务器未返回有效的 LiveKit 凭证，请检查 application.properties 中的 livekit 配置');
            }

            this.showStatus('正在建立连接...');
            this.room = new LivekitClient.Room();

            // 远端视频轨道订阅
            this.room.on(LivekitClient.RoomEvent.TrackSubscribed, (track, publication, participant) => {
                if (track.kind === 'video') {
                    const remoteArea = document.getElementById('remoteVideoArea');
                    const remoteVideo = document.getElementById('remoteVideo');
                    const avatarCenter = document.getElementById('avatarCenter');
                    if (remoteVideo) {
                        track.attach(remoteVideo);
                        remoteArea.classList.add('show');
                        if (avatarCenter) avatarCenter.style.display = 'none';
                    }
                }
            });

            this.room.on(LivekitClient.RoomEvent.TrackUnsubscribed, (track) => {
                track.detach();
                const remoteArea = document.getElementById('remoteVideoArea');
                const avatarCenter = document.getElementById('avatarCenter');
                if (remoteArea) remoteArea.classList.remove('show');
                if (avatarCenter) avatarCenter.style.display = 'flex';
            });

            this.room.on(LivekitClient.RoomEvent.Disconnected, () => {
                Utils.showToast('通话已结束', 'info');
                this.goBackToChat();
            });

            // 连接并发布本地轨道
            await this.room.connect(serverUrl, token);
            this.hideStatus();
            this.startTimer();
            this.setInCallState();

            // 发布音频
            await this.room.localParticipant.setMicrophoneEnabled(true);

            // 发布视频
            if (!this.isAudioCall) {
                await this.room.localParticipant.setCameraEnabled(true);
                const localVideo = document.getElementById('localVideo');
                const localPlaceholder = document.getElementById('localVideoPlaceholder');
                const videoPub = this.room.localParticipant.getTrackPublication(LivekitClient.Track.Source.Camera);
                if (videoPub && videoPub.videoTrack && localVideo) {
                    videoPub.videoTrack.attach(localVideo);
                    localVideo.style.display = 'block';
                    if (localPlaceholder) localPlaceholder.style.display = 'none';
                }
            }

        } catch (error) {
            console.error('LiveKit connection failed:', error);
            Utils.showToast('连接失败: ' + error.message, 'error');
            this.goBackToChat();
        }
    },

    // 显示状态
    showStatus(text) {
        const statusEl = document.getElementById('connectionStatus');
        if (!statusEl) return;
        const textEl = statusEl.querySelector('.conn-text');
        if (textEl) textEl.textContent = text;
        statusEl.classList.add('show');
    },

    // 隐藏状态
    hideStatus() {
        const statusEl = document.getElementById('connectionStatus');
        if (statusEl) statusEl.classList.remove('show');
    },

    // 进入通话状态：停止光环动画、显示通话时长
    setInCallState() {
        const avatarCenter = document.getElementById('avatarCenter');
        const avatarZone = document.getElementById('avatarZone');
        const ring1 = document.getElementById('ring1');
        const ring2 = document.getElementById('ring2');
        const stateText = document.getElementById('callStateText');
        const timer = document.getElementById('callDuration');
        if (avatarZone) avatarZone.style.display = 'none';
        if (ring1) ring1.style.display = 'none';
        if (ring2) ring2.style.display = 'none';
        if (stateText) {
            stateText.textContent = '通话中';
            stateText.className = 'call-state-text active';
        }
        if (timer) timer.style.display = 'block';
    },

    // 切换静音
    async toggleMute() {
        if (!this.room) return;
        const enabled = !this.room.localParticipant.isMicrophoneEnabled;
        await this.room.localParticipant.setMicrophoneEnabled(enabled);

        const muteBtn = document.getElementById('btnMute');
        const iconOn = document.getElementById('iconMicOn');
        const iconOff = document.getElementById('iconMicOff');
        const lbl = document.getElementById('lblMute');
        if (muteBtn) muteBtn.classList.toggle('on', !enabled);
        if (iconOn) iconOn.style.display = enabled ? 'block' : 'none';
        if (iconOff) iconOff.style.display = enabled ? 'none' : 'block';
        if (lbl) lbl.textContent = enabled ? '静音' : '已静音';
    },

    // 切换摄像头
    async toggleVideo() {
        if (!this.room || this.isAudioCall) return;
        const enabled = !this.room.localParticipant.isCameraEnabled;
        await this.room.localParticipant.setCameraEnabled(enabled);

        const videoBtn = document.getElementById('btnVideo');
        const iconOn = document.getElementById('iconVideoOn');
        const iconOff = document.getElementById('iconVideoOff');
        const lbl = document.getElementById('lblVideo');
        const localVideo = document.getElementById('localVideo');
        const localPlaceholder = document.getElementById('localVideoPlaceholder');
        if (videoBtn) videoBtn.classList.toggle('on', !enabled);
        if (iconOn) iconOn.style.display = enabled ? 'block' : 'none';
        if (iconOff) iconOff.style.display = enabled ? 'none' : 'block';
        if (lbl) lbl.textContent = enabled ? '关摄像头' : '开摄像头';
        if (localVideo) localVideo.style.display = enabled ? 'block' : 'none';
        if (localPlaceholder) localPlaceholder.style.display = enabled ? 'none' : 'flex';
    },

    // 挂断
    async endCall() {
        if (this.room) {
            await this.room.disconnect();
        }
        this.goBackToChat();
    },

    // 返回聊天
    goBackToChat() {
        if (this.durationInterval) clearInterval(this.durationInterval);
        const url = this.appointmentId ? `chat.html?appointmentId=${this.appointmentId}` : 'dashboard.html';
        window.location.href = url;
    },

    // 返回按钮（供 HTML onclick 调用）
    goBack() {
        this.endCall();
    },

    // 显示客户信息
    displayClientInfo() {
        const nameEl = document.getElementById('callClientName');
        const avatarEl = document.getElementById('callAvatar');
        if (this.appointment) {
            const childName = this.appointment.childName || '家长';
            if (nameEl) nameEl.textContent = childName;
            if (avatarEl) avatarEl.textContent = childName.charAt(0).toUpperCase();
        }
    },

    // 配置通话模式（音频/视频）
    configureCallMode() {
        const badge = document.getElementById('callTypeBadge');
        const videoCtrl = document.getElementById('videoCtrlBtn');
        const localWrap = document.querySelector('[style*="position:absolute;top:56px"]');
        const localVideo = document.getElementById('localVideo');
        const localPlaceholder = document.getElementById('localVideoPlaceholder');

        if (badge) badge.textContent = this.isAudioCall ? '语音通话' : '视频通话';
        if (this.isAudioCall) {
            document.title = '语音通话 - 童养园咨询师端';
            if (videoCtrl) videoCtrl.style.display = 'none';
            if (localWrap) localWrap.style.display = 'none';
            const stateText = document.getElementById('callStateText');
            if (stateText) stateText.textContent = '等待对方接听...';
        }
    },

    // 计时器
    startTimer() {
        this.startTime = Date.now();
        const durationEl = document.getElementById('callDuration');
        const stateText = document.getElementById('callStateText');
        this.durationInterval = setInterval(() => {
            const seconds = Math.floor((Date.now() - this.startTime) / 1000);
            const m = String(Math.floor(seconds / 60)).padStart(2, '0');
            const s = String(seconds % 60).padStart(2, '0');
            if (durationEl) durationEl.textContent = `${m}:${s}`;
            if (stateText) {
                stateText.textContent = '通话中';
                stateText.className = 'call-state-text active';
            }
        }, 1000);
    }
};

// 页面加载完成后初始化
window.onload = () => {
    VideoCall.init();
};
