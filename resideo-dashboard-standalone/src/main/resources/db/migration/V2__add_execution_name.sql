ALTER TABLE executions ADD COLUMN IF NOT EXISTS name VARCHAR(255);

UPDATE executions SET name = 'Execution ' || SUBSTRING(cast(id as varchar), 1, 8) WHERE name IS NULL;
