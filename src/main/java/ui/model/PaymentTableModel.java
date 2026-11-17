package ui.model;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import model.PaymentRow;

public final class PaymentTableModel extends RecordTableModel<PaymentRow> {
    public PaymentTableModel() { super("Receipt reference", "Student number", "Amount", "Paid at (+08:00)", "Status", "Office", "Term", "Student"); }
    @Override public Class<?> getColumnClass(int column) { return column == 2 ? BigDecimal.class : String.class; }
    @Override public Object getValueAt(int row, int column) { PaymentRow p = rowAt(row); return switch (column) {
        case 0 -> p.payment().receiptReference(); case 1 -> p.studentNumber(); case 2 -> p.payment().amount();
        case 3 -> p.payment().paidAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); case 4 -> p.payment().voidedAt() == null ? "Recorded" : "Voided";
        case 5 -> p.officeCode(); case 6 -> p.termCode(); case 7 -> p.studentName();
        default -> throw new IndexOutOfBoundsException(column);
    }; }
}
