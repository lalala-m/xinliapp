import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# Check if admin exists
stdin, stdout, stderr = ssh.exec_command(
    "mysql -u root -p'TongyuanDB2024!' mental_health_db -e \"SELECT id, phone, nickname, user_type, status FROM users WHERE phone='admin';\""
)
result = stdout.read().decode()
print("Current admin user:", result)

# If not exists, create admin user with BCrypt password for '123456'
if "admin" not in result:
    print("Creating admin user...")
    # BCrypt hash for '123456'
    password_hash = "$2a$10$2TQ8DoQfj4GZJcqPqwmXOedksmHksRsaE3/kUkk5sC4WXsPmyLv5y"
    cmd = f"""mysql -u root -p'TongyuanDB2024!' mental_health_db -e "INSERT INTO users (phone, password, nickname, user_type, status, gmt_create, gmt_modified) VALUES ('admin', '{password_hash}', '管理员', 'ADMIN', 'ACTIVE', NOW(), NOW());" """
    stdin, stdout, stderr = ssh.exec_command(cmd)
    print("Insert result:", stdout.read().decode())
    print("Insert error:", stderr.read().decode())
else:
    # Update password
    print("Updating admin password...")
    password_hash = "$2a$10$2TQ8DoQfj4GZJcqPqwmXOedksmHksRsaE3/kUkk5sC4WXsPmyLv5y"
    cmd = f"""mysql -u root -p'TongyuanDB2024!' mental_health_db -e "UPDATE users SET password='{password_hash}' WHERE phone='admin';" """
    stdin, stdout, stderr = ssh.exec_command(cmd)
    print("Update result:", stdout.read().decode())
    print("Update error:", stderr.read().decode())

# Verify
stdin, stdout, stderr = ssh.exec_command(
    "mysql -u root -p'TongyuanDB2024!' mental_health_db -e \"SELECT id, phone, nickname, user_type, status, LENGTH(password) as pwd_len FROM users WHERE phone='admin';\""
)
print("\nAfter fix:", stdout.read().decode())

ssh.close()
