-- ============================================================
-- DataAgent RBAC 权限认证迁移脚本
-- 数据库: MySQL (saa_data_agent2)
-- 说明: 为集成 Sa-Token RBAC 创建用户/角色/权限表，并修改现有表
-- 执行方式: 在 MySQL 客户端中执行，或配置 spring.sql.init.mode=always
-- ============================================================

-- -------------------------------------------------------
-- 1. 创建 RBAC 核心表
-- -------------------------------------------------------

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码(BCrypt哈希)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    permission_code VARCHAR(64) NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(64) NOT NULL COMMENT '权限名称',
    resource VARCHAR(32) NOT NULL COMMENT '资源标识',
    action VARCHAR(32) NOT NULL COMMENT '操作标识',
    description VARCHAR(255) DEFAULT NULL COMMENT '权限描述',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

-- -------------------------------------------------------
-- 2. 修改现有表: agent 增加 created_by 字段
--    兼容旧版 MySQL（不支持 ADD COLUMN IF NOT EXISTS）
-- -------------------------------------------------------

SET @dbname = DATABASE();
SET @columnname = 'created_by';
SET @addcol_sql = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @dbname
              AND TABLE_NAME = 'agent'
              AND COLUMN_NAME = @columnname
        ),
        'SELECT 1;',
        'ALTER TABLE agent ADD COLUMN created_by BIGINT DEFAULT NULL COMMENT "创建者ID(引用sys_user.id)", ADD INDEX idx_created_by (created_by);'
    )
);
PREPARE stmt FROM @addcol_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------
-- 3. 插入默认数据
-- -------------------------------------------------------

-- 3.1 插入四个默认角色
INSERT INTO sys_role (role_code, role_name, description) VALUES
('super-admin', '超级管理员', '拥有系统全部权限，可管理用户、角色和权限'),
('admin', '管理员', '可管理Agent、数据源、知识库等，可发布Agent'),
('analyst', '分析师', '可创建和编辑自己的Agent，使用对话功能'),
('viewer', '访客', '仅可查看和对话，不能修改任何配置')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- 3.2 插入全部权限 (resource:action 扁平编码)
INSERT INTO sys_permission (permission_code, permission_name, resource, action, description) VALUES
-- Agent 模块
('agent:view', '查看Agent', 'agent', 'view', '查看Agent列表和详情'),
('agent:create', '创建Agent', 'agent', 'create', '创建新的Agent'),
('agent:update', '修改Agent', 'agent', 'update', '修改Agent配置'),
('agent:delete', '删除Agent', 'agent', 'delete', '删除Agent'),
('agent:publish', '发布/下线Agent', 'agent', 'publish', '发布或下线Agent'),
('agent:apikey', '管理API Key', 'agent', 'apikey', '管理Agent的API Key'),
-- 数据源模块
('datasource:view', '查看数据源', 'datasource', 'view', '查看数据源列表和详情'),
('datasource:create', '创建数据源', 'datasource', 'create', '创建新的数据源'),
('datasource:update', '修改数据源', 'datasource', 'update', '修改数据源配置'),
('datasource:delete', '删除数据源', 'datasource', 'delete', '删除数据源'),
-- 知识库模块
('knowledge:view', '查看知识库', 'knowledge', 'view', '查看知识库内容'),
('knowledge:create', '创建知识', 'knowledge', 'create', '创建新的知识条目'),
('knowledge:update', '修改知识', 'knowledge', 'update', '修改知识内容'),
('knowledge:delete', '删除知识', 'knowledge', 'delete', '删除知识条目'),
('knowledge:refresh', '刷新向量存储', 'knowledge', 'refresh', '刷新知识库的向量存储'),
-- 模型配置模块
('model-config:view', '查看模型配置', 'model-config', 'view', '查看模型配置'),
('model-config:manage', '管理模型配置', 'model-config', 'manage', '增删改模型配置'),
-- 提示词配置模块
('prompt-config:view', '查看提示词配置', 'prompt-config', 'view', '查看提示词配置'),
('prompt-config:manage', '管理提示词配置', 'prompt-config', 'manage', '增删改提示词配置'),
-- 对话模块
('chat:use', '使用对话', 'chat', 'use', '使用Agent对话功能'),
-- 语义模型
('semantic-model:view', '查看语义模型', 'semantic-model', 'view', '查看语义模型'),
('semantic-model:manage', '管理语义模型', 'semantic-model', 'manage', '增删改语义模型'),
-- 预设问题
('preset-question:view', '查看预设问题', 'preset-question', 'view', '查看预设问题'),
('preset-question:manage', '管理预设问题', 'preset-question', 'manage', '增删改预设问题'),
-- 文件上传
('file:upload', '上传文件', 'file', 'upload', '上传文件'),
-- 系统管理
('system:user:manage', '管理用户', 'system', 'manage', '管理系统用户'),
('system:role:manage', '管理角色权限', 'system', 'manage', '管理系统角色和权限')
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name);

-- 3.3 插入默认 super-admin 用户 (密码: admin123, BCrypt 加密)
-- BCrypt 加密后的密码 '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO' 对应明文 'admin123'
INSERT INTO sys_user (id, username, password, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 1)
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- 3.4 给用户分配 super-admin 角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT 1, id FROM sys_role WHERE role_code = 'super-admin'
ON DUPLICATE KEY UPDATE user_id = user_id;

-- 3.5 给 super-admin 角色分配全部权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p WHERE r.role_code = 'super-admin'
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 3.6 给 admin 角色分配权限 (不含系统管理和API Key管理)
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'admin'
  AND p.permission_code NOT IN ('system:user:manage', 'system:role:manage', 'agent:apikey')
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 3.7 给 analyst 角色分配权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'analyst'
  AND p.permission_code IN (
    'agent:view', 'agent:create', 'agent:update', 'agent:delete',
    'datasource:view',
    'knowledge:view', 'knowledge:create', 'knowledge:update', 'knowledge:delete',
    'model-config:view',
    'prompt-config:view',
    'chat:use',
    'semantic-model:view',
    'preset-question:view',
    'file:upload'
  )
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 3.8 给 viewer 角色分配权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'viewer'
  AND p.permission_code IN (
    'agent:view',
    'datasource:view',
    'knowledge:view',
    'model-config:view',
    'prompt-config:view',
    'chat:use',
    'semantic-model:view',
    'preset-question:view'
  )
ON DUPLICATE KEY UPDATE role_id = role_id;

-- -------------------------------------------------------
-- 4. 回填现有 agent 的 created_by
-- -------------------------------------------------------

-- 将现有 agent 的 created_by 设为 admin (ID=1)
-- 如果已有 admin_id 且不为空，则使用 admin_id 作为 created_by
UPDATE agent SET created_by = COALESCE(admin_id, 1) WHERE created_by IS NULL;

-- -------------------------------------------------------
-- 5. 完成
-- -------------------------------------------------------
-- 默认 super-admin 账号:
--   用户名: admin
--   密码: admin123
--   请在首次登录后立即修改密码!
