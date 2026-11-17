-- Run once AFTER both schema migrations, as the installation DBA.
-- In this SAME mysql session first set @bootstrap_admin_hash to the $2a$ hash
-- emitted by app.PasswordHashTool. A missing/invalid hash aborts the first INSERT.
USE queue_clearance;
SET SESSION time_zone = '+08:00';
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
SET @audit_user_id = NULL;
SET @audit_request_id = 'installation-seed-v1';
START TRANSACTION;
INSERT INTO users (username, password_hash, display_name, role, must_change_password)
VALUES ('admin', @bootstrap_admin_hash, 'System Administrator', 'admin', FALSE);
SET @audit_user_id = LAST_INSERT_ID();
INSERT INTO organizations (code, name, queue_open) VALUES
    ('LSG', 'LSG', FALSE), ('PSITS', 'PSITS', FALSE), ('PHG', 'PHG', FALSE),
    ('LIBRARY', 'Library', FALSE), ('CCSO', 'CCSO', FALSE), ('CIT', 'CIT', FALSE), ('ESO', 'ESO', FALSE);
-- Assumed calendar, deliberately inactive until the registrar confirms dates.
INSERT INTO terms (code, name, starts_on, ends_on, is_current, is_active)
VALUES ('2026-2027-1', 'Academic Year 2026-2027 - First Semester', '2026-08-01', '2026-12-31', FALSE, FALSE);
COMMIT;
SET @audit_user_id = NULL;
SET @audit_request_id = NULL;
SET @bootstrap_admin_hash = NULL;
