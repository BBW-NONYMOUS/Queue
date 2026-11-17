package service;

import dao.UnitOfWork;
import java.util.List;
import java.util.Objects;
import model.*;

public final class OfficeService {
    private final UnitOfWork transactions;
    private final AuthService auth;

    public OfficeService(UnitOfWork transactions, AuthService auth) { this.transactions = transactions; this.auth = auth; }

    public List<Organization> list(Session session) throws ServiceException {
        return transactions.read(context -> {
            User actor = auth.require(context, session, Role.ADMIN, Role.OFFICER);
            return context.organizations().list().stream()
                    .filter(office -> actor.role() == Role.ADMIN || Objects.equals(actor.orgId(), office.id())).toList();
        });
    }

    public Organization save(Session session, Organization original, OfficeDraft input) throws ServiceException {
        if (original == null || input == null) { throw new ValidationException("Select an office to edit."); }
        OfficeDraft draft = new OfficeDraft(Validation.required(input.name(), "Office name", 120), input.queueOpen(), input.active());
        if (draft.queueOpen() && !draft.active()) { throw new ValidationException("An inactive office cannot open its queue."); }
        return transactions.write(null, context -> {
            User actor = auth.require(context, session, Role.ADMIN, Role.OFFICER);
            if (actor.role() == Role.OFFICER && !Objects.equals(actor.orgId(), original.id())) { throw new AuthorizationException("You can update only your assigned office."); }
            context.auditActor(actor.id());
            Organization current = context.organizations().lockById(original.id()).orElseThrow(() -> new NotFoundException("Office"));
            // A reassignment waits on this same office lock; recheck after acquiring it.
            auth.require(context, session, Role.ADMIN, Role.OFFICER);
            Validation.unchanged(original.updatedAt(), current.updatedAt());
            if (actor.role() == Role.OFFICER && (!draft.name().equals(current.name()) || draft.active() != current.active())) {
                throw new AuthorizationException("Officers can change queue availability only.");
            }
            if (current.active() && !draft.active() && context.organizations().hasActiveTickets(current.id())) {
                throw new ValidationException("Complete or cancel this office's active tickets before deactivation.");
            }
            context.organizations().update(current.id(), draft);
            return context.organizations().findById(current.id()).orElseThrow(() -> new NotFoundException("Office"));
        });
    }
}
