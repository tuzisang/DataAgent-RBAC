-- 初始化数据文件
-- 只在表为空时插入示例数据

-- 业务知识示例数据
INSERT IGNORE INTO `business_knowledge` (`id`, `business_term`, `description`, `synonyms`, `is_recall`, `agent_id`, `created_time`, `updated_time`) VALUES
(1, 'Customer Satisfaction', 'Measures how satisfied customers are with the service or product.', 'customer happiness, client contentment', 0, 1, NOW(), NOW()),
(2, 'Net Promoter Score', 'A measure of the likelihood of customers recommending a company to others.', 'NPS, customer loyalty score', 0, 1, NOW(), NOW()),
(3, 'Customer Retention Rate', 'The percentage of customers who continue to use a service over a given period.', 'retention, customer loyalty', 0, 2, NOW(), NOW());

-- 语义模型示例数据
INSERT IGNORE INTO `semantic_model` (`id`, `agent_id`, `datasource_id`, `table_name`, `column_name`, `business_name`, `synonyms`, `business_description`, `column_comment`, `data_type`, `created_time`, `updated_time`, `status`) VALUES
(1, 1, 2, 'customer_feedback', 'csat_score', 'customerSatisfactionScore', 'satisfaction score, customer rating', 'Customer satisfaction rating from 1-10', '客户满意度评分', 'integer', NOW(), NOW(), 0),
(2, 1, 2, 'customer_feedback', 'nps_value', 'netPromoterScore', 'NPS, promoter score', 'Net Promoter Score from -100 to 100', '净推荐值', 'integer', NOW(), NOW(), 0),
(3, 2, 1, 'customer_metrics', 'retention_pct', 'customerRetentionRate', 'retention rate, loyalty rate', 'Percentage of retained customers', '客户保留率', 'decimal', NOW(), NOW(), 0);

-- 智能体示例数据
INSERT IGNORE INTO `agent` (`id`, `name`, `description`, `avatar`, `status`, `api_key`, `api_key_enabled`, `prompt`, `category`, `admin_id`, `tags`, `create_time`, `update_time`) VALUES
(1, '中国人口GDP数据智能体', '专门处理中国人口和GDP相关数据查询分析的智能体', '/avatars/china-gdp-agent.png', 'draft', NULL, 0, '你是一个专业的数据分析助手，专门处理中国人口和GDP相关的数据查询。请根据用户的问题，生成准确的SQL查询语句。', '数据分析', 2100246635, '人口数据,GDP分析,经济统计', NOW(), NOW()),
(2, '销售数据分析智能体', '专注于销售数据分析和业务指标计算的智能体', '/avatars/sales-agent.png', 'draft', NULL, 0, '你是一个销售数据分析专家，能够帮助用户分析销售趋势、客户行为和业务指标。', '业务分析', 2100246635, '销售分析,业务指标,客户分析', NOW(), NOW()),
(3, '财务报表智能体', '专门处理财务数据和报表分析的智能体', '/avatars/finance-agent.png', 'draft', NULL, 0, '你是一个财务分析专家，专门处理财务数据查询和报表生成。', '财务分析', 2100246635, '财务数据,报表分析,会计', NOW(), NOW()),
(4, '库存管理智能体', '专注于库存数据管理和供应链分析的智能体', '/avatars/inventory-agent.png', 'draft', NULL, 0, '你是一个库存管理专家，能够帮助用户查询库存状态、分析供应链数据。', '供应链', 2100246635, '库存管理,供应链,物流', NOW(), NOW());

