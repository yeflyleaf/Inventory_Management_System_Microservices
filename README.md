<div align="center">

# 进销存管理系统 (微服务重构版)

**基于 Spring Cloud Alibaba + Vue 3 的全栈式、多端部署分布式进销存管理系统**

</div>

<p align="center">
  <!-- Frontend Tech -->
  <a href="https://vuejs.org/"><img src="https://img.shields.io/badge/Framework-Vue%203.5.22-42b883?style=flat-square&logo=vue.js" alt="Framework"></a>
  <a href="https://vite.dev/"><img src="https://img.shields.io/badge/Build-Vite%207.1.11-646cff?style=flat-square&logo=vite" alt="Build"></a>
  <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript"><img src="https://img.shields.io/badge/Language-JavaScript-F7DF1E?style=flat-square&logo=javascript&logoColor=black" alt="Language"></a>
  <a href="https://pinia.vuejs.org/"><img src="https://img.shields.io/badge/State-Pinia%203.0.3-yellow?style=flat-square&logo=pinia" alt="State"></a>
  <a href="https://echarts.apache.org/"><img src="https://img.shields.io/badge/Data%20Vis-ECharts%206.0.0-AA0000?style=flat-square&logo=apache-echarts" alt="ECharts"></a>
  <br>
  <!-- Backend & Cloud -->
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Core-Spring%20Boot%203.3.2-6DB33F?style=flat-square&logo=spring-boot" alt="Spring Boot"></a>
  <a href="https://spring.io/projects/spring-cloud-alibaba"><img src="https://img.shields.io/badge/Cloud-Alibaba%202023.0.1-6DB33F?style=flat-square&logo=spring" alt="SCA"></a>
  <a href="https://mybatis.org/mybatis-3/"><img src="https://img.shields.io/badge/ORM-MyBatis%203.0.5-black?style=flat-square" alt="MyBatis"></a>
  <a href="https://nacos.io/"><img src="https://img.shields.io/badge/Registry-Nacos%202.3.2-34A3DB?style=flat-square" alt="Nacos"></a>
  <a href="https://seata.io/"><img src="https://img.shields.io/badge/TX-Seata%201.8.0-green?style=flat-square" alt="Seata"></a>
  <a href="https://sentinelguard.io/"><img src="https://img.shields.io/badge/Guard-Sentinel%201.8.8-red?style=flat-square" alt="Sentinel"></a>
  <br>
  <!-- Platform & Tools -->
  <a href="https://www.electronjs.org/"><img src="https://img.shields.io/badge/Desktop-Electron%2039.2.7-47848F?style=flat-square&logo=electron" alt="Electron"></a>
  <a href="https://capacitorjs.com/"><img src="https://img.shields.io/badge/Mobile-Capacitor%208.0.0-3DDC84?style=flat-square&logo=android" alt="Capacitor"></a>
  <a href="https://www.docker.com/"><img src="https://img.shields.io/badge/DevOps-Docker%20%2F%20K8s-2496ED?style=flat-square&logo=docker" alt="DevOps"></a>
  <a href="https://github.com/alibaba/easyexcel"><img src="https://img.shields.io/badge/Excel-EasyExcel%203.3.4-blue?style=flat-square" alt="EasyExcel"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-AGPL%203.0-orange?style=flat-square" alt="License"></a>
</p>

---

## 📖 项目简介 (Overview)

**Inventory Management System** 是一个现代化的企业级分布式进销存管理解决方案。本项目从传统单体架构全面重构，迁移至 **Spring Cloud Alibaba 微服务体系**，采用前后端分离架构。

系统不仅涵盖了基础的采购、销售、库存核心业务，还深度融合了分布式事务、流量治理、服务注册与发现等微服务核心设计。此外，前端基于跨端技术栈构建，支持 **Web 浏览器、Desktop 桌面应用 (Electron) 以及 Mobile 移动端 (Android)** 的全场景多端覆盖。

