-- ============================================
-- 童康源心理健康咨询系统 - 数据库变更脚本
-- 执行日期: 2026-03-23
-- ============================================

-- 1. User表添加 current_child_id 字段
ALTER TABLE users ADD COLUMN current_child_id BIGINT;

-- 1.1 Appointments表添加 domain 字段（咨询领域）
ALTER TABLE appointments ADD COLUMN domain VARCHAR(100) COMMENT '咨询领域/方向' AFTER description;

-- 2. 创建用户徽章表
CREATE TABLE IF NOT EXISTS user_badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id VARCHAR(50) NOT NULL,
    badge_name VARCHAR(100),
    badge_icon VARCHAR(200),
    category VARCHAR(50),
    earned_at DATETIME,
    INDEX idx_user_badges_user_id (user_id),
    INDEX idx_user_badges_badge_id (badge_id)
);

-- ============================================
-- 订单系统相关表 (2026-03-23 新增)
-- ============================================

-- 3. 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    package_id BIGINT COMMENT '套餐ID',
    package_name VARCHAR(200) COMMENT '套餐名称',
    order_type VARCHAR(20) NOT NULL COMMENT 'ORDER_TYPE_MEMBER:会员订单, ORDER_TYPE_RECHARGE:充值订单, ORDER_TYPE_PACKAGE:套餐订单',
    original_price DECIMAL(10,2) COMMENT '原价',
    discount_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    actual_price DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    payment_method VARCHAR(20) COMMENT 'ALIPAY, WECHAT, BALANCE',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING:待支付, PAID:已支付, REFUNDED:已退款, CANCELLED:已取消, EXPIRED:已过期',
    payment_time DATETIME COMMENT '支付时间',
    transaction_id VARCHAR(128) COMMENT '第三方交易流水号',
    vip_valid_days INT COMMENT 'VIP有效天数',
    vip_start_time DATETIME COMMENT 'VIP开始时间',
    vip_expire_time DATETIME COMMENT 'VIP过期时间',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    remark VARCHAR(500) COMMENT '备注',
    gmt_create DATETIME NOT NULL COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL COMMENT '修改时间',
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_order_no (order_no),
    INDEX idx_orders_status (payment_status),
    INDEX idx_orders_user_status (user_id, payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 4. 添加订单索引（如果表已存在）
-- ALTER TABLE orders ADD INDEX idx_orders_user_id (user_id);
-- ALTER TABLE orders ADD INDEX idx_orders_order_no (order_no);
-- ALTER TABLE orders ADD INDEX idx_orders_status (payment_status);

-- 3. 初始化一些咨询师数据（可选）
/*
INSERT INTO consultants (user_id, name, title, specialty, identity_tier, rating, served_count, is_available, gmt_create, gmt_modified)
VALUES 
(1, '张老师', '高级心理咨询师', '情绪问题,行为问题', 'GOLD', 4.80, 156, true, NOW(), NOW()),
(2, '李医生', '资深心理专家', '学习问题,注意力问题', 'PLATINUM', 4.95, 320, true, NOW(), NOW()),
(3, '王指导', '家庭教育指导师', '家庭教育,亲子关系', 'SILVER', 4.60, 89, true, NOW(), NOW());
*/

-- 4. 初始化问题分类数据（可选）
/*
INSERT INTO test_questions (package_id, question_type, question_text, options, correct_answer, sort_order)
VALUES 
(1, 'SINGLE_CHOICE', '以下哪种方法适合缓解孩子焦虑？', '["A. 强制要求", "B. 倾听和陪伴", "C. 忽视", "D. 惩罚"]', 'B', 1),
(1, 'SINGLE_CHOICE', '孩子注意力不集中时应该？', '["A. 批评", "B. 分段学习", "C. 强制专注", "D. 放弃"]', 'B', 2);
*/
