import paramiko
import os

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)
print('SSH连接成功')

# 创建Web目录
ssh.exec_command('mkdir -p /opt/tongyangyuan/web')

# 上传Web文件
web_dir = r'D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Web'
sftp = ssh.open_sftp()

# 上传所有HTML文件
for item in os.listdir(web_dir):
    local_path = os.path.join(web_dir, item)
    remote_path = f'/opt/tongyangyuan/web/{item}'
    if os.path.isfile(local_path):
        sftp.put(local_path, remote_path)
        print(f'上传: {item}')
    elif os.path.isdir(local_path) and item in ['css', 'js', 'images', 'admin']:
        ssh.exec_command(f'mkdir -p {remote_path}')
        for subitem in os.listdir(local_path):
            sub_local = os.path.join(local_path, subitem)
            sub_remote = f'{remote_path}/{subitem}'
            if os.path.isfile(sub_local):
                sftp.put(sub_local, sub_remote)
        print(f'上传目录: {item}')

sftp.close()

# 配置Nginx
nginx_conf = '''server {
    listen 80;
    server_name 139.196.5.153;
    
    location / {
        root /opt/tongyangyuan/web;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
'''

with open('/tmp/nginx_default', 'w') as f:
    f.write(nginx_conf)

sftp = ssh.open_sftp()
sftp.put('/tmp/nginx_default', '/etc/nginx/sites-available/default')
sftp.close()

# 重启Nginx
ssh.exec_command('systemctl restart nginx')
print('Nginx配置完成')

ssh.close()
print('Web部署完成')
