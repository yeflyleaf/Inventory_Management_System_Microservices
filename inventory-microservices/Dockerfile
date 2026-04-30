# 阶段 1: 构建阶段 (可选，如果本地已 mvn install 则直接使用阶段 2)
# FROM maven:3.8.4-openjdk-17-slim AS build
# WORKDIR /app
# COPY . .
# RUN mvn clean package -DskipTests

# 阶段 2: 运行阶段
FROM eclipse-temurin:17-jre-alpine

# 作者信息
LABEL maintainer="antigravity@google.com"

# 设置工作目录
WORKDIR /app

# 设置时区为亚洲/上海
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 参数化 JAR 文件路径 (由 build args 传入)
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# JVM 参数设置，可以根据微服务内存情况调整
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 暴露端口 (实际由 application.yml 决定)
# EXPOSE 8080

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

# --- 构建示例指令 ---
# docker build --build-arg JAR_FILE=user-service/target/*.jar -t user-service:1.0 .
