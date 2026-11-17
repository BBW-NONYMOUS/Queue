-- Forward migration. Stop ALL clients; take a DBA backup before V005 through V007.
-- DDL commits implicitly. Restore the backup if this maintenance sequence fails.
USE queue_clearance;
ALTER TABLE queue_tickets ADD COLUMN issue_request_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NULL;
DELIMITER $$
DROP TRIGGER bi_queue_counters$$
CREATE TRIGGER bi_queue_counters BEFORE INSERT ON queue_counters
FOR EACH ROW
BEGIN
    SET NEW.created_at = CURRENT_TIMESTAMP(6);
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
    IF NEW.queue_date <> CURRENT_DATE THEN
        SIGNAL SQLSTATE '45001' SET MESSAGE_TEXT = 'Queue date changed';
    END IF;
END$$

DROP TRIGGER bu_queue_counters$$
CREATE TRIGGER bu_queue_counters BEFORE UPDATE ON queue_counters
FOR EACH ROW
BEGIN
    IF NEW.last_seq <> OLD.last_seq AND NEW.queue_date <> CURRENT_DATE THEN
        SIGNAL SQLSTATE '45001' SET MESSAGE_TEXT = 'Queue date changed';
    END IF;
    IF NOT (NEW.org_id <=> OLD.org_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    IF NOT (NEW.queue_date <=> OLD.queue_date) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    SET NEW.created_at = OLD.created_at;
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
END$$

DROP TRIGGER bi_queue_tickets$$
CREATE TRIGGER bi_queue_tickets BEFORE INSERT ON queue_tickets
FOR EACH ROW
BEGIN
    IF NEW.issue_request_id IS NULL OR CHAR_LENGTH(NEW.issue_request_id) <> 36 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ticket request key required';
    END IF;
    SET NEW.created_at = CURRENT_TIMESTAMP(6);
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
    IF NEW.queue_date <> CURRENT_DATE THEN
        SIGNAL SQLSTATE '45001' SET MESSAGE_TEXT = 'Queue date changed';
    END IF;
    SET NEW.sort_at = CURRENT_TIMESTAMP(6);
    IF NEW.called_at IS NOT NULL THEN
        SET NEW.called_at = CURRENT_TIMESTAMP(6);
    END IF;
    IF NEW.serving_at IS NOT NULL THEN
        SET NEW.serving_at = CURRENT_TIMESTAMP(6);
    END IF;
    IF NEW.completed_at IS NOT NULL THEN
        SET NEW.completed_at = CURRENT_TIMESTAMP(6);
    END IF;
    IF NEW.cancelled_at IS NOT NULL THEN
        SET NEW.cancelled_at = CURRENT_TIMESTAMP(6);
    END IF;
END$$

DROP TRIGGER bu_queue_tickets$$
CREATE TRIGGER bu_queue_tickets BEFORE UPDATE ON queue_tickets
FOR EACH ROW
BEGIN
    IF OLD.issue_request_id IS NOT NULL AND NOT (NEW.issue_request_id <=> OLD.issue_request_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ticket request key is immutable';
    END IF;
    IF NEW.status <> OLD.status AND NEW.status IN ('waiting','called','serving','deferred') AND NEW.queue_date <> CURRENT_DATE THEN
        SIGNAL SQLSTATE '45001' SET MESSAGE_TEXT = 'Queue date changed';
    END IF;
    IF NOT (NEW.id <=> OLD.id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    IF NOT (NEW.student_id <=> OLD.student_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    IF NOT (NEW.org_id <=> OLD.org_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    IF NOT (NEW.term_id <=> OLD.term_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    IF NOT (NEW.queue_date <=> OLD.queue_date) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    IF NOT (NEW.seq <=> OLD.seq) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    IF NOT (NEW.issued_by <=> OLD.issued_by) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    SET NEW.created_at = OLD.created_at;
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
    IF NOT (NEW.sort_at <=> OLD.sort_at) THEN
        SET NEW.sort_at = CURRENT_TIMESTAMP(6);
    END IF;
    IF NEW.called_at IS NOT NULL AND NOT (NEW.called_at <=> OLD.called_at) THEN
        SET NEW.called_at = CURRENT_TIMESTAMP(6);
    END IF;
    IF NEW.serving_at IS NOT NULL AND NOT (NEW.serving_at <=> OLD.serving_at) THEN
        SET NEW.serving_at = CURRENT_TIMESTAMP(6);
    END IF;
    IF NEW.completed_at IS NOT NULL AND NOT (NEW.completed_at <=> OLD.completed_at) THEN
        SET NEW.completed_at = CURRENT_TIMESTAMP(6);
    END IF;
    IF NEW.cancelled_at IS NOT NULL AND NOT (NEW.cancelled_at <=> OLD.cancelled_at) THEN
        SET NEW.cancelled_at = CURRENT_TIMESTAMP(6);
    END IF;
END$$

DROP TRIGGER ai_queue_tickets$$
CREATE TRIGGER ai_queue_tickets AFTER INSERT ON queue_tickets
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'INSERT', 'queue_tickets', CAST(NEW.id AS CHAR),
        NULL,
        JSON_OBJECT(
            'id', NEW.id,
            'issue_request_id', NEW.issue_request_id,
            'student_id', NEW.student_id,
            'org_id', NEW.org_id,
            'term_id', NEW.term_id,
            'queue_date', NEW.queue_date,
            'seq', NEW.seq,
            'status', NEW.status,
            'sort_at', NEW.sort_at,
            'defer_count', NEW.defer_count,
            'issued_by', NEW.issued_by,
            'served_by', NEW.served_by,
            'called_at', NEW.called_at,
            'serving_at', NEW.serving_at,
            'completed_at', NEW.completed_at,
            'cancelled_at', NEW.cancelled_at,
            'cancellation_reason', NEW.cancellation_reason,
            'active_student_id', NEW.active_student_id,
            'occupied_org_id', NEW.occupied_org_id,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

DROP TRIGGER au_queue_tickets$$
CREATE TRIGGER au_queue_tickets AFTER UPDATE ON queue_tickets
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'UPDATE', 'queue_tickets', CAST(NEW.id AS CHAR),
        JSON_OBJECT(
            'id', OLD.id,
            'issue_request_id', OLD.issue_request_id,
            'student_id', OLD.student_id,
            'org_id', OLD.org_id,
            'term_id', OLD.term_id,
            'queue_date', OLD.queue_date,
            'seq', OLD.seq,
            'status', OLD.status,
            'sort_at', OLD.sort_at,
            'defer_count', OLD.defer_count,
            'issued_by', OLD.issued_by,
            'served_by', OLD.served_by,
            'called_at', OLD.called_at,
            'serving_at', OLD.serving_at,
            'completed_at', OLD.completed_at,
            'cancelled_at', OLD.cancelled_at,
            'cancellation_reason', OLD.cancellation_reason,
            'active_student_id', OLD.active_student_id,
            'occupied_org_id', OLD.occupied_org_id,
            'created_at', OLD.created_at,
            'updated_at', OLD.updated_at),
        JSON_OBJECT(
            'id', NEW.id,
            'issue_request_id', NEW.issue_request_id,
            'student_id', NEW.student_id,
            'org_id', NEW.org_id,
            'term_id', NEW.term_id,
            'queue_date', NEW.queue_date,
            'seq', NEW.seq,
            'status', NEW.status,
            'sort_at', NEW.sort_at,
            'defer_count', NEW.defer_count,
            'issued_by', NEW.issued_by,
            'served_by', NEW.served_by,
            'called_at', NEW.called_at,
            'serving_at', NEW.serving_at,
            'completed_at', NEW.completed_at,
            'cancelled_at', NEW.cancelled_at,
            'cancellation_reason', NEW.cancellation_reason,
            'active_student_id', NEW.active_student_id,
            'occupied_org_id', NEW.occupied_org_id,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$
DELIMITER ;
ALTER TABLE audit_logs ADD COLUMN queue_call_student_id BIGINT UNSIGNED
GENERATED ALWAYS AS (CASE
 WHEN table_name='queue_tickets' AND action='UPDATE'
 AND JSON_UNQUOTE(JSON_EXTRACT(new_values,'$.status'))='called'
 AND (COALESCE(JSON_UNQUOTE(JSON_EXTRACT(old_values,'$.status')),'')<>'called'
 OR NOT (JSON_EXTRACT(new_values,'$.called_at') <=> JSON_EXTRACT(old_values,'$.called_at')))
 THEN CAST(JSON_UNQUOTE(JSON_EXTRACT(new_values,'$.student_id')) AS UNSIGNED) ELSE NULL END) VIRTUAL,
ADD INDEX idx_queue_call_events(queue_call_student_id,occurred_at,id);
