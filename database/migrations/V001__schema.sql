-- Queue-Clearance / MySQL 8.4 ONLY / execute once against an empty database.
-- Forward-only baseline. MySQL DDL implicitly commits: restore a DBA backup on
-- failed installation; do not rerun a partly applied baseline or drop live data.
CREATE DATABASE queue_clearance CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE queue_clearance;
SET SESSION time_zone = '+08:00';
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

CREATE TABLE organizations (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    name VARCHAR(120) NOT NULL,
    queue_open BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id), UNIQUE KEY uq_organizations_code (code),
    CONSTRAINT ck_organizations_code CHECK (code IN ('LSG','PSITS','PHG','LIBRARY','CCSO','CIT','ESO')),
    CONSTRAINT ck_organizations_flags CHECK (queue_open IN (0,1) AND is_active IN (0,1)),
    CONSTRAINT ck_organizations_name CHECK (CHAR_LENGTH(TRIM(name)) > 0)
) ENGINE=InnoDB;

CREATE TABLE students (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    student_number VARCHAR(32) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    middle_name VARCHAR(80) NULL,
    last_name VARCHAR(80) NOT NULL,
    suffix VARCHAR(16) NULL,
    program VARCHAR(80) NOT NULL,
    year_level TINYINT UNSIGNED NOT NULL,
    section VARCHAR(32) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id), UNIQUE KEY uq_students_number (student_number),
    KEY idx_students_filters (is_active, program, year_level, section),
    KEY idx_students_name (last_name, first_name),
    CONSTRAINT ck_students_year CHECK (year_level BETWEEN 1 AND 6),
    CONSTRAINT ck_students_active CHECK (is_active IN (0,1)),
    CONSTRAINT ck_students_required CHECK (CHAR_LENGTH(TRIM(student_number)) > 0
        AND CHAR_LENGTH(TRIM(first_name)) > 0 AND CHAR_LENGTH(TRIM(last_name)) > 0
        AND CHAR_LENGTH(TRIM(program)) > 0 AND CHAR_LENGTH(TRIM(section)) > 0)
) ENGINE=InnoDB;

CREATE TABLE terms (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(120) NOT NULL,
    starts_on DATE NOT NULL,
    ends_on DATE NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    current_slot TINYINT GENERATED ALWAYS AS (CASE WHEN is_current = 1 THEN 1 ELSE NULL END) STORED,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id), UNIQUE KEY uq_terms_code (code), UNIQUE KEY uq_terms_current (current_slot),
    CONSTRAINT ck_terms_dates CHECK (ends_on >= starts_on),
    CONSTRAINT ck_terms_flags CHECK (is_current IN (0,1) AND is_active IN (0,1)
        AND (is_current = 0 OR is_active = 1)),
    CONSTRAINT ck_terms_names CHECK (CHAR_LENGTH(TRIM(code)) > 0 AND CHAR_LENGTH(TRIM(name)) > 0)
) ENGINE=InnoDB;

CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(60) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    display_name VARCHAR(160) NOT NULL,
    role ENUM('admin','officer','cashier','student') NOT NULL,
    student_id BIGINT UNSIGNED NULL,
    org_id BIGINT UNSIGNED NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id), UNIQUE KEY uq_users_username (username), UNIQUE KEY uq_users_student (student_id),
    KEY idx_users_org_role (org_id, role, is_active),
    CONSTRAINT fk_users_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_users_org FOREIGN KEY (org_id) REFERENCES organizations(id),
    CONSTRAINT ck_users_assignment CHECK (
        (role = 'student' AND student_id IS NOT NULL AND org_id IS NULL) OR
        (role = 'officer' AND org_id IS NOT NULL AND student_id IS NULL) OR
        (role IN ('admin','cashier') AND student_id IS NULL AND org_id IS NULL)),
    CONSTRAINT ck_users_flags CHECK (is_active IN (0,1) AND must_change_password IN (0,1)),
    CONSTRAINT ck_users_names CHECK (CHAR_LENGTH(TRIM(username)) > 0 AND CHAR_LENGTH(TRIM(display_name)) > 0),
    CONSTRAINT ck_users_bcrypt CHECK (CHAR_LENGTH(password_hash) = 60
        AND LEFT(password_hash, 4) = '$2a$'
        AND SUBSTRING(password_hash, 5, 2) BETWEEN '04' AND '16'
        AND SUBSTRING(password_hash, 7, 1) = '$')
) ENGINE=InnoDB;

