package ui.util;
import java.time.*;
import java.util.*;
import model.*;
/** Audit IDs are deduplicated individually: transaction commit order need not match ID order. */
public final class StudentCallTracker {
    private final LocalDateTime login; private final Set<Long> seen=new HashSet<>(); private LocalDate day;
    public StudentCallTracker(LocalDateTime login) { this.login=login; }
    public List<QueueCallEvent> accept(StudentQueueSnapshot snapshot) {
        boolean first=day==null;if(!Objects.equals(day,snapshot.clock().day())){day=snapshot.clock().day();seen.clear();}
        var result=new ArrayList<QueueCallEvent>();
        for(var call:snapshot.calls()) {
            if(!seen.add(call.eventId()))continue;
            boolean live=snapshot.tickets().stream().anyMatch(r->r.ticket().id()==call.ticketId()&&(r.ticket().status()==QueueStatus.CALLED||r.ticket().status()==QueueStatus.SERVING));
            if(!first||login==null||!call.calledAt().isBefore(login)||live)result.add(call);
        }
        return List.copyOf(result);
    }
}
