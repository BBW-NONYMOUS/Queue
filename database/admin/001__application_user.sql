-- Run once as the installation DBA, AFTER migrations and seed.
-- Assumption: campus LAN is 192.168.1.0/24. Change this host restriction to
-- the actual campus subnet BEFORE executing. Do not use '%' for deployment.
-- MySQL returns the generated password once; save it in external app.properties.
CREATE USER 'queue_app'@'192.168.1.0/255.255.255.0'
    IDENTIFIED WITH caching_sha2_password BY RANDOM PASSWORD REQUIRE SSL;

GRANT SELECT, INSERT, UPDATE ON queue_clearance.organizations TO 'queue_app'@'192.168.1.0/255.255.255.0';
GRANT SELECT, INSERT, UPDATE ON queue_clearance.students TO 'queue_app'@'192.168.1.0/255.255.255.0';
GRANT SELECT, INSERT, UPDATE ON queue_clearance.terms TO 'queue_app'@'192.168.1.0/255.255.255.0';
GRANT SELECT, INSERT, UPDATE ON queue_clearance.users TO 'queue_app'@'192.168.1.0/255.255.255.0';
GRANT SELECT, INSERT, UPDATE ON queue_clearance.fines TO 'queue_app'@'192.168.1.0/255.255.255.0';
GRANT SELECT, INSERT, UPDATE ON queue_clearance.payments TO 'queue_app'@'192.168.1.0/255.255.255.0';
GRANT SELECT, INSERT, UPDATE ON queue_clearance.queue_counters TO 'queue_app'@'192.168.1.0/255.255.255.0';
GRANT SELECT, INSERT, UPDATE ON queue_clearance.queue_tickets TO 'queue_app'@'192.168.1.0/255.255.255.0';
GRANT SELECT, INSERT, UPDATE ON queue_clearance.clearance_records TO 'queue_app'@'192.168.1.0/255.255.255.0';
GRANT SELECT ON queue_clearance.audit_logs TO 'queue_app'@'192.168.1.0/255.255.255.0';
GRANT SELECT ON queue_clearance.v_fine_balances TO 'queue_app'@'192.168.1.0/255.255.255.0';
GRANT SELECT ON queue_clearance.v_office_balances TO 'queue_app'@'192.168.1.0/255.255.255.0';
GRANT SELECT ON queue_clearance.v_queue_board TO 'queue_app'@'192.168.1.0/255.255.255.0';
SHOW GRANTS FOR 'queue_app'@'192.168.1.0/255.255.255.0';
