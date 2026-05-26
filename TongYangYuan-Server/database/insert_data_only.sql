-- =====================================================
-- 仅插入数据的SQL（不创建表）
-- 如果表已存在，只需执行INSERT部分
-- =====================================================

-- 插入默认套餐（使用 IGNORE 避免重复错误）
INSERT IGNORE INTO membership_packages (name, code, price, original_price, days, description, features, sort_order) VALUES
('月度会员', 'month', 99.00, 99.00, 30, '30天无限次咨询服务', '{"consultations": -1, "video_calls": -1, "pdf_export": true}', 1);

INSERT IGNORE INTO membership_packages (name, code, price, original_price, days, description, features, sort_order) VALUES
('季度会员', 'quarter', 269.00, 299.00, 90, '90天无限次咨询服务 · 省30元', '{"consultations": -1, "video_calls": -1, "pdf_export": true, "priority_support": true}', 2);

-- 插入支付配置项
INSERT IGNORE INTO payment_config (config_key, config_value, description) VALUES
('WECHAT_APP_ID', '', '微信开放平台应用APPID');

INSERT IGNORE INTO payment_config (config_key, config_value, description) VALUES
('WECHAT_MCH_ID', '', '微信商户号');

INSERT IGNORE INTO payment_config (config_key, config_value, description) VALUES
('WECHAT_API_KEY', '', '微信支付API密钥');

INSERT IGNORE INTO payment_config (config_key, config_value, description) VALUES
('WECHAT_APP_SECRET', '', '微信应用Secret');

INSERT IGNORE INTO payment_config (config_key, config_value, description) VALUES
('WECHAT_CERT_PATH', '', '微信支付证书路径');

INSERT IGNORE INTO payment_config (config_key, config_value, description) VALUES
('WECHAT_SANDBOX', 'false', '是否使用沙箱环境');

INSERT IGNORE INTO payment_config (config_key, config_value, description) VALUES
('ALIPAY_APP_ID', '', '支付宝应用APPID');

INSERT IGNORE INTO payment_config (config_key, config_value, description) VALUES
('ALIPAY_PRIVATE_KEY', '', '支付宝私钥');

INSERT IGNORE INTO payment_config (config_key, config_value, description) VALUES
('ALIPAY_PUBLIC_KEY', '', '支付宝公钥');

INSERT IGNORE INTO payment_config (config_key, config_value, description) VALUES
('ALIPAY_SANDBOX', 'false', '是否使用沙箱环境');

INSERT IGNORE INTO payment_config (config_key, config_value, description) VALUES
('PAYMENT_NOTIFY_URL', 'http://localhost:8080/api/payment/notify/wechat', '支付回调地址');

INSERT IGNORE INTO payment_config (config_key, config_value, description) VALUES
('PAYMENT_RETURN_URL', 'http://localhost:8080/payment/return', '支付跳转地址');

INSERT IGNORE INTO payment_config (config_key, config_value, description) VALUES
('ORDER_EXPIRE_MINUTES', '30', '订单过期时间(分钟)');

INSERT IGNORE INTO payment_config (config_key, config_value, description) VALUES
('CURRENCY', 'CNY', '货币类型');