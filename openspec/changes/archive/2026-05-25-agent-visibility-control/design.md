## Context

当前 Agent 列表对非管理员用户仅按 `created_by` 过滤，导致 analyst/viewer 永远看不到任何 Agent（他们无法创建 Agent）。用户期望非管理员能看到已发布的 Agent，且管理员能灵活分配可见性。

## Goals / Non-Goals

**Goals:**
- 非管理员用户的 Agent 列表 = 已发布的 + 自己创建的 + 管理员额外分配的
- 管理员可在用户管理页面为用户分配可查看的 Agent

**Non-Goals:**
- 不支持"拒绝"规则（只能正向分配，不能显式隐藏某个 Agent）
- 不改变编辑/删除等操作的权限控制（仍由 `checkOwnership` + `@SaCheckPermission` 控制）
- 不改变 Agent 详情页的访问逻辑

## Decisions

### 1. 列表查询逻辑：三合一 UNION

非管理员用户的 Agent 列表查询条件改为三条路径的并集：
1. `status = 'published'` — 所有已发布 Agent
2. `created_by = userId` — 用户自己创建的
3. `agent_id IN (SELECT agent_id FROM sys_user_agent_visibility WHERE user_id = userId)` — 管理员额外分配的

**Alternatives considered:**
- **单条 OR 查询**: `WHERE status='published' OR created_by=? OR id IN (subquery)` — 采用此方案，简洁且 DB 优化器可处理。
- **应用层合并**: 三次查询在 Java 中去重合并 — 代码复杂度高，被否决。

### 2. 新增 sys_user_agent_visibility 关联表

新建三字段表 `sys_user_agent_visibility(id, user_id, agent_id, create_time)`，`(user_id, agent_id)` 唯一索引。管理员通过 API 全量替换某用户的可见 Agent 列表。

**Alternatives considered:**
- **复用 sys_user_role 表**: 为每个 Agent 创建角色 — 过度设计，角色和 Agent 不是同一层级概念。
- **Agent 表加 visible_to 字段**: 无法支持多用户分配，差。

### 3. API 设计：全量替换模式

`PUT /api/admin/users/{id}/agent-visibility` 接收 `{ "agentIds": [1, 2, 3] }`，先删后插。与现有 `assignRoles` 模式一致。

### 4. 前端：在用户管理页面加"分配Agent"按钮

在 AdminUserManage.vue 表格操作列新增"分配Agent"按钮，弹窗使用 `el-select` 多选 Agent 列表。

## Risks / Trade-offs

- **OR + 子查询性能**: 用户量小时无影响。用户量大时可加索引 `idx_agent_status`、`idx_visibility_user` 优化。
- **全量替换 API**: 并发场景下可能存在竞态 — 管理页面低频操作，风险可接受。
