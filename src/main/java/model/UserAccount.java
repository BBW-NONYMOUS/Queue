package model;

/** Administrative row projection that never contains a password hash. */
public record UserAccount(User user, String studentNumber, String officeCode) {
    public String assignment() {
        return switch (user.role()) {
            case STUDENT -> studentNumber == null ? "Unassigned" : studentNumber;
            case OFFICER -> officeCode == null ? "Unassigned" : officeCode;
            default -> "Campus";
        };
    }
}
