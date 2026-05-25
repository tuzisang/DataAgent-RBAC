## 1. Schema Fix

- [x] 1.1 Add `created_by BIGINT` column and `idx_created_by` index to `agent` table in `schema.sql`
- [x] 1.2 Add `created_by BIGINT` column and `idx_created_by` index to `h2/schema-h2.sql` (test schema)
- [x] 1.3 Create `migration.sql` with ALTER TABLE for existing databases

## 2. Mapper Fix

- [x] 2.1 Add `created_by` field to `AgentMapper.insert` INSERT statement

## 3. Verify

- [x] 3.1 Compile project with `mvn compile -P AL-repo,M2-repo,TP-repo`
