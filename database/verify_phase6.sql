-- Read-only verification after V008. Run as DBA; investigate each FAIL before resuming clients.
USE queue_clearance;
SELECT CASE WHEN COUNT(*)=3 THEN 'PASS: audit table/time index' ELSE 'FAIL: audit index' END result
FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='audit_logs' AND index_name='idx_audit_table_time';
SELECT CASE WHEN COUNT(*)=10 THEN 'PASS: ten domain tables' ELSE 'FAIL: table count' END result
FROM information_schema.tables WHERE table_schema=DATABASE() AND table_type='BASE TABLE';
SELECT CASE WHEN COUNT(*)=47 THEN 'PASS: integrity and audit triggers' ELSE 'FAIL: trigger count' END result
FROM information_schema.triggers WHERE trigger_schema=DATABASE();
SELECT CASE WHEN COUNT(*)=0 THEN 'PASS: ordinary clearance has no debt' ELSE 'FAIL: cleared balance without exception' END result
FROM clearance_records c JOIN v_office_balances b ON b.student_id=c.student_id AND b.org_id=c.org_id AND b.term_id=c.term_id
WHERE c.status='cleared' AND c.balance_exception=0 AND b.balance>0;
SELECT CASE WHEN COUNT(*)=0 THEN 'PASS: reasoned clearance exceptions' ELSE 'FAIL: incomplete exception' END result
FROM clearance_records WHERE balance_exception=1 AND (status<>'cleared' OR exception_reason IS NULL OR CHAR_LENGTH(TRIM(exception_reason))=0 OR exception_by IS NULL OR exception_at IS NULL);
-- Read-only audit viewer and reports do not insert domain records. Backup has a
-- separate filesystem manifest; the application's audit_logs grant remains SELECT.
