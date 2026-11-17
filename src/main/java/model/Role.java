package model;

public enum Role {
    ADMIN("Administrator"),
    OFFICER("Office officer"),
    CASHIER("Cashier"),
    STUDENT("Student");

    private final String displayName;

    Role(String displayName) {
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
