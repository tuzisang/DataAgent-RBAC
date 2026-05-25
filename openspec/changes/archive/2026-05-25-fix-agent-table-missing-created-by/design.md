## Context

`Agent` entity 已定义 `createdBy` 字段（Long 类型），`AgentMapper.findByConditionsWithCreator` 在 WHERE 子句中按 `created_by` 过滤。但 `schema.sql` 中 `agent` 表从未定义该列，INSERT 语句也未写入该字段。RBAC 权限改造后，非管理员用户登录即调用此查询，直接导致 SQL 异常。

## Goals / Non-Goals

**Goals:**
- `agent` 表 DDL 补充 `created_by BIGINT` 列 + 索引
- `AgentMapper.insert` 补充 `created_by` 字段写入
- 提供存量数据库迁移 SQL

**Non-Goals:**
- 不修改查询逻辑或 RBAC 权限控制
- 不处理已有数据的 `created_by` 回填（历史 agent 的 creator 可设为 NULL）

## Decisions

1. **列类型选择 BIGINT**：与 Java entity `Long createdBy` 一致，也与 `admin_id` 等其他 ID 列类型统一。
2. **新增索引 `idx_created_by`**：`findByConditionsWithCreator` 以 `created_by` 为查询条件，增加索引避免全表扫描。
3. **迁移脚本独立于 schema.sql**：迁移脚本放在 change 目录下，供存量数据库手动执行，不污染主 DDL。
4. **不使用外键**：与现有 `admin_id` 模式一致，`created_by` 仅存储用户ID不做外键约束，避免用户表迁移复杂性。

## Risks / Trade-offs

- [存量数据库未执行迁移] → 在 tasks 中明确列出迁移步骤，部署时需人工执行 `migration.sql`
- [历史数据 created_by 为 NULL] → 管理员查询使用 `findAll`/`findByStatus`，不经过 `findByConditionsWithCreator`，无影响。后续可选择性回填。
