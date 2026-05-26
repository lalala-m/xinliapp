-- =====================================================
-- 支付系统数据库初始化脚本
-- 使用说明：直接执行此文件即可
-- 数据库: mental_health_db
-- =====================================================

-- 1. 创建会员套餐表
CREATE TABLE IF NOT EXISTS membership_packages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '套餐名称',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '套餐代码: month/quarter',
    price DECIMAL(10,2) NOT NULL COMMENT '价格(元)',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    days INT NOT NULL COMMENT '有效期(天)',
    description VARCHAR(200) DEFAULT NULL COMMENT '套餐描述',
    features TEXT DEFAULT NULL COMMENT '功能特性(JSON)',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员套餐表';

-- 2. 创建支付订单表
CREATE TABLE IF NOT EXISTS payment_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    
    -- 订单信息
    package_code VARCHAR(20) NOT NULL COMMENT '套餐代码',
    package_name VARCHAR(50) NOT NULL COMMENT '套餐名称',
    price DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    quantity INT DEFAULT 1 COMMENT '数量',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    
    -- 支付信息
    payment_method VARCHAR(20) DEFAULT NULL COMMENT '支付方式: alipay/wechat',
    payment_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '支付状态: PENDING/SUCCESS/FAILED/REFUNDED/CLOSED',
    trade_no VARCHAR(64) DEFAULT NULL COMMENT '第三方交易号',
    paid_time DATETIME DEFAULT NULL COMMENT '支付时间',
    
    -- 回调信息
    callback_url VARCHAR(500) DEFAULT NULL COMMENT '回调通知URL',
    callback_raw TEXT DEFAULT NULL COMMENT '回调原始数据',
    callback_verified BOOLEAN DEFAULT FALSE COMMENT '回调是否已验证',
    
    -- 附加信息
    attach VARCHAR(500) DEFAULT NULL COMMENT '附加数据',
    client_ip VARCHAR(50) DEFAULT NULL COMMENT '客户端IP',
    device_info VARCHAR(200) DEFAULT NULL COMMENT '设备信息',
    
    -- 错误信息
    error_code VARCHAR(50) DEFAULT NULL COMMENT '错误码',
    error_msg VARCHAR(200) DEFAULT NULL COMMENT '错误信息',
    
    -- 时间戳
    expires_at DATETIME DEFAULT NULL COMMENT '订单过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_payment_status (payment_status),
    INDEX idx_created_at (created_at),
    INDEX idx_paid_time (paid_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';

-- 3. 创建会员权益表
CREATE TABLE IF NOT EXISTS membership_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    package_code VARCHAR(20) NOT NULL COMMENT '套餐代码',
    package_name VARCHAR(50) NOT NULL COMMENT '套餐名称',
    
    -- 时间信息
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    days INT NOT NULL COMMENT '有效期天数',
    
    -- 来源信息
    order_id BIGINT DEFAULT NULL COMMENT '来源订单ID',
    order_no VARCHAR(64) DEFAULT NULL COMMENT '来源订单号',
    source VARCHAR(20) DEFAULT 'PURCHASE' COMMENT '来源: PURCHASE/ADMIN/GIFT/TRIAL',
    
    -- 状态
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/EXPIRED/CANCELLED',
    auto_renew BOOLEAN DEFAULT FALSE COMMENT '是否自动续费',
    
    -- 备注
    remark VARCHAR(200) DEFAULT NULL COMMENT '备注',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_end_time (end_time),
    UNIQUE KEY uk_user_package_order (user_id, order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员权益记录表';

-- 4. 创建支付配置表
CREATE TABLE IF NOT EXISTS payment_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(50) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT DEFAULT NULL COMMENT '配置值',
    description VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付配置表';

-- =====================================================
-- 插入初始数据
-- =====================================================

-- 插入默认套餐
INSERT INTO membership_packages (name, code, price, original_price, days, description, features, sort_order) VALUES
('月度会员', 'month', 99.00, 99.00, 30, '30天无限次咨询服务', '{"consultations": -1, "video_calls": -1, "pdf_export": true}', 1),
('季度会员', 'quarter', 269.00, 299.00, 90, '90天无限次咨询服务 · 省30元', '{"consultations": -1, "video_calls": -1, "pdf_export": true, "priority_support": true}', 2);

-- 插入支付配置项
INSERT INTO payment_config (config_key, config_value, description) VALUES
-- 微信支付配置
('WECHAT_APP_ID', '', '微信开放平台应用APPID'),
('WECHAT_MCH_ID', '', '微信商户号'),
('WECHAT_API_KEY', '', '微信支付API密钥'),
('WECHAT_APP_SECRET', '', '微信应用Secret'),
('WECHAT_CERT_PATH', '', '微信支付证书路径'),
('WECHAT_SANDBOX', 'false', '是否使用沙箱环境'),

-- 支付宝配置
('ALIPAY_APP_ID', '', '支付宝应用APPID'),
('ALIPAY_PRIVATE_KEY', '', '支付宝私钥'),
('ALIPAY_PUBLIC_KEY', '', '支付宝公钥'),
('ALIPAY_SANDBOX', 'false', '是否使用沙箱环境'),

-- 通用配置
('PAYMENT_NOTIFY_URL', 'http://localhost:8080/api/payment/notify/wechat', '支付回调地址'),
('PAYMENT_RETURN_URL', 'http://localhost:8080/payment/return', '支付跳转地址'),
('ORDER_EXPIRE_MINUTES', '30', '订单过期时间(分钟)'),
('CURRENCY', 'CNY', '货币类型');

-- =====================================================
-- 验证数据
-- =====================================================
SELECT '=== 会员套餐 ===' as info;
SELECT * FROM membership_packages;

SELECT '=== 支付配置 ===' as info;
SELECT * FROM payment_config;