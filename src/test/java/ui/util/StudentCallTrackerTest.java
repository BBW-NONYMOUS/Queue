package ui.util;
import model.*;import java.time.*;import java.util.*;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class StudentCallTrackerTest {
    private final LocalDateTime now=LocalDateTime.of(2026,11,17,9,0);
    private QueueCallEvent call(long event,long ticket,LocalDateTime at){return new QueueCallEvent(event,ticket,OfficeCode.PSITS,at.toLocalDate(),42,at);}
    private StudentQueueSnapshot snapshot(LocalDateTime time,List<QueueRow> tickets,QueueCallEvent... calls){return new StudentQueueSnapshot(new QueueClock(time.toLocalDate(),time),new Student(100,"TEST-100","Test",null,"Student",null,"BSIT",1,"A",true,now,now),null,List.of(),tickets,List.of(calls));}
    private QueueRow live(){return new QueueRow(new QueueTicket(7,100,2,1,now.toLocalDate(),42,QueueStatus.CALLED,now,0,1,2L,now.minusMinutes(2),null,null,null,null,now,now,UUID.randomUUID()),OfficeCode.PSITS,"TEST-100","Test Student");}
    @Test void suppressesHistoricalTerminalCallsAtLogin(){var tracker=new StudentCallTracker(now.minusMinutes(1));assertTrue(tracker.accept(snapshot(now,List.of(),call(30,7,now.minusHours(1)))).isEmpty());}
    @Test void alertsLiveCallEvenWhenItPrecedesLogin(){var tracker=new StudentCallTracker(now);assertEquals(1,tracker.accept(snapshot(now,List.of(live()),call(30,7,now.minusMinutes(2)))).size());}
    @Test void recordsFastCompletedCallAndDeduplicatesIndividualIds(){var tracker=new StudentCallTracker(now);tracker.accept(snapshot(now,List.of()));assertEquals(1,tracker.accept(snapshot(now,List.of(),call(32,7,now))).size());assertTrue(tracker.accept(snapshot(now,List.of(),call(32,7,now))).isEmpty());assertEquals(1,tracker.accept(snapshot(now,List.of(),call(31,8,now),call(32,7,now))).size());}
    @Test void resetsSeenEventsOnServerDateRollover(){var tracker=new StudentCallTracker(now);tracker.accept(snapshot(now,List.of(),call(10,7,now)));assertEquals(1,tracker.accept(snapshot(now.plusDays(1),List.of(),call(11,8,now.plusDays(1)))).size());}
}
