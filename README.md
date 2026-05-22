# 面向小型零售门店的库存出入库与采购预警系统

## 项目定位

本系统面向小型零售门店的日常库存管理场景，围绕商品库存维护、出入库流水记录、采购申请审批和低库存预警展开设计。

系统重点解决以下问题：

- 库存变化缺少可追溯记录，无法确认每次库存变更的来源。
- 采购申请缺少审批流程约束，容易出现越权审批或重复变更状态。
- 低库存商品发现不及时，补货依赖人工查看。
- 普通员工、店长和管理员之间的权限边界不清。

## 最小运行版启动方式

### 启动后端

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

### 启动前端

前端保持独立运行，使用本地静态服务启动：

```bash
cd frontend
npm start
```

前端默认地址：

```text
http://localhost:5173
```

## 测试账号

管理员账号：

```text
用户名：manager
密码：123456
```

普通员工账号：

```text
用户名：staff
密码：123456
```

## 角色权限说明

- 管理员登录后可以查看商品库存、库存流水、低库存预警，并可以进入采购审批模块。
- 管理员可以审批 PENDING 状态采购单，可以驳回采购单，也可以对 APPROVED 状态采购单执行采购入库。
- 普通员工登录后可以查看商品库存、提交采购申请、查看自己的采购申请状态。
- 普通员工登录后不显示采购审批菜单，也不能执行审批、驳回或采购入库操作。
- 前端页面会根据登录角色切换可见模块，后端接口也会校验审批人角色，避免只依赖前端隐藏按钮。

## 前后端联动说明

如果后端已启动，前端会自动连接：

```text
http://localhost:8080/api/dashboard
```

并使用数据库中的真实演示数据。

如果后端未启动，页面会自动回退到前端演示数据，方便在无法启动后端时查看界面效果。

## MySQL 运行方式（可选优化）

项目默认使用 H2 文件数据库，便于课程验收时快速启动。为提升数据库可迁移性，项目同时提供 MySQL 运行配置和完整 7 张核心表结构脚本。

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

说明：H2 用于默认演示和自动化测试，MySQL profile 用于展示关系型数据库部署能力，两套脚本保持相同的核心表结构和初始化数据。
