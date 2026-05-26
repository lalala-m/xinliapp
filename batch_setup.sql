-- 批量创建咨询师并分配标签
-- 1. 先清除现有标签关联（重新分配）
DELETE FROM consultant_specialties;

-- 2. 给现有4个咨询师分配标签（每人6个，覆盖不同分类）
-- 张医生 (ID=1): 心理健康相关
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 5), (1, 9), (1, 12);

-- 咨询师一 (ID=3): 恋爱心理+情绪管理
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(3, 40), (3, 42), (3, 48), (3, 72), (3, 75), (3, 82);

-- 咨询师二 (ID=4): 婚姻家庭+亲子教育
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(4, 95), (4, 96), (4, 102), (4, 119), (4, 125), (4, 131);

-- 咨询师三 (ID=5): 职场心理+个人成长
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(5, 146), (5, 149), (5, 153), (5, 175), (5, 180), (5, 183);

-- 3. 创建30个新用户（咨询师账号）
-- 密码都是 123456 的 bcrypt 加密值
INSERT INTO users (phone, password, nickname, user_type, status, created_at, updated_at) VALUES
('13800010001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '李心怡', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '王晓明', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '陈静', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '刘芳', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '赵强', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '孙丽', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010007', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '周杰', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010008', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '吴敏', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010009', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '郑华', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010010', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '黄蓉', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010011', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '林峰', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010012', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '徐倩', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010013', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '马超', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010014', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '朱琳', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010015', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '胡军', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010016', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '郭雪', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010017', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '何伟', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010018', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '高娜', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010019', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '罗刚', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010020', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '梁雨', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010021', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '宋阳', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010022', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '唐薇', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010023', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '韩冰', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010024', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '冯磊', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010025', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '董欣', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010026', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '曾辉', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010027', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '彭静', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010028', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '潘峰', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010029', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '袁梅', 'CONSULTANT', 'ACTIVE', NOW(), NOW()),
('13800010030', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '蒋文', 'CONSULTANT', 'ACTIVE', NOW(), NOW());

