package model;

import java.time.LocalDateTime;
import java.time.LocalDate;

/** Academic clearance period. */
public record Term(
        long id,
        String code,
        String name,
        LocalDate startsOn,
        LocalDate endsOn,
        boolean current,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
