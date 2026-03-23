-- ============================================
-- TongYangYuan 视频咨询功能数据库初始化脚本
-- 执行前请确保 mental_health_db 数据库已创建
-- ============================================

USE mental_health_db;

-- ============================================
-- 聊天消息表
-- ============================================
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    appointment_id BIGINT COMMENT '预约ID',
    sender_user_id BIGINT NOT NULL COMMENT '发送者用户ID',
    receiver_user_id BIGINT NOT NULL COMMENT '接收者用户ID',
    message_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型: TEXT, IMAGE, VIDEO, AUDIO, SYSTEM',
    content TEXT COMMENT '消息内容',
    media_url VARCHAR(500) COMMENT '媒体文件URL',
    is_from_consultant BOOLEAN DEFAULT FALSE COMMENT '是否来自咨询师',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_appointment (appointment_id),
    INDEX idx_sender (sender_user_id),
    INDEX idx_receiver (receiver_user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- ============================================
-- 用户在线状态表
-- ============================================
CREATE TABLE IF NOT EXISTS user_online_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT UNIQUE NOT NULL COMMENT '用户ID',
    is_online BOOLEAN DEFAULT FALSE COMMENT '是否在线',
    last_seen_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后在线时间',
    device_type VARCHAR(50) COMMENT '设备类型: ANDROID, IOS, WEB',
    socket_id VARCHAR(100) COMMENT 'Socket连接ID',
    INDEX idx_user_id (user_id),
    INDEX idx_is_online (is_online)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户在线状态表';

-- ============================================
-- 咨询记录表（视频通话记录）
-- ============================================
CREATE TABLE IF NOT EXISTS consultation_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    appointment_id BIGINT NOT NULL COMMENT '预约ID',
    record_type VARCHAR(20) DEFAULT 'VIDEO' COMMENT '记录类型: VIDEO, AUDIO, TEXT',
    room_id VARCHAR(100) COMMENT '视频房间ID',
    start_time TIMESTAMP NULL COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    duration_seconds INT DEFAULT 0 COMMENT '通话时长(秒)',
    recording_url VARCHAR(500) COMMENT '录制文件URL',
    quality_rating INT COMMENT '通话质量评分 1-5',
    notes TEXT COMMENT '备注',
    status VARCHAR(20) DEFAULT 'COMPLETED' COMMENT '状态: IN_PROGRESS, COMPLETED, CANCELLED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_appointment (appointment_id),
    INDEX idx_room_id (room_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='咨询记录表';

-- ============================================
-- 视频通话会话表
-- ============================================
CREATE TABLE IF NOT EXISTS video_call_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    appointment_id BIGINT NOT NULL COMMENT '预约ID',
    room_name VARCHAR(100) NOT NULL COMMENT '房间名称(LiveKit Room)',
    caller_id BIGINT NOT NULL COMMENT '主叫用户ID',
    callee_id BIGINT NOT NULL COMMENT '被叫用户ID',
    call_type VARCHAR(20) DEFAULT 'VIDEO' COMMENT '通话类型: VIDEO, AUDIO',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, RINGING, CONNECTED, ENDED, MISSED, REJECTED',
    initiated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间',
    answered_at TIMESTAMP NULL COMMENT '接听时间',
    ended_at TIMESTAMP NULL COMMENT '结束时间',
    end_reason VARCHAR(50) COMMENT '结束原因: NORMAL, TIMEOUT, ERROR, REJECTED',
    livekit_token VARCHAR(500) COMMENT 'LiveKit Token',
    token_expires_at TIMESTAMP NULL COMMENT 'Token过期时间',
    INDEX idx_appointment (appointment_id),
    INDEX idx_room_name (room_name),
    INDEX idx_caller (caller_id),
    INDEX idx_callee (callee_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频通话会话表';

-- ============================================
-- 视频质量日志表
-- ============================================
CREATE TABLE IF NOT EXISTS video_quality_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    user_id BIGINT COMMENT '用户ID',
    video_enabled BOOLEAN DEFAULT TRUE COMMENT '视频是否开启',
    audio_enabled BOOLEAN DEFAULT TRUE COMMENT '音频是否开启',
    video_bitrate INT COMMENT '视频码率(kbps)',
    audio_bitrate INT COMMENT '音频码率(kbps)',
    packet_loss DECIMAL(5,2) COMMENT '丢包率(%)',
    latency_ms INT COMMENT '延迟(ms)',
    jitter_ms INT COMMENT '抖动(ms)',
    resolution VARCHAR(20) COMMENT '分辨率如 1280x720',
    frame_rate INT COMMENT '帧率',
    INDEX idx_session (session_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频质量日志表';

-- ============================================
-- 存储过程：更新通话时长
-- ============================================
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS update_call_duration(IN session_id BIGINT)
BEGIN
    DECLARE start_time_val TIMESTAMP;
    DECLARE end_time_val TIMESTAMP;
    DECLARE duration_val INT;
    
    SELECT start_time, ended_at INTO start_time_val, end_time_val
    FROM video_call_sessions
    WHERE id = session_id;
    
    IF start_time_val IS NOT NULL AND end_time_val IS NOT NULL THEN
        SET duration_val = TIMESTAMPDIFF(SECOND, start_time_val, end_time_val);
        
        UPDATE video_call_sessions
        SET duration_seconds = duration_val
        WHERE id = session_id;
        
        -- 同时更新咨询记录
        UPDATE consultation_records
        SET duration_seconds = duration_val,
            end_time = end_time_val
        WHERE appointment_id = (SELECT appointment_id FROM video_call_sessions WHERE id = session_id);
    END IF;
END //
DELIMITER ;

-- ============================================
-- 初始化系统管理员的在线状态
-- ============================================
INSERT INTO user_online_status (user_id, is_online, device_type)
VALUES (1, FALSE, 'SYSTEM')
ON DUPLICATE KEY UPDATE last_seen_at = CURRENT_TIMESTAMP;

-- ============================================
-- 创建索引优化查询
-- ============================================
-- 为聊天消息创建复合索引优化对话查询
CREATE INDEX IF NOT EXISTS idx_chat_conversation 
ON chat_messages (sender_user_id, receiver_user_id, created_at);

-- 为通话记录创建复合索引
CREATE INDEX IF NOT EXISTS idx_call_user 
ON video_call_sessions (caller_id, initiated_at);

CREATE INDEX IF NOT EXISTS idx_call_consultant 
ON video_call_sessions (callee_id, initiated_at);

-- ============================================
-- 验证脚本
-- ============================================
-- SELECT '验证表创建成功' AS status;
-- SELECT COUNT(*) AS table_count FROM information_schema.tables 
-- WHERE table_schema = 'mental_health_db' 
-- AND table_name IN ('chat_messages', 'user_online_status', 'consultation_records', 'video_call_sessions', 'video_quality_logs');
