package ui.model;
import model.*;
public final class AuditTableModel extends RecordTableModel<AuditRow> {
    public AuditTableModel(){super("Server time","Actor","Action","Table","Record key","Request");}
    @Override public Object getValueAt(int row,int col){var r=rowAt(row);return switch(col){case 0->r.entry().occurredAt().toString().replace('T',' ');case 1->r.actorName();case 2->r.entry().action().name();case 3->r.entry().tableName();case 4->r.entry().recordKey();default->r.entry().requestId();};}
}
