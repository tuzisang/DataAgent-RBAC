## 1. 数据库表结构

- [ ] 1.1 创建 5 张 RBAC 表的迁移 SQL：`sys_user`、`sys_role`、`sys_permission`、`sys_user_role`、`sys_role_permission`
- [ ] 1.2 给现有 `agent` 表增加 `created_by` 字段，默认值为系统用户
- [ ] 1.3 插入默认数据：4 个角色、全部权限、一个默认 `super-admin` 用户（BCrypt 密码）
- [ ] 1.4 回填现有 `agent` 行的 `created_by`

## 2. 后端依赖与配置

- [ ] 2.1 在 `data-agent-management/pom.xml` 中添加 `sa-token-reactor-spring-boot3-starter` 和 `sa-token-redis-jackson`
- [ ] 2.2 在 `application.yml` 中配置 `sa-token` 属性（Token 超时、Redis 等）
- [ ] 2.3 创建 `SaTokenConfigure`，注册 `SaReactorFilter` Bean：拦截 `/**`，排除认证/公开路径
- [ ] 2.4 创建 `StpInterfaceImpl` 实现 `StpInterface`：从数据库读取角色和权限
- [ ] 2.5 注册 `CorsWebFilter` Bean，白名单从 `application.yml` 加载，移除所有 Controller 的 `@CrossOrigin`

## 3. 后端 RBAC 领域层

- [ ] 3.1 创建实体类：`SysUser`、`SysRole`、`SysPermission`、`SysUserRole`、`SysRolePermission`
- [ ] 3.2 为 5 张 RBAC 表创建 MyBatis Mapper 接口和 XML 文件
- [ ] 3.3 创建 `UserService`，包含方法：`validate(username, password)`、`getById`、`createUser`、`updateStatus`、`resetPassword`
- [ ] 3.4 创建 `RoleService`，包含方法：`getRoleCodesByUserId`、`getPermissionsByRoleCodes`、`updateRolePermissions`
- [ ] 3.5 创建 `PermissionService`，包含方法：`listAllGroupedByResource`

## 4. 后端认证与管理 Controller

- [ ] 4.1 创建 `AuthController`，提供端点：`/api/auth/login`、`/api/auth/logout`、`/api/auth/register`、`/api/auth/info`
- [ ] 4.2 在 `AuthController.register` 和 `UserService.validate` 中实现 BCrypt 密码编码
- [ ] 4.3 创建 `AdminUserController`，路径 `/api/admin/users`，标注 `@SaCheckRole("super-admin")`
- [ ] 4.4 创建 `AdminRoleController`，路径 `/api/admin/roles`，标注 `@SaCheckRole("super-admin")`
- [ ] 4.5 创建 `AdminPermissionController`，路径 `/api/admin/permissions`，标注 `@SaCheckRole("super-admin")`
- [ ] 4.6 扩展 `GlobalExceptionHandler`，捕获 `NotLoginException`（401）和 `NotPermissionException`（403）

## 5. 后端 API 保护 — Controller 注解

- [ ] 5.1 给 `AgentController` 添加 `@SaCheckPermission`（list→view、create→create、update→update、delete→delete、publish→publish、apikey→apikey）
- [ ] 5.2 给 `DatasourceController` 添加 `@SaCheckPermission`（全部端点）
- [ ] 5.3 给 `ModelConfigController` 添加 `@SaCheckPermission`（全部端点）
- [ ] 5.4 给 `PromptConfigController` 添加 `@SaCheckPermission`（全部端点）
- [ ] 5.5 给 `AgentKnowledgeController` 添加 `@SaCheckPermission`（全部端点）
- [ ] 5.6 给 `BusinessKnowledgeController` 添加 `@SaCheckPermission`（全部端点）
- [ ] 5.7 给 `ChatController` 添加 `@SaCheckPermission`（全部端点）
- [ ] 5.8 给 `GraphController` 添加 `@SaCheckPermission`（stream/search→chat:use）
- [ ] 5.9 给剩余 Controller 添加 `@SaCheckPermission`：`SemanticModelController`、`AgentPresetQuestionController`、`AgentDatasourceController`、`SessionEventController`、`FileUploadController`、`EchoController`

