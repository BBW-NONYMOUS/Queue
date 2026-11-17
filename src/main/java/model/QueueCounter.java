package model;

import java.time.LocalDateTime;
import java.time.LocalDate;

/** Daily office sequence counter, updated under a row lock. */
public record QueueCounter(
        long orgId,
        LocalDate queueDate,
        long lastSeq,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
