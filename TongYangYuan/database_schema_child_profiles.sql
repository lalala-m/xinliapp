-- 孩子档案表
CREATE TABLE child_profiles (
    id VARCHAR(100) PRIMARY KEY,
    user_phone VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    birth_date VARCHAR(20),
    ethnicity VARCHAR(50),
    native_place VARCHAR(100),
    family_rank VARCHAR(50),
    birth_place VARCHAR(100),
    language_env VARCHAR(200),
    school VARCHAR(200),
    home_address VARCHAR(500),
    interests TEXT,
    activities TEXT,
    body_status VARCHAR(50),
    body_status_detail TEXT,
    medical_history TEXT,
    medical_history_other TEXT,
    father_phone VARCHAR(20),
    mother_phone VARCHAR(20),
    guardian_phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_phone (user_phone)
);

-- 示例数据
INSERT INTO child_profiles (
    id, user_phone, name, gender, birth_date, ethnicity, native_place,
    family_rank, birth_place, language_env, school, home_address,
    interests, activities, body_status, body_status_detail,
    medical_history, medical_history_other,
    father_phone, mother_phone, guardian_phone
) VALUES (
    'child-001', '13800000000', '小明', 'BOY', '2015-06-15', '汉族', '北京市',
    '独生子女', '北京市朝阳区', '普通话', '朝阳区实验小学', '北京市朝阳区某某小区',
    '画画,阅读,运动', '每周游泳两次', '良好', '',
    '无', '',
    '13800000001', '13800000002', '13800000001'
);
