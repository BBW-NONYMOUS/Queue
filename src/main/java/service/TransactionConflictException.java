package service;

/** The entire transaction was rolled back; a service may retry its complete work. */
public final class TransactionConflictException extends DataAccessException {
    public TransactionConflictException(Throwable cause) {
        super("Another workstation is updating this record. Please try again.", cause);
    }
}
