package dao;
import java.sql.*;
import java.time.*;
import java.util.*;
import model.*;
import service.ServiceException;
final class ClearanceDao extends BaseDao implements ClearanceRepository {
    ClearanceDao(Connection connection,boolean writable){super(connection,writable);}
    @Override public List<ClearanceRow> rows(long student,long term,Long office) throws ServiceException {
        return query("""
            SELECT s.*,o.id office_id,o.code office_code,o.name office_name,o.queue_open,o.is_active office_active,
              o.created_at office_created,o.updated_at office_updated,t.id term_id,t.code term_code,t.name term_name,
              t.starts_on,t.ends_on,t.is_current,t.is_active term_active,t.created_at term_created,t.updated_at term_updated,
              c.id clearance_id,c.status,c.remarks,c.cleared_by,c.cleared_at,c.balance_exception,c.exception_reason,
              c.exception_by,c.exception_at,c.created_at clearance_created,c.updated_at clearance_updated,
              COALESCE((SELECT SUM(b.balance) FROM v_fine_balances b WHERE b.student_id=s.id AND b.org_id=o.id AND b.term_id=t.id),0.00) balance
            FROM students s CROSS JOIN organizations o JOIN terms t ON t.id=?
            LEFT JOIN clearance_records c ON c.student_id=s.id AND c.org_id=o.id AND c.term_id=t.id
            WHERE s.id=? AND (? IS NULL OR o.id=?) ORDER BY o.id
            """,p->{p.setLong(1,term);p.setLong(2,student);p.setObject(3,office);p.setObject(4,office);},r->{
                Student st=new Student(r.getLong("id"),r.getString("student_number"),r.getString("first_name"),r.getString("middle_name"),r.getString("last_name"),r.getString("suffix"),r.getString("program"),r.getInt("year_level"),r.getString("section"),r.getBoolean("is_active"),time(r,"created_at"),time(r,"updated_at"));
                Organization org=new Organization(r.getLong("office_id"),OfficeCode.valueOf(r.getString("office_code")),r.getString("office_name"),r.getBoolean("queue_open"),r.getBoolean("office_active"),time(r,"office_created"),time(r,"office_updated"));
                Term period=new Term(r.getLong("term_id"),r.getString("term_code"),r.getString("term_name"),r.getObject("starts_on",LocalDate.class),r.getObject("ends_on",LocalDate.class),r.getBoolean("is_current"),r.getBoolean("term_active"),time(r,"term_created"),time(r,"term_updated"));
                Long id=r.getObject("clearance_id",Long.class);ClearanceRecord record=id==null?null:new ClearanceRecord(id,st.id(),org.id(),period.id(),ClearanceStatus.valueOf(r.getString("status").toUpperCase(Locale.ROOT)),r.getString("remarks"),r.getObject("cleared_by",Long.class),time(r,"cleared_at"),r.getBoolean("balance_exception"),r.getString("exception_reason"),r.getObject("exception_by",Long.class),time(r,"exception_at"),time(r,"clearance_created"),time(r,"clearance_updated"));
                return new ClearanceRow(st,org,period,record,r.getBigDecimal("balance"));
            });
    }
    private static LocalDateTime time(ResultSet r,String column)throws SQLException{return r.getObject(column,LocalDateTime.class);}
    @Override public void save(long student,long office,long term,ClearanceDraft d,long actor)throws ServiceException {
        update("""
            INSERT INTO clearance_records(student_id,org_id,term_id,status,remarks,cleared_by,cleared_at,balance_exception,exception_reason,exception_by,exception_at)
            VALUES(?,?,?,?,?,?,IF(? IS NULL,NULL,CURRENT_TIMESTAMP(6)),?,?,?,IF(? IS NULL,NULL,CURRENT_TIMESTAMP(6)))
            ON DUPLICATE KEY UPDATE status=?,remarks=?,cleared_by=?,cleared_at=IF(? IS NULL,NULL,CURRENT_TIMESTAMP(6)),
            balance_exception=?,exception_reason=?,exception_by=?,exception_at=IF(? IS NULL,NULL,CURRENT_TIMESTAMP(6))
            """,p->{Long clearer=d.status()==ClearanceStatus.CLEARED?actor:null;Long exception=d.balanceException()?actor:null;
                int n=1;p.setLong(n++,student);p.setLong(n++,office);p.setLong(n++,term);
                for(int repeat=0;repeat<2;repeat++){p.setString(n++,d.status().databaseValue());p.setString(n++,d.remarks());p.setObject(n++,clearer);p.setObject(n++,clearer);p.setBoolean(n++,d.balanceException());p.setString(n++,d.exceptionReason());p.setObject(n++,exception);p.setObject(n++,exception);}
            });
    }
}
