package model;
public record QueueSnapshot(QueueClock clock, java.util.List<QueueRow> tickets, QueueRow current, long expired, boolean hasNext) {
    public QueueSnapshot { tickets = java.util.List.copyOf(tickets); }
}
