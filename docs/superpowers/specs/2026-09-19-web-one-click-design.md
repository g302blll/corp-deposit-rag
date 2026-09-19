# 前后端分离与一键启动设计

## 目标

将仓库整理为 `corp-deposit-rag-web` 和 `corp-deposit-rag-server` 两个工程，并提供 Windows 一键启动能力。双击 `start-all.bat` 后完成前后端依赖安装、最新 JAR 打包、服务启动、健康等待和浏览器打开，用户可以立即执行产品推荐和办理意向测试。

## 目录

仓库根目录保留项目治理文档、总 README、`start-all.bat` 和 `stop-all.bat`。Maven Wrapper、父 POM、共享模块、四个 Spring Boot 服务和 SQL 放入 `corp-deposit-rag-server`。Vue 3 + Vite 工程放入 `corp-deposit-rag-web`。

## 网页

测试页使用 Vue 3，包含客户编号、自然语言需求、推荐按钮、客户信息、候选方案卡片、方案选择、幂等键和创建意向按钮。前端只调用 `/api/v1/assistant/**`；Vite 将 `/api` 代理到 `http://127.0.0.1:8084`，不修改后端 CORS。

## 一键脚本

`start-all.bat` 调用后端工程内的 PowerShell 启动器。启动器检查 Java、Node、npm、Nacos 8848、MySQL 3306 和 `MYSQL_PASSWORD`，运行 Maven 全量测试打包与 npm 测试构建，随后隐藏启动四个 JAR 和 Vite。PID 与日志写入根目录 `.runtime`（Git 忽略）。所有端口就绪后打开浏览器；任一步失败时输出原因和日志位置。

`stop-all.bat` 只停止 `.runtime` 中登记且命令行属于当前仓库的进程，避免误杀其他 Java/Node 进程。

## 测试

后端继续执行 Maven 全量测试。前端用 Vitest 覆盖推荐请求、意向请求和错误转换。最终执行生产构建，并用启动脚本在现有 MySQL/Nacos 环境完成页面、API、Nacos 注册和停止流程冒烟测试。
