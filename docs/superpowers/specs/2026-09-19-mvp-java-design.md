# V1.1 MVP Java 代码级设计

## 目标

交付一个可运行的 Java 17 + Spring Cloud MVP，跑通客户查询、产品准入、期限与执行利率查询、产品匹配、收益试算和办理意向创建。默认且当前仅使用确定性 Mock LLM，不产生外部 API 费用。

## 范围

本轮包含：

- Maven 多模块工程和 Maven Wrapper
- `common-core`、`common-web`
- `customer-service`
- `deposit-product-service`
- `deposit-business-service`
- `ai-assistant-service`
- Nacos 服务注册、MySQL 持久化、Flyway 初始化数据
- 第一条端到端 REST 链路和自动化测试

本轮不包含 Gateway、JWT、RAG、开户、存单、支取、通知指令、到期和自动转存。

## 架构

服务间只通过 HTTP 契约交互，不跨服务访问其他服务的数据表。当前共用一个 MySQL 实例和 `corporate_deposit_ai` 数据库，但表所有权保持清晰：客户表归 `customer-service`，产品配置表归 `deposit-product-service`，意向表归 `deposit-business-service`。

`ai-assistant-service` 是编排层。它将自然语言转换为结构化需求，调用客户、产品和意向服务，并组织结果；它不直接访问金融业务表。为了使测试和本地 Demo 可重复，当前版本只启用固定规则提取金额、期限和流动性偏好的 Mock 实现。真实 Anthropic 客户端延后到确有需要时再增加，避免产生 API 费用。

## 模块职责

### common-core

保存跨服务稳定值对象、枚举、金额和利率工具，以及 API 契约 DTO。金额为分，利率倍率为 1,000,000，计算全程使用 `BigDecimal`。

### common-web

保存统一响应和异常处理。业务失败返回稳定错误码，不泄露堆栈和密钥。

### customer-service

按客户编号查询客户及两级分类；不存在时返回明确的 `CUSTOMER_NOT_FOUND`。

### deposit-product-service

按客户小类执行白名单准入，查询有效期限和指定业务日的唯一执行利率。匹配服务按最低起存金额、期限偏好和流动性要求过滤候选项，再调用 Java 计息服务生成候选方案；无有效利率的候选项被排除且不使用默认利率。

### deposit-business-service

在一个数据库事务内创建意向主单和明细。请求携带幂等键，数据库唯一索引保证重试不重复创建。

### ai-assistant-service

提供 `/api/v1/assistant/plans` 和 `/api/v1/assistant/intentions`。前者编排客户与产品服务，后者在用户确认后创建意向。Mock 解析器只负责提取需求，不做准入、利率或计息判断。

## 数据流

1. 客户经理提交客户编号和自然语言需求。
2. Mock 解析器提取金额、期限、流动性偏好和币种。
3. 编排器从客户服务取得分类。
4. 编排器把结构化需求与客户分类交给产品匹配服务。
5. 产品服务依次执行准入、期限、最低金额、有效利率和 Java 试算。
6. 编排器返回多个候选方案及确定性说明。
7. 客户经理选择方案并提交幂等键。
8. 业务服务事务性创建意向主单与明细。

## 计息规则

MVP 仅提供“预期收益试算”，不是开户结算。按 `本金 × 年利率 × 实际天数 / 365` 计算，使用 `RoundingMode.HALF_UP` 舍入到分。期限被明确转换为天数；该规则仅用于本项目 Demo，后续接入真实业务规则时通过策略接口替换。

## 错误处理

- 客户不存在：停止编排。
- 无准入产品或无有效利率：返回空候选与原因，不编造方案。
- 下游超时或不可用：返回 `DOWNSTREAM_UNAVAILABLE`，不自动创建意向。
- 重复幂等键：返回首次创建的意向。
- Mock 模式不读取任何外部模型密钥，也不会发起付费 API 请求。

## 测试

- `common-core`：金额、利率与利息舍入单元测试。
- 产品服务：白名单、最低金额、有效期利率和无利率排除测试。
- 业务服务：多明细事务创建和幂等测试。
- AI 服务：Mock 解析与编排测试，HTTP 客户端使用本地 stub，不调用付费 API。
- 根工程执行 `mvn test`，并对四个服务执行启动检查。

## 版本选择

- Java 17
- Spring Boot 3.2.9
- Spring Cloud 2023.0.3
- Spring Cloud Alibaba 2023.0.1.2
- MyBatis-Plus 3.5.7
- MySQL 8.x、Flyway、JUnit 5、Testcontainers（集成测试按环境启用）

该组合遵循 Spring Cloud Alibaba 2023.x 对 Spring Boot 3.2.x 和 JDK 17 的兼容范围。

