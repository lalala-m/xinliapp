-- 同阳缘心理健康咨询系统数据库设计
-- 数据库名称: mental_health_db
-- 字符集: utf8mb4

CREATE DATABASE IF NOT EXISTS mental_health_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mental_health_db;

-- 1. 用户表（统一用户表，包含咨询师和家长）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    phone VARCHAR(20) UNIQUE NOT NULL COMMENT '手机号',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    user_type ENUM('CONSULTANT', 'PARENT') NOT NULL COMMENT '用户类型：咨询师/家长',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE' COMMENT '账户状态',
    INDEX idx_phone (phone),
    INDEX idx_user_type (user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 咨询师信息表
CREATE TABLE IF NOT EXISTS consultants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '咨询师ID',
    user_id BIGINT UNIQUE NOT NULL COMMENT '关联用户ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    title VARCHAR(100) COMMENT '职称',
    specialty TEXT COMMENT '专长领域',
    identity_tier ENUM('BRONZE', 'SILVER', 'GOLD', 'PLATINUM') DEFAULT 'BRONZE' COMMENT '认证等级',
    rating DECIMAL(3,2) DEFAULT 5.00 COMMENT '评分（0-5）',
    served_count INT DEFAULT 0 COMMENT '服务人数',
    intro TEXT COMMENT '个人介绍',
    avatar_color VARCHAR(20) DEFAULT '#6FA6F8' COMMENT '头像颜色',
    is_available BOOLEAN DEFAULT TRUE COMMENT '是否可预约',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_rating (rating),
    INDEX idx_identity_tier (identity_tier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询师信息表';

-- 3. 儿童档案表
CREATE TABLE IF NOT EXISTS children (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '儿童ID',
    parent_user_id BIGINT NOT NULL COMMENT '家长用户ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender ENUM('MALE', 'FEMALE', 'OTHER') COMMENT '性别',
    birth_date DATE COMMENT '出生日期',
    ethnicity VARCHAR(50) COMMENT '民族',
    birthplace VARCHAR(200) COMMENT '籍贯',
    birth_order INT COMMENT '家庭排行',
    birth_location VARCHAR(200) COMMENT '出生地',
    language_environment VARCHAR(200) COMMENT '语言环境',
    school VARCHAR(200) COMMENT '学校',
    home_address VARCHAR(500) COMMENT '家庭住址',
    interests TEXT COMMENT '兴趣爱好',
    activities TEXT COMMENT '活动',
    health_status TEXT COMMENT '身体状况',
    medical_history TEXT COMMENT '病史',
    parent_contact VARCHAR(500) COMMENT '父母联系方式（JSON格式）',
    guardian_contact VARCHAR(500) COMMENT '监护人联系方式（JSON格式）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (parent_user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_parent_user_id (parent_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='儿童档案表';

-- 4. 预约记录表
CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
    appointment_no VARCHAR(50) UNIQUE NOT NULL COMMENT '预约编号',
    consultant_id BIGINT NOT NULL COMMENT '咨询师ID',
    parent_user_id BIGINT NOT NULL COMMENT '家长用户ID',
    child_id BIGINT COMMENT '儿童ID',
    child_name VARCHAR(50) COMMENT '儿童姓名',
    child_age INT COMMENT '儿童年龄',
    appointment_date DATE NOT NULL COMMENT '预约日期',
    time_slot VARCHAR(50) NOT NULL COMMENT '时间段',
    description TEXT COMMENT '问题描述',
    status ENUM('PENDING', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '预约状态',
    is_pinned BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    is_chatted BOOLEAN DEFAULT FALSE COMMENT '是否已聊天',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (consultant_id) REFERENCES consultants(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE SET NULL,
    INDEX idx_consultant_id (consultant_id),
    INDEX idx_parent_user_id (parent_user_id),
    INDEX idx_appointment_date (appointment_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约记录表';

-- 5. 聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    appointment_id BIGINT NOT NULL COMMENT '关联预约ID',
    sender_user_id BIGINT NOT NULL COMMENT '发送者用户ID',
    receiver_user_id BIGINT NOT NULL COMMENT '接收者用户ID',
    message_type ENUM('TEXT', 'IMAGE', 'VIDEO', 'AUDIO', 'SYSTEM') NOT NULL COMMENT '消息类型',
    content TEXT COMMENT '消息内容',
    media_url VARCHAR(500) COMMENT '媒体文件URL',
    is_from_consultant BOOLEAN NOT NULL COMMENT '是否来自咨询师',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_appointment_id (appointment_id),
    INDEX idx_sender_user_id (sender_user_id),
    INDEX idx_receiver_user_id (receiver_user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 6. 视频通话记录表
CREATE TABLE IF NOT EXISTS video_calls (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通话ID',
    appointment_id BIGINT NOT NULL COMMENT '关联预约ID',
    caller_user_id BIGINT NOT NULL COMMENT '呼叫者用户ID',
    callee_user_id BIGINT NOT NULL COMMENT '被叫者用户ID',
    call_status ENUM('CALLING', 'CONNECTED', 'ENDED', 'REJECTED', 'MISSED') NOT NULL COMMENT '通话状态',
    start_time TIMESTAMP NULL COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    duration INT DEFAULT 0 COMMENT '通话时长（秒）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    FOREIGN KEY (caller_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (callee_user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_appointment_id (appointment_id),
    INDEX idx_caller_user_id (caller_user_id),
    INDEX idx_callee_user_id (callee_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频通话记录表';

-- 7. 会员充值记录表
CREATE TABLE IF NOT EXISTS recharge_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '充值ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '充值金额',
    payment_method VARCHAR(50) COMMENT '支付方式',
    transaction_no VARCHAR(100) UNIQUE COMMENT '交易流水号',
    status ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING' COMMENT '支付状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    paid_at TIMESTAMP NULL COMMENT '支付时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_no (transaction_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

-- 8. 用户设置表
CREATE TABLE IF NOT EXISTS user_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设置ID',
    user_id BIGINT UNIQUE NOT NULL COMMENT '用户ID',
    is_premium BOOLEAN DEFAULT FALSE COMMENT '是否付费会员',
    has_child_profile BOOLEAN DEFAULT FALSE COMMENT '是否有儿童档案',
    notification_enabled BOOLEAN DEFAULT TRUE COMMENT '是否开启通知',
    theme VARCHAR(20) DEFAULT 'LIGHT' COMMENT '主题设置',
    language VARCHAR(10) DEFAULT 'zh_CN' COMMENT '语言设置',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设置表';

-- 9. 在线状态表（用于实时通信）
CREATE TABLE IF NOT EXISTS user_online_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '状态ID',
    user_id BIGINT UNIQUE NOT NULL COMMENT '用户ID',
    is_online BOOLEAN DEFAULT FALSE COMMENT '是否在线',
    last_seen_at TIMESTAMP NULL COMMENT '最后在线时间',
    device_type VARCHAR(20) COMMENT '设备类型：ANDROID/WEB',
    socket_id VARCHAR(100) COMMENT 'WebSocket连接ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_online (is_online)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户在线状态表';

-- 插入测试数据

-- 插入测试用户（咨询师）
INSERT INTO users (phone, password, user_type, nickname, status) VALUES
('13800000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHlALq.1eBzQxBfzMlvK', 'CONSULTANT', '张医生', 'ACTIVE'),
('13800000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHlALq.1eBzQxBfzMlvK', 'CONSULTANT', '李医生', 'ACTIVE'),
('13800000003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHlALq.1eBzQxBfzMlvK', 'CONSULTANT', '王医生', 'ACTIVE');

-- 插入测试用户（家长）
INSERT INTO users (phone, password, user_type, nickname, status) VALUES
('13900000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHlALq.1eBzQxBfzMlvK', 'PARENT', '王女士', 'ACTIVE'),
('13900000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHlALq.1eBzQxBfzMlvK', 'PARENT', '李先生', 'ACTIVE');

-- 插入咨询师信息
INSERT INTO consultants (user_id, name, title, specialty, identity_tier, rating, served_count, intro) VALUES
(1, '张医生', '儿童心理咨询师', '儿童焦虑、学习障碍', 'GOLD', 4.8, 1200, '拥有10年儿童心理咨询经验，擅长处理儿童焦虑和学习障碍问题。'),
(2, '李医生', '青少年心理专家', '青春期问题、情绪管理', 'PLATINUM', 4.9, 1500, '资深青少年心理专家，专注于青春期心理健康和情绪管理。'),
(3, '王医生', '家庭治疗师', '亲子关系、家庭矛盾', 'SILVER', 4.7, 800, '家庭治疗专家，帮助改善亲子关系和解决家庭矛盾。');

-- 插入儿童档案
INSERT INTO children (parent_user_id, name, gender, birth_date, school) VALUES
(4, '小明', 'MALE', '2016-05-15', '阳光小学'),
(5, '小红', 'FEMALE', '2015-08-20', '希望小学');

-- 插入预约记录
INSERT INTO appointments (appointment_no, consultant_id, parent_user_id, child_id, child_name, child_age, appointment_date, time_slot, description, status) VALUES
('APT20260104001', 1, 4, 1, '小明', 8, '2026-01-05', '14:00-15:00', '孩子最近学习压力大，出现焦虑情绪', 'PENDING'),
('APT20260104002', 2, 5, 2, '小红', 9, '2026-01-06', '10:00-11:00', '孩子情绪波动较大，需要咨询', 'ACCEPTED');

-- 插入用户设置
INSERT INTO user_settings (user_id, is_premium, has_child_profile) VALUES
(1, FALSE, FALSE),
(2, FALSE, FALSE),
(3, FALSE, FALSE),
(4, TRUE, TRUE),
(5, TRUE, TRUE);
