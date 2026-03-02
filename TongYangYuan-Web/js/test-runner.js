// 测评运行逻辑
const TestRunner = {
    packageId: null,
    questions: [],
    currentIndex: 0,
    answers: {}, // { questionId: optionIndex }
    startTime: 0,

    init() {
        const params = new URLSearchParams(location.search);
        this.packageId = params.get('packageId');
        if (!this.packageId) {
            alert('参数错误');
            history.back();
            return;
        }

        this.startTime = Date.now();
        this.loadQuestions();
    },

    async loadQuestions() {
        try {
            const data = await API.get(`/api/tests/packages/${this.packageId}/questions`);
            this.questions = data;
            document.getElementById('totalNum').textContent = this.questions.length;
            this.renderQuestion();
        } catch (error) {
            console.error('加载题目失败', error);
            document.getElementById('qText').textContent = '题目加载失败，请重试';
        }
    },

    renderQuestion() {
        if (this.questions.length === 0) return;
        
        const q = this.questions[this.currentIndex];
        document.getElementById('currentNum').textContent = this.currentIndex + 1;
        document.getElementById('qText').textContent = q.questionText;

        // 解析选项
        let options = [];
        try {
            options = JSON.parse(q.options); // ["A", "B", "C"] or [{label:"A", value:1}]
        } catch (e) {
            console.error('选项解析失败', e);
        }

        const optionsHtml = options.map((opt, idx) => {
            const isSelected = this.answers[q.id] === idx;
            return `
                <div class="option-item ${isSelected ? 'selected' : ''}" 
                     onclick="TestRunner.selectOption(${idx})">
                    ${opt}
                </div>
            `;
        }).join('');
        document.getElementById('qOptions').innerHTML = optionsHtml;

        // 按钮状态
        document.getElementById('btnPrev').style.visibility = this.currentIndex === 0 ? 'hidden' : 'visible';
        document.getElementById('btnNext').textContent = this.currentIndex === this.questions.length - 1 ? '提交' : '下一题';
    },

    selectOption(idx) {
        const q = this.questions[this.currentIndex];
        this.answers[q.id] = idx;
        this.renderQuestion(); // 重新渲染以高亮
    },

    prev() {
        if (this.currentIndex > 0) {
            this.currentIndex--;
            this.renderQuestion();
        }
    },

    next() {
        const q = this.questions[this.currentIndex];
        if (this.answers[q.id] === undefined) {
            alert('请先选择一个选项');
            return;
        }

        if (this.currentIndex < this.questions.length - 1) {
            this.currentIndex++;
            this.renderQuestion();
        } else {
            this.submit();
        }
    },

    async submit() {
        if (!confirm('确定要提交问卷吗？')) return;

        const timeSpent = Math.floor((Date.now() - this.startTime) / 1000);
        const answersJson = JSON.stringify(this.answers);

        try {
            // POST /api/tests/packages/{packageId}/submit
            await API.post(`/api/tests/packages/${this.packageId}/submit`, {
                answers: answersJson,
                timeSpent: timeSpent
            });
            
            alert('提交成功！');
            location.href = 'assessment.html'; // 返回列表或结果页
        } catch (error) {
            console.error('提交失败', error);
            alert('提交失败，请重试');
        }
    }
};

document.addEventListener('DOMContentLoaded', () => TestRunner.init());