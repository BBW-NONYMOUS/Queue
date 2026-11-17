package model;

public enum OfficeCode {
    LSG("LSG"),
    PSITS("PSITS"),
    PHG("PHG"),
    LIBRARY("LIBRARY"),
    CCSO("CCSO"),
    CIT("CIT"),
    ESO("ESO");

    private final String displayName;

    OfficeCode(String displayName) {
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
