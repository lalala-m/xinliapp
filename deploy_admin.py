import paramiko
import os

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)
print('SSH连接成功')

# 创建admin目录
ssh.exec_command('mkdir -p /opt/tongyangyuan/web/admin')

# 上传admin文件
admin_dir = r'D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Web\admin'
sftp = ssh.open_sftp()

for item in os.listdir(admin_dir):
    local_path = os.path.join(admin_dir, item)
    remote_path = f'/opt/tongyangyuan/web/admin/{item}'
    if os.path.isfile(local_path):
        sftp.put(local_path, remote_path)
        print(f'上传: admin/{item}')

sftp.close()

# 检查Nginx配置，确保admin目录可访问
stdin, stdout, stderr = ssh.exec_command('cat /etc/nginx/sites-available/default | grep -A5 "location /admin"')
result = stdout.read().decode('utf-8', errors='replace')
if not result.strip():
    print('需要添加admin目录配置')
    # admin目录在location /下已经可以通过root指令访问，不需要额外配置

# 测试访问
stdin, stdout, stderr = ssh.exec_command('curl -s http://localhost/admin/login.html | head -5')
print('访问测试:', stdout.read().decode('utf-8', errors='replace')[:200])

ssh.close()
print('管理后台部署完成')
