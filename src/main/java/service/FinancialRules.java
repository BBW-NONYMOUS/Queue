package service;

import dao.DaoContext;
import model.*;

final class FinancialRules {
    private FinancialRules() { }

    static void officeScope(User actor, long orgId) throws AuthorizationException {
        if (actor.role() == Role.OFFICER && !java.util.Objects.equals(actor.orgId(), orgId)) {
            throw new AuthorizationException("You can manage fines only for your assigned office.");
        }
    }

    static void reopenClearance(DaoContext context, Fine fine, String reason) throws ServiceException {
        if (fine.waivedAt() != null || context.finance().paidAmount(fine.id()).compareTo(fine.amount()) >= 0) { return; }
        ClearanceRecord record = context.finance().lockClearance(fine.studentId(), fine.orgId(), fine.termId()).orElse(null);
        if (record != null && record.status() == ClearanceStatus.CLEARED && !record.balanceException()) {
            context.finance().holdClearance(record.id(), reason);
        }
    }
}
