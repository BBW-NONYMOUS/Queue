package model;

import java.time.LocalDateTime;

/** Current student record; historical roster snapshots are not stored. */
public record Student(
        long id,
        String studentNumber,
        String firstName,
        String middleName,
        String lastName,
        String suffix,
        String program,
        int yearLevel,
        String section,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
