package ui.util;

import java.awt.Component;
import javax.swing.JTable;

public final class StatusCellRenderer extends PlainTextCellRenderer {
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
        super.getTableCellRendererComponent(table, value, selected, focused, row, column);
        if (!selected) {
            String status = String.valueOf(value);
            setForeground(switch (status) {
                case "Active", "Open", "Paid", "Waived", "Recorded", "Completed", "Serving", "Cleared", "Cleared (exception)", "Approved" -> UiStyle.SUCCESS;
                case "Required", "Partial", "Waiting", "Deferred", "Called", "Pending", "On hold" -> new java.awt.Color(0xB54708);
                case "Unpaid", "Voided", "Cancelled" -> UiStyle.ERROR;
                default -> UiStyle.MUTED;
            });
        }
        return this;
    }
}
