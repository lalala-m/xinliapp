import paramiko
import os

# APK path
apk_local = r"D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan\app\build\outputs\apk\release\app-release.apk"
apk_remote = "/opt/tongyangyuan/web/tongyangyuan-app.apk"
apk_url = "http://139.196.5.153/tongyangyuan-app.apk"

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# Upload APK
sftp = ssh.open_sftp()
print(f"Uploading APK ({os.path.getsize(apk_local) / 1024 / 1024:.1f} MB)...")
sftp.put(apk_local, apk_remote)
sftp.close()
print("Upload complete!")

# Verify
stdin, stdout, stderr = ssh.exec_command(f"ls -la {apk_remote}")
print("Server file:", stdout.read().decode())

ssh.close()
print(f"\nDownload URL: {apk_url}")
