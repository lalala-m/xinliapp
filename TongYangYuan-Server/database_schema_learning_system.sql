-- 学习系统数据库表结构

-- 1. 咨询记录表（扩展）
CREATE TABLE IF NOT EXISTS consultation_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL,
    consultant_id BIGINT NOT NULL,
    parent_user_id BIGINT NOT NULL,
    child_id BIGINT,
    consultation_type VARCHAR(20) NOT NULL COMMENT '咨询类型：ONLINE, OFFLINE, VIDEO',
    duration INT COMMENT '咨询时长（分钟）',
    summary TEXT COMMENT '咨询摘要',
    consultant_feedback TEXT COMMENT '咨询师反馈',
    core_issue_tags VARCHAR(500) COMMENT '核心困扰标签，逗号分隔',
    rating DECIMAL(3,2) COMMENT '用户评分',
    user_comment TEXT COMMENT '用户评价',
    status VARCHAR(20) DEFAULT 'COMPLETED' COMMENT '状态：COMPLETED, CANCELLED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (consultant_id) REFERENCES consultants(id),
    FOREIGN KEY (parent_user_id) REFERENCES users(id),
    INDEX idx_parent_user (parent_user_id),
    INDEX idx_consultant (consultant_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询记录表';

-- 2. 学习包表
CREATE TABLE IF NOT EXISTS learning_packages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL COMMENT '学习包标题',
    category VARCHAR(50) NOT NULL COMMENT '分类：情绪调节、人际沟通、职场心理、亲子教育等',
    description TEXT COMMENT '学习包描述',
    issue_tags VARCHAR(500) COMMENT '关联的困扰标签，逗号分隔',
    cover_image VARCHAR(500) COMMENT '封面图片URL',
    video_count INT DEFAULT 0 COMMENT '视频数量',
    total_duration INT DEFAULT 0 COMMENT '总时长（分钟）',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习包表';

-- 3. 学习视频表
CREATE TABLE IF NOT EXISTS learning_videos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    package_id BIGINT NOT NULL COMMENT '所属学习包ID',
    title VARCHAR(100) NOT NULL COMMENT '视频标题',
    description TEXT COMMENT '视频描述',
    video_url VARCHAR(500) NOT NULL COMMENT '视频URL',
    cover_image VARCHAR(500) COMMENT '封面图片URL',
    duration INT NOT NULL COMMENT '视频时长（秒）',
    sort_order INT DEFAULT 0 COMMENT '在学习包中的排序',
    verification_question TEXT COMMENT '验证题目（JSON格式）',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (package_id) REFERENCES learning_packages(id) ON DELETE CASCADE,
    INDEX idx_package (package_id),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习视频表';

-- 4. 用户学习进度表
CREATE TABLE IF NOT EXISTS user_learning_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    package_id BIGINT NOT NULL COMMENT '学习包ID',
    video_id BIGINT NOT NULL COMMENT '视频ID',
    watch_duration INT DEFAULT 0 COMMENT '已观看时长（秒）',
    is_completed BOOLEAN DEFAULT FALSE COMMENT '是否完成',
    verification_passed BOOLEAN DEFAULT FALSE COMMENT '验证题是否通过',
    last_watch_position INT DEFAULT 0 COMMENT '最后观看位置（秒）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (package_id) REFERENCES learning_packages(id),
    FOREIGN KEY (video_id) REFERENCES learning_videos(id),
    UNIQUE KEY uk_user_video (user_id, video_id),
    INDEX idx_user_package (user_id, package_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户学习进度表';

-- 5. 测试题库表
CREATE TABLE IF NOT EXISTS test_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    package_id BIGINT NOT NULL COMMENT '所属学习包ID',
    question_type VARCHAR(20) NOT NULL COMMENT '题型：SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE',
    question_text TEXT NOT NULL COMMENT '题目内容',
    options TEXT COMMENT '选项（JSON格式）',
    correct_answer VARCHAR(100) NOT NULL COMMENT '正确答案',
    explanation TEXT COMMENT '答案解析',
    score INT DEFAULT 1 COMMENT '分值',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (package_id) REFERENCES learning_packages(id) ON DELETE CASCADE,
    INDEX idx_package (package_id),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试题库表';

-- 6. 用户测试记录表
CREATE TABLE IF NOT EXISTS user_test_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    package_id BIGINT NOT NULL COMMENT '学习包ID',
    total_questions INT NOT NULL COMMENT '总题数',
    correct_count INT DEFAULT 0 COMMENT '正确题数',
    score INT DEFAULT 0 COMMENT '得分',
    total_score INT NOT NULL COMMENT '总分',
    accuracy DECIMAL(5,2) COMMENT '正确率',
    time_spent INT COMMENT '答题用时（秒）',
    answers TEXT COMMENT '答题详情（JSON格式）',
    status VARCHAR(20) DEFAULT 'COMPLETED' COMMENT '状态：IN_PROGRESS, COMPLETED, TIMEOUT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (package_id) REFERENCES learning_packages(id),
    INDEX idx_user (user_id),
    INDEX idx_package (package_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户测试记录表';

-- 7. 心理状态评估报告表
CREATE TABLE IF NOT EXISTS psychological_assessments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    package_id BIGINT NOT NULL COMMENT '学习包ID',
    test_record_id BIGINT NOT NULL COMMENT '测试记录ID',
    consultation_record_id BIGINT COMMENT '关联的咨询记录ID',
    knowledge_mastery TEXT COMMENT '知识掌握情况（JSON格式）',
    psychological_state TEXT COMMENT '心理状态评估',
    improvement_analysis TEXT COMMENT '改善分析',
    recommendations TEXT COMMENT '建议',
    weak_points TEXT COMMENT '薄弱环节',
    report_data TEXT COMMENT '完整报告数据（JSON格式）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (package_id) REFERENCES learning_packages(id),
    FOREIGN KEY (test_record_id) REFERENCES user_test_records(id),
    FOREIGN KEY (consultation_record_id) REFERENCES consultation_records(id),
    INDEX idx_user (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心理状态评估报告表';

-- 8. 用户推荐学习包表
CREATE TABLE IF NOT EXISTS user_recommended_packages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    package_id BIGINT NOT NULL COMMENT '学习包ID',
    consultation_record_id BIGINT COMMENT '触发推荐的咨询记录ID',
    reason TEXT COMMENT '推荐理由',
    is_viewed BOOLEAN DEFAULT FALSE COMMENT '是否已查看',
    is_started BOOLEAN DEFAULT FALSE COMMENT '是否已开始学习',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (package_id) REFERENCES learning_packages(id),
    FOREIGN KEY (consultation_record_id) REFERENCES consultation_records(id),
    UNIQUE KEY uk_user_package (user_id, package_id, consultation_record_id),
    INDEX idx_user (user_id),
    INDEX idx_is_viewed (is_viewed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户推荐学习包表';

-- 9. 咨询师资质证书表
CREATE TABLE IF NOT EXISTS consultant_certificates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consultant_id BIGINT NOT NULL COMMENT '咨询师ID',
    certificate_name VARCHAR(100) NOT NULL COMMENT '证书名称',
    certificate_no VARCHAR(100) COMMENT '证书编号',
    issuing_authority VARCHAR(100) COMMENT '颁发机构',
    issue_date DATE COMMENT '颁发日期',
    certificate_image VARCHAR(500) COMMENT '证书图片URL',
    is_verified BOOLEAN DEFAULT FALSE COMMENT '是否已验证',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (consultant_id) REFERENCES consultants(id) ON DELETE CASCADE,
    INDEX idx_consultant (consultant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询师资质证书表';

-- 10. 咨询师案例表
CREATE TABLE IF NOT EXISTS consultant_cases (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consultant_id BIGINT NOT NULL COMMENT '咨询师ID',
    case_title VARCHAR(200) NOT NULL COMMENT '案例标题',
    case_description TEXT COMMENT '案例描述',
    case_category VARCHAR(50) COMMENT '案例分类',
    case_result TEXT COMMENT '案例结果',
    is_public BOOLEAN DEFAULT TRUE COMMENT '是否公开',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (consultant_id) REFERENCES consultants(id) ON DELETE CASCADE,
    INDEX idx_consultant (consultant_id),
    INDEX idx_is_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询师案例表';

-- 11. 咨询师服务价格表
CREATE TABLE IF NOT EXISTS consultant_pricing (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consultant_id BIGINT NOT NULL COMMENT '咨询师ID',
    service_type VARCHAR(50) NOT NULL COMMENT '服务类型：ONLINE_TEXT, ONLINE_VOICE, ONLINE_VIDEO, OFFLINE',
    duration INT NOT NULL COMMENT '时长（分钟）',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (consultant_id) REFERENCES consultants(id) ON DELETE CASCADE,
    UNIQUE KEY uk_consultant_service (consultant_id, service_type, duration),
    INDEX idx_consultant (consultant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询师服务价格表';

-- 插入示例数据

-- 示例学习包
INSERT INTO learning_packages (title, category, description, issue_tags, video_count, total_duration) VALUES
('职场压力缓解技巧', '职场心理', '帮助您有效应对职场压力，提升工作幸福感', '职场压力,工作焦虑,职业倦怠', 5, 45),
('情绪调节实用方法', '情绪调节', '学习科学的情绪管理方法，提升情绪调节能力', '情绪管理,焦虑,抑郁', 4, 35),
('亲子沟通艺术', '亲子教育', '改善亲子关系，建立良好的沟通模式', '亲子关系,沟通障碍,教育焦虑', 5, 50),
('人际关系处理技巧', '人际沟通', '提升人际交往能力，建立和谐人际关系', '人际关系,社交焦虑,沟通技巧', 4, 40);

-- 示例学习视频（为第一个学习包）
INSERT INTO learning_videos (package_id, title, description, video_url, duration, sort_order, verification_question) VALUES
(1, '认识职场压力', '了解职场压力的来源和表现形式', 'https://example.com/video1.mp4', 600, 1, '{"question":"职场压力的主要来源不包括以下哪项？","options":["工作任务繁重","人际关系复杂","充足的休息时间","职业发展焦虑"],"answer":"C"}'),
(1, '压力应对策略', '学习有效的压力应对方法', 'https://example.com/video2.mp4', 540, 2, '{"question":"以下哪种方法不适合缓解压力？","options":["适度运动","深呼吸放松","长期熬夜","与人倾诉"],"answer":"C"}'),
(1, '时间管理技巧', '掌握高效的时间管理方法', 'https://example.com/video3.mp4', 480, 3, '{"question":"时间管理的核心原则是什么？","options":["做更多的事","优先处理重要紧急的事","同时处理多项任务","延长工作时间"],"answer":"B"}');

-- 示例测试题（为第一个学习包）
INSERT INTO test_questions (package_id, question_type, question_text, options, correct_answer, explanation, score, sort_order) VALUES
(1, 'SINGLE_CHOICE', '以下哪项不是职场压力的常见表现？', '["焦虑不安","失眠多梦","精力充沛","注意力不集中"]', 'C', '精力充沛是健康状态的表现，不是压力的表现', 1, 1),
(1, 'MULTIPLE_CHOICE', '有效缓解职场压力的方法包括（多选）', '["规律运动","合理安排工作","寻求社会支持","过度饮酒"]', 'A,B,C', '规律运动、合理安排工作和寻求社会支持都是健康的压力缓解方法', 2, 2),
(1, 'TRUE_FALSE', '长期的职场压力不会对身体健康造成影响', '["正确","错误"]', 'B', '长期压力会对身心健康造成严重影响', 1, 3);
