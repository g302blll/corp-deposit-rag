# ARCHITECTURE.md

# 对公存款智能服务助手 - 系统设计事实

## 1. 项目定位

项目名称：

**对公存款智能服务助手**

主要用户：

- V1 主要用户：银行客户经理
- 企业客户场景兼顾，但 V1 不单独实现企业客户登录端

系统定位：

> 企业级 AI Copilot + 对公存款业务服务。

系统不是简单的 RAG 聊天机器人，而是：

```text
LLM
+
RAG
+
Agent / Tool Calling
+
Java 确定性业务服务
+
模拟真实银行存款业务
```

---

# 2. V1 核心能力

已确认：

1. 对公存款知识问答
2. 企业客户查询
3. 客户产品准入查询
4. 产品期限查询
5. 产品执行利率查询
6. 产品智能匹配
7. 存款收益试算
8. AI 多轮对话
9. 创建存款办理意向
10. 知识库管理
11. 模拟存款开户
12. 活期账户
13. 定期存单
14. 通知存款
15. 大额存单
16. 统一交易流水

---

# 3. V1 不做

当前明确不作为第一阶段范围：

- 真实银行核心系统
- 真实资金交易
- 企业实名认证
- 短信验证码
- 语音客服
- OCR 材料审核
- 手机 App
- 微信小程序
- 复杂工作流审批

---

# 4. 系统架构

```text
                         Vue Web
                            │
                            ▼
                     API Gateway
                            │
        ┌───────────────────┼────────────────────┐
        │                   │                    │
        ▼                   ▼                    ▼
ai-assistant-service  customer-service   deposit-product-service
        │                   │                    │
        ▼                   │                    │
knowledge-service           └──────────┬─────────┘
                                       │
                                       ▼
                              deposit-business-service
                                       │
                                       ▼
                                     MySQL
```

辅助基础设施：

```text
Redis
Nacos
Vector DB
Docker
```

---

# 5. 服务职责

## gateway-service

负责：

- API 统一入口
- JWT 验证
- 路由
- 限流
- 基础审计

---

## customer-service

负责：

- 客户查询
- 客户大类
- 客户小类
- 客户经理关系

领域对象：

```text
Customer
CustomerLargeCategory
CustomerSmallCategory
```

---

## deposit-product-service

负责：

- 产品
- 产品期限
- 最低起存金额
- 最低留存金额
- 客户准入
- 客户分类执行利率
- 产品匹配

核心领域服务：

```text
ProductService
ProductEligibilityService
ProductRateService
ProductMatchService
```

---

## deposit-business-service

负责：

- 办理意向
- 开户
- 活期账户
- 定期存单
- 通知存款
- 大额存单
- 支取
- 通知指令
- 到期
- 自动转存
- 计息
- 交易流水

核心服务：

```text
DepositIntentionService
DepositOpenService
DepositWithdrawService
NoticeInstructionService
DepositTransactionService
InterestCalculationService
DepositMaturityService
DepositRenewService
```

---

## ai-assistant-service

负责：

- LLM
- Prompt
- 会话
- 上下文
- Agent
- Tool Calling
- 将用户自然语言转换为结构化参数
- 调用 Java 业务服务
- 将确定性结果组织成自然语言

不允许直接写核心金融业务表。

---

## knowledge-service

负责：

- 文档上传
- 解析
- Chunk
- Embedding
- Vector DB
- Rerank
- RAG 检索
- 引用来源

---

# 6. 客户分类模型

客户采用两级分类。

大类编码为 INT，例如：

```text
1 政府机构
2 事业单位
3 国有企业
4 民营企业
5 外资企业
```

小类编码为 INT，例如：

```text
101 各级政府、行政机关、财政局、税务局等
201 学校、医院、科研院所、文化事业单位等
301 央企、地方国企、国有独资/控股企业
```

关系：

```text
customer_large_category
    1:N
customer_small_category
    1:N
customer
```

`customer` 只保存：

```text
small_category_id
```

大类通过小类查询。

---

# 7. 客户产品准入

客户分类决定产品准入。

模型：

```text
product_customer_scope
```

当前采用白名单模式：

```text
有记录 = 可以办理
无记录 = 不可以办理
```

关联维度：

```text
product_id
small_category_id
```

LLM 不得参与准入判断。

---

# 8. 产品模型

V1 产品范围：

