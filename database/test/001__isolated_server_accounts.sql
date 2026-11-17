-- DBA SETUP FOR AN ISOLATED MYSQL 8.4 TEST SERVER ONLY. Never run on campus MySQL.
-- The JUnit fixture recreates ONLY queue_clearance_test after matching server_uuid.
-- Keep the generated passwords in the external integration properties file.
CREATE USER 'queue_it_owner'@'localhost' IDENTIFIED BY RANDOM PASSWORD REQUIRE SSL;
CREATE USER 'queue_it_app'@'localhost' IDENTIFIED BY RANDOM PASSWORD REQUIRE SSL;
GRANT CREATE,DROP,ALTER,INDEX,SELECT,INSERT,UPDATE,DELETE,REFERENCES,CREATE VIEW,SHOW VIEW,TRIGGER
    ON queue_clearance_test.* TO 'queue_it_owner'@'localhost' WITH GRANT OPTION;
-- On a binary-logged test server, the DBA may need to enable
-- log_bin_trust_function_creators for this isolated server, or use an isolated
-- DBA owner account. Do not change the campus server's global settings.
SELECT @@server_uuid AS isolated_server_uuid,VERSION() AS server_version;
