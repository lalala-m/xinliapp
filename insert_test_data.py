import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# 插入测试咨询师
sql_consultants = '''
USE mental_health_db;

-- 先创建用户（咨询师账号）
INSERT INTO users (phone, password, user_type, nickname, status, is_vip, vip_expire_time, gmt_create, gmt_modified) VALUES
('13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'CONSULTANT', '张医生', 'ACTIVE', false, NULL, NOW(), NOW()),
('13800138002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'CONSULTANT', '李医生', 'ACTIVE', false, NULL, NOW(), NOW()),
('13800138003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'CONSULTANT', '王医生', 'ACTIVE', false, NULL, NOW(), NOW()),
('13800138004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'CONSULTANT', '刘医生', 'ACTIVE', false, NULL, NOW(), NOW()),
('13800138005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'CONSULTANT', '陈医生', 'ACTIVE', false, NULL, NOW(), NOW());

-- 获取刚插入的用户ID
SET @uid1 = LAST_INSERT_ID();
SET @uid2 = @uid1 + 1;
SET @uid3 = @uid1 + 2;
SET @uid4 = @uid1 + 3;
SET @uid5 = @uid1 + 4;

-- 插入咨询师信息
INSERT INTO consultants (user_id, name, title, specialty, intro, is_available, rating, served_count, avatar_color, is_deleted, gmt_create, gmt_modified) VALUES
(@uid1, '张医生', '国家二级心理咨询师', '儿童心理、学习障碍、注意力训练', '拥有15年儿童心理咨询经验，擅长处理儿童学习困难和注意力问题，帮助超过1000个家庭解决教育难题。', true, 4.9, 328, '#6FA6F8', 0, NOW(), NOW()),
(@uid2, '李医生', '高级心理咨询师', '青少年心理、情绪管理、家庭关系', '专注于青少年心理健康领域，擅长情绪管理和家庭关系调适，累计咨询时长超过5000小时。', true, 4.8, 256, '#FF8A65', 0, NOW(), NOW()),
(@uid3, '王医生', '注册心理师', '焦虑抑郁、心理创伤、睡眠障碍', '临床心理学博士，擅长认知行为疗法，对焦虑抑郁等情绪问题有丰富治疗经验。', true, 4.7, 189, '#81C784', 0, NOW(), NOW()),
(@uid4, '刘医生', '儿童发展心理学专家', '自闭症谱系、社交障碍、语言发展', '儿童发展心理学硕士，专注于自闭症和社交障碍的早期干预，帮助儿童提升社交能力。', true, 4.9, 145, '#BA68C8', 0, NOW(), NOW()),
(@uid5, '陈医生', '家庭治疗师', '亲子关系、家庭教育、婚姻咨询', '家庭治疗方向专家，擅长解决亲子冲突和家庭教育问题，促进家庭和谐。', true, 4.6, 412, '#FFD54F', 0, NOW(), NOW());

-- 获取刚插入的咨询师ID
SET @cid1 = LAST_INSERT_ID();
SET @cid2 = @cid1 + 1;
SET @cid3 = @cid1 + 2;
SET @cid4 = @cid1 + 3;
SET @cid5 = @cid1 + 4;

-- 插入咨询师标签关联
INSERT INTO consultant_specialties (consultant_id, tag_id) VALUES
(@cid1, 1), (@cid1, 2), (@cid1, 10), (@cid1, 15),  -- 张医生：抑郁、焦虑、学习障碍、注意力
(@cid2, 2), (@cid2, 3), (@cid2, 20), (@cid2, 25),  -- 李医生：焦虑、创伤、情绪管理、家庭关系
(@cid3, 1), (@cid3, 2), (@cid3, 4), (@cid3, 30),    -- 王医生：抑郁、焦虑、双相、睡眠障碍
(@cid4, 5), (@cid4, 10), (@cid4, 35), (@cid4, 40),  -- 刘医生：强迫、学习障碍、自闭症、社交障碍
(@cid5, 20), (@cid5, 25), (@cid5, 45), (@cid5, 50); -- 陈医生：情绪管理、家庭关系、亲子教育、婚姻咨询
'''

# 写入SQL文件并执行
with open('/tmp/insert_consultants.sql', 'w', encoding='utf-8') as f:
    f.write(sql_consultants)

sftp = ssh.open_sftp()
sftp.put('/tmp/insert_consultants.sql', '/tmp/insert_consultants.sql')
sftp.close()

stdin, stdout, stderr = ssh.exec_command('mysql -u root -pTongyuanDB2024! < /tmp/insert_consultants.sql 2>&1')
result = stdout.read().decode('utf-8', errors='replace')
error = stderr.read().decode('utf-8', errors='replace')
print('执行结果:', result[:500] if result else '完成')
if error: print('错误:', error[:500])

# 验证数据
stdin, stdout, stderr = ssh.exec_command('mysql -u root -pTongyuanDB2024! -e "SELECT COUNT(*) FROM mental_health_db.consultants;"')
print('咨询师数量:', stdout.read().decode('utf-8', errors='replace'))

stdin, stdout, stderr = ssh.exec_command('mysql -u root -pTongyuanDB2024! -e "SELECT COUNT(*) FROM mental_health_db.consultant_specialties;"')
print('标签关联数量:', stdout.read().decode('utf-8', errors='replace'))

ssh.close()
print('测试数据插入完成')
