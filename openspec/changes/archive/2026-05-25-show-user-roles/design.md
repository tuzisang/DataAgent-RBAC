## Context

The admin users page (`/admin/users`) currently lists users with columns: ID, username, status, createTime, and action buttons. The "Assign Role" dialog allows super-admins to assign roles to users, but there is no way to see what roles a user currently has without opening the dialog.

The backend `GET /api/admin/users` endpoint returns plain `SysUser` entities with no role information. However, the infrastructure to query roles per user already exists: `SysRoleMapper.findByUserId(Long userId)` returns full `SysRole` objects via a JOIN on `sys_user_role`.

## Goals / Non-Goals

**Goals:**
- Show each user's assigned roles directly in the user list table
- Minimize backend changes by reusing existing mapper methods

**Non-Goals:**
- Change role assignment behavior
- Add role editing inline (still done via dialog)
- Add role-based filtering or sorting of the user list
- Change the `SysUser` database schema

## Decisions

### 1. Add transient `roles` field to `SysUser` entity

Use `@TableField(exist = false)` to add a non-persistent `List<SysRole> roles` field to `SysUser`. This avoids creating a separate DTO class while keeping the database schema unchanged.

**Alternatives considered:**
- **New UserVO class**: More separation of concerns, but overkill for a single additional field and inconsistent with the project's existing pattern of exposing entities directly.
- **Separate API endpoint** (`GET /api/admin/users/{id}/roles`): Would require N+1 API calls from the frontend to display roles for all users in the table. Rejected for poor UX (loading spinners per row).

### 2. Enrich users with roles in `UserService.findAll()`

Modify `UserService.findAll()` to inject `SysRoleMapper` and call `sysRoleMapper.findByUserId()` for each user. The `SysRoleMapper.findByUserId()` method already exists and returns full `SysRole` objects.

**Trade-off acknowledged**: This results in N+1 queries (1 query for users + N queries for roles). For an admin page with a small user count, this is acceptable. If performance becomes a concern, a single batch query can be added later.

### 3. Frontend: Add Roles column with `el-tag` components

Add a new `el-table-column` between "status" and "createTime" showing role names as small `el-tag` components. Use `type="info"` for a neutral appearance. If a user has no roles, display a muted "无角色" text.

## Risks / Trade-offs

- **N+1 query in user list**: Each user triggers a separate role query. Mitigation: Acceptable for admin panel usage (low traffic, small user count). Can be optimized with a batch query if needed.
- **Transient field on entity**: `SysUser` gains a field that MyBatis ignores. No risk to existing inserts/updates since `@TableField(exist = false)` ensures it's excluded from SQL.
