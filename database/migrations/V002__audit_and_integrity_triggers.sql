-- MySQL 8.4. Apply once after V001 and before the seed script.
-- Triggers run with installation-definer rights; keep that account available.
-- All domain writes and their audit rows commit or roll back together.
USE queue_clearance;
SET SESSION time_zone = '+08:00';
DELIMITER $$

CREATE TRIGGER bi_organizations BEFORE INSERT ON organizations
FOR EACH ROW
BEGIN
    SET NEW.created_at = CURRENT_TIMESTAMP(6);
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
END$$

CREATE TRIGGER bu_organizations BEFORE UPDATE ON organizations
FOR EACH ROW
BEGIN
    IF NOT (NEW.code <=> OLD.code) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record assignment and office codes are immutable';
    END IF;
    IF NOT (NEW.id <=> OLD.id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    SET NEW.created_at = OLD.created_at;
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
END$$

CREATE TRIGGER ai_organizations AFTER INSERT ON organizations
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'INSERT', 'organizations', CAST(NEW.id AS CHAR),
        NULL,
        JSON_OBJECT(
            'id', NEW.id,
            'code', NEW.code,
            'name', NEW.name,
            'queue_open', NEW.queue_open,
            'is_active', NEW.is_active,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

CREATE TRIGGER au_organizations AFTER UPDATE ON organizations
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'UPDATE', 'organizations', CAST(NEW.id AS CHAR),
        JSON_OBJECT(
            'id', OLD.id,
            'code', OLD.code,
            'name', OLD.name,
            'queue_open', OLD.queue_open,
            'is_active', OLD.is_active,
            'created_at', OLD.created_at,
            'updated_at', OLD.updated_at),
        JSON_OBJECT(
            'id', NEW.id,
            'code', NEW.code,
            'name', NEW.name,
            'queue_open', NEW.queue_open,
            'is_active', NEW.is_active,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

CREATE TRIGGER bd_organizations BEFORE DELETE ON organizations
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Deletion is prohibited; deactivate, cancel, waive or void instead';
END$$

CREATE TRIGGER bi_students BEFORE INSERT ON students
FOR EACH ROW
BEGIN
    SET NEW.created_at = CURRENT_TIMESTAMP(6);
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
END$$

CREATE TRIGGER bu_students BEFORE UPDATE ON students
FOR EACH ROW
BEGIN
    IF NOT (NEW.id <=> OLD.id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    SET NEW.created_at = OLD.created_at;
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
END$$

CREATE TRIGGER ai_students AFTER INSERT ON students
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'INSERT', 'students', CAST(NEW.id AS CHAR),
        NULL,
        JSON_OBJECT(
            'id', NEW.id,
            'student_number', NEW.student_number,
            'first_name', NEW.first_name,
            'middle_name', NEW.middle_name,
            'last_name', NEW.last_name,
            'suffix', NEW.suffix,
            'program', NEW.program,
            'year_level', NEW.year_level,
            'section', NEW.section,
            'is_active', NEW.is_active,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

CREATE TRIGGER au_students AFTER UPDATE ON students
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'UPDATE', 'students', CAST(NEW.id AS CHAR),
        JSON_OBJECT(
            'id', OLD.id,
            'student_number', OLD.student_number,
            'first_name', OLD.first_name,
            'middle_name', OLD.middle_name,
            'last_name', OLD.last_name,
            'suffix', OLD.suffix,
            'program', OLD.program,
            'year_level', OLD.year_level,
            'section', OLD.section,
            'is_active', OLD.is_active,
            'created_at', OLD.created_at,
            'updated_at', OLD.updated_at),
        JSON_OBJECT(
            'id', NEW.id,
            'student_number', NEW.student_number,
            'first_name', NEW.first_name,
            'middle_name', NEW.middle_name,
            'last_name', NEW.last_name,
            'suffix', NEW.suffix,
            'program', NEW.program,
            'year_level', NEW.year_level,
            'section', NEW.section,
            'is_active', NEW.is_active,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

CREATE TRIGGER bd_students BEFORE DELETE ON students
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Deletion is prohibited; deactivate, cancel, waive or void instead';
END$$

CREATE TRIGGER bi_terms BEFORE INSERT ON terms
FOR EACH ROW
BEGIN
    SET NEW.created_at = CURRENT_TIMESTAMP(6);
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
END$$

CREATE TRIGGER bu_terms BEFORE UPDATE ON terms
FOR EACH ROW
BEGIN
    IF NOT (NEW.id <=> OLD.id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    SET NEW.created_at = OLD.created_at;
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
END$$

CREATE TRIGGER ai_terms AFTER INSERT ON terms
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'INSERT', 'terms', CAST(NEW.id AS CHAR),
        NULL,
        JSON_OBJECT(
            'id', NEW.id,
            'code', NEW.code,
            'name', NEW.name,
            'starts_on', NEW.starts_on,
            'ends_on', NEW.ends_on,
            'is_current', NEW.is_current,
            'is_active', NEW.is_active,
            'current_slot', NEW.current_slot,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

CREATE TRIGGER au_terms AFTER UPDATE ON terms
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'UPDATE', 'terms', CAST(NEW.id AS CHAR),
        JSON_OBJECT(
            'id', OLD.id,
            'code', OLD.code,
            'name', OLD.name,
            'starts_on', OLD.starts_on,
            'ends_on', OLD.ends_on,
            'is_current', OLD.is_current,
            'is_active', OLD.is_active,
            'current_slot', OLD.current_slot,
            'created_at', OLD.created_at,
            'updated_at', OLD.updated_at),
        JSON_OBJECT(
            'id', NEW.id,
            'code', NEW.code,
            'name', NEW.name,
            'starts_on', NEW.starts_on,
            'ends_on', NEW.ends_on,
            'is_current', NEW.is_current,
            'is_active', NEW.is_active,
            'current_slot', NEW.current_slot,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

CREATE TRIGGER bd_terms BEFORE DELETE ON terms
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Deletion is prohibited; deactivate, cancel, waive or void instead';
END$$

CREATE TRIGGER bi_users BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    SET NEW.created_at = CURRENT_TIMESTAMP(6);
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
    IF NEW.last_login_at IS NOT NULL THEN
        SET NEW.last_login_at = CURRENT_TIMESTAMP(6);
    END IF;
END$$

CREATE TRIGGER bu_users BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    IF NOT (NEW.id <=> OLD.id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    SET NEW.created_at = OLD.created_at;
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
    IF NEW.last_login_at IS NOT NULL AND NOT (NEW.last_login_at <=> OLD.last_login_at) THEN
        SET NEW.last_login_at = CURRENT_TIMESTAMP(6);
    END IF;
END$$

CREATE TRIGGER ai_users AFTER INSERT ON users
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'INSERT', 'users', CAST(NEW.id AS CHAR),
        NULL,
        JSON_OBJECT(
            'id', NEW.id,
            'username', NEW.username,
            'display_name', NEW.display_name,
            'role', NEW.role,
            'student_id', NEW.student_id,
            'org_id', NEW.org_id,
            'is_active', NEW.is_active,
            'must_change_password', NEW.must_change_password,
            'last_login_at', NEW.last_login_at,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at,
            'password_changed', TRUE));
END$$

CREATE TRIGGER au_users AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'UPDATE', 'users', CAST(NEW.id AS CHAR),
        JSON_OBJECT(
            'id', OLD.id,
            'username', OLD.username,
            'display_name', OLD.display_name,
            'role', OLD.role,
            'student_id', OLD.student_id,
            'org_id', OLD.org_id,
            'is_active', OLD.is_active,
            'must_change_password', OLD.must_change_password,
            'last_login_at', OLD.last_login_at,
            'created_at', OLD.created_at,
            'updated_at', OLD.updated_at,
            'password_changed', NOT (NEW.password_hash <=> OLD.password_hash)),
        JSON_OBJECT(
            'id', NEW.id,
            'username', NEW.username,
            'display_name', NEW.display_name,
            'role', NEW.role,
            'student_id', NEW.student_id,
            'org_id', NEW.org_id,
            'is_active', NEW.is_active,
            'must_change_password', NEW.must_change_password,
            'last_login_at', NEW.last_login_at,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at,
            'password_changed', NOT (NEW.password_hash <=> OLD.password_hash)));
END$$

CREATE TRIGGER bd_users BEFORE DELETE ON users
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Deletion is prohibited; deactivate, cancel, waive or void instead';
END$$

CREATE TRIGGER bi_fines BEFORE INSERT ON fines
FOR EACH ROW
BEGIN
    SET NEW.created_at = CURRENT_TIMESTAMP(6);
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
    IF NEW.waived_at IS NOT NULL THEN
        SET NEW.waived_at = CURRENT_TIMESTAMP(6);
    END IF;
END$$

CREATE TRIGGER bu_fines BEFORE UPDATE ON fines
FOR EACH ROW
BEGIN
    IF NOT (NEW.student_id <=> OLD.student_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record assignment and office codes are immutable';
    END IF;
    IF NOT (NEW.org_id <=> OLD.org_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record assignment and office codes are immutable';
    END IF;
    IF NOT (NEW.term_id <=> OLD.term_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record assignment and office codes are immutable';
    END IF;
    IF NOT (NEW.assessed_by <=> OLD.assessed_by) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record assignment and office codes are immutable';
    END IF;
    IF NOT (NEW.id <=> OLD.id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    SET NEW.created_at = OLD.created_at;
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
    IF NEW.waived_at IS NOT NULL AND NOT (NEW.waived_at <=> OLD.waived_at) THEN
        SET NEW.waived_at = CURRENT_TIMESTAMP(6);
    END IF;
END$$

CREATE TRIGGER ai_fines AFTER INSERT ON fines
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'INSERT', 'fines', CAST(NEW.id AS CHAR),
        NULL,
        JSON_OBJECT(
            'id', NEW.id,
            'student_id', NEW.student_id,
            'org_id', NEW.org_id,
            'term_id', NEW.term_id,
            'reason', NEW.reason,
            'amount', NEW.amount,
            'assessed_by', NEW.assessed_by,
            'waived_at', NEW.waived_at,
            'waived_by', NEW.waived_by,
            'waiver_reason', NEW.waiver_reason,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

CREATE TRIGGER au_fines AFTER UPDATE ON fines
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'UPDATE', 'fines', CAST(NEW.id AS CHAR),
        JSON_OBJECT(
            'id', OLD.id,
            'student_id', OLD.student_id,
            'org_id', OLD.org_id,
            'term_id', OLD.term_id,
            'reason', OLD.reason,
            'amount', OLD.amount,
            'assessed_by', OLD.assessed_by,
            'waived_at', OLD.waived_at,
            'waived_by', OLD.waived_by,
            'waiver_reason', OLD.waiver_reason,
            'created_at', OLD.created_at,
            'updated_at', OLD.updated_at),
        JSON_OBJECT(
            'id', NEW.id,
            'student_id', NEW.student_id,
            'org_id', NEW.org_id,
            'term_id', NEW.term_id,
            'reason', NEW.reason,
            'amount', NEW.amount,
            'assessed_by', NEW.assessed_by,
            'waived_at', NEW.waived_at,
            'waived_by', NEW.waived_by,
            'waiver_reason', NEW.waiver_reason,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

CREATE TRIGGER bd_fines BEFORE DELETE ON fines
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Deletion is prohibited; deactivate, cancel, waive or void instead';
END$$

CREATE TRIGGER bi_payments BEFORE INSERT ON payments
FOR EACH ROW
BEGIN
    SET NEW.created_at = CURRENT_TIMESTAMP(6);
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
    SET NEW.paid_at = CURRENT_TIMESTAMP(6);
    IF NEW.voided_at IS NOT NULL THEN
        SET NEW.voided_at = CURRENT_TIMESTAMP(6);
    END IF;
END$$

CREATE TRIGGER bu_payments BEFORE UPDATE ON payments
FOR EACH ROW
BEGIN
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
            'received_by', NEW.received_by,
            'paid_at', NEW.paid_at,
            'voided_at', NEW.voided_at,
            'voided_by', NEW.voided_by,
            'void_reason', NEW.void_reason,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

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
            'received_by', NEW.received_by,
            'paid_at', NEW.paid_at,
            'voided_at', NEW.voided_at,
            'voided_by', NEW.voided_by,
            'void_reason', NEW.void_reason,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

CREATE TRIGGER bd_payments BEFORE DELETE ON payments
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Deletion is prohibited; deactivate, cancel, waive or void instead';
END$$

CREATE TRIGGER bi_queue_counters BEFORE INSERT ON queue_counters
FOR EACH ROW
BEGIN
    SET NEW.created_at = CURRENT_TIMESTAMP(6);
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
    SET NEW.queue_date = CURRENT_DATE;
END$$

CREATE TRIGGER bu_queue_counters BEFORE UPDATE ON queue_counters
FOR EACH ROW
BEGIN
    IF NOT (NEW.org_id <=> OLD.org_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    IF NOT (NEW.queue_date <=> OLD.queue_date) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    SET NEW.created_at = OLD.created_at;
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
END$$

CREATE TRIGGER ai_queue_counters AFTER INSERT ON queue_counters
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'INSERT', 'queue_counters', CONCAT(NEW.org_id, ':', DATE_FORMAT(NEW.queue_date, '%Y-%m-%d')),
        NULL,
        JSON_OBJECT(
            'org_id', NEW.org_id,
            'queue_date', NEW.queue_date,
            'last_seq', NEW.last_seq,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

CREATE TRIGGER au_queue_counters AFTER UPDATE ON queue_counters
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'UPDATE', 'queue_counters', CONCAT(NEW.org_id, ':', DATE_FORMAT(NEW.queue_date, '%Y-%m-%d')),
        JSON_OBJECT(
            'org_id', OLD.org_id,
            'queue_date', OLD.queue_date,
            'last_seq', OLD.last_seq,
            'created_at', OLD.created_at,
            'updated_at', OLD.updated_at),
        JSON_OBJECT(
            'org_id', NEW.org_id,
            'queue_date', NEW.queue_date,
            'last_seq', NEW.last_seq,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

CREATE TRIGGER bd_queue_counters BEFORE DELETE ON queue_counters
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Deletion is prohibited; deactivate, cancel, waive or void instead';
END$$

CREATE TRIGGER bi_queue_tickets BEFORE INSERT ON queue_tickets
FOR EACH ROW
BEGIN
    SET NEW.created_at = CURRENT_TIMESTAMP(6);
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
    SET NEW.queue_date = CURRENT_DATE;
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

CREATE TRIGGER bu_queue_tickets BEFORE UPDATE ON queue_tickets
FOR EACH ROW
BEGIN
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

CREATE TRIGGER au_queue_tickets AFTER UPDATE ON queue_tickets
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'UPDATE', 'queue_tickets', CAST(NEW.id AS CHAR),
        JSON_OBJECT(
            'id', OLD.id,
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

CREATE TRIGGER bd_queue_tickets BEFORE DELETE ON queue_tickets
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Deletion is prohibited; deactivate, cancel, waive or void instead';
END$$

CREATE TRIGGER bi_clearance_records BEFORE INSERT ON clearance_records
FOR EACH ROW
BEGIN
    SET NEW.created_at = CURRENT_TIMESTAMP(6);
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
    IF NEW.cleared_at IS NOT NULL THEN
        SET NEW.cleared_at = CURRENT_TIMESTAMP(6);
    END IF;
    IF NEW.exception_at IS NOT NULL THEN
        SET NEW.exception_at = CURRENT_TIMESTAMP(6);
    END IF;
END$$

CREATE TRIGGER bu_clearance_records BEFORE UPDATE ON clearance_records
FOR EACH ROW
BEGIN
    IF NOT (NEW.student_id <=> OLD.student_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record assignment and office codes are immutable';
    END IF;
    IF NOT (NEW.org_id <=> OLD.org_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record assignment and office codes are immutable';
    END IF;
    IF NOT (NEW.term_id <=> OLD.term_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record assignment and office codes are immutable';
    END IF;
    IF NOT (NEW.id <=> OLD.id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Record identity and original payment details are immutable';
    END IF;
    SET NEW.created_at = OLD.created_at;
    SET NEW.updated_at = CURRENT_TIMESTAMP(6);
    IF NEW.cleared_at IS NOT NULL AND NOT (NEW.cleared_at <=> OLD.cleared_at) THEN
        SET NEW.cleared_at = CURRENT_TIMESTAMP(6);
    END IF;
    IF NEW.exception_at IS NOT NULL AND NOT (NEW.exception_at <=> OLD.exception_at) THEN
        SET NEW.exception_at = CURRENT_TIMESTAMP(6);
    END IF;
END$$

CREATE TRIGGER ai_clearance_records AFTER INSERT ON clearance_records
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'INSERT', 'clearance_records', CAST(NEW.id AS CHAR),
        NULL,
        JSON_OBJECT(
            'id', NEW.id,
            'student_id', NEW.student_id,
            'org_id', NEW.org_id,
            'term_id', NEW.term_id,
            'status', NEW.status,
            'remarks', NEW.remarks,
            'cleared_by', NEW.cleared_by,
            'cleared_at', NEW.cleared_at,
            'balance_exception', NEW.balance_exception,
            'exception_reason', NEW.exception_reason,
            'exception_by', NEW.exception_by,
            'exception_at', NEW.exception_at,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

CREATE TRIGGER au_clearance_records AFTER UPDATE ON clearance_records
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (actor_user_id, database_account, connection_id, request_id,
        action, table_name, record_key, old_values, new_values)
    VALUES (@audit_user_id, USER(), CONNECTION_ID(), LEFT(@audit_request_id, 64),
        'UPDATE', 'clearance_records', CAST(NEW.id AS CHAR),
        JSON_OBJECT(
            'id', OLD.id,
            'student_id', OLD.student_id,
            'org_id', OLD.org_id,
            'term_id', OLD.term_id,
            'status', OLD.status,
            'remarks', OLD.remarks,
            'cleared_by', OLD.cleared_by,
            'cleared_at', OLD.cleared_at,
            'balance_exception', OLD.balance_exception,
            'exception_reason', OLD.exception_reason,
            'exception_by', OLD.exception_by,
            'exception_at', OLD.exception_at,
            'created_at', OLD.created_at,
            'updated_at', OLD.updated_at),
        JSON_OBJECT(
            'id', NEW.id,
            'student_id', NEW.student_id,
            'org_id', NEW.org_id,
            'term_id', NEW.term_id,
            'status', NEW.status,
            'remarks', NEW.remarks,
            'cleared_by', NEW.cleared_by,
            'cleared_at', NEW.cleared_at,
            'balance_exception', NEW.balance_exception,
            'exception_reason', NEW.exception_reason,
            'exception_by', NEW.exception_by,
            'exception_at', NEW.exception_at,
            'created_at', NEW.created_at,
            'updated_at', NEW.updated_at));
END$$

CREATE TRIGGER bd_clearance_records BEFORE DELETE ON clearance_records
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Deletion is prohibited; deactivate, cancel, waive or void instead';
END$$

CREATE TRIGGER bu_audit_logs BEFORE UPDATE ON audit_logs
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Audit entries are append-only';
END$$

CREATE TRIGGER bd_audit_logs BEFORE DELETE ON audit_logs
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Audit entries are append-only';
END$$

DELIMITER ;
