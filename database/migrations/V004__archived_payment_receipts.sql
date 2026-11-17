-- Apply once as DBA after V003, with ALL application clients stopped.
-- DDL implicitly commits. Keep clients stopped until every trigger is restored.
-- Forward-only. Existing payments remain unchanged; historical snapshots are
-- unavailable for legacy rows and must never be fabricated from current names.
-- Recovery: restore the pre-migration backup, or repair through a new forward migration.
USE queue_clearance;
SET SESSION time_zone = '+08:00';
ALTER TABLE payments ADD COLUMN receipt_snapshot JSON NULL;
DELIMITER $$

DROP TRIGGER bi_payments$$
CREATE TRIGGER bi_payments BEFORE INSERT ON payments
FOR EACH ROW
BEGIN
    IF NEW.receipt_snapshot IS NULL OR JSON_TYPE(NEW.receipt_snapshot) <> 'OBJECT'
        OR NOT JSON_CONTAINS_PATH(NEW.receipt_snapshot, 'all', '$.studentNumber', '$.studentName',
            '$.officeCode', '$.officeName', '$.termCode', '$.termName', '$.reason',
            '$.assessedAmount', '$.paidAmount', '$.balanceAfter', '$.receiverUsername', '$.receiverName') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'An archived receipt snapshot is required for each new payment';
    END IF;
    SET NEW.created_at = CURRENT_TIMESTAMP(6);
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
    SET NEW.paid_at = CURRENT_TIMESTAMP(6);
    IF NEW.voided_at IS NOT NULL THEN
        SET NEW.voided_at = CURRENT_TIMESTAMP(6);
    END IF;
END$$

DROP TRIGGER bu_payments$$
CREATE TRIGGER bu_payments BEFORE UPDATE ON payments
FOR EACH ROW
BEGIN
    IF NOT (NEW.receipt_snapshot <=> OLD.receipt_snapshot) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'The original receipt snapshot is immutable';
    END IF;
    IF NOT (NEW.id <=> OLD.id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    IF NOT (NEW.fine_id <=> OLD.fine_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    IF NOT (NEW.amount <=> OLD.amount) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    IF NOT (NEW.receipt_reference <=> OLD.receipt_reference) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    IF NOT (NEW.received_by <=> OLD.received_by) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    IF NOT (NEW.paid_at <=> OLD.paid_at) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    IF OLD.voided_at IS NOT NULL OR NEW.voided_at IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A payment can only be updated once to record a void';
    END IF;
    SET NEW.created_at = OLD.created_at;
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
    IF NEW.voided_at IS NOT NULL AND NOT (NEW.voided_at <=> OLD.voided_at) THEN
        SET NEW.voided_at = CURRENT_TIMESTAMP(6);
    END IF;
END$$

DROP TRIGGER ai_payments$$
CREATE TRIGGER ai_payments AFTER INSERT ON payments
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'INSERT', 'payments', CAST(NEW.id AS CHAR),
        NULL,
        JSON_OBJECT(
            'id', NEW.id,
            'fine_id', NEW.fine_id,
            'amount', NEW.amount,
            'receipt_reference', NEW.receipt_reference,
            'receipt_snapshot', NEW.receipt_snapshot,
            'received_by', NEW.received_by,
            'paid_at', NEW.paid_at,
            'voided_at', NEW.voided_at,
            'voided_by', NEW.voided_by,
            'void_reason', NEW.void_reason,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

DROP TRIGGER au_payments$$
CREATE TRIGGER au_payments AFTER UPDATE ON payments
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'UPDATE', 'payments', CAST(NEW.id AS CHAR),
        JSON_OBJECT(
            'id', OLD.id,
            'fine_id', OLD.fine_id,
            'amount', OLD.amount,
            'receipt_reference', OLD.receipt_reference,
            'receipt_snapshot', OLD.receipt_snapshot,
            'received_by', OLD.received_by,
            'paid_at', OLD.paid_at,
            'voided_at', OLD.voided_at,
            'voided_by', OLD.voided_by,
            'void_reason', OLD.void_reason,
            'created_at', OLD.created_at,
            'updated_at', OLD.updated_at),
        JSON_OBJECT(
            'id', NEW.id,
            'fine_id', NEW.fine_id,
            'amount', NEW.amount,
            'receipt_reference', NEW.receipt_reference,
            'receipt_snapshot', NEW.receipt_snapshot,
            'received_by', NEW.received_by,
            'paid_at', NEW.paid_at,
            'voided_at', NEW.voided_at,
            'voided_by', NEW.voided_by,
            'void_reason', NEW.void_reason,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

DELIMITER ;
