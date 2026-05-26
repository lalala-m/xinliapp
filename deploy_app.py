import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('139.196.5.153', username='root', password='12345Q_wert', timeout=15)
print('SSH连接成功')

# 创建应用目录
ssh.exec_command('mkdir -p /opt/tongyangyuan')

# 上传JAR包
sftp = ssh.open_sftp()
sftp.put(r'D:\AllProject\AndroidStudioProjects\TYY\TongYangYuan-Server\target\mental-health-server-1.0.0.jar', '/opt/tongyangyuan/app.jar')
print('JAR包上传成功')

# 创建启动脚本
start_script = '''#!/bin/bash
cd /opt/tongyangyuan
nohup java -Xms512m -Xmx1024m -jar app.jar --spring.profiles.active=prod > app.log 2>&1 &
echo $! > app.pid
'''

with open('/tmp/start.sh', 'w') as f:
    f.write(start_script)

sftp.put('/tmp/start.sh', '/opt/tongyangyuan/start.sh')
ssh.exec_command('chmod +x /opt/tongyangyuan/start.sh')
print('启动脚本创建成功')

# 创建生产环境配置
props = '''# 生产环境配置
server.port=8080
server.address=0.0.0.0
server.servlet.context-path=/api

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/mental_health_db?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=TongyuanDB2024!
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Redis配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.timeout=5000ms

# JPA配置 - 生产环境用update自动建表
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# JWT配置
jwt.secret=TongYangYuanMentalHealthSecretKey2026VeryLongSecretKeyForJWTTokenGenerationThatIsLongEnoughForHS256
jwt.expiration=86400000

# CORS配置
cors.allowed-origins=*
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*
cors.allow-credentials=true

# 日志配置
logging.level.root=INFO
logging.level.com.tongyangyuan=INFO
logging.level.org.springframework.web=INFO

# 文件上传
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# 静态资源
spring.web.resources.static-locations=classpath:/static/,file:./uploads/

# 测试模式关闭
app.test-mode=false
app.server-url=http://139.196.5.153:8080
'''

with open('/tmp/application-prod.properties', 'w', encoding='utf-8') as f:
    f.write(props)

sftp.put('/tmp/application-prod.properties', '/opt/tongyangyuan/application-prod.properties')
print('生产配置创建成功')

sftp.close()

# 启动应用
stdin, stdout, stderr = ssh.exec_command('cd /opt/tongyangyuan && ./start.sh && sleep 5 && curl -s http://localhost:8080/api/')
output = stdout.read().decode('utf-8', errors='replace')
print('启动结果:', output[:200])

ssh.close()
print('部署完成')
