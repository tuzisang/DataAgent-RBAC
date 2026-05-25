## 1. Database — New Table

- [ ] 1.1 创建 `sys_user_agent_visibility` 建表 DDL 并添加到 `sql/schema.sql` 和 `sql/h2/schema-h2.sql`

## 2. Backend — Entity & Mapper

- [ ] 2.1 创建 `SysUserAgentVisibility` entity
- [ ] 2.2 创建 `SysUserAgentVisibilityMapper`（insert, deleteByUserId, findAgentIdsByUserId）

## 3. Backend — Agent List Query

- [ ] 3.1 修改 `AgentMapper.findByConditionsWithCreator` 查询逻辑，加入 `status = 'published'` 和 `sys_user_agent_visibility` 子查询

## 4. Backend — Visibility API

- [ ] 4.1 在 `AdminUserController` 中新增 `GET /api/admin/users/{id}/agent-visibility` 和 `PUT /api/admin/users/{id}/agent-visibility` 端点

## 5. Frontend — API Service

- [ ] 5.1 在 `admin.ts` 中新增 `SysAgent` 简要接口和 `getUserAgentVisibility`、`updateUserAgentVisibility` 方法

## 6. Frontend — UI

- [ ] 6.1 在 `AdminUserManage.vue` 操作列新增"分配Agent"按钮和弹窗（多选 Agent 列表）

## 7. Verification

- [ ] 7.1 编译后端验证无错误
