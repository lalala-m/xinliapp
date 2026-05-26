import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# 检查应用状态
stdin, stdout, stderr = ssh.exec_command('curl -s http://localhost:8080/api/')
result = stdout.read().decode('utf-8', errors='replace')
print('应用状态:', result[:300] if result else '无响应')

# 检查进程
stdin, stdout, stderr = ssh.exec_command('ps aux | grep java | grep -v grep')
print('Java进程:', stdout.read().decode('utf-8', errors='replace')[:200])

# 检查日志
stdin, stdout, stderr = ssh.exec_command('tail -10 /opt/tongyangyuan/app.log')
print('日志:', stdout.read().decode('utf-8', errors='replace')[:500])

ssh.close()
