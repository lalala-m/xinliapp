// 测评列表逻辑
const Assessment = {
    init() {
        this.loadTests();
    },

    async loadTests() {
        const listEl = document.getElementById('testList');
        try {
            // 这里调用获取学习包的接口，但筛选出包含测评的（或者后端有专门的测评列表接口）
            // 假设后端 LearningPackage 包含 isAssessment 或者 category='assessment'
            // 暂时复用 learning/packages 接口，实际应根据业务调整
            const packages = await API.get('/api/learning/packages/category/测评'); // 假设有这个分类
            
            if (!packages || packages.length === 0) {
                // 如果没有专门的分类，列出所有包供测试
                const all = await API.get('/api/learning/packages');
                this.renderList(all);
            } else {
                this.renderList(packages);
            }
        } catch (error) {
            console.error('加载测评失败', error);
            listEl.innerHTML = '<div class="error">加载失败</div>';
        }
    },

    renderList(items) {
        const listEl = document.getElementById('testList');
        if (!items.length) {
            listEl.innerHTML = '<div class="empty">暂无测评</div>';
            return;
        }

        listEl.innerHTML = items.map(item => `
            <div class="assessment-card">
                <div class="assessment-info">
                    <h3>${item.title}</h3>
                    <p>${item.description || '暂无描述'}</p>
                    <p style="margin-top: 5px; color: #999; font-size: 12px;">题目数: ${item.videoCount || 10} 题</p>
                </div>
                <a href="test.html?packageId=${item.id}" class="btn-start">开始测评</a>
            </div>
        `).join('');
    }
};

document.addEventListener('DOMContentLoaded', () => Assessment.init());