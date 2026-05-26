USE mental_health_db;

INSERT INTO membership_packages (name, code, price, original_price, days, description, features, sort_order, is_active) VALUES
('钱包充值50', 'wallet_50', 50.00, 50.00, 0, '钱包余额充值50元', '{}', 99, TRUE),
('钱包充值100', 'wallet_100', 100.00, 100.00, 0, '钱包余额充值100元，赠送5元', '{}', 100, TRUE),
('钱包充值200', 'wallet_200', 200.00, 200.00, 0, '钱包余额充值200元，赠送15元', '{}', 101, TRUE),
('钱包充值500', 'wallet_500', 500.00, 500.00, 0, '钱包余额充值500元，赠送50元', '{}', 102, TRUE);
