package model;
public record QueueRow(QueueTicket ticket, OfficeCode office, String studentNumber, String studentName) {
    public String number() { return office.name() + "-" + String.format(java.util.Locale.ROOT, "%03d", ticket.seq()); }
}
