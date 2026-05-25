## 新增需求

### 需求：双认证通道
系统必须支持两个独立的认证通道：Sa-Token 用于 Web UI 用户，API Key 用于外部系统集成。当请求头中存在前缀为 `sk-` 的 `X-API-Key` 时，`SaReactorFilter` 必须跳过 Sa-Token 校验。

#### 场景：外部系统使用有效 API Key 调用
- **当** 请求 `/api/agent/123/api-key/status` 时带有 `X-API-Key: sk-xxxxxxxx`
- **那么** `ApiKeyAuthFilter` 校验该 Key，请求绕过 Sa-Token 检查

#### 场景：外部系统使用无效 API Key 调用
- **当** 请求带有无效或已禁用的 `X-API-Key`
- **那么** `ApiKeyAuthFilter` 返回 HTTP 401

### 需求：API Key 过滤器执行顺序
`ApiKeyAuthFilter` 必须在 WebFlux 过滤器链中位于 `SaReactorFilter` 之后执行。API Key 路径（`/api/agent/*/api-key/**`）必须从 Sa-Token 登录检查中排除。

#### 场景：API Key 路径绕过登录检查
- **当** 请求 `/api/agent/1/api-key/generate` 无 Sa-Token 但有有效 API Key
- **那么** `SaReactorFilter` 放行，`ApiKeyAuthFilter` 校验该 Key

### 需求：SSE 流式端点认证
SSE 流式端点 `/api/stream/search` 必须接受以下认证方式：通过 `Authorization: Bearer <token>` 请求头或 `?token=<token>` URL 参数进行 Sa-Token 认证，以及通过 `X-API-Key` 请求头进行 API Key 认证。

#### 场景：SSE 使用 URL 参数传递 Sa-Token
- **当** Web UI 客户端连接 `/api/stream/search?token=xxx&agentId=1&query=test`
- **那么** Controller 提取 Token，使用 `StpUtil` 校验，然后继续流式输出

#### 场景：SSE 使用请求头传递 API Key
- **当** 外部系统连接 `/api/stream/search` 并带有 `X-API-Key: sk-xxx`
- **那么** Controller 校验 API Key，并确保它与请求的 `agentId` 匹配

#### 场景：SSE 无任何认证
- **当** 客户端连接 `/api/stream/search` 既无 Token 也无 API Key
- **那么** Controller 返回一个带错误状态的 SSE 事件并关闭流

### 需求：API Key 范围校验
使用 API Key 认证时，系统必须校验请求的 Agent 与 API Key 所属的 Agent 是否匹配。通过 API Key 跨 Agent 访问必须被拒绝。

#### 场景：API Key 访问错误的 Agent
- **当** 请求使用 Agent 1 的 API Key 访问 Agent 2 的数据
- **那么** 系统返回 HTTP 403，消息为"API Key 无权访问此 Agent"
