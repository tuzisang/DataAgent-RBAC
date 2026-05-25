## ADDED Requirements

### Requirement: User list displays assigned roles

The admin user list table SHALL display each user's currently assigned roles as colored tags.

#### Scenario: User with roles shown in table
- **WHEN** a super-admin views the user management page at `/admin/users`
- **THEN** each user row displays their assigned role names as `el-tag` components in a dedicated "角色" column

#### Scenario: User with no roles
- **WHEN** a user has no assigned roles
- **THEN** the roles column displays a muted placeholder text "无角色"

#### Scenario: Role tags update after assignment
- **WHEN** a super-admin successfully assigns new roles to a user via the "分配角色" dialog
- **THEN** the user list refreshes and the roles column reflects the updated role assignments

### Requirement: User list API includes role data

The `GET /api/admin/users` endpoint SHALL include role information for each user in the response.

#### Scenario: User list with roles
- **WHEN** the API returns the user list
- **THEN** each user object contains a `roles` array with each role's `id`, `roleCode`, `roleName`, and `description` fields

#### Scenario: User with no assigned roles
- **WHEN** a user has no rows in `sys_user_role`
- **THEN** the user object contains an empty `roles` array
