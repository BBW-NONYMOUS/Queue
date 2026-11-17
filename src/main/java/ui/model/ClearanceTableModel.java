package ui.model;
import model.*;
public final class ClearanceTableModel extends RecordTableModel<ClearanceRow> {
    public ClearanceTableModel(){super("Office","Clearance","Balance (PHP)","Balance exception","Remarks");}
    @Override public Object getValueAt(int row,int col){var r=rowAt(row);return switch(col){case 0->r.office().code().name();case 1->r.status().displayName();case 2->r.balance();case 3->r.record()!=null&&r.record().balanceException()?"Approved":"None";default->r.record()==null?"":r.record().remarks();};}
    @Override public Class<?> getColumnClass(int column){return column==2?java.math.BigDecimal.class:String.class;}
}
