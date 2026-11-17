-- Run as DBA after installation on an otherwise idle database.
-- The smoke-test data and its audit entries are rolled back; AUTO_INCREMENT
-- sequences may have gaps, which are intentional and harmless.
USE queue_clearance;
SET SESSION time_zone = '+08:00';
SELECT VERSION() AS server_version, @@session.time_zone AS session_time_zone;
SELECT IF(COUNT(*) = 10, 'PASS', 'FAIL') AS ten_tables
FROM information_schema.tables WHERE table_schema = 'queue_clearance' AND table_type = 'BASE TABLE';
SELECT IF(COUNT(*) = 47, 'PASS', 'FAIL') AS forty_seven_triggers
FROM information_schema.triggers WHERE trigger_schema = 'queue_clearance';
SELECT IF(COUNT(*) = 3, 'PASS', 'FAIL') AS three_views
FROM information_schema.views WHERE table_schema = 'queue_clearance';
SELECT IF(COUNT(*) = 7, 'PASS', 'FAIL') AS seven_offices FROM organizations;
SELECT IF(COUNT(*) = 1, 'PASS', 'FAIL') AS bootstrap_admin
FROM users WHERE username = 'admin' AND role = 'admin' AND is_active = TRUE;
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL') AS password_hashes_excluded_from_audit
FROM audit_logs WHERE JSON_CONTAINS_PATH(new_values, 'one', '$.password_hash')
    OR JSON_CONTAINS_PATH(old_values, 'one', '$.password_hash');

SET @audit_user_id = (SELECT id FROM users WHERE username = 'admin');
SET @audit_request_id = UUID();
SET @verification_request = @audit_request_id;
SET @verification_org = (SELECT id FROM organizations WHERE code = 'PSITS');
SET @verification_term = (SELECT id FROM terms WHERE code = '2026-2027-1');
START TRANSACTION;
INSERT INTO students (student_number, first_name, last_name, program, year_level, section)
VALUES (CONCAT('VERIFY-', UUID()), 'Schema', 'Verification', 'BSIT', 1, 'A');
SET @verification_student = LAST_INSERT_ID();
INSERT INTO fines (student_id, org_id, term_id, reason, amount, assessed_by)
VALUES (@verification_student, @verification_org, @verification_term, 'Schema smoke test', 100.00, @audit_user_id);
SET @verification_fine = LAST_INSERT_ID();
INSERT INTO payments (fine_id, amount, receipt_reference, received_by)
VALUES (@verification_fine, 40.00, CONCAT('VERIFY-', UUID()), @audit_user_id);
SET @verification_payment = LAST_INSERT_ID();
SELECT IF(balance = 60.00 AND status = 'partial', 'PASS', 'FAIL') AS partial_payment_balance
FROM v_fine_balances WHERE fine_id = @verification_fine;
UPDATE payments SET voided_at = CURRENT_TIMESTAMP(6), voided_by = @audit_user_id,
    void_reason = 'Schema verification rollback' WHERE id = @verification_payment;
SELECT IF(balance = 100.00 AND status = 'unpaid', 'PASS', 'FAIL') AS void_restores_balance
FROM v_fine_balances WHERE fine_id = @verification_fine;
UPDATE fines SET waived_at = CURRENT_TIMESTAMP(6), waived_by = @audit_user_id,
    waiver_reason = 'Schema verification waiver' WHERE id = @verification_fine;
SELECT IF(balance = 0.00 AND status = 'waived', 'PASS', 'FAIL') AS waiver_balance
FROM v_fine_balances WHERE fine_id = @verification_fine;
-- Exercise the same counter row used by future ticket issuance, with a lock.
SELECT id FROM organizations WHERE id = @verification_org FOR UPDATE;
INSERT INTO queue_counters (org_id, queue_date, last_seq)
VALUES (@verification_org, CURRENT_DATE, 0)
ON DUPLICATE KEY UPDATE last_seq = last_seq;
SELECT last_seq INTO @verification_seq FROM queue_counters
WHERE org_id = @verification_org AND queue_date = CURRENT_DATE FOR UPDATE;
SET @verification_seq = @verification_seq + 1;
UPDATE queue_counters SET last_seq = @verification_seq
WHERE org_id = @verification_org AND queue_date = CURRENT_DATE;
INSERT INTO queue_tickets (student_id, org_id, term_id, seq, issued_by)
VALUES (@verification_student, @verification_org, @verification_term, @verification_seq, @audit_user_id);
SELECT IF(COUNT(*) = 1, 'PASS', 'FAIL') AS waiting_ticket
FROM queue_tickets WHERE student_id = @verification_student AND status = 'waiting';
INSERT INTO clearance_records (student_id, org_id, term_id)
VALUES (@verification_student, @verification_org, @verification_term);
SELECT IF(COUNT(*) >= 9, 'PASS', 'FAIL') AS all_smoke_writes_audited
FROM audit_logs WHERE request_id = @verification_request;
ROLLBACK;
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL') AS rollback_includes_audit
FROM audit_logs WHERE request_id = @verification_request;
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL') AS rollback_includes_student
FROM students WHERE id = @verification_student;
SET @audit_user_id = NULL;
SET @audit_request_id = NULL;
