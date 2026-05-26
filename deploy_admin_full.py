import paramiko
import os

LOCAL_ADMIN = r"D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Web\admin"
REMOTE_WEB = "/opt/tongyangyuan/web"
REMOTE_ADMIN = f"{REMOTE_WEB}/admin"

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

sftp = ssh.open_sftp()

# Create remote directories
for subdir in ['css', 'js']:
    try:
        sftp.mkdir(f"{REMOTE_ADMIN}/{subdir}")
        print(f"Created: {REMOTE_ADMIN}/{subdir}")
    except IOError:
        print(f"Exists: {REMOTE_ADMIN}/{subdir}")

# Upload all files
count = 0
for root, dirs, files in os.walk(LOCAL_ADMIN):
    for file in files:
        local_path = os.path.join(root, file)
        rel_path = os.path.relpath(local_path, LOCAL_ADMIN).replace("\\", "/")
        remote_path = f"{REMOTE_ADMIN}/{rel_path}"
        sftp.put(local_path, remote_path)
        count += 1
        print(f"Uploaded: {rel_path}")

sftp.close()

# Verify
stdin, stdout, stderr = ssh.exec_command(f"find {REMOTE_ADMIN} -type f | wc -l")
remote_count = stdout.read().decode().strip()
print(f"\nTotal files on server: {remote_count}")

# List css files
stdin, stdout, stderr = ssh.exec_command(f"ls -la {REMOTE_ADMIN}/css/")
print("CSS files:", stdout.read().decode())

# Restart nginx
stdin, stdout, stderr = ssh.exec_command("nginx -s reload 2>/dev/null || service nginx reload")
print("Nginx reload:", stdout.read().decode())

ssh.close()
print(f"\nDone! Uploaded {count} files.")
print(f"Admin URL: http://139.196.5.153/admin/login.html")
