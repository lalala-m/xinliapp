-- 清理所有现有数据
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM chat_messages;
DELETE FROM appointments;
DELETE FROM posts;
DELETE FROM consultation_records;
DELETE FROM user_test_records;
DELETE FROM user_learning_progress;
DELETE FROM user_recommended_packages;
DELETE FROM psychological_assessments;
DELETE FROM admin_logs;
DELETE FROM consultants;
DELETE FROM children;
DELETE FROM users;
DELETE FROM user_online_status;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入咨询师账户（用于电脑端登录）
-- 账号：13800000001  密码：password123
INSERT INTO users (id, phone, password, user_type, nickname, status) VALUES
(1, '13800000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHlALq.1eBzQxBfzMlvK', 'CONSULTANT', '张医生', 'ACTIVE');

-- 插入家长账户（用于手机端登录）
-- 账号：13900000001  密码：password123
INSERT INTO users (id, phone, password, user_type, nickname, status) VALUES
(2, '13900000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHlALq.1eBzQxBfzMlvK', 'PARENT', '王女士', 'ACTIVE');

-- 插入管理员账户（用于后台管理）
-- 账号：admin  密码：password123
INSERT INTO users (id, phone, password, user_type, nickname, status) VALUES
(3, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdHlALq.1eBzQxBfzMlvK', 'ADMIN', '管理员', 'ACTIVE');

-- 插入咨询师详细信息
INSERT INTO consultants (id, user_id, name, title, specialty, identity_tier, rating, served_count, intro) VALUES
(1, 1, '张医生', '儿童心理咨询师', '儿童焦虑、学习障碍', 'GOLD', 4.8, 0, '拥有10年儿童心理咨询经验，擅长处理儿童焦虑和学习障碍问题。');

-- 插入孩子信息
-- 关联家长ID: 2 (王女士)
INSERT INTO children (parent_user_id, name, gender, birth_date, ethnicity, native_place, birth_place, family_rank, language_env, school, home_address, interests, activities, body_status, medical_history, father_phone, mother_phone, created_at, updated_at) VALUES
(2, '小明', 'BOY', '2015-05-20', '汉族', '北京', '北京', '老大', '普通话', '北京市第一实验小学', '北京市朝阳区幸福家园1号楼', '乐高积木, 绘画', '游泳课, 钢琴班', '很好', '无', '13900000001', '13900000002', NOW(), NOW());

-- 插入示例预约 (确保电脑端刷新时有数据)
-- ID: 1, 咨询师ID: 1 (对应张医生), 家长ID: 2
INSERT INTO appointments (id, appointment_no, consultant_id, parent_user_id, child_name, child_age, appointment_date, time_slot, description, status, is_pinned, is_chatted, created_at, updated_at) VALUES
(1, 'APT-20231001-001', 1, 2, '小明', 8, CURRENT_DATE, '10:00-11:00', '孩子最近注意力不集中', 'IN_PROGRESS', 0, 1, NOW(), NOW());
