-- Run manually as DBA on MySQL 8.4, only after changing the exact host address.
-- Assumption: the designated host/admin workstation connects from 192.168.1.10.
-- This is a separate protected credential. Never use it as db.username in clients.
CREATE USER 'queue_backup'@'192.168.1.10'
    IDENTIFIED WITH caching_sha2_password BY RANDOM PASSWORD REQUIRE SSL;
GRANT SELECT, SHOW VIEW, TRIGGER ON queue_clearance.* TO 'queue_backup'@'192.168.1.10';
SHOW GRANTS FOR 'queue_backup'@'192.168.1.10';
-- Save the generated password once in the host's ACL-protected MySQL [client]
-- option file. No PROCESS, LOCK TABLES, RELOAD, DELETE or data-write grants needed
-- for the configured --no-tablespaces/--single-transaction/GTID-purged-OFF dump.
