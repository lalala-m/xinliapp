// 童养园咨询师端 - API 请求封装

const API = {
    // 获取完整的 API URL
    getUrl(endpoint) {
        return `${CONFIG.API_BASE_URL}${endpoint}`;
    },

    // 获取 Auth Token
    getToken() {
        // 优先从 Android 原生接口获取（家长端）
        if (window.Android && window.Android.getToken) {
            const token = window.Android.getToken();
            if (token) return token;
        }
        
        // 其次尝试从 Web Storage 获取（咨询师端）
        const consultant = ConsultantStorage.getCurrentConsultant();
        return consultant ? consultant.token : null;
    },

    // 通用请求方法
    async request(endpoint, options = {}) {
        const url = this.getUrl(endpoint);
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            mode: 'cors', // 显式启用 CORS
            ...options,
            headers
        };

        try {
            const response = await fetch(url, config);

            // 处理 401 未授权
            if (response.status === 401) {
                Utils.showToast('登录已过期，请重新登录', 'error');
                Auth.logout();
                return null;
            }

            const data = await response.json();

            // 统一处理后端 ApiResponse 结构
            // 假设后端返回结构: { code: 200, message: "success", data: ... }
            if (data.code === 200) {
                return data.data;
            } else {
                Utils.showToast(data.message || '请求失败', 'error');
                throw new Error(data.message || '请求失败');
            }
        } catch (error) {
            console.error('API Request Error:', error);

            // 提供更详细的错误信息
            if (error.name === 'TypeError' && error.message.includes('fetch')) {
                Utils.showToast('无法连接到服务器，请确认：\n1. 后端服务是否已启动\n2. API地址是否正确\n3. 网络连接是否正常', 'error');
                console.error('详细错误：无法连接到', url);
                console.error('请检查后端服务是否运行在 http://localhost:8080');
            } else {
                Utils.showToast(error.message || '网络请求出错', 'error');
            }
            throw error;
        }
    },

    // GET 请求
    get(endpoint, options = {}) {
        return this.request(endpoint, {
            method: 'GET',
            ...options
        });
    },

    // POST 请求
    post(endpoint, body, options = {}) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body),
            ...options
        });
    },

    // PUT 请求
    put(endpoint, body, options = {}) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(body),
            ...options
        });
    },

    // DELETE 请求
    delete(endpoint, options = {}) {
        return this.request(endpoint, {
            method: 'DELETE',
            ...options
        });
    }
};
