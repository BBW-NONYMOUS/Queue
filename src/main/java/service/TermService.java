package service;

import dao.UnitOfWork;
import java.util.List;
import model.*;

/** Small admin catalogue needed to activate registrar-confirmed financial terms. */
public final class TermService {
    private final UnitOfWork transactions;
    private final AuthService auth;
    public TermService(UnitOfWork transactions, AuthService auth) { this.transactions = transactions; this.auth = auth; }
    public List<Term> list(Session session) throws ServiceException { return transactions.read(context -> { auth.require(context, session, Role.ADMIN); return context.finance().terms(); }); }
    public Term save(Session session, Term original, TermDraft input) throws ServiceException {
        if (input == null || input.startsOn() == null || input.endsOn() == null || input.endsOn().isBefore(input.startsOn())) { throw new ValidationException("Enter valid start/end dates with the end on or after the start."); }
        if (input.startsOn().getYear() < 1000 || input.endsOn().getYear() > 9999) { throw new ValidationException("Term dates must have years from 1000 to 9999."); }
        if (input.current() && !input.active()) { throw new ValidationException("The current term must be active."); }
        TermDraft draft = new TermDraft(Validation.required(input.code(), "Term code", 32), Validation.required(input.name(), "Term name", 120), input.startsOn(), input.endsOn(), input.active(), input.current());
        return transactions.write(null, context -> {
            auth.require(context, session, Role.ADMIN);
            List<Term> locked = context.finance().lockTerms();
            User actor = auth.require(context, session, Role.ADMIN); context.auditActor(actor.id());
            Term current = original == null ? null : locked.stream().filter(t -> t.id() == original.id()).findFirst().orElseThrow(() -> new NotFoundException("Term"));
            if (current != null) {
                Validation.unchanged(original.updatedAt(), current.updatedAt());
                if ((!draft.active() || (current.current() && !draft.current())) && context.finance().termHasActiveTickets(current.id())) { throw new ValidationException("Complete or cancel this term's active tickets before closing or switching it."); }
                if ((!current.code().equals(draft.code()) || !current.startsOn().equals(draft.startsOn()) || !current.endsOn().equals(draft.endsOn())) && context.finance().termHasRecords(current.id())) { throw new ValidationException("A term with records keeps its code and dates. Create a new term instead."); }
            }
            Term duplicate = context.finance().findTermByCode(draft.code()).orElse(null);
            if (duplicate != null && (current == null || duplicate.id() != current.id())) { throw new ValidationException("That term code is already in use."); }
            if (draft.current()) {
                for (Term term : locked) {
                    if (term.current() && (current == null || term.id() != current.id())) {
                        if (context.finance().termHasActiveTickets(term.id())) { throw new ValidationException("Complete or cancel the current term's active tickets before switching terms."); }
                        context.finance().clearCurrentTerm(term.id());
                    }
                }
            }
            long id;
            if (current == null) { id = context.finance().insertTerm(draft); }
            else { id = current.id(); context.finance().updateTerm(id, draft); }
            return context.finance().findTerm(id).orElseThrow(() -> new NotFoundException("Term"));
        });
    }
}
