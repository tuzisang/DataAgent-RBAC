## 背景

DataAgent 是一个基于 Spring Boot 3.4.8 + WebFlux + Vue 3 的应用。目前没有任何认证机制，全部 15 个 REST Controller 都是完全开放的，都用了 `@CrossOrigin(origins = "*")`。项目已有一套 API Key 机制（`ApiKeyUtil`、`Agent.apiKey`）供外部系统调用，这套机制必须保留。

后端是 **Spring WebFlux（反应式）**，不是 Spring MVC。这是一个关键约束——任何安全框架都必须支持 Reactor/Mono/Flux 模式。

## 目标 / 非目标

**目标：**
- 将 Sa-Token RBAC 认证集成到 WebFlux 后端
- 为全部 15+ 个 Controller 端点增加角色/权限校验
- 新增前端登录/注册/管理后台页面
- 维持双认证通道：Sa-Token 用于 Web UI，API Key 用于外部调用
- 数据归属控制（`analyst` 只能操作自己创建的 Agent）

**非目标：**
- OAuth2 / SSO 集成（超出范围）
- 基于 JWT 的认证（Sa-Token 使用自己的 Token 机制）
- 多租户隔离
- 按用户限流（后续可扩展）
- 修改现有 API Key 的生成/掩码逻辑

## 设计决策

### 决策 1：使用 Sa-Token Reactor starter，不用 Spring Security
**理由：**
- Sa-Token 原生提供 `SaReactorFilter` 适配 WebFlux；Spring Security 的 WebFlux 配置（`SecurityWebFilterChain`）出了名的冗长
- Sa-Token 的 `@SaCheckPermission` 注解模型与我们的扁平 `resource:action` 权限设计完美契合
- Spring Security 需要自定义 `ReactiveAuthenticationManager`、`ServerAuthenticationConverter` 等一堆组件
- 团队偏好轻量、注解驱动的安全方案

**备选方案：** Spring Security（因复杂度被拒）、Apache Shiro（不支持 WebFlux）、自定义 JWT Filter（重复造轮子）

### 决策 2：使用 `sa-token-redis-jackson` 持久化 Token
**理由：**
- Sa-Token 默认将 Token 存在内存（`ConcurrentHashMap`）中
- 如果 DataAgent 多实例部署，内存 Token 重启即丢失，且无法在 Pod 间共享
- Redis 持久化确保 Token 在重启后依然有效，并支持集群部署
- 项目已有 Redis 兼容基础设施（可与向量存储共用 Redis）

**备选方案：** 仅内存存储（单节点演示可用，因生产就绪性被拒）

### 决策 3：`ApiKeyAuthFilter` 独立实现，不合并到 SaReactorFilter
**理由：**
- 职责分离：Sa-Token 负责用户会话，API Key 负责机器对机器认证
- `ApiKeyAuthFilter` 可独立测试，未来可抽成公共库
- API Key 校验语义不同（按 Agent 范围，不是按用户范围）

**备选方案：** 在 `SaReactorFilter.setAuth()` 中一并处理（因耦合性和可测试性被拒）

### 决策 4：权限编码采用扁平 `resource:action` 字符串
**理由：**
- 扁平字符串最适合 Sa-Token 的 `StpUtil.checkPermission()` API
- 层级权限（如 `agent.*`）增加了复杂度，但对 15 个 Controller 来说没有明显收益
- 数据库存储简单，前端权限树渲染也方便

**备选方案：** 带通配符的层级权限树（因 YAGNI 被拒）

### 决策 5：Agent 数据归属通过 `created_by` 字段实现
**理由：**
- 最简单的数据归属模型：在 Agent 行上记录创建者 ID
- Service 层在修改前校验 `agent.getCreatedBy().equals(currentUserId)`
- 当前规模不需要单独的 ACL 表

**备选方案：** 完整的 ACL 表逐行权限控制（因过度设计被拒）

### 决策 6：Web UI 的 SSE Token 通过 URL 参数传递
**理由：**
- SSE over HTTP/1.1 无法通过浏览器 `EventSource` API 发送自定义请求头
- SSE 唯一可选的传输方式是：Cookie（`*` 跨域有问题）、URL 参数、或查询字符串
- URL 参数 `?token=xxx` 是业界标准变通方案
- API Key 调用方仍可使用 `X-API-Key` 请求头（它们不是浏览器）

**备选方案：** SSE 改为 WebSocket（改动太大，会破坏协议契约）

## 风险 / 权衡

| 风险 | 缓解措施 |
|------|---------|
| 新增 5 张数据库表和迁移脚本可能导致现有部署失败 | 提供 Flyway/Liquibase 迁移脚本；为已有环境提供手动 SQL 文档 |
| 移除 `@CrossOrigin(origins = "*")` 影响本地开发 | 在 `application.yml` 的 `cors.allowed-origins` 中保留 localhost 默认值 |
| SSE 端点认证变更导致现有客户端崩溃 | 保持向后兼容：如未提供认证则记录警告但仍允许通过（可配置严格模式） |
| Analyst 数据归属导致现有 Agent 记录异常（无 `created_by`） | 迁移脚本将现有行的 `created_by` 设为系统用户 ID |
| Sa-Token Redis 依赖增加基础设施要求 | 文档说明 Redis 可选；单节点部署可降级为内存模式 |
| 角色/权限管理 UI 增加前端复杂度 | 管理页面仅 super-admin 使用；对普通用户无影响 |
| 登录页面改变首次使用体验 | 保留模型配置检查守卫；在其之前增加认证检查 |

## 迁移计划

1. **阶段 0 — 数据库**：执行迁移 SQL 创建 5 张 RBAC 表，给 `agent` 表增加 `created_by`
2. **阶段 1 — 后端核心**：添加依赖、创建 `SaTokenConfigure`、`StpInterfaceImpl`、`AuthController`
3. **阶段 2 — 后端保护**：给所有 Controller 添加 `@SaCheckPermission`，实现数据归属校验
4. **阶段 3 — API Key 共存**：实现 `ApiKeyAuthFilter`，调整 SSE 端点
5. **阶段 4 — 前端认证页面**：创建 Login.vue、Register.vue、管理后台页面
6. **阶段 5 — 前端集成**：Axios 拦截器、路由守卫、权限指令
7. **阶段 6 — CORS 与收尾**：移除 `@CrossOrigin`、配置 `CorsWebFilter`、加固安全

**回滚**：移除 Sa-Token 依赖和配置 Bean；从 git 历史恢复 `@CrossOrigin` 注解。

## 待决问题

1. 密码哈希用 BCrypt 还是 Argon2？（Sa-Token 不强制要求——需要选择 Spring Security Crypto 实现或手动 BCrypt）
2. `super-admin` 账号是否应在首次启动时自动创建？如果是，默认密码是什么？（建议：自动创建并在日志中打印随机密码）
3. 是否需要"记住我"功能？（可延后——Sa-Token 支持 Token 超时延长）
4. API Key 调用是否也应限流？（可延后）
