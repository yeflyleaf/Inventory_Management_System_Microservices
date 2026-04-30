# ============================================================================
# 进销存管理系统 - 前端构建与 Nginx 托管 Dockerfile
# ============================================================================

# 阶段 1: frontend-builder - 前端构建阶段
FROM node:22-alpine AS frontend-builder

WORKDIR /app

# 1. 复制 package.json 和 lock 文件
COPY Frontend/package*.json ./

# 2. 安装依赖
RUN npm ci

# 3. 复制前端源码并构建
COPY Frontend/ ./
RUN npm run build

# 阶段 2: frontend - Nginx 托管静态资源
FROM nginx:stable-alpine AS frontend

RUN apk add --no-cache curl tzdata

ENV TZ=Asia/Shanghai

# 清理默认文件
RUN rm -rf /usr/share/nginx/html/* /etc/nginx/conf.d/*

# 复制自定义 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制静态资源
COPY --from=frontend-builder /app/dist/ /usr/share/nginx/html/

# 权限加固
RUN chmod -R 755 /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:80/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
