package model;
public record QueueCallEvent(long eventId, long ticketId, OfficeCode office, java.time.LocalDate queueDate,
        long seq, java.time.LocalDateTime calledAt) {
    public String number() { return office.name() + "-" + String.format(java.util.Locale.ROOT, "%03d", seq); }
}
