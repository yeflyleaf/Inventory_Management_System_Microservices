# ============================================================================
# 进销存管理系统 - Frontend Dockerfile (K8s 部署版)
# ============================================================================

# Stage 1: Build the Vue application
FROM node:22-alpine AS build-stage

WORKDIR /app

# 复制 package.json 进行依赖安装 (利用 Docker 层缓存)
COPY package*.json ./
RUN npm ci

# 复制源码并编译
COPY . .
RUN npm run build

# Stage 2: Serve the application with Nginx
FROM nginx:stable-alpine

# 安装 curl 用于 healthcheck
RUN apk add --no-cache curl tzdata

ENV TZ=Asia/Shanghai

# 清理默认 Nginx 配置
RUN rm -rf /usr/share/nginx/html/* /etc/nginx/conf.d/*

# 从编译阶段拷贝静态资源到 Nginx 目录
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 拷贝 K8s 专用的 Nginx 配置文件
COPY nginx-k8s.conf /etc/nginx/conf.d/default.conf

# 权限加固
RUN chmod -R 755 /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:80/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