## 6. 后端数据归属控制

- [ ] 6.1 给 `Agent` 实体增加 `createdBy` 字段，确保在 `AgentController.create` 中设置当前用户
- [ ] 6.2 修改 `AgentService.update`，如果当前用户是 `analyst` 且非创建者，则拒绝更新
- [ ] 6.3 修改 `AgentService.deleteById`，如果当前用户是 `analyst` 且非创建者，则拒绝删除
- [ ] 6.4 修改 `AgentService.findAll`/`search`/`findByStatus`，当前用户为 `analyst` 时按创建者过滤
- [ ] 6.5 修改 `AgentController.list`，为非 admin 角色注入创建者过滤条件

## 7. API Key 共存与 SSE

- [ ] 7.1 创建 `ApiKeyAuthFilter` 作为 `WebFilter`，在过滤器链中位于 `SaReactorFilter` 之后
- [ ] 7.2 配置 `SaReactorFilter` 排除项，包含 `/api/agent/*/api-key/**` 和 `/api/stream/search`
- [ ] 7.3 修改 `GraphController.streamSearch`，提取 `?token=` 参数并使用 `StpUtil` 校验
- [ ] 7.4 修改 `GraphController.streamSearch`，接受 `X-API-Key` 请求头并校验 Agent 范围
- [ ] 7.5 确保 SSE 认证失败时使用统一的 `ApiResponse` 格式返回错误事件

## 8. 前端认证页面

- [ ] 8.1 创建 `Login.vue`，用户名/密码表单，调用 `/api/auth/login`，将 Token/权限存入 localStorage
- [ ] 8.2 创建 `Register.vue`，用户名/密码表单，调用 `/api/auth/register`，自动重定向到登录页
- [ ] 8.3 在 Vue Router 中添加 `/login` 和 `/register` 路由（无认证守卫）
- [ ] 8.4 创建 `AdminUserManage.vue` 供 super-admin 使用：用户 CRUD 表格、角色分配、状态开关
- [ ] 8.5 创建 `AdminRoleManage.vue` 供 super-admin 使用：角色列表、权限树编辑器
- [ ] 8.6 创建 `AdminPermissionView.vue` 供 super-admin 使用：只读权限目录
- [ ] 8.7 添加 `/admin/users`、`/admin/roles`、`/admin/permissions` 路由，带角色守卫（仅 super-admin）

## 9. 前端集成

- [ ] 9.1 添加 Axios 请求拦截器，从 localStorage 注入 `Authorization: Bearer <token>`
- [ ] 9.2 添加 Axios 响应拦截器，处理 401/403：清除 Token 并重定向到 `/login`
- [ ] 9.3 添加 `beforeEach` 路由守卫：未认证用户重定向到 `/login`，已认证用户离开 `/login`
- [ ] 9.4 添加 `afterEach` 守卫或导航逻辑：非 super-admin 从 `/admin/*` 重定向到 `/`
- [ ] 9.5 注册全局 `v-permission` 指令，控制按钮/菜单显示
- [ ] 9.6 更新布局侧边栏/导航栏，根据 `super-admin` 角色显示/隐藏管理菜单项
- [ ] 9.7 登录后将用户信息、角色、权限存入 Pinia/Vuex（或响应式全局状态）

## 10. 安全加固与测试

- [ ] 10.1 移除 15+ 个 Controller 上的所有 `@CrossOrigin(origins = "*")` 注解
- [ ] 10.2 添加 `spring.security.bcrypt.strength` 配置或硬编码 BCrypt strength=10
- [ ] 10.3 确保 `GlobalExceptionHandler` 对所有 Sa-Token 异常返回 `ApiResponse` 格式
- [ ] 10.4 端到端测试登录流程（前端 → 后端 → Token → 受保护 API）
- [ ] 10.5 测试 API Key 外部调用在无 Sa-Token 时仍能正常工作
- [ ] 10.6 测试 SSE 流的 `?token=` 和 `X-API-Key` 两种认证路径
- [ ] 10.7 测试数据归属：analyst A 不能查看/编辑 analyst B 的 Agent
- [ ] 10.8 测试管理页面：viewer 不能访问 `/admin/users`
