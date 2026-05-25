## Why

普通用户登录后调用 `GET /api/agent/list`，非管理员走 `findByConditionsWithCreator` 按 `created_by` 过滤，但 `agent` 表缺少该列，导致 `SQLSyntaxErrorException`。`created_by` 字段在 Java entity 和 mapper 查询中已存在，但 DDL 和 INSERT 未同步更新。

## What Changes

- `schema.sql` 中 `agent` 表增加 `created_by` 列及索引
- `AgentMapper.insert` 的 INSERT 语句增加 `created_by` 字段
- 提供存量数据库的 ALTER TABLE 迁移脚本

## Capabilities

### New Capabilities

- `agent-created-by-column`: `agent` 表支持记录创建者ID，使非管理员用户登录后可按创建者过滤智能体列表

### Modified Capabilities

<!-- No existing specs to modify -->

## Impact

- **Schema**: `agent` 表新增 `created_by BIGINT` 列 + 索引
- **Mapper**: `AgentMapper.insert` 增加 `created_by` 参数
- **Migration**: 存量数据库需执行 ALTER TABLE 添加列
- **No breaking changes**: 新列为 NULL 兼容，对现有管理员查询无影响
