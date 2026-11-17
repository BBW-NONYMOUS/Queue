package ui.model;

import model.Organization;

public final class OfficeTableModel extends RecordTableModel<Organization> {
    public OfficeTableModel() { super("Code", "Office name", "Queue", "Status"); }
    @Override public Object getValueAt(int row, int column) {
        Organization o = rowAt(row);
        return switch (column) {
            case 0 -> o.code().name(); case 1 -> o.name(); case 2 -> o.queueOpen() ? "Open" : "Closed"; case 3 -> o.active() ? "Active" : "Inactive";
            default -> throw new IndexOutOfBoundsException(column);
        };
    }
}
