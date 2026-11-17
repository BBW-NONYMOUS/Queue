package model;

public enum ClearanceStatus {
    PENDING("Pending"),
    CLEARED("Cleared"),
    ON_HOLD("On hold");

    private final String displayName;

    ClearanceStatus(String displayName) {
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
