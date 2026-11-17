package ui.util;

import javax.swing.table.DefaultTableCellRenderer;

public class PlainTextCellRenderer extends DefaultTableCellRenderer {
    public PlainTextCellRenderer() { putClientProperty("html.disable", Boolean.TRUE); }
    @Override public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean selected,
            boolean focused, int row, int column) {
        super.getTableCellRendererComponent(table, value, selected, focused, row, column);
        String text = value == null ? "" : value.toString();
        setToolTipText(text.isEmpty() ? null : "<html><div style='width:320px'>" + UiStyle.escape(text) + "</div></html>");
        return this;
    }
}
