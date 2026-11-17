package dao;
import java.sql.*;import java.lang.reflect.Proxy;import java.util.concurrent.atomic.AtomicReference;import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;import service.*;import static org.junit.jupiter.api.Assertions.*;
class DaoSafetyTest {
    private static final class Probe extends BaseDao {Probe(boolean writable){super((Connection)Proxy.newProxyInstance(Probe.class.getClassLoader(),new Class[]{Connection.class},(p,m,a)->{throw new AssertionError("JDBC must not be reached");}),writable);}void read()throws ServiceException{query("SELECT 1",s->{},r->r.getInt(1));}void write()throws ServiceException{update("UPDATE t SET x=?",s->s.setInt(1,1));}}
    @Test void blocksJdbcOnTheEdt()throws Exception{AtomicReference<Throwable> result=new AtomicReference<>();SwingUtilities.invokeAndWait(()->{try{new Probe(true).read();}catch(Throwable ex){result.set(ex);}});assertInstanceOf(IllegalStateException.class,result.get());}
    @Test void blocksWritesInReadOnlyUnitOfWork(){assertThrows(IllegalStateException.class,()->new Probe(false).write());}
    @Test void translatesRollbackConflictAndDateRollover(){assertInstanceOf(TransactionConflictException.class,SqlErrors.translate(new SQLException("deadlock","40001",1213)));assertInstanceOf(QueueDateChangedException.class,SqlErrors.translate(new SQLException("rollover","45001")));assertInstanceOf(ConflictException.class,SqlErrors.translate(new SQLException("duplicate","23000",1062)));}
    @Test void hidesRawSqlErrorFromForms(){var failure=SqlErrors.translate(new SQLException("sensitive SQL details","08001"));assertFalse(failure.getMessage().contains("sensitive"));assertNotNull(failure.getCause());}
}
