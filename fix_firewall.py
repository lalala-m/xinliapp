import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# 开放80端口
cmds = [
    'ufw allow 80',
    'ufw status',
]

for cmd in cmds:
    stdin, stdout, stderr = ssh.exec_command(cmd)
    print(stdout.read().decode('utf-8', errors='replace'))

ssh.close()
print('防火墙修复完成')
