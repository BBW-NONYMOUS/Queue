package model;

public enum QueueStatus {
    WAITING("Waiting"),
    CALLED("Called"),
    SERVING("Serving"),
    DEFERRED("Deferred"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    QueueStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public String databaseValue() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
