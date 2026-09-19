# FRONTEND_PROJECT_STATE.md

# 对公存款智能服务助手 - 前端当前状态

## 1. 当前阶段

已完成前端设计：

```text
V0.1 目标/角色/风格
V0.2 用户流程与页面边界
V0.3 页面字段级需求
V0.4 UI原型与视觉布局
V0.5 接口映射与TypeScript数据模型
```

V1.2 Vue 前端工程与核心 MVP 已完成，推荐及创建意向真实链路已联调通过。

## 2. 已确认需求

- 简单客户经理登录。
- 登录后直接 `/assistant`。
- AI 工作台左客户、右对话。
- 未选择客户可通用咨询。
- 选择客户后做个性化准入/利率/方案。
- 切换客户开启新会话。
- 推荐方案嵌入聊天流。
- 方案支持组合产品。
- 方案依据使用 Drawer。
- 创建意向必须二次确认。
- 创建意向具备幂等 key。
- 产品中心第一阶段只读。
- PC 优先。

## 3. 后端状态

后端 MVP 已完成，技术：Java 17、Spring Boot、Spring Cloud Alibaba、MyBatis-Plus、MySQL、Nacos、Mock 自然语言需求提取。

服务：

```text
customer-service            8081
deposit-product-service     8082
deposit-business-service    8083
ai-assistant-service        8084
```

已确认接口：

```text
POST /api/v1/assistant/plans
POST /api/v1/assistant/intentions
```

推荐示例：

```powershell
curl.exe http://127.0.0.1:8084/api/v1/assistant/plans `
  -H "Content-Type: application/json" `
  -d '{"customerNo":"CUST001","message":"有800万资金想存一年，优先收益，可以长期不用"}'
```

创建意向示例：

```powershell
curl.exe http://127.0.0.1:8084/api/v1/assistant/intentions `
  -H "Content-Type: application/json" `
  -d '{"idempotencyKey":"demo-001","customerNo":"CUST001","requirementText":"800万存一年","details":[{"productId":2,"productTermId":23,"amountInCents":800000000,"interestRate":15000,"expectedInterestInCents":12000000,"currencyCode":"001"}]}'
```

相同 `idempotencyKey` 重复提交返回原意向，不重复落库。

## 4. 当前接口缺口

完整前端还需确认/补齐：

```text
GET 客户列表
GET 客户详情
GET 客户可办理产品
GET 办理意向列表
GET 办理意向详情
GET 产品列表
GET 产品详情/期限/准入/利率
```

缺口不能阻塞 AI 工作台。允许先封装契约并用 mock 数据。

## 5. 目标 MVP 演示链路

```text
登录
→ 选择 CUST001
→ 输入“有800万资金想存一年，优先收益，可以长期不用”
→ 调用真实 /assistant/plans
→ 方案卡展示
→ 查看依据 Drawer
→ 选择方案
→ 二次确认
→ 调用真实 /assistant/intentions
→ 显示交易流水号
```

该链路优先级最高。

## 6. 下一阶段顺序

MVP 后再做：

```text
真实客户查询
→ 意向列表/详情真实接口
→ 产品中心真实接口
→ 登录真实JWT
→ RAG知识库
→ 存单/业务查询
```