```text
1 活期存款
2 定期存款
3 通知存款
4 大额存单
```

配置层：

```text
deposit_product
deposit_product_term
product_customer_scope
deposit_product_rate
```

---

# 9. 产品期限

一个产品支持多个期限。

示例：

```text
定期：
3M
6M
1Y

通知：
1D
7D
```

产品期限包含：

```text
term_code
term_name
term_value
term_unit
min_open_amount
min_retain_amount
notice_days
```

不同期限可以有不同最低起存金额。

---

# 10. 金额与利率规则

金额：

```text
BIGINT
单位：分
```

利率：

```text
BIGINT
倍率：1,000,000
```

例如：

```text
5000 = 0.5%
15000 = 1.5%
```

---

# 11. 币种

业务币种编码：

```text
001 = CNY
```

币种字典：

```text
currency_dict
```

业务表统一保存：

```text
currency_code
```

---

# 12. 执行利率模型

`deposit_product_rate` 查询维度：

```text
产品
+
产品期限
+
客户大类
+
客户小类
+
币种
+
业务日期
=
执行利率
```

字段核心包括：

```text
product_id
product_term_id
large_category_code
small_category_code
currency_code
interest_rate
effective_date
expire_date
status
```

必须保留历史利率。

新利率不能覆盖旧记录。

查不到有效利率时：

```text
不允许试算
不允许继续办理
```

禁止默认利率兜底。

---

# 13. AI 产品匹配模型

LLM 负责从自然语言提取：

```text
总金额
预期存期
流动资金需求
流动性偏好
币种
```

Java 负责：

```text
客户分类
↓
产品准入
↓
期限
↓
最低金额
↓
执行利率
↓
收益试算
↓
候选组合方案
```

LLM 最后负责解释多个方案。

不输出强制“最佳产品”。

---

# 14. 办理意向模型

## deposit_intention

一条记录：

> 一个客户的一次整体办理意向。

可以包含多条意向明细。

状态：

```text
0 草稿
1 已确认
2 办理中
3 部分成功
4 全部成功
5 失败
6 取消
```

`PART_SUCCESS` 保留。

---

## deposit_intention_detail

一条记录：

> 整体意向中的一笔具体产品办理意向。

状态：

```text
0 待办理
1 办理中
2 成功
3 失败
4 取消
```

主单状态根据明细汇总。

---

# 15. AI 与实际业务关系

AI 意向只是渠道来源之一。

真实存款数据可能来源：

```text
1 AI助手
2 柜面
3 手机银行
4 企业网银
9 其他
```

因此实际账户/存单表必须独立存在。

AI 意向关联：

```text
intention_detail_id
```

允许为 NULL。

---

# 16. 活期存款模型

活期采用账户模型：

```text
demand_deposit_account
```

特点：

- 无固定到期日
- 不存在“提前支取”
- 可多次存入/支取
- 当前余额持续变化
- 利率调整前后分段计息

核心字段：

```text
account_no
customer
product
currency_code
balance
status
open_date
source_channel
source_business_no
version
```

---

# 17. 定期存款模型

定期采用存单/证实书模型：

```text
time_deposit_certificate
```

核心字段：

```text
certificate_no
customer
deposit_account_no
product
term
currency_code
original_principal
current_principal
value_date
maturity_date
interest_rate
expected_interest
paid_interest
auto_renew_flag
status
source_channel
```

定期支持：

- 正常到期
- 全额提前支取
- 部分提前支取
- 到期未支取
- 自动转存

部分提前支取：

```text
支取部分：
按支取日活期利率计息

剩余部分：
保持原起息日
保持原到期日
保持原约定利率
```

若部分支取后：

```text
剩余本金 < min_retain_amount
```

则整笔按提前支取规则结清。

---

# 18. 通知存款模型

存单：

```text
notice_deposit_certificate
```

通知指令：

```text
notice_withdraw_instruction
```

通知存款支持：

- 正常通知支取
- 部分支取
- 未通知直接支取
- 提前于约定日支取
- 逾期支取
- 实际存期不足通知期限
- 实际支取金额少于通知金额
- 实际支取金额超过通知金额
- 已通知但未支取
- 通知后取消

正常通知支取：

> 支取部分按支取日对应通知存款挂牌利率、按实际存期计息。

不满足通知规则的相关金额通常按：

```text
支取日活期利率
```

