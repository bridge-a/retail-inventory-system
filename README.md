# 面向小型零售门店的库存流水、采购审批与低库存预警管理系统

## 项目简介

本项目面向小型零售门店的日常库存管理场景，围绕商品库存维护、出入库流水记录、采购申请审批和低库存预警展开设计。

系统不追求完整进销存平台的复杂功能，而是重点解决以下问题：

- 库存变化缺少可追溯记录
- 出库可能导致库存为负
- 采购审批缺少状态流转约束
- 低库存发现不及时
- 普通员工与管理员权限边界不清晰

本项目采用前后端分离架构，后端提供 REST API，前端提供角色登录、模块切换和业务操作页面。

## 技术栈

| 层次     | 技术                                                |
| -------- | --------------------------------------------------- |
| 前端     | Vue 3 + Vite + JavaScript                           |
| 后端     | Spring Boot 3.3.5 + Java 17                         |
| 数据访问 | Spring JDBC + Mapper/JdbcMapper                     |
| 数据库   | 默认快速运行使用 H2，正式部署提供 MySQL 8.x profile |
| 协作流程 | Git Flow + Feature Branch + Pull Request Review     |

## 核心功能

1. 用户与权限模块：管理员、普通员工登录与权限区分
2. 商品与库存模块：商品分类、商品编码、当前库存、安全库存阈值
3. 出入库流水模块：入库、出库、库存变更记录
4. 采购申请模块：员工提交采购申请，初始状态为 `PENDING`
5. 采购审批模块：管理员审批、驳回、采购入库
6. 低库存预警模块：库存低于安全库存后生成或刷新预警

## 核心业务规则

- 商品编码必须唯一，并在保存前进行标准化处理
- 库存不能直接随意修改，必须通过入库或出库生成库存流水
- 出库数量不能大于当前库存
- 库存流水必须记录 `beforeStock` 和 `afterStock`
- 普通员工不能进入采购审批模块
- 采购单创建后状态必须为 `PENDING`
- 只有 `PENDING` 状态可以审批或驳回
- 只有 `APPROVED` 状态可以执行采购入库
- 采购入库成功后采购单状态变为 `COMPLETED`
- `REJECTED` 和 `COMPLETED` 为终态，不能再次审批
- 出入库后需要刷新低库存预警状态

## 项目目录结构

```text
retail-inventory-system/
├── backend/                         Spring Boot 后端
│   ├── src/main/java/com/example/inventory/
│   │   ├── controller/              Controller 层
│   │   ├── service/                 Service 接口
│   │   ├── service/impl/            Service 实现
│   │   ├── mapper/                  DAO/Mapper 接口
│   │   ├── mapper/jdbc/             JDBC Mapper 实现
│   │   ├── dto/                     请求对象
│   │   └── entity/                  实体对象
│   └── src/main/resources/
│       ├── application.yml          默认快速运行配置
│       ├── application-mysql.yml    MySQL profile 配置
│       ├── schema.sql               默认数据库表结构
│       ├── data.sql                 默认演示数据
│       ├── schema-mysql.sql         MySQL 表结构脚本
│       └── data-mysql.sql           MySQL 初始化数据
├── frontend/                        Vue 3 + Vite 前端
│   ├── src/
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── doc/sql/
│   ├── create-mysql-database.sql    MySQL 建库脚本
│   ├── schema.sql                   7 张核心表结构
│   └── data-mysql.sql               MySQL 初始化数据
└── README.md
```

## 环境要求

- JDK 17
- Maven 3.8+
- Node.js 18+
- npm
- MySQL 8.x，正式部署时需要

如果本机命令行无法识别 `mvn`，可以安装 Maven 并配置环境变量，或使用 IntelliJ IDEA 的 Maven 面板运行后端。

## 快速运行方式

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认运行地址：

```text
http://localhost:8080
```

健康检查接口：

```text
http://localhost:8080/api/health
```

默认快速运行方式使用 H2 数据库，便于课程验收时快速启动。

H2 控制台：

```text
http://localhost:8080/h2-console
```

H2 连接信息：

