package model;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/** Assessed fine. Status and balance come from the payment ledger. */
public record Fine(
        long id,
        long studentId,
        long orgId,
        long termId,
        String reason,
        BigDecimal amount,
        long assessedBy,
        LocalDateTime waivedAt,
        Long waivedBy,
        String waiverReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
