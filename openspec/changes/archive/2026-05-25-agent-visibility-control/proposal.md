## Why

非管理员用户（analyst/viewer）登录后智能体列表为空，因为当前逻辑只显示 `created_by` 为自己的 Agent，但这些角色无法创建 Agent，形成死循环。同时，管理员需要能灵活控制每个用户可以看到哪些 Agent，而不能仅依赖"已发布"这一种规则。

## What Changes

- **Agent 列表查询逻辑**：非管理员用户查看 `/api/agent/list` 时，返回已发布（published）的 Agent + 自己创建的 Agent，而非仅自己创建的
- **用户-Agent 可见性分配**：新增 `sys_user_agent_visibility` 关联表，管理员可为特定用户分配额外可见的 Agent
- **管理后台 UI**：用户管理页面新增"分配Agent"按钮，弹窗选择该用户可额外看到的 Agent
- 已存在 `agent-created-by-column` spec 中的非管理员列表需求需更新

## Capabilities

### New Capabilities
- `agent-list-visibility`: 非管理员用户查看 Agent 列表时，显示所有已发布 Agent + 自己创建的 Agent + 管理员额外分配的 Agent
- `agent-visibility-assignment`: 管理员可在用户管理页面为用户分配可查看的 Agent

### Modified Capabilities
- `agent-created-by-column`: 非管理员用户列表查询条件从"仅自己创建的"改为"已发布 + 自己创建的 + 额外分配的"

## Impact

- `AgentMapper.java` — 修改 `findByConditionsWithCreator` 查询，或新增方法
- `AgentController.java` — 修改 `list()` 中非管理员分支的查询逻辑
- 新增 `SysUserAgentVisibility` entity + mapper + 建表 DDL
- 新增 `GET/PUT /api/admin/users/{id}/agent-visibility` API
- `AdminUserManage.vue` — 新增"分配Agent"按钮和弹窗
- `admin.ts` — 新增 API 方法和类型定义
