-- Data migration only. Keep all clients stopped. Run after V005, before V007.
USE queue_clearance;
START TRANSACTION;
SET @audit_user_id=NULL, @audit_request_id='migration-v006';
UPDATE queue_tickets SET issue_request_id=UUID() WHERE issue_request_id IS NULL;
COMMIT;
SET @audit_user_id=NULL, @audit_request_id=NULL;
