package dao.integration;
import dao.support.*;
import model.*;
import service.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.sql.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Timeout(120)
class QueueConcurrencyIT {
    private MySqlFixture database;private String day;
    @BeforeAll void createDisposableSchema()throws Exception{database=MySqlFixture.open();}
    @AfterAll void close(){if(database!=null)database.close();}
    @BeforeEach void resetDesk()throws Exception{database.cancelActiveTickets();day=database.string("SELECT CURRENT_DATE");}
    @AfterEach void checkServerDate()throws Exception{assertEquals(day,database.string("SELECT CURRENT_DATE"),"Server date changed during the test. Rerun away from midnight.");}
    private MySqlFixture.Client pc(int index){return index%2==0?database.a:database.b;}
    @Test void concurrentIssueProducesContiguousUniqueNumbersAcrossTwoPcs()throws Exception {
        long before=database.counter(1);List<Callable<QueueRow>> requests=new ArrayList<>();
        for(int i=0;i<8;i++){int n=i;requests.add(()->pc(n).queue().issue(pc(n).admin(),1,MySqlFixture.studentNumber(n),UUID.randomUUID()));}
        var outcomes=ConcurrentCalls.run(requests);for(var outcome:outcomes)assertNull(outcome.error(),()->String.valueOf(outcome.error()));
        var tickets=outcomes.stream().map(o->o.value().ticket()).toList();
        assertEquals(8,tickets.stream().map(QueueTicket::id).distinct().count());assertEquals(8,tickets.stream().map(QueueTicket::seq).distinct().count());
        assertEquals(java.util.stream.LongStream.rangeClosed(before+1,before+8).boxed().toList(),tickets.stream().map(QueueTicket::seq).sorted().toList());
        assertEquals(before+8,database.counter(1));
        for(var ticket:tickets)assertEquals(1,database.number("SELECT COUNT(*) FROM audit_logs WHERE table_name='queue_tickets' AND action='INSERT' AND record_key=? AND actor_user_id=1",Long.toString(ticket.id())));
    }
    @Test void simultaneousRequestsForOneStudentCreateOnlyOneActiveTicket()throws Exception {
        long before=database.counter(1);List<Callable<QueueRow>> requests=new ArrayList<>();
        for(int i=0;i<8;i++){int n=i;requests.add(()->pc(n).queue().issue(pc(n).admin(),1,MySqlFixture.studentNumber(0),UUID.randomUUID()));}
        var outcomes=ConcurrentCalls.run(requests);assertEquals(1,outcomes.stream().filter(ConcurrentCalls.Outcome::succeeded).count());
        for(var outcome:outcomes)if(!outcome.succeeded())assertInstanceOf(ValidationException.class,outcome.error());
        assertEquals(before+1,database.counter(1));assertEquals(1,database.number("SELECT COUNT(*) FROM queue_tickets WHERE org_id=1 AND student_id=100 AND queue_date=CURRENT_DATE AND status='waiting'"));
    }
    @Test void sameIssueRequestReplaysOneTicketEvenAfterCompletion()throws Exception {
        long before=database.counter(1);UUID key=UUID.randomUUID();List<Callable<QueueRow>> requests=new ArrayList<>();
        for(int i=0;i<8;i++){int n=i;requests.add(()->pc(n).queue().issue(pc(n).admin(),1,MySqlFixture.studentNumber(0),key));}
        var outcomes=ConcurrentCalls.run(requests);for(var result:outcomes)assertNull(result.error());
        assertEquals(1,outcomes.stream().map(o->o.value().ticket().id()).distinct().count());assertEquals(before+1,database.counter(1));
        var a=database.a;var called=a.queue().callNext(a.admin(),1).orElseThrow();var serving=a.queue().transition(a.admin(),called,QueueAction.START_SERVING,null);a.queue().transition(a.admin(),serving,QueueAction.COMPLETE,null);
        QueueRow replay=database.b.queue().issue(database.b.admin(),1,MySqlFixture.studentNumber(0),key);
        assertEquals(called.ticket().id(),replay.ticket().id());assertEquals(QueueStatus.COMPLETED,replay.ticket().status());
    }
    @Test void concurrentCallNextHasOneWinnerAndNeverSkipsTheFirstTicket()throws Exception {
        List<QueueRow> issued=new ArrayList<>();for(int i=0;i<6;i++)issued.add(database.a.queue().issue(database.a.admin(),1,MySqlFixture.studentNumber(i),UUID.randomUUID()));
        QueueRow expected=issued.stream().min(Comparator.comparing((QueueRow r)->r.ticket().sortAt()).thenComparingLong(r->r.ticket().id())).orElseThrow();
        List<Callable<Optional<QueueRow>>> calls=new ArrayList<>();for(int i=0;i<8;i++){int n=i;calls.add(()->pc(n).queue().callNext(pc(n).admin(),1));}
        var outcomes=ConcurrentCalls.run(calls);assertEquals(1,outcomes.stream().filter(ConcurrentCalls.Outcome::succeeded).count());
        for(var outcome:outcomes)if(outcome.succeeded())assertEquals(expected.ticket().id(),outcome.value().orElseThrow().ticket().id());else assertInstanceOf(ValidationException.class,outcome.error());
        assertEquals(1,database.number("SELECT COUNT(*) FROM queue_tickets WHERE org_id=1 AND status IN ('called','serving')"));
        assertEquals(1,database.number("SELECT COUNT(*) FROM audit_logs WHERE queue_call_student_id=? AND record_key=?",expected.ticket().studentId(),Long.toString(expected.ticket().id())));
        assertTrue(database.a.transactions().read(c->c.queue().calls(expected.ticket().studentId())).stream().anyMatch(e->e.ticketId()==expected.ticket().id()));
    }
    @Test void requeueKeepsNumberAndMovesTicketBehindExistingWaiters()throws Exception {
        var a=database.a;var first=a.queue().issue(a.admin(),1,MySqlFixture.studentNumber(0),UUID.randomUUID());var second=a.queue().issue(a.admin(),1,MySqlFixture.studentNumber(1),UUID.randomUUID());
        var called=a.queue().callNext(a.admin(),1).orElseThrow();var deferred=a.queue().transition(a.admin(),called,QueueAction.DEFER,null);var requeued=a.queue().transition(a.admin(),deferred,QueueAction.REQUEUE,null);
        assertEquals(first.ticket().seq(),requeued.ticket().seq());assertEquals(1,requeued.ticket().deferCount());assertNull(requeued.ticket().calledAt());
        assertEquals(second.ticket().id(),database.b.queue().callNext(database.b.admin(),1).orElseThrow().ticket().id());
    }
    @Test void failedTransactionRollsBackTicketCounterAndAuditTogether()throws Exception {
        long before=database.counter(1);UUID request=UUID.randomUUID();var a=database.a;
        assertThrows(ValidationException.class,()->a.transactions().write(1L,c->{c.organizations().lockById(1).orElseThrow();c.queue().currentTerm(true).orElseThrow();c.students().lockById(100).orElseThrow();var clock=c.queue().clock();long sequence=c.queue().nextSequence(1,clock.day());c.queue().issue(1,100,1,clock.day(),sequence,1,request);throw new ValidationException("Injected rollback");}));
        assertEquals(before,database.counter(1));assertEquals(0,database.number("SELECT COUNT(*) FROM queue_tickets WHERE issue_request_id=?",request.toString()));
        assertEquals(0,database.number("SELECT COUNT(*) FROM audit_logs WHERE table_name='queue_tickets' AND new_values->>'$.issue_request_id'=?",request.toString()));
    }
    @Test void applicationAccountCannotDeleteCreateTablesOrAppendAuditDirectly()throws Exception {
        try(Connection connection=database.a.pool().getConnection()) {
            for(String sql:List.of("DELETE FROM students WHERE id=115","CREATE TABLE should_be_denied(id BIGINT)","INSERT INTO audit_logs(actor_user_id,database_account,connection_id,action,table_name,record_key,new_values) VALUES(1,'fake',1,'UPDATE','students','100',JSON_OBJECT())")) {
                SQLException failure=assertThrows(SQLException.class,()->{try(PreparedStatement p=MySqlFixture.statement(connection,sql)){p.execute();}});
                assertTrue(Set.of(1142,1044).contains(failure.getErrorCode()),"Expected a grant denial, not a trigger or syntax failure");
            }
        }
    }
    @Test void pooledConnectionsDoNotLeakAuditIdentity()throws Exception {
        database.a.queue().issue(database.a.admin(),1,MySqlFixture.studentNumber(0),UUID.randomUUID());List<Connection> borrowed=new ArrayList<>();
        try {for(int i=0;i<6;i++)borrowed.add(database.a.pool().getConnection());for(Connection connection:borrowed)try(PreparedStatement p=MySqlFixture.statement(connection,"SELECT @audit_user_id,@audit_request_id");ResultSet r=p.executeQuery()){assertTrue(r.next());assertNull(r.getObject(1));assertNull(r.getObject(2));}}
        finally {for(Connection connection:borrowed)connection.close();}
    }
}
