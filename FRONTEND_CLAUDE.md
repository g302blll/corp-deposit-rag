# CLAUDE.md

# 对公存款智能服务助手 - 前端 AI 工作规则

## 1. 任务与读取顺序

你正在为“对公存款智能服务助手”实现 V1.2 前端 MVP。开始开发前必须依次阅读：

1. `CLAUDE.md`
2. `FRONTEND_ARCHITECTURE.md`
3. `FRONTEND_PROJECT_STATE.md`
4. `FRONTEND_IMPLEMENTATION_GUIDE.md`

然后直接开始创建前端工程，不重新进行需求澄清。若文档与用户后续最新明确指令冲突，以用户最新指令为准。

## 2. MVP 目标

主要用户：银行客户经理。

第一阶段必须跑通：

```text
登录
→ AI 智能工作台
→ 选择客户
→ 输入自然语言资金需求
→ 调用 AI 推荐接口
→ 展示 1~N 个结构化方案
→ 查看方案依据
→ 选择方案
→ 二次确认
→ 创建办理意向
→ 展示交易流水号/意向结果
```

系统定位：**银行业务系统 + AI Copilot 工作台**，不是传统 CRUD 后台，也不是 ChatGPT 克隆。

## 3. 技术栈

固定使用：

- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- Vue Router
- Axios
- Node.js 20+

npm/pnpm 二选一，全项目统一。

## 4. UI 风格

已确认：蓝白金融科技、现代、克制、企业级、结构化、可信。

规则：

- 顶部轻导航，不做大型左侧菜单。
- AI 工作台采用左侧客户上下文 + 右侧会话区。
- 推荐方案卡片嵌入聊天流。
- 意向列表/详情采用稳重银行后台风格。
- PC 优先，最低 1280px，重点 1440px/1920px。
- 第一阶段不做移动端。

禁止：过度渐变、夸张动画、巨大圆角、纯聊天机器人外观、老旧重后台风格。

## 5. 页面范围

```text
/login
/assistant
/intentions
/intentions/:id
/products
/products/:id
```

第一阶段不主动增加知识库管理、利率维护、客户维护、存单操作、复杂 RBAC、Dashboard。

## 6. 关键交互规则

### 登录

登录后默认进入 `/assistant`，不进入 Dashboard。

### 客户上下文

- 未选择客户时允许通用咨询。
- 个性化准入/利率/方案推荐需要客户上下文。
- 左侧长期显示当前客户、大小类、可办理产品。
- 切换客户必须提示“将开启新的咨询会话”。
- 切换后必须清空旧客户会话，禁止 A 客户上下文污染 B 客户。

### 推荐方案

必须为：AI 说明 + 结构化方案卡片。

方案数据必须支持组合产品：

```ts
details: DepositPlanDetail[]
```

### 查看依据

使用右侧 Drawer，不跳新页面。展示客户分类、准入、期限、最低金额、利率匹配条件、生效日期、试算结果。

### 创建意向

必须二次确认，并显示：

> 本操作仅创建办理意向，不会直接执行真实存款交易。

### 幂等

创建意向时生成 `idempotencyKey`，优先 `crypto.randomUUID()`。

**同一次创建操作重试必须复用相同 key，不能重新生成。**

## 7. 后端现状

| 服务 | 端口 | 职责 |
| --- | ---: | --- |
| customer-service | 8081 | 客户及大小类 |
| deposit-product-service | 8082 | 准入、期限、利率、匹配、试算 |
| deposit-business-service | 8083 | 办理意向 |
| ai-assistant-service | 8084 | Mock 参数提取与 Tool 编排 |

当前确认可用：

```text
POST /api/v1/assistant/plans
POST /api/v1/assistant/intentions
```

查询类接口如果缺失：先按契约封装 API，页面可用 mock 兜底，不得阻塞主链。

## 8. 环境变量

不要在组件中写死端口。

```env
VITE_CUSTOMER_API=http://127.0.0.1:8081
VITE_PRODUCT_API=http://127.0.0.1:8082
VITE_BUSINESS_API=http://127.0.0.1:8083
VITE_ASSISTANT_API=http://127.0.0.1:8084
```

未来 Gateway 上线后切到统一 `/api`。

## 9. 数据类型规则

### BIGINT ID

后端 `Long/BIGINT` ID 前端统一按 `string` 处理。

### 金额

后端：`BIGINT`，单位分。前端必须统一使用 `src/utils/money.ts` 格式化，禁止页面自行 `/100`。

### 利率

后端：`BIGINT`，倍率 1,000,000；`15000 = 1.5%`。展示百分比使用 `rate / 10000`，统一走 `src/utils/rate.ts`。

### 币种

`001 = CNY/人民币/¥`，统一走 `src/utils/currency.ts`。

## 10. API 规则

组件/页面禁止直接调用 axios。统一：

```text
src/api/request.ts
src/api/assistant.ts
src/api/customer.ts
src/api/intention.ts
src/api/product.ts
```

`request.ts` 统一处理 Token、超时、401、网络异常、通用业务错误。

## 11. Pinia

优先只实现：

- `userStore`: user/token/login/logout
- `customerStore`: currentCustomer/eligibleProducts/select/switch/clear
- `conversationStore`: conversationId/messages/currentRequirement/plans/reset

切换客户必须 `conversationStore.reset()`。

## 12. 消息模型

至少支持：

```text
TEXT
PLAN
ERROR
SYSTEM
INTENTION_RESULT
```

建议拆 renderer，不要在单个 ChatMessage 中堆大量 v-if。

## 13. 代码质量

- TypeScript 类型优先，避免大面积 `any`。
- 页面层不做金融计算。
- 组件职责单一。
- API、types、stores、utils 分层清晰。
- 金额/利率/状态显示统一组件或工具。
- 业务异常要保留在聊天上下文，不要全部用瞬时 toast。

## 14. 开发顺序

1. 初始化工程
2. 安装 Element Plus/Pinia/Router/Axios
3. Layout + Router
4. types/utils/api/store
5. 登录页
6. AI 工作台骨架
7. 客户选择
8. 真实推荐接口
9. 方案卡片
10. 方案依据 Drawer
11. 创建意向 Modal
12. 真实创建意向接口
13. 意向结果卡
14. 意向列表/详情
15. 产品中心
16. 错误状态
17. 构建验证
18. README
