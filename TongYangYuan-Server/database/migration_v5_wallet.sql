-- 钱包系统数据库迁移 v5
-- 创建时间: 2026-05-04

-- 1. 用户钱包表
CREATE TABLE IF NOT EXISTS user_wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '当前余额',
    total_recharged DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计充值',
    total_consumed DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户钱包表';

-- 2. 钱包交易记录表
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    transaction_no VARCHAR(64) NOT NULL UNIQUE COMMENT '交易流水号',
    type VARCHAR(20) NOT NULL COMMENT '类型: RECHARGE/CONSUME/REFUND',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额(正数)',
    balance_before DECIMAL(10,2) NOT NULL COMMENT '交易前余额',
    balance_after DECIMAL(10,2) NOT NULL COMMENT '交易后余额',
    related_order_no VARCHAR(64) COMMENT '关联订单号',
    related_appointment_id BIGINT COMMENT '关联预约ID',
    description VARCHAR(200) COMMENT '交易描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包交易记录表';

-- 3. 充值套餐表
CREATE TABLE IF NOT EXISTS recharge_packages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '套餐名称',
    amount DECIMAL(10,2) NOT NULL COMMENT '充值金额',
    bonus_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '赠送金额',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值套餐表';

-- 插入默认充值套餐
INSERT INTO recharge_packages (name, amount, bonus_amount, sort_order) VALUES
('充值 ¥50', 50.00, 0.00, 1),
('充值 ¥100', 100.00, 5.00, 2),
('充值 ¥200', 200.00, 15.00, 3),
('充值 ¥500', 500.00, 50.00, 4);

-- 4. 修改咨询师表：添加咨询费用字段
ALTER TABLE consultants ADD COLUMN IF NOT EXISTS consultation_fee DECIMAL(10,2) DEFAULT 50.00 COMMENT '单次咨询费用';

-- 5. 修改预约表：添加支付相关字段
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS payment_amount DECIMAL(10,2) DEFAULT NULL COMMENT '预约支付金额';
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20) DEFAULT 'UNPAID' COMMENT '支付状态: UNPAID/PAID/REFUNDED';
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS paid_by_wallet BOOLEAN DEFAULT FALSE COMMENT '是否钱包支付';

-- 6. 修改会员套餐表：添加钱包充值套餐
INSERT INTO membership_packages (name, code, price, original_price, days, description, features, sort_order, is_active) VALUES
('钱包充值 ¥50', 'wallet_50', 50.00, 50.00, 0, '钱包余额充值50元', '{"type": "wallet_recharge", "amount": 50}', 99, TRUE),
('钱包充值 ¥100', 'wallet_100', 100.00, 100.00, 0, '钱包余额充值100元，赠送5元', '{"type": "wallet_recharge", "amount": 100, "bonus": 5}', 100, TRUE),
('钱包充值 ¥200', 'wallet_200', 200.00, 200.00, 0, '钱包余额充值200元，赠送15元', '{"type": "wallet_recharge", "amount": 200, "bonus": 15}', 101, TRUE),
('钱包充值 ¥500', 'wallet_500', 500.00, 500.00, 0, '钱包余额充值500元，赠送50元', '{"type": "wallet_recharge", "amount": 500, "bonus": 50}', 102, TRUE);
