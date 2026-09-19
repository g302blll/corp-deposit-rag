# CLAUDE.md

# 对公存款智能服务助手 - AI 工作规则

## 1. 文件用途

本文件用于告诉 AI / Claude Code 在本项目中应该如何工作。

进入新上下文时，AI 必须优先阅读：

1. `CLAUDE.md`
2. `ARCHITECTURE.md`
3. `PROJECT_STATE.md`

读取顺序不可颠倒。

其中：

- `CLAUDE.md`：定义 AI 工作规范、开发规范、目录约定、技术栈、禁止事项。
- `ARCHITECTURE.md`：记录已经确认的系统设计事实。
- `PROJECT_STATE.md`：记录当前开发阶段、已完成事项、待办事项和下一步。

如三份文档之间存在冲突：

1. 以用户最新明确指令为最高优先级。
2. 其次以 `PROJECT_STATE.md` 中的“关键决策”与“最近变更”为准。
3. 再以 `ARCHITECTURE.md` 为准。
4. `CLAUDE.md` 主要约束工作方式，不用于覆盖业务事实。

---

# 2. 项目目标

项目名称：

**对公存款智能服务助手**

项目目标：

构建一个可真实运行、可用于面试展示的 Java + LLM 企业级 AI 项目。

系统面向银行客户经理，兼顾企业客户场景，通过：

- LLM
- RAG
- Agent / Tool Calling
- Java 确定性业务服务
- MySQL
- Redis
- Spring Cloud

实现：

- 对公存款知识问答
- 客户信息查询
- 产品准入查询
- 产品智能匹配
- 利率查询
- 收益试算
- 办理意向生成
- 模拟真实存款业务
- 存单/账户查询
- 通知存款支取等业务能力

核心原则：

> LLM 负责理解、编排和自然语言解释；客户准入、利率、金额、计息、开户、支取、状态流转等确定性业务必须由 Java 服务执行。

---

# 3. AI 工作方式

## 3.1 不允许直接脑补业务规则

遇到未确认的金融业务规则时：

- 不得自行假设为事实。
- 必须在设计中标记为 `待确认`。
- 如已有用户明确规则，以用户规则为准。
- 不得用“行业通常如此”覆盖用户已经定义的规则。

---

## 3.2 不重复询问已经确认的内容

以下内容一旦进入 `PROJECT_STATE.md -> 已确认/关键决策`，除非用户主动修改，否则视为冻结。

AI 不得重复询问。

---

## 3.3 设计优先级

设计时依次遵循：

1. 业务正确性
2. 数据一致性
3. 可落地性
4. 可维护性
5. 可扩展性
6. 技术展示价值
7. 避免无意义的技术堆砌

---

## 3.4 先完成 MVP，再扩展复杂业务

开发优先顺序：

1. AI 产品咨询/匹配 MVP
2. 创建办理意向
3. 模拟办理并生成真实业务数据
4. 存款开户/存单
5. 支取/计息
6. RAG 知识库
7. 完善监控、安全、DevOps

不得为了完整覆盖所有银行业务而阻塞 MVP。

---

# 4. 技术栈约定

建议技术栈：

- JDK 17
- Spring Boot 3.x
- Spring Cloud
- Spring Cloud Alibaba / Nacos
- Spring Cloud Gateway
- Spring Security + JWT
- MyBatis / MyBatis-Plus
- MySQL 8.x
- Redis
- Spring AI
- RocketMQ / Kafka：第二阶段按需加入
- 向量数据库：Milvus / pgvector / Elasticsearch，后续择一
- Vue 3
- Docker / Docker Compose
- Prometheus + Grafana：后续阶段
- ELK / Loki：后续阶段

不允许为了展示“微服务”把系统过度拆分。

---

# 5. Java 开发规范

## 5.1 主键

数据库所有核心表统一：

```sql
sn_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY
```

当前阶段不使用雪花算法。

原因：

- 数据集中落 MySQL。
- 数据库内部主键无需跨库生成。
- 跨服务追踪使用业务编号。
- 后续真正分库分表时，再评估 Snowflake / 号段模式。

---

## 5.2 金额

所有金额数据库字段：

```text
BIGINT
单位：分
```

例如：

```text
500万元 = 500000000 分
```

Java：

- 持久化使用 `Long`
- 计算过程使用 `BigDecimal`
- 禁止使用 `double` / `float` 进行金额计算

建议统一封装：

```java
MoneyUtils
```

禁止在业务代码中到处手工 `/ 100`。

---

## 5.3 利率

利率统一：

```text
BIGINT
倍率：1,000,000
```

示例：

