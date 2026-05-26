import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# BCrypt hash for '123456'
password_hash = "$2a$10$2TQ8DoQfj4GZJcqPqwmXOedksmHksRsaE3/kUkk5sC4WXsPmyLv5y"

# Create SQL file
sql_content = f"""DELETE FROM users WHERE phone='10000000000';
INSERT INTO users (phone, password, nickname, user_type, status, gmt_create, gmt_modified) 
VALUES ('10000000000', '{password_hash}', '超级管理员', 'ADMIN', 'ACTIVE', NOW(), NOW());
"""

stdin, stdout, stderr = ssh.exec_command("cat > /tmp/create_admin.sql << 'EOF'\n" + sql_content + "EOF")
print("Write SQL:", stdout.read().decode(), stderr.read().decode())

# Execute
stdin, stdout, stderr = ssh.exec_command("mysql -u root -p'TongyuanDB2024!' mental_health_db < /tmp/create_admin.sql")
print("Result:", stdout.read().decode())
print("Error:", stderr.read().decode())

# Verify
stdin, stdout, stderr = ssh.exec_command(
    "mysql -u root -p'TongyuanDB2024!' mental_health_db -e \"SELECT id, phone, nickname, user_type, status, LENGTH(password) as pwd_len FROM users WHERE phone='10000000000';\""
)
print("Verify:", stdout.read().decode())

# Test login
stdin, stdout, stderr = ssh.exec_command(
    "curl -s -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{\"phone\":\"10000000000\",\"password\":\"123456\"}'"
)
response = stdout.read().decode()
print("\nLogin test:", response)

ssh.close()
