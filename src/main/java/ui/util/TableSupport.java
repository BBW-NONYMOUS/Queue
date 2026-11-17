package ui.util;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

public final class TableSupport {
    private TableSupport() {
    }
    public static void configure(JTable table, AbstractTableModel model, String accessibleName, int... statusColumns) {
        table.setModel(model);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new java.awt.Dimension(8, 0));
        table.getTableHeader().setReorderingAllowed(false);
        table.getAccessibleContext().setAccessibleName(accessibleName);
        table.setDefaultRenderer(String.class, new PlainTextCellRenderer());
        java.awt.Container ancestor = javax.swing.SwingUtilities.getAncestorOfClass(javax.swing.JScrollPane.class, table);
        if (ancestor instanceof javax.swing.JScrollPane scroll) { scroll.setColumnHeaderView(table.getTableHeader()); }
        for (int column : statusColumns) { table.getColumnModel().getColumn(column).setCellRenderer(new StatusCellRenderer()); }
    }

    public static void widths(JTable table, int... widths) {
        if (widths.length != table.getColumnCount()) { throw new IllegalArgumentException("Specify a width for every column."); }
        for (int i = 0; i < widths.length; i++) { table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]); }
    }
}