CREATE TABLE fines (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    student_id BIGINT UNSIGNED NOT NULL,
    org_id BIGINT UNSIGNED NOT NULL,
    term_id BIGINT UNSIGNED NOT NULL,
    reason VARCHAR(500) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    assessed_by BIGINT UNSIGNED NOT NULL,
    waived_at DATETIME(6) NULL,
    waived_by BIGINT UNSIGNED NULL,
    waiver_reason VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id), KEY idx_fines_student_office_term (student_id, org_id, term_id),
    KEY idx_fines_term_office (term_id, org_id, created_at),
    KEY idx_fines_assessed_by (assessed_by), KEY idx_fines_waived_by (waived_by),
    CONSTRAINT fk_fines_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_fines_org FOREIGN KEY (org_id) REFERENCES organizations(id),
    CONSTRAINT fk_fines_term FOREIGN KEY (term_id) REFERENCES terms(id),
    CONSTRAINT fk_fines_assessor FOREIGN KEY (assessed_by) REFERENCES users(id),
    CONSTRAINT fk_fines_waiver_user FOREIGN KEY (waived_by) REFERENCES users(id),
    CONSTRAINT ck_fines_amount CHECK (amount > 0),
    CONSTRAINT ck_fines_reason CHECK (CHAR_LENGTH(TRIM(reason)) > 0),
    CONSTRAINT ck_fines_waiver CHECK (
        (waived_at IS NULL AND waived_by IS NULL AND waiver_reason IS NULL) OR
        (waived_at IS NOT NULL AND waived_by IS NOT NULL AND waiver_reason IS NOT NULL
            AND CHAR_LENGTH(TRIM(waiver_reason)) > 0))
) ENGINE=InnoDB;

CREATE TABLE payments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    fine_id BIGINT UNSIGNED NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    receipt_reference VARCHAR(64) NOT NULL,
    received_by BIGINT UNSIGNED NOT NULL,
    paid_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    voided_at DATETIME(6) NULL,
    voided_by BIGINT UNSIGNED NULL,
    void_reason VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id), UNIQUE KEY uq_payments_receipt (receipt_reference),
    KEY idx_payments_fine_valid (fine_id, voided_at), KEY idx_payments_date (paid_at, id),
    KEY idx_payments_receiver (received_by, paid_at), KEY idx_payments_voided_by (voided_by),
    CONSTRAINT fk_payments_fine FOREIGN KEY (fine_id) REFERENCES fines(id),
    CONSTRAINT fk_payments_receiver FOREIGN KEY (received_by) REFERENCES users(id),
    CONSTRAINT fk_payments_voider FOREIGN KEY (voided_by) REFERENCES users(id),
    CONSTRAINT ck_payments_amount CHECK (amount > 0),
    CONSTRAINT ck_payments_receipt CHECK (CHAR_LENGTH(TRIM(receipt_reference)) > 0),
    CONSTRAINT ck_payments_void CHECK (
        (voided_at IS NULL AND voided_by IS NULL AND void_reason IS NULL) OR
        (voided_at IS NOT NULL AND voided_by IS NOT NULL AND void_reason IS NOT NULL
            AND CHAR_LENGTH(TRIM(void_reason)) > 0 AND voided_at >= paid_at))
) ENGINE=InnoDB;

CREATE TABLE queue_counters (
    org_id BIGINT UNSIGNED NOT NULL,
    queue_date DATE NOT NULL,
    last_seq INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (org_id, queue_date),
    CONSTRAINT fk_queue_counters_org FOREIGN KEY (org_id) REFERENCES organizations(id),
    CONSTRAINT ck_queue_counters_seq CHECK (last_seq <= 99999999)
) ENGINE=InnoDB;

