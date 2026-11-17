package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import model.*;
import service.DataAccessException;
import service.NotFoundException;
import service.ServiceException;

final class FinanceDao extends BaseDao implements FinanceRepository {
    private static final String FINE_COLUMNS = "f.id, f.student_id, f.org_id, f.term_id, f.reason, f.amount, f.assessed_by, f.waived_at, f.waived_by, f.waiver_reason, f.created_at, f.updated_at";
    private static final String PAYMENT_COLUMNS = "p.id, p.fine_id, p.amount, p.receipt_reference, p.received_by, p.paid_at, p.voided_at, p.voided_by, p.void_reason, p.created_at, p.updated_at";
    private static final String TERM_SELECT = "SELECT id, code, name, starts_on, ends_on, is_current, is_active, created_at, updated_at FROM terms";
    private static final String FINE_ROW_SELECT = "SELECT " + FINE_COLUMNS + """
            , s.student_number, CONCAT_WS(' ', s.first_name, s.middle_name, s.last_name, s.suffix) AS student_name,
            o.code AS office_code, t.code AS term_code, b.paid_amount, b.balance, b.status AS fine_status
            FROM fines f JOIN students s ON s.id = f.student_id JOIN organizations o ON o.id = f.org_id
            JOIN terms t ON t.id = f.term_id JOIN v_fine_balances b ON b.fine_id = f.id
            """;

    FinanceDao(Connection connection, boolean writable) { super(connection, writable); }

