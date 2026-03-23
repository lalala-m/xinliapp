// 童养园咨询师端 - 视频通话逻辑 (LiveKit 重构版)

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
        this.currentUserId = this.consultant.id;

        const urlParams = new URLSearchParams(window.location.search);
        this.appointmentId = urlParams.get('appointmentId');
        this.callType = (urlParams.get('type') || 'video').toLowerCase();
        this.isAudioCall = this.callType === 'audio';

        if (!this.appointmentId) {
            Utils.showToast('预约ID不存在', 'error');
            return this.goBackToChat();
        }

        await this.loadAppointmentInfo();
        this.displayClientInfo();
        this.configureCallMode();
        
        await this.connectToLiveKit();
    },

    // 加载预约信息
    async loadAppointmentInfo() {
        try {
            const apt = await API.get(`/appointments/${this.appointmentId}`);
            if (!apt) throw new Error('Appointment not found');
            this.appointment = apt;
            return true;
        } catch (error) {
            Utils.showToast('加载预约信息失败', 'error');
            this.goBackToChat();
            return false;
        }
    },

    // 连接到 LiveKit
    async connectToLiveKit() {
        try {
            this.showStatus('正在获取通话凭证...');
            // 获取 Token
            const response = await fetch(`${Config.API_BASE_URL}/livekit/token?room=${this.appointmentId}&identity=${this.currentUserId}`);
            const { token, serverUrl } = await response.json();

            this.showStatus('正在建立连接...');
            this.room = new LivekitClient.Room();

            // 设置事件处理
            this.room.on(LivekitClient.RoomEvent.TrackSubscribed, (track, publication, participant) => {
                if (track.kind === 'video') {
                    const remoteVideo = document.getElementById('remoteVideo');
                    const placeholder = document.getElementById('remoteVideoPlaceholder');
                    track.attach(remoteVideo);
                    remoteVideo.style.display = 'block';
                    if (placeholder) placeholder.style.display = 'none';
                }
            });

            this.room.on(LivekitClient.RoomEvent.TrackUnsubscribed, (track) => {
                track.detach();
            });

            this.room.on(LivekitClient.RoomEvent.Disconnected, () => {
                Utils.showToast('通话已结束', 'info');
                this.goBackToChat();
            });

            // 连接并发布本地轨道
            await this.room.connect(serverUrl, token);
            this.hideStatus();
            this.startTimer();

            // 发布音频
            await this.room.localParticipant.setMicrophoneEnabled(true);
            
            // 发布视频
            if (!this.isAudioCall) {
                await this.room.localParticipant.setCameraEnabled(true);
                const localVideo = document.getElementById('localVideo');
                const placeholder = document.getElementById('localVideoPlaceholder');
                
                // 查找本地视频轨道并显示
                const videoPub = this.room.localParticipant.getTrackPublication(LivekitClient.Track.Source.Camera);
                if (videoPub && videoPub.videoTrack) {
                    videoPub.videoTrack.attach(localVideo);
                    localVideo.style.display = 'block';
                    if (placeholder) placeholder.style.display = 'none';
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
        const textEl = statusEl.querySelector('.connection-text');
        if (textEl) textEl.textContent = text;
        statusEl.style.display = 'flex';
    },

    // 隐藏状态
    hideStatus() {
        const statusEl = document.getElementById('connectionStatus');
        statusEl.style.display = 'none';
    },

    // 切换静音
    async toggleMute() {
        if (!this.room) return;
        const enabled = !this.room.localParticipant.isMicrophoneEnabled;
        await this.room.localParticipant.setMicrophoneEnabled(enabled);
        
        const muteBtn = document.getElementById('muteBtn');
        if (muteBtn) {
            muteBtn.classList.toggle('active', !enabled);
            muteBtn.textContent = enabled ? '🎤' : '🔇';
        }
    },

    // 挂断
    async endCall() {
        if (this.room) {
            await this.room.disconnect();
        }
        this.goBackToChat();
    },

    // 切换摄像头
    async switchCamera() {
        if (!this.room || this.isAudioCall) return;
        // LiveKit 自动处理设备选择，可以通过重新开启摄像头触发或使用具体设备ID
        const isEnabled = this.room.localParticipant.isCameraEnabled;
        await this.room.localParticipant.setCameraEnabled(!isEnabled);
        await this.room.localParticipant.setCameraEnabled(isEnabled);
    },

    // 计时器
    startTimer() {
        this.startTime = Date.now();
        const durationEl = document.getElementById('callDuration');
        this.durationInterval = setInterval(() => {
            const seconds = Math.floor((Date.now() - this.startTime) / 1000);
            const m = Math.floor(seconds / 60).toString().padStart(2, '0');
            const s = (seconds % 60).toString().padStart(2, '0');
            if (durationEl) durationEl.textContent = `${m}:${s}`;
        }, 1000);
    },

    // 返回聊天
    goBackToChat() {
        if (this.durationInterval) clearInterval(this.durationInterval);
        const url = this.appointmentId ? `chat.html?appointmentId=${this.appointmentId}` : 'index.html';
        window.location.href = url;
    },

    // 显示客户信息
    displayClientInfo() {
        const nameEl = document.getElementById('callClientName');
        if (nameEl && this.appointment) {
            nameEl.textContent = `家长#${this.appointment.parentUserId}（${this.appointment.childName}）`;
        }
    },

    configureCallMode() {
        if (this.isAudioCall) {
            document.title = '语音通话 - 童养园咨询师端';
            const switchBtn = document.querySelector('.switch-camera');
            if (switchBtn) switchBtn.style.display = 'none';
            const remoteIcon = document.querySelector('#remoteVideoPlaceholder .remote-video-icon');
            if (remoteIcon) remoteIcon.textContent = '🎧';
            const remoteText = document.querySelector('#remoteVideoPlaceholder .remote-video-text');
            if (remoteText) remoteText.textContent = '等待语音接入...';
            const localPlaceholder = document.getElementById('localVideoPlaceholder');
            if (localPlaceholder) localPlaceholder.textContent = '🎧';
        }
    }
};

// 页面加载完成后初始化
window.onload = () => {
    VideoCall.init();
};
