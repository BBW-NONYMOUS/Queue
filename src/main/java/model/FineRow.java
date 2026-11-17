package model;

import java.math.BigDecimal;

public record FineRow(Fine fine, String studentNumber, String studentName, String officeCode,
        String termCode, BigDecimal paidAmount, BigDecimal balance, FineStatus status) {
}
