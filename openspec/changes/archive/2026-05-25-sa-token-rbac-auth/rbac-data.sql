-- ============================================================
-- DataAgent RBAC 数据初始化 (DML)
-- 在 rbac-schema.sql 执行完毕后执行
-- ============================================================

-- 插入四个默认角色
INSERT INTO sys_role (role_code, role_name, description) VALUES
('super-admin', '超级管理员', '拥有系统全部权限，可管理用户、角色和权限'),
('admin', '管理员', '可管理Agent、数据源、知识库等，可发布Agent'),
('analyst', '分析师', '可创建和编辑自己的Agent，使用对话功能'),
('viewer', '访客', '仅可查看和对话，不能修改任何配置');

-- 插入全部权限
INSERT INTO sys_permission (permission_code, permission_name, resource, action, description) VALUES
('agent:view', '查看Agent', 'agent', 'view', '查看Agent列表和详情'),
('agent:create', '创建Agent', 'agent', 'create', '创建新的Agent'),
('agent:update', '修改Agent', 'agent', 'update', '修改Agent配置'),
('agent:delete', '删除Agent', 'agent', 'delete', '删除Agent'),
('agent:publish', '发布/下线Agent', 'agent', 'publish', '发布或下线Agent'),
('agent:apikey', '管理API Key', 'agent', 'apikey', '管理Agent的API Key'),
('datasource:view', '查看数据源', 'datasource', 'view', '查看数据源列表和详情'),
('datasource:create', '创建数据源', 'datasource', 'create', '创建新的数据源'),
('datasource:update', '修改数据源', 'datasource', 'update', '修改数据源配置'),
('datasource:delete', '删除数据源', 'datasource', 'delete', '删除数据源'),
('knowledge:view', '查看知识库', 'knowledge', 'view', '查看知识库内容'),
('knowledge:create', '创建知识', 'knowledge', 'create', '创建新的知识条目'),
('knowledge:update', '修改知识', 'knowledge', 'update', '修改知识内容'),
('knowledge:delete', '删除知识', 'knowledge', 'delete', '删除知识条目'),
('knowledge:refresh', '刷新向量存储', 'knowledge', 'refresh', '刷新知识库的向量存储'),
('model-config:view', '查看模型配置', 'model-config', 'view', '查看模型配置'),
('model-config:manage', '管理模型配置', 'model-config', 'manage', '增删改模型配置'),
('prompt-config:view', '查看提示词配置', 'prompt-config', 'view', '查看提示词配置'),
('prompt-config:manage', '管理提示词配置', 'prompt-config', 'manage', '增删改提示词配置'),
('chat:use', '使用对话', 'chat', 'use', '使用Agent对话功能'),
('semantic-model:view', '查看语义模型', 'semantic-model', 'view', '查看语义模型'),
('semantic-model:manage', '管理语义模型', 'semantic-model', 'manage', '增删改语义模型'),
('preset-question:view', '查看预设问题', 'preset-question', 'view', '查看预设问题'),
('preset-question:manage', '管理预设问题', 'preset-question', 'manage', '增删改预设问题'),
('file:upload', '上传文件', 'file', 'upload', '上传文件'),
('system:user:manage', '管理用户', 'system', 'manage', '管理系统用户'),
('system:role:manage', '管理角色权限', 'system', 'manage', '管理系统角色和权限');

-- 插入默认 super-admin 用户 (密码: admin123)
INSERT INTO sys_user (id, username, password, status) VALUES
(1, 'admin', '$2a$10$v7pmKZOFVaYXJSYu9YGDXOqfrD3zxemSsR79mtdWt3udwlUF8n0Hy', 1);

-- 给用户分配 super-admin 角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT 1, id FROM sys_role WHERE role_code = 'super-admin';

-- super-admin 拥有全部权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p WHERE r.role_code = 'super-admin';

-- admin 拥有除系统管理和 API Key 外的全部权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'admin'
  AND p.permission_code NOT IN ('system:user:manage', 'system:role:manage', 'agent:apikey');

-- analyst 权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'analyst'
  AND p.permission_code IN (
    'agent:view', 'agent:create', 'agent:update', 'agent:delete',
    'datasource:view',
    'knowledge:view', 'knowledge:create', 'knowledge:update', 'knowledge:delete',
    'model-config:view', 'prompt-config:view', 'chat:use',
    'semantic-model:view', 'preset-question:view', 'file:upload'
  );

-- viewer 权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'viewer'
  AND p.permission_code IN (
    'agent:view', 'datasource:view', 'knowledge:view',
    'model-config:view', 'prompt-config:view', 'chat:use',
    'semantic-model:view', 'preset-question:view'
  );

-- 回填现有 agent 的 created_by
UPDATE agent SET created_by = COALESCE(admin_id, 1) WHERE created_by IS NULL;

-- 完成。默认账号: admin / admin123
