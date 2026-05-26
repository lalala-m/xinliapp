import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

# Use heredoc to avoid shell interpretation issues
password_hash = "$2a$10$2TQ8DoQfj4GZJcqPqwmXOedksmHksRsaE3/kUkk5sC4WXsPmyLv5y"

# Create SQL file with proper escaping
sql_content = f"UPDATE users SET password='{password_hash}' WHERE phone='admin';\n"

# Use cat with heredoc
stdin, stdout, stderr = ssh.exec_command("cat > /tmp/fix_admin.sql << 'EOF'\n" + sql_content + "EOF")
print("Write:", stdout.read().decode(), stderr.read().decode())

# Execute
stdin, stdout, stderr = ssh.exec_command("mysql -u root -p'TongyuanDB2024!' mental_health_db < /tmp/fix_admin.sql")
print("Result:", stdout.read().decode())
print("Error:", stderr.read().decode())

# Verify
stdin, stdout, stderr = ssh.exec_command(
    "mysql -u root -p'TongyuanDB2024!' mental_health_db -e \"SELECT LENGTH(password) as pwd_len FROM users WHERE phone='admin';\""
)
print("Verify:", stdout.read().decode())

ssh.close()
