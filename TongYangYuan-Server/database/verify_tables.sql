-- =====================================================
-- 支付系统数据验证脚本（不会修改数据，只查看）
-- =====================================================

-- 检查套餐数据
SELECT '=== 会员套餐 ===' as info;
SELECT id, name, code, price, days, is_active FROM membership_packages ORDER BY sort_order;

-- 检查支付配置
SELECT '=== 支付配置 ===' as info;
SELECT config_key, config_value, description FROM payment_config;

-- 检查订单表结构
SELECT '=== 订单表结构 ===' as info;
DESCRIBE payment_orders;

-- 检查会员权益表结构
SELECT '=== 会员权益表结构 ===' as info;
DESCRIBE membership_records;

-- 统计订单数量
SELECT '=== 订单统计 ===' as info;
SELECT payment_status, COUNT(*) as count FROM payment_orders GROUP BY payment_status;