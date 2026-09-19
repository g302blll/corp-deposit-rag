# FRONTEND_IMPLEMENTATION_GUIDE.md

# Claude Code 前端实施清单

## 1. 直接创建工程

工程名建议：

```text
corporate-deposit-ai-web
```

建议命令：

```bash
npm create vite@latest corporate-deposit-ai-web -- --template vue-ts
cd corporate-deposit-ai-web
npm install
npm install element-plus pinia vue-router axios
```

不要重新询问需求，直接实施。

## 2. 推荐目录

```text
corporate-deposit-ai-web/
├── .env.development
├── .env.example
├── README.md
├── src/
│   ├── api/
│   │   ├── request.ts
│   │   ├── assistant.ts
│   │   ├── customer.ts
│   │   ├── intention.ts
│   │   └── product.ts
│   ├── components/
│   │   ├── assistant/
│   │   │   ├── AssistantInput.vue
│   │   │   ├── ChatMessage.vue
│   │   │   ├── TextMessage.vue
│   │   │   ├── PlanMessage.vue
│   │   │   ├── ErrorMessage.vue
│   │   │   ├── IntentionResultMessage.vue
│   │   │   ├── DepositPlanCard.vue
│   │   │   ├── DepositPlanDetailDrawer.vue
│   │   │   └── IntentionConfirmDialog.vue
│   │   ├── customer/
│   │   │   ├── CustomerContextPanel.vue
│   │   │   └── CustomerSelectorDrawer.vue
│   │   └── common/
│   │       ├── StatusTag.vue
│   │       ├── MoneyText.vue
│   │       ├── RateText.vue
│   │       └── EmptyState.vue
│   ├── layout/MainLayout.vue
│   ├── router/index.ts
│   ├── stores/
│   │   ├── user.ts
│   │   ├── customer.ts
│   │   └── conversation.ts
│   ├── types/
│   │   ├── api.ts
│   │   ├── assistant.ts
│   │   ├── customer.ts
│   │   ├── intention.ts
│   │   └── product.ts
│   ├── utils/
│   │   ├── money.ts
│   │   ├── rate.ts
│   │   ├── currency.ts
│   │   ├── idempotency.ts
│   │   └── date.ts
│   ├── views/
│   │   ├── login/index.vue
│   │   ├── assistant/index.vue
│   │   ├── intention/index.vue
│   │   ├── intention/detail.vue
│   │   ├── product/index.vue
│   │   └── product/detail.vue
│   ├── App.vue
│   ├── main.ts
│   └── style.css
```

## 3. 环境变量

`.env.development`：

```env
VITE_CUSTOMER_API=http://127.0.0.1:8081
VITE_PRODUCT_API=http://127.0.0.1:8082
VITE_BUSINESS_API=http://127.0.0.1:8083
VITE_ASSISTANT_API=http://127.0.0.1:8084
```

## 4. Router

实现：

```text
/login
/assistant
/intentions
/intentions/:id
/products
/products/:id
```

- `/login` 不用 MainLayout。
- 其他页面使用 MainLayout。
- 未登录跳 `/login`。
- 登录后默认 `/assistant`。

## 5. 登录

第一阶段可前端 mock：

```text
zhangsan / 123456
```

成功后：保存 mock token + 当前用户，跳 `/assistant`。

## 6. MainLayout

顶部：Logo/系统名、AI工作台、办理意向、产品中心、当前用户、退出。

不要大侧边栏。

## 7. 核心 types

按 `FRONTEND_ARCHITECTURE.md` 定义：

- ApiResponse
- CustomerSummary / CustomerDetail
- DepositRequirement
- RateInfo
- DepositPlan / DepositPlanDetail
- PlanRequest / PlanResponse
- CreateIntentionRequest / Detail
- IntentionResult
- ChatMessage

所有后端 BIGINT ID 前端用 string。

## 8. API

### assistant.ts

实现：

```ts
getPlans(request)
createIntention(request)
```

真实地址：

```text
POST ${VITE_ASSISTANT_API}/api/v1/assistant/plans
POST ${VITE_ASSISTANT_API}/api/v1/assistant/intentions
```

如果后端实际 JSON 与前端目标模型略不同，必须在 API 适配层转换，页面不要直接适配后端脏结构。

### customer/intention/product

先封装接口契约；若后端缺失则开发环境 mock。

