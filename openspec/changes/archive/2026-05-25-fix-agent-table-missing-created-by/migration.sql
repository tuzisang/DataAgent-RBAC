-- Migration: Add created_by column to agent table
-- For existing databases that were created before this column was added to schema.sql
-- Execute this script manually on your production database if needed

ALTER TABLE agent
    ADD COLUMN created_by BIGINT COMMENT '创建者ID' AFTER admin_id,
    ADD INDEX idx_created_by (created_by);
