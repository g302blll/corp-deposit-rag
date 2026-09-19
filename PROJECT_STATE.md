# PROJECT_STATE.md

# 对公存款智能服务助手 - 当前项目状态

## 1. 当前阶段

当前阶段：

**V1.1 MVP Java 可运行版本已完成**

已经完成：

- V0.1 项目范围确认
- V0.2 业务边界
- V0.3 核心用例
- V0.4 客户分类/产品准入规则
- V0.5 产品期限/利率规则
- V0.6 数据库领域模型
- V0.7 核心数据库设计
- V0.8 四类存款业务规则
- V0.9 MySQL DDL
- V1.0 Java 领域服务初步设计
- V1.1 Maven 多模块与共享领域契约
- V1.1 客户、产品匹配/试算、办理意向、Mock AI 编排服务
- V1.1 MySQL Flyway 初始化、Nacos 注册与端到端冒烟验证

---

# 2. 当前目标

本阶段目标已完成：

> 完成第一个可以真正开始编码的 MVP Java 设计。

优先链路：

```text
客户经理
↓
选择客户
↓
输入自然语言资金需求
↓
LLM提取结构化需求
↓
查询客户大小类
↓
查询可办理产品
↓
查询期限
↓
查询执行利率
↓
Java生成候选存款组合方案
↓
Java收益试算
↓
LLM解释多个方案
↓
客户经理选择方案
↓
二次确认
↓
创建 deposit_intention
↓
创建 deposit_intention_detail
```

第一阶段完成到：

```text
“创建办理意向”
```

即可形成第一个 AI Agent 演示 MVP。

真实开户、支取作为下一阶段。

---

# 3. 已确认用户角色

V1：

```text
主要用户：银行客户经理
```

允许不选择客户时做通用知识问答。

选择客户后支持：

```text
这个客户能办理什么？
这个客户属于什么分类？
这个客户800万怎么存？
```

知识管理员负责：

- 上传知识文档
- 查看解析状态
- Chunk
- 启停知识

企业客户独立登录端暂不做。

---

# 4. 已确认产品范围

V1 只做：

```text
1 活期存款
2 定期存款
3 通知存款
4 大额存单
```

---

# 5. 已确认核心原则

## AI 与 Java

```text
LLM：
理解
参数提取
Agent编排
自然语言解释

Java：
准入
利率
金额
期限
计息
产品匹配
开户
支取
状态
数据库事务
```

---

## 产品匹配

不允许：

```text
LLM自己决定产品能不能办理
```

必须：

```text
LLM提取需求
↓
Java准入规则
↓
Java期限/最低金额规则
↓
Java利率
↓
Java试算
↓
Java候选方案
↓
LLM解释
```

---

# 6. 客户分类关键决策

大小类为两级主数据。

大类：

```text
INT
```

例：

```text
1 政府机构
2 事业单位
3 国有企业
4 民营企业
5 外资企业
```

小类：

```text
INT
```

例：

```text
101 政府/行政机关等
201 学校/医院/科研院所等
301 央企/地方国企/国有独资控股等
```

`customer` 当前只保存：

```text
small_category_id
```

客户大类通过小类归属查询。

---

# 7. 产品准入关键决策

关系：

```text
product_customer_scope
```

白名单：

```text
有记录 = 允许
无记录 = 禁止
```

目前关联：

```text
product_id
small_category_id
```

---

# 8. 产品期限关键决策

一个产品支持多个期限。

不同期限允许：

```text
不同最低起存金额
不同最低留存金额
```

字段：

```text
min_open_amount
min_retain_amount
```

通知存款期限还包含：

```text
notice_days
```

---

# 9. 利率关键决策

利率不是 DECIMAL。

统一：

```text
BIGINT
倍率 1,000,000
```

例：

```text
5000 = 0.5%
15000 = 1.5%
```

`deposit_product_rate` 必须包含：

```text
product_id
product_term_id
large_category_code
small_category_code
currency_code
interest_rate
effective_date
expire_date
```

客户大类必须参与利率匹配。

查不到利率：

```text
不允许默认兜底
不允许试算/办理
```

---

# 10. 币种关键决策

币种业务编码：

```text
001 = CNY
```

业务表保存：

```text
currency_code
```

---

# 11. 主键关键决策

所有核心表：

```text
sn_id BIGINT AUTO_INCREMENT
```

当前不使用雪花算法。

业务追踪依赖：

```text
transaction_no
certificate_no
account_no
instruction_no
detail_no
```

后续只有在：

- 分库分表
- 多数据库并行写
- 跨系统独立生成统一主键

时再考虑 Snowflake / 号段。

---

# 12. 金额关键决策

所有金额：

```text
BIGINT
单位：分
```

Java：

```text
Long持久化
BigDecimal计算
```

---

# 13. 办理意向关键决策

## deposit_intention

代表：

> 一个客户一次整体办理意向。

一条主单可以有多个产品明细。

