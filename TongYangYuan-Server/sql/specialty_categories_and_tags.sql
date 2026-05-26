-- ============================================================
-- 咨询师擅长领域分类及问题标签数据
-- 9大分类，共200+问题标签
-- ============================================================

USE mental_health_db;

-- 1. 创建分类表
CREATE TABLE IF NOT EXISTS specialty_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '分类编码',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(100) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专长分类表';

-- 2. 创建问题标签表
CREATE TABLE IF NOT EXISTS specialty_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL COMMENT '所属分类ID',
    name VARCHAR(100) NOT NULL COMMENT '标签名称',
    code VARCHAR(100) COMMENT '标签编码',
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES specialty_categories(id) ON DELETE CASCADE,
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专长问题标签表';

-- 3. 创建咨询师-标签关联表
CREATE TABLE IF NOT EXISTS consultant_specialties (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consultant_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (consultant_id) REFERENCES consultants(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES specialty_tags(id) ON DELETE CASCADE,
    UNIQUE KEY uk_consultant_tag (consultant_id, tag_id),
    INDEX idx_consultant_id (consultant_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询师专长关联表';

-- 4. 插入9大分类
INSERT INTO specialty_categories (code, name, sort_order) VALUES
('mental_health', '心理健康', 1),
('love', '恋爱心理', 2),
('interpersonal', '人际关系', 3),
('emotion', '情绪管理', 4),
('marriage', '婚姻家庭', 5),
('parenting', '亲子教育', 6),
('career', '职场心理', 7),
('sexual', '性心理', 8),
('growth', '个人成长', 9);

-- 5. 插入问题标签
-- 心理健康 (mental_health) - 42个标签
SET @cat_mental = (SELECT id FROM specialty_categories WHERE code = 'mental_health');
INSERT INTO specialty_tags (category_id, name, sort_order) VALUES
(@cat_mental, '抑郁症', 1),
(@cat_mental, '焦虑症', 2),
(@cat_mental, '心理创伤', 3),
(@cat_mental, '双相情感障碍', 4),
(@cat_mental, '强迫症', 5),
(@cat_mental, '注意力缺陷多动障碍ADHD', 6),
(@cat_mental, 'PTSD创伤应激综合症', 7),
(@cat_mental, '躯体化反应', 8),
(@cat_mental, '睡眠问题', 9),
(@cat_mental, '回避型人格', 10),
(@cat_mental, '社交恐惧', 11),
(@cat_mental, '自杀倾向', 12),
(@cat_mental, '自残行为问题', 13),
(@cat_mental, '边缘型人格障碍BPD', 14),
(@cat_mental, '微笑抑郁症', 15),
(@cat_mental, '暴饮暴食', 16),
(@cat_mental, '成瘾问题', 17),
(@cat_mental, '人格障碍', 18),
(@cat_mental, '丧失与哀伤辅导', 19),
(@cat_mental, '偏执型人格障碍', 20),
(@cat_mental, '自恋性人格特质', 21),
(@cat_mental, '表演型人格', 22),
(@cat_mental, '妄想性障碍', 23),
(@cat_mental, '恋物癖', 24),
(@cat_mental, '产后抑郁', 25),
(@cat_mental, '性别认同', 26),
(@cat_mental, '躁狂症', 27),
(@cat_mental, '自闭症', 28),
(@cat_mental, '冲动控制/破坏性行为', 29),
(@cat_mental, '斯德哥尔摩综合症', 30),
(@cat_mental, '恐高', 31),
(@cat_mental, '进食障碍(厌食症)', 32),
(@cat_mental, '怀孕/围产期/产后心理健康', 33),
(@cat_mental, '异装症', 34),
(@cat_mental, '幽闭恐惧症', 35),
(@cat_mental, '更年期情绪问题', 36),
(@cat_mental, '巨物恐惧症', 37),
(@cat_mental, '器质性精神障碍', 38),
(@cat_mental, '异食症', 39);

-- 恋爱心理 (love) - 18个标签
SET @cat_love = (SELECT id FROM specialty_categories WHERE code = 'love');
INSERT INTO specialty_tags (category_id, name, sort_order) VALUES
(@cat_love, '失恋分手', 1),
(@cat_love, '情感创伤', 2),
(@cat_love, '回避型依恋', 3),
(@cat_love, '分离焦虑', 4),
(@cat_love, '择偶焦虑', 5),
(@cat_love, '前任情结', 6),
(@cat_love, '第三者困扰', 7),
(@cat_love, '多重恋爱困扰', 8),
(@cat_love, '异地恋', 9),
(@cat_love, '处女情结', 10),
(@cat_love, '结婚恐惧', 11),
(@cat_love, '单恋困扰', 12),
(@cat_love, '冷暴力', 13),
(@cat_love, '相亲恐惧', 14),
(@cat_love, '爱情嫉妒', 15),
(@cat_love, '性单恋', 16),
(@cat_love, '恋爱成瘾', 17),
(@cat_love, '处男情结', 18);

-- 人际关系 (interpersonal) - 14个标签
SET @cat_inter = (SELECT id FROM specialty_categories WHERE code = 'interpersonal');
INSERT INTO specialty_tags (category_id, name, sort_order) VALUES
(@cat_inter, '人际关系敏感', 1),
(@cat_inter, '社交焦虑', 2),
(@cat_inter, '社交困难', 3),
(@cat_inter, '社交退缩', 4),
(@cat_inter, '社交技巧', 5),
(@cat_inter, '社交孤立', 6),
(@cat_inter, '异性恐惧', 7),
(@cat_inter, '被害妄想', 8),
(@cat_inter, '友谊嫉妒', 9),
(@cat_inter, '对视恐惧', 10),
(@cat_inter, '余光恐惧', 11),
(@cat_inter, '网络人际关系成瘾', 12),
(@cat_inter, '电话恐惧', 13),
(@cat_inter, '网络暴力', 14);

-- 情绪管理 (emotion) - 23个标签
SET @cat_emotion = (SELECT id FROM specialty_categories WHERE code = 'emotion');
INSERT INTO specialty_tags (category_id, name, sort_order) VALUES
(@cat_emotion, '内耗情绪', 1),
(@cat_emotion, '焦虑情绪', 2),
(@cat_emotion, '抑郁情绪', 3),
(@cat_emotion, '恐惧', 4),
(@cat_emotion, '无价值感', 5),
(@cat_emotion, '情绪调节困难', 6),
(@cat_emotion, '暴躁易怒', 7),
(@cat_emotion, '情绪低落', 8),
(@cat_emotion, '孤独', 9),
(@cat_emotion, '自责', 10),
(@cat_emotion, '情绪失控', 11),
(@cat_emotion, '死亡焦虑', 12),
(@cat_emotion, '空虚感', 13),
(@cat_emotion, '压抑', 14),
(@cat_emotion, '情绪性进食', 15),
(@cat_emotion, '情感淡漠', 16),
(@cat_emotion, '怨恨心理', 17),
(@cat_emotion, '失败恐惧', 18),
(@cat_emotion, '空心病', 19),
(@cat_emotion, '负债', 20),
(@cat_emotion, '犯罪恐惧', 21),
(@cat_emotion, '外表焦虑', 22),
(@cat_emotion, '躁狂', 23);

-- 婚姻家庭 (marriage) - 24个标签
SET @cat_marriage = (SELECT id FROM specialty_categories WHERE code = 'marriage');
INSERT INTO specialty_tags (category_id, name, sort_order) VALUES
(@cat_marriage, '原生家庭创伤', 1),
(@cat_marriage, '伴侣沟通', 2),
(@cat_marriage, '婚外性', 3),
(@cat_marriage, '关系修复', 4),
(@cat_marriage, '亲密关系', 5),
(@cat_marriage, '老公出轨', 6),
(@cat_marriage, '亲子沟通', 7),
(@cat_marriage, '婚姻危机', 8),
(@cat_marriage, '家庭冲突', 9),
(@cat_marriage, '离婚咨询', 10),
(@cat_marriage, '婆媳矛盾', 11),
(@cat_marriage, '精神出轨', 12),
(@cat_marriage, '感情变故', 13),
(@cat_marriage, '三角关系', 14),
(@cat_marriage, '依恋问题', 15),
(@cat_marriage, '婚姻倦怠', 16),
(@cat_marriage, '重男轻女', 17),
(@cat_marriage, '冷暴力', 18),
(@cat_marriage, '催婚压力', 19),
(@cat_marriage, '家暴创伤', 20),
(@cat_marriage, '恐婚', 21),
(@cat_marriage, '育儿分歧', 22),
(@cat_marriage, '分娩恐惧', 23),
(@cat_marriage, '丧偶', 24);

-- 亲子教育 (parenting) - 27个标签
SET @cat_parent = (SELECT id FROM specialty_categories WHERE code = 'parenting');
INSERT INTO specialty_tags (category_id, name, sort_order) VALUES
(@cat_parent, '休学厌学', 1),
(@cat_parent, '青少年情绪问题', 2),
(@cat_parent, '青少年抑郁', 3),
(@cat_parent, '青少年学业压力', 4),
(@cat_parent, '未成年网瘾', 5),
(@cat_parent, '青少年人际关系', 6),
(@cat_parent, '自卑/不自信', 7),
(@cat_parent, '学习拖延', 8),
(@cat_parent, '性教育', 9),
(@cat_parent, '青春期性心理', 10),
(@cat_parent, '考试焦虑', 11),
(@cat_parent, '恋母情结', 12),
(@cat_parent, '青春期叛逆', 13),
(@cat_parent, '早恋', 14),
(@cat_parent, '校园暴力创伤', 15),
(@cat_parent, '儿童分离焦虑', 16),
(@cat_parent, '注意力问题', 17),
(@cat_parent, '暴力倾向', 18),
(@cat_parent, '多动症', 19),
(@cat_parent, '校园欺凌', 20),
(@cat_parent, '逃学', 21),
(@cat_parent, '未成年人性别认知困难', 22),
(@cat_parent, '攻击行为问题', 23),
(@cat_parent, '青春期风险行为', 24),
(@cat_parent, '儿童自闭倾向', 25),
(@cat_parent, '幼儿入园恐惧', 26),
(@cat_parent, '自理困难', 27);

-- 职场心理 (career) - 18个标签
SET @cat_career = (SELECT id FROM specialty_categories WHERE code = 'career');
INSERT INTO specialty_tags (category_id, name, sort_order) VALUES
(@cat_career, '工作迷茫', 1),
(@cat_career, '职业发展', 2),
(@cat_career, '职场人际', 3),
(@cat_career, '职业倦怠', 4),
(@cat_career, '失业焦虑', 5),
(@cat_career, '就业压力', 6),
(@cat_career, '入职焦虑', 7),
(@cat_career, '内卷', 8),
(@cat_career, '沟通技能', 9),
(@cat_career, '职场PUA', 10),
(@cat_career, '职场霸凌', 11),
(@cat_career, '职场晋升', 12),
(@cat_career, '潜规则', 13),
(@cat_career, '跳槽', 14),
(@cat_career, '同级关系', 15),
(@cat_career, '职场信任', 16),
(@cat_career, '职场恋情', 17),
(@cat_career, '职场歧视', 18);

-- 性心理 (sexual) - 12个标签
SET @cat_sexual = (SELECT id FROM specialty_categories WHERE code = 'sexual');
INSERT INTO specialty_tags (category_id, name, sort_order) VALUES
(@cat_sexual, '性瘾', 1),
(@cat_sexual, '性变态', 2),
(@cat_sexual, '性取向', 3),
(@cat_sexual, '性生活不协调', 4),
(@cat_sexual, '同性恋', 5),
(@cat_sexual, '性健康和功能性障碍', 6),
(@cat_sexual, '性创伤', 7),
(@cat_sexual, '双性恋', 8),
(@cat_sexual, '性冷淡', 9),
(@cat_sexual, '性侵创伤症候群', 10),
(@cat_sexual, '跨性别', 11);

-- 个人成长 (growth) - 29个标签
SET @cat_growth = (SELECT id FROM specialty_categories WHERE code = 'growth');
INSERT INTO specialty_tags (category_id, name, sort_order) VALUES
(@cat_growth, '童年创伤', 1),
(@cat_growth, '自我探索', 2),
(@cat_growth, '自我价值', 3),
(@cat_growth, '学业压力', 4),
(@cat_growth, '女性成长', 5),
(@cat_growth, '自卑', 6),
(@cat_growth, '讨好型人格', 7),
(@cat_growth, '完美主义', 8),
(@cat_growth, '拖延症', 9),
(@cat_growth, '性格缺陷', 10),
(@cat_growth, '男性成长', 11),
(@cat_growth, '时间管理', 12),
(@cat_growth, '选择困难', 13),
(@cat_growth, '抗挫力', 14),
(@cat_growth, '自尊', 15),
(@cat_growth, '宗教信仰', 16),
(@cat_growth, '中年危机', 17),
(@cat_growth, '同一性混乱', 18),
(@cat_growth, '留学适应', 19),
(@cat_growth, '睡前拖延', 20),
(@cat_growth, '彼得潘综合征', 21),
(@cat_growth, '非理性消费', 22),
(@cat_growth, '皮肤饥渴', 23),
(@cat_growth, '反社会行为', 24),
(@cat_growth, '跨文化适应压力', 25),
(@cat_growth, '虚荣心', 26);

-- ============================================================
-- 6. 初始化咨询师-标签关联数据
-- 为测试咨询师建立与专长标签的关联
-- ============================================================

-- 张医生(id=1): 儿童焦虑、学习障碍
-- 关联标签: 焦虑症(mental_health), 注意力缺陷多动障碍ADHD(mental_health), 注意力问题(parenting), 考试焦虑(parenting)
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(1, (SELECT id FROM specialty_tags WHERE name = '焦虑症')),
(1, (SELECT id FROM specialty_tags WHERE name = '注意力缺陷多动障碍ADHD')),
(1, (SELECT id FROM specialty_tags WHERE name = '注意力问题')),
(1, (SELECT id FROM specialty_tags WHERE name = '考试焦虑'));

-- 李医生(id=2): 青春期问题、情绪管理
-- 关联标签: 青春期叛逆(parenting), 青春期性心理(parenting), 焦虑情绪(emotion), 情绪调节困难(emotion), 青少年情绪问题(parenting)
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(2, (SELECT id FROM specialty_tags WHERE name = '青春期叛逆')),
(2, (SELECT id FROM specialty_tags WHERE name = '青春期性心理')),
(2, (SELECT id FROM specialty_tags WHERE name = '焦虑情绪')),
(2, (SELECT id FROM specialty_tags WHERE name = '情绪调节困难')),
(2, (SELECT id FROM specialty_tags WHERE name = '青少年情绪问题'));

-- 王医生(id=3): 亲子关系、家庭矛盾
-- 关联标签: 亲子沟通(marriage), 家庭冲突(marriage), 原生家庭创伤(marriage), 亲密关系(marriage), 关系修复(marriage)
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(3, (SELECT id FROM specialty_tags WHERE name = '亲子沟通')),
(3, (SELECT id FROM specialty_tags WHERE name = '家庭冲突')),
(3, (SELECT id FROM specialty_tags WHERE name = '原生家庭创伤')),
(3, (SELECT id FROM specialty_tags WHERE name = '亲密关系')),
(3, (SELECT id FROM specialty_tags WHERE name = '关系修复'));
