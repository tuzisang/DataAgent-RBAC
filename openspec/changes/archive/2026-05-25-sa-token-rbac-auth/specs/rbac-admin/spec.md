## 新增需求

### 需求：super-admin 用户管理
系统必须提供用户管理的 CRUD 接口，仅 `super-admin` 可访问。包括创建用户、禁用/启用账号、重置密码、分配角色。

#### 场景：super-admin 创建新用户
- **当** super-admin 向 `/api/admin/users` POST 用户名、密码和角色列表
- **那么** 系统创建用户并返回创建后的用户对象

#### 场景：非 super-admin 尝试用户管理
- **当** `admin` 调用 `/api/admin/users`
- **那么** 系统返回 HTTP 403，消息为"需要超级管理员权限"

#### 场景：super-admin 禁用用户账号
- **当** super-admin 向 `/api/admin/users/{id}/status` PUT `status=disabled`
- **那么** 该用户的 `status` 字段被更新，且现有 Token 被作废

### 需求：super-admin 角色管理
系统必须提供接口供 `super-admin` 查看和修改角色-权限映射关系。

#### 场景：super-admin 更新角色权限
- **当** super-admin 向 `/api/admin/roles/{roleCode}/permissions` PUT 新的权限列表
- **那么** 该角色的权限被替换为新列表

#### 场景：super-admin 列出所有角色及权限
- **当** super-admin GET `/api/admin/roles`
- **那么** 系统返回所有角色及其关联权限

### 需求：权限列表展示
系统必须提供一个只读接口，列出系统中全部可用权限，按资源分组。

#### 场景：super-admin 查看权限目录
- **当** super-admin GET `/api/admin/permissions`
- **那么** 系统返回按资源名分组的所有权限编码

### 需求：前端管理页面访问控制
前端管理页面（`/admin/users`、`/admin/roles`、`/admin/permissions`）仅对拥有 `super-admin` 角色的用户开放。未授权用户必须被重定向到首页。

#### 场景：viewer 访问管理页面
- **当** `viewer` 角色用户访问 `/admin/users`
- **那么** 前端重定向到 `/`
