package dao.support;

import com.zaxxer.hikari.*;
import dao.*;
import model.*;
import service.*;
import java.sql.*;
import java.nio.file.*;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

/** Owns only the explicitly acknowledged disposable schema. Never reads app.properties. */
public final class MySqlFixture implements AutoCloseable {
    private static final List<String> TABLES=List.of("users","students","organizations","terms","fines","payments","queue_tickets","queue_counters","clearance_records");
    private final Connection owner;
    private final List<HikariDataSource> pools=new ArrayList<>();
    private final MySqlSettings settings;
    private boolean advisoryLock,closed;
    public final Client a,b;
    public record Client(HikariDataSource pool,JdbcUnitOfWork transactions,AuthService auth,Session admin,Session officer,Session cashier,Session student,QueueService queue,PaymentService payments,FineService fines,ClearanceService clearance) { }

    public static MySqlFixture open()throws Exception {
        String file=System.getProperty("queueclearance.it.config");
        if(file==null||file.isBlank()||file.contains("${"))throw new IllegalStateException("mysql-it requires -Dqueueclearance.it.config=<external test properties>; it never uses app.properties.");
        return new MySqlFixture(MySqlSettings.load(Path.of(file)));
    }
    private MySqlFixture(MySqlSettings settings)throws Exception {
        this.settings=settings;owner=DriverManager.getConnection(settings.rootUrl(),settings.owner(),settings.ownerPassword());
        Client first=null,second=null;
        try {
            String version=string("SELECT VERSION()");
            if(!version.matches("8\\.4\\..*")||version.toLowerCase(Locale.ROOT).contains("mariadb"))throw new IllegalStateException("Integration tests require Oracle MySQL 8.4.");
            if(!settings.serverUuid().equalsIgnoreCase(string("SELECT @@server_uuid")))throw new IllegalStateException("The test server UUID does not match. No schema was changed.");
            if(number("SELECT GET_LOCK('queue_clearance_test_junit',0)")!=1)throw new IllegalStateException("Another integration-test process owns the disposable schema.");
            advisoryLock=true;
            execute("SET SESSION time_zone='+08:00'");
            // Literal, deliberately non-configurable test schema. Production schema is never a target.
            execute("DROP DATABASE IF EXISTS `queue_clearance_test`");
            execute("CREATE DATABASE `queue_clearance_test` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci");
            owner.setCatalog(MySqlSettings.SCHEMA);
            migrate();grantClient();seed();
            first=client("pc-a");second=client("pc-b");
        } catch(Exception|Error ex) {try{close();}catch(RuntimeException cleanup){ex.addSuppressed(cleanup);}throw ex;}
        a=first;b=second;
    }
    private void migrate()throws Exception {
        try(var paths=Files.list(settings.project().resolve("database/migrations"))) {
            for(Path script:paths.filter(p->p.getFileName().toString().matches("V[0-9]+__.*\\.sql")).sorted().toList()) {
                for(String sql:SqlScript.statements(Files.readString(script))) {
                    if(sql.matches("(?s)CREATE DATABASE queue_clearance\\b.*")||sql.equalsIgnoreCase("USE queue_clearance"))continue;
                    if(sql.toLowerCase(Locale.ROOT).contains("queue_clearance"))throw new IllegalStateException("Unexpected production-schema reference in a migration.");
                    execute(sql);
                }
            }
        }
        if(number("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='queue_clearance_test' AND table_type='BASE TABLE'")!=10
                ||number("SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_schema='queue_clearance_test'")!=47)throw new IllegalStateException("The disposable schema did not load all production migrations.");
    }
    private void grantClient()throws SQLException {
        String account="'"+settings.client()+"'@'"+settings.clientHost()+"'";
        // These identifiers are compiled-in allowlists, never user-supplied SQL.
        for(String table:TABLES)execute("GRANT SELECT,INSERT,UPDATE ON `queue_clearance_test`.`"+table+"` TO "+account);
        for(String table:List.of("audit_logs","v_fine_balances","v_office_balances","v_queue_board"))execute("GRANT SELECT ON `queue_clearance_test`.`"+table+"` TO "+account);
    }
    private void seed()throws SQLException {
        execute("SET @audit_user_id=NULL,@audit_request_id='junit-fixtures'");
        for(OfficeCode office:OfficeCode.values())execute("INSERT INTO organizations(code,name,queue_open,is_active) VALUES(?,?,1,1)",office.name(),office.name());
        execute("INSERT INTO terms(code,name,starts_on,ends_on,is_current,is_active) VALUES('IT-TERM','Integration term',CURRENT_DATE-INTERVAL 90 DAY,CURRENT_DATE+INTERVAL 90 DAY,1,1)");
        for(int i=0;i<16;i++)execute("INSERT INTO students(id,student_number,first_name,last_name,program,year_level,section,is_active) VALUES(?,?,?,'Test','BSIT',1,'IT',1)",100+i,studentNumber(i),"Student "+i);
        String hash=BCrypt.hashpw("Integration-only-password-42",BCrypt.gensalt(12));
        user(1,"it-admin","admin",null,null,hash);execute("SET @audit_user_id=1");
        user(2,"it-officer","officer",null,1L,hash);user(3,"it-cashier","cashier",null,null,hash);user(4,"it-student","student",100L,null,hash);
        execute("SET @audit_user_id=NULL,@audit_request_id=NULL");
    }
    private void user(long id,String name,String role,Long student,Long office,String hash)throws SQLException {
        execute("INSERT INTO users(id,username,password_hash,display_name,role,student_id,org_id,is_active,must_change_password) VALUES(?,?,?,?,?,?,?,1,0)",id,name,hash,name,role,student,office);
    }
    private Client client(String name)throws ServiceException {
        HikariConfig config=new HikariConfig();config.setPoolName("junit-"+name);config.setJdbcUrl(settings.databaseUrl());config.setUsername(settings.client());config.setPassword(settings.clientPassword());config.setMaximumPoolSize(6);config.setMinimumIdle(0);config.setConnectionTimeout(10000);config.setInitializationFailTimeout(-1);
        config.addDataSourceProperty("allowMultiQueries","false");config.addDataSourceProperty("sslMode","VERIFY_IDENTITY");config.addDataSourceProperty("connectionTimeZone","+08:00");config.addDataSourceProperty("forceConnectionTimeZoneToSession","true");
        HikariDataSource pool=new HikariDataSource(config);pools.add(pool);JdbcUnitOfWork work=new JdbcUnitOfWork(pool);AuthService auth=new AuthService(work,12);
        Session admin=auth.login("it-admin",password()),officer=auth.login("it-officer",password()),cashier=auth.login("it-cashier",password()),student=auth.login("it-student",password());
        return new Client(pool,work,auth,admin,officer,cashier,student,new QueueService(work,auth),new PaymentService(work,auth),new FineService(work,auth),new ClearanceService(work,auth));
    }
    private static char[] password(){return "Integration-only-password-42".toCharArray();}
    public static String studentNumber(int offset){return String.format(Locale.ROOT,"IT-STUDENT-%03d",offset);}
    public void cancelActiveTickets()throws SQLException {
        execute("SET @audit_user_id=1,@audit_request_id='junit-between-tests'");
        execute("UPDATE queue_tickets SET status='cancelled',cancelled_at=CURRENT_TIMESTAMP(6),cancellation_reason='Integration test reset' WHERE status IN ('waiting','called','serving','deferred')");
        execute("SET @audit_user_id=NULL,@audit_request_id=NULL");
    }
    public long counter(long office)throws SQLException {return number("SELECT COALESCE((SELECT last_seq FROM queue_counters WHERE org_id=? AND queue_date=CURRENT_DATE),0)",office);}
    public long number(String sql,Object... values)throws SQLException {try(PreparedStatement p=statement(owner,sql,values);ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Expected a scalar");return r.getLong(1);}}
    public String string(String sql,Object... values)throws SQLException {try(PreparedStatement p=statement(owner,sql,values);ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Expected a scalar");return r.getString(1);}}
    public void execute(String sql,Object... values)throws SQLException {try(PreparedStatement p=statement(owner,sql,values)){p.execute();}}
    public static PreparedStatement statement(Connection connection,String sql,Object... values)throws SQLException {PreparedStatement p=connection.prepareStatement(sql);p.setQueryTimeout(20);for(int i=0;i<values.length;i++)p.setObject(i+1,values[i]);return p;}
    @Override public synchronized void close() {
        if(closed)return;closed=true;IllegalStateException failure=null;
        for(HikariDataSource pool:pools)try{pool.close();}catch(RuntimeException ex){failure=cleanupFailure(failure,ex);}
        try {
            if(advisoryLock)try(PreparedStatement p=owner.prepareStatement("SELECT RELEASE_LOCK('queue_clearance_test_junit')")){p.execute();}
        }catch(SQLException ex){failure=cleanupFailure(failure,ex);}
        finally {try{owner.close();}catch(SQLException ex){failure=cleanupFailure(failure,ex);}}
        if(failure!=null)throw failure;
    }
    private static IllegalStateException cleanupFailure(IllegalStateException failure,Exception cause){
        if(failure==null)return new IllegalStateException("Could not close the MySQL test fixture",cause);
        failure.addSuppressed(cause);return failure;
    }
}
