package service;

public final class ConflictException extends DataAccessException {
    public ConflictException(Throwable cause) {
        super("Another change conflicts with this record. Refresh and try again.", cause);
    }
}
