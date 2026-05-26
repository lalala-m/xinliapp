import paramiko
import json

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# Test login
cmd = "curl -s -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{\"phone\":\"admin\",\"password\":\"123456\"}'"
stdin, stdout, stderr = ssh.exec_command(cmd)
response = stdout.read().decode()
error = stderr.read().decode()

print("RESPONSE:", response)
if error:
    print("ERROR:", error)

# Check if app is running
stdin2, stdout2, stderr2 = ssh.exec_command("ps aux | grep java | grep -v grep")
print("\nJAVA PROCESS:", stdout2.read().decode())

ssh.close()
