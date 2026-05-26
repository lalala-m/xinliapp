import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# 重新设置密码为123456（使用正确的BCrypt）
sql = '''USE mental_health_db;
UPDATE users SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi' WHERE phone = 'admin';
SELECT id, phone, user_type, status FROM users WHERE phone = 'admin';'''

with open('/tmp/fix_admin.sql', 'w', encoding='utf-8') as f:
    f.write(sql)

sftp = ssh.open_sftp()
sftp.put('/tmp/fix_admin.sql', '/tmp/fix_admin.sql')
sftp.close()

stdin, stdout, stderr = ssh.exec_command('mysql -u root -pTongyuanDB2024! < /tmp/fix_admin.sql 2>&1')
result = stdout.read().decode('utf-8', errors='replace')
print('修复结果:', result[:500])

ssh.close()
