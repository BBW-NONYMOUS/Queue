-- Read-only post-migration checks. Run as DBA after V005, V006 and V007.
USE queue_clearance;
SELECT CASE WHEN COUNT(*)=10 THEN 'PASS: ten domain tables' ELSE 'FAIL: table count' END result
FROM information_schema.tables WHERE table_schema=DATABASE() AND table_type='BASE TABLE';
SELECT CASE WHEN COUNT(*)=47 THEN 'PASS: integrity/audit triggers retained' ELSE 'FAIL: trigger count' END result
FROM information_schema.triggers WHERE trigger_schema=DATABASE();
SELECT CASE WHEN COUNT(*)=1 THEN 'PASS: required issue request key' ELSE 'FAIL: request key' END result
FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='queue_tickets'
AND column_name='issue_request_id' AND is_nullable='NO';
SELECT CASE WHEN COUNT(*)=1 THEN 'PASS: unique request index' ELSE 'FAIL: request index' END result
FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='queue_tickets'
AND index_name='uq_queue_issue_request' AND non_unique=0;
SELECT CASE WHEN COUNT(*)=3 THEN 'PASS: indexed student call events' ELSE 'FAIL: call index' END result
FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='audit_logs'
AND index_name='idx_queue_call_events';
SELECT CASE WHEN COUNT(*)=0 THEN 'PASS: all tickets have request keys' ELSE 'FAIL: missing request keys' END result
FROM queue_tickets WHERE issue_request_id IS NULL OR CHAR_LENGTH(issue_request_id)<>36;
SELECT CASE WHEN COUNT(*)=0 THEN 'PASS: counters cover issued sequences' ELSE 'FAIL: counter mismatch' END result
FROM queue_tickets q JOIN queue_counters c ON c.org_id=q.org_id AND c.queue_date=q.queue_date WHERE q.seq>c.last_seq;
-- Both result sets must be empty. Prior-day active tickets are closed by the
-- officer's Close previous day, issue, or Call next operation, never by a poll.
SELECT org_id,COUNT(*) occupied FROM queue_tickets WHERE status IN ('called','serving') GROUP BY org_id HAVING COUNT(*)>1;
SELECT org_id,queue_date,student_id,COUNT(*) active_tickets FROM queue_tickets
WHERE status IN ('waiting','called','serving','deferred') GROUP BY org_id,queue_date,student_id HAVING COUNT(*)>1;
