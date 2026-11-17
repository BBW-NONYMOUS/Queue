package service;
/** Roll back the complete queue operation and retry against the new server date. */
public final class QueueDateChangedException extends DataAccessException {
    public QueueDateChangedException(Throwable cause) { super("The queue date changed. Please refresh and try again.", cause); }
}
