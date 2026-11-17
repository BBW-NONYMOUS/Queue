package model;

/** No password field: password arrays are transferred separately and erased. */
public record UserDraft(String username, String displayName, Role role, String studentNumber,
        Long orgId, boolean active, boolean mustChangePassword) {
}
