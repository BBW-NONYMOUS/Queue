package ui.util;

import java.awt.Component;
import java.math.BigDecimal;
import javax.swing.JTable;

public final class MoneyCellRenderer extends PlainTextCellRenderer {
    public MoneyCellRenderer() { setHorizontalAlignment(RIGHT); }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
        super.getTableCellRendererComponent(table, value instanceof BigDecimal amount ? UiStyle.pesos(amount) : "", selected, focused, row, column);
        return this;
    }
}
