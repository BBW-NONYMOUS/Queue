-- Read-only checks after V003. Execute with the MySQL 8.4 client as DBA.
USE queue_clearance;
SET SESSION time_zone = '+08:00';
SELECT VERSION() AS server_version;
SELECT IF(GROUP_CONCAT(column_name ORDER BY seq_in_index) = 'role,is_active,id', 'PASS', 'FAIL') AS administration_lock_index
FROM information_schema.statistics
WHERE table_schema = 'queue_clearance' AND table_name = 'users' AND index_name = 'idx_users_role_active';
SELECT IF(COUNT(*) >= 1, 'PASS', 'FAIL') AS active_administrator_exists
FROM users WHERE role = 'admin' AND is_active = TRUE;
SELECT IF(COUNT(*) = 7, 'PASS', 'FAIL') AS seven_offices FROM organizations;
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL') AS inactive_office_queues_closed
FROM organizations WHERE is_active = FALSE AND queue_open = TRUE;
SELECT IF(COUNT(*) = 10, 'PASS', 'FAIL') AS ten_innodb_tables
FROM information_schema.tables WHERE table_schema = 'queue_clearance' AND table_type = 'BASE TABLE' AND engine = 'InnoDB';
SELECT IF(COUNT(*) = 47, 'PASS', 'FAIL') AS audit_and_integrity_triggers_retained
FROM information_schema.triggers WHERE trigger_schema = 'queue_clearance';
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL') AS no_password_hash_in_audit
FROM audit_logs WHERE JSON_CONTAINS_PATH(new_values, 'one', '$.password_hash')
    OR JSON_CONTAINS_PATH(old_values, 'one', '$.password_hash');
