# TongYangYuan Backend Spring Boot Dockerfile
FROM openjdk:17-jdk

WORKDIR /app

# Set timezone
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'Asia/Shanghai' > /etc/timezone && \
    mkdir -p /app/logs /app/uploads

# Copy JAR file
COPY mental-health-server-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
