package ui.model;

import java.math.BigDecimal;
import model.FineRow;

public final class FineTableModel extends RecordTableModel<FineRow> {
    public FineTableModel() { super("Student number", "Office", "Fine amount", "Paid", "Balance", "Status", "Reason", "Term", "Student"); }
    @Override public Class<?> getColumnClass(int column) { return column >= 2 && column <= 4 ? BigDecimal.class : String.class; }
    @Override public Object getValueAt(int row, int column) { FineRow f = rowAt(row); return switch (column) {
        case 0 -> f.studentNumber(); case 1 -> f.officeCode(); case 2 -> f.fine().amount(); case 3 -> f.paidAmount(); case 4 -> f.balance();
        case 5 -> f.status().displayName(); case 6 -> f.fine().reason(); case 7 -> f.termCode(); case 8 -> f.studentName(); default -> throw new IndexOutOfBoundsException(column);
    }; }
}
