-- Read-only verification AFTER V004; use the MySQL 8.4 client as DBA.
USE queue_clearance;
SET SESSION time_zone = '+08:00';
SELECT VERSION() AS server_version;
SELECT IF(COUNT(*) = 1, 'PASS', 'FAIL') AS payment_archive_column
FROM information_schema.columns WHERE table_schema = 'queue_clearance'
    AND table_name = 'payments' AND column_name = 'receipt_snapshot' AND data_type = 'json';
SELECT IF(COUNT(*) = 4, 'PASS', 'FAIL') AS receipt_triggers_installed
FROM information_schema.triggers WHERE trigger_schema = 'queue_clearance'
    AND trigger_name IN ('bi_payments','bu_payments','ai_payments','au_payments')
    AND action_statement LIKE '%receipt_snapshot%';
SELECT IF(COUNT(*) = 47, 'PASS', 'FAIL') AS all_integrity_triggers_retained
FROM information_schema.triggers WHERE trigger_schema = 'queue_clearance';
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL') AS no_overpaid_fines
FROM v_fine_balances WHERE paid_amount > amount;
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL') AS no_unapproved_clearance_with_balance
FROM clearance_records c JOIN v_office_balances b
    ON b.student_id = c.student_id AND b.org_id = c.org_id AND b.term_id = c.term_id
WHERE c.status = 'cleared' AND c.balance_exception = FALSE AND b.balance > 0;
SELECT COUNT(*) AS legacy_payments_without_archived_receipt
FROM payments WHERE receipt_snapshot IS NULL;
SELECT IF(COUNT(*) = 0, 'PASS', 'FAIL') AS password_hashes_excluded_from_audit
FROM audit_logs WHERE JSON_CONTAINS_PATH(new_values, 'one', '$.password_hash')
    OR JSON_CONTAINS_PATH(old_values, 'one', '$.password_hash');
