## 1. Backend — Entity & Service

- [ ] 1.1 Add transient `roles` field (`List<SysRole>`) to `SysUser` entity with `@TableField(exist = false)`
- [ ] 1.2 Inject `SysRoleMapper` into `UserService` and modify `findAll()` to populate each user's `roles` list

## 2. Frontend — API Interface

- [ ] 2.1 Add `roles: SysRole[]` field to the `SysUser` interface in `admin.ts`

## 3. Frontend — UI Table

- [ ] 3.1 Add a "角色" column to the user table in `AdminUserManage.vue`, rendering role names as `el-tag` components (or "无角色" placeholder when empty)

## 4. Verification

- [ ] 4.1 Compile backend with IDEA Maven command and verify no errors
- [ ] 4.2 Verify the admin users page displays role tags correctly at `http://127.0.0.1:3001/admin/users`
