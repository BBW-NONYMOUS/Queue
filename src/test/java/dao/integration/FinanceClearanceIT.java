package dao.integration;
import dao.support.*;import model.*;import service.*;import java.math.BigDecimal;import java.util.*;import java.util.concurrent.Callable;
import org.junit.jupiter.api.*;import static org.junit.jupiter.api.Assertions.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS) @Timeout(120)
class FinanceClearanceIT {
    private MySqlFixture database;
    @BeforeAll void setup()throws Exception{database=MySqlFixture.open();}
    @AfterAll void close(){if(database!=null)database.close();}
    private FineRow fine(int student,String amount)throws Exception{return database.a.fines().save(database.a.admin(),null,new FineDraft(MySqlFixture.studentNumber(student),1,1,"JUnit fine",new BigDecimal(amount)));}
    @Test void competingPaymentsCannotOverpayOneFine()throws Exception {
        var fine=fine(1,"100.00");List<Callable<Payment>> work=List.of(()->database.a.payments().record(database.a.cashier(),fine.fine().id(),new BigDecimal("80.00"),"IT-"+UUID.randomUUID()),()->database.b.payments().record(database.b.cashier(),fine.fine().id(),new BigDecimal("80.00"),"IT-"+UUID.randomUUID()));
        var results=ConcurrentCalls.run(work);assertEquals(1,results.stream().filter(ConcurrentCalls.Outcome::succeeded).count());for(var result:results)if(!result.succeeded())assertInstanceOf(ValidationException.class,result.error());
        assertEquals("80.00",database.string("SELECT CAST(SUM(amount) AS CHAR) FROM payments WHERE fine_id=? AND voided_at IS NULL",fine.fine().id()));
    }
    @Test void receiptRetryAndVoidPreserveLedgerAndReopenClearance()throws Exception {
        var a=database.a;var fine=fine(2,"100.00");String reference="IT-"+UUID.randomUUID();var paid=a.payments().record(a.cashier(),fine.fine().id(),new BigDecimal("100.00"),reference);
        assertEquals(paid.id(),a.payments().record(a.cashier(),fine.fine().id(),new BigDecimal("100.00"),reference).id());
        var reviewed=a.clearance().find(a.admin(),MySqlFixture.studentNumber(2),1).getFirst();var cleared=a.clearance().decide(a.admin(),reviewed,new ClearanceDraft(ClearanceStatus.CLEARED,null,false,null));assertEquals(ClearanceStatus.CLEARED,cleared.status());
        a.payments().voidPayment(a.admin(),paid.id(),"JUnit receipt correction");var current=a.clearance().find(a.admin(),MySqlFixture.studentNumber(2),1).getFirst();assertEquals(ClearanceStatus.ON_HOLD,current.status());assertEquals(new BigDecimal("100.00"),current.balance());
        assertThrows(ValidationException.class,()->a.payments().record(a.cashier(),fine.fine().id(),new BigDecimal("100.00"),reference));assertNotNull(a.payments().receipt(a.admin(),paid.id()).payment().voidedAt());
    }
    @Test void fineRacingClearanceCannotLeaveOrdinaryClearanceWithDebt()throws Exception {
        var a=database.a;var b=database.b;var reviewed=a.clearance().find(a.admin(),MySqlFixture.studentNumber(3),1).getFirst();
        List<Callable<Object>> requests=List.of(()->a.clearance().decide(a.admin(),reviewed,new ClearanceDraft(ClearanceStatus.CLEARED,null,false,null)),()->b.fines().save(b.admin(),null,new FineDraft(MySqlFixture.studentNumber(3),1,1,"Concurrent assessment",new BigDecimal("25.00"))));
        var results=ConcurrentCalls.run(requests);assertTrue(results.get(1).succeeded(),()->String.valueOf(results.get(1).error()));if(!results.getFirst().succeeded())assertInstanceOf(RecordChangedException.class,results.getFirst().error());
        var current=a.clearance().find(a.admin(),MySqlFixture.studentNumber(3),1).getFirst();assertEquals(new BigDecimal("25.00"),current.balance());assertNotEquals(ClearanceStatus.CLEARED,current.status());
    }
    @Test void onlyAdminCanApproveAReasonedBalanceException()throws Exception {
        fine(4,"60.00");var a=database.a;var row=a.clearance().find(a.admin(),MySqlFixture.studentNumber(4),1).getFirst();
        assertThrows(ValidationException.class,()->a.clearance().decide(a.admin(),row,new ClearanceDraft(ClearanceStatus.CLEARED,null,false,null)));
        assertThrows(AuthorizationException.class,()->a.clearance().decide(a.officer(),row,new ClearanceDraft(ClearanceStatus.CLEARED,null,true,"Denied officer exception")));
        var approved=a.clearance().decide(a.admin(),row,new ClearanceDraft(ClearanceStatus.CLEARED,null,true,"Approved support"));assertTrue(approved.record().balanceException());assertEquals(1,approved.record().exceptionBy());
    }
    @Test void everyReportQueryExecutesAgainstTheRealSchema()throws Exception {
        var a=database.a;for(ReportType type:ReportType.values()){
            var filter=new ReportFilter(type,type==ReportType.STUDENTS?null:1L,null,"","",null,"",null,null,false);
            var data=a.transactions().snapshot(c->{a.auth().require(c,a.admin(),Role.ADMIN);return c.oversight().report(filter);});assertFalse(data.columns().isEmpty());
        }
    }
}
