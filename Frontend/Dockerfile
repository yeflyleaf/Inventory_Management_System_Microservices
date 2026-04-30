# Stage 1: Build the Vue application
FROM node:20-alpine as build-stage
WORKDIR /app
# 复制 package.json 进行依赖安装
COPY package*.json ./
RUN npm install
# 复制源码并编译
COPY . .
RUN npm run build

# Stage 2: Serve the application with Nginx
FROM nginx:alpine
# 从编译阶段拷贝静态资源到 Nginx 目录
COPY --from=build-stage /app/dist /usr/share/nginx/html
# 拷贝 K8s 专用的 Nginx 配置文件
COPY nginx-k8s.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