## 9. 客户 mock

至少提供：

```text
CUST001
XX国有企业
大类：3 国有企业
小类：301 央企、地方国企、国有独资/控股企业
等级：A
客户经理：张经理
```

```text
CUST002
XX人民医院
大类：2 事业单位
小类：201 学校、医院、科研院所、文化事业单位等
```

## 10. Stores

### userStore

`user/token/login/logout`

### customerStore

`currentCustomer/eligibleProducts/selectCustomer/clearCustomer/switchCustomer`

切换客户时重置 conversation。

### conversationStore

`conversationId/messages/currentRequirement/plans/loading/sendMessage/appendMessage/setPlans/reset`

工作台页面不要堆所有逻辑。

## 11. AI 工作台

组成：

```text
CustomerContextPanel
+
AI Conversation Area
```

会话区：消息列表 + AssistantInput。

初始文案：

> 您好，我可以帮助分析客户资金需求、产品准入、执行利率与预计收益。

## 12. 推荐调用流程

```text
用户输入
→ append USER
→ assistantApi.getPlans()
→ append ASSISTANT TEXT
→ append PLAN
→ DepositPlanCard
```

## 13. DepositPlanCard

必须支持 `details.length >= 1`，包括组合方案。

展示：方案名/类型、总金额、总预计收益、每条明细产品/金额/期限/利率/预计利息、查看依据、选择方案。

## 14. 方案详情 Drawer

尽量使用 PlanResponse 已返回数据，不要无意义重复请求。

## 15. 创建意向 Dialog

打开时生成一次 `idempotencyKey`。开始提交后，所有重试复用该 key。

提交结构：

```ts
{
  idempotencyKey,
  customerNo,
  requirementText,
  conversationId,
  planId,
  details: plan.details.map(...)
}
```

成功：关闭 Modal，追加 `INTENTION_RESULT` 消息。

若后端返回原意向（幂等命中），仍作为成功展示。

## 16. 金额/利率/币种工具

`money.ts`：`formatMoney()`、`formatMoneyShort()`。

`rate.ts`：`formatRate(15000) -> 1.50%`。

`currency.ts`：`001 -> 人民币/CNY/¥`。

页面禁止自行换算。

## 17. 意向/产品页面

如果真实 GET 接口未完成：先完成页面骨架和 mock，API 文件保留真实调用位置，README 标记 TODO。

不要阻塞 AI 主链。

## 18. 错误处理

工作台业务错误建议追加 ERROR 消息，例如：

```text
当前客户暂无有效执行利率。
```

网络错误可提示：

```text
服务暂时不可用，请稍后重试。
```

关键业务错误不要只用 ElMessage 一闪而过。

## 19. 样式

建议 CSS Variables：

```text
--primary
--page-bg
--card-bg
--text-primary
--text-secondary
--border-color
--success
--warning
--danger
```

圆角 6~10px，轻阴影。

## 20. 验收标准

### 构建

```bash
npm install
npm run dev
npm run build
```

通过。

### 页面

`/login`、`/assistant`、`/intentions`、`/products` 可访问。

### 主链

```text
登录
→ 选择 CUST001
→ 输入 800万示例
→ 真实调用 /assistant/plans
→ 显示方案卡
→ 查看依据
→ 选择方案
→ 确认
→ 真实调用 /assistant/intentions
→ 显示交易流水号
```

### 代码

- 无大面积 any
- 无组件直接 axios
- 无页面自行金额/利率计算
- 切换客户清空会话
- 创建意向幂等重试逻辑正确

## 21. README 必须记录

- 技术栈
- Node 版本
- 安装/启动/构建命令
- 环境变量
- 后端依赖服务
- Demo 账号
- Demo 操作步骤
- 已实现功能
- 当前 mock 项
- 待补后端接口

## 22. Claude Code 执行顺序

```text
1 创建工程
2 安装依赖
3 建目录
4 types/utils/api/stores/router
5 登录页
6 MainLayout
7 AI工作台
8 Mock客户选择
9 接真实推荐接口
10 方案卡
11 详情Drawer
12 创建意向Modal
13 接真实创建接口
14 意向结果卡
15 意向/产品页面骨架
16 npm run build
17 修复错误
18 更新README
```

完成后报告：创建文件、真实联调接口、仍在 mock 的内容、构建结果、下一步。
