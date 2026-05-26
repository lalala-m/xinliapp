/**
 * WebRTC 视频通话 (Web端)
 * 基于 task 项目的原生 WebRTC 实现
 * 使用简化信令协议（原生 WebSocket，非 STOMP）
 */

const WebRTCVideoCall = {
    appointmentId: null,
    consultant: null,
    currentUserId: null,
    role: null,        // 'caller' | 'callee'
    callType: 'video',
    isAudioCall: false,

    // WebRTC
    localStream: null,
    remoteStream: null,
    peerConnection: null,

    // 信令
    ws: null,
    isInCall: false,
    candidateQueue: [],
    remoteDescriptionSet: false,
    pendingOffer: null,  // 缓存收到的offer（被叫在接听前收到）
    hasAcceptedCall: false,  // 被叫是否已点击接听

    // 视频录制
    mediaRecorder: null,
    recordedChunks: [],
    isRecording: false,
    recordingStartTime: 0,
    recordedBlobs: [],
    
    // 🔧 新增：录制画布相关
    recordingCanvas: null,
    recordingCanvasCtx: null,
    canvasDrawInterval: null,
    mixedStream: null,
    
    // 🔧 新增：通话开始时间（用于计算通话时长）
    callStartTime: 0,

    // 防止重复发送call请求
    _hasSentCall: false,

    // 配置
    config: {
        iceServers: [
            { urls: 'stun:stun.l.google.com:19302' },
            {
                urls: 'turn:openrelay.metered.ca:80',
                username: 'openrelayproject',
                credential: 'openrelayproject'
            }
        ]
    },

    // 初始化
    async init() {
        this.consultant = Auth.checkAuth();
        // 从URL参数获取userId（用于测试或未登录场景）
        const urlParams = new URLSearchParams(window.location.search);
        const urlUserId = urlParams.get('userId');
        
        if (this.consultant) {
            this.currentUserId = this.consultant.userId || this.consultant.id;
        } else if (urlUserId) {
            // 未登录但URL中有userId，使用URL中的userId
            this.currentUserId = parseInt(urlUserId) || 0;
            console.log('Using userId from URL:', this.currentUserId);
        } else {
            console.error('Not logged in and no userId in URL');
            Utils.showToast('请先登录', 'error');
            return;
        }

        this.appointmentId = urlParams.get('appointmentId');
        this.role = urlParams.get('role') || 'callee';
        this.callType = (urlParams.get('type') || 'video').toLowerCase();
        this.isAudioCall = this.callType === 'audio';
        this.targetUserId = urlParams.get('targetUserId') || null;
        // 如果从聊天页面点击接听跳转过来，标记为已接听
        this.hasAcceptedCall = urlParams.get('accepted') === '1';

        if (!this.appointmentId) {
            Utils.showToast('预约ID不存在', 'error');
            return this.goBackToChat();
        }

        this.configureCallMode();
        await this.loadAppointmentInfo();

        // 请求媒体设备
        await this.requestMediaDevices();

        // 连接信令服务器
        this.connectSignaling();
    },

    // ---- 媒体设备 ----

    async requestMediaDevices() {
        try {
            this._setStatus('请求摄像头和麦克风...');
            this.localStream = await navigator.mediaDevices.getUserMedia({
                video: !this.isAudioCall,
                audio: true
            });
            const localVideo = document.getElementById('localVideo');
            if (localVideo) {
                localVideo.srcObject = this.localStream;
                localVideo.classList.add('active');
            }
            const localPh = document.getElementById('localVideoPlaceholder');
            if (localPh) localPh.classList.add('hidden');
            this._setStatus('媒体设备已就绪');
        } catch (err) {
            console.error('获取媒体设备失败:', err);
            this._setStatus('无法访问摄像头: ' + err.message);
            Utils.showToast('无法访问摄像头: ' + err.message, 'error');
        }
    },

    // ---- 信令连接 ----

    // 🔧 修复：断开已有的信令连接，避免重复连接导致房间人数异常
    disconnectSignaling() {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            console.log('[WebRTC] Closing existing signaling connection');
            // 设置标志以便在关闭后知道需要重新连接
            this._shouldReconnectAfterClose = false;
            // 🔧 发送 leave 消息，让服务器知道我们要离开
            try {
                this.ws.send(JSON.stringify({ type: 'leave', fromUserId: this.currentUserId }));
            } catch (e) {
                console.log('[WebRTC] Failed to send leave message:', e);
            }
            this.ws.close();
            // 注意：不立即设为 null，等待 onclose 事件处理
        }
    },

    connectSignaling() {
        this._setStatus('连接信令服务器...');
        // 🔧 修复：添加 userId 参数，让服务器可以识别同一用户的重复连接
        const wsUrl = `${CONFIG.VIDEO_SIGNALING_URL}?roomId=${this.appointmentId}&userId=${this.currentUserId}`;
        console.log('WebSocket URL:', wsUrl);
        console.log('Role:', this.role, 'isCaller:', this.role === 'caller');

        // 如果已有连接，先等待其关闭
        if (this.ws && this.ws.readyState !== WebSocket.CLOSED) {
            console.log('[WebRTC] Waiting for existing connection to close...');
            this._shouldReconnectAfterClose = true;
            this.ws.close();
            return; // onclose 会触发重新连接
        }

        this.ws = new WebSocket(wsUrl);

        this.ws.onopen = () => {
            console.log('Signaling connected, role=' + this.role);
            this._setStatus('已连接');
            if (this.role === 'caller') {
                this._setStatus('等待对方接入...');
                console.log('Caller: waiting for peer to join...');
            } else {
                this._setStatus('等待来电...');
                console.log('Callee: waiting for call...');
            }
        };

        this.ws.onmessage = (event) => {
            console.log('Received:', event.data);
            try {
                const data = JSON.parse(event.data);
                this.handleMessage(data);
            } catch (e) {
                console.error('Parse message failed:', e);
            }
        };

        this.ws.onclose = () => {
            console.log('Signaling disconnected');
            this._setStatus('连接已断开');
            this.ws = null; // 清除引用
            
            // 如果是计划内的重连，立即执行
            if (this._shouldReconnectAfterClose) {
                console.log('[WebRTC] Reconnecting after close...');
                this._shouldReconnectAfterClose = false;
                // 延迟一下再连接，确保旧连接已完全关闭
                setTimeout(() => this.connectSignaling(), 100);
                return;
            }
            
            // 3秒后自动重连（如果被叫且还在等待来电）
            if (this.role === 'callee' && !this.isInCall) {
                console.log('Callee: reconnecting in 3s...');
                setTimeout(() => this.connectSignaling(), 3000);
            }
        };

        this.ws.onerror = (e) => {
            console.error('Signaling error:', e);
            this._setStatus('连接错误');
        };
    },

    handleMessage(data) {
        switch (data.type) {
            case 'peerJoined':
                // 对方已加入房间
                console.log('peerJoined received, role=' + this.role + ', isInCall=' + this.isInCall + ', hasSentCall=' + this._hasSentCall);
                // 防止重复发送call请求：只有未发送过call且不在通话中时才发送
                if (this.role === 'caller' && !this.isInCall && !this._hasSentCall) {
                    console.log('Peer joined, sending call request');
                    this._hasSentCall = true;
                    this.sendCallRequest();
                } else {
                    console.log('Ignoring peerJoined (role=' + this.role + ', isInCall=' + this.isInCall + ', hasSentCall=' + this._hasSentCall + ')');
                }
                break;
            case 'peerLeft':
                // 对方离开房间，重置 hasSentCall 以便对方重新加入时可以再次发送 call
                console.log('peerLeft received, resetting hasSentCall');
                if (this.role === 'caller' && !this.isInCall) {
                    this._hasSentCall = false;
                    console.log('Reset _hasSentCall to false');
                }
                break;
            case 'call':
                // 被叫收到call请求，显示接听弹窗
                if (this.role === 'callee' && !this.isInCall) {
                    console.log('Received call request, showing incoming UI');
                    this.showIncomingUI();
                }
                break;
            case 'accept':
                // 主叫收到accept，开始发送offer
                if (this.role === 'caller' && !this.isInCall) {
                    console.log('Call accepted, starting WebRTC negotiation');
                    this.startCall();
                }
                break;
            case 'offer':
                // 被叫收到offer
                if (this.role === 'callee') {
                    if (this.hasAcceptedCall) {
                        this.handleOffer(data);
                    } else {
                        // 还没点击接听，缓存offer
                        console.log('Offer received before acceptance, caching');
                        this.pendingOffer = data;
                        this.showIncomingUI();
                    }
                }
                break;
            case 'answer':
                this.handleAnswer(data);
                break;
            case 'candidate':
                this.handleCandidate(data);
                break;
            case 'end':
                this.endCall();
                break;
            case 'reject':
                // 对方拒绝通话
                console.log('Call rejected by peer');
                this._setStatus('对方已拒绝通话');
                Utils.showToast('对方已拒绝通话', 'info');
                this.endCall();
                break;
            case 'recordingStarted':
                // 对方（咨询师）开始录制，显示提示但不启动本地录制
                console.log('recordingStarted from peer');
                this.showRecordingNotification(true);
                // 🔧 修改：仅电脑端（咨询师）录制，手机端只显示提示
                break;
            case 'recordingStopped':
                // 对方停止录制
                console.log('recordingStopped from peer');
                this.showRecordingNotification(false);
                break;
            case 'userCount':
                console.log('Room user count:', data.count);
                this.updatePeerOnlineStatus(data.count);
                break;
        }
    },

    sendCallRequest() {
        console.log('Sending call request, callType=' + this.callType);
        this.sendMessage({
            type: 'call',
            fromUserId: this.currentUserId,
            appointmentId: this.appointmentId,
            callType: this.callType
        });
    },

    sendMessage(data) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            const msg = JSON.stringify(data);
            console.log('Sending:', msg);
            try {
                this.ws.send(msg);
                console.log('Message sent successfully');
            } catch (e) {
                console.error('Failed to send message:', e);
            }
        } else {
            console.error('WebSocket not open, readyState:', this.ws ? this.ws.readyState : 'null');
        }
    },

    // ---- 通话流程 ----

    async startCall() {
        try {
            this.isInCall = true;
            this.callStartTime = Date.now(); // 🔧 修复：记录通话开始时间
            this.candidateQueue = [];
            this.remoteDescriptionSet = false;

            if (!this.localStream) {
                await this.requestMediaDevices();
            }

            this._setStatus('创建连接...');
            this._showWaitingPeerOverlay(false);
            this.peerConnection = new RTCPeerConnection(this.config);

            // 添加本地轨道
            this.localStream.getTracks().forEach(track => {
                this.peerConnection.addTrack(track, this.localStream);
            });

            // 远程视频
            this.peerConnection.ontrack = (event) => {
                console.log('Received remote stream');
                this.remoteStream = event.streams[0];
                const remoteVideo = document.getElementById('remoteVideo');
                if (remoteVideo) {
                    remoteVideo.srcObject = this.remoteStream;
                }
                const waiting = document.getElementById('vcWaiting');
                if (waiting) waiting.style.display = 'none';
                this._enterInCallState();
            };

            // ICE candidate
            this.peerConnection.onicecandidate = (event) => {
                if (event.candidate) {
                    this.sendMessage({
                        type: 'candidate',
                        candidate: {
                            candidate: event.candidate.candidate,
                            sdpMid: event.candidate.sdpMid,
                            sdpMLineIndex: event.candidate.sdpMLineIndex
                        }
                    });
                }
            };

            // 连接状态
            this.peerConnection.onconnectionstatechange = () => {
                console.log('Connection state:', this.peerConnection.connectionState);
                const net = document.getElementById('vcNetIndicator');
                if (!net) return;
                if (this.peerConnection.connectionState === 'connected') {
                    net.className = 'vc-net connected';
                    net.querySelector('span').textContent = '已连接';
                    this._enterInCallState();
                } else if (this.peerConnection.connectionState === 'disconnected' ||
                           this.peerConnection.connectionState === 'failed') {
                    net.className = 'vc-net error';
                    net.querySelector('span').textContent = '连接断开';
                }
            };

            // 创建 offer（语音通话时不请求视频）
            const offerOptions = {};
            if (this.isAudioCall) {
                offerOptions.offerToReceiveVideo = false;
                offerOptions.offerToReceiveAudio = true;
            }
            const offer = await this.peerConnection.createOffer(offerOptions);
            await this.peerConnection.setLocalDescription(offer);

            // 直接发送offer（不需要call/accept握手）
            this.sendMessage({ type: 'offer', sdp: offer.sdp });

            this._setStatus('等待对方响应...');
        } catch (err) {
            console.error('Start call failed:', err);
            this._setStatus('发起通话失败: ' + err.message);
        }
    },

    async handleOffer(data) {
        try {
            this.isInCall = true;
            console.log('handleOffer: creating peer connection');
            
            // 🔧 修复：确保本地媒体流已就绪
            if (!this.localStream) {
                await this.requestMediaDevices();
            }
            
            if (!this.peerConnection) {
                this.peerConnection = new RTCPeerConnection(this.config);

                if (this.localStream) {
                    this.localStream.getTracks().forEach(track => {
                        this.peerConnection.addTrack(track, this.localStream);
                    });
                }

                this.peerConnection.ontrack = (event) => {
                    console.log('ontrack: received remote stream');
                    this.remoteStream = event.streams[0];
                    const remoteVideo = document.getElementById('remoteVideo');
                    if (remoteVideo && !this.isAudioCall) {
                        remoteVideo.srcObject = this.remoteStream;
                    }
                    // 隐藏等待遮罩
                    const waiting = document.getElementById('vcWaiting');
                    if (waiting) waiting.style.display = 'none';
                    // 语音通话时即使没有视频也要进入通话状态
                    this._enterInCallState();
                };

                this.peerConnection.onicecandidate = (event) => {
                    if (event.candidate) {
                        console.log('onicecandidate: sending candidate');
                        this.sendMessage({
                            type: 'candidate',
                            candidate: {
                                candidate: event.candidate.candidate,
                                sdpMid: event.candidate.sdpMid,
                                sdpMLineIndex: event.candidate.sdpMLineIndex
                            }
                        });
                    }
                };

                this.peerConnection.onconnectionstatechange = () => {
                    console.log('connection state:', this.peerConnection.connectionState);
                    const net = document.getElementById('vcNetIndicator');
                    if (!net) return;
                    if (this.peerConnection.connectionState === 'connected') {
                        net.className = 'vc-net connected';
                        net.querySelector('span').textContent = '已连接';
                    }
                };
            }

            console.log('handleOffer: setting remote description');
            await this.peerConnection.setRemoteDescription(new RTCSessionDescription({
                type: 'offer',
                sdp: data.sdp
            }));
            this.remoteDescriptionSet = true;
            console.log('handleOffer: remote description set, flushing candidate queue');
            await this.flushCandidateQueue();

            console.log('handleOffer: creating answer');
            const answerOptions = {};
            if (this.isAudioCall) {
                answerOptions.offerToReceiveVideo = false;
                answerOptions.offerToReceiveAudio = true;
            }
            const answer = await this.peerConnection.createAnswer(answerOptions);
            console.log('handleOffer: setting local description');
            await this.peerConnection.setLocalDescription(answer);

            console.log('handleOffer: sending answer');
            this.sendMessage({ type: 'answer', sdp: answer.sdp });
            console.log('handleOffer: done');
        } catch (err) {
            console.error('Handle offer failed:', err);
        }
    },

    async handleAnswer(data) {
        try {
            console.log('handleAnswer: setting remote description');
            if (this.peerConnection) {
                await this.peerConnection.setRemoteDescription(new RTCSessionDescription({
                    type: 'answer',
                    sdp: data.sdp
                }));
                this.remoteDescriptionSet = true;
                console.log('handleAnswer: remote description set, flushing candidate queue');
                await this.flushCandidateQueue();
                console.log('handleAnswer: done');
            }
        } catch (err) {
            console.error('Handle answer failed:', err);
        }
    },

    async handleCandidate(data) {
        try {
            if (this.peerConnection && data.candidate) {
                let candidateObj = data.candidate;
                if (typeof candidateObj === 'string') {
                    candidateObj = JSON.parse(candidateObj);
                }

                const candidate = {
                    candidate: candidateObj.candidate,
                    sdpMid: candidateObj.sdpMid,
                    sdpMLineIndex: candidateObj.sdpMLineIndex
                };

                if (this.remoteDescriptionSet) {
                    await this.peerConnection.addIceCandidate(candidate);
                } else {
                    this.candidateQueue.push(candidate);
                }
            }
        } catch (err) {
            console.error('Handle candidate failed:', err);
        }
    },

    async flushCandidateQueue() {
        for (const candidate of this.candidateQueue) {
            try {
                await this.peerConnection.addIceCandidate(candidate);
            } catch (err) {
                console.error('Add queued candidate failed:', err);
            }
        }
        this.candidateQueue = [];
    },

    // ---- UI 控制 ----

    showIncomingUI() {
        this._showIncomingOverlay(true);
        const name = (this.appointment && this.appointment.childName)
            ? this.appointment.childName
            : '家长';
        document.getElementById('incomingName').textContent = name;
        document.getElementById('incomingSub').textContent =
            this.isAudioCall ? '语音来电…' : '视频来电…';
    },

    async acceptIncomingCall() {
        this._showIncomingOverlay(false);
        this._setStatus('正在接听...');
        this.isInCall = true;
        this.callStartTime = Date.now(); // 🔧 修复：记录通话开始时间
        this.hasAcceptedCall = true;
        
        // 发送accept通知对方可以发送offer了
        this.sendMessage({
            type: 'accept',
            fromUserId: this.currentUserId,
            appointmentId: this.appointmentId
        });
        
        // 如果有缓存的offer，立即处理
        if (this.pendingOffer) {
            console.log('Processing pending offer');
            await this.handleOffer(this.pendingOffer);
            this.pendingOffer = null;
        }
    },

    rejectIncomingCall() {
        this._showIncomingOverlay(false);
        this.sendMessage({ type: 'end' });
        this.goBackToChat();
    },

    async toggleMute() {
        if (!this.localStream) return;
        const audioTrack = this.localStream.getAudioTracks()[0];
        if (audioTrack) {
            audioTrack.enabled = !audioTrack.enabled;
            const btn = document.getElementById('btnMute');
            const icOn = btn?.querySelector('.ic-mic-on');
            const icOff = btn?.querySelector('.ic-mic-off');
            const lbl = document.getElementById('lblMute');
            const enabled = audioTrack.enabled;
            if (icOn) icOn.style.display = enabled ? 'block' : 'none';
            if (icOff) icOff.style.display = enabled ? 'none' : 'block';
            if (lbl) lbl.textContent = enabled ? '静音' : '已静音';
        }
    },

    async toggleVideo() {
        if (!this.localStream || this.isAudioCall) return;
        const videoTrack = this.localStream.getVideoTracks()[0];
        if (videoTrack) {
            videoTrack.enabled = !videoTrack.enabled;
            const btn = document.getElementById('btnVideo');
            const icOn = btn?.querySelector('.ic-cam-on');
            const icOff = btn?.querySelector('.ic-cam-off');
            const lbl = document.getElementById('lblVideo');
            const enabled = videoTrack.enabled;
            if (icOn) icOn.style.display = enabled ? 'block' : 'none';
            if (icOff) icOff.style.display = enabled ? 'none' : 'block';
            if (lbl) lbl.textContent = enabled ? '关摄像头' : '开摄像头';
        }
    },

    async endCall() {
        // 停止录制并上传
        if (this.isRecording) {
            await this.stopAndUploadRecording();
        }

        this.sendMessage({ type: 'end' });

        if (this.peerConnection) {
            this.peerConnection.close();
            this.peerConnection = null;
        }

        if (this.localStream) {
            this.localStream.getTracks().forEach(track => track.stop());
            this.localStream = null;
        }

        this.isInCall = false;
        this._hasSentCall = false;
        this.candidateQueue = [];
        this.remoteDescriptionSet = false;

        // 保存通话记录到聊天
        this.saveCallRecordToChat();

        this.goBackToChat();
    },

    // 保存通话记录到聊天
    async saveCallRecordToChat() {
        try {
            // 🔧 修复：计算通话时长
            let durationSec = 0;
            let durationStr = '';
            if (this.callStartTime > 0) {
                durationSec = Math.floor((Date.now() - this.callStartTime) / 1000);
                if (durationSec < 60) {
                    durationStr = durationSec + '秒';
                } else if (durationSec < 3600) {
                    durationStr = Math.floor(durationSec / 60) + '分' + (durationSec % 60) + '秒';
                } else {
                    durationStr = Math.floor(durationSec / 3600) + '时' + Math.floor((durationSec % 3600) / 60) + '分';
                }
            }
            
            const callTypeCode = this.isAudioCall ? 'audio' : 'video';
            let content;
            if (durationSec > 0) {
                content = `CALL_ENDED:${callTypeCode}:${durationSec}:${durationStr}`;
            } else {
                content = `CALL_ENDED:${callTypeCode}:0:已取消`;
            }
            
            const message = {
                appointmentId: this.appointmentId,
                content: content,
                messageType: 'system'
            };

            await API.post('/chat/send', message);
            console.log('Call record saved to chat:', content);
        } catch (err) {
            console.error('Save call record failed:', err);
        }
    },

    // ---- 录制功能 ----

    showRecordingNotification(started) {
        if (started) {
            Utils.showToast('对方已开始录制通话', 'info');
        } else {
            Utils.showToast('对方已停止录制', 'info');
        }
    },

    // 🔧 初始化录制画布：创建画中画效果（大画面=远程，小画面=本地）
    _initRecordingCanvas() {
        if (!this.recordingCanvas) {
            this.recordingCanvas = document.getElementById('recordingCanvas');
            if (!this.recordingCanvas) {
                console.error('Recording canvas not found');
                return false;
            }
            this.recordingCanvasCtx = this.recordingCanvas.getContext('2d');
        }
        return true;
    },

    // 🔧 绘制画中画画面到画布
    _drawCanvasFrame() {
        if (!this.recordingCanvas || !this.recordingCanvasCtx) return;
        
        const canvas = this.recordingCanvas;
        const ctx = this.recordingCanvasCtx;
        const remoteVideo = document.getElementById('remoteVideo');
        const localVideo = document.getElementById('localVideo');
        
        // 画布尺寸 1280x720
        const cw = canvas.width;
        const ch = canvas.height;
        
        // 清空画布
        ctx.fillStyle = '#000000';
        ctx.fillRect(0, 0, cw, ch);
        
        // 1. 绘制远程视频（大画面，全屏）
        if (remoteVideo && remoteVideo.readyState >= 2 && remoteVideo.videoWidth > 0) {
            // 保持比例缩放填充
            const scale = Math.max(cw / remoteVideo.videoWidth, ch / remoteVideo.videoHeight);
            const dw = remoteVideo.videoWidth * scale;
            const dh = remoteVideo.videoHeight * scale;
            const dx = (cw - dw) / 2;
            const dy = (ch - dh) / 2;
            ctx.drawImage(remoteVideo, dx, dy, dw, dh);
        } else {
            // 远程视频未就绪，显示提示文字
            ctx.fillStyle = '#1a1a2e';
            ctx.fillRect(0, 0, cw, ch);
            ctx.fillStyle = '#888';
            ctx.font = '24px sans-serif';
            ctx.textAlign = 'center';
            ctx.fillText('等待对方视频...', cw / 2, ch / 2);
        }
        
        // 2. 绘制本地视频（小画面，右下角，画中画）
        if (localVideo && localVideo.readyState >= 2 && localVideo.videoWidth > 0) {
            const pipWidth = 320;   // 小画面宽度
            const pipHeight = 240;  // 小画面高度
            const pipMargin = 20;   // 边距
            const pipX = cw - pipWidth - pipMargin;
            const pipY = ch - pipHeight - pipMargin;
            
            // 绘制小画面背景（圆角边框效果）
            ctx.fillStyle = 'rgba(0,0,0,0.5)';
            ctx.fillRect(pipX - 4, pipY - 4, pipWidth + 8, pipHeight + 8);
            
            // 绘制本地视频
            ctx.drawImage(localVideo, pipX, pipY, pipWidth, pipHeight);
            
            // 绘制边框
            ctx.strokeStyle = 'rgba(255,255,255,0.6)';
            ctx.lineWidth = 2;
            ctx.strokeRect(pipX, pipY, pipWidth, pipHeight);
            
            // 绘制"我"的标签
            ctx.fillStyle = 'rgba(0,0,0,0.6)';
            ctx.fillRect(pipX, pipY, 40, 24);
            ctx.fillStyle = '#fff';
            ctx.font = '12px sans-serif';
            ctx.textAlign = 'left';
            ctx.fillText('我', pipX + 8, pipY + 16);
        }
        
        // 3. 绘制录制指示器（红点）
        if (this.isRecording) {
            const now = Date.now();
            if (Math.floor(now / 1000) % 2 === 0) { // 每秒闪烁
                ctx.beginPath();
                ctx.arc(30, 30, 10, 0, Math.PI * 2);
                ctx.fillStyle = '#ff4444';
                ctx.fill();
                
                ctx.fillStyle = '#fff';
                ctx.font = '14px sans-serif';
                ctx.textAlign = 'left';
                ctx.fillText('REC', 48, 35);
            }
        }
    },

    startRecording() {
        if (this.isRecording) {
            return;
        }

        try {
            console.log('Starting recording with picture-in-picture...');
            
            // 🔧 初始化画布
            if (!this._initRecordingCanvas()) {
                Utils.showToast('录制初始化失败', 'error');
                return;
            }
            
            // 🔧 获取视频元素
            const remoteVideo = document.getElementById('remoteVideo');
            const localVideo = document.getElementById('localVideo');
            
            if (!remoteVideo || !localVideo) {
                console.error('Video elements not found');
                Utils.showToast('视频元素未找到', 'error');
                return;
            }
            
            // 🔧 开始定时绘制画布（30fps）
            this.canvasDrawInterval = setInterval(() => {
                this._drawCanvasFrame();
            }, 1000 / 30);
            
            // 🔧 从画布捕获视频流
            const canvasStream = this.recordingCanvas.captureStream(30); // 30fps
            
            // 🔧 混合音频：本地音频 + 远程音频
            const audioContext = new AudioContext();
            const dest = audioContext.createMediaStreamDestination();
            
            // 添加本地音频
            if (this.localStream && this.localStream.getAudioTracks().length > 0) {
                const localSource = audioContext.createMediaStreamSource(this.localStream);
                localSource.connect(dest);
            }
            
            // 添加远程音频
            if (this.remoteStream && this.remoteStream.getAudioTracks().length > 0) {
                const remoteSource = audioContext.createMediaStreamSource(this.remoteStream);
                remoteSource.connect(dest);
            }
            
            // 将音频轨道添加到画布视频流
            dest.stream.getAudioTracks().forEach(track => {
                canvasStream.addTrack(track);
            });
            
            this.mixedStream = canvasStream;
            
            // 设置录制选项
            const options = {
                mimeType: 'video/webm;codecs=vp9,opus'
            };
            if (!MediaRecorder.isTypeSupported(options.mimeType)) {
                options.mimeType = 'video/webm;codecs=vp8,opus';
                if (!MediaRecorder.isTypeSupported(options.mimeType)) {
                    options.mimeType = 'video/webm';
                }
            }

            this.recordedBlobs = [];
            this.mediaRecorder = new MediaRecorder(this.mixedStream, options);
            this.recordingStartTime = Date.now();

            this.mediaRecorder.ondataavailable = (event) => {
                if (event.data && event.data.size > 0) {
                    this.recordedBlobs.push(event.data);
                }
            };

            this.mediaRecorder.onstop = () => {
                console.log('Recording stopped, blobs:', this.recordedBlobs.length);
            };

            this.mediaRecorder.start(1000); // 每秒收集数据
            this.isRecording = true;

            // 显示录制指示器
            this.showRecordingIndicator(true);

            // 通知对方开始录制
            this.sendMessage({ type: 'recordingStarted' });

            Utils.showToast('通话已开始录制（画中画模式）', 'info');

        } catch (err) {
            console.error('Start recording failed:', err);
            Utils.showToast('录制启动失败: ' + err.message, 'error');
            // 清理
            this._cleanupRecording();
        }
    },

    // 🔧 清理录制资源
    _cleanupRecording() {
        if (this.canvasDrawInterval) {
            clearInterval(this.canvasDrawInterval);
            this.canvasDrawInterval = null;
        }
        if (this.mixedStream) {
            this.mixedStream.getTracks().forEach(track => track.stop());
            this.mixedStream = null;
        }
        this.isRecording = false;
    },

    stopRecording() {
        if (!this.isRecording || !this.mediaRecorder) return;

        try {
            console.log('Stopping recording...');
            this.mediaRecorder.stop();
            
            // 🔧 清理画布定时器和流
            this._cleanupRecording();

            // 隐藏录制指示器
            this.showRecordingIndicator(false);

            // 通知对方停止录制
            this.sendMessage({ type: 'recordingStopped' });

        } catch (err) {
            console.error('Stop recording failed:', err);
        }
    },

    async stopAndUploadRecording() {
        this.stopRecording();

        if (this.recordedBlobs.length === 0) {
            console.log('No recorded data to upload');
            return;
        }

        // 🔧 辅助函数：同时输出到 console 和 localStorage（防止页面跳转丢失日志）
        const log = (key, value) => {
            const msg = `[Upload] ${key}: ${JSON.stringify(value)}`;
            console.log(msg);
            const logs = JSON.parse(localStorage.getItem('uploadDebugLogs') || '[]');
            logs.push({time: new Date().toISOString(), key, value: String(value)});
            localStorage.setItem('uploadDebugLogs', JSON.stringify(logs.slice(-50))); // 保留最近50条
        };

        try {
            const blob = new Blob(this.recordedBlobs, { type: 'video/webm' });
            log('blobSize', blob.size);

            if (blob.size === 0) {
                console.warn('Recording blob is empty, skipping upload');
                return;
            }

            const formData = new FormData();
            formData.append('file', blob, 'call_recording.webm');

            const token = API.getToken();
            const uploadUrl = API.getUrl('/upload/chat-video');
            log('uploadUrl', uploadUrl);
            log('tokenExists', !!token);
            log('currentUserId', this.currentUserId);
            log('targetUserId', this.targetUserId);
            log('appointmentId', this.appointmentId);

            const response = await fetch(uploadUrl, {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + token
                },
                body: formData
            });

            log('responseStatus', response.status);
            const responseText = await response.text();
            log('responseText', responseText.substring(0, 500)); // 只保留前500字符

            if (response.ok) {
                try {
                    const result = JSON.parse(responseText);
                    log('parseSuccess', true);
                    log('resultData', result.data);
                    log('resultUrl', result.url);
                    
                    // 🔧 修复：服务器返回的字段名是 "data" 不是 "url"
                    const mediaUrl = result.data || result.url;
                    if (mediaUrl) {
                        // 保存到聊天记录
                        await this.saveVideoMessage(mediaUrl);
                        Utils.showToast('录制已保存', 'success');
                        log('saveSuccess', true);
                    } else {
                        log('noMediaUrl', true);
                        Utils.showToast('录制上传失败：服务器未返回文件地址', 'error');
                    }
                } catch (e) {
                    log('parseError', e.message);
                    Utils.showToast('录制上传失败：解析响应失败', 'error');
                }
            } else {
                log('httpError', response.status);
                Utils.showToast('上传录制失败：HTTP ' + response.status, 'error');
            }

        } catch (err) {
            log('catchError', err.message);
            Utils.showToast('上传录制失败: ' + err.message, 'error');
        }
    },

    async saveVideoMessage(mediaUrl) {
        try {
            if (!this.appointmentId) return;

            const message = {
                appointmentId: Number(this.appointmentId),
                senderUserId: this.currentUserId,
                receiverUserId: this.targetUserId,
                content: '[视频录制] 通话已录制',
                mediaUrl: mediaUrl,
                messageType: 'VIDEO',
                isFromConsultant: true
            };

            await API.post('/messages', message);
            console.log('Video recording saved to chat');

        } catch (err) {
            console.error('Save video message failed:', err);
        }
    },

    showRecordingIndicator(show) {
        let indicator = document.getElementById('recordingIndicator');
        if (!indicator) {
            // 创建录制指示器
            indicator = document.createElement('div');
            indicator.id = 'recordingIndicator';
            indicator.className = 'vc-recording-indicator';
            indicator.innerHTML = `
                <div class="vc-recording-dot"></div>
                <span class="vc-recording-text">通话录制中</span>
            `;
            document.querySelector('.vc-root').appendChild(indicator);
        }
        indicator.style.display = show ? 'flex' : 'none';
    },

    // ---- 辅助方法 ----

    // ---- 辅助方法 ----

    _setStatus(text) {
        const el = document.getElementById('connStatusText');
        if (el) el.textContent = text;
    },

    _showLoading(show) {
        const el = document.getElementById('connectionStatus');
        if (el) el.classList.toggle('show', show);
    },

    _showIncomingOverlay(show) {
        const el = document.getElementById('incomingCallOverlay');
        if (el) el.style.display = show ? 'flex' : 'none';
    },

    _showWaitingPeerOverlay(show) {
        const el = document.getElementById('waitingPeerOverlay');
        if (el) el.style.display = show ? 'flex' : 'none';
    },

    _enterInCallState() {
        const waiting = document.getElementById('vcWaiting');
        const stateEl = document.getElementById('callStateText');
        if (waiting) waiting.style.display = 'none';
        if (stateEl) {
            stateEl.textContent = '通话中';
            stateEl.className = 'vc-state active';
        }
        this._showWaitingPeerOverlay(false);
        
        // 通话连接后自动开始录制
        if (!this.isRecording) {
            this.startRecording();
        }
    },

    // 🔧 新增：更新对方在线状态显示
    updatePeerOnlineStatus(count) {
        const isPeerOnline = count >= 2;
        const statusEl = document.getElementById('peerOnlineStatus');
        if (statusEl) {
            if (isPeerOnline) {
                statusEl.innerHTML = '<span class="status-dot online"></span>对方已在线';
                statusEl.className = 'peer-status online';
            } else {
                statusEl.innerHTML = '<span class="status-dot offline"></span>等待对方接入...';
                statusEl.className = 'peer-status waiting';
            }
        }
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
            document.title = '语音通话 - 童康源';
            if (videoCtrl) videoCtrl.style.display = 'none';
            if (localWrap) localWrap.style.display = 'none';
        }
    },

    async loadAppointmentInfo() {
        try {
            const appointment = await API.get(`/appointments/${this.appointmentId}`);
            if (appointment) {
                this.appointment = appointment;
                this.displayClientInfo();
            }
        } catch (error) {
            console.error('加载预约失败', error);
        }
    },

    displayClientInfo() {
        const name = (this.appointment && this.appointment.childName)
            ? this.appointment.childName
            : '家长';
        const nameEl = document.getElementById('callClientName');
        const waitingNameEl = document.getElementById('waitingPeerName');
        const incomingNameEl = document.getElementById('incomingName');
        if (nameEl) nameEl.textContent = name;
        if (waitingNameEl) waitingNameEl.textContent = name;
        if (incomingNameEl) incomingNameEl.textContent = name;
    },

    goBackToChat() {
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        const url = this.appointmentId
            ? `chat.html?appointmentId=${this.appointmentId}`
            : 'dashboard.html';
        // 使用 replace 跳转，避免浏览器返回时回到视频通话页面
        window.location.replace(url);
    }
};

// 页面加载时初始化
window.addEventListener('DOMContentLoaded', () => {
    WebRTCVideoCall.init();
});

// 🔧 修复：页面关闭/刷新时断开 WebSocket 连接，确保信令服务器正确更新在线人数
window.addEventListener('beforeunload', () => {
    console.log('[WebRTC Video] Page unloading, disconnecting signaling...');
    WebRTCVideoCall.disconnectSignaling();
});

// 🔧 额外处理：页面隐藏时也断开连接（移动端切换应用时）
document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'hidden') {
        console.log('[WebRTC Video] Page hidden, disconnecting signaling...');
        WebRTCVideoCall.disconnectSignaling();
    }
});
