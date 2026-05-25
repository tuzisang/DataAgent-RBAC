-- ============================================================
-- Agent Visibility Control — 独立 DDL
-- 在目标数据库中执行此脚本
-- ============================================================

CREATE TABLE IF NOT EXISTS sys_user_agent_visibility (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    agent_id BIGINT NOT NULL COMMENT '智能体ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_agent (user_id, agent_id),
    INDEX idx_user_id (user_id),
    INDEX idx_agent_id (agent_id)
) ENGINE = InnoDB COMMENT = '用户-Agent可见性分配表';
