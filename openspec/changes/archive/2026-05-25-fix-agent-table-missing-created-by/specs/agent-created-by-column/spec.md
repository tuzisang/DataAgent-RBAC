## ADDED Requirements

### Requirement: Agent table has created_by column
The `agent` table SHALL include a `created_by` column of type BIGINT to store the ID of the user who created the agent.

#### Scenario: Table schema includes created_by
- **WHEN** the schema DDL for `agent` table is executed
- **THEN** the table SHALL have a `created_by BIGINT` column with a corresponding index `idx_created_by`

### Requirement: Agent insert writes created_by
When inserting a new agent record, the system SHALL persist the `created_by` value from the entity to the database column.

#### Scenario: Insert agent with creator
- **WHEN** a new agent is created with a non-null `createdBy` value
- **THEN** the `created_by` column in the inserted row SHALL equal the entity's `createdBy` value

#### Scenario: Insert agent without creator
- **WHEN** a new agent is created with a null `createdBy` value
- **THEN** the `created_by` column in the inserted row SHALL be NULL

### Requirement: Non-admin user can list agents without SQL error
A non-admin user calling `GET /api/agent/list` SHALL receive a list of agents filtered by their user ID, without any database error.

#### Scenario: Non-admin user lists agents
- **WHEN** a non-admin user with ID=5 requests `GET /api/agent/list`
- **THEN** the system SHALL query `agent` table with `WHERE created_by = 5` and return matching results without SQL syntax errors
