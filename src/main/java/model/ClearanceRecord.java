package model;

import java.time.LocalDateTime;

/** Office clearance decision and explicit balance exception metadata. */
public record ClearanceRecord(
        long id,
        long studentId,
        long orgId,
        long termId,
        ClearanceStatus status,
        String remarks,
        Long clearedBy,
        LocalDateTime clearedAt,
        boolean balanceException,
        String exceptionReason,
        Long exceptionBy,
        LocalDateTime exceptionAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
