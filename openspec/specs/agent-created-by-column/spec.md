## Purpose

`agent` 表通过 `created_by` 列记录智能体的创建者ID，支持 RBAC 权限控制下按多条件可见性查询。

## Requirements

### Requirement: Agent table has created_by column
The `agent` table SHALL include a `created_by` column of type BIGINT to store the ID of the user who created the agent.

#### Scenario: Table schema includes created_by
- **WHEN** the schema DDL for `agent` table is executed
- **THEN** the table SHALL have a `created_by BIGINT` column with a corresponding index `idx_created_by`

### Requirement: Agent insert writes created_by
When inserting a new agent record, the system SHALL persist the `created_by` value from the entity to the database column.

#### Scenario: Insert agent with creator
- **WHEN** a new agent is created with a non-null `createdBy` value
- **THEN** the `created_by` column in the inserted row SHALL equal the entity's `createdBy` value

#### Scenario: Insert agent without creator
- **WHEN** a new agent is created with a null `createdBy` value
- **THEN** the `created_by` column in the inserted row SHALL be NULL

### Requirement: Non-admin user can list agents with visibility control
非管理员用户调用 `GET /api/agent/list` 时，系统 SHALL 返回该用户有权限查看的 Agent 列表：所有 `status = 'published'` 的 Agent、该用户自己创建的 Agent、以及通过 `sys_user_agent_visibility` 表分配给该用户的 Agent，结果自动去重，不出现数据库错误。

#### Scenario: Non-admin user lists agents
- **WHEN** 非管理员用户（ID=5）请求 `GET /api/agent/list`
- **THEN** 系统 SHALL 查询 `agent` 表，条件为 `status = 'published' OR created_by = 5 OR id IN (SELECT agent_id FROM sys_user_agent_visibility WHERE user_id = 5)`，结果去重，无 SQL 语法错误

#### Scenario: Non-admin user with no created or assigned agents
- **WHEN** 非管理员用户没有创建过任何 Agent，也没有被分配任何 Agent
- **THEN** 系统 SHALL 返回所有 `status = 'published'` 的 Agent