CREATE TABLE queue_tickets (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    student_id BIGINT UNSIGNED NOT NULL,
    org_id BIGINT UNSIGNED NOT NULL,
    term_id BIGINT UNSIGNED NOT NULL,
    queue_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    seq INT UNSIGNED NOT NULL,
    status ENUM('waiting','called','serving','deferred','completed','cancelled') NOT NULL DEFAULT 'waiting',
    sort_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    defer_count INT UNSIGNED NOT NULL DEFAULT 0,
    issued_by BIGINT UNSIGNED NOT NULL,
    served_by BIGINT UNSIGNED NULL,
    called_at DATETIME(6) NULL,
    serving_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    cancelled_at DATETIME(6) NULL,
    cancellation_reason VARCHAR(500) NULL,
    active_student_id BIGINT UNSIGNED GENERATED ALWAYS AS (
        CASE WHEN status IN ('waiting','called','serving','deferred') THEN student_id ELSE NULL END) STORED,
    occupied_org_id BIGINT UNSIGNED GENERATED ALWAYS AS (
        CASE WHEN status IN ('called','serving') THEN org_id ELSE NULL END) STORED,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id), UNIQUE KEY uq_queue_number (org_id, queue_date, seq),
    UNIQUE KEY uq_queue_active_student (org_id, queue_date, active_student_id),
    UNIQUE KEY uq_queue_occupied_office (occupied_org_id),
    KEY idx_queue_next (org_id, queue_date, status, sort_at, id),
    KEY idx_queue_student_alert (student_id, queue_date, status, called_at, id),
    KEY idx_queue_report (term_id, org_id, queue_date, status),
    KEY idx_queue_issuer (issued_by), KEY idx_queue_server (served_by),
    CONSTRAINT fk_queue_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_queue_org FOREIGN KEY (org_id) REFERENCES organizations(id),
    CONSTRAINT fk_queue_term FOREIGN KEY (term_id) REFERENCES terms(id),
    CONSTRAINT fk_queue_counter FOREIGN KEY (org_id, queue_date) REFERENCES queue_counters(org_id, queue_date),
    CONSTRAINT fk_queue_issuer FOREIGN KEY (issued_by) REFERENCES users(id),
    CONSTRAINT fk_queue_server FOREIGN KEY (served_by) REFERENCES users(id),
    CONSTRAINT ck_queue_seq CHECK (seq BETWEEN 1 AND 99999999),
    CONSTRAINT ck_queue_assigned CHECK (status NOT IN ('called','serving','completed') OR served_by IS NOT NULL),
    CONSTRAINT ck_queue_called CHECK (status NOT IN ('called','serving','completed') OR called_at IS NOT NULL),
    CONSTRAINT ck_queue_serving CHECK (status NOT IN ('serving','completed') OR serving_at IS NOT NULL),
    CONSTRAINT ck_queue_completed CHECK ((status = 'completed') = (completed_at IS NOT NULL)),
    CONSTRAINT ck_queue_cancelled CHECK (
        (status = 'cancelled' AND cancelled_at IS NOT NULL AND cancellation_reason IS NOT NULL
            AND CHAR_LENGTH(TRIM(cancellation_reason)) > 0) OR
        (status <> 'cancelled' AND cancelled_at IS NULL AND cancellation_reason IS NULL))
) ENGINE=InnoDB;

