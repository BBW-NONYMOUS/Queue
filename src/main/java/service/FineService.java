package service;

import dao.UnitOfWork;
import java.util.List;
import model.*;

public final class FineService {
    private final UnitOfWork transactions;
    private final AuthService auth;
    public FineService(UnitOfWork transactions, AuthService auth) { this.transactions = transactions; this.auth = auth; }

    public FinanceReferences references(Session session) throws ServiceException {
        return transactions.read(context -> {
            User actor = auth.require(context, session, Role.ADMIN, Role.OFFICER, Role.CASHIER);
            return new FinanceReferences(context.organizations().list().stream()
                    .filter(o -> actor.role() != Role.OFFICER || java.util.Objects.equals(actor.orgId(), o.id())).toList(), context.finance().terms());
        });
    }

    public Page<FineRow> search(Session session, FineFilter input) throws ServiceException {
        if (input == null) { throw new ValidationException("Enter fine search filters."); }
        Validation.page(input.page(), input.pageSize());
        String number = Validation.filter(input.studentNumber(), "Student number", 32);
        return transactions.read(context -> {
            User actor = auth.require(context, session, Role.ADMIN, Role.OFFICER, Role.CASHIER);
            Long office = input.orgId();
            if (actor.role() == Role.OFFICER) {
                if (office != null) { FinancialRules.officeScope(actor, office); }
                office = actor.orgId();
            }
            List<FineRow> rows = context.finance().searchFines(new FineFilter(number, office, input.termId(), input.status(), input.page(), input.pageSize()));
            return new Page<>(rows.subList(0, Math.min(rows.size(), input.pageSize())), input.page(), input.pageSize(), rows.size() > input.pageSize());
        });
    }

    public FineRow save(Session session, FineRow original, FineDraft input) throws ServiceException {
        if (input == null || input.orgId() <= 0 || input.termId() <= 0) { throw new ValidationException("Choose the office and term."); }
        FineDraft draft = new FineDraft(Validation.required(input.studentNumber(), "Student number", 32), input.orgId(), input.termId(),
                Validation.required(input.reason(), "Fine reason", 500), Money.positive(input.amount()));
        return transactions.write(null, context -> {
            User actor = auth.require(context, session, Role.ADMIN, Role.OFFICER);
            FinancialRules.officeScope(actor, draft.orgId()); context.auditActor(actor.id());
            Student observed = context.students().findByNumber(draft.studentNumber()).orElseThrow(() -> new NotFoundException("Student number"));
            Student student = context.students().lockById(observed.id()).orElseThrow(() -> new NotFoundException("Student"));
            if (!student.studentNumber().equals(observed.studentNumber())) { throw new RecordChangedException(); }
            actor = auth.require(context, session, Role.ADMIN, Role.OFFICER); FinancialRules.officeScope(actor, draft.orgId());
            if (!student.active()) { throw new ValidationException("New or edited fines require an active student record."); }
            Fine fine = null;
            if (original != null) {
                fine = context.finance().lockFine(original.fine().id()).orElseThrow(() -> new NotFoundException("Fine"));
                FinancialRules.officeScope(actor, fine.orgId()); Validation.unchanged(original.fine().updatedAt(), fine.updatedAt());
                if (fine.studentId() != student.id() || fine.orgId() != draft.orgId() || fine.termId() != draft.termId()) { throw new ValidationException("A fine's student, office and term cannot be reassigned."); }
                if (fine.waivedAt() != null) { throw new ValidationException("A waived fine cannot be edited."); }
                if (context.finance().paidAmount(fine.id()).compareTo(draft.amount()) > 0) { throw new ValidationException("The fine amount cannot be lower than its non-voided payments."); }
            }
            Organization office = context.organizations().findById(draft.orgId()).orElseThrow(() -> new NotFoundException("Office"));
            Term term = context.finance().findTerm(draft.termId()).orElseThrow(() -> new NotFoundException("Term"));
            if (!office.active() || !term.active()) { throw new ValidationException("New or edited fines require an active office and term."); }
            long id;
            if (fine == null) { id = context.finance().insertFine(draft, student.id(), actor.id()); }
            else { id = fine.id(); context.finance().updateFine(id, draft.reason(), draft.amount()); }
            Fine current = context.finance().findFine(id).orElseThrow(() -> new NotFoundException("Fine"));
            FinancialRules.reopenClearance(context, current, "Outstanding balance after fine assessment or amendment.");
            return context.finance().fineRow(id);
        });
    }

    public void waive(Session session, FineRow original, String inputReason) throws ServiceException {
        if (original == null || original.fine() == null || original.balance() == null) { throw new ValidationException("Select a fine to waive."); }
        String reason = Validation.required(inputReason, "Waiver reason", 500);
        transactions.write(null, context -> {
            User actor = auth.require(context, session, Role.ADMIN, Role.OFFICER);
            Fine observed = context.finance().findFine(original.fine().id()).orElseThrow(() -> new NotFoundException("Fine"));
            FinancialRules.officeScope(actor, observed.orgId());
            context.students().lockById(observed.studentId()).orElseThrow(() -> new NotFoundException("Student"));
            Fine fine = context.finance().lockFine(observed.id()).orElseThrow(() -> new NotFoundException("Fine"));
            actor = auth.require(context, session, Role.ADMIN, Role.OFFICER); FinancialRules.officeScope(actor, fine.orgId());
            Validation.unchanged(original.fine().updatedAt(), fine.updatedAt());
            java.math.BigDecimal remaining = fine.amount().subtract(context.finance().paidAmount(fine.id()));
            if (fine.waivedAt() != null || remaining.signum() <= 0) { throw new ValidationException("This fine has no remaining balance to waive."); }
            if (remaining.compareTo(original.balance()) != 0) { throw new RecordChangedException(); }
            context.auditActor(actor.id()); context.finance().waiveFine(fine.id(), actor.id(), reason);
            return null;
        });
    }
}
