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

测试账号：

```text
管理员：manager / 123456
普通员工：staff / 123456
```

如果后端已启动，前端会自动连接 `http://localhost:8080/api/dashboard` 并使用数据库数据；如果后端未启动，页面会自动回退到前端演示数据。
