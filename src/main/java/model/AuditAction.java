package model;

public enum AuditAction {
    INSERT("Insert"),
    UPDATE("Update");

    private final String displayName;

    AuditAction(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public String databaseValue() {
        return name();
    }

    @Override
    public String toString() {
        return displayName;
    }
}
