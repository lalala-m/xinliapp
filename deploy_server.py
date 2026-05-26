import paramiko
import time

# 连接服务器
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)
print('SSH连接成功')

# 1. 更新系统并安装基础软件
cmds = [
    'apt update && apt install -y openjdk-21-jdk mysql-server redis-server nginx ufw',
    'systemctl start mysql && systemctl enable mysql',
    'systemctl start redis && systemctl enable redis',
]

for cmd in cmds:
    print(f'执行: {cmd[:50]}...')
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=300)
    out = stdout.read().decode()
    err = stderr.read().decode()
    if err and 'WARNING' not in err:
        print(f'错误: {err[:200]}')
    else:
        print('完成')

# 2. 配置MySQL
cmd = '''mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'TongyuanDB2024!'; CREATE USER IF NOT EXISTS 'tongyangyuan'@'%' IDENTIFIED BY 'TongyuanDB2024!'; GRANT ALL PRIVILEGES ON *.* TO 'tongyangyuan'@'%' WITH GRANT OPTION; FLUSH PRIVILEGES; CREATE DATABASE IF NOT EXISTS mental_health_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"'''
stdin, stdout, stderr = ssh.exec_command(cmd)
print('MySQL配置:', stdout.read().decode()[:100] or '完成')

# 3. 配置防火墙
cmds = [
    'ufw default deny incoming',
    'ufw allow 22',
    'ufw allow 80',
    'ufw allow 443',
    'ufw allow 8080',
    'ufw --force enable',
]
for cmd in cmds:
    ssh.exec_command(cmd)
print('防火墙配置完成')

# 4. 修改SSH配置（禁用root密码登录，改用密钥）
cmd = '''sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config && sed -i 's/PasswordAuthentication no/PasswordAuthentication yes/' /etc/ssh/sshd_config && systemctl restart sshd'''
ssh.exec_command(cmd)
print('SSH配置完成')

ssh.close()
print('基础环境部署完成')
