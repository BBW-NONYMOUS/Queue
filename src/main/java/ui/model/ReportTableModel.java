package ui.model;
import javax.swing.table.AbstractTableModel;
import model.*;
public final class ReportTableModel extends AbstractTableModel {
    private ReportData data;
    public void show(ReportData data){if(!javax.swing.SwingUtilities.isEventDispatchThread())throw new IllegalStateException("Update models on the EDT");this.data=data;fireTableStructureChanged();}
    @Override public int getRowCount(){return data==null?0:Math.min(100,data.rows().size());}
    @Override public int getColumnCount(){return data==null?0:data.columns().size();}
    @Override public String getColumnName(int column){return data.columns().get(column).label();}
    @Override public Class<?> getColumnClass(int column){return switch(data.columns().get(column).type()){case MONEY->java.math.BigDecimal.class;case INTEGER->Integer.class;case DATE->java.time.LocalDate.class;case DATETIME->java.time.LocalDateTime.class;default->String.class;};}
    @Override public Object getValueAt(int row,int column){return data.rows().get(row).get(column);}
    @Override public boolean isCellEditable(int row,int column){return false;}
}
