<div align="center">

# 进销存管理系统 (微服务重构版)

**基于 Spring Cloud Alibaba + Vue 3 的全栈式、多端部署分布式进销存系统**

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

**Inventory Management System** 是一个现代化的企业级进销存管理解决方案。本项目从单体架构全面迁移至 **Spring Cloud Alibaba 微服务体系**，采用前后端分离设计。

系统不仅涵盖了基础的进、销、存核心业务，还集成了分布式事务、流量治理、服务注册与发现等微服务核心能力。此外，前端采用跨端技术栈，支持 **Web、Desktop (Electron) 以及 Mobile (Android)** 的全场景覆盖。

### 🌟 核心特性

- **分布式架构**：基于 Spring Cloud Alibaba 2023 核心组件构建，支撑高并发、高可用。
- **强一致性保证**：集成 **Seata** 解决跨服务的分布式事务问题，确保库存与订单数据的绝对一致。
- **全链路流量安全**：通过 **Sentinel** 实现服务熔断、降级与限流；**Gateway** 统一鉴权与路由。
- **多端触达**：一份代码通过 **Electron** 转换为桌面应用，通过 **Capacitor** 转换为原生移动端应用。
- **云原生就绪**：提供完整的 Docker Compose 中间件编排及 Kubernetes 生产集群部署方案。

---

## 🌌 七大核心微服务组件 (The Seven Core Components)

本项目深度集成了微服务架构中的七大核心能力，确保了系统在高并发场景下的稳定性、可扩展性与数据一致性：

> [!IMPORTANT]
> **这七大核心组件构成了本系统的分布式骨架：**

| 核心组件                | 技术实现         | 核心职能描述                                                 |
| :---------------------- | :--------------- | :----------------------------------------------------------- |
| **1. 服务发现与配置**   | **Nacos**        | 集成服务注册发现与配置中心，实现配置的热更新与服务动态管理。 |
| **2. 声明式服务调用**   | **OpenFeign**    | 以 Java 接口形式定义 RPC 调用，解耦微服务间的通信逻辑。      |
| **3. 负载均衡治理**     | **LoadBalancer** | 在服务消费端实现智能路由选择，确保流量均匀分配至可用实例。   |
| **4. 全方位流量防护**   | **Sentinel**     | 针对异常流量进行限流、降级与熔断，保障核心业务的健壮性。     |
| **5. 统一微服务网关**   | **Gateway**      | 系统的单一入口，承载路由转发、权限校验及请求过滤等关键任务。 |
| **6. 分布式事务一致性** | **Seata**        | 提供跨数据库、跨服务的事务保障，通过 AT 模式确保数据零丢失。 |
| **7. 容器化集群编排**   | **Kubernetes**   | 适配云原生部署，支持服务的自愈、扩容与全生命周期自动化管理。 |

---

### 📂 模块结构

| 模块名称          | 技术细节                  | 功能描述                                         |
| :---------------- | :------------------------ | :----------------------------------------------- |
| `common-api`      | POJO, Utils, DTO          | 公共实体类、异常处理、工具类及 Feign 接口定义    |
| `gateway-service` | Spring Cloud Gateway, JWT | 统一入口、鉴权过滤、限流控制及动态路由           |
| `user-service`    | Spring Security, RBAC     | 用户认证、角色权限、看板数据聚合 (Dashboard)     |
| `product-service` | MyBatis Plus, Barcode     | 商品管理、分类维护、库存快照、条码识别、文件上传 |
| `order-service`   | Seata, Feign              | 采购入库、销售出库、单据管理、分布式事务控制     |

---

## 📂 目录结构与架构设计 (Architecture & Directory Structure)

### 🏗️ 整体架构设计

项目采用典型的 **三层分布式架构** 设计，实现了从端到云的全链路解耦：

1.  **端侧 (Client Layer)**：基于 **Vue 3 + Vite** 构建。通过 **Electron** 适配桌面端，通过 **Capacitor** 适配安卓端，实现“一套代码，多端部署”。
2.  **网关与核心业务层 (Gateway & Services Layer)**：
    - **Gateway**: 负责流量接入、JWT 鉴权、动态路由及 Sentinel 流量治理。
    - **Services**: 包含 `user`, `product`, `order` 三大核心微服务，利用 **OpenFeign** 进行内部通信。
3.  **基础设施层 (Infrastructure Layer)**：提供 **Nacos** (发现/配置)、**Seata** (事务)、**Redis** (缓存) 及 **MySQL** (持久化) 等支撑能力。

### 📁 项目目录导航

