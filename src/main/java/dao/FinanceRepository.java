package dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import model.*;
import service.ServiceException;

/** Financial ledger and its clearance/academic-period consistency boundary. */
public interface FinanceRepository {
    List<FineRow> searchFines(FineFilter filter) throws ServiceException;
    Optional<Fine> findFine(long id) throws ServiceException;
    Optional<Fine> lockFine(long id) throws ServiceException;
    FineRow fineRow(long id) throws ServiceException;
    BigDecimal paidAmount(long fineId) throws ServiceException;
    long insertFine(FineDraft draft, long studentId, long actor) throws ServiceException;
    void updateFine(long id, String reason, BigDecimal amount) throws ServiceException;
    void waiveFine(long id, long actor, String reason) throws ServiceException;
    List<PaymentRow> searchPayments(PaymentFilter filter) throws ServiceException;
    Optional<Payment> findPayment(long id) throws ServiceException;
    Optional<Payment> findByReceipt(String reference) throws ServiceException;
    Optional<Payment> lockPayment(long id) throws ServiceException;
    long insertPayment(long fineId, BigDecimal amount, String reference, long actor, BigDecimal balanceAfter) throws ServiceException;
    void voidPayment(long id, long actor, String reason) throws ServiceException;
    ReceiptData receipt(long paymentId) throws ServiceException;
    boolean archivedReceipt(long paymentId) throws ServiceException;
    Optional<ClearanceRecord> lockClearance(long studentId, long orgId, long termId) throws ServiceException;
    void holdClearance(long id, String reason) throws ServiceException;
    List<Term> terms() throws ServiceException;
    List<Term> lockTerms() throws ServiceException;
    Optional<Term> lockTermShared(long id) throws ServiceException;
    Optional<Term> findTerm(long id) throws ServiceException;
    Optional<Term> findTermByCode(String code) throws ServiceException;
    long insertTerm(TermDraft draft) throws ServiceException;
    void updateTerm(long id, TermDraft draft) throws ServiceException;
    void clearCurrentTerm(long id) throws ServiceException;
    boolean termHasActiveTickets(long id) throws ServiceException;
    boolean termHasRecords(long id) throws ServiceException;
}
