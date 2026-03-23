# TongYangYuan 后端 Spring Boot Dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

# 设置时区为上海
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'Asia/Shanghai' > /etc/timezone

# 安装字体（避免中文乱码）和必要的工具
RUN apt-get update && apt-get install -y \
    fontconfig \
    fonts-noto-cjk \
    curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /app/logs /app/uploads

# 复制 JAR 文件
COPY mental-health-server-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# JVM 优化参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
