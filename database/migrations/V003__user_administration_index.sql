-- Apply once as DBA after V002. Online secondary index for ordered admin locks.
-- Forward-only: retain deployed migrations unchanged; no data is removed.
-- Recovery, if needed: add a NEW forward migration with
-- ALTER TABLE users DROP INDEX idx_users_role_active, ALGORITHM=INPLACE, LOCK=NONE;
-- Removing this optional performance index does not remove business data.
USE queue_clearance;
ALTER TABLE users ADD INDEX idx_users_role_active (role, is_active, id),
    ALGORITHM=INPLACE, LOCK=NONE;
