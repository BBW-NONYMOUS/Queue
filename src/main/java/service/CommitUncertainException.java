package service;

/** Never automatically retry: the server may already have committed the write. */
public final class CommitUncertainException extends DataAccessException {
    public CommitUncertainException(Throwable cause) {
        super("The result could not be confirmed. Check the record before trying again.", cause);
    }
}
