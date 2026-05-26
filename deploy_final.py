import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# 停止旧应用
ssh.exec_command('pkill -f java')

# 上传新JAR包
sftp = ssh.open_sftp()
sftp.put(r'D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Server\target\mental-health-server-1.0.0.jar', '/opt/tongyangyuan/app.jar')
print('JAR包上传成功')

# 上传新的register.html
sftp.put(r'D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Web\register.html', '/opt/tongyangyuan/web/register.html')
print('register.html上传成功')

sftp.close()

# 启动新应用
ssh.exec_command('cd /opt/tongyangyuan && nohup java -Xms256m -Xmx512m -jar app.jar --spring.profiles.active=prod > app.log 2>&1 &')
print('应用启动中...')

import time
time.sleep(25)

# 检查状态
stdin, stdout, stderr = ssh.exec_command('curl -s http://localhost:8080/api/')
result = stdout.read().decode('utf-8', errors='replace')
print('应用状态:', result[:200] if result else '无响应')

ssh.close()
print('部署完成')
