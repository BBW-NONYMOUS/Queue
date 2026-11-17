package dao;

import model.Role;
import model.User;

/** Authentication-only projection. Never expose this object to a form or logger. */
public record LoginAccount(User user, String passwordHash, boolean studentActive, boolean officeActive,
        String studentNumber, String officeCode, String officeName) {
    public boolean eligible() {
        return user.active()
                && (user.role() != Role.STUDENT || studentActive)
                && (user.role() != Role.OFFICER || officeActive);
    }

    @Override
    public String toString() {
        return "LoginAccount[userId=" + user.id() + ", passwordHash=REDACTED]";
    }
}