CREATE TABLE clearance_records (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    student_id BIGINT UNSIGNED NOT NULL,
    org_id BIGINT UNSIGNED NOT NULL,
    term_id BIGINT UNSIGNED NOT NULL,
    status ENUM('pending','cleared','on_hold') NOT NULL DEFAULT 'pending',
    remarks VARCHAR(500) NULL,
    cleared_by BIGINT UNSIGNED NULL,
    cleared_at DATETIME(6) NULL,
    balance_exception BOOLEAN NOT NULL DEFAULT FALSE,
    exception_reason VARCHAR(500) NULL,
    exception_by BIGINT UNSIGNED NULL,
    exception_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id), UNIQUE KEY uq_clearance_student_office_term (student_id, org_id, term_id),
    KEY idx_clearance_term_office_status (term_id, org_id, status),
    KEY idx_clearance_clearer (cleared_by), KEY idx_clearance_exception_user (exception_by),
    CONSTRAINT fk_clearance_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_clearance_org FOREIGN KEY (org_id) REFERENCES organizations(id),
    CONSTRAINT fk_clearance_term FOREIGN KEY (term_id) REFERENCES terms(id),
    CONSTRAINT fk_clearance_user FOREIGN KEY (cleared_by) REFERENCES users(id),
    CONSTRAINT fk_clearance_exception_user FOREIGN KEY (exception_by) REFERENCES users(id),
    CONSTRAINT ck_clearance_cleared CHECK (
        (status = 'cleared' AND cleared_by IS NOT NULL AND cleared_at IS NOT NULL) OR
        (status <> 'cleared' AND cleared_by IS NULL AND cleared_at IS NULL)),
    CONSTRAINT ck_clearance_exception CHECK (
        (balance_exception = 0 AND exception_reason IS NULL AND exception_by IS NULL AND exception_at IS NULL) OR
        (balance_exception = 1 AND status = 'cleared' AND exception_reason IS NOT NULL
            AND CHAR_LENGTH(TRIM(exception_reason)) > 0 AND exception_by IS NOT NULL AND exception_at IS NOT NULL)),
    CONSTRAINT ck_clearance_hold CHECK (status <> 'on_hold' OR (remarks IS NOT NULL AND CHAR_LENGTH(TRIM(remarks)) > 0))
) ENGINE=InnoDB;

CREATE TABLE audit_logs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    actor_user_id BIGINT UNSIGNED NULL,
    database_account VARCHAR(288) NOT NULL,
    connection_id BIGINT UNSIGNED NOT NULL,
    request_id VARCHAR(64) NULL,
    action ENUM('INSERT','UPDATE') NOT NULL,
    table_name VARCHAR(64) NOT NULL,
    record_key VARCHAR(100) NOT NULL,
    old_values JSON NULL,
    new_values JSON NOT NULL,
    occurred_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id), KEY idx_audit_time (occurred_at, id),
    KEY idx_audit_actor (actor_user_id, occurred_at),
    KEY idx_audit_record (table_name, record_key, occurred_at),
    KEY idx_audit_request (request_id),
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- Balances/status are projections of the ledger, never mutable stored totals.
CREATE SQL SECURITY INVOKER VIEW v_fine_balances AS
SELECT f.id AS fine_id, f.student_id, f.org_id, f.term_id, f.reason, f.amount,
    COALESCE(p.paid_amount, 0.00) AS paid_amount,
    CASE WHEN f.waived_at IS NOT NULL THEN 0.00
        ELSE f.amount - COALESCE(p.paid_amount, 0.00) END AS balance,
    CASE WHEN f.waived_at IS NOT NULL THEN 'waived'
        WHEN COALESCE(p.paid_amount, 0.00) = 0 THEN 'unpaid'
        WHEN p.paid_amount < f.amount THEN 'partial' ELSE 'paid' END AS status,
    f.waived_at, f.waived_by, f.waiver_reason, f.created_at
FROM fines f LEFT JOIN (
    SELECT fine_id, SUM(amount) AS paid_amount FROM payments WHERE voided_at IS NULL GROUP BY fine_id
) p ON p.fine_id = f.id;

CREATE SQL SECURITY INVOKER VIEW v_office_balances AS
SELECT student_id, org_id, term_id, SUM(amount) AS assessed_amount,
    SUM(paid_amount) AS paid_amount, SUM(balance) AS balance
FROM v_fine_balances GROUP BY student_id, org_id, term_id;

CREATE SQL SECURITY INVOKER VIEW v_queue_board AS
SELECT q.id, q.org_id, o.code AS office_code, q.queue_date, q.seq,
    CONCAT(o.code, '-', CASE WHEN q.seq < 1000 THEN LPAD(q.seq, 3, '0') ELSE CAST(q.seq AS CHAR) END) AS ticket_number,
    q.status, q.called_at, q.serving_at, q.served_by
FROM queue_tickets q JOIN organizations o ON o.id = q.org_id
WHERE q.status IN ('called','serving') AND q.queue_date = CURRENT_DATE;
