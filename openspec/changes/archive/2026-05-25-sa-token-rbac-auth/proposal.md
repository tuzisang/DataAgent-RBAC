## Why

DataAgent 目前完全开放，无任何认证授权机制。所有 15 个 REST Controller 均使用 `@CrossOrigin(origins = "*")`，任何能访问网络的人都可以调用全部 API。这在生产环境是不可接受的。需要在不破坏现有 API Key 外部调用机制的前提下，引入基于 Sa-Token 的 RBAC 权限认证体系，实现用户登录、角色控制、权限拦截。

## What Changes

- **新增 Sa-Token Reactor 依赖**（`sa-token-reactor-spring-boot3-starter`），适配 Spring WebFlux 反应式环境。
- **新增用户/角色/权限数据库表**：`sys_user`、`sys_role`、`sys_permission`、`sys_user_role`、`sys_role_permission`。
- **新增认证模块**：登录/登出/注册/获取当前用户信息的 REST API。
- **新增 Sa-Token 全局配置**：`SaReactorFilter` 拦截路由，`StpInterfaceImpl` 提供角色和权限数据源。
- **所有 Controller 增加方法级权限注解**：`@SaCheckPermission`、`@SaCheckRole`。
- **数据权限控制**：Agent 表增加 `created_by` 字段，`analyst` 只能操作自己的 Agent。
- **API Key 与 Sa-Token 双通道共存**：Web UI 走 Sa-Token，外部调用走 API Key；SSE 流式接口特殊处理。
- **前端新增页面**：登录页、注册页、用户管理页、角色管理页、权限管理页（仅 super-admin）。
- **前端改造**：Axios 拦截器注入 Token、路由守卫、权限指令 `v-permission`。
- **CORS 收紧**：从 `origins = "*"` 改为配置化白名单，与 Sa-Token 统一在 Filter 层处理。

## Capabilities

### New Capabilities
- `user-auth`: 用户认证体系（登录、登出、注册、Token 管理、密码加密）
- `rbac-core`: RBAC 核心模型（角色、权限、用户-角色、角色-权限关联管理）
- `rbac-admin`: 超管后台（用户管理、角色分配、权限分配、账号状态控制）
- `rbac-api-protection`: API 接口权限保护（全局拦截器 + Controller 方法级注解）
- `api-key-coexist`: API Key 与 Sa-Token 双认证通道共存机制

### Modified Capabilities
- （无现有 spec 需要修改，当前系统无认证能力，不涉及既有行为变更）

## Impact

- **后端依赖**：新增 `sa-token-reactor-spring-boot3-starter`、`sa-token-redis-jackson`（可选 Redis 持久化）。
- **数据库**：新增 5 张 RBAC 表，现有 `agent` 表增加 `created_by` 字段。
- **所有 Controller**：需添加 `@SaCheckPermission` 注解，移除或收紧 `@CrossOrigin`。
- **前端路由**：新增 `/login`、`/register`、`/admin/users`、`/admin/roles`、`/admin/permissions`。
- **现有 API Key 机制**：保留不变，增加独立的 `ApiKeyAuthFilter` 处理外部调用。
- **SSE 流式接口**（`/api/stream/search`）：需调整 Token 传递方式（URL 参数或 Header）。