```text
.
├── Frontend/                        # Vue 3 前端跨平台主工程
│   ├── src/                         # 业务源码目录
│   │   ├── api/                     # Axios 请求接口封装 (按微服务模块拆分)
│   │   ├── assets/                  # 静态资源 (含主样式 main.css 及图标)
│   │   ├── components/              # 复用业务组件 (条码识别、Excel 导出等)
│   │   ├── router/                  # Vue Router 路由管理 (包含导航守卫)
│   │   ├── stores/                  # Pinia 状态管理中心 (用户登录态、库存缓存)
│   │   ├── views/                   # 页面组件 (看板、商品管理、采购销售流水)
│   │   └── App.vue                  # 根组件 (定义主布局结构)
│   ├── electron/                    # Electron 桌面端入口与主进程脚本
│   ├── android/                     # Capacitor 适配生成的原生安卓工程
│   ├── public/                      # 无需编译的静态资源 (Favicon, Manifest)
│   ├── vite.config.js               # Vite 核心配置 (定义开发代理与构建策略)
│   └── package.json                 # 前端工程配置与自动化脚本
├── inventory-microservices/         # Spring Cloud Alibaba 后端微服务集群
│   ├── common-api/                  # 全局公共模块 (全服务共享依赖)
│   │   ├── src/main/java/           # 存放 Entity、DTO、VO 及 Feign 客户端定义
│   │   └── pom.xml                  # 维护通用依赖 (Lombok, Jackson 等)
│   ├── gateway-service/             # 系统入口网关 (Port: 8888)
│   │   └── src/main/resources/      # Nacos 注册配置、路由转发规则、Sentinel 规则
│   ├── user-service/                # 用户、权限与数据大屏看板服务 (Port: 8081)
│   │   ├── src/main/java/           # 认证逻辑、RBAC 权限管理及看板统计接口
│   │   └── src/main/resources/      # MyBatis Mapper 映射与数据库连接配置
│   ├── product-service/             # 商品资料、分类与库存核心服务 (Port: 8082)
│   │   └── src/main/java/           # 包含 ZXing 条码识别、文件上传下载逻辑
│   ├── order-service/               # 交易单据与分布式事务控制中心 (Port: 8083)
│   │   └── src/main/java/           # 集成 Seata @GlobalTransactional 控制事务
│   ├── mysql/                       # 数据库资源中心
│   │   └── init/                    # 容器初始化 SQL (含业务表结构与演示数据)
│   ├── k8s/                         # 云原生编排清单 (各服务 Deployment/Service)
│   ├── docker-compose-middleware.yml # 基础设施(Nacos, Redis, Seata, Sentinel)一键启动
│   └── pom.xml                      # 顶级 Maven 依赖管理 (版本锁、全局仓库定义)
├── docker-compose.yml               # 生产环境一键部署编排 (含 Nginx/前端镜像)
├── nginx.conf                       # 前端静态托管与网关反向代理 Nginx 配置
└── README.md                        # 项目技术文档与开发手册
```

---

## 🛠️ 技术栈 (Tech Stack)

### 后端 (Backend)

- **核心框架**: Spring Boot 3.3.2 + Spring Cloud 2023
- **微服务治理**: Spring Cloud Alibaba (Nacos, Sentinel, Seata)
- **持久层**: MyBatis Plus + MySQL 8.0
- **缓存/消息**: Redis 7.x
- **安全鉴权**: JWT (Stateless) + Spring Security

### 前端 (Frontend)

- **核心框架**: Vue 3.5 (Composition API)
- **构建工具**: Vite 7.x
- **状态管理**: Pinia
- **组件库**: (如 Element Plus 等)
- **可视化**: ECharts 6.x
- **跨平台**: Electron (Desktop) + Capacitor (Android)

---

## 🚀 快速开始 (Getting Started)

### 1. 前置依赖

- **JDK 17**
- **Node.js v20+**
- **Docker & Docker Compose**
- **MySQL 8.0**

### 2. 数据库初始化

1. 在本地 MySQL 中创建名为 `demo1` 的数据库。
2. 运行 `inventory-microservices/mysql/init/` 下的 SQL 脚本。
3. **特别注意**：由于 Nacos 使用 MySQL 存储，请将 Nacos 2.x 的全量脚本也导入 `demo1` 库。

### 3. 启动中间件

```bash
cd inventory-microservices
docker-compose -f docker-compose-middleware.yml up -d
```

访问地址：

- Nacos: `http://localhost:8848/nacos` (nacos/nacos)
- Sentinel: `http://localhost:8858` (sentinel/sentinel)

### 4. 运行服务

在 IDE 中按顺序启动：

1. `GatewayApplication` (8888)
2. `UserApplication` (8081)
3. `ProductApplication` (8082)
4. `OrderApplication` (8083)

### 5. 启动前端

```bash
cd Frontend
npm install
npm run dev
```

---

## 🚢 生产部署 (Deployment)

### Docker Compose 全量部署

项目根目录下提供了 `docker-compose.yml`，一键拉起包括前端 Nginx 在内的完整环境：

```bash
docker-compose up -d
```

### Kubernetes (K8s) 部署

提供跨平台一键部署脚本，自动完成 Maven 编译 → Docker 镜像构建 → K8s 资源下发 → 等待中间件就绪：

```bash
# Linux / macOS
cd inventory-microservices
chmod +x deploy-k8s.sh && ./deploy-k8s.sh
```

```powershell
# Windows PowerShell
cd inventory-microservices
powershell -ExecutionPolicy Bypass -File .\deploy-k8s.ps1
```

> **前提条件**：本地需已安装 Docker、kubectl，并启用 K8s 集群（如 Docker Desktop K8s 或 Minikube）。首次部署后需进入 Nacos 控制台导入配置。

---

## 📄 许可证 (License)

本项目采用 [AGPL-3.0](LICENSE) 许可证发布。

Copyright © 2026-Present [**yeflyleaf**](https://github.com/yeflyleaf).
