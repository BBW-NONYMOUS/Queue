package dao;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import model.*;
import service.*;

final class QueueDao extends BaseDao implements QueueRepository {
    private static final String ROW = "SELECT q.*, o.code office_code, s.student_number, CONCAT_WS(' ',s.first_name,s.last_name) student_name FROM queue_tickets q JOIN organizations o ON o.id=q.org_id JOIN students s ON s.id=q.student_id ";
    QueueDao(Connection connection, boolean writable) { super(connection,writable); }
    private static java.time.LocalDateTime time(ResultSet r,String name) throws SQLException { var t=r.getTimestamp(name); return t==null?null:t.toLocalDateTime(); }
    private static QueueRow row(ResultSet r) throws SQLException {
        Number served=(Number)r.getObject("served_by");
        return new QueueRow(new QueueTicket(r.getLong("id"),r.getLong("student_id"),r.getLong("org_id"),r.getLong("term_id"),r.getDate("queue_date").toLocalDate(),r.getLong("seq"),QueueStatus.valueOf(r.getString("status").toUpperCase(Locale.ROOT)),time(r,"sort_at"),r.getLong("defer_count"),r.getLong("issued_by"),served==null?null:served.longValue(),time(r,"called_at"),time(r,"serving_at"),time(r,"completed_at"),time(r,"cancelled_at"),r.getString("cancellation_reason"),time(r,"created_at"),time(r,"updated_at"),UUID.fromString(r.getString("issue_request_id"))),OfficeCode.valueOf(r.getString("office_code")),r.getString("student_number"),r.getString("student_name"));
    }
    @Override public QueueClock clock() throws ServiceException { return query("SELECT CURRENT_DATE d,CURRENT_TIMESTAMP(6) t",s->{},r->new QueueClock(r.getDate("d").toLocalDate(),r.getTimestamp("t").toLocalDateTime())).getFirst(); }
    @Override public Optional<Term> currentTerm(boolean lock) throws ServiceException {
        if(lock) requireWritable();
        return query("SELECT * FROM terms WHERE is_current=1 AND is_active=1"+(lock?" FOR SHARE":""),s->{},r->new Term(r.getLong("id"),r.getString("code"),r.getString("name"),r.getDate("starts_on").toLocalDate(),r.getDate("ends_on").toLocalDate(),r.getBoolean("is_current"),r.getBoolean("is_active"),time(r,"created_at"),time(r,"updated_at"))).stream().findFirst();
    }
    @Override public Optional<QueueRow> find(long id,boolean lock) throws ServiceException {
        if(lock) { requireWritable(); query("SELECT id FROM queue_tickets WHERE id=? FOR UPDATE",s->s.setLong(1,id),r->r.getLong(1)); }
        return query(ROW+"WHERE q.id=?",s->s.setLong(1,id),QueueDao::row).stream().findFirst();
    }
    @Override public Optional<QueueRow> byRequest(UUID request) throws ServiceException { return query(ROW+"WHERE q.issue_request_id=?",s->s.setString(1,request.toString()),QueueDao::row).stream().findFirst(); }
    @Override public Optional<QueueRow> activeStudent(long office,long student,LocalDate day) throws ServiceException { return query(ROW+"WHERE q.org_id=? AND q.student_id=? AND q.queue_date=? AND q.status IN ('waiting','called','serving','deferred')",s->{s.setLong(1,office);s.setLong(2,student);s.setObject(3,day);},QueueDao::row).stream().findFirst(); }
    @Override public long nextSequence(long office,LocalDate day) throws ServiceException {
        update("INSERT INTO queue_counters(org_id,queue_date,last_seq) VALUES(?,?,0) ON DUPLICATE KEY UPDATE last_seq=last_seq",s->{s.setLong(1,office);s.setObject(2,day);});
        long last=query("SELECT last_seq FROM queue_counters WHERE org_id=? AND queue_date=? FOR UPDATE",s->{s.setLong(1,office);s.setObject(2,day);},r->r.getLong(1)).getFirst();
        if(last>=99999999) throw new ValidationException("This office has reached its daily ticket limit.");
        update("UPDATE queue_counters SET last_seq=last_seq+1 WHERE org_id=? AND queue_date=?",s->{s.setLong(1,office);s.setObject(2,day);}); return last+1;
    }
    @Override public long issue(long office,long student,long term,LocalDate day,long seq,long actor,UUID request) throws ServiceException {
        return insert("INSERT INTO queue_tickets(org_id,student_id,term_id,queue_date,seq,issued_by,issue_request_id) VALUES(?,?,?,?,?,?,?)",s->{s.setLong(1,office);s.setLong(2,student);s.setLong(3,term);s.setObject(4,day);s.setLong(5,seq);s.setLong(6,actor);s.setString(7,request.toString());});
    }
    @Override public int expire(long office) throws ServiceException { return update("UPDATE queue_tickets SET status='cancelled',cancelled_at=CURRENT_TIMESTAMP(6),cancellation_reason='Daily queue rollover' WHERE org_id=? AND queue_date<CURRENT_DATE AND status IN ('waiting','called','serving','deferred')",s->s.setLong(1,office)); }
    @Override public Optional<QueueRow> occupied(long office) throws ServiceException { return query(ROW+"WHERE q.org_id=? AND q.status IN ('called','serving')",s->s.setLong(1,office),QueueDao::row).stream().findFirst(); }
    @Override public Optional<QueueRow> nextWaiting(long office) throws ServiceException {
        requireWritable(); var ids=query("SELECT id FROM queue_tickets WHERE org_id=? AND queue_date=CURRENT_DATE AND status='waiting' ORDER BY sort_at,id LIMIT 1 FOR UPDATE",s->s.setLong(1,office),r->r.getLong(1));
        return ids.isEmpty()?Optional.empty():find(ids.getFirst(),false);
    }
    @Override public void call(long id,long actor) throws ServiceException { update("UPDATE queue_tickets SET status='called',served_by=?,called_at=CURRENT_TIMESTAMP(6) WHERE id=?",s->{s.setLong(1,actor);s.setLong(2,id);}); }
    @Override public void transition(long id,QueueAction action,long actor,String reason) throws ServiceException {
        String set=switch(action) {
            case START_SERVING -> "status='serving',serving_at=CURRENT_TIMESTAMP(6),served_by=?";
            case COMPLETE -> "status='completed',completed_at=CURRENT_TIMESTAMP(6)";
            case DEFER -> "status='deferred',defer_count=defer_count+1";
            case REQUEUE -> "status='waiting',sort_at=CURRENT_TIMESTAMP(6),served_by=NULL,called_at=NULL,serving_at=NULL";
            case CANCEL -> "status='cancelled',cancelled_at=CURRENT_TIMESTAMP(6),cancellation_reason=?";
        };
        update("UPDATE queue_tickets SET "+set+" WHERE id=?",s->{int n=1;if(action==QueueAction.START_SERVING)s.setLong(n++,actor);if(action==QueueAction.CANCEL)s.setString(n++,reason);s.setLong(n,id);});
    }
    @Override public QueueSnapshot snapshot(long office,String status,int page) throws ServiceException {
        String filter=status.equals("active")?" AND q.status IN ('waiting','called','serving','deferred')":status.equals("all")?"":" AND q.status=?";
        var rows=query(ROW+"WHERE q.org_id=? AND q.queue_date=CURRENT_DATE"+filter+" ORDER BY q.sort_at,q.id LIMIT 26 OFFSET ?",s->{int n=1;s.setLong(n++,office);if(!status.equals("active")&&!status.equals("all"))s.setString(n++,status);s.setInt(n,page*25);},QueueDao::row);
        long expired=query("SELECT COUNT(*) FROM queue_tickets WHERE org_id=? AND queue_date<CURRENT_DATE AND status IN ('waiting','called','serving','deferred')",s->s.setLong(1,office),r->r.getLong(1)).getFirst();
        return new QueueSnapshot(clock(),rows.subList(0,Math.min(25,rows.size())),occupied(office).orElse(null),expired,rows.size()>25);
    }
    @Override public List<QueueRow> studentTickets(long student) throws ServiceException { return query(ROW+"WHERE q.student_id=? AND q.queue_date=CURRENT_DATE ORDER BY q.id DESC LIMIT 200",s->s.setLong(1,student),QueueDao::row); }
    @Override public List<BoardRow> board() throws ServiceException { return query("""
        SELECT o.id,o.code,o.name,o.is_active,o.queue_open,q.seq,q.status,
          (SELECT COUNT(*) FROM queue_tickets w WHERE w.org_id=o.id AND w.queue_date=CURRENT_DATE AND w.status='waiting') waiting
        FROM organizations o LEFT JOIN queue_tickets q ON q.org_id=o.id AND q.queue_date=CURRENT_DATE AND q.status IN ('called','serving') ORDER BY o.id
        """,s->{},r->{long seq=r.getLong("seq");String number=r.wasNull()?"—":r.getString("code")+"-"+String.format(Locale.ROOT,"%03d",seq);return new BoardRow(r.getLong("id"),OfficeCode.valueOf(r.getString("code")),r.getString("name"),r.getBoolean("is_active"),r.getBoolean("queue_open"),number,r.getString("status")==null?"Idle":r.getString("status"),r.getLong("waiting"));}); }
    @Override public List<QueueCallEvent> calls(long student) throws ServiceException { return query("""
        SELECT a.id, CAST(a.record_key AS UNSIGNED) ticket_id,o.code,
          JSON_UNQUOTE(JSON_EXTRACT(a.new_values,'$.queue_date')) queue_date,
          JSON_UNQUOTE(JSON_EXTRACT(a.new_values,'$.seq')) seq,
          JSON_UNQUOTE(JSON_EXTRACT(a.new_values,'$.called_at')) called_at
        FROM audit_logs a JOIN organizations o ON o.id=CAST(JSON_UNQUOTE(JSON_EXTRACT(a.new_values,'$.org_id')) AS UNSIGNED)
        WHERE a.queue_call_student_id=? AND a.occurred_at>=CURRENT_DATE ORDER BY a.occurred_at DESC,a.id DESC LIMIT 1000
        """,s->s.setLong(1,student),r->new QueueCallEvent(r.getLong("id"),r.getLong("ticket_id"),OfficeCode.valueOf(r.getString("code")),LocalDate.parse(r.getString("queue_date")),r.getLong("seq"),java.time.LocalDateTime.parse(r.getString("called_at").replace(' ','T')))); }
}
