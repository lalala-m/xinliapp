import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# 检查Web目录内容
stdin, stdout, stderr = ssh.exec_command('ls -la /opt/tongyangyuan/web/')
print('Web目录内容:')
print(stdout.read().decode('utf-8', errors='replace'))

# 把login.html复制为index.html
stdin, stdout, stderr = ssh.exec_command('cp /opt/tongyangyuan/web/login.html /opt/tongyangyuan/web/index.html')
print('创建index.html:', stdout.read().decode('utf-8', errors='replace') or '完成')

# 重启Nginx
ssh.exec_command('systemctl restart nginx')
print('Nginx重启完成')

# 测试
stdin, stdout, stderr = ssh.exec_command('curl -s http://localhost/ | head -5')
print('访问测试:', stdout.read().decode('utf-8', errors='replace')[:300])

ssh.close()
