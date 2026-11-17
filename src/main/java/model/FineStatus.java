package model;

public enum FineStatus {
    UNPAID("Unpaid"),
    PARTIAL("Partial"),
    PAID("Paid"),
    WAIVED("Waived");

    private final String displayName;

    FineStatus(String displayName) {
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
