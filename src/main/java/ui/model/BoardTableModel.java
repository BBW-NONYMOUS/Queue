package ui.model;
import model.BoardRow;
public final class BoardTableModel extends RecordTableModel<BoardRow> {
    public BoardTableModel(){super("Office","Now serving","Status","Waiting","New tickets");}
    @Override public Object getValueAt(int row,int col){var r=rowAt(row);return switch(col){case 0->r.office().name();case 1->r.number();case 2->r.status().substring(0,1).toUpperCase(java.util.Locale.ROOT)+r.status().substring(1);case 3->Long.toString(r.waiting());default->!r.active()?"Inactive":r.open()?"Open":"Closed";};}
}
