-- Contract migration. Keep all clients stopped until all three scripts succeed.
USE queue_clearance;
ALTER TABLE queue_tickets MODIFY issue_request_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
ADD UNIQUE KEY uq_queue_issue_request(issue_request_id);
