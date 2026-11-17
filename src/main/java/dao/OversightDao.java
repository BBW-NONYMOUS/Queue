package dao;
import java.sql.*;
import java.time.*;
import java.util.*;
import model.*;
import service.*;
final class OversightDao extends BaseDao implements OversightRepository {
    private static final String AUDIT="SELECT a.*,JSON_PRETTY(a.old_values) old_json,JSON_PRETTY(a.new_values) new_json,COALESCE(u.username,'Database/system') actor_name FROM audit_logs a LEFT JOIN users u ON u.id=a.actor_user_id ";
    OversightDao(Connection connection,boolean writable){super(connection,writable);}
    @Override public List<AuditRow> audit(AuditFilter f)throws ServiceException {
        return query(AUDIT+"WHERE (?='' OR a.table_name=?) AND (?='' OR u.username=?) AND (?='' OR a.record_key=?) AND a.occurred_at>=? AND a.occurred_at<? ORDER BY a.occurred_at DESC,a.id DESC LIMIT 51 OFFSET ?",p->{int n=1;for(String value:List.of(f.table(),f.actor(),f.recordKey())){p.setString(n++,value);p.setString(n++,value);}p.setObject(n++,f.from().atStartOfDay());p.setObject(n++,f.through().plusDays(1).atStartOfDay());p.setInt(n,f.page()*50);},OversightDao::auditRow);
    }
    @Override public Optional<AuditRow> auditEntry(long id)throws ServiceException{return query(AUDIT+"WHERE a.id=?",p->p.setLong(1,id),OversightDao::auditRow).stream().findFirst();}
    private static AuditRow auditRow(ResultSet r)throws SQLException {
        return new AuditRow(new AuditLog(r.getLong("id"),r.getObject("actor_user_id",Long.class),r.getString("database_account"),r.getLong("connection_id"),r.getString("request_id"),AuditAction.valueOf(r.getString("action")),r.getString("table_name"),r.getString("record_key"),r.getString("old_json"),r.getString("new_json"),r.getObject("occurred_at",LocalDateTime.class)),r.getString("actor_name"));
    }
    @Override public ReportData report(ReportFilter f)throws ServiceException {
        var parameters=new ArrayList<Object>();String select,from,office=null,term=null,date=null;List<ReportColumn> columns;
        String name="CONCAT_WS(' ',s.first_name,s.middle_name,s.last_name,s.suffix)";
        switch(f.type()) {
            case STUDENTS->{select="s.student_number,"+name+",s.program,s.year_level,s.section,IF(s.is_active,'Active','Inactive')";from="students s";columns=columns("Student number","Student","Program","Year","Section","Status");}
            case QUEUE->{select="q.queue_date,o.code,CONCAT(o.code,'-',IF(q.seq<1000,LPAD(q.seq,3,'0'),CAST(q.seq AS CHAR))),s.student_number,"+name+",t.code,q.status,q.defer_count,u.username,q.completed_at";
                from="queue_tickets q JOIN students s ON s.id=q.student_id JOIN organizations o ON o.id=q.org_id JOIN terms t ON t.id=q.term_id LEFT JOIN users u ON u.id=q.served_by";office="q.org_id";term="q.term_id";date="q.queue_date";columns=columns("Date","Office","Ticket","Student number","Student","Term","Status","Deferrals","Serving user","Completed");}
            case FINES->{select="s.student_number,"+name+",o.code,t.code,b.reason,b.amount,b.paid_amount,b.balance,b.status";
                from="v_fine_balances b JOIN students s ON s.id=b.student_id JOIN organizations o ON o.id=b.org_id JOIN terms t ON t.id=b.term_id";office="b.org_id";term="b.term_id";date="b.created_at";columns=moneyColumns(columns("Student number","Student","Office","Term","Reason","Assessed (PHP)","Paid (PHP)","Balance (PHP)","Status"),5,6,7);}
            case PAYMENTS->{select="p.receipt_reference,p.paid_at,COALESCE(p.receipt_snapshot->>'$.studentNumber',s.student_number),COALESCE(p.receipt_snapshot->>'$.studentName',"+name+"),o.code,t.code,p.amount,IF(p.voided_at IS NULL,p.amount,0.00),IF(p.voided_at IS NULL,'Recorded','Voided'),u.username,p.void_reason";
                from="payments p JOIN fines f ON f.id=p.fine_id JOIN students s ON s.id=f.student_id JOIN organizations o ON o.id=f.org_id JOIN terms t ON t.id=f.term_id JOIN users u ON u.id=p.received_by";office="f.org_id";term="f.term_id";date="p.paid_at";columns=moneyColumns(columns("Receipt","Paid at","Student number at payment","Student at payment","Office","Term","Amount (PHP)","Net received (PHP)","Status","Receiving user","Void reason"),6,7);}
            case CLEARANCE->{select="s.student_number,"+name+",o.code,t.code,COALESCE(c.status,'pending'),COALESCE((SELECT SUM(b.balance) FROM v_fine_balances b WHERE b.student_id=s.id AND b.org_id=o.id AND b.term_id=t.id),0.00),IF(COALESCE(c.balance_exception,0),'Approved','None'),c.exception_reason,c.remarks";
                from="students s CROSS JOIN organizations o JOIN terms t ON t.id=? LEFT JOIN clearance_records c ON c.student_id=s.id AND c.org_id=o.id AND c.term_id=t.id";parameters.add(f.termId());office="o.id";term="t.id";columns=moneyColumns(columns("Student number","Student","Office","Term","Status","Balance (PHP)","Balance exception","Exception reason","Remarks"),5);}
            default->throw new IllegalArgumentException("Unknown report");
        }
        StringBuilder sql=new StringBuilder("SELECT "+select+" FROM "+from+" WHERE 1=1");
        if(!f.includeInactive())sql.append(" AND s.is_active=1");
        add(sql,parameters,!f.studentNumber().isEmpty(),"s.student_number = ?",f.studentNumber());
        add(sql,parameters,!f.program().isEmpty(),"s.program = ?",f.program());
        add(sql,parameters,f.yearLevel()!=null,"s.year_level = ?",f.yearLevel());
        add(sql,parameters,!f.section().isEmpty(),"s.section = ?",f.section());
        add(sql,parameters,office!=null&&f.orgId()!=null,office+" = ?",f.orgId());
        add(sql,parameters,term!=null&&f.termId()!=null,term+" = ?",f.termId());
        if(date!=null){add(sql,parameters,f.from()!=null,date+" >= ?",f.from()==null?null:f.from().atStartOfDay());add(sql,parameters,f.through()!=null,date+" < ?",f.through()==null?null:f.through().plusDays(1).atStartOfDay());}
        sql.append(" ORDER BY s.student_number");if(office!=null)sql.append(',').append(office);if(date!=null)sql.append(',').append(date);
        // Stable tie breakers keep exports reproducible without changing their data scope.
        sql.append(switch(f.type()){case STUDENTS->",s.id";case QUEUE->",q.id";case FINES->",b.fine_id";case PAYMENTS->",p.id";case CLEARANCE->",s.id";});sql.append(" LIMIT 10001");
        List<ReportColumn> headings=columns;
        var rows=query(sql.toString(),p->{for(int i=0;i<parameters.size();i++)p.setObject(i+1,parameters.get(i));},r->{var values=new ArrayList<Object>();for(int i=0;i<headings.size();i++){Object value=switch(headings.get(i).type()){case MONEY->r.getBigDecimal(i+1);case INTEGER->r.getObject(i+1,Integer.class);case DATE->r.getObject(i+1,LocalDate.class);case DATETIME->r.getObject(i+1,LocalDateTime.class);case TEXT->r.getString(i+1);};if(headings.get(i).label().equals("Status")&&value instanceof String status)value=status.substring(0,1).toUpperCase(Locale.ROOT)+status.substring(1).replace('_',' ');values.add(value);}return Collections.unmodifiableList(values);});
        if(rows.size()>10000)throw new ValidationException("This report exceeds 10,000 rows. Narrow the office, student, program, term or dates and export again.");
        LocalDateTime now=query("SELECT CURRENT_TIMESTAMP(6)",p->{},r->r.getObject(1,LocalDateTime.class)).getFirst();
        String scope="Term: "+(f.termId()==null?"all":query("SELECT code FROM terms WHERE id=?",p->p.setLong(1,f.termId()),r->r.getString(1)).getFirst())+"; office: "+(f.orgId()==null?"all":query("SELECT code FROM organizations WHERE id=?",p->p.setLong(1,f.orgId()),r->r.getString(1)).getFirst())+"; student: "+(f.studentNumber().isEmpty()?"all":f.studentNumber())+"; program: "+(f.program().isEmpty()?"all":f.program())+"; year: "+(f.yearLevel()==null?"all":f.yearLevel())+"; section: "+(f.section().isEmpty()?"all":f.section())+"; dates: "+(f.from()==null?"any":f.from())+" to "+(f.through()==null?"any":f.through())+"; inactive students: "+f.includeInactive();
        return new ReportData(f.type().toString()+" report",now,scope,columns,rows);
    }
    private static void add(StringBuilder sql,List<Object> parameters,boolean include,String expression,Object value){if(include){sql.append(" AND ").append(expression);parameters.add(value);}}
    private static List<ReportColumn> columns(String... names){return Arrays.stream(names).map(n->new ReportColumn(n,switch(n){case "Year","Deferrals"->ReportValueType.INTEGER;case "Date"->ReportValueType.DATE;case "Paid at","Completed"->ReportValueType.DATETIME;default->ReportValueType.TEXT;})).toList();}
    private static List<ReportColumn> moneyColumns(List<ReportColumn> original,int... indices){var result=new ArrayList<>(original);for(int i:indices)result.set(i,new ReportColumn(result.get(i).label(),true));return List.copyOf(result);}
}
