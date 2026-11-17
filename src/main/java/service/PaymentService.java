package service;

import dao.UnitOfWork;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import model.*;

public final class PaymentService {
    private final UnitOfWork transactions;
    private final AuthService auth;
    public PaymentService(UnitOfWork transactions, AuthService auth) { this.transactions = transactions; this.auth = auth; }

    public FinanceReferences references(Session session) throws ServiceException {
        return transactions.read(context -> { auth.require(context, session, Role.ADMIN, Role.CASHIER); return new FinanceReferences(context.organizations().list(), context.finance().terms()); });
    }

    public Page<PaymentRow> search(Session session, PaymentFilter input) throws ServiceException {
        if (input == null) { throw new ValidationException("Enter payment search filters."); }
        Validation.page(input.page(), input.pageSize());
        if (input.from() != null && input.through() != null && input.from().isAfter(input.through())) { throw new ValidationException("The From date cannot be later than the Through date."); }
        for (java.time.LocalDate date : new java.time.LocalDate[]{input.from(), input.through()}) {
            if (date != null && (date.getYear() < 1000 || date.getYear() > 9999)) { throw new ValidationException("Payment dates must have years from 1000 to 9999."); }
        }
        PaymentFilter filter = new PaymentFilter(Validation.filter(input.query(), "Receipt or student number", 100), input.orgId(), input.termId(), input.voided(), input.from(), input.through(), input.page(), input.pageSize());
        return transactions.read(context -> {
            auth.require(context, session, Role.ADMIN, Role.CASHIER);
            List<PaymentRow> rows = context.finance().searchPayments(filter);
            return new Page<>(rows.subList(0, Math.min(rows.size(), filter.pageSize())), filter.page(), filter.pageSize(), rows.size() > filter.pageSize());
        });
    }

    public Payment record(Session session, long fineId, BigDecimal inputAmount, String inputReference) throws ServiceException {
        BigDecimal amount = Money.positive(inputAmount);
        String reference = Validation.required(inputReference, "Receipt reference", 64).toUpperCase(Locale.ROOT);
        if (!reference.matches("[A-Z0-9][A-Z0-9._/-]{2,63}")) { throw new ValidationException("Use a receipt reference of 3 to 64 letters, digits, dots, underscores, slashes or hyphens."); }
        return transactions.write(null, context -> {
            User actor = auth.require(context, session, Role.ADMIN, Role.CASHIER);
            Fine observed = context.finance().findFine(fineId).orElseThrow(() -> new NotFoundException("Fine"));
            context.students().lockById(observed.studentId()).orElseThrow(() -> new NotFoundException("Student"));
            Fine fine = context.finance().lockFine(fineId).orElseThrow(() -> new NotFoundException("Fine"));
            actor = auth.require(context, session, Role.ADMIN, Role.CASHIER); context.auditActor(actor.id());
            Payment existing = context.finance().findByReceipt(reference).orElse(null);
            if (existing != null) {
                if (existing.fineId() == fineId && existing.amount().compareTo(amount) == 0 && existing.receivedBy() == actor.id() && existing.voidedAt() == null) { return existing; }
                throw new ValidationException("That receipt reference is already in use, including voided receipts. Check Payments before proceeding.");
            }
            if (fine.waivedAt() != null) { throw new ValidationException("Payments cannot be accepted against a waived fine."); }
            BigDecimal balance = fine.amount().subtract(context.finance().paidAmount(fine.id()));
            if (balance.signum() <= 0 || amount.compareTo(balance) > 0) { throw new ValidationException("The payment exceeds the current remaining balance. Refresh the fine and try again."); }
            long id = context.finance().insertPayment(fine.id(), amount, reference, actor.id(), balance.subtract(amount));
            return context.finance().findPayment(id).orElseThrow(() -> new NotFoundException("Payment"));
        });
    }

    public void voidPayment(Session session, long paymentId, String inputReason) throws ServiceException {
        String reason = Validation.required(inputReason, "Void reason", 500);
        transactions.write(null, context -> {
            User actor = auth.require(context, session, Role.ADMIN);
            Payment observed = context.finance().findPayment(paymentId).orElseThrow(() -> new NotFoundException("Payment"));
            Fine observedFine = context.finance().findFine(observed.fineId()).orElseThrow(() -> new NotFoundException("Fine"));
            context.students().lockById(observedFine.studentId()).orElseThrow(() -> new NotFoundException("Student"));
            Fine fine = context.finance().lockFine(observed.fineId()).orElseThrow(() -> new NotFoundException("Fine"));
            Payment payment = context.finance().lockPayment(paymentId).orElseThrow(() -> new NotFoundException("Payment"));
            actor = auth.require(context, session, Role.ADMIN); context.auditActor(actor.id());
            if (payment.voidedAt() != null) { throw new ValidationException("This payment has already been voided."); }
            context.finance().voidPayment(payment.id(), actor.id(), reason);
            FinancialRules.reopenClearance(context, fine, "Balance reopened after payment void: " + payment.receiptReference());
            return null;
        });
    }

    public ReceiptData receipt(Session session, long id) throws ServiceException {
        return transactions.read(context -> {
            auth.require(context, session, Role.ADMIN, Role.CASHIER);
            context.finance().findPayment(id).orElseThrow(() -> new NotFoundException("Payment"));
            if (!context.finance().archivedReceipt(id)) { throw new ValidationException("This legacy payment has no archived receipt. Contact the administrator."); }
            return context.finance().receipt(id);
        });
    }
}
