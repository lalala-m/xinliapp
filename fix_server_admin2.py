import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# Use single quotes around the hash to prevent $ expansion
password_hash = "$2a$10$2TQ8DoQfj4GZJcqPqwmXOedksmHksRsaE3/kUkk5sC4WXsPmyLv5y"

# Write SQL to a file first
sql = f"UPDATE users SET password='{password_hash}' WHERE phone='admin';"
stdin, stdout, stderr = ssh.exec_command(f"echo '{sql}' > /tmp/fix_admin.sql")
print("Write SQL:", stdout.read().decode(), stderr.read().decode())

# Execute SQL from file
stdin, stdout, stderr = ssh.exec_command("mysql -u root -p'TongyuanDB2024!' mental_health_db < /tmp/fix_admin.sql")
print("Update result:", stdout.read().decode())
print("Update error:", stderr.read().decode())

# Verify
stdin, stdout, stderr = ssh.exec_command(
    "mysql -u root -p'TongyuanDB2024!' mental_health_db -e \"SELECT id, phone, nickname, user_type, status, LENGTH(password) as pwd_len FROM users WHERE phone='admin';\""
)
print("After fix:", stdout.read().decode())

ssh.close()
