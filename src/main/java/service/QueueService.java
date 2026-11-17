package service;

import dao.*;
import java.util.*;
import model.*;

/** Office row serializes all queue mutations, including recovery and daily rollover. */
public final class QueueService {
    private final UnitOfWork transactions;
    private final AuthService auth;
    public QueueService(UnitOfWork transactions,AuthService auth) { this.transactions=Objects.requireNonNull(transactions);this.auth=Objects.requireNonNull(auth); }
    private static boolean terminal(QueueStatus status) { return status==QueueStatus.COMPLETED||status==QueueStatus.CANCELLED; }
    private static void officeScope(User actor,long office) throws AuthorizationException {
        if(actor.role()==Role.OFFICER&&!Objects.equals(actor.orgId(),office))throw new AuthorizationException("Use your assigned office queue.");
    }
    private User writer(DaoContext c,Session session,long office) throws ServiceException {
        User actor=auth.require(c,session,Role.ADMIN,Role.OFFICER,Role.STUDENT);officeScope(actor,office);return actor;
    }
    private Organization lockOffice(DaoContext c,long office) throws ServiceException { return c.organizations().lockById(office).orElseThrow(()->new NotFoundException("Office")); }
    private <T>T write(UnitOfWork.Work<T> work) throws ServiceException {
        for(int attempt=0;;attempt++) {
            try { return transactions.write(null,work); }
            catch(TransactionConflictException|QueueDateChangedException ex) { if(attempt>=2)throw ex; }
        }
    }
    private static void sameDay(DaoContext c,QueueClock clock) throws ServiceException {
        if(!c.queue().clock().day().equals(clock.day()))throw new QueueDateChangedException(null);
    }
    private static void replayScope(User actor,QueueRow row,long office) throws ServiceException {
        if(row.ticket().orgId()!=office || row.ticket().issuedBy()!=actor.id()
                || actor.role()==Role.STUDENT&&!Objects.equals(actor.studentId(),row.ticket().studentId()))
            throw new AuthorizationException("This request belongs to another account or office.");
    }
    public List<Organization> offices(Session session) throws ServiceException {
        return transactions.read(c->{User actor=auth.require(c,session,Role.ADMIN,Role.OFFICER,Role.STUDENT);
            return c.organizations().list().stream().filter(o->actor.role()!=Role.OFFICER||Objects.equals(actor.orgId(),o.id())).toList();});
    }
    public QueueRow issue(Session session,long office,String studentNumber,UUID request) throws ServiceException {
        if(request==null||office<=0)throw new ValidationException("Choose an office for the ticket.");
        return write(c->{
            writer(c,session,office);Organization organization=lockOffice(c,office);User actor=writer(c,session,office);c.auditActor(actor.id());
            var replay=c.queue().byRequest(request);if(replay.isPresent()){replayScope(actor,replay.get(),office);return replay.get();}
            if(!organization.active()||!organization.queueOpen())throw new ValidationException("This office is closed for new tickets.");
            QueueClock clock=c.queue().clock();
            Term term=c.queue().currentTerm(true).orElseThrow(()->new ValidationException("An administrator must select the current active term before issuing tickets."));
            Student observed;
            if(actor.role()==Role.STUDENT)observed=c.students().findById(actor.studentId()).orElseThrow(()->new NotFoundException("Student"));
            else observed=c.students().findByNumber(Validation.required(studentNumber,"Student number",32)).orElseThrow(()->new NotFoundException("Student number"));
            Student student=c.students().lockById(observed.id()).orElseThrow(()->new NotFoundException("Student"));
            actor=writer(c,session,office);
            if(actor.role()==Role.STUDENT&&!Objects.equals(actor.studentId(),student.id()))throw new AuthorizationException("Use your own student record.");
            if(!observed.studentNumber().equals(student.studentNumber()))throw new RecordChangedException();
            if(!student.active())throw new ValidationException("Tickets require an active student record.");
            c.queue().expire(office);
            var active=c.queue().activeStudent(office,student.id(),clock.day());
            if(active.isPresent())throw new ValidationException("An active ticket already exists: "+active.get().number()+". View that ticket before requesting another.");
            long sequence=c.queue().nextSequence(office,clock.day());
            long id=c.queue().issue(office,student.id(),term.id(),clock.day(),sequence,actor.id(),request);
            sameDay(c,clock);return c.queue().find(id,false).orElseThrow(()->new NotFoundException("Ticket"));
        });
    }
    public Optional<QueueRow> recoverIssue(Session session,long office,UUID request) throws ServiceException {
        if(request==null)throw new ValidationException("The request could not be checked.");
        return write(c->{writer(c,session,office);lockOffice(c,office);User actor=writer(c,session,office);
            var row=c.queue().byRequest(request);if(row.isPresent())replayScope(actor,row.get(),office);return row;});
    }
    public Optional<QueueRow> callNext(Session session,long office) throws ServiceException {
        return write(c->{auth.require(c,session,Role.ADMIN,Role.OFFICER);lockOffice(c,office);
            User actor=auth.require(c,session,Role.ADMIN,Role.OFFICER);officeScope(actor,office);c.auditActor(actor.id());
            QueueClock clock=c.queue().clock();c.queue().expire(office);
            if(c.queue().occupied(office).isPresent())throw new ValidationException("Complete or defer the current ticket before calling another.");
            var next=c.queue().nextWaiting(office);if(next.isEmpty()){sameDay(c,clock);return Optional.empty();}
            c.queue().call(next.get().ticket().id(),actor.id());sameDay(c,clock);return c.queue().find(next.get().ticket().id(),false);
        });
    }
    public int closePreviousDay(Session session,long office) throws ServiceException {
        return write(c->{auth.require(c,session,Role.ADMIN,Role.OFFICER);lockOffice(c,office);User actor=auth.require(c,session,Role.ADMIN,Role.OFFICER);officeScope(actor,office);c.auditActor(actor.id());return c.queue().expire(office);});
    }
    public QueueRow transition(Session session,QueueRow original,QueueAction action,String reason) throws ServiceException {
        if(original==null||original.ticket()==null||action==null)throw new ValidationException("Select a ticket first.");
        String checkedReason=action==QueueAction.CANCEL?Validation.required(reason,"Cancellation reason",500):null;
        return write(c->{
            long office=original.ticket().orgId();writer(c,session,office);lockOffice(c,office);
            if(action==QueueAction.REQUEUE){QueueRow observed=c.queue().find(original.ticket().id(),false).orElseThrow(()->new NotFoundException("Ticket"));
                if(observed.ticket().orgId()!=office)throw new RecordChangedException();
                Student student=c.students().lockById(observed.ticket().studentId()).orElseThrow(()->new NotFoundException("Student"));if(!student.active())throw new ValidationException("Requeue requires an active student record.");}
            QueueRow row=c.queue().find(original.ticket().id(),true).orElseThrow(()->new NotFoundException("Ticket"));
            User actor=writer(c,session,office);QueueTicket ticket=row.ticket();Validation.unchanged(original.ticket().updatedAt(),ticket.updatedAt());
            if(ticket.orgId()!=office)throw new RecordChangedException();
            if(actor.role()==Role.STUDENT){
                if(!Objects.equals(actor.studentId(),ticket.studentId()) || !(action==QueueAction.REQUEUE || action==QueueAction.CANCEL&&(ticket.status()==QueueStatus.WAITING||ticket.status()==QueueStatus.DEFERRED)))
                    throw new AuthorizationException("Students can requeue their deferred tickets or cancel their waiting and deferred tickets.");
            }
            QueueClock clock=c.queue().clock();
            if(action!=QueueAction.CANCEL&&!ticket.queueDate().equals(clock.day()))throw new ValidationException("This ticket is from a previous day. Close the previous day's tickets and issue a new ticket.");
            boolean allowed=switch(action){
                case START_SERVING->ticket.status()==QueueStatus.CALLED;
                case COMPLETE->ticket.status()==QueueStatus.SERVING;
                case DEFER->ticket.status()==QueueStatus.CALLED||ticket.status()==QueueStatus.SERVING;
                case REQUEUE->ticket.status()==QueueStatus.DEFERRED;
                case CANCEL->!terminal(ticket.status());
            };
            if(!allowed)throw new ValidationException("That action is unavailable for a "+ticket.status().displayName().toLowerCase(Locale.ROOT)+" ticket. Refresh the queue.");
            if(action==QueueAction.COMPLETE && actor.role()!=Role.ADMIN&&!Objects.equals(ticket.servedBy(),actor.id()))throw new AuthorizationException("Only the serving officer or an administrator can complete this ticket.");
            c.auditActor(actor.id());c.queue().transition(ticket.id(),action,actor.id(),checkedReason);
            if(action!=QueueAction.CANCEL)sameDay(c,clock);
            return c.queue().find(ticket.id(),false).orElseThrow(()->new NotFoundException("Ticket"));
        });
    }
    public QueueSnapshot officeSnapshot(Session session,long office,String status,int page) throws ServiceException {
        if(status==null||!Set.of("active","all","waiting","called","serving","deferred","completed","cancelled").contains(status))throw new ValidationException("Choose a queue status.");Validation.page(page,25);
        return transactions.read(c->{User actor=auth.require(c,session,Role.ADMIN,Role.OFFICER);officeScope(actor,office);return c.queue().snapshot(office,status,page);});
    }
    public List<BoardRow> board(Session session) throws ServiceException {
        return transactions.read(c->{auth.require(c,session,Role.ADMIN,Role.OFFICER,Role.CASHIER);return c.queue().board();});
    }
    public StudentQueueSnapshot studentSnapshot(Session session) throws ServiceException {
        return transactions.read(c->{User actor=auth.require(c,session,Role.STUDENT);
            Student student=c.students().findById(actor.studentId()).orElseThrow(()->new NotFoundException("Student"));
            QueueClock before=c.queue().clock();var board=c.queue().board();var tickets=c.queue().studentTickets(student.id());var calls=c.queue().calls(student.id());
            Term term=c.queue().currentTerm(false).orElse(null);var clearance=term==null?List.<ClearanceRow>of():c.clearance().rows(student.id(),term.id(),null);
            sameDay(c,before);return new StudentQueueSnapshot(before,student,term,board,tickets,calls,clearance);});
    }
}
