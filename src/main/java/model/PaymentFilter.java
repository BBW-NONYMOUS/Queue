package model;

import java.time.LocalDate;

public record PaymentFilter(String query, Long orgId, Long termId, Boolean voided,
        LocalDate from, LocalDate through, int page, int pageSize) {
}