-- 智能体知识示例数据
INSERT IGNORE INTO `agent_knowledge` (`id`, `agent_id`, `title`, `content`, `type`, `is_recall`, `embedding_status`, `file_type`, `created_time`, `updated_time`) VALUES 
(1, 1, '中国人口统计数据说明', '中国人口统计数据包含了历年的人口总数、性别比例、年龄结构、城乡分布等详细信息。数据来源于国家统计局，具有权威性和准确性。查询时请注意数据的时间范围和统计口径。', 'DOCUMENT', 1, 'PENDING', 'text', NOW(), NOW()),
(2, 1, 'GDP数据使用指南', 'GDP（国内生产总值）数据反映了国家经济发展水平。包含名义GDP、实际GDP、GDP增长率等指标。数据按季度和年度进行统计，支持按地区、行业进行分类查询。', 'DOCUMENT', 1, 'PENDING', 'text', NOW(), NOW()),
(3, 1, '常见查询问题', '问：如何查询2023年的人口数据？\n答：可以使用"SELECT * FROM population WHERE year = 2023"进行查询。\n\n问：如何计算GDP增长率？\n答：GDP增长率 = (当年GDP - 上年GDP) / 上年GDP * 100%', 'QA', 1, 'PENDING', 'text', NOW(), NOW()),
(4, 2, '销售数据字段说明', '销售数据表包含以下关键字段：\n- sales_amount：销售金额\n- customer_id：客户ID\n- product_id：产品ID\n- sales_date：销售日期\n- region：销售区域\n- sales_rep：销售代表', 'DOCUMENT', 1, 'PENDING', 'text', NOW(), NOW()),
(5, 2, '客户分析指标体系', '客户分析包含多个维度：\n1. 客户价值分析：RFM模型（最近购买时间、购买频次、购买金额）\n2. 客户生命周期：新客户、活跃客户、流失客户\n3. 客户满意度：NPS评分、满意度调研\n4. 客户行为分析：购买偏好、渠道偏好', 'DOCUMENT', 1, 'PENDING', 'text', NOW(), NOW()),
(6, 3, '财务报表模板', '标准财务报表包含：\n1. 资产负债表：反映企业财务状况\n2. 利润表：反映企业经营成果\n3. 现金流量表：反映企业现金流动情况\n4. 所有者权益变动表：反映股东权益变化', 'DOCUMENT', 1, 'PENDING', 'pdf', NOW(), NOW()),
(7, 4, '库存管理最佳实践', '库存管理的核心要点：\n1. 安全库存设置：确保不断货\n2. ABC分类管理：重点管理A类物料\n3. 先进先出原则：避免库存积压\n4. 定期盘点：确保数据准确性\n5. 供应商管理：建立稳定供应关系', 'DOCUMENT', 1, 'PENDING', 'text', NOW(), NOW());

-- 数据源示例数据
-- 示例数据源可以运行docker-compose-datasource.yml建立，或者手动修改为自己的数据源
INSERT IGNORE INTO `datasource` (`id`, `name`, `type`, `host`, `port`, `database_name`, `username`, `password`, `connection_url`, `status`, `test_status`, `description`, `creator_id`, `create_time`, `update_time`) VALUES 
(1, '生产环境MySQL数据库', 'mysql', 'mysql-data', 3306, 'product_db', 'root', 'root', 'jdbc:mysql://mysql-data:3306/product_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true', 'inactive', 'unknown', '生产环境主数据库，包含核心业务数据', 2100246635, NOW(), NOW()),
(2, '数据仓库PostgreSQL', 'postgresql', 'postgres-data', 5432, 'data_warehouse', 'postgres', 'postgres', 'jdbc:postgresql://postgres-data:5432/data_warehouse', 'inactive', 'unknown', '数据仓库，用于数据分析和报表生成', 2100246635, NOW(), NOW());

-- 智能体数据源关联示例数据
INSERT IGNORE INTO `agent_datasource` (`id`, `agent_id`, `datasource_id`, `is_active`, `create_time`, `update_time`) VALUES
(1, 1, 2, 0, NOW(), NOW()),  -- 中国人口GDP数据智能体使用数据仓库
(2, 2, 1, 0, NOW(), NOW()),  -- 销售数据分析智能体使用生产环境数据库
(3, 3, 1, 0, NOW(), NOW()),  -- 财务报表智能体使用生产环境数据库
(4, 4, 1, 0, NOW(), NOW());  -- 库存管理智能体使用生产环境数据库

-- ============================================================
-- RBAC 权限体系默认数据
-- ============================================================

-- 插入四个默认角色
INSERT IGNORE INTO `sys_role` (`role_code`, `role_name`, `description`) VALUES
('super-admin', '超级管理员', '拥有系统全部权限，可管理用户、角色和权限'),
('admin', '管理员', '可管理Agent、数据源、知识库等，可发布Agent'),
('analyst', '分析师', '可创建和编辑自己的Agent，使用对话功能'),
('viewer', '访客', '仅可查看和对话，不能修改任何配置');

-- 插入全部权限
INSERT IGNORE INTO `sys_permission` (`permission_code`, `permission_name`, `resource`, `action`, `description`) VALUES
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
INSERT IGNORE INTO `sys_user` (`id`, `username`, `password`, `status`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 1);

-- 给用户分配 super-admin 角色
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT 1, id FROM sys_role WHERE role_code = 'super-admin';

-- super-admin 拥有全部权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM sys_role r, sys_permission p WHERE r.role_code = 'super-admin';

-- admin 拥有除系统管理和 API Key 外的全部权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'admin'
  AND p.permission_code NOT IN ('system:user:manage', 'system:role:manage', 'agent:apikey');

-- analyst 权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
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
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'viewer'
  AND p.permission_code IN (
    'agent:view', 'datasource:view', 'knowledge:view',
    'model-config:view', 'prompt-config:view', 'chat:use',
    'semantic-model:view', 'preset-question:view'
  );

-- 回填现有 agent 的 created_by
UPDATE `agent` SET created_by = COALESCE(admin_id, 1) WHERE created_by IS NULL;