    @Override public List<FineRow> searchFines(FineFilter f) throws ServiceException {
        return query(FINE_ROW_SELECT + " WHERE (? = '' OR s.student_number = ?) AND (? IS NULL OR f.org_id = ?) "
                + "AND (? IS NULL OR f.term_id = ?) AND (? IS NULL OR b.status = ?) ORDER BY f.created_at DESC, f.id DESC LIMIT ? OFFSET ?", s -> {
            s.setString(1, f.studentNumber()); s.setString(2, f.studentNumber()); s.setObject(3, f.orgId()); s.setObject(4, f.orgId());
            s.setObject(5, f.termId()); s.setObject(6, f.termId());
            s.setString(7, f.status() == null ? null : f.status().databaseValue()); s.setString(8, f.status() == null ? null : f.status().databaseValue());
            s.setInt(9, f.pageSize() + 1); s.setInt(10, f.page() * f.pageSize());
        }, FinanceDao::mapFineRow);
    }
    @Override public Optional<Fine> findFine(long id) throws ServiceException {
        return query("SELECT " + FINE_COLUMNS + " FROM fines f WHERE f.id = ?", s -> s.setLong(1, id), FinanceDao::mapFine).stream().findFirst();
    }
    @Override public Optional<Fine> lockFine(long id) throws ServiceException {
        requireWritable();
        return query("SELECT " + FINE_COLUMNS + " FROM fines f WHERE f.id = ? FOR UPDATE", s -> s.setLong(1, id), FinanceDao::mapFine).stream().findFirst();
    }
    @Override public FineRow fineRow(long id) throws ServiceException {
        return query(FINE_ROW_SELECT + " WHERE f.id = ?", s -> s.setLong(1, id), FinanceDao::mapFineRow).stream().findFirst().orElseThrow(() -> new NotFoundException("Fine"));
    }
    @Override public BigDecimal paidAmount(long id) throws ServiceException {
        return query("SELECT COALESCE(SUM(amount), 0.00) FROM payments WHERE fine_id = ? AND voided_at IS NULL", s -> s.setLong(1, id), r -> r.getBigDecimal(1)).getFirst();
    }
    @Override public long insertFine(FineDraft d, long studentId, long actor) throws ServiceException {
        return insert("INSERT INTO fines (student_id, org_id, term_id, reason, amount, assessed_by) VALUES (?, ?, ?, ?, ?, ?)", s -> {
            s.setLong(1, studentId); s.setLong(2, d.orgId()); s.setLong(3, d.termId()); s.setString(4, d.reason()); s.setBigDecimal(5, d.amount()); s.setLong(6, actor);
        });
    }
    @Override public void updateFine(long id, String reason, BigDecimal amount) throws ServiceException {
        update("UPDATE fines SET reason = ?, amount = ? WHERE id = ?", s -> { s.setString(1, reason); s.setBigDecimal(2, amount); s.setLong(3, id); });
    }
    @Override public void waiveFine(long id, long actor, String reason) throws ServiceException {
        update("UPDATE fines SET waived_at = CURRENT_TIMESTAMP(6), waived_by = ?, waiver_reason = ? WHERE id = ? AND waived_at IS NULL", s -> { s.setLong(1, actor); s.setString(2, reason); s.setLong(3, id); });
    }
    @Override public List<PaymentRow> searchPayments(PaymentFilter f) throws ServiceException {
        return query("SELECT " + PAYMENT_COLUMNS + """
                , fi.student_id, COALESCE(p.receipt_snapshot->>'$.studentNumber', st.student_number) AS student_number,
                COALESCE(p.receipt_snapshot->>'$.studentName', CONCAT_WS(' ',st.first_name,st.middle_name,st.last_name,st.suffix)) AS student_name,
                COALESCE(p.receipt_snapshot->>'$.officeCode', o.code) AS office_code,
                COALESCE(p.receipt_snapshot->>'$.termCode', t.code) AS term_code,
                p.receipt_snapshot IS NOT NULL AS archived_receipt
                FROM payments p JOIN fines fi ON fi.id = p.fine_id JOIN students st ON st.id = fi.student_id
                JOIN organizations o ON o.id = fi.org_id JOIN terms t ON t.id = fi.term_id
                WHERE (? = '' OR p.receipt_reference LIKE ? ESCAPE '!' OR st.student_number LIKE ? ESCAPE '!'
                    OR p.receipt_snapshot->>'$.studentNumber' LIKE ? ESCAPE '!')
                AND (? IS NULL OR fi.org_id = ?) AND (? IS NULL OR fi.term_id = ?)
                AND (? IS NULL OR (p.voided_at IS NOT NULL) = ?)
                AND (? IS NULL OR p.paid_at >= ?) AND (? IS NULL OR p.paid_at <= ?)
                ORDER BY p.paid_at DESC, p.id DESC LIMIT ? OFFSET ?
                """, s -> {
            s.setString(1, f.query()); s.setString(2, SearchSql.contains(f.query())); s.setString(3, SearchSql.contains(f.query())); s.setString(4, SearchSql.contains(f.query()));
            s.setObject(5, f.orgId()); s.setObject(6, f.orgId()); s.setObject(7, f.termId()); s.setObject(8, f.termId());
            s.setObject(9, f.voided()); s.setObject(10, f.voided());
            LocalDateTime from = f.from() == null ? null : f.from().atStartOfDay();
            LocalDateTime through = f.through() == null ? null : f.through().atTime(23, 59, 59, 999999000);
            s.setObject(11, from); s.setObject(12, from); s.setObject(13, through); s.setObject(14, through);
            s.setInt(15, f.pageSize() + 1); s.setInt(16, f.page() * f.pageSize());
        }, r -> new PaymentRow(mapPayment(r), r.getLong("student_id"), r.getString("student_number"), r.getString("student_name"), r.getString("office_code"), r.getString("term_code"), r.getBoolean("archived_receipt")));
    }
    @Override public Optional<Payment> findPayment(long id) throws ServiceException {
        return query("SELECT " + PAYMENT_COLUMNS + " FROM payments p WHERE p.id = ?", s -> s.setLong(1, id), FinanceDao::mapPayment).stream().findFirst();
    }
    @Override public Optional<Payment> findByReceipt(String reference) throws ServiceException {
        return query("SELECT " + PAYMENT_COLUMNS + " FROM payments p WHERE p.receipt_reference = ?", s -> s.setString(1, reference), FinanceDao::mapPayment).stream().findFirst();
    }
    @Override public Optional<Payment> lockPayment(long id) throws ServiceException {
        requireWritable();
        return query("SELECT " + PAYMENT_COLUMNS + " FROM payments p WHERE p.id = ? FOR UPDATE", s -> s.setLong(1, id), FinanceDao::mapPayment).stream().findFirst();
    }
    @Override public long insertPayment(long fineId, BigDecimal amount, String reference, long actor, BigDecimal balanceAfter) throws ServiceException {
        return insert("""
                INSERT INTO payments (fine_id, amount, receipt_reference, received_by, receipt_snapshot)
                SELECT f.id, ?, ?, ?, JSON_OBJECT(
                    'studentNumber', s.student_number, 'studentName', CONCAT_WS(' ',s.first_name,s.middle_name,s.last_name,s.suffix),
                    'officeCode', o.code, 'officeName', o.name, 'termCode', t.code, 'termName', t.name,
                    'reason', f.reason, 'assessedAmount', CAST(f.amount AS CHAR),
                    'paidAmount', CAST(? AS CHAR), 'balanceAfter', CAST(? AS CHAR),
                    'receiverUsername', u.username, 'receiverName', u.display_name)
                FROM fines f JOIN students s ON s.id = f.student_id JOIN organizations o ON o.id = f.org_id
                JOIN terms t ON t.id = f.term_id JOIN users u ON u.id = ? WHERE f.id = ?
                """, s -> { s.setBigDecimal(1, amount); s.setString(2, reference); s.setLong(3, actor); s.setBigDecimal(4, amount); s.setBigDecimal(5, balanceAfter); s.setLong(6, actor); s.setLong(7, fineId); });
    }
    @Override public void voidPayment(long id, long actor, String reason) throws ServiceException {
        update("UPDATE payments SET voided_at = CURRENT_TIMESTAMP(6), voided_by = ?, void_reason = ? WHERE id = ? AND voided_at IS NULL", s -> { s.setLong(1, actor); s.setString(2, reason); s.setLong(3, id); });
    }
    @Override public ReceiptData receipt(long id) throws ServiceException {
        return query("SELECT " + PAYMENT_COLUMNS + """
                , p.receipt_snapshot IS NOT NULL AS archived,
                p.receipt_snapshot->>'$.studentNumber' AS student_number, p.receipt_snapshot->>'$.studentName' AS student_name,
                p.receipt_snapshot->>'$.officeCode' AS office_code, p.receipt_snapshot->>'$.officeName' AS office_name,
                p.receipt_snapshot->>'$.termCode' AS term_code, p.receipt_snapshot->>'$.termName' AS term_name,
                p.receipt_snapshot->>'$.reason' AS fine_reason, p.receipt_snapshot->>'$.assessedAmount' AS assessed,
                p.receipt_snapshot->>'$.balanceAfter' AS balance_after,
                p.receipt_snapshot->>'$.receiverUsername' AS receiver_username, p.receipt_snapshot->>'$.receiverName' AS receiver_name
                FROM payments p WHERE p.id = ?
                """, s -> s.setLong(1, id), r -> {
            if (!r.getBoolean("archived")) { throw new SQLException("Legacy payment has no archived receipt", "45000"); }
            return new ReceiptData(mapPayment(r), archivedText(r, "student_number"), archivedText(r, "student_name"), archivedText(r, "office_code"),
                    archivedText(r, "office_name"), archivedText(r, "term_code"), archivedText(r, "term_name"), archivedText(r, "fine_reason"),
                    archivedMoney(r, "assessed"), archivedMoney(r, "balance_after"), archivedText(r, "receiver_username"), archivedText(r, "receiver_name"));
        }).stream().findFirst().orElseThrow(() -> new NotFoundException("Payment"));
    }
    @Override public boolean archivedReceipt(long id) throws ServiceException {
        return query("SELECT receipt_snapshot IS NOT NULL FROM payments WHERE id = ?", s -> s.setLong(1, id), r -> r.getBoolean(1)).stream().findFirst().orElse(false);
    }
    @Override public Optional<ClearanceRecord> lockClearance(long studentId, long orgId, long termId) throws ServiceException {
        requireWritable();
        return query("SELECT id, student_id, org_id, term_id, status, remarks, cleared_by, cleared_at, balance_exception, exception_reason, exception_by, exception_at, created_at, updated_at FROM clearance_records WHERE student_id = ? AND org_id = ? AND term_id = ? FOR UPDATE", s -> {
            s.setLong(1, studentId); s.setLong(2, orgId); s.setLong(3, termId);
        }, r -> new ClearanceRecord(r.getLong("id"), r.getLong("student_id"), r.getLong("org_id"), r.getLong("term_id"), ClearanceStatus.valueOf(r.getString("status").toUpperCase(java.util.Locale.ROOT)),
                r.getString("remarks"), r.getObject("cleared_by", Long.class), r.getObject("cleared_at", LocalDateTime.class), r.getBoolean("balance_exception"), r.getString("exception_reason"),
                r.getObject("exception_by", Long.class), r.getObject("exception_at", LocalDateTime.class), r.getObject("created_at", LocalDateTime.class), r.getObject("updated_at", LocalDateTime.class))).stream().findFirst();
    }
    @Override public void holdClearance(long id, String reason) throws ServiceException {
        update("UPDATE clearance_records SET status = 'on_hold', remarks = ?, cleared_by = NULL, cleared_at = NULL WHERE id = ?", s -> { s.setString(1, reason); s.setLong(2, id); });
    }
    @Override public List<Term> terms() throws ServiceException { return query(TERM_SELECT + " ORDER BY starts_on DESC, id DESC", s -> {}, FinanceDao::mapTerm); }
    @Override public List<Term> lockTerms() throws ServiceException { requireWritable(); return query(TERM_SELECT + " ORDER BY id FOR UPDATE", s -> {}, FinanceDao::mapTerm); }
    @Override public Optional<Term> lockTermShared(long id) throws ServiceException { requireWritable();return query(TERM_SELECT+" WHERE id=? FOR SHARE",s->s.setLong(1,id),FinanceDao::mapTerm).stream().findFirst(); }
    @Override public Optional<Term> findTerm(long id) throws ServiceException { return query(TERM_SELECT + " WHERE id = ?", s -> s.setLong(1, id), FinanceDao::mapTerm).stream().findFirst(); }
    @Override public Optional<Term> findTermByCode(String code) throws ServiceException { return query(TERM_SELECT + " WHERE code = ?", s -> s.setString(1, code), FinanceDao::mapTerm).stream().findFirst(); }
    @Override public long insertTerm(TermDraft d) throws ServiceException { return insert("INSERT INTO terms (code, name, starts_on, ends_on, is_active, is_current) VALUES (?, ?, ?, ?, ?, ?)", s -> bindTerm(s, d)); }
    @Override public void updateTerm(long id, TermDraft d) throws ServiceException { update("UPDATE terms SET code = ?, name = ?, starts_on = ?, ends_on = ?, is_active = ?, is_current = ? WHERE id = ?", s -> { bindTerm(s, d); s.setLong(7, id); }); }
    @Override public void clearCurrentTerm(long id) throws ServiceException { update("UPDATE terms SET is_current = FALSE WHERE id = ?", s -> s.setLong(1, id)); }
    @Override public boolean termHasActiveTickets(long id) throws ServiceException { return !query("SELECT id FROM queue_tickets WHERE term_id = ? AND status IN ('waiting','called','serving','deferred') LIMIT 1", s -> s.setLong(1, id), r -> r.getLong(1)).isEmpty(); }
    @Override public boolean termHasRecords(long id) throws ServiceException {
        return query("SELECT EXISTS(SELECT 1 FROM fines WHERE term_id = ?) OR EXISTS(SELECT 1 FROM queue_tickets WHERE term_id = ?) OR EXISTS(SELECT 1 FROM clearance_records WHERE term_id = ?)", s -> { s.setLong(1, id); s.setLong(2, id); s.setLong(3, id); }, r -> r.getBoolean(1)).getFirst();
    }
    private static void bindTerm(PreparedStatement s, TermDraft d) throws SQLException { s.setString(1, d.code()); s.setString(2, d.name()); s.setObject(3, d.startsOn()); s.setObject(4, d.endsOn()); s.setBoolean(5, d.active()); s.setBoolean(6, d.current()); }
    private static String archivedText(ResultSet row, String column) throws SQLException {
        String value = row.getString(column);
        if (value == null || value.isBlank()) { throw new SQLException("Archived receipt field is missing", "45000"); }
        return value;
    }
    private static BigDecimal archivedMoney(ResultSet row, String column) throws SQLException {
        try {
            BigDecimal value = new BigDecimal(archivedText(row, column)).setScale(2, java.math.RoundingMode.UNNECESSARY);
            if (value.signum() < 0) { throw new ArithmeticException(); }
            return value;
        } catch (NumberFormatException | ArithmeticException ex) { throw new SQLException("Archived receipt amount is invalid", "45000", ex); }
    }
    private static Term mapTerm(ResultSet r) throws SQLException { return new Term(r.getLong("id"), r.getString("code"), r.getString("name"), r.getObject("starts_on", LocalDate.class), r.getObject("ends_on", LocalDate.class), r.getBoolean("is_current"), r.getBoolean("is_active"), r.getObject("created_at", LocalDateTime.class), r.getObject("updated_at", LocalDateTime.class)); }
    private static Fine mapFine(ResultSet r) throws SQLException { return new Fine(r.getLong("id"), r.getLong("student_id"), r.getLong("org_id"), r.getLong("term_id"), r.getString("reason"), r.getBigDecimal("amount"), r.getLong("assessed_by"), r.getObject("waived_at", LocalDateTime.class), r.getObject("waived_by", Long.class), r.getString("waiver_reason"), r.getObject("created_at", LocalDateTime.class), r.getObject("updated_at", LocalDateTime.class)); }
    private static FineRow mapFineRow(ResultSet r) throws SQLException { return new FineRow(mapFine(r), r.getString("student_number"), r.getString("student_name"), r.getString("office_code"), r.getString("term_code"), r.getBigDecimal("paid_amount"), r.getBigDecimal("balance"), FineStatus.valueOf(r.getString("fine_status").toUpperCase(java.util.Locale.ROOT))); }
    private static Payment mapPayment(ResultSet r) throws SQLException { return new Payment(r.getLong("id"), r.getLong("fine_id"), r.getBigDecimal("amount"), r.getString("receipt_reference"), r.getLong("received_by"), r.getObject("paid_at", LocalDateTime.class), r.getObject("voided_at", LocalDateTime.class), r.getObject("voided_by", Long.class), r.getString("void_reason"), r.getObject("created_at", LocalDateTime.class), r.getObject("updated_at", LocalDateTime.class)); }
}
