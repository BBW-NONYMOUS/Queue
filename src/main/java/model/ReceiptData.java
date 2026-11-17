package model;

import java.math.BigDecimal;

/** Identity and charge fields are archived at payment time; void metadata is live. */
public record ReceiptData(Payment payment, String studentNumber, String studentName,
        String officeCode, String officeName, String termCode, String termName, String reason,
        BigDecimal assessedAmount, BigDecimal balanceAfter, String receiverUsername, String receiverName) {
}
