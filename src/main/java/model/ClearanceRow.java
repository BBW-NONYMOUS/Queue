package model;
import java.math.BigDecimal;
/** An absent record is pending; balance is always derived from non-voided payments. */
public record ClearanceRow(Student student, Organization office, Term term, ClearanceRecord record, BigDecimal balance) {
    public ClearanceStatus status(){return record==null?ClearanceStatus.PENDING:record.status();}
}