---

## 🏗️ 架构设计与系统概览 (Architecture)

### 1. 微服务拓扑架构图

系统采用三层分布式架构，由端侧、API 网关、业务微服务集群与基础设施层构成：

```mermaid
flowchart TD
    subgraph 端侧层 [多端接入层 (Client Layer)]
        Web[Web 浏览器]
        ElectronApp[Electron 桌面端]
        AndroidApp[Capacitor 安卓端]
    end

    subgraph 网关层 [统一入口与流量治理]
        Gateway[Spring Cloud Gateway :8888]
        SentinelDash[Sentinel 控制台 :8858]
        Gateway -.->|流量治理规则| SentinelDash
    end

    subgraph 业务微服务集群 [Business Microservices]
        UserSvc[User Service :8081<br/>用户认证与数据看板]
        ProductSvc[Product Service :8082<br/>商品、仓库与条码]
        OrderSvc[Order Service :8083<br/>采购销售单据]

        OrderSvc -- "OpenFeign RPC" --> ProductSvc
        OrderSvc -- "OpenFeign RPC" --> UserSvc
    end

    subgraph 基础设施层 [Infrastructure & Middleware]
        Nacos[Nacos Server :8848/9848<br/>注册与配置中心]
        Seata[Seata TC :8091<br/>分布式事务控制]
        Redis[(Redis 缓存/Token)]
        MySQL[(MySQL 8.0 业务库)]
    end

    %% 流量路由与控制流
    Web & ElectronApp & AndroidApp -->|HTTP/HTTPS 请求| Gateway
    Gateway -->|路由转发 & JWT 鉴权| UserSvc
    Gateway -->|路由转发 & JWT 鉴权| ProductSvc
    Gateway -->|路由转发 & JWT 鉴权| OrderSvc

    %% 基础设施依赖
    UserSvc & ProductSvc & OrderSvc & Gateway -.->|服务注册与拉取| Nacos
    OrderSvc & ProductSvc -.->|分布式事务协作| Seata
    UserSvc & Gateway -.->|Token 状态同步| Redis
    UserSvc & ProductSvc & OrderSvc -->|持久化读写| MySQL
```

### 2. 微服务七大核心组件

| 核心组件             | 技术实现         | 核心职能描述                                                                 |
| :------------------- | :--------------- | :--------------------------------------------------------------------------- |
| **服务发现与配置**   | **Nacos**        | 集成服务注册发现与配置中心，实现配置的热更新与服务实例的动态发现。           |
| **声明式服务调用**   | **OpenFeign**    | 以 Java 接口形式定义 RPC 调用，简化微服务间的业务通信逻辑。                  |
| **负载均衡治理**     | **LoadBalancer** | 在服务消费端实现智能路由选择，确保流量均匀分配至健康的微服务实例。           |
| **全方位流量防护**   | **Sentinel**     | 针对异常流量进行实时限流、降级与熔断防护，保障核心业务的健壮性。             |
| **统一微服务网关**   | **Gateway**      | 系统的单一流量入口，承载路由转发、JWT权限校验、跨域处理等任务。              |
| **分布式事务一致性** | **Seata**        | 提供跨数据库、跨服务的事务保障，通过 AT 模式确保库存扣减与订单生成的原子性。 |
| **容器化集群编排**   | **Kubernetes**   | 适配云原生部署，支持服务的自愈、滚动更新、水平扩容与全生命周期管理。         |

---

## 📂 项目目录导航 (Directory Structure)

