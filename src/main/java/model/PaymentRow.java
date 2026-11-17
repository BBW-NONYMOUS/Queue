package model;

public record PaymentRow(Payment payment, long studentId, String studentNumber, String studentName,
        String officeCode, String termCode, boolean archivedReceipt) {
}
