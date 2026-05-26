<template>
  <div class="container">
    <h1>视频通话 (Web 端)</h1>
    <div class="video-container">
      <div class="video-wrapper">
        <video ref="localVideo" autoplay muted playsinline></video>
        <span class="label">本地</span>
        <div v-if="isRecording" class="recording-indicator">录制中</div>
      </div>
      <div class="video-wrapper">
        <video ref="remoteVideo" autoplay playsinline></video>
        <span class="label">远程</span>
      </div>
    </div>
    <!-- 隐藏的 canvas 用于录制 -->
    <canvas ref="canvas" style="display: none;"></canvas>
    <div class="status">
      <p>状态: <span :class="statusClass">{{ status }}</span></p>
      <p v-if="isRecording">录制状态: 正在录制本地和远程视频</p>
    </div>
    <div v-if="error" class="error">
      <p>错误: {{ error }}</p>
    </div>
    <div v-if="logs.length > 0" class="logs">
      <p>日志:</p>
      <ul>
        <li v-for="(log, index) in logs" :key="index">{{ log }}</li>
      </ul>
    </div>
    <div class="controls">
      <button @click="startCall" :disabled="isInCall || !localStream" class="btn btn-green">
        开始通话
      </button>
      <button @click="endCall" :disabled="!isInCall" class="btn btn-red">
        结束通话
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      ws: null,
      localStream: null,
      remoteStream: null,
      peerConnection: null,
      isInCall: false,
      status: '未连接',
      error: null,
      logs: [],
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
      candidateQueue: [],
      remoteDescriptionSet: false,
      // 录制相关
      isRecording: false,
      localRecorder: null,
      remoteRecorder: null,
      recordedChunks: [],
      recordingInterval: null,
      saveDirectory: 'E:\\tools\\Trae\\task\\VideoRecording'
    }
  },
  computed: {
    statusClass() {
      return {
        'status-connected': this.status === '已连接',
        'status-calling': this.status === '通话中'
      }
    }
  },
  mounted() {
    this.addLog('页面加载完成');
    this.requestMediaDevices();
    this.connectWebSocket();
  },
  beforeDestroy() {
    this.stopRecording();
  },
  methods: {
    addLog(message) {
      const time = new Date().toLocaleTimeString();
      this.logs.unshift(`[${time}] ${message}`);
      if (this.logs.length > 30) this.logs.pop();
    },

    async requestMediaDevices() {
      try {
        this.addLog('请求摄像头和麦克风权限...');
        this.localStream = await navigator.mediaDevices.getUserMedia({
          video: true,
          audio: true
        });
        this.addLog('摄像头和麦克风权限已获取');
        this.$refs.localVideo.srcObject = this.localStream;
        this.error = null;
      } catch (err) {
        this.addLog('获取媒体设备失败: ' + err.message);
        this.error = '无法访问摄像头: ' + err.message;
      }
    },

    connectWebSocket() {
      this.addLog('连接信令服务器...');
      const wsUrl = `ws://${window.location.hostname}:8080/signaling`;
      this.addLog('WebSocket URL: ' + wsUrl);
      this.ws = new WebSocket(wsUrl);

      this.ws.onopen = () => {
        this.addLog('WebSocket 连接成功');
        this.status = '已连接';
        this.error = null;
      };

      this.ws.onmessage = (event) => {
        this.addLog('【收到完整消息】' + event.data);
        try {
          const data = JSON.parse(event.data);
          this.handleMessage(data);
        } catch (e) {
          this.addLog('【消息解析失败】' + e.message);
          this.error = '消息解析失败: ' + e.message;
        }
      };

      this.ws.onclose = () => {
        this.addLog('WebSocket 连接已关闭');
        this.status = '未连接';
        this.stopRecording();
      };

      this.ws.onerror = () => {
        this.addLog('WebSocket 错误');
        this.error = 'WebSocket 连接错误';
      };
    },

    handleMessage(data) {
      this.addLog('【处理消息】类型: ' + data.type);

      switch (data.type) {
        case 'call':
          this.addLog('【处理call】收到来电请求');
          if (confirm('有来电，是否接听？')) {
            this.handleIncomingCall();
          } else {
            this.sendMessage({ type: 'end' });
          }
          break;
        case 'offer':
          this.addLog('【处理offer】offer sdp 长度: ' + (data.sdp ? data.sdp.length : 0));
          this.handleOffer(data);
          break;
        case 'answer':
          this.addLog('【处理answer】answer sdp 长度: ' + (data.sdp ? data.sdp.length : 0));
          this.handleAnswer(data);
          break;
        case 'candidate':
          this.addLog('【处理candidate】收到 candidate');
          this.handleCandidate(data);
          break;
        case 'end':
          this.addLog('【处理end】收到结束通话消息');
          this.endCall();
          break;
        case 'userCount':
          this.addLog('当前在线人数: ' + data.count);
          break;
      }
    },

    async startCall() {
      try {
        this.addLog('开始通话...');
        this.error = null;
        
        this.candidateQueue = [];
        this.remoteDescriptionSet = false;

        if (!this.localStream) {
          this.addLog('本地视频流未准备好');
          await this.requestMediaDevices();
          if (!this.localStream) {
            this.error = '无法获取摄像头';
            return;
          }
        }

        this.addLog('创建 PeerConnection...');
        this.peerConnection = new RTCPeerConnection(this.config);

        this.localStream.getTracks().forEach(track => {
          this.peerConnection.addTrack(track, this.localStream);
          this.addLog('添加 track: ' + track.kind);
        });

        this.peerConnection.ontrack = (event) => {
          this.addLog('收到远程视频流');
          this.remoteStream = event.streams[0];
          this.$refs.remoteVideo.srcObject = this.remoteStream;
          // 开始录制远程视频
          this.startRemoteRecording();
        };

        this.peerConnection.onicecandidate = (event) => {
          if (event.candidate) {
            this.addLog('发送 ICE candidate: ' + event.candidate.candidate.substring(0, 50) + '...');
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
          this.addLog('连接状态: ' + this.peerConnection.connectionState);
          if (this.peerConnection.connectionState === 'connected') {
            this.addLog('通话已接通，开始录制');
            this.startRecording();
          } else if (this.peerConnection.connectionState === 'disconnected' || 
                     this.peerConnection.connectionState === 'failed' ||
                     this.peerConnection.connectionState === 'closed') {
            this.addLog('通话已断开，停止录制');
            this.stopRecording();
          }
        };

        const offer = await this.peerConnection.createOffer();
        this.addLog('创建 offer 成功');
        await this.peerConnection.setLocalDescription(offer);

        this.addLog('发送 call 信号');
        this.sendMessage({ type: 'call' });

        this.addLog('发送 offer (使用 sdp 字段)');
        this.sendMessage({
          type: 'offer',
          sdp: offer.sdp
        });

        this.isInCall = true;
        this.status = '通话中';
      } catch (err) {
        this.addLog('错误: ' + err.message);
        this.error = err.message;
        this.status = '错误';
      }
    },

    async handleIncomingCall() {
      this.addLog('接听来电');
      if (!this.localStream) {
        this.addLog('本地视频流未准备好');
        await this.requestMediaDevices();
      }
      this.isInCall = true;
      this.status = '通话中';
    },

    async flushCandidateQueue() {
      this.addLog('【flushCandidateQueue】开始刷新队列，数量: ' + this.candidateQueue.length);
      for (let i = 0; i < this.candidateQueue.length; i++) {
        const candidate = this.candidateQueue[i];
        this.addLog('【flushCandidateQueue】处理队列中的 candidate ' + (i+1) + '/' + this.candidateQueue.length);
        try {
          await this.peerConnection.addIceCandidate(candidate);
          this.addLog('【flushCandidateQueue】队列 candidate ' + (i+1) + ' 添加成功');
        } catch (err) {
          this.addLog('【flushCandidateQueue】队列 candidate ' + (i+1) + ' 添加失败: ' + err.message);
        }
      }
      this.candidateQueue = [];
      this.addLog('【flushCandidateQueue】队列已清空');
    },

    async handleOffer(data) {
      try {
        if (!this.peerConnection) {
          this.addLog('创建 PeerConnection (for offer)...');
          this.peerConnection = new RTCPeerConnection(this.config);

          if (this.localStream) {
            this.localStream.getTracks().forEach(track => {
              this.peerConnection.addTrack(track, this.localStream);
            });
          }

          this.peerConnection.ontrack = (event) => {
            this.addLog('收到远程视频流');
            this.remoteStream = event.streams[0];
            this.$refs.remoteVideo.srcObject = this.remoteStream;
            // 开始录制远程视频
            this.startRemoteRecording();
          };

          this.peerConnection.onicecandidate = (event) => {
            if (event.candidate) {
              this.addLog('发送 ICE candidate: ' + event.candidate.candidate.substring(0, 50) + '...');
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
            this.addLog('连接状态: ' + this.peerConnection.connectionState);
            if (this.peerConnection.connectionState === 'connected') {
              this.addLog('通话已接通，开始录制');
              this.startRecording();
            } else if (this.peerConnection.connectionState === 'disconnected' || 
                       this.peerConnection.connectionState === 'failed' ||
                       this.peerConnection.connectionState === 'closed') {
              this.addLog('通话已断开，停止录制');
              this.stopRecording();
            }
          };
        }

        this.addLog('设置远程描述 (offer)');
        await this.peerConnection.setRemoteDescription(new RTCSessionDescription({
          type: 'offer',
          sdp: data.sdp
        }));
        this.addLog('设置远程描述成功');

        this.remoteDescriptionSet = true;
        this.addLog('remoteDescriptionSet 设为 true');

        await this.flushCandidateQueue();

        this.addLog('创建 answer');
        const answer = await this.peerConnection.createAnswer();
        this.addLog('设置本地描述 (answer)');
        await this.peerConnection.setLocalDescription(answer);

        this.addLog('发送 answer (使用 sdp 字段)');
        this.sendMessage({
          type: 'answer',
          sdp: answer.sdp
        });
      } catch (err) {
        this.addLog('处理 offer 错误: ' + err.message);
        this.error = err.message;
      }
    },

    async handleAnswer(data) {
      try {
        if (this.peerConnection) {
          this.addLog('设置远程描述 (answer)');
          await this.peerConnection.setRemoteDescription(new RTCSessionDescription({
            type: 'answer',
            sdp: data.sdp
          }));
          this.addLog('设置远程描述成功');

          this.remoteDescriptionSet = true;
          this.addLog('remoteDescriptionSet 设为 true');

          await this.flushCandidateQueue();
        }
      } catch (err) {
        this.addLog('处理 answer 错误: ' + err.message);
        this.error = err.message;
      }
    },

    async handleCandidate(data) {
      try {
        if (this.peerConnection && data.candidate) {
          this.addLog('【handleCandidate】原始 candidate: ' + JSON.stringify(data.candidate));
          
          let candidateObj = data.candidate;
          if (typeof candidateObj === 'string') {
            try {
              candidateObj = JSON.parse(candidateObj);
              this.addLog('【handleCandidate】成功解析 candidate 字符串为对象');
            } catch (e) {
              this.addLog('【handleCandidate】Candidate 字符串解析失败');
            }
          }
          
          const candidateStr = candidateObj && candidateObj.candidate ? candidateObj.candidate : '';
          
          if (!candidateStr) {
            this.addLog('【handleCandidate】跳过空的 ICE candidate');
            return;
          }
          
          const logStr = candidateStr.length > 50 ? candidateStr.substring(0, 50) + '...' : candidateStr;
          this.addLog('【handleCandidate】准备添加 ICE candidate: ' + logStr);
          
          const finalCandidate = {
            candidate: candidateStr,
            sdpMid: candidateObj ? candidateObj.sdpMid : null,
            sdpMLineIndex: candidateObj ? candidateObj.sdpMLineIndex : null
          };
          
          this.addLog('【handleCandidate】最终 candidate: ' + JSON.stringify(finalCandidate));
          this.addLog('【handleCandidate】remoteDescriptionSet: ' + this.remoteDescriptionSet);

          if (this.remoteDescriptionSet) {
            this.addLog('【handleCandidate】可以添加，remoteDescription 已设置');
            await this.peerConnection.addIceCandidate(finalCandidate);
            this.addLog('【handleCandidate】添加 ICE candidate 成功');
          } else {
            this.addLog('【handleCandidate】remoteDescription 未设置，先缓存');
            this.candidateQueue.push(finalCandidate);
            this.addLog('【handleCandidate】已缓存，队列长度: ' + this.candidateQueue.length);
          }
        }
      } catch (err) {
        this.addLog('【handleCandidate】处理 candidate 错误: ' + err.message);
        this.error = err.message;
      }
    },

    endCall() {
      this.addLog('结束通话...');

      if (this.peerConnection) {
        this.peerConnection.close();
        this.peerConnection = null;
      }

      if (this.$refs.remoteVideo) {
        this.$refs.remoteVideo.srcObject = null;
      }

      this.sendMessage({ type: 'end' });

      this.candidateQueue = [];
      this.remoteDescriptionSet = false;
      
      this.isInCall = false;
      this.status = '已连接';
      this.error = null;
      
      // 停止录制
      this.stopRecording();
    },

    sendMessage(data) {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        const message = JSON.stringify(data);
        this.addLog('【发送完整消息】' + message);
        this.ws.send(message);
      } else {
        this.addLog('WebSocket 未连接，无法发送消息');
      }
    },

    // 开始录制视频
    startRecording() {
      if (this.isRecording) {
        this.addLog('已经在录制中');
        return;
      }

      try {
        this.addLog('【开始录制】准备录制视频...');
        
        // 创建保存目录
        const saveDir = this.saveDirectory;
        const timestamp = this.getTimestamp();
        
        // 录制本地视频（从 localStream）
        if (this.localStream) {
          this.addLog('【开始录制】录制本地视频');
          this.localRecorder = new MediaRecorder(this.localStream, {
            mimeType: 'video/webm;codecs=vp9'
          });
          
          this.localRecorder.ondataavailable = (event) => {
            if (event.data && event.data.size > 0) {
              this.recordedChunks.push({ type: 'local', data: event.data });
            }
          };
          
          this.localRecorder.onstop = () => {
            this.addLog('【停止录制】本地视频录制停止');
            const blob = new Blob(this.recordedChunks.filter(r => r.type === 'local').map(r => r.data), { type: 'video/webm' });
            this.downloadFile(blob, `local_video_${timestamp}.webm`, saveDir);
            this.recordedChunks = this.recordedChunks.filter(r => r.type !== 'local');
          };
          
          this.localRecorder.start(1000);
          this.addLog('【开始录制】本地视频录制开始');
        }
        
        // 录制远程视频（从 remoteStream）
        this.startRemoteRecording();
        
        this.isRecording = true;
        this.addLog('【开始录制】录制状态: true');
      } catch (err) {
        this.addLog('【开始录制】错误: ' + err.message);
        this.error = '录制失败: ' + err.message;
      }
    },

    // 开始录制远程视频
    startRemoteRecording() {
      if (this.remoteStream && !this.remoteRecorder) {
        try {
          this.addLog('【开始录制】录制远程视频');
          this.remoteRecorder = new MediaRecorder(this.remoteStream, {
            mimeType: 'video/webm;codecs=vp9'
          });
          
          let remoteChunks = [];
          
          this.remoteRecorder.ondataavailable = (event) => {
            if (event.data && event.data.size > 0) {
              remoteChunks.push(event.data);
            }
          };
          
          this.remoteRecorder.onstop = () => {
            this.addLog('【停止录制】远程视频录制停止');
            const blob = new Blob(remoteChunks, { type: 'video/webm' });
            const saveDir = this.saveDirectory;
            const timestamp = this.getTimestamp();
            this.downloadFile(blob, `remote_video_${timestamp}.webm`, saveDir);
          };
          
          this.remoteRecorder.start(1000);
          this.addLog('【开始录制】远程视频录制开始');
        } catch (err) {
          this.addLog('【开始录制】远程视频录制错误: ' + err.message);
        }
      }
    },

    // 停止录制
    stopRecording() {
      if (!this.isRecording) {
        return;
      }

      this.addLog('【停止录制】准备停止录制...');
      
      if (this.localRecorder && this.localRecorder.state !== 'inactive') {
        this.addLog('【停止录制】停止本地视频录制');
        this.localRecorder.stop();
      }
      this.localRecorder = null;
      
      if (this.remoteRecorder && this.remoteRecorder.state !== 'inactive') {
        this.addLog('【停止录制】停止远程视频录制');
        this.remoteRecorder.stop();
      }
      this.remoteRecorder = null;
      
      this.isRecording = false;
      this.addLog('【停止录制】录制状态: false');
    },

    // 获取时间戳
    getTimestamp() {
      const now = new Date();
      return now.toISOString().replace(/[:.]/g, '-').slice(0, 19);
    },

    // 下载文件（由于浏览器限制，使用 download 方式）
    downloadFile(blob, filename, directory) {
      try {
        this.addLog(`【保存视频】准备保存 ${filename} 到 ${directory}`);
        
        // 由于浏览器安全限制，无法直接写入文件系统
        // 使用 download 方式提示用户保存
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        a.style.display = 'none';
        document.body.appendChild(a);
        a.click();
        
        // 清理
        setTimeout(() => {
          document.body.removeChild(a);
          URL.revokeObjectURL(url);
        }, 100);
        
        this.addLog(`【保存视频】已触发下载: ${filename}`);
        this.addLog(`【保存视频】提示: 请在浏览器下载设置中修改保存路径为: ${directory}`);
        this.addLog(`【保存视频】或者在浏览器下载记录中移动文件到: ${directory}`);
      } catch (err) {
        this.addLog(`【保存视频】保存失败: ${err.message}`);
        this.error = '保存视频失败: ' + err.message;
      }
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: Arial, sans-serif;
  background: #1a1a2e;
  color: white;
  min-height: 100vh;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  text-align: center;
  margin-bottom: 20px;
  color: #00d9ff;
}

.video-container {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-bottom: 20px;
}

.video-wrapper {
  position: relative;
  width: 400px;
  height: 300px;
  background: #16213e;
  border-radius: 10px;
  overflow: hidden;
}

.video-wrapper video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-wrapper .label {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background: rgba(0, 0, 0, 0.5);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.recording-indicator {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #ff4444;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.status {
  text-align: center;
  margin-bottom: 10px;
}

.status p {
  margin: 5px 0;
}

.status span {
  font-weight: bold;
}

.status-connected { color: #00ff88; }
.status-calling { color: #00d9ff; }

.error {
  text-align: center;
  margin-bottom: 10px;
  color: #ff4444;
  background: rgba(255, 68, 68, 0.1);
  padding: 10px;
  border-radius: 8px;
}

.logs {
  background: #16213e;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 20px;
  max-height: 300px;
  overflow-y: auto;
}

.logs ul {
  list-style: none;
}

.logs li {
  font-size: 11px;
  padding: 3px 0;
  color: #aaa;
}

.controls {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.btn {
  padding: 12px 30px;
  font-size: 16px;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-green { background: #00d9ff; color: #1a1a2e; }
.btn-red { background: #ff4444; color: white; }
</style>
