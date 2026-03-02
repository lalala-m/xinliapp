// 童养园咨询师端 - 模拟数据
// ⚠️ 警告：此文件仅用于开发和测试环境
// 生产环境应该从服务器API获取真实数据

// 开发模式标志 - 设置为false以使用真实API
const USE_MOCK_DATA = false;

const MockData = {
    // 咨询师数据
    consultants: [
        {
            id: 'consultant_001',
            name: '张医生',
            phone: '13800000001',
            password: '123456',
            title: '儿童心理咨询师',
            specialty: '儿童焦虑、学习障碍',
            identityTier: 'GOLD',
            rating: 4.8,
            servedCount: 1200,
            intro: '拥有10年儿童心理咨询经验，擅长处理儿童焦虑、学习障碍等问题。',
            avatarColor: '#6FA6F8'
        },
        {
            id: 'consultant_002',
            name: '李医生',
            phone: '13800000002',
            password: '123456',
            title: '青少年心理咨询师',
            specialty: '青春期问题、情绪管理',
            identityTier: 'SILVER',
            rating: 4.6,
            servedCount: 800,
            intro: '专注青少年心理健康，帮助孩子度过青春期的各种挑战。',
            avatarColor: '#FF6B6B'
        }
    ],

    // 初始化预约数据
    initAppointments() {
        const appointments = [
            {
                id: 'apt_001',
                consultantId: 'consultant_001',
                consultantName: '张医生',
                clientName: '王女士',
                childName: '小明',
                childAge: 8,
                date: Utils.formatDate(new Date()),
                timeSlot: '14:00-15:00',
                description: '孩子最近学习压力大，出现焦虑情绪',
                status: CONFIG.APPOINTMENT_STATUS.PENDING,
                createdAt: Date.now() - 3600000,
                clientPhone: '13900000001'
            },
            {
                id: 'apt_002',
                consultantId: 'consultant_001',
                consultantName: '张医生',
                clientName: '李女士',
                childName: '小红',
                childAge: 10,
                date: Utils.formatDate(new Date()),
                timeSlot: '15:00-16:00',
                description: '孩子不愿意与同学交流，性格内向',
                status: CONFIG.APPOINTMENT_STATUS.ACCEPTED,
                createdAt: Date.now() - 7200000,
                clientPhone: '13900000002'
            },
            {
                id: 'apt_003',
                consultantId: 'consultant_001',
                consultantName: '张医生',
                clientName: '张先生',
                childName: '小刚',
                childAge: 7,
                date: Utils.formatDate(new Date(Date.now() - 86400000)),
                timeSlot: '10:00-11:00',
                description: '孩子注意力不集中，上课容易走神',
                status: CONFIG.APPOINTMENT_STATUS.COMPLETED,
                createdAt: Date.now() - 172800000,
                clientPhone: '13900000003'
            }
        ];

        AppointmentStorage.setAll(appointments);
        return appointments;
    },

    // 初始化消息数据
    initMessages() {
        const messages = {
            'apt_002': [
                {
                    id: 'msg_001',
                    type: CONFIG.MESSAGE_TYPE.SYSTEM,
                    content: '咨询已开始',
                    timestamp: Date.now() - 3600000,
                    fromConsultant: false
                },
                {
                    id: 'msg_002',
                    type: CONFIG.MESSAGE_TYPE.TEXT,
                    content: '您好，张医生，我是小红的妈妈',
                    timestamp: Date.now() - 3500000,
                    fromConsultant: false
                },
                {
                    id: 'msg_003',
                    type: CONFIG.MESSAGE_TYPE.TEXT,
                    content: '您好，李女士。我看到您的预约了，请详细说说小红的情况。',
                    timestamp: Date.now() - 3400000,
                    fromConsultant: true
                },
                {
                    id: 'msg_004',
                    type: CONFIG.MESSAGE_TYPE.TEXT,
                    content: '小红今年10岁，最近在学校不太愿意和同学交流，回家也不太说话',
                    timestamp: Date.now() - 3300000,
                    fromConsultant: false
                },
                {
                    id: 'msg_005',
                    type: CONFIG.MESSAGE_TYPE.TEXT,
                    content: '我理解您的担心。这种情况持续多久了？',
                    timestamp: Date.now() - 3200000,
                    fromConsultant: true
                }
            ],
            'apt_003': [
                {
                    id: 'msg_006',
                    type: CONFIG.MESSAGE_TYPE.SYSTEM,
                    content: '咨询已开始',
                    timestamp: Date.now() - 86400000,
                    fromConsultant: false
                },
                {
                    id: 'msg_007',
                    type: CONFIG.MESSAGE_TYPE.TEXT,
                    content: '张医生您好，我是小刚的爸爸',
                    timestamp: Date.now() - 86300000,
                    fromConsultant: false
                },
                {
                    id: 'msg_008',
                    type: CONFIG.MESSAGE_TYPE.TEXT,
                    content: '您好，张先生。请说说小刚的情况。',
                    timestamp: Date.now() - 86200000,
                    fromConsultant: true
                },
                {
                    id: 'msg_009',
                    type: CONFIG.MESSAGE_TYPE.SYSTEM,
                    content: '咨询已结束',
                    timestamp: Date.now() - 82800000,
                    fromConsultant: false
                }
            ]
        };

        MessageStorage.setAll(messages);
        return messages;
    },

    // 验证登录
    validateLogin(phone, password) {
        const consultant = this.consultants.find(
            c => c.phone === phone && c.password === password
        );
        return consultant || null;
    },

    // 初始化所有数据
    initAll() {
        // 检查是否已经初始化过
        const initialized = Storage.get('tyy_initialized', false);

        if (!initialized) {
            // 首次初始化
            this.initAppointments();
            this.initMessages();
            Storage.set('tyy_initialized', true);
        }
    },

    // 生成模拟消息（用于测试）
    generateMockMessage(fromConsultant = false) {
        const clientMessages = [
            '孩子最近情绪不太稳定',
            '谢谢医生的建议',
            '我会按照您说的方法试试',
            '孩子今天表现好多了',
            '还有什么需要注意的吗？'
        ];

        const consultantMessages = [
            '我理解您的担心，让我们一起来分析一下',
            '这是很正常的反应，不用太担心',
            '建议您可以尝试这样做...',
            '保持耐心很重要',
            '如果有任何问题，随时联系我'
        ];

        const messages = fromConsultant ? consultantMessages : clientMessages;
        const content = messages[Math.floor(Math.random() * messages.length)];

        return {
            id: Utils.generateId('msg_'),
            type: CONFIG.MESSAGE_TYPE.TEXT,
            content: content,
            timestamp: Date.now(),
            fromConsultant: fromConsultant
        };
    },

    // 添加模拟预约
    addMockAppointment(consultantId) {
        const names = ['王女士', '李女士', '张先生', '刘女士', '陈先生'];
        const childNames = ['小明', '小红', '小刚', '小丽', '小华'];
        const descriptions = [
            '孩子学习压力大',
            '孩子不愿意交流',
            '孩子注意力不集中',
            '孩子情绪波动大',
            '孩子睡眠质量差'
        ];

        const appointment = {
            id: Utils.generateId('apt_'),
            consultantId: consultantId,
            consultantName: '张医生',
            clientName: names[Math.floor(Math.random() * names.length)],
            childName: childNames[Math.floor(Math.random() * childNames.length)],
            childAge: Math.floor(Math.random() * 5) + 6,
            date: Utils.formatDate(new Date()),
            timeSlot: `${Math.floor(Math.random() * 8) + 9}:00-${Math.floor(Math.random() * 8) + 10}:00`,
            description: descriptions[Math.floor(Math.random() * descriptions.length)],
            status: CONFIG.APPOINTMENT_STATUS.PENDING,
            createdAt: Date.now(),
            clientPhone: `139${Math.floor(Math.random() * 100000000).toString().padStart(8, '0')}`
        };

        AppointmentStorage.add(appointment);
        return appointment;
    }
};

// 页面加载时初始化数据（只执行一次）
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        MockData.initAll();
    });
} else {
    // DOM已经加载完成
    MockData.initAll();
}
