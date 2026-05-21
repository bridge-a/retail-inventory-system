# 最小运行版验证清单

## 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

预期结果：

```text
Tomcat started on port 8080
Started InventoryApplication
```

## 2. 检查健康接口

访问：

```text
http://localhost:8080/api/health
```

预期返回包含：

```json
{
  "status": "UP",
  "service": "retail-inventory-system",
  "database": "H2",
  "version": "runtime-minimal-v1"
}
```

## 3. 检查看板接口

访问：

```text
http://localhost:8080/api/dashboard
```

预期结果：

```text
返回 products、records、warnings、purchaseOrders 数据
```

## 4. 打开前端页面

打开：

```text
frontend/index.html
```

预期结果：

```text
页面顶部显示“后端健康检查通过，已连接数据库”
商品库存、库存流水、采购审批和低库存预警正常展示
```

## 5. 验证核心交互

- 管理员可以审批 PENDING 采购单
- 普通员工不能审批采购单
- 出库数量大于库存时应失败
- 采购入库后采购单状态变为 COMPLETED
- 入库或出库后库存流水增加

## 6. 后端未启动兜底验证

关闭后端后重新打开前端：

```text
frontend/index.html
```

预期结果：

```text
页面提示“后端未启动，当前使用前端演示数据”
前端仍可进行本地演示，不影响展示
```