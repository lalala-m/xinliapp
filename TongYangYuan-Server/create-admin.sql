-- 童养园系统 - 创建管理员账号
-- 数据库：mental_health_db

USE mental_health_db;

-- 创建管理员账号
-- 密码：admin123 (BCrypt加密后的值)
INSERT INTO users (id, phone, password, user_type, nickname, status, created_at, updated_at) 
VALUES (
    999, 
    'admin', 
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- admin123
    'ADMIN', 
    '系统管理员', 
    'ACTIVE',
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE 
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    user_type = 'ADMIN',
    nickname = '系统管理员',
    status = 'ACTIVE';

-- 创建管理员操作日志表
CREATE TABLE IF NOT EXISTS admin_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_user_id BIGINT NOT NULL COMMENT '管理员用户ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(50) COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    details TEXT COMMENT '操作详情',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_admin_user (admin_user_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (admin_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';

-- 为users表添加审核相关字段（如果不存在）
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS is_approved BOOLEAN DEFAULT TRUE COMMENT '是否已审核',
ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP NULL COMMENT '审核时间',
ADD COLUMN IF NOT EXISTS approved_by BIGINT NULL COMMENT '审核人ID';

-- 为consultants表添加审核状态字段（如果不存在）
ALTER TABLE consultants
ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20) DEFAULT 'APPROVED' COMMENT '审核状态: PENDING, APPROVED, REJECTED',
ADD COLUMN IF NOT EXISTS approval_note TEXT COMMENT '审核备注';

-- 输出管理员账号信息
SELECT '管理员账号创建成功！' AS message;
SELECT '账号: admin' AS account;
SELECT '密码: admin123' AS password;
SELECT '用户类型: ADMIN' AS user_type;
