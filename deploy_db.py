import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)
print('SSH连接成功')

# 上传数据库文件
sftp = ssh.open_sftp()
sftp.put(r'D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Server\database_backup.sql', '/tmp/database_backup.sql')
print('数据库文件上传成功')
sftp.close()

# 导入数据库
cmd = 'mysql -u root -pTongyuanDB2024! mental_health_db < /tmp/database_backup.sql 2>&1'
stdin, stdout, stderr = ssh.exec_command(cmd, timeout=120)
out = stdout.read().decode('utf-8', errors='replace')
err = stderr.read().decode('utf-8', errors='replace')
if out: print('导入输出:', out[:500])
if err: print('导入错误:', err[:500])
if not out and not err: print('数据库导入成功')

# 验证
stdin, stdout, stderr = ssh.exec_command('mysql -u root -pTongyuanDB2024! -e "USE mental_health_db; SHOW TABLES;" | head -20')
print('数据库表:', stdout.read().decode('utf-8', errors='replace')[:500])

ssh.close()
