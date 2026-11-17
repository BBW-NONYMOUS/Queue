package model;


/** Opaque session token plus a display profile; services trust their registry, not this profile. */
public record Session(
        java.util.UUID token,
        User user) {
    @Override
    public String toString() {
        return "Session[token=REDACTED, userId=" + (user == null ? "none" : user.id()) + "]";
    }
}
