# V1.3 查询接口与前端真实数据设计

## 目标

补齐客户、产品、办理意向的只读查询接口，替换前端客户、产品和意向页面中的 Mock 数据，同时保持现有推荐与创建意向链路不变。

本阶段不引入 Gateway、JWT、分页框架、跨服务数据库查询或付费 LLM。

## 架构决策

采用“服务自治 + 前端聚合”：

- `customer-service` 只查询客户及其大小类。
- `deposit-product-service` 只查询产品及期限。
- `deposit-business-service` 只查询办理意向及明细。
- 前端以 `customerNo`、`productId`、`productTermId` 聚合展示名称。
- 任一服务不得直接读取其他服务拥有的业务表。

前端维护请求周期内的客户、产品字典，不把补全后的名称写回业务数据。

## 后端接口

### 客户

```text
GET /api/v1/customers
GET /api/v1/customers/{customerNo}
```

列表仅返回 `status = 1` 且大小类启用的客户，按 `customer_no` 排序。返回字段：

```text
customerNo
customerName
largeCategoryCode
largeCategoryName
smallCategoryCode
smallCategoryName
```

客户不存在时继续返回现有 `CUSTOMER_NOT_FOUND` 业务错误。

### 产品

```text
GET /api/v1/products
GET /api/v1/products/{productId}
```

列表返回启用产品，按 `product_code` 排序。详情返回基础信息和全部启用期限：

```text
productId
productCode
productName
depositType
status
terms[]
  productTermId
  termCode
  termName
  termDays
  minOpenAmountInCents
  minRetainAmountInCents
  noticeDays
```

产品不存在或停用时返回 `PRODUCT_NOT_FOUND`。准入和执行利率仍使用已有接口，不混入产品静态详情。

### 办理意向

```text
GET /api/v1/intentions?customerNo=&status=
GET /api/v1/intentions/{intentionNo}
```

列表筛选参数均可选，结果按 `created_at DESC, sn_id DESC` 排序。本阶段演示数据量小，不分页；后续引入分页时保持单项结构不变。

主单字段：

```text
intentionNo
customerNo
requirementText
totalAmountInCents
status
sourceChannel
createdAt
updatedAt
```

详情额外包含：

```text
details[]
  detailNo
  productId
  productTermId
  currencyCode
  amountInCents
  interestRate
  expectedInterestInCents
  status
  createdAt
  updatedAt
```

意向不存在时返回 `INTENTION_NOT_FOUND`。查询接口不改变任何状态。

## 前端数据流

Vite 为三个查询服务增加明确代理前缀：

```text
/customer-api  -> customer-service:8081，转写为 /api
/product-api   -> deposit-product-service:8082，转写为 /api
/business-api  -> deposit-business-service:8083，转写为 /api
/api           -> ai-assistant-service:8084
```

页面统一通过 `src/api/request.ts` 的 Axios 实例调用，不在组件中直接发请求。

- 客户选择器调用真实客户列表，删除客户 Mock。
- 产品列表显示真实产品；产品详情显示期限、最低起存和最低留存金额。
- 意向列表加载意向、客户、产品数据，在前端建立字典补全名称。
- 意向详情按编号查询，并复用客户、产品字典补全主单和明细。
- 后端 BIGINT ID 在 API 适配层转换为字符串。
- 金额、利率、币种继续使用统一工具函数，页面不自行计算。

## 路由与展示

保留现有路由：

```text
/assistant
/intentions
/intentions/:id
/products
/products/:id
```

产品列表行可进入详情；意向列表行可进入详情。列表支持客户编号和状态筛选，不新增 Dashboard 或维护功能。

## 错误处理

- 404 业务错误显示后端业务消息。
- 网络失败或超时显示统一服务不可用提示。
- 聚合补全找不到名称时显示原始编号，不把整个页面判为失败。
- 不再以静默 Mock 兜底，避免把测试数据误认为真实业务数据。

## 测试与验收

后端按 Repository/Service/Controller 分层进行 TDD，覆盖：

- 启用客户列表和不存在客户。
- 产品列表、产品期限详情和不存在产品。
- 意向列表筛选、意向明细和不存在意向。

前端测试覆盖：

- 三类 API 请求地址及 BIGINT 字符串适配。
- 客户/产品字典聚合和缺失名称降级。
- 业务错误保留。

最终验收：

1. Maven 全量测试通过。
2. Vitest 与 TypeScript 生产构建通过。
3. 一键启动脚本通过。
4. `/assistant` 使用真实客户列表。
5. `/products` 与详情显示真实产品和期限。
6. 创建意向后可在 `/intentions` 与详情中查询到同一笔数据。
7. 推荐与创建意向原链路无回归。
