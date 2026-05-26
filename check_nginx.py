import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# 检查Nginx配置
stdin, stdout, stderr = ssh.exec_command('cat /etc/nginx/sites-available/default')
print('Nginx配置:')
print(stdout.read().decode('utf-8', errors='replace'))

# 检查是否有index.html
stdin, stdout, stderr = ssh.exec_command('ls -la /opt/tongyangyuan/web/index.html')
print('index文件:')
print(stdout.read().decode('utf-8', errors='replace'))

# 检查Nginx错误日志
stdin, stdout, stderr = ssh.exec_command('tail -5 /var/log/nginx/error.log')
print('错误日志:')
print(stdout.read().decode('utf-8', errors='replace')[:500])

ssh.close()