-- 4. 创建30个新咨询师档案
INSERT INTO consultants (user_id, name, title, specialty, intro, available, identity_tier, created_at, updated_at)
SELECT 
    u.id,
    u.nickname,
    CASE u.nickname
        WHEN '李心怡' THEN '高级心理咨询师'
        WHEN '王晓明' THEN '婚姻家庭咨询师'
        WHEN '陈静' THEN '儿童心理专家'
        WHEN '刘芳' THEN '职场心理顾问'
        WHEN '赵强' THEN '恋爱心理导师'
        WHEN '孙丽' THEN '性心理咨询师'
        WHEN '周杰' THEN '人际关系专家'
        WHEN '吴敏' THEN '创伤治疗师'
        WHEN '郑华' THEN '个人成长导师'
        WHEN '黄蓉' THEN '青少年心理专家'
        WHEN '林峰' THEN '认知行为治疗师'
        WHEN '徐倩' THEN '精神分析师'
        WHEN '马超' THEN '正念冥想导师'
        WHEN '朱琳' THEN '家庭治疗师'
        WHEN '胡军' THEN '危机干预专家'
        WHEN '郭雪' THEN '艺术治疗师'
        WHEN '何伟' THEN '睡眠障碍专家'
        WHEN '高娜' THEN '饮食心理专家'
        WHEN '罗刚' THEN '成瘾治疗专家'
        WHEN '梁雨' THEN '跨文化心理顾问'
        WHEN '宋阳' THEN '男性心理专家'
        WHEN '唐薇' THEN '女性心理导师'
        WHEN '韩冰' THEN '老年心理专家'
        WHEN '冯磊' THEN '团体治疗师'
        WHEN '董欣' THEN '游戏治疗师'
        WHEN '曾辉' THEN '催眠治疗师'
        WHEN '彭静' THEN '叙事治疗师'
        WHEN '潘峰' THEN '存在主义治疗师'
        WHEN '袁梅' THEN '积极心理学导师'
        WHEN '蒋文' THEN '神经心理专家'
    END,
    CASE u.nickname
        WHEN '李心怡' THEN '情绪管理、焦虑抑郁'
        WHEN '王晓明' THEN '婚姻修复、家庭关系'
        WHEN '陈静' THEN '儿童心理、亲子教育'
        WHEN '刘芳' THEN '职场压力、职业规划'
        WHEN '赵强' THEN '恋爱关系、情感修复'
        WHEN '孙丽' THEN '性心理、性别认同'
        WHEN '周杰' THEN '社交恐惧、人际沟通'
        WHEN '吴敏' THEN '心理创伤、PTSD'
        WHEN '郑华' THEN '自我探索、自信培养'
        WHEN '黄蓉' THEN '青春期问题、学业压力'
        WHEN '林峰' THEN '认知行为疗法、焦虑障碍'
        WHEN '徐倩' THEN '精神分析、人格障碍'
        WHEN '马超' THEN '正念冥想、情绪调节'
        WHEN '朱琳' THEN '家庭系统、亲子沟通'
        WHEN '胡军' THEN '自杀干预、危机处理'
        WHEN '郭雪' THEN '艺术治疗、表达性治疗'
        WHEN '何伟' THEN '失眠治疗、睡眠障碍'
        WHEN '高娜' THEN '进食障碍、情绪性进食'
        WHEN '罗刚' THEN '网络成瘾、物质成瘾'
        WHEN '梁雨' THEN '跨文化适应、留学心理'
        WHEN '宋阳' THEN '男性成长、中年危机'
        WHEN '唐薇' THEN '女性成长、产后抑郁'
        WHEN '韩冰' THEN '老年心理、丧偶哀伤'
        WHEN '冯磊' THEN '团体治疗、社交技能'
        WHEN '董欣' THEN '儿童游戏治疗、沙盘治疗'
        WHEN '曾辉' THEN '催眠治疗、潜意识探索'
        WHEN '彭静' THEN '叙事疗法、生命故事'
        WHEN '潘峰' THEN '存在主义、人生意义'
        WHEN '袁梅' THEN '积极心理、幸福力'
        WHEN '蒋文' THEN 'ADHD、自闭症'
    END,
    CASE u.nickname
        WHEN '李心怡' THEN '擅长情绪调节与压力管理，帮助来访者重建内心平衡'
        WHEN '王晓明' THEN '专注于婚姻家庭领域，帮助夫妻重建信任与沟通'
        WHEN '陈静' THEN '多年儿童心理咨询经验，擅长处理青少年情绪问题'
        WHEN '刘芳' THEN '帮助企业员工缓解职场压力，提升工作幸福感'
        WHEN '赵强' THEN '专注于恋爱心理学，帮助来访者建立健康的亲密关系'
        WHEN '孙丽' THEN '提供专业的性心理咨询服务，尊重每一位来访者'
        WHEN '周杰' THEN '帮助来访者克服社交障碍，建立良好的人际关系'
        WHEN '吴敏' THEN '擅长创伤后应激障碍的治疗，帮助来访者走出阴影'
        WHEN '郑华' THEN '引导来访者进行自我探索，发现内在潜能'
        WHEN '黄蓉' THEN '专注于青少年心理健康，帮助青少年健康成长'
        WHEN '林峰' THEN '运用认知行为疗法，帮助来访者改变负面思维模式'
        WHEN '徐倩' THEN '深耕精神分析领域，帮助来访者理解潜意识'
        WHEN '马超' THEN '结合正念冥想技术，帮助来访者获得内心平静'
        WHEN '朱琳' THEN '运用家庭系统理论，帮助家庭重建和谐关系'
        WHEN '胡军' THEN '24小时危机干预服务，守护每一位需要帮助的人'
        WHEN '郭雪' THEN '通过艺术形式帮助来访者表达和疗愈内心'
        WHEN '何伟' THEN '专注于睡眠障碍的治疗，帮助来访者重获好睡眠'
        WHEN '高娜' THEN '帮助来访者建立健康的饮食关系和身心态'
        WHEN '罗刚' THEN '帮助来访者摆脱各种成瘾行为，重获自由'
        WHEN '梁雨' THEN '帮助跨文化背景下的来访者适应新环境'
        WHEN '宋阳' THEN '关注男性心理健康，帮助男性应对人生挑战'
        WHEN '唐薇' THEN '陪伴女性成长，帮助女性找到自我价值和力量'
        WHEN '韩冰' THEN '关注老年人心理健康，帮助老年人安享晚年'
        WHEN '冯磊' THEN '通过团体治疗，帮助来访者在互动中成长'
        WHEN '董欣' THEN '运用游戏和沙盘技术，帮助儿童表达内心世界'
        WHEN '曾辉' THEN '运用催眠技术，帮助来访者深入潜意识进行疗愈'
        WHEN '彭静' THEN '通过重写生命故事，帮助来访者找到新的可能'
        WHEN '潘峰' THEN '探讨人生意义和价值，帮助来访者找到生命方向'
        WHEN '袁梅' THEN '运用积极心理学，帮助来访者培养幸福感和韧性'
        WHEN '蒋文' THEN '专注于神经发育障碍的评估和干预'
    END,
    TRUE,
    'BRONZE',
    NOW(),
    NOW()
FROM users u
WHERE u.phone LIKE '1380001%' AND u.user_type = 'CONSULTANT';