```text
.
├── Frontend/                        # Vue 3 前端跨平台主工程
│   ├── src/                         # 业务源码目录
│   │   ├── api/                     # Axios 请求接口封装 (按微服务模块拆分)
│   │   ├── assets/                  # 静态资源 (含主样式 main.css 及图标)
│   │   ├── components/              # 复用业务组件 (条码识别、Excel 导出等)
│   │   ├── router/                  # Vue Router 路由管理 (包含导航守卫)
│   │   ├── stores/                  # Pinia 状态管理中心 (用户登录态、缓存)
│   │   ├── views/                   # 页面组件 (看板、商品管理、采购销售流水)
│   │   └── App.vue                  # 根组件 (定义主布局结构)
│   ├── electron/                    # Electron 桌面端入口与主进程脚本
│   ├── android/                     # Capacitor 适配生成的原生安卓工程
│   ├── public/                      # 无需编译的静态资源
│   ├── vite.config.js               # Vite 核心配置 (开发代理与构建策略)
│   └── package.json                 # 前端工程配置与自动化脚本
├── inventory-microservices/         # Spring Cloud Alibaba 后端微服务集群
│   ├── common-api/                  # 全局公共模块 (全服务共享依赖)
│   │   ├── src/main/java/           # 存放 Entity、DTO、VO 及 Feign 客户端定义
│   │   └── pom.xml                  # 维护通用依赖 (Lombok, Jackson 等)
│   ├── gateway-service/             # 系统入口网关 (Port: 8888)
│   │   └── src/main/resources/      # Nacos 注册配置、路由转发规则、Sentinel 规则
│   ├── user-service/                # 用户认证、权限与看板数据服务 (Port: 8081)
│   │   ├── src/main/java/           # 认证逻辑、RBAC 权限管理及看板统计接口
│   │   └── src/main/resources/      # MyBatis Mapper 映射与数据库连接配置
│   ├── product-service/             # 商品资料、分类与库存核心服务 (Port: 8082)
│   │   └── src/main/java/           # 包含 ZXing 条码识别、文件上传下载逻辑
│   ├── order-service/               # 交易单据与分布式事务控制中心 (Port: 8083)
│   │   └── src/main/java/           # 集成 Seata @GlobalTransactional 控制事务
│   ├── mysql/                       # 数据库资源中心
│   │   └── init/                    # 容器初始化 SQL (含业务表结构与演示数据)
│   ├── k8s/                         # 云原生编排清单 (各服务 Deployment/Service)
│   ├── docker-compose-middleware.yml # 基础设施 (Nacos, Redis, Seata, Sentinel) 一键启动
│   └── pom.xml                      # 顶级 Maven 依赖管理
├── docker-compose.yml               # 生产环境一键部署编排 (含 Nginx/前端镜像)
├── nginx.conf                       # 前端静态托管与网关反向代理 Nginx 配置
└── README.md                        # 项目技术文档与开发手册
```

---

## 🛠️ 技术栈清单 (Tech Stack)

### 后端核心技术

- **基础框架**: Spring Boot 3.3.2
- **微服务骨架**: Spring Cloud 2023.0.3 + Spring Cloud Alibaba 2023.0.1.2
- **注册/配置中心**: Nacos 2.3.2
- **流量防护**: Sentinel 1.8.8
- **分布式事务**: Seata 1.8.0 (使用 AT 模式)
- **网关**: Spring Cloud Gateway
- **持久层框架**: MyBatis Plus 3.0.5 + MySQL 8.0
- **缓存/会话**: Redis 7.x
- **核心工具**: EasyExcel 3.3.4 (Excel 导入导出), ZXing (条码处理)

### 前端核心技术

- **核心框架**: Vue 3.5.22 (Composition API)
- **构建工具**: Vite 7.1.11
- **状态管理**: Pinia 3.0.3
- **数据可视化**: ECharts 6.0.0
- **桌面容器**: Electron 39.2.7
- **移动容器**: Capacitor 8.0.0 (面向 Android)
- **代码质量**: ESLint 9.x + Oxlint + Prettier

---

## 🚀 快速开始与本地开发 (Getting Started)

### 1. 前置环境要求

