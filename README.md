# 对公存款智能服务助手

V1.2 MVP 已包含 Vue 3 前端和 Java 微服务后端，默认通过 Mock 提取自然语言需求，不调用付费模型 API。

## 目录结构

```text
corp-deposit-rag/
├── corp-deposit-rag-web/       Vue 3 + TypeScript 前端
├── corp-deposit-rag-server/    Maven 多模块 Java 后端
├── start-all.bat               构建并启动全部服务、打开网页
└── stop-all.bat                停止本项目启动的全部进程
```

## 一键启动

确保 MySQL、Nacos 已启动后，直接双击根目录 `start-all.bat`。脚本会检查环境、运行后端测试并打包最新 JAR、测试和构建前端、启动全部服务、验证推荐接口，最后打开 `http://127.0.0.1:5173`。

未设置 `MYSQL_PASSWORD` 时会在控制台安全询问，密码不会落盘。关闭服务请双击 `stop-all.bat`。演示账号：`zhangsan / 123456`。

## 模块与端口

| 模块 | 端口 | 职责 |
| --- | ---: | --- |
| `customer-service` | 8081 | 客户及大小类查询 |
| `deposit-product-service` | 8082 | 准入、期限、利率、匹配、试算 |
| `deposit-business-service` | 8083 | 幂等创建办理意向 |
| `ai-assistant-service` | 8084 | Mock 参数提取与 Tool 编排 |

## 环境

```powershell
$env:MYSQL_USERNAME = 'root'
$env:MYSQL_PASSWORD = '<本地 MySQL 密码>'
$env:NACOS_SERVER_ADDR = '127.0.0.1:8848'
```

密码不写入 Git。首次启动时 Flyway 会创建表和演示数据；JDBC URL 已允许创建 `corporate_deposit_ai` 数据库。

## 构建和启动

```powershell
cd corp-deposit-rag-server
.\mvnw.cmd clean package
java -jar customer-service\target\customer-service-1.1.0-SNAPSHOT.jar
java -jar deposit-product-service\target\deposit-product-service-1.1.0-SNAPSHOT.jar
java -jar deposit-business-service\target\deposit-business-service-1.1.0-SNAPSHOT.jar
java -jar ai-assistant-service\target\ai-assistant-service-1.1.0-SNAPSHOT.jar
```

## Mock 推荐

```powershell
curl.exe http://127.0.0.1:8084/api/v1/assistant/plans `
  -H "Content-Type: application/json" `
  -d '{"customerNo":"CUST001","message":"有800万资金想存一年，优先收益，可以长期不用"}'
```

## 创建办理意向

从推荐响应选择方案后调用：

```powershell
curl.exe http://127.0.0.1:8084/api/v1/assistant/intentions `
  -H "Content-Type: application/json" `
  -d '{"idempotencyKey":"demo-001","customerNo":"CUST001","requirementText":"800万存一年","details":[{"productId":2,"productTermId":23,"amountInCents":800000000,"interestRate":15000,"expectedInterestInCents":12000000,"currencyCode":"001"}]}'
```

重复提交相同 `idempotencyKey` 会返回原意向，不会重复落库。

