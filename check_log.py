import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

stdin, stdout, stderr = ssh.exec_command('tail -50 /opt/tongyangyuan/app.log')
result = stdout.read().decode('utf-8', errors='replace')
# 只打印ASCII字符，避免编码问题
safe_result = ''.join(c if ord(c) < 128 else '?' for c in result[:1000])
print(safe_result)

ssh.close()
