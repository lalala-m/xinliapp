// 在线学习逻辑
const Learning = {
    currentCategory: 'all',

    init() {
        this.bindEvents();
        this.loadPackages();
    },

    bindEvents() {
        // 标签切换
        document.querySelectorAll('.tab-item').forEach(item => {
            item.addEventListener('click', (e) => {
                document.querySelectorAll('.tab-item').forEach(t => t.classList.remove('active'));
                e.target.classList.add('active');
                this.currentCategory = e.target.dataset.category;
                this.loadPackages();
            });
        });
    },

    async loadPackages() {
        const listEl = document.getElementById('courseList');
        listEl.innerHTML = '<div class="loading">加载中...</div>';

        try {
            let url = '/api/learning/packages';
            if (this.currentCategory !== 'all') {
                url = `/api/learning/packages/category/${encodeURIComponent(this.currentCategory)}`;
            }

            const packages = await API.get(url);
            this.renderPackages(packages);
        } catch (error) {
            console.error('加载课程失败', error);
            listEl.innerHTML = '<div class="error">加载失败，请重试</div>';
        }
    },

    renderPackages(packages) {
        const listEl = document.getElementById('courseList');
        if (!packages || packages.length === 0) {
            listEl.innerHTML = '<div class="empty">暂无课程</div>';
            return;
        }

        listEl.innerHTML = packages.map(pkg => `
            <div class="course-card" onclick="location.href='course.html?id=${pkg.id}'">
                <img src="${pkg.coverImage || 'https://via.placeholder.com/300x180'}" class="course-cover">
                <div class="course-info">
                    <div class="course-title">${pkg.title}</div>
                    <div class="course-desc">${pkg.description || '暂无描述'}</div>
                    <div class="course-meta">
                        <span class="tag">${pkg.category}</span>
                        <span>${pkg.videoCount || 0} 节课 · ${pkg.totalDuration || 0} 分钟</span>
                    </div>
                </div>
            </div>
        `).join('');
    }
};

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => Learning.init());