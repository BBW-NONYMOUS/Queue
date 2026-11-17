package service;
import dao.*;
import model.*;
import java.util.*;
public final class ClearanceService {
    private final UnitOfWork transactions;private final AuthService auth;
    public ClearanceService(UnitOfWork transactions,AuthService auth){this.transactions=transactions;this.auth=auth;}
    public FinanceReferences references(Session session)throws ServiceException {
        return transactions.read(c->{User actor=auth.require(c,session,Role.values());return new FinanceReferences(c.organizations().list().stream().filter(o->actor.role()!=Role.OFFICER||Objects.equals(actor.orgId(),o.id())).toList(),c.finance().terms());});
    }
    public List<ClearanceRow> find(Session session,String number,long term)throws ServiceException {
        return transactions.snapshot(c->{User actor=auth.require(c,session,Role.values());
            Student student=actor.role()==Role.STUDENT?c.students().findById(actor.studentId()).orElseThrow(()->new NotFoundException("Student")):
                c.students().findByNumber(Validation.required(number,"Student number",32)).orElseThrow(()->new NotFoundException("Student number"));
            c.finance().findTerm(term).orElseThrow(()->new NotFoundException("Term"));
            return c.clearance().rows(student.id(),term,actor.role()==Role.OFFICER?actor.orgId():null);
        });
    }
    public ClearanceRow decide(Session session,ClearanceRow reviewed,ClearanceDraft input)throws ServiceException {
        if(reviewed==null||input==null||input.status()==null)throw new ValidationException("Select an office and a clearance status.");
        String remarks=input.status()==ClearanceStatus.ON_HOLD?Validation.required(input.remarks(),"Hold reason",500):Validation.optional(input.remarks(),"Remarks",500);
        String reason=input.balanceException()?Validation.required(input.exceptionReason(),"Balance exception reason",500):null;
        if(input.balanceException()&&input.status()!=ClearanceStatus.CLEARED)throw new ValidationException("A balance exception applies only to a cleared decision.");
        return transactions.write(null,c->{
            User actor=auth.require(c,session,Role.ADMIN,Role.OFFICER);scope(actor,reviewed.office().id());
            c.organizations().lockSharedById(reviewed.office().id()).orElseThrow(()->new NotFoundException("Office"));
            // Match queue's office -> term -> student order. Shared term lock prevents concurrent closure.
            Term term=c.finance().lockTermShared(reviewed.term().id()).orElseThrow(()->new NotFoundException("Term"));
            Student student=c.students().lockById(reviewed.student().id()).orElseThrow(()->new NotFoundException("Student"));
            actor=auth.require(c,session,Role.ADMIN,Role.OFFICER);scope(actor,reviewed.office().id());
            ClearanceRow current=c.clearance().rows(student.id(),term.id(),reviewed.office().id()).getFirst();
            var record=c.finance().lockClearance(student.id(),current.office().id(),term.id()).orElse(null);
            if(reviewed.record()==null){if(record!=null)throw new RecordChangedException();}
            else if(record==null)throw new RecordChangedException();else Validation.unchanged(reviewed.record().updatedAt(),record.updatedAt());
            if(reviewed.balance().compareTo(current.balance())!=0)throw new RecordChangedException();
            if(!student.active()||!current.office().active()||!term.active())throw new ValidationException("Clearance decisions require an active student, office and term.");
            if(input.balanceException()&&actor.role()!=Role.ADMIN)throw new AuthorizationException("An administrator must approve a balance exception.");
            if(input.status()==ClearanceStatus.CLEARED&&current.balance().signum()>0&&!input.balanceException())throw new ValidationException("An unpaid balance remains. Collect payment or ask an administrator to record a reasoned exception.");
            if(input.balanceException()&&current.balance().signum()<=0)throw new ValidationException("There is no outstanding balance requiring an exception.");
            c.auditActor(actor.id());c.clearance().save(student.id(),current.office().id(),term.id(),new ClearanceDraft(input.status(),remarks,input.balanceException(),reason),actor.id());
            return c.clearance().rows(student.id(),term.id(),current.office().id()).getFirst();
        });
    }
    private static void scope(User actor,long office)throws AuthorizationException{if(actor.role()==Role.OFFICER&&!Objects.equals(actor.orgId(),office))throw new AuthorizationException("Use your assigned office's clearance records.");}
}
