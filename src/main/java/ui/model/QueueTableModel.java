package ui.model;
import model.QueueRow;
public final class QueueTableModel extends RecordTableModel<QueueRow> {
    public QueueTableModel(){super("Ticket","Student number","Student","Status","Deferrals","Issued");}
    @Override public Object getValueAt(int row,int col){var r=rowAt(row);return switch(col){case 0->r.number();case 1->r.studentNumber();case 2->r.studentName();case 3->r.ticket().status().displayName();case 4->Long.toString(r.ticket().deferCount());default->r.ticket().createdAt().toLocalTime().withNano(0).toString();};}
}