```text
5000  = 0.5%
10000 = 1.0%
13500 = 1.35%
15000 = 1.5%
```

实际计算：

```text
interest_rate / 1,000,000
```

Java 中转换为 `BigDecimal` 后计算。

禁止使用 `double` / `float`。

---

## 5.4 币种

币种使用银行业务编码：

```text
001 = CNY
```

数据库字段统一：

```text
currency_code VARCHAR(...)
```

不要直接保存 `"CNY"` 作为业务主编码。

可以通过币种字典映射：

```text
001 -> 人民币 -> CNY
```

---

## 5.5 枚举

数据库状态类字段可使用 `INT`。

Java 必须使用 Enum 封装，禁止出现大量魔法数字：

```java
DepositType.TIME
IntentionStatus.PART_SUCCESS
TransactionType.PARTIAL_EARLY_WITHDRAW
```

---

## 5.6 事务

涉及以下操作必须使用数据库事务：

- 开户
- 生成存单
- 支取
- 本金变更
- 计息结算
- 状态流转
- 写交易流水

资金类修改优先考虑：

```sql
SELECT ... FOR UPDATE
```

数据库事务是最终一致性保障。

Redis 分布式锁只能作为多实例下的辅助机制，不能替代数据库一致性控制。

---

## 5.7 幂等

所有跨服务写操作必须考虑：

- 接口重试
- MQ 重复消费
- 网络超时后重发
- AI Tool 重复调用

优先使用：

- 唯一业务号
- 唯一索引
- 幂等表/幂等键
- 状态机校验

---

# 6. AI / Agent 规则

AI 不得直接：

- 修改客户分类
- 修改产品准入
- 修改执行利率
- 直接操作存单表
- 直接执行真实资金支取
- 绕过业务校验写数据库

AI 可以：

- 查询客户
- 查询可办理产品
- 查询期限
- 查询执行利率
- 调用 Java 试算
- 调用 Java 产品匹配
- 创建办理意向
- 查询存单/账户
- 调用知识库检索

V1 约定：

> AI 最多创建办理意向；正式开户、支取等核心资金动作由确定性业务系统执行。

---

# 7. RAG 规则

以下属于结构化业务事实，必须从业务数据库/API获取，不得依赖 RAG：

- 客户属于哪个大小类
- 某客户可以办理哪些产品
- 产品期限
- 最低金额
- 当前执行利率
- 客户账户
- 存单
- 交易状态

以下适合 RAG：

- 产品说明
- 办理规则解释
- 业务管理办法
- 操作规程
- FAQ
- 制度依据

没有可靠 RAG 依据时，不允许 LLM 自由编造制度答案。

---

# 8. 推荐项目目录

```text
corporate-deposit-ai/
├── CLAUDE.md
├── ARCHITECTURE.md
├── PROJECT_STATE.md
├── docs/
│   ├── sql/
│   ├── api/
│   ├── design/
│   └── rag/
│
├── gateway-service/
├── customer-service/
├── deposit-product-service/
├── deposit-business-service/
├── ai-assistant-service/
├── knowledge-service/
│
├── common-core/
├── common-web/
├── common-security/
└── common-ai/
```

---

# 9. 核心服务职责

```text
gateway-service
    JWT / 路由 / 限流

customer-service
    客户 / 客户大类 / 客户小类

deposit-product-service
    产品 / 期限 / 客户准入 / 利率 / 产品匹配

deposit-business-service
    意向 / 开户 / 存单 / 活期账户 / 支取 / 通知 / 计息 / 交易流水

ai-assistant-service
    LLM / Agent / Tool Calling / 会话上下文

knowledge-service
    文档 / Chunk / Embedding / RAG
```

---

# 10. 禁止事项

禁止：

- 使用 `double` 保存或计算金额和利率
- AI 直接生成金融计算结果并视为事实
- AI 自行判断客户是否有产品准入资格
- AI 自行编造利率
- AI 绕开 Java 服务直接操作核心业务表
- 为了“像微服务”而过度拆服务
- 在当前阶段无必要地引入雪花主键
- 直接覆盖历史存单的利率、起息日、到期日
- 自动转存时修改原存单作为新一期存单
- 删除历史利率记录后覆盖成新利率
- 将客户分类、产品准入等主数据放到知识库替代结构化查询

---

# 11. 如何更新项目文档

发生架构变化时：

更新：

```text
ARCHITECTURE.md
```

发生开发进度变化时：

更新：

```text
PROJECT_STATE.md
```

只有工作方式、技术规范发生变化时才修改：

```text
CLAUDE.md
```

每完成一个阶段，都要更新 `PROJECT_STATE.md`。

