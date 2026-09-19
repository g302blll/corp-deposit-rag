# FRONTEND_ARCHITECTURE.md

# 对公存款智能服务助手 - 前端架构与需求事实

## 1. 产品定位

前端定位：**银行客户经理 AI 存款顾问工作台**。

第一阶段核心展示：

```text
客户上下文
+ 自然语言需求
+ AI/Java 产品匹配
+ 结构化存款方案
+ 方案依据
+ 办理意向
```

## 2. 用户与导航

主要用户：银行客户经理。

顶部主导航：

```text
AI工作台 | 办理意向 | 产品中心                       张经理
```

不使用大型左侧菜单。

## 3. 路由

```text
/login
/assistant
/intentions
/intentions/:id
/products
/products/:id
```

## 4. 登录页

简单客户经理登录。登录成功直接进入 `/assistant`。

页面建议左右布局：左侧项目说明/能力点，右侧工号、密码、登录按钮。

## 5. AI 工作台

布局：左侧约 300px 客户上下文，右侧 AI 会话主区。

### 未选择客户

显示：

```text
当前未选择客户
选择客户后可以进行：
- 产品准入分析
- 客户执行利率查询
- 个性化存款方案推荐
[选择客户]
```

右侧仍允许通用咨询。

### 已选择客户

显示：

- 客户名称
- 客户编号
- 客户大类
- 客户小类
- 客户等级
- 客户经理
- 可办理产品
- 切换客户

## 6. 客户选择

使用 Drawer/Modal，不跳页。

字段：客户编号、客户名称、大类、小类、等级、客户经理。

切换已有客户时必须确认并清空当前会话。

## 7. AI 会话

用户消息靠右，AI 靠左。输入支持 Enter 发送、Shift+Enter 换行、发送中禁止重复提交。

建议快捷问题：

```text
800万存一年，优先收益
500万，需要保持一定流动性
这个客户能办理哪些产品？
```

## 8. 推荐方案

推荐结果必须是结构化卡片，支持组合方案。

示例：

```text
方案A｜收益优先
总金额：800万元
预计收益：12万元

单位定期存款
800万元 · 1年 · 1.50%
预计收益：12万元

[查看依据] [选择方案]
```

组合方案：

```text
600万 → 1年定期
200万 → 7天通知存款
```

## 9. 方案详情 Drawer

建议宽 480~560px，展示：

- 客户依据
- 产品依据
- 准入结果
- 产品期限
- 最低起存金额
- 最低留存金额
- 利率匹配条件
- 生效/失效日期
- 本金、利率、期限、预计收益

前端只展示后端确定性结果，不自行重算金融结果。

## 10. 创建办理意向

选择方案后弹出中等尺寸 Modal。展示客户、总金额、明细、利率、预计利息，并提示不会直接执行资金交易。

确认后调用：

```text
POST /api/v1/assistant/intentions
```

## 11. 意向成功卡

展示：

- 交易流水号
- 客户
- 总金额
- 状态
- 查看详情

幂等命中已有记录不视为错误。

## 12. 办理意向列表

筛选：交易流水号、客户名称、状态、开始/结束日期。

表格：交易流水号、客户编号、客户名称、总金额、状态、客户经理、创建时间、确认时间、操作。

状态：草稿、已确认、办理中、部分成功、成功、失败、已取消。

## 13. 办理意向详情

主信息：交易流水号、客户、大小类、总金额、状态、客户经理、创建/确认时间、备注。

明细：明细编号、产品、类型、期限、币种、金额、利率、预计利息、状态、失败原因。

## 14. 产品中心

第一阶段只读。

产品列表：产品编码、名称、类型、币种、状态。

详情包含：基础信息、支持期限、客户准入、客户分类执行利率。

## 15. 服务映射

```text
customer-service :8081        客户及大小类
deposit-product-service :8082 产品/期限/准入/利率/匹配/试算
deposit-business-service :8083 办理意向
ai-assistant-service :8084    Mock参数提取/Tool编排
```

AI 工作台优先调用 8084。

## 16. 核心 TypeScript 契约

```ts
export interface PlanRequest {
  customerNo?: string
  message: string
}

export interface DepositRequirement {
  totalAmountInCents: number
  expectedTermValue?: number
  expectedTermUnit?: 'DAY' | 'MONTH' | 'YEAR'
  liquidityAmountInCents?: number
  liquidityLevel?: 'LOW' | 'MEDIUM' | 'HIGH'
  preference?: 'YIELD' | 'LIQUIDITY' | 'BALANCED'
  currencyCode: string
}

export interface RateInfo {
  rateId?: string
  largeCategoryCode: number
  largeCategoryName?: string
  smallCategoryCode: number
  smallCategoryName?: string
  currencyCode: string
  interestRate: number
  effectiveDate: string
  expireDate?: string
}

export interface DepositPlanDetail {
  productId: string
  productCode: string
  productName: string
  productType: number
  productTermId: string
  termCode?: string
  termName?: string
  currencyCode: string
  amountInCents: number
  interestRate: number
  expectedInterestInCents: number
  minOpenAmountInCents?: number
  minRetainAmountInCents?: number
  eligible: boolean
  eligibilityMessage?: string
  rateInfo?: RateInfo
}

export interface DepositPlan {
  planId: string
  planName: string
  planType?: 'YIELD' | 'LIQUIDITY' | 'BALANCED'
  description?: string
  totalAmountInCents: number
  totalExpectedInterestInCents: number
  details: DepositPlanDetail[]
}

export interface PlanResponse {
  conversationId?: string
  extractedRequirement: DepositRequirement
  message: string
  plans: DepositPlan[]
}
```

## 17. 创建意向契约

```ts
export interface CreateIntentionDetail {
  productId: string
  productTermId: string
  amountInCents: number
  interestRate: number
  expectedInterestInCents: number
  currencyCode: string
}

export interface CreateIntentionRequest {
  idempotencyKey: string
  customerNo: string
  requirementText: string
  conversationId?: string
  planId?: string
  details: CreateIntentionDetail[]
}
```

## 18. 格式化规则

金额：后端分 → `¥8,000,000.00` / `800万元`。

利率：`15000 → 1.50%`。

币种：`001 → 人民币 / CNY / ¥`。

## 19. 必须覆盖的异常

```text
未选择客户
客户不存在/停用
无产品准入
无有效利率
金额低于最低起存金额
Mock无法识别需求
服务不可用/超时
创建意向失败
幂等重复提交
Token失效
```

业务异常必须展示业务含义，不要全部显示“系统错误”。
