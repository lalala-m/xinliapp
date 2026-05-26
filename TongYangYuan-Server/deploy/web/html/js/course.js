// 课程详情与播放逻辑
const Course = {
    packageId: null,
    videos: [],
    currentVideo: null,
    progress: {},

    init() {
        const params = new URLSearchParams(location.search);
        this.packageId = params.get('id');
        if (!this.packageId) {
            alert('参数错误');
            history.back();
            return;
        }

        this.loadPackageDetail();
        this.loadProgress();
        this.bindVideoEvents();
    },

    async loadPackageDetail() {
        try {
            const data = await API.get(`/api/learning/packages/${this.packageId}`);
            // 后端返回结构: { package: {...}, videos: [...] }
            const pkg = data.package || data;
            document.getElementById('packageTitle').textContent = pkg.title;
            document.getElementById('packageDesc').textContent = pkg.description;
            
            this.videos = data.videos || [];
            this.renderVideoList();
        } catch (error) {
            console.error('加载详情失败', error);
        }
    },

    async loadProgress() {
        try {
            // 获取用户在该课程的进度
            const data = await API.get(`/api/learning/packages/${this.packageId}/progress`);
            this.progress = data || {}; // { completedVideoIds: [], lastVideoId: ... }
            this.updateUIProgress();
        } catch (error) {
            console.error('加载进度失败', error);
        }
    },

    renderVideoList() {
        const listEl = document.getElementById('videoList');
        listEl.innerHTML = this.videos.map((video, index) => {
            const isCompleted = this.isVideoCompleted(video.id);
            return `
                <div class="video-item ${isCompleted ? 'completed' : ''}" 
                     onclick="Course.playVideo(${index})"
                     id="video-item-${video.id}">
                    <div class="video-status">${isCompleted ? '✓' : (index + 1)}</div>
                    <div class="video-info">
                        <div class="video-title">${video.title}</div>
                        <div class="video-duration">时长: ${Math.floor(video.duration / 60)}分${video.duration % 60}秒</div>
                    </div>
                </div>
            `;
        }).join('');
    },

    isVideoCompleted(videoId) {
        // 根据 this.progress 判断
        // 假设 progress 结构: { userProgressList: [ { videoId: 1, isCompleted: true } ] }
        if (!this.progress.userProgressList) return false;
        const p = this.progress.userProgressList.find(item => item.videoId === videoId);
        return p && p.isCompleted;
    },

    playVideo(index) {
        const video = this.videos[index];
        if (!video) return;

        this.currentVideo = video;
        const player = document.getElementById('videoPlayer');
        player.src = video.videoUrl;
        player.play();

        // 高亮当前播放
        document.querySelectorAll('.video-item').forEach(el => el.classList.remove('active'));
        document.getElementById(`video-item-${video.id}`).classList.add('active');
    },

    bindVideoEvents() {
        const player = document.getElementById('videoPlayer');
        
        // 监听播放进度，定期上报
        player.addEventListener('timeupdate', () => {
            if (player.currentTime > 0 && !player.paused) {
                // 每10秒或暂停时上报一次，这里简化为暂停或结束时上报
            }
        });

        // 播放结束
        player.addEventListener('ended', () => {
            this.reportProgress(player.duration, true);
            // 检查是否有验证题
            if (this.currentVideo.verificationQuestion) {
                this.showQuiz(this.currentVideo.verificationQuestion);
            }
        });

        // 暂停时保存进度
        player.addEventListener('pause', () => {
            this.reportProgress(player.currentTime, false);
        });
    },

    async reportProgress(position, isCompleted) {
        if (!this.currentVideo) return;
        try {
            await API.post(`/api/learning/videos/${this.currentVideo.id}/progress`, {
                packageId: this.packageId,
                watchDuration: Math.floor(position),
                lastPosition: Math.floor(position)
            });
            
            if (isCompleted) {
                this.markVideoCompleted(this.currentVideo.id);
            }
        } catch (error) {
            console.error('上报进度失败', error);
        }
    },

    markVideoCompleted(videoId) {
        const el = document.getElementById(`video-item-${videoId}`);
        if (el) el.classList.add('completed');
        // 刷新总进度
        this.loadProgress();
    },

    updateUIProgress() {
        // 计算百分比
        if (!this.progress.userProgressList) return;
        const total = this.videos.length;
        const completed = this.progress.userProgressList.filter(p => p.isCompleted).length;
        const percent = total === 0 ? 0 : Math.round((completed / total) * 100);
        
        document.getElementById('totalProgress').style.width = percent + '%';
        document.getElementById('progressText').textContent = percent + '%';
    },

    showQuiz(questionJson) {
        try {
            const question = JSON.parse(questionJson); // { question: "", options: [], answer: "" }
            document.getElementById('quizQuestion').textContent = question.question;
            
            const optionsHtml = question.options.map((opt, idx) => `
                <button class="btn btn-outline" onclick="Course.checkAnswer('${opt}', '${question.answer}')">
                    ${opt}
                </button>
            `).join('');
            
            document.getElementById('quizOptions').innerHTML = optionsHtml;
            document.getElementById('quizModal').style.display = 'flex';
        } catch (e) {
            console.error('解析题目失败', e);
        }
    },

    async checkAnswer(selected, correct) {
        if (selected === correct) {
            alert('回答正确！');
            document.getElementById('quizModal').style.display = 'none';
            // 上报验证通过
            await API.post(`/api/learning/videos/${this.currentVideo.id}/verify`, {
                answer: selected
            });
        } else {
            alert('回答错误，请重试');
        }
    }
};

document.addEventListener('DOMContentLoaded', () => Course.init());