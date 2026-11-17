package model;

import java.time.LocalDateTime;

/** Append-only audit entry. JSON remains a transport value, without a JSON dependency. */
public record AuditLog(
        long id,
        Long actorUserId,
        String databaseAccount,
        long connectionId,
        String requestId,
        AuditAction action,
        String tableName,
        String recordKey,
        String oldValuesJson,
        String newValuesJson,
        LocalDateTime occurredAt) {
}
