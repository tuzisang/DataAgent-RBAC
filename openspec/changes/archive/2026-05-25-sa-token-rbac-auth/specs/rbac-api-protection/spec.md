## 新增需求

### 需求：全局认证过滤器
系统必须注册 `SaReactorFilter`，拦截所有 HTTP 请求并强制认证。开放路径必须包括 `/api/auth/login`、`/api/auth/register`、`/favicon.ico`、静态资源、Swagger 端点。

#### 场景：未认证请求访问受保护 API
- **当** 未认证客户端调用 `/api/agent/list`
- **那么** 系统返回 HTTP 401，消息为"未登录"

#### 场景：已认证请求访问受保护 API
- **当** 已认证客户端调用 `/api/agent/list`
- **那么** 请求继续进入 Controller

### 需求：方法级权限注解
所有变更型 Controller 方法（POST、PUT、DELETE）和敏感读取方法必须标注 `@SaCheckPermission`。管理操作必须使用 `@SaCheckRole("super-admin")`。

#### 场景：权限不足无法创建
- **当** `viewer` 调用 POST `/api/agent`（需要 `agent:create`）
- **那么** 系统返回 HTTP 403，消息为"无此权限: agent:create"

#### 场景：权限足够可以创建
- **当** `analyst` 调用 POST `/api/agent`（需要 `agent:create`）
- **那么** 请求继续进入 Service 层

### 需求：CORS 策略收紧
Controller 上的所有 `@CrossOrigin(origins = "*")` 注解必须移除。CORS 必须通过 `CorsWebFilter` 集中配置，白名单从 application 属性加载。

#### 场景：来自未授权源的请求
- **当** 请求来自配置白名单之外的源
- **那么** CORS 过滤器在认证之前将其拒绝

### 需求：认证失败统一错误响应
所有认证和授权失败必须返回统一的错误格式，与现有 `ApiResponse` 结构一致，包含 `code` 和 `message` 字段。

#### 场景：Sa-Token 抛出 NotLoginException
- **当** 无有效 Token 的请求命中受保护端点
- **那么** 全局异常处理器捕获并返回 `ApiResponse.error("未登录")`，HTTP 401

#### 场景：Sa-Token 抛出 NotPermissionException
- **当** 已认证用户缺少所需权限命中受保护端点
- **那么** 全局异常处理器捕获并返回 `ApiResponse.error("无此权限: xxx")`，HTTP 403
