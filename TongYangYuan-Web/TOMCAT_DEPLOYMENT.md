# 童养园咨询师端 - Tomcat部署指南

## 部署方式

### 方式一：直接部署到webapps目录

1. **复制项目文件**
   ```
   将整个 TongYangYuan-Web 文件夹复制到 Tomcat 的 webapps 目录下
   例如：C:\apache-tomcat-9.0.xx\webapps\TongYangYuan-Web
   ```

2. **启动Tomcat**
   ```bash
   # Windows
   cd C:\apache-tomcat-9.0.xx\bin
   startup.bat

   # Linux/Mac
   cd /path/to/tomcat/bin
   ./startup.sh
   ```

3. **访问应用**
   ```
   http://localhost:8080/TongYangYuan-Web/
   ```

### 方式二：打包为WAR文件部署

1. **创建WAR包结构**
   ```
   TongYangYuan-Web.war
   ├── WEB-INF/
   │   └── web.xml
   ├── css/
   ├── js/
   ├── index.html
   ├── dashboard.html
   ├── chat.html
   └── video-call.html
   ```

2. **打包WAR文件**
   ```bash
   # 在项目根目录执行
   jar -cvf TongYangYuan-Web.war *
   ```

3. **部署WAR文件**
   ```
   将 TongYangYuan-Web.war 复制到 Tomcat 的 webapps 目录
   Tomcat会自动解压并部署
   ```

4. **访问应用**
   ```
   http://localhost:8080/TongYangYuan-Web/
   ```

### 方式三：配置虚拟目录

1. **编辑 server.xml**
   ```xml
   在 <Host> 标签内添加：
   <Context path="/consultant" docBase="D:/AllProject/AndroidStudioProjects/TongYangYuan-Web" reloadable="true"/>
   ```

2. **重启Tomcat**

3. **访问应用**
   ```
   http://localhost:8080/consultant/
   ```

## 配置说明

### web.xml配置

已创建 `WEB-INF/web.xml` 文件，包含：
- 欢迎页面配置
- MIME类型映射
- 错误页面配置
- 会话配置

### 端口配置

如需修改Tomcat端口，编辑 `conf/server.xml`：
```xml
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443" />
```

将 `port="8080"` 改为其他端口，如 `port="80"`

## 测试部署

1. **启动Tomcat后检查日志**
   ```
   查看 logs/catalina.out (Linux/Mac)
   或 logs/catalina.yyyy-mm-dd.log (Windows)
   ```

2. **访问测试**
   ```
   http://localhost:8080/TongYangYuan-Web/
   ```

3. **登录测试**
   - 手机号: 13800000001
   - 密码: 123456

## 常见问题

### 1. 404错误
- 检查项目路径是否正确
- 检查Tomcat是否正常启动
- 检查URL是否正确

### 2. 静态资源加载失败
- 检查MIME类型配置
- 检查文件路径大小写
- 清除浏览器缓存

### 3. LocalStorage不工作
- 确保使用http://或https://访问
- 不要使用file://协议
- 检查浏览器隐私设置

### 4. 视频通话无法使用
- 需要HTTPS协议才能访问摄像头
- 配置SSL证书或使用localhost测试

## 生产环境建议

### 1. 启用HTTPS
```xml
<!-- 在 server.xml 中配置SSL -->
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="150" SSLEnabled="true">
    <SSLHostConfig>
        <Certificate certificateKeystoreFile="conf/keystore.jks"
                     type="RSA" />
    </SSLHostConfig>
</Connector>
```

### 2. 配置GZIP压缩
```xml
<Connector port="8080" protocol="HTTP/1.1"
           compression="on"
           compressionMinSize="2048"
           noCompressionUserAgents="gozilla, traviata"
           compressableMimeType="text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json"/>
```

### 3. 设置缓存策略
在 `web.xml` 中添加：
```xml
<filter>
    <filter-name>CacheControlFilter</filter-name>
    <filter-class>org.apache.catalina.filters.ExpiresFilter</filter-class>
    <init-param>
        <param-name>ExpiresByType text/css</param-name>
        <param-value>access plus 1 month</param-value>
    </init-param>
    <init-param>
        <param-name>ExpiresByType application/javascript</param-name>
        <param-value>access plus 1 month</param-value>
    </init-param>
</filter>
```

### 4. 配置跨域（如需要）
```xml
<filter>
    <filter-name>CorsFilter</filter-name>
    <filter-class>org.apache.catalina.filters.CorsFilter</filter-class>
    <init-param>
        <param-name>cors.allowed.origins</param-name>
        <param-value>*</param-value>
    </init-param>
</filter>
```

## 性能优化

1. **启用连接池**
2. **配置JVM参数**
   ```bash
   JAVA_OPTS="-Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m"
   ```
3. **启用APR连接器**
4. **配置线程池**

## 监控和日志

1. **访问日志**
   ```
   logs/localhost_access_log.yyyy-mm-dd.txt
   ```

2. **应用日志**
   ```
   logs/catalina.out
   ```

3. **错误日志**
   ```
   logs/catalina.yyyy-mm-dd.log
   ```

## 备份和恢复

定期备份：
- webapps/TongYangYuan-Web/ (应用文件)
- conf/ (配置文件)
- logs/ (日志文件，可选)

## 更新部署

1. 停止Tomcat
2. 备份当前版本
3. 替换新版本文件
4. 启动Tomcat
5. 测试功能

## 联系支持

如有问题，请查看：
- Tomcat官方文档: https://tomcat.apache.org/
- 项目README.md
- DEVELOPMENT_PROGRESS.md
