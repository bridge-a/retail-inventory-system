# 面向小型零售门店的库存出入库与采购预警系统

## 项目定位

本系统面向小型零售门店的日常库存管理场景，围绕商品库存维护、出入库流水记录、采购申请审批和低库存预警展开设计。

系统重点解决以下问题：

- 库存变化缺少可追溯记录，无法确认每次库存变更的来源。
- 采购申请缺少审批流程约束，容易出现越权审批或重复变更状态。
- 低库存商品发现不及时，补货依赖人工查看。
- 普通员工、店长和管理员之间的权限边界不清。

## 技术结构

- 后端：Spring Boot，默认运行在 `http://localhost:8080`
- 前端：Vue 3 + Vite，默认运行在 `http://localhost:5173`
- 数据库：默认 H2，另提供 MySQL profile 和初始化脚本

前端保持独立运行，不打包进后端，前后端通过 REST API 联动。

## 启动后端

后端使用 Spring Boot + H2 数据库，启动后会自动初始化演示数据。

```bash
cd backend
mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

H2 控制台：

```text
http://localhost:8080/h2-console
```

连接信息：

```text
JDBC URL: jdbc:h2:file:./data/retail_inventory
User Name: sa
Password: 留空
```

## 启动前端

前端已改造为 Vue 3 + Vite 最小工程。

```bash
cd frontend
npm install
npm start
```

也可以使用：

```bash
npm run dev
```

前端默认地址：

```text
http://localhost:5173
```

前端 API 基础地址固定为：

```text
http://localhost:8080
```

如果后端未启动，前端会显示“后端未连接”提示，并使用少量演示数据，避免页面空白。

## 测试账号

管理员：

```text
用户名：manager
密码：123456
```

普通员工：

```text
用户名：staff
密码：123456
```

## 角色权限说明

- 管理员登录后可以查看首页概览、商品库存、库存流水、采购申请、采购审批、低库存预警。
- 管理员可以审批 `PENDING` 状态采购单，可以驳回采购单，也可以对 `APPROVED` 状态采购单执行采购入库。
- 普通员工登录后可以查看首页概览、商品库存、库存流水、采购申请、低库存预警。
- 普通员工不能进入采购审批模块，前端不会显示采购审批入口，模块切换时也会进行二次拦截。
- 页面会显示当前登录角色，方便区分管理员视角和普通员工视角。

## 前后端联动说明

前端使用 `fetch` 调用后端 REST API，主要接口包括：

```text
POST /api/users/login
GET  /api/dashboard
POST /api/stock/in
POST /api/stock/out
POST /api/purchase-orders
POST /api/purchase-orders/{id}/approve
POST /api/purchase-orders/{id}/reject
POST /api/purchase-orders/{id}/complete
GET  /api/warnings
```

如果后端已启动，页面优先使用数据库中的真实演示数据。如果后端未启动或连接中断，页面会保留前端演示数据，并提示当前处于演示模式。

## MySQL 运行方式

项目默认使用 H2 文件数据库，便于课程验收时快速启动。项目同时提供 MySQL 运行配置和完整表结构脚本。

MySQL 建库脚本：

```text
doc/sql/create-mysql-database.sql
```

完整 MySQL 表结构脚本：

```text
doc/sql/schema.sql
backend/src/main/resources/schema-mysql.sql
```

MySQL 初始化数据脚本：

```text
doc/sql/data-mysql.sql
backend/src/main/resources/data-mysql.sql
```

使用 MySQL 运行前，先创建数据库：

```bash
mysql -u root -p < doc/sql/create-mysql-database.sql
```

然后启动后端并启用 `mysql` profile：

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

如果本地 MySQL 账号不是 `root / 123456`，可以通过环境变量覆盖：

```bash
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_password
MYSQL_URL=jdbc:mysql://localhost:3306/retail_inventory?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
```
