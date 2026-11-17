-- DEVELOPMENT SAMPLE DATA. Never run on a campus deployment.
-- Run once as root AFTER 001__initial_data.sql and all migrations, on the same day
-- you want queue tickets to appear on (tickets must carry today's queue_date).
-- Every seeded account signs in with the password:  Password@2026
-- If it was already applied, the first student INSERT fails on the unique
-- student number and the whole transaction is rolled back by the client exit.
USE queue_clearance;
SET SESSION time_zone = '+08:00';
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

SET @dev_hash = '$2a$12$flk4prxB30grNLtgulYDs.hCRF0q3Gm/yaimpuFk9VxoLNCcFYt4y';
SET @audit_user_id = (SELECT id FROM users WHERE username = 'admin');
SET @audit_request_id = 'dev-sample-seed-v1';

START TRANSACTION;

-- ---------------------------------------------------------------- terms
UPDATE terms SET is_active = TRUE, is_current = TRUE WHERE code = '2026-2027-1';
INSERT INTO terms (code, name, starts_on, ends_on, is_current, is_active) VALUES
    ('2025-2026-2', 'Academic Year 2025-2026 - Second Semester', '2026-01-05', '2026-05-30', FALSE, FALSE);
SET @term    = (SELECT id FROM terms WHERE code = '2026-2027-1');
SET @oldterm = (SELECT id FROM terms WHERE code = '2025-2026-2');

-- ---------------------------------------------------------------- offices
UPDATE organizations SET queue_open = TRUE WHERE code IN ('LSG','PSITS','LIBRARY','CIT');
SET @lsg     = (SELECT id FROM organizations WHERE code = 'LSG');
SET @psits   = (SELECT id FROM organizations WHERE code = 'PSITS');
SET @phg     = (SELECT id FROM organizations WHERE code = 'PHG');
SET @library = (SELECT id FROM organizations WHERE code = 'LIBRARY');
SET @ccso    = (SELECT id FROM organizations WHERE code = 'CCSO');
SET @cit     = (SELECT id FROM organizations WHERE code = 'CIT');
SET @eso     = (SELECT id FROM organizations WHERE code = 'ESO');

-- ---------------------------------------------------------------- staff accounts
INSERT INTO users (username, password_hash, display_name, role, org_id, must_change_password) VALUES
    ('officer_lsg',     @dev_hash, 'Maria Santos',      'officer', @lsg,     FALSE),
    ('officer_psits',   @dev_hash, 'Jose Dela Cruz',    'officer', @psits,   FALSE),
    ('officer_phg',     @dev_hash, 'Ana Reyes',         'officer', @phg,     FALSE),
    ('officer_library', @dev_hash, 'Carlo Mendoza',     'officer', @library, FALSE),
    ('officer_ccso',    @dev_hash, 'Liza Garcia',       'officer', @ccso,    FALSE),
    ('officer_cit',     @dev_hash, 'Ramon Villanueva',  'officer', @cit,     FALSE),
    ('officer_eso',     @dev_hash, 'Grace Bautista',    'officer', @eso,     FALSE);
INSERT INTO users (username, password_hash, display_name, role, must_change_password) VALUES
    ('cashier1', @dev_hash, 'Paulo Ramos',  'cashier', FALSE),
    ('cashier2', @dev_hash, 'Irene Castro', 'cashier', FALSE);
SET @o_lsg     = (SELECT id FROM users WHERE username = 'officer_lsg');
SET @o_psits   = (SELECT id FROM users WHERE username = 'officer_psits');
SET @o_phg     = (SELECT id FROM users WHERE username = 'officer_phg');
SET @o_library = (SELECT id FROM users WHERE username = 'officer_library');
SET @o_ccso    = (SELECT id FROM users WHERE username = 'officer_ccso');
SET @o_cit     = (SELECT id FROM users WHERE username = 'officer_cit');
SET @o_eso     = (SELECT id FROM users WHERE username = 'officer_eso');
SET @cashier1  = (SELECT id FROM users WHERE username = 'cashier1');
SET @cashier2  = (SELECT id FROM users WHERE username = 'cashier2');

-- ---------------------------------------------------------------- students
INSERT INTO students (student_number, first_name, middle_name, last_name, suffix, program, year_level, section, is_active) VALUES
    ('2024-00001', 'Juan',      'Protacio', 'Dela Cruz',  NULL, 'BSIT', 3, 'A', TRUE),
    ('2024-00002', 'Maria',     'Clara',    'Santos',     NULL, 'BSIT', 3, 'A', TRUE),
    ('2024-00003', 'Andres',    NULL,       'Bonifacio',  NULL, 'BSCS', 2, 'B', TRUE),
    ('2024-00004', 'Gabriela',  'Cariño',   'Silang',     NULL, 'BSIS', 4, 'A', TRUE),
    ('2024-00005', 'Emilio',    'Famy',     'Aguinaldo',  'Jr.', 'BSIT', 1, 'C', TRUE),
    ('2024-00006', 'Melchora',  NULL,       'Aquino',     NULL, 'BSCS', 1, 'A', TRUE),
    ('2024-00007', 'Apolinario', NULL,      'Mabini',     NULL, 'BSIS', 2, 'B', TRUE),
    ('2024-00008', 'Marcelo',   'Hilario',  'Del Pilar',  NULL, 'BSIT', 4, 'B', TRUE),
    ('2024-00009', 'Gregoria',  NULL,       'De Jesus',   NULL, 'BSIT', 2, 'A', TRUE),
    ('2024-00010', 'Antonio',   NULL,       'Luna',       NULL, 'BSCS', 3, 'B', TRUE),
    ('2024-00011', 'Teresa',    'Lopez',    'Magbanua',   NULL, 'BSIS', 1, 'A', TRUE),
    ('2024-00012', 'Diego',     NULL,       'Silang',     NULL, 'BSIT', 1, 'B', TRUE),
    ('2024-00013', 'Trinidad',  NULL,       'Tecson',     NULL, 'BSCS', 4, 'A', TRUE),
    ('2024-00014', 'Francisco', 'Baltazar', 'Balagtas',   NULL, 'BSIT', 2, 'C', TRUE),
    ('2024-00015', 'Leona',     NULL,       'Florentino', NULL, 'BSIS', 3, 'A', TRUE),
    ('2024-00016', 'Graciano',  NULL,       'Lopez Jaena', NULL, 'BSCS', 2, 'A', TRUE),
    ('2024-00017', 'Josefa',    'Llanes',   'Escoda',     NULL, 'BSIT', 3, 'B', TRUE),
    ('2024-00018', 'Panday',    NULL,       'Pira',       NULL, 'BSIS', 4, 'B', TRUE),
    ('2024-00019', 'Macario',   NULL,       'Sakay',      NULL, 'BSIT', 4, 'A', TRUE),
    ('2023-00120', 'Vicente',   NULL,       'Lim',        NULL, 'BSCS', 4, 'B', FALSE);
SET @s1  = (SELECT id FROM students WHERE student_number = '2024-00001');
SET @s2  = (SELECT id FROM students WHERE student_number = '2024-00002');
SET @s3  = (SELECT id FROM students WHERE student_number = '2024-00003');
SET @s4  = (SELECT id FROM students WHERE student_number = '2024-00004');
SET @s5  = (SELECT id FROM students WHERE student_number = '2024-00005');
SET @s6  = (SELECT id FROM students WHERE student_number = '2024-00006');
SET @s7  = (SELECT id FROM students WHERE student_number = '2024-00007');
SET @s8  = (SELECT id FROM students WHERE student_number = '2024-00008');
SET @s9  = (SELECT id FROM students WHERE student_number = '2024-00009');
SET @s10 = (SELECT id FROM students WHERE student_number = '2024-00010');
SET @s11 = (SELECT id FROM students WHERE student_number = '2024-00011');
SET @s12 = (SELECT id FROM students WHERE student_number = '2024-00012');

-- Student sign-in accounts (role student). Username = "student" + last digit(s).
INSERT INTO users (username, password_hash, display_name, role, student_id, must_change_password) VALUES
    ('student1', @dev_hash, 'Juan Dela Cruz',    'student', @s1, FALSE),
    ('student2', @dev_hash, 'Maria Santos',      'student', @s2, FALSE),
    ('student3', @dev_hash, 'Andres Bonifacio',  'student', @s3, FALSE),
    ('student4', @dev_hash, 'Gabriela Silang',   'student', @s4, FALSE),
    ('student5', @dev_hash, 'Emilio Aguinaldo',  'student', @s5, FALSE);

-- ---------------------------------------------------------------- fines
INSERT INTO fines (student_id, org_id, term_id, reason, amount, assessed_by) VALUES
    (@s1, @lsg,     @term, 'Absent during General Assembly',          50.00,  @o_lsg),
    (@s1, @library, @term, 'Overdue book: Data Structures (5 days)', 25.00,  @o_library),
    (@s2, @psits,   @term, 'Unpaid membership fee',                   150.00, @o_psits),
    (@s3, @lsg,     @term, 'Absent during Acquaintance Party',        50.00,  @o_lsg),
    (@s3, @cit,     @term, 'Lost laboratory ID',                      100.00, @o_cit),
    (@s4, @library, @term, 'Overdue book: Software Engineering',      40.00,  @o_library),
    (@s4, @phg,     @term, 'Missing uniform during inspection',       75.00,  @o_phg),
    (@s5, @psits,   @term, 'Unpaid membership fee',                   150.00, @o_psits),
    (@s6, @ccso,    @term, 'Absent during Community Outreach',        100.00, @o_ccso),
    (@s7, @lsg,     @term, 'Absent during Intramurals opening',       50.00,  @o_lsg),
    (@s8, @eso,     @term, 'Missed English proficiency seminar',      60.00,  @o_eso),
    (@s9, @cit,     @term, 'Damaged laboratory keyboard',             350.00, @o_cit),
    (@s10, @library,@term, 'Overdue book: Operating Systems',         30.00,  @o_library),
    (@s11, @psits,  @term, 'Unpaid membership fee',                   150.00, @o_psits),
    (@s12, @lsg,    @term, 'Absent during General Assembly',          50.00,  @o_lsg),
    (@s2, @lsg,     @oldterm, 'Absent during Foundation Day (previous semester)', 50.00, @o_lsg);
SET @f_s1_lsg   = (SELECT id FROM fines WHERE student_id = @s1 AND org_id = @lsg AND term_id = @term);
SET @f_s1_lib   = (SELECT id FROM fines WHERE student_id = @s1 AND org_id = @library);
SET @f_s2_psits = (SELECT id FROM fines WHERE student_id = @s2 AND org_id = @psits);
SET @f_s3_lsg   = (SELECT id FROM fines WHERE student_id = @s3 AND org_id = @lsg);
SET @f_s4_lib   = (SELECT id FROM fines WHERE student_id = @s4 AND org_id = @library);
SET @f_s5_psits = (SELECT id FROM fines WHERE student_id = @s5 AND org_id = @psits);
SET @f_s7_lsg   = (SELECT id FROM fines WHERE student_id = @s7 AND org_id = @lsg);
SET @f_s9_cit   = (SELECT id FROM fines WHERE student_id = @s9 AND org_id = @cit);
SET @f_s2_old   = (SELECT id FROM fines WHERE student_id = @s2 AND org_id = @lsg AND term_id = @oldterm);

-- Waived fine (officer discretion).
UPDATE fines SET waived_at = CURRENT_TIMESTAMP(6), waived_by = @o_lsg,
    waiver_reason = 'Excused: medical certificate presented' WHERE id = @f_s7_lsg;

-- ---------------------------------------------------------------- payments
-- Each payment stores the archived receipt snapshot required by the trigger.
INSERT INTO payments (fine_id, amount, receipt_reference, received_by, receipt_snapshot) VALUES
    (@f_s1_lsg, 50.00, 'OR-2026-000101', @cashier1, JSON_OBJECT(
        'studentNumber','2024-00001','studentName','Juan P. Dela Cruz','officeCode','LSG','officeName','LSG',
        'termCode','2026-2027-1','termName','Academic Year 2026-2027 - First Semester',
        'reason','Absent during General Assembly','assessedAmount','50.00','paidAmount','50.00','balanceAfter','0.00',
        'receiverUsername','cashier1','receiverName','Paulo Ramos')),
    (@f_s2_psits, 100.00, 'OR-2026-000102', @cashier1, JSON_OBJECT(
        'studentNumber','2024-00002','studentName','Maria C. Santos','officeCode','PSITS','officeName','PSITS',
        'termCode','2026-2027-1','termName','Academic Year 2026-2027 - First Semester',
        'reason','Unpaid membership fee','assessedAmount','150.00','paidAmount','100.00','balanceAfter','50.00',
        'receiverUsername','cashier1','receiverName','Paulo Ramos')),
    (@f_s3_lsg, 50.00, 'OR-2026-000103', @cashier2, JSON_OBJECT(
        'studentNumber','2024-00003','studentName','Andres Bonifacio','officeCode','LSG','officeName','LSG',
        'termCode','2026-2027-1','termName','Academic Year 2026-2027 - First Semester',
        'reason','Absent during Acquaintance Party','assessedAmount','50.00','paidAmount','50.00','balanceAfter','0.00',
        'receiverUsername','cashier2','receiverName','Irene Castro')),
    (@f_s4_lib, 40.00, 'OR-2026-000104', @cashier2, JSON_OBJECT(
        'studentNumber','2024-00004','studentName','Gabriela C. Silang','officeCode','LIBRARY','officeName','Library',
        'termCode','2026-2027-1','termName','Academic Year 2026-2027 - First Semester',
        'reason','Overdue book: Software Engineering','assessedAmount','40.00','paidAmount','40.00','balanceAfter','0.00',
        'receiverUsername','cashier2','receiverName','Irene Castro')),
    (@f_s9_cit, 150.00, 'OR-2026-000105', @cashier1, JSON_OBJECT(
        'studentNumber','2024-00009','studentName','Gregoria De Jesus','officeCode','CIT','officeName','CIT',
        'termCode','2026-2027-1','termName','Academic Year 2026-2027 - First Semester',
        'reason','Damaged laboratory keyboard','assessedAmount','350.00','paidAmount','150.00','balanceAfter','200.00',
        'receiverUsername','cashier1','receiverName','Paulo Ramos')),
    (@f_s5_psits, 150.00, 'OR-2026-000106', @cashier1, JSON_OBJECT(
        'studentNumber','2024-00005','studentName','Emilio F. Aguinaldo Jr.','officeCode','PSITS','officeName','PSITS',
        'termCode','2026-2027-1','termName','Academic Year 2026-2027 - First Semester',
        'reason','Unpaid membership fee','assessedAmount','150.00','paidAmount','150.00','balanceAfter','0.00',
        'receiverUsername','cashier1','receiverName','Paulo Ramos')),
    (@f_s2_old, 50.00, 'OR-2026-000090', @cashier2, JSON_OBJECT(
        'studentNumber','2024-00002','studentName','Maria C. Santos','officeCode','LSG','officeName','LSG',
        'termCode','2025-2026-2','termName','Academic Year 2025-2026 - Second Semester',
        'reason','Absent during Foundation Day (previous semester)','assessedAmount','50.00','paidAmount','50.00','balanceAfter','0.00',
        'receiverUsername','cashier2','receiverName','Irene Castro'));
-- Voided payment: the receipt was issued in error and re-collected under OR-2026-000106.
UPDATE payments SET voided_at = CURRENT_TIMESTAMP(6), voided_by = @cashier1,
    void_reason = 'Wrong student encoded; reissued' WHERE receipt_reference = 'OR-2026-000106';

-- ---------------------------------------------------------------- clearance records
INSERT INTO clearance_records (student_id, org_id, term_id, status, remarks, cleared_by, cleared_at) VALUES
    (@s1, @lsg,     @term, 'cleared', 'Fine settled',        @o_lsg,     CURRENT_TIMESTAMP(6)),
    (@s3, @lsg,     @term, 'cleared', NULL,                  @o_lsg,     CURRENT_TIMESTAMP(6)),
    (@s4, @library, @term, 'cleared', 'Book returned, fine paid', @o_library, CURRENT_TIMESTAMP(6)),
    (@s6, @lsg,     @term, 'cleared', NULL,                  @o_lsg,     CURRENT_TIMESTAMP(6)),
    (@s7, @lsg,     @term, 'cleared', 'Fine waived',         @o_lsg,     CURRENT_TIMESTAMP(6));
INSERT INTO clearance_records (student_id, org_id, term_id, status, remarks) VALUES
    (@s1, @library, @term, 'pending', NULL),
    (@s2, @psits,   @term, 'on_hold', 'Remaining balance of PHP 50.00'),
    (@s9, @cit,     @term, 'on_hold', 'Balance PHP 200.00 on damaged equipment'),
    (@s5, @psits,   @term, 'pending', NULL),
    (@s8, @eso,     @term, 'pending', NULL);
-- Cleared with a balance exception approved by the officer.
INSERT INTO clearance_records (student_id, org_id, term_id, status, remarks, cleared_by, cleared_at,
    balance_exception, exception_reason, exception_by, exception_at) VALUES
    (@s10, @library, @term, 'cleared', 'Allowed to clear; payment promised next week', @o_library, CURRENT_TIMESTAMP(6),
     TRUE, 'Scholarship release pending; dean approved', @o_library, CURRENT_TIMESTAMP(6));

-- ---------------------------------------------------------------- today's queues
INSERT INTO queue_counters (org_id, queue_date, last_seq) VALUES
    (@lsg, CURRENT_DATE, 6), (@psits, CURRENT_DATE, 3), (@library, CURRENT_DATE, 2), (@cit, CURRENT_DATE, 1);
-- LSG: one completed, one cancelled, one being served, three waiting.
INSERT INTO queue_tickets (student_id, org_id, term_id, queue_date, seq, status, issued_by, served_by,
    called_at, serving_at, completed_at, cancelled_at, cancellation_reason, issue_request_id) VALUES
    (@s1,  @lsg, @term, CURRENT_DATE, 1, 'completed', @o_lsg, @o_lsg, NOW(6), NOW(6), NOW(6), NULL, NULL, UUID()),
    (@s12, @lsg, @term, CURRENT_DATE, 2, 'cancelled', @o_lsg, NULL, NULL, NULL, NULL, NOW(6), 'Student left the line', UUID()),
    (@s3,  @lsg, @term, CURRENT_DATE, 3, 'serving',   @o_lsg, @o_lsg, NOW(6), NOW(6), NULL, NULL, NULL, UUID()),
    (@s7,  @lsg, @term, CURRENT_DATE, 4, 'waiting',   @o_lsg, NULL, NULL, NULL, NULL, NULL, NULL, UUID()),
    (@s8,  @lsg, @term, CURRENT_DATE, 5, 'waiting',   @o_lsg, NULL, NULL, NULL, NULL, NULL, NULL, UUID()),
    (@s9,  @lsg, @term, CURRENT_DATE, 6, 'waiting',   @o_lsg, NULL, NULL, NULL, NULL, NULL, NULL, UUID());
-- PSITS: one called (at the desk), two waiting.
INSERT INTO queue_tickets (student_id, org_id, term_id, queue_date, seq, status, issued_by, served_by,
    called_at, issue_request_id) VALUES
    (@s2,  @psits, @term, CURRENT_DATE, 1, 'called',  @o_psits, @o_psits, NOW(6), UUID()),
    (@s5,  @psits, @term, CURRENT_DATE, 2, 'waiting', @o_psits, NULL, NULL, UUID()),
    (@s11, @psits, @term, CURRENT_DATE, 3, 'waiting', @o_psits, NULL, NULL, UUID());
-- LIBRARY and CIT: waiting only (one deferred).
INSERT INTO queue_tickets (student_id, org_id, term_id, queue_date, seq, status, defer_count, issued_by, issue_request_id) VALUES
    (@s4,  @library, @term, CURRENT_DATE, 1, 'waiting',  0, @o_library, UUID()),
    (@s10, @library, @term, CURRENT_DATE, 2, 'deferred', 1, @o_library, UUID()),
    (@s6,  @cit,     @term, CURRENT_DATE, 1, 'waiting',  0, @o_cit,     UUID());

COMMIT;

SET @audit_user_id = NULL;
SET @audit_request_id = NULL;
SET @dev_hash = NULL;

SELECT 'students' AS entity, COUNT(*) AS total FROM students
UNION ALL SELECT 'users', COUNT(*) FROM users
UNION ALL SELECT 'fines', COUNT(*) FROM fines
UNION ALL SELECT 'payments', COUNT(*) FROM payments
UNION ALL SELECT 'clearance_records', COUNT(*) FROM clearance_records
UNION ALL SELECT 'queue_tickets (today)', COUNT(*) FROM queue_tickets WHERE queue_date = CURRENT_DATE;
