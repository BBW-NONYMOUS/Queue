package ui.model;
import java.util.*;
import model.*;
public final class StudentOfficeTableModel extends RecordTableModel<BoardRow> {
    private Map<Long,ClearanceRow> clearances=Map.of();
    public StudentOfficeTableModel(){super("Office","Now serving","Queue status","Waiting","New tickets","Balance (PHP)","Clearance");}
    public void clearances(List<ClearanceRow> rows){var result=new HashMap<Long,ClearanceRow>();for(var row:rows)result.put(row.office().id(),row);clearances=Map.copyOf(result);}
    @Override public Class<?> getColumnClass(int column){return column==5?java.math.BigDecimal.class:String.class;}
    @Override public Object getValueAt(int row,int column){var r=rowAt(row);var c=clearances.get(r.orgId());return switch(column){case 0->r.office().name();case 1->r.number();case 2->r.status().substring(0,1).toUpperCase(Locale.ROOT)+r.status().substring(1);case 3->Long.toString(r.waiting());case 4->!r.active()?"Inactive":r.open()?"Open":"Closed";case 5->c==null?null:c.balance();default->c==null?"Unavailable":c.status().displayName()+(c.record()!=null&&c.record().balanceException()?" (exception)":"");};}
}
