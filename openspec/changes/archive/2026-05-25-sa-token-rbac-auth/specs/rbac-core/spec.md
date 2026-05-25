## 新增需求

### 需求：基于角色的访问控制模型
系统必须实现标准 RBAC 模型，包含四个角色：`super-admin`、`admin`、`analyst`、`viewer`。权限必须以 `resource:action` 字符串形式编码。

#### 场景：系统初始化时创建默认角色
- **当** 系统首次启动
- **那么** 确保四个默认角色存在并带有预定义的权限集合

#### 场景：super-admin 拥有全部权限
- **当** 系统检查 `super-admin` 的权限
- **那么** 该用户拥有全部已定义权限，包括 `system:user:manage`

### 需求：用户-角色关联
系统必须支持通过 `sys_user_role` 关联表为每个用户分配一个或多个角色。

#### 场景：给用户分配 admin 角色
- **当** super-admin 给某个用户分配 `admin` 角色
- **那么** 该用户获得 `admin` 角色关联的所有权限

### 需求：角色-权限关联
系统必须支持通过 `sys_role_permission` 关联表为每个角色分配一个或多个权限。

#### 场景：为角色增加权限
- **当** super-admin 给 `analyst` 角色增加 `agent:publish` 权限
- **那么** 所有拥有 `analyst` 角色的用户都获得 `agent:publish` 权限

### 需求：权限编码格式
系统必须使用扁平的 `resource:action` 权限编码格式。合法的资源包括：`agent`、`datasource`、`knowledge`、`model-config`、`prompt-config`、`chat`、`system`。合法的操作包括：`view`、`create`、`update`、`delete`、`publish`、`apikey`、`manage`、`use`、`refresh`。

#### 场景：校验权限字符串格式
- **当** 系统处理 `agent:create` 的权限检查
- **那么** 它解析到 `sys_permission` 中编码为 `agent:create` 的权限记录

### 需求：Agent 数据归属
系统必须强制要求 `analyst` 角色用户只能修改或删除自己创建的 Agent。`agent` 表必须增加 `created_by` 字段，引用 `sys_user.id`。

#### 场景：Analyst 删除自己的 Agent
- **当** `analyst` 尝试删除自己创建的 Agent
- **那么** 删除成功

#### 场景：Analyst 删除他人的 Agent
- **当** `analyst` 尝试删除其他用户创建的 Agent
- **那么** 系统返回 HTTP 403，消息为"无权删除他人创建的 Agent"

#### 场景：Admin 删除任意 Agent
- **当** `admin` 尝试删除任意 Agent
- **那么** 删除成功，不受创建者限制
