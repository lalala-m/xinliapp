// 童康源咨询师端 - 认证管理

const Auth = {
    // 登录
    async login(phone, password, remember = true) {
        // 验证输入
        if (!phone || phone.length !== 11) {
            Utils.showToast('请输入正确的手机号', 'error');
            return false;
        }

        if (!password) {
            Utils.showToast('请输入密码', 'error');
            return false;
        }

        try {
            // 调用登录 API
            const loginResult = await API.post('/auth/login', {
                phone: phone,
                password: password
            });

            if (!loginResult) return false;

            // 检查用户类型
            if (loginResult.userType !== 'CONSULTANT') {
                Utils.showToast('非咨询师账号，无法登录', 'error');
                return false;
            }

            // 获取咨询师详细信息
            // 此时 Storage 中还没有 Token，需要手动传入 headers
            const consultant = await API.get(`/consultants/user/${loginResult.userId}`, {
                headers: {
                    'Authorization': `Bearer ${loginResult.token}`
                }
            });
            
            if (!consultant) {
                Utils.showToast('获取咨询师信息失败', 'error');
                return false;
            }

            // 合并信息
            const loginData = {
                ...consultant,
                token: loginResult.token,
                loginTime: Date.now(),
                remember: remember
            };

            ConsultantStorage.setCurrentConsultant(loginData);
            Utils.showToast('登录成功', 'success');

            // 使用replace避免返回到登录页
            setTimeout(() => {
                window.location.replace('dashboard.html');
            }, 500);

            return true;

        } catch (error) {
            console.error('Login error:', error);
            // Utils.showToast已经在API.js中处理了
            return false;
        }
    },

    // 退出登录
    logout() {
        if (Utils.confirm('确定要退出登录吗？')) {
            ConsultantStorage.clearCurrentConsultant();
            sessionStorage.clear(); // 清除会话存储
            Utils.showToast('已退出登录', 'success');
            setTimeout(() => {
                window.location.replace('index.html');
            }, 500);
        }
    },

    // 检查登录状态
    checkAuth() {
        const consultant = ConsultantStorage.getCurrentConsultant();

        if (!consultant) {
            // 未登录，跳转到登录页（只在非登录页执行）
            const isLoginPage = window.location.pathname.endsWith('index.html') ||
                               window.location.pathname === '/' ||
                               window.location.pathname.endsWith('/');

            if (!isLoginPage && !sessionStorage.getItem('redirecting')) {
                sessionStorage.setItem('redirecting', 'true');
                setTimeout(() => {
                    sessionStorage.removeItem('redirecting');
                    window.location.replace('index.html');
                }, 100);
            }
            return null;
        }

        return consultant;
    },

    // 获取当前咨询师
    getCurrentConsultant() {
        return ConsultantStorage.getCurrentConsultant();
    }
};

// 登录页面逻辑
document.addEventListener('DOMContentLoaded', () => {
    // 只在登录页面执行自动跳转检查
    const isLoginPage = window.location.pathname.endsWith('index.html') ||
                        window.location.pathname === '/' ||
                        window.location.pathname.endsWith('/');

    if (isLoginPage && ConsultantStorage.isLoggedIn()) {
        // 防止重复跳转
        if (!sessionStorage.getItem('redirecting')) {
            sessionStorage.setItem('redirecting', 'true');
            setTimeout(() => {
                sessionStorage.removeItem('redirecting');
                window.location.replace('dashboard.html');
            }, 100);
        }
        return;
    }

    const loginForm = document.getElementById('loginForm');
    if (!loginForm) return;

    // 处理登录表单提交
    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const phone = document.getElementById('phone').value.trim();
        const password = document.getElementById('password').value;
        const remember = document.getElementById('rememberMe').checked;

        Auth.login(phone, password, remember);
    });

    // 手机号输入限制
    const phoneInput = document.getElementById('phone');
    if (phoneInput) {
        phoneInput.addEventListener('input', (e) => {
            e.target.value = e.target.value.replace(/\D/g, '');
        });
    }

    // 快速填充测试账号
    const testAccounts = document.querySelector('.test-accounts');
    if (testAccounts) {
        testAccounts.addEventListener('click', () => {
            document.getElementById('phone').value = '13800000001';
            document.getElementById('password').value = '123456';
        });
        testAccounts.style.cursor = 'pointer';
    }
});
