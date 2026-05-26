import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)

queries = [
    ("咨询师数量", "SELECT COUNT(*) FROM mental_health_db.consultants"),
    ("标签数量", "SELECT COUNT(*) FROM mental_health_db.specialty_tags"),
    ("咨询师标签关联", "SELECT COUNT(*) FROM mental_health_db.consultant_specialties"),
    ("咨询师列表", "SELECT id, name, user_id, is_available FROM mental_health_db.consultants LIMIT 5"),
    ("标签列表", "SELECT id, name FROM mental_health_db.specialty_tags LIMIT 10"),
]

for title, sql in queries:
    cmd = f'mysql -u root -p123456 -e "{sql}"'
    stdin, stdout, stderr = ssh.exec_command(cmd)
    result = stdout.read().decode('utf-8', errors='replace')
    print(f'{title}: {result}')

ssh.close()
