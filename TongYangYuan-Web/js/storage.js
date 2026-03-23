// 童养园咨询师端 - 本地存储管理

const Storage = {
    // 保存数据
    set(key, value) {
        try {
            const data = JSON.stringify(value);
            localStorage.setItem(key, data);
            return true;
        } catch (error) {
            console.error('Storage set error:', error);
            return false;
        }
    },

    // 获取数据
    get(key, defaultValue = null) {
        try {
            const data = localStorage.getItem(key);
            return data ? JSON.parse(data) : defaultValue;
        } catch (error) {
            console.error('Storage get error:', error);
            return defaultValue;
        }
    },

    // 删除数据
    remove(key) {
        try {
            localStorage.removeItem(key);
            return true;
        } catch (error) {
            console.error('Storage remove error:', error);
            return false;
        }
    },

    // 清空所有数据
    clear() {
        try {
            localStorage.clear();
            return true;
        } catch (error) {
            console.error('Storage clear error:', error);
            return false;
        }
    },

    // 检查键是否存在
    has(key) {
        return localStorage.getItem(key) !== null;
    }
};

// 咨询师数据管理
const ConsultantStorage = {
    // 保存当前登录的咨询师
    setCurrentConsultant(consultant) {
        return Storage.set(CONFIG.STORAGE_KEYS.CONSULTANT, consultant);
    },

    // 获取当前登录的咨询师
    getCurrentConsultant() {
        return Storage.get(CONFIG.STORAGE_KEYS.CONSULTANT);
    },

    // 清除当前咨询师（退出登录）
    clearCurrentConsultant() {
        return Storage.remove(CONFIG.STORAGE_KEYS.CONSULTANT);
    },

    // 检查是否已登录
    isLoggedIn() {
        return Storage.has(CONFIG.STORAGE_KEYS.CONSULTANT);
    }
};

// 预约数据管理
const AppointmentStorage = {
    // 获取所有预约
    getAll() {
        return Storage.get(CONFIG.STORAGE_KEYS.APPOINTMENTS, []);
    },

    // 保存所有预约
    setAll(appointments) {
        return Storage.set(CONFIG.STORAGE_KEYS.APPOINTMENTS, appointments);
    },

    // 根据ID获取预约
    getById(id) {
        const appointments = this.getAll();
        return appointments.find(apt => apt.id === id);
    },

    // 添加预约
    add(appointment) {
        const appointments = this.getAll();
        appointments.push(appointment);
        return this.setAll(appointments);
    },

    // 更新预约
    update(id, updates) {
        const appointments = this.getAll();
        const index = appointments.findIndex(apt => apt.id === id);
        if (index !== -1) {
            appointments[index] = { ...appointments[index], ...updates };
            return this.setAll(appointments);
        }
        return false;
    },

    // 删除预约
    delete(id) {
        const appointments = this.getAll();
        const filtered = appointments.filter(apt => apt.id !== id);
        return this.setAll(filtered);
    },

    // 获取咨询师的预约
    getByConsultant(consultantId) {
        const appointments = this.getAll();
        return appointments.filter(apt => apt.consultantId === consultantId);
    },

    // 获取今日预约
    getTodayAppointments(consultantId) {
        const today = Utils.formatDate(new Date());
        const appointments = this.getByConsultant(consultantId);
        return appointments.filter(apt => apt.date === today);
    },

    // 获取待处理预约
    getPendingAppointments(consultantId) {
        const appointments = this.getByConsultant(consultantId);
        return appointments.filter(apt =>
            apt.status === CONFIG.APPOINTMENT_STATUS.PENDING ||
            apt.status === CONFIG.APPOINTMENT_STATUS.ACCEPTED
        );
    }
};

// 消息数据管理
const MessageStorage = {
    // 获取所有消息
    getAll() {
        return Storage.get(CONFIG.STORAGE_KEYS.MESSAGES, {});
    },

    // 保存所有消息
    setAll(messages) {
        return Storage.set(CONFIG.STORAGE_KEYS.MESSAGES, messages);
    },

    // 获取指定预约的消息
    getByAppointment(appointmentId) {
        const allMessages = this.getAll();
        return allMessages[appointmentId] || [];
    },

    // 添加消息
    addMessage(appointmentId, message) {
        const allMessages = this.getAll();
        if (!allMessages[appointmentId]) {
            allMessages[appointmentId] = [];
        }
        allMessages[appointmentId].push(message);
        return this.setAll(allMessages);
    },

    // 批量添加消息
    addMessages(appointmentId, messages) {
        const allMessages = this.getAll();
        if (!allMessages[appointmentId]) {
            allMessages[appointmentId] = [];
        }
        allMessages[appointmentId].push(...messages);
        return this.setAll(allMessages);
    },

    // 清空指定预约的消息
    clearAppointmentMessages(appointmentId) {
        const allMessages = this.getAll();
        delete allMessages[appointmentId];
        return this.setAll(allMessages);
    },

    // 获取最后一条消息
    getLastMessage(appointmentId) {
        const messages = this.getByAppointment(appointmentId);
        return messages.length > 0 ? messages[messages.length - 1] : null;
    }
};

// 设置数据管理
const SettingsStorage = {
    // 获取设置
    get() {
        return Storage.get(CONFIG.STORAGE_KEYS.SETTINGS, {
            notifications: true,
            sound: true,
            autoAccept: false,
            theme: 'light'
        });
    },

    // 保存设置
    set(settings) {
        return Storage.set(CONFIG.STORAGE_KEYS.SETTINGS, settings);
    },

    // 更新单个设置
    update(key, value) {
        const settings = this.get();
        settings[key] = value;
        return this.set(settings);
    }
};
