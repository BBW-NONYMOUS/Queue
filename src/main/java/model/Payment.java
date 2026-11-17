package model;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/** Immutable payment details with optional void metadata. */
public record Payment(
        long id,
        long fineId,
        BigDecimal amount,
        String receiptReference,
        long receivedBy,
        LocalDateTime paidAt,
        LocalDateTime voidedAt,
        Long voidedBy,
        String voidReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
