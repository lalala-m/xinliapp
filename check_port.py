import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# 检查防火墙
stdin, stdout, stderr = ssh.exec_command('ufw status')
print('防火墙状态:')
print(stdout.read().decode('utf-8', errors='replace'))

# 检查端口监听
stdin, stdout, stderr = ssh.exec_command('netstat -tlnp | grep -E "80|8080"')
print('端口监听:')
print(stdout.read().decode('utf-8', errors='replace'))

# 本地测试80
stdin, stdout, stderr = ssh.exec_command('curl -s http://localhost/ | head -3')
print('本地访问80:')
print(stdout.read().decode('utf-8', errors='replace'))

ssh.close()
