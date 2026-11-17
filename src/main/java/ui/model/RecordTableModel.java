package ui.model;

import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

public abstract class RecordTableModel<T> extends AbstractTableModel {
    private List<T> rows = List.of();
    private final String[] columns;

    protected RecordTableModel(String... columns) { this.columns = columns.clone(); }
    public final void setRows(List<T> rows) {
        if (!SwingUtilities.isEventDispatchThread()) { throw new IllegalStateException("Update table models on the EDT."); }
        this.rows = List.copyOf(rows); fireTableDataChanged();
    }
    public final T rowAt(int index) { return rows.get(index); }
    @Override public final int getRowCount() { return rows.size(); }
    @Override public final int getColumnCount() { return columns.length; }
    @Override public final String getColumnName(int column) { return columns[column]; }
    @Override public Class<?> getColumnClass(int column) { return String.class; }
    @Override public final boolean isCellEditable(int row, int column) { return false; }
}
