## Purpose

管理员可通过用户管理页面为用户分配 Agent 可见性，控制哪些用户能查看特定的非公开 Agent。

## Requirements

### Requirement: Admin can assign agent visibility to a user
管理员 SHALL 能通过 API 为用户设置可查看的 Agent 列表（全量替换模式）。

#### Scenario: Assign agents to user
- **WHEN** 管理员调用 `PUT /api/admin/users/{id}/agent-visibility` 传入 `[1, 2, 3]`
- **THEN** `sys_user_agent_visibility` 表中该用户的记录被替换为这三条，旧记录被删除

#### Scenario: Clear all agent visibility for a user
- **WHEN** 管理员调用 `PUT /api/admin/users/{id}/agent-visibility` 传入 `[]`
- **THEN** `sys_user_agent_visibility` 表中该用户的所有记录被删除

### Requirement: Admin can view agent visibility for a user
管理员 SHALL 能查询某个用户当前被分配了哪些 Agent。

#### Scenario: Get user's assigned agents
- **WHEN** 管理员调用 `GET /api/admin/users/{id}/agent-visibility`
- **THEN** 返回该用户当前分配的 `agentIds` 列表

### Requirement: Admin can assign agent visibility via user management page
用户管理页面的表格操作列 SHALL 提供"分配Agent"按钮，点击后弹出多选弹窗。

#### Scenario: Open agent assignment dialog
- **WHEN** 管理员点击某用户行的"分配Agent"按钮
- **THEN** 弹窗显示所有 Agent 列表（含名称和状态），已分配的 Agent 默认选中

#### Scenario: Save agent assignment
- **WHEN** 管理员在弹窗中选择/取消 Agent 并点击确定
- **THEN** 系统保存分配结果，用户列表刷新

### Requirement: Agent visibility assignment validates input
分配 Agent 可见性时，系统 SHALL 校验传入的 agent ID 均存在。

#### Scenario: Invalid agent ID rejected
- **WHEN** 管理员传入包含不存在 agent ID 的列表
- **THEN** 系统返回 400 错误 "部分Agent不存在"
