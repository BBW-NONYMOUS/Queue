-- Forward-only index migration. Stop clients and take a DBA backup before applying once.
USE queue_clearance;
CREATE INDEX idx_audit_table_time ON audit_logs(table_name,occurred_at,id);
