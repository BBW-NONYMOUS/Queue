package model;

import java.math.BigDecimal;

/** Computed fine balance projection, never a persisted running total. */
public record FineBalance(
        long fineId,
        long studentId,
        long orgId,
        long termId,
        BigDecimal amount,
        BigDecimal paidAmount,
        BigDecimal balance,
        FineStatus status) {
}
