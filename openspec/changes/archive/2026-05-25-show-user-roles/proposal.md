## Why

The admin users page (`/admin/users`) currently allows super-admins to assign roles to users but provides no visibility into what roles each user already has. To see a user's current roles, an admin must open the "Assign Role" dialog for each individual user. This makes auditing user permissions tedious and error-prone, especially as the user base grows.

## What Changes

- **Backend**: `GET /api/admin/users` endpoint returns role information alongside each user record (role IDs, codes, and names)
- **Frontend**: `AdminUserManage.vue` table displays a "Roles" column showing each user's assigned roles as tags
- **Frontend**: `SysUser` TypeScript interface updated to include roles field

## Capabilities

### New Capabilities
- `user-role-visibility`: Display each user's currently assigned roles in the admin users list table

### Modified Capabilities
<!-- none - the existing role assignment functionality is unchanged -->

## Impact

- `AdminUserController.java` — `listUsers()` method needs to include role data
- `UserService.java` / `SysUserMapper.java` — may need a new query or DTO to join `sys_user_role` + `sys_role`
- `SysUser` entity or a new response DTO — add transient role list field
- `admin.ts` (frontend API service) — update `SysUser` interface
- `AdminUserManage.vue` — add Roles column and render role tags
