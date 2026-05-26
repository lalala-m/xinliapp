import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# 上传新的register.html
sftp = ssh.open_sftp()
sftp.put(r'D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Web\register.html', '/opt/tongyangyuan/web/register.html')
print('register.html更新成功')
sftp.close()

ssh.close()
print('Web更新完成')
