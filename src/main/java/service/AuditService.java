package service;
import dao.UnitOfWork;
import model.*;
import java.time.*;
import java.util.*;
public final class AuditService {
    public static final List<String> TABLES=List.of("users","students","organizations","terms","fines","payments","queue_tickets","queue_counters","clearance_records");
    private final UnitOfWork transactions;private final AuthService auth;
    public AuditService(UnitOfWork transactions,AuthService auth){this.transactions=transactions;this.auth=auth;}
    public Page<AuditRow> search(Session session,AuditFilter input)throws ServiceException {
        if(input==null)throw new ValidationException("Enter audit filters.");Validation.page(input.page(),50);
        String table=Validation.filter(input.table(),"Table",64),actor=Validation.filter(input.actor(),"Actor username",64),key=Validation.filter(input.recordKey(),"Record key",100);
        if(!table.isEmpty()&&!TABLES.contains(table))throw new ValidationException("Choose a listed audit table.");
        return transactions.read(c->{auth.require(c,session,Role.ADMIN);LocalDate today=c.queue().clock().day();LocalDate from=input.from()==null?today.minusDays(29):input.from(),through=input.through()==null?today:input.through();
            ReportService.dates(from,through);if(java.time.temporal.ChronoUnit.DAYS.between(from,through)>=366)throw new ValidationException("Search at most 366 days of audit entries at a time.");
            var rows=c.oversight().audit(new AuditFilter(table,actor,key,from,through,input.page()));return new Page<>(rows.subList(0,Math.min(50,rows.size())),input.page(),50,rows.size()>50);
        });
    }
    public AuditRow details(Session session,long id)throws ServiceException{return transactions.read(c->{auth.require(c,session,Role.ADMIN);return c.oversight().auditEntry(id).orElseThrow(()->new NotFoundException("Audit entry"));});}
}