通知存款部分支取后：

```text
remaining >= min_retain_amount
```

则原存单继续。

如果：

```text
remaining < min_retain_amount
```

则剩余资金转活期。

---

# 19. 大额存单模型

```text
large_deposit_certificate
```

支持：

- 正常存入
- 全额提前支取
- 部分提前支取
- 正常到期
- 到期后未兑付

部分提前支取：

```text
支取部分：
按提前支取规则，当前设计常见为支取日活期利率

剩余部分：
原存入日
原利率
原到期日
```

如果部分支取后：

```text
剩余本金 < min_retain_amount
```

则：

> 整笔办理全额提前支取。

---

# 20. 存款统一交易流水

统一表：

```text
deposit_transaction
```

用于记录所有真实本金和利息变化。

维度：

```text
deposit_type
deposit_business_id
```

`deposit_type`：

```text
1 活期
2 定期
3 通知
4 大额存单
```

交易类型：

```text
1 OPEN
2 DEPOSIT
3 WITHDRAW
4 PARTIAL_EARLY_WITHDRAW
5 FULL_EARLY_WITHDRAW
6 NORMAL_MATURITY
7 NOTICE_WITHDRAW
8 CONVERT_TO_DEMAND
9 AUTO_RENEW
10 INTEREST_PAYMENT
```

所有本金字段统一存正数。

方向由 `transaction_type` 判断。

---

# 21. 自动转存

自动转存必须：

```text
旧存单到期
↓
旧存单状态结束
↓
生成新存单
↓
新一期使用转存日最新执行利率
```

禁止直接修改旧存单：

```text
value_date
maturity_date
interest_rate
```

形成：

```text
old certificate
↓
renew_source_sn_id
↓
new certificate
```

---

# 22. 计息策略架构

统一：

```text
InterestCalculationService
```

策略：

```text
DemandInterestCalculator
TimeDepositInterestCalculator
NoticeDepositInterestCalculator
LargeDepositInterestCalculator
```

通知存款额外使用：

```text
NoticeWithdrawRuleService
```

判断：

```text
NORMAL
NO_NOTICE
EARLY
OVERDUE
SHORT_HOLDING
LESS_THAN_NOTICE_AMOUNT
GREATER_THAN_NOTICE_AMOUNT
CANCELLED
```

---

# 23. 开户领域服务

统一入口：

```text
DepositOpenService
```

处理器：

```text
DemandDepositOpenHandler
TimeDepositOpenHandler
NoticeDepositOpenHandler
LargeDepositOpenHandler
```

统一过程：

```text
客户
↓
客户准入
↓
产品状态
↓
期限
↓
最低起存金额
↓
执行利率
↓
计息/到期信息
↓
创建账户/存单
↓
OPEN交易流水
↓
更新AI意向（如果来源AI）
```

---

# 24. 支取领域服务

统一入口：

```text
DepositWithdrawService
```

处理器：

```text
DemandWithdrawHandler
TimeDepositWithdrawHandler
NoticeDepositWithdrawHandler
LargeDepositWithdrawHandler
```

涉及本金修改必须事务处理。

核心账户/存单更新需要数据库行锁或等价并发控制。

---

# 25. 当前数据库表

当前核心 DDL 已生成文件：

```text
corporate_deposit_ai_v0.9_mysql_ddl.sql
```

现有表：

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

# 26. AI Tool 设计方向

计划提供：

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

AI 不直接调用实际资金支取服务。

---

# 27. V1.2 前后端目录与运行架构（2026-09-19）

仓库采用前后端双目录：

```text
corp-deposit-rag-web     Vue 3 + TypeScript + Vite + Element Plus + Pinia
corp-deposit-rag-server  Java 17 + Spring Boot Maven 多模块
```

开发态由 Vite `5173` 提供页面并代理 `/api` 到 `ai-assistant-service:8084`。前端通过 API 适配层把后端单产品候选转换为支持 `details[]` 的组合方案模型；金额、利率与收益只展示后端确定性结果。

一键启动流程为：依赖检查 → 后端测试与 JAR 打包 → 前端测试与构建 → 启动四个 Java 服务与 Vite → 代理接口冒烟测试 → 打开浏览器。运行 PID、精确命令标记与日志写入 Git 忽略的 `.runtime`，停止脚本只清理 PID 和命令标记同时匹配的登记进程。

