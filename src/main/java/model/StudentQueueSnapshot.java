package model;
public record StudentQueueSnapshot(QueueClock clock, Student student, Term term, java.util.List<BoardRow> offices,
        java.util.List<QueueRow> tickets, java.util.List<QueueCallEvent> calls,java.util.List<ClearanceRow> clearances) {
    public StudentQueueSnapshot { offices=java.util.List.copyOf(offices); tickets=java.util.List.copyOf(tickets); calls=java.util.List.copyOf(calls);clearances=java.util.List.copyOf(clearances); }
    public StudentQueueSnapshot(QueueClock clock,Student student,Term term,java.util.List<BoardRow> offices,java.util.List<QueueRow> tickets,java.util.List<QueueCallEvent> calls){this(clock,student,term,offices,tickets,calls,java.util.List.of());}
}
