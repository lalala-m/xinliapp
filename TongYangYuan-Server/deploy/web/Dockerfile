# TongYangYuan 前端 Nginx Dockerfile
FROM nginx:alpine

# 删除默认配置
RUN rm -rf /etc/nginx/conf.d/default.conf /usr/share/nginx/html/*

# 复制前端构建文件
COPY dist/ /usr/share/nginx/html/

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 创建上传目录
RUN mkdir -p /usr/share/nginx/html/uploads

# 暴露端口
EXPOSE 80 443

# Nginx 前台运行
CMD ["nginx", "-g", "daemon off;"]
