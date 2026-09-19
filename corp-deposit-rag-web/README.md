# corp-deposit-rag-web

对公存款智能服务助手 V1.2 前端，面向银行客户经理，采用 Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router 与 Axios。

## 本地运行

要求 Node.js 20+。在本目录执行：

```powershell
npm install
npm run dev
```

生产构建与测试：

```powershell
npm test
npm run build
```

也可在仓库根目录双击 `start-all.bat`，自动构建并启动前后端。

## 演示

- 地址：`http://127.0.0.1:5173`
- 账号：`zhangsan`
- 密码：`123456`
- 演示链路：登录 → 选择 `CUST001` → 输入“800万存一年，优先收益” → 查看方案依据 → 选择方案 → 二次确认 → 创建办理意向。

## 环境变量

复制 `.env.example` 为 `.env.development` 可配置四个后端服务地址。未配置时，开发服务器把 `/api` 代理到 `http://127.0.0.1:8084`。

## 已实现与 Mock 边界

- 推荐与创建意向调用真实 `ai-assistant-service` 接口。
- 客户列表、登录、产品列表当前使用前端 Mock。
- 意向列表/详情和产品详情等待后端 GET 接口补齐，页面骨架与 API 边界已保留。
- LLM 默认使用后端 Mock 参数提取，不调用付费 API。