- **Java 开发包**: JDK 17
- **前端运行环境**: Node.js v20.19.0+ 或 v22.12.0+
- **构建管理工具**: Maven 3.8+ (或使用自带的 `./mvnw`)
- **容器与数据库**: Docker & Docker Compose

### 2. 运行前端工程

#### 启动 Web 开发服务器

```bash
cd Frontend
npm install
npm run dev
```

访问 Web 页面：[http://localhost:5173](http://localhost:5173)

#### 启动 Electron 桌面开发版

```bash
npm run electron:dev
```

#### 启动后端微服务

推荐使用 IntelliJ IDEA 启动

---

## 🚢 生产部署方案 (Deployment)

### 方案 A：Docker Compose 整体容器化部署

1. 进入前端目录打包

```bash
cd Frontend
npm run build
```

2. 进入后端微服务目录并进行打包（跳过测试）

```bash
cd inventory-microservices
mvn clean package -DskipTests
```

3. 进入项目根目录，使用根目录的 `docker-compose.yml` 可以一键构建并部署包括前端 Nginx、微服务集群和中间件在内的全量环境：

```bash
# 构建并后台运行所有服务
docker-compose up -d --build
```

### 方案 B：Kubernetes (K8s) 云原生部署 (零基础上手指南)

本项目专门针对本地开发环境（如 Docker Desktop）编写了**一键化部署脚本**。该脚本会自动帮你完成：**Maven 编译打包 ➜ 本地 Docker 镜像构建 ➜ K8s 命名空间与配置创建 ➜ 中间件就绪性等待 ➜ 数据库连通性测试 ➜ 自动发布 Nacos 配置（含重试） ➜ 业务 Pod 滚动更新**。

**K8s 部署方案核心特性：**

- ⚡ **零停机滚动更新** — 所有 Deployment 配置 `RollingUpdate` 策略 (maxSurge:1, maxUnavailable:0)
- 🔄 **启动依赖保障** — 业务 Pod 通过 `initContainers` 自动等待 Nacos/Seata 就绪后再启动
- ⏱️ **优雅终止** — 所有 Pod 配置 `terminationGracePeriodSeconds`，避免请求中断
- 🛡️ **健壮性探针** — 全面配置 `startupProbe` / `readinessProbe` / `livenessProbe` 三级健康检查
- 🧠 **JVM 调优** — 统一配置 G1GC 垃圾回收器与内存参数
- 📦 **可选生产增强** — 提供 HPA 弹性伸缩、PDB 中断预算、NetworkPolicy 网络策略

#### 1. 前置准备工作

在运行脚本前，请务必确保以下两点：

1. **启用 Docker Desktop 的 Kubernetes 功能**：
   - 打开 Docker Desktop 设置 (齿轮图标)。
   - 选择 **Kubernetes** 选项卡。
   - 勾选 **Enable Kubernetes**，然后点击 **Apply & restart**。等待右下角的 Kubernetes 状态指示灯变绿。
2. **本地主机运行 MySQL**：
   - 脚本要求 **MySQL 必须运行在宿主机本地**（端口 3306），且不能作为 Docker 容器或 K8s Workload 运行。
   - K8s 内部的微服务将通过 `host.docker.internal` 域名安全地连接到你宿主机的 MySQL 实例。

#### 2. 一键运行部署

根据你的操作系统，打开终端执行以下命令：

##### Windows (PowerShell) 环境运行：

打开 PowerShell：

```powershell
cd inventory-microservices
powershell -ExecutionPolicy Bypass -File .\deploy-k8s.ps1
```

##### Linux / macOS 环境运行：

打开 Terminal：

```bash
cd inventory-microservices
chmod +x deploy-k8s.sh && ./deploy-k8s.sh
```

---

## 📄 开源许可证 (License)

本项目采用 [AGPL-3.0](LICENSE) 许可证发布。

Copyright © 2026-Present [**yeflyleaf**](https://github.com/yeflyleaf). 保留所有权利。