状态：

```text
0 DRAFT
1 CONFIRMED
2 PROCESSING
3 PART_SUCCESS
4 SUCCESS
5 FAILED
6 CANCELLED
```

`PART_SUCCESS` 已确认保留。

---

## deposit_intention_detail

代表：

> 一笔具体产品办理意向。

状态：

```text
0 WAITING
1 PROCESSING
2 SUCCESS
3 FAILED
4 CANCELLED
```

---

# 14. 实际存款数据关键决策

真实业务不是 AI 专属。

来源可能：

```text
1 AI助手
2 柜面
3 手机银行
4 企业网银
9 其他
```

因此：

- 活期账户独立
- 定期存单独立
- 通知存款独立
- 大额存单独立

`intention_detail_id` 可以为 NULL。

---

# 15. 活期存款规则

表：

```text
demand_deposit_account
```

规则：

- 可随时存入
- 可随时部分/全部支取
- 无到期日
- 无提前支取概念
- 多次存入/支取
- 利率变化分段计息
- balance 表示当前余额

---

# 16. 定期存款规则

表：

```text
time_deposit_certificate
```

支持：

- 正常存入
- 正常到期
- 全额提前支取
- 部分提前支取
- 到期未支取
- 自动转存

提前支取：

```text
支取部分：
按实际存期 + 支取日活期利率

剩余部分：
继续原起息日 + 原利率 + 原到期日
```

若：

```text
剩余 < 最低留存金额
```

则整笔按提前支取结清。

---

# 17. 通知存款规则

表：

```text
notice_deposit_certificate
notice_withdraw_instruction
```

场景：

```text
正常支取
部分支取
未通知直接支取
提前支取
逾期支取
实际存期不足通知期限
实际支取金额少于通知金额
实际支取金额超过通知金额
已通知但最终不支取
通知后取消通知
```

正常通知支取：

```text
按支取日对应通知存款挂牌利率
按实际存期计息
```

未满足通知条件的相关部分：

```text
通常按支取日活期利率
```

部分支取：

```text
剩余 >= 最低留存
→ 原通知存款继续

剩余 < 最低留存
→ 剩余资金转活期
```

---

# 18. 大额存单规则

表：

```text
large_deposit_certificate
```

支持：

- 正常存入
- 全额提前支取
- 部分提前支取
- 正常到期
- 到期后未及时兑付

部分提前支取：

```text
支取部分：
按提前支取规则，当前业务假设常见为活期利率

剩余部分：
继续原起息日、原利率、原到期日
```

若：

```text
剩余 < 最低留存金额
```

则：

```text
整笔执行全额提前支取
```

---

# 19. 自动转存规则

自动转存：

```text
原存单到期
↓
原存单结束
↓
创建新存单
↓
新存单采用转存日最新利率
```

禁止覆盖旧存单。

新存单：

```text
renew_source_sn_id = 原存单sn_id
```

---

# 20. 统一交易流水

表：

```text
deposit_transaction
```

交易类型当前定义：

```text
OPEN
DEPOSIT
WITHDRAW
PARTIAL_EARLY_WITHDRAW
FULL_EARLY_WITHDRAW
NORMAL_MATURITY
NOTICE_WITHDRAW
CONVERT_TO_DEMAND
AUTO_RENEW
INTEREST_PAYMENT
```

所有本金金额保存正数。

---

# 21. 数据库文件

已经生成：

```text
corporate_deposit_ai_v0.9_mysql_ddl.sql
```

DDL 包含 16 张表：

```text
currency_dict
customer_large_category
customer_small_category
customer
deposit_product
deposit_product_term
product_customer_scope
deposit_product_rate
deposit_intention
deposit_intention_detail
demand_deposit_account
time_deposit_certificate
notice_deposit_certificate
notice_withdraw_instruction
large_deposit_certificate
deposit_transaction
```

---

# 22. 当前 Java 领域服务设计

已提出：

```text
CustomerService

ProductService
ProductEligibilityService
ProductRateService
ProductMatchService

InterestCalculationService

DepositIntentionService
DepositOpenService
DepositWithdrawService
NoticeInstructionService
DepositTransactionService
DepositMaturityService
DepositRenewService

AIOrchestratorService
KnowledgeRetrievalService
```

---

# 23. 计息策略设计

计划使用 Strategy 模式：

```text
InterestCalculator
├── DemandInterestCalculator
├── TimeDepositInterestCalculator
├── NoticeDepositInterestCalculator
└── LargeDepositInterestCalculator
```

统一入口：

```text
InterestCalculationService
```

通知存款额外：

```text
NoticeWithdrawRuleService
```

---

# 24. 开户策略设计

```text
DepositOpenHandler
├── DemandDepositOpenHandler
├── TimeDepositOpenHandler
├── NoticeDepositOpenHandler
└── LargeDepositOpenHandler
```

统一：

```text
DepositOpenService
```