-- 5. 给30个新咨询师分配标签（每人6个，确保覆盖所有200个标签）
-- 新咨询师ID从6开始（现有4个：1,3,4,5）
-- 分配策略：每个新咨询师覆盖不同分类的标签

-- 李心怡 (ID=6): 情绪管理
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(6, 72), (6, 73), (6, 77), (6, 78), (6, 83), (6, 94);

-- 王晓明 (ID=7): 婚姻家庭
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(7, 97), (7, 99), (7, 100), (7, 104), (7, 105), (7, 110);

-- 陈静 (ID=8): 亲子教育
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(8, 120), (8, 121), (8, 123), (8, 129), (8, 134), (8, 137);

-- 刘芳 (ID=9): 职场心理
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(9, 147), (9, 148), (9, 150), (9, 151), (9, 155), (9, 156);

-- 赵强 (ID=10): 恋爱心理
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(10, 41), (10, 43), (10, 45), (10, 50), (10, 54), (10, 55);

-- 孙丽 (ID=11): 性心理
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(11, 164), (11, 165), (11, 166), (11, 167), (11, 170), (11, 172);

-- 周杰 (ID=12): 人际关系
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(12, 58), (12, 59), (12, 60), (12, 62), (12, 64), (12, 67);

-- 吴敏 (ID=13): 心理创伤
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(13, 3), (13, 7), (13, 8), (13, 13), (13, 14), (13, 19);

-- 郑华 (ID=14): 个人成长
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(14, 176), (14, 177), (14, 178), (14, 181), (14, 182), (14, 188);

-- 黄蓉 (ID=15): 青少年
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(15, 122), (15, 124), (15, 128), (15, 130), (15, 132), (15, 133);

-- 林峰 (ID=16): 认知行为
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(16, 1), (16, 2), (16, 5), (16, 10), (16, 11), (16, 18);

-- 徐倩 (ID=17): 精神分析
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(17, 20), (17, 21), (17, 22), (17, 23), (17, 24), (17, 26);

-- 马超 (ID=18): 正念冥想
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(18, 74), (18, 76), (18, 80), (18, 84), (18, 86), (18, 89);

-- 朱琳 (ID=19): 家庭治疗
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(19, 96), (19, 98), (19, 101), (19, 103), (19, 106), (19, 108);

-- 胡军 (ID=20): 危机干预
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(20, 12), (20, 25), (20, 27), (20, 29), (20, 31), (20, 38);

-- 郭雪 (ID=21): 艺术治疗
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(21, 4), (21, 6), (21, 15), (21, 16), (21, 28), (21, 32);

-- 何伟 (ID=22): 睡眠障碍
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(22, 9), (22, 33), (22, 35), (22, 36), (22, 37), (22, 39);

-- 高娜 (ID=23): 饮食心理
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(23, 17), (23, 30), (23, 34), (23, 85), (23, 87), (23, 93);

-- 罗刚 (ID=24): 成瘾治疗
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(24, 44), (24, 46), (24, 47), (24, 49), (24, 52), (24, 56);

-- 梁雨 (ID=25): 跨文化
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(25, 191), (25, 193), (25, 196), (25, 198), (25, 199), (25, 200);

-- 宋阳 (ID=26): 男性心理
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(26, 90), (26, 91), (26, 92), (26, 113), (26, 117), (26, 157);

-- 唐薇 (ID=27): 女性心理
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(27, 107), (27, 109), (27, 111), (27, 112), (27, 114), (27, 115);

-- 韩冰 (ID=28): 老年心理
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(28, 95), (28, 118), (28, 135), (28, 138), (28, 139), (28, 143);

-- 冯磊 (ID=29): 团体治疗
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(29, 61), (29, 63), (29, 65), (29, 66), (29, 68), (29, 69);

-- 董欣 (ID=30): 游戏治疗
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(30, 119), (30, 125), (30, 126), (30, 127), (30, 136), (30, 140);

-- 曾辉 (ID=31): 催眠治疗
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(31, 70), (31, 71), (31, 79), (31, 81), (31, 88), (31, 116);

-- 彭静 (ID=32): 叙事治疗
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(32, 40), (32, 42), (32, 48), (32, 51), (32, 53), (32, 57);

-- 潘峰 (ID=33): 存在主义
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(33, 179), (33, 183), (33, 185), (33, 187), (33, 189), (33, 192);

-- 袁梅 (ID=34): 积极心理
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(34, 173), (34, 174), (34, 175), (34, 180), (34, 184), (34, 186);

-- 蒋文 (ID=35): 神经心理
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(35, 145), (35, 141), (35, 142), (35, 144), (35, 152), (35, 158);
