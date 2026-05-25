## Purpose

非管理员用户（analyst、viewer）访问 Agent 列表时，通过多条件可见性规则确保他们能看到已发布的 Agent、自己创建的 Agent、以及管理员分配的 Agent。

## Requirements

### Requirement: Non-admin user sees published agents
非管理员用户访问 Agent 列表时，系统 SHALL 返回所有 `status = 'published'` 的 Agent。

#### Scenario: Viewer sees published agents
- **WHEN** viewer 角色用户请求 `GET /api/agent/list`
- **THEN** 返回结果包含所有 `status = 'published'` 的 Agent

#### Scenario: Published agent with no creator still visible
- **WHEN** 存在 `status = 'published'` 且 `created_by` 为 NULL 的 Agent
- **THEN** viewer 角色用户的列表结果中仍包含该 Agent

### Requirement: Non-admin user sees own agents
非管理员用户访问 Agent 列表时，系统 SHALL 返回该用户自己创建的 Agent（无论状态）。

#### Scenario: Analyst sees own draft agent
- **WHEN** analyst 用户创建了一个 `status = 'draft'` 的 Agent
- **THEN** 该用户在 Agent 列表中能看到这个 draft Agent

### Requirement: Non-admin user sees assigned agents
非管理员用户访问 Agent 列表时，系统 SHALL 返回管理员通过 `sys_user_agent_visibility` 表分配给的 Agent。

#### Scenario: Viewer sees assigned non-published agent
- **WHEN** 管理员将某个 `status = 'draft'` 的 Agent 分配给 viewer 用户
- **THEN** 该 viewer 用户在 Agent 列表中能看到这个 draft Agent

### Requirement: Deduplication of agent list
非管理员用户的 Agent 列表 SHALL 自动去重，当同一个 Agent 同时满足多个可见条件时只出现一次。

#### Scenario: Own published agent appears once
- **WHEN** 用户自己创建了一个 `status = 'published'` 的 Agent
- **THEN** 该 Agent 在列表结果中只出现一次
