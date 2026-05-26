import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# 创建管理员账号SQL
sql = '''
USE mental_health_db;

-- 删除已存在的admin账号
DELETE FROM users WHERE phone = 'admin';

-- 插入管理员账号（密码123456，已BCrypt加密）
INSERT INTO users (phone, password, user_type, nickname, status, is_vip, vip_expire_time, gmt_create, gmt_modified) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'ADMIN', '管理员', 'ACTIVE', false, NULL, NOW(), NOW());

-- 验证插入
SELECT id, phone, user_type, nickname, status FROM users WHERE phone = 'admin';
'''

with open('/tmp/create_admin.sql', 'w', encoding='utf-8') as f:
    f.write(sql)

sftp = ssh.open_sftp()
sftp.put('/tmp/create_admin.sql', '/tmp/create_admin.sql')
sftp.close()

stdin, stdout, stderr = ssh.exec_command('mysql -u root -pTongyuanDB2024! < /tmp/create_admin.sql 2>&1')
result = stdout.read().decode('utf-8', errors='replace')
error = stderr.read().decode('utf-8', errors='replace')
print('执行结果:', result[:500] if result else '完成')
if error: print('错误:', error[:500])

ssh.close()
print('管理员账号创建完成')