开户基本流程：

```text
查询客户
↓
校验产品准入
↓
校验产品/期限
↓
校验最低起存金额
↓
获取执行利率
↓
计算业务参数
↓
创建账户/存单
↓
写OPEN交易流水
↓
更新AI意向（如果存在）
```

---

# 25. 支取策略设计

计划：

```text
WithdrawHandler
├── DemandWithdrawHandler
├── TimeDepositWithdrawHandler
├── NoticeDepositWithdrawHandler
└── LargeDepositWithdrawHandler
```

统一：

```text
DepositWithdrawService
```

涉及本金修改：

```text
@Transactional
+
数据库行锁 / 乐观锁
```

---

# 26. AI Tool 初步清单

```text
get_customer_profile
query_eligible_products
query_product_terms
query_product_rate
calculate_interest
match_deposit_plan
create_deposit_intention
query_deposit_certificate
```

AI 暂不允许直接：

```text
open_deposit
withdraw_deposit
```

---

# 27. 下一步

下一步进入：

**V1.1 MVP Java 代码级设计**

需要继续完成：

1. Maven 多模块工程结构
2. 每个服务的包结构
3. Maven 依赖
4. customer-service DTO / Entity / Mapper / Service
5. deposit-product-service DTO / Entity / Mapper / Service
6. `ProductEligibilityService`
7. `ProductRateService`
8. `ProductMatchService`
9. `InterestCalculationService`
10. AI Tool DTO 与接口
11. 第一条完整时序：
   ```text
   AI提问
   → Tool
   → Java业务服务
   → 产品方案
   → AI解释
   ```
12. 第一版 REST API
13. 第一版测试数据
14. 第一版可运行 Demo

---

# 28. 当前最优先任务

不要先开发完整开户/支取。

最优先：

```text
客户查询
+
客户分类
+
产品准入
+
产品期限
+
利率
+
产品匹配
+
收益试算
+
创建办理意向
```

跑通后再做：

```text
开户
存单
支取
通知指令
到期
自动转存
```

---

# 29. V1.1 最近变更（2026-09-19）

已落地 `common-core`、`common-web`、`customer-service`、`deposit-product-service`、`deposit-business-service` 和 `ai-assistant-service`。

完整链路已经跑通：

```text
自然语言需求（Mock 提取）
→ 客户及大小类查询
→ 产品白名单准入
→ 期限与最低起存金额
→ 业务日有效利率
→ Java 收益试算
→ 多候选方案
→ 幂等创建办理意向及明细
```

关键实现：

- Mock 是默认且当前唯一启用的需求提取器，不调用付费 API。
- 三个数据服务使用独立 Flyway 历史表，共用 `corporate_deposit_ai` 数据库但不跨服务读写业务表。
- 本地数据库密码只通过环境变量传入，未写入 Git。
- `mvnw.cmd clean package` 构建成功，12 个自动化测试通过。
- 四个服务已在本地 Nacos 注册成功。
- HTTP 冒烟验证覆盖客户查询、5 个候选方案、创建意向和幂等重试。

# 30. 下一步（V1.2）

优先进入模拟开户阶段：

1. 实现 `DepositOpenService` 统一入口。
2. 先实现定期存款开户处理器与 `time_deposit_certificate`。
3. 在同一事务内写 `OPEN` 交易流水并更新意向明细状态。
4. 增加数据库行锁/乐观锁和跨服务幂等测试。
5. 再扩展活期、通知存款和大额存单开户。

RAG、Gateway/JWT 和真实 LLM 接入继续后置，不阻塞确定性存款业务闭环。

# 31. V1.2 前端与一键启动已完成（2026-09-19）

- 项目已整理为 `corp-deposit-rag-web` 和 `corp-deposit-rag-server` 两个主目录。
- 前端使用 Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router、Axios。
- 已实现 Mock 登录、顶部导航、客户上下文、客户切换清会话、真实推荐、结构化方案卡、依据 Drawer、二次确认、幂等创建意向、意向/产品页面骨架。
- 推荐与创建意向走真实后端；登录、客户列表和产品列表暂用前端 Mock。
- 根目录新增 `start-all.bat` / `stop-all.bat`，会打包最新 JAR、测试并构建前端、启动服务、验证代理接口、打开网页及安全停止进程。
- 完整一键启动实测通过：后端 12 项测试、前端 7 项测试、npm 安全审计 0 漏洞、5 个端口就绪。
- 中文真实链路实测返回 5 个方案，并成功创建办理意向 `INT2026091918175179DE5598`。

# 32. 下一步

1. 补齐客户列表、意向列表/详情、产品列表/详情后端 GET 接口，替换前端 Mock。
2. 实现真实 JWT 登录与 Gateway 统一入口。
3. 继续模拟开户阶段：定期存款开户、OPEN 流水与意向状态更新。
4. RAG 与真实付费 LLM 继续后置，默认保持 Mock。
