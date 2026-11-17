package model;

import java.time.LocalDateTime;

/** One of the seven clearance offices. */
public record Organization(
        long id,
        OfficeCode code,
        String name,
        boolean queueOpen,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