```text
JDBC URL: jdbc:h2:file:./data/retail_inventory
User Name: sa
Password: 留空
```

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行地址：

```text
http://localhost:5173
```

前端会访问后端 `http://localhost:8080` 提供的接口。

## 测试账号

| 角色     | 用户名  | 密码   | 权限说明                                           |
| -------- | ------- | ------ | -------------------------------------------------- |
| 管理员   | manager | 123456 | 可查看库存、出入库、提交采购、审批采购、采购入库   |
| 普通员工 | staff   | 123456 | 可查看库存、出入库、提交采购，不能进入采购审批模块 |

## MySQL 运行方式

项目默认保留快速运行方式，同时提供 MySQL profile 和完整 7 张核心表脚本，用于正式部署和数据库设计说明。

### 1. 创建数据库

在项目根目录执行：

```bash
mysql -u root -p < doc/sql/create-mysql-database.sql
```

### 2. 启动 MySQL profile

进入后端目录：

```bash
cd backend
```

如果本地 MySQL 用户名和密码是 `root / 123456`，可直接执行：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

如果本地 MySQL 密码不同，可在 PowerShell 中设置环境变量：

```powershell
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="your_password"
$env:MYSQL_URL="jdbc:mysql://localhost:3306/retail_inventory?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"

mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

MySQL profile 会使用以下脚本初始化数据：

```text
backend/src/main/resources/schema-mysql.sql
backend/src/main/resources/data-mysql.sql
```

## 验收检查点

建议按以下顺序检查系统功能：

1. 启动后端，访问 `/api/health`，确认后端服务正常
2. 启动前端，访问 `http://localhost:5173`
3. 使用 `manager / 123456` 登录，确认可以进入采购审批模块
4. 使用 `staff / 123456` 登录，确认采购审批模块不可见且不可进入
5. 执行出库操作，确认库存不能扣成负数
6. 执行入库或出库后，确认生成库存流水
7. 提交采购申请，确认采购单初始状态为 `PENDING`
8. 管理员审批采购单，确认状态变为 `APPROVED`
9. 对 `APPROVED` 采购单执行采购入库，确认状态变为 `COMPLETED`
10. 确认 `COMPLETED` 采购单不能再次审批
11. 出库导致库存低于安全库存后，确认低库存预警刷新
12. 入库恢复库存后，确认预警状态更新

## Git 协作说明

本项目按 Git Flow 方式组织协作过程：

- `main`：最终发布分支
- `develop`：日常集成分支
- `feature/*`：功能开发分支

主要功能分支包括：

```text
feature/auth-role
feature/product-stock
feature/stock-record
feature/purchase-order
feature/purchase-approval
feature/stock-warning
feature/integration-test
feature/frontend-ui
feature/database-optimization
feature/frontend-vue-optimization
```

每个功能分支通过 Pull Request 合并到 `develop`，并由另一名成员进行 Review。PR 中保留了问题评论、修复 commit、审批记录和最终合并记录，用于体现结对编程中的角色互换、代码复审和交叉集成测试过程。

最终版本通过 `develop -> main` 的发布 PR 合并到主分支。

## 课程作业关注点对应

| 作业要求                    | 项目体现                                                     |
| --------------------------- | ------------------------------------------------------------ |
| 至少 4 个模块               | 用户权限、商品库存、库存流水、采购申请、采购审批、低库存预警 |
| 前后端分离                  | Vue 3 + Vite 独立运行，Spring Boot 提供 REST API             |
| ER 图和表结构               | 提供 7 张核心表设计和 MySQL SQL 脚本                         |
| Controller-Service-DAO 分层 | 后端包含 Controller、Service、Mapper/JdbcMapper 分层         |
| Git Flow / Feature Branch   | 使用 `develop` 和多个 `feature/*` 分支                       |
| PR 复审                     | 功能分支均通过 PR 合并，并保留复审与修复记录                 |
| 角色互换                    | 执行日志中记录驾驶员、领航员和关键贡献                       |
| 交叉集成测试                | 覆盖库存、采购、权限、预警和状态流转场景                     |
