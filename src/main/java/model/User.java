package model;

import java.time.LocalDateTime;

/** Authenticated account profile. Deliberately excludes the password hash. */
public record User(
        long id,
        String username,
        String displayName,
        Role role,
        Long studentId,
        Long orgId,
        boolean active,
        boolean mustChangePassword,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
