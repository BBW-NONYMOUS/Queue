package model;

import java.time.LocalDateTime;
import java.time.LocalDate;

/** Queue ticket whose sequence remains stable when deferred or requeued. */
public record QueueTicket(
        long id,
        long studentId,
        long orgId,
        long termId,
        LocalDate queueDate,
        long seq,
        QueueStatus status,
        LocalDateTime sortAt,
        long deferCount,
        long issuedBy,
        Long servedBy,
        LocalDateTime calledAt,
        LocalDateTime servingAt,
        LocalDateTime completedAt,
        LocalDateTime cancelledAt,
        String cancellationReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        java.util.UUID issueRequestId) {
}
