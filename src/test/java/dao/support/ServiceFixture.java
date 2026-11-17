package dao.support;
import dao.*;import model.*;import service.*;
import java.lang.reflect.*;import java.time.*;import java.math.BigDecimal;import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

/** Deterministic service collaborator. SQL/concurrency claims belong to the MySQL ITs. */
public final class ServiceFixture implements UnitOfWork,DaoContext {
    public static final String PASSWORD="Unit-test-password-42";
    private static final String HASH=BCrypt.hashpw(PASSWORD,BCrypt.gensalt(12));
    public final Map<Long,User> accounts=new HashMap<>();public final Map<Long,String> hashes=new HashMap<>();
    public final Map<Long,Student> students=new HashMap<>();public final Map<Long,Organization> offices=new LinkedHashMap<>();
    public final Map<Long,QueueRow> tickets=new LinkedHashMap<>();public final Map<Long,ClearanceRecord> decisions=new HashMap<>();
    public final List<String> locks=new ArrayList<>();
    public BigDecimal balance=new BigDecimal("0.00");public LocalDate day=LocalDate.of(2026,11,17);
    public long sequence,actor;private long ids=10,ticks;public int conflicts,attempts;public boolean loseCommit,termPresent=true;
    public ServiceFixture(){
        for(OfficeCode code:OfficeCode.values()){long id=offices.size()+1;offices.put(id,new Organization(id,code,code.name(),true,true,time(),time()));}
        students.put(100L,student(100,"TEST-100"));students.put(101L,student(101,"TEST-101"));
        user(1,Role.ADMIN,null,null);user(2,Role.OFFICER,null,1L);user(3,Role.CASHIER,null,null);user(4,Role.STUDENT,100L,null);
    }
    private Student student(long id,String number){return new Student(id,number,"Test",null,"Student",null,"BSIT",1,"A",true,time(),time());}
    private void user(long id,Role role,Long student,Long office){accounts.put(id,new User(id,role.databaseValue(),role.displayName(),role,student,office,true,false,null,time(),time()));hashes.put(id,HASH);}
    public LocalDateTime time(){return day.atTime(9,0).plusNanos(++ticks*1000);}
    public Term term(){return new Term(1,"TEST","Test term",day.minusDays(1),day.plusDays(1),true,true,time(),time());}
    public Session login(AuthService auth,Role role)throws ServiceException{return auth.login(role.databaseValue(),PASSWORD.toCharArray());}
    public void setUserState(long id,Role role,Long student,Long office,boolean active,boolean mustChange){User u=accounts.get(id);accounts.put(id,new User(id,u.username(),u.displayName(),role,student,office,active,mustChange,u.lastLoginAt(),u.createdAt(),time()));}
    private LoginAccount account(long id){User u=accounts.get(id);if(u==null)return null;Student s=students.get(u.studentId());Organization o=offices.get(u.orgId());return new LoginAccount(u,hashes.get(id),s!=null&&s.active(),o!=null&&o.active(),s==null?null:s.studentNumber(),o==null?null:o.code().name(),o==null?null:o.name());}
    @Override public <T>T read(Work<T> work)throws ServiceException{background();return work.execute(this);}
    @Override public <T>T write(Long id,Work<T> work)throws ServiceException {
        background();attempts++;if(conflicts>0){conflicts--;throw new TransactionConflictException(null);}
        var beforeTickets=new LinkedHashMap<>(tickets);var beforeDecisions=new HashMap<>(decisions);var beforeAccounts=new HashMap<>(accounts);var beforeHashes=new HashMap<>(hashes);long beforeSequence=sequence;actor=id==null?0:id;
        try{T result=work.execute(this);if(loseCommit){loseCommit=false;throw new CommitUncertainException(new java.io.IOException("Synthetic lost response"));}return result;}
        catch(ServiceException|RuntimeException ex){if(!(ex instanceof CommitUncertainException)){tickets.clear();tickets.putAll(beforeTickets);decisions.clear();decisions.putAll(beforeDecisions);accounts.clear();accounts.putAll(beforeAccounts);hashes.clear();hashes.putAll(beforeHashes);sequence=beforeSequence;}throw ex;}
    }
    private static void background(){if(javax.swing.SwingUtilities.isEventDispatchThread())throw new IllegalStateException("Database access on EDT");}
    @Override public void auditActor(long id){actor=id;}
    private interface Dispatch{Object call(String name,Object[] args)throws Throwable;}
    @SuppressWarnings("unchecked") private <T>T repository(Class<T> type,Dispatch dispatch){return (T)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{type},(p,m,a)->{
        if(m.getDeclaringClass()==Object.class)return switch(m.getName()){case "toString"->"Test "+type.getSimpleName();case "hashCode"->System.identityHashCode(p);default->p==a[0];};return dispatch.call(m.getName(),a==null?new Object[0]:a);});}
    private AssertionError unexpected(String name){return new AssertionError("Unexpected repository call: "+name);}
    @Override public UserRepository users(){return repository(UserRepository.class,(name,a)->switch(name){
        case "findByUsername"->accounts.values().stream().filter(u->u.username().equalsIgnoreCase((String)a[0])).map(u->account(u.id())).findFirst();
        case "findById","lockById"->Optional.ofNullable(account((Long)a[0]));
        case "recordLogin"->{long id=(Long)a[0];User u=accounts.get(id);accounts.put(id,new User(id,u.username(),u.displayName(),u.role(),u.studentId(),u.orgId(),u.active(),u.mustChangePassword(),time(),u.createdAt(),time()));yield null;}
        case "changePassword"->{long id=(Long)a[0];hashes.put(id,(String)a[1]);User u=accounts.get(id);setUserState(id,u.role(),u.studentId(),u.orgId(),u.active(),false);yield null;}
        default->throw unexpected(name);});}
    @Override public StudentRepository students(){return repository(StudentRepository.class,(name,a)->switch(name){
        case "findById"->Optional.ofNullable(students.get((Long)a[0]));
        case "lockById"->{locks.add("student:"+a[0]);yield Optional.ofNullable(students.get((Long)a[0]));}
        case "findByNumber"->students.values().stream().filter(s->s.studentNumber().equals(a[0])).findFirst();
        default->throw unexpected(name);});}
    @Override public OrganizationRepository organizations(){return repository(OrganizationRepository.class,(name,a)->switch(name){
        case "findById"->Optional.ofNullable(offices.get((Long)a[0]));case "list"->List.copyOf(offices.values());
        case "lockById","lockSharedById"->{locks.add("office:"+a[0]);yield Optional.ofNullable(offices.get((Long)a[0]));}
        default->throw unexpected(name);});}
    @Override public FinanceRepository finance(){return repository(FinanceRepository.class,(name,a)->switch(name){
        case "findTerm","lockTermShared"->termPresent?Optional.of(term()):Optional.empty();case "terms"->List.of(term());
        case "lockClearance"->Optional.ofNullable(decisions.get((Long)a[1]));default->throw unexpected(name);});}
    @Override public ClearanceRepository clearance(){return new ClearanceRepository(){
        public List<ClearanceRow> rows(long student,long term,Long office){return offices.values().stream().filter(o->office==null||office==o.id()).map(o->new ClearanceRow(students.get(student),o,ServiceFixture.this.term(),decisions.get(o.id()),o.id()==1?balance:new BigDecimal("0.00"))).toList();}
        public void save(long student,long office,long term,ClearanceDraft draft,long user){var now=time();var old=decisions.get(office);boolean clear=draft.status()==ClearanceStatus.CLEARED;decisions.put(office,new ClearanceRecord(old==null?++ids:old.id(),student,office,term,draft.status(),draft.remarks(),clear?user:null,clear?now:null,draft.balanceException(),draft.exceptionReason(),draft.balanceException()?user:null,draft.balanceException()?now:null,old==null?now:old.createdAt(),now));}
    };}
    @Override public OversightRepository oversight(){throw unexpected("oversight");}
    @Override public QueueRepository queue(){return repository(QueueRepository.class,(name,a)->switch(name){
        case "clock"->new QueueClock(day,time());case "currentTerm"->termPresent?Optional.of(term()):Optional.empty();
        case "byRequest"->tickets.values().stream().filter(r->r.ticket().issueRequestId().equals(a[0])).findFirst();
        case "find"->Optional.ofNullable(tickets.get((Long)a[0]));
        case "activeStudent"->tickets.values().stream().filter(r->r.ticket().orgId()==(Long)a[0]&&r.ticket().studentId()==(Long)a[1]&&r.ticket().queueDate().equals(a[2])&&active(r.ticket().status())).findFirst();
        case "expire"->0;case "nextSequence"->{locks.add("counter");yield ++sequence;}
        case "issue"->{long id=++ids;var now=time();Student student=students.get((Long)a[1]);QueueTicket ticket=new QueueTicket(id,student.id(),(Long)a[0],(Long)a[2],(LocalDate)a[3],(Long)a[4],QueueStatus.WAITING,now,0,(Long)a[5],null,null,null,null,null,null,now,now,(UUID)a[6]);tickets.put(id,new QueueRow(ticket,offices.get((Long)a[0]).code(),student.studentNumber(),"Test Student"));yield id;}
        case "occupied"->tickets.values().stream().filter(r->r.ticket().orgId()==(Long)a[0]&&(r.ticket().status()==QueueStatus.CALLED||r.ticket().status()==QueueStatus.SERVING)).findFirst();
        case "nextWaiting"->tickets.values().stream().filter(r->r.ticket().orgId()==(Long)a[0]&&r.ticket().status()==QueueStatus.WAITING).min(Comparator.comparing(r->r.ticket().sortAt()));
        case "call"->{change((Long)a[0],QueueStatus.CALLED,(Long)a[1],null);yield null;}
        case "transition"->{QueueAction action=(QueueAction)a[1];QueueStatus status=switch(action){case START_SERVING->QueueStatus.SERVING;case COMPLETE->QueueStatus.COMPLETED;case DEFER->QueueStatus.DEFERRED;case REQUEUE->QueueStatus.WAITING;case CANCEL->QueueStatus.CANCELLED;};change((Long)a[0],status,action==QueueAction.START_SERVING?(Long)a[2]:null,(String)a[3]);yield null;}
        default->throw unexpected(name);});}
    private static boolean active(QueueStatus status){return status!=QueueStatus.COMPLETED&&status!=QueueStatus.CANCELLED;}
    private void change(long id,QueueStatus status,Long user,String reason){var row=tickets.get(id);var t=row.ticket();var now=time();boolean requeue=status==QueueStatus.WAITING;var changed=new QueueTicket(id,t.studentId(),t.orgId(),t.termId(),t.queueDate(),t.seq(),status,requeue?now:t.sortAt(),t.deferCount()+(status==QueueStatus.DEFERRED?1:0),t.issuedBy(),requeue?null:user==null?t.servedBy():user,requeue?null:status==QueueStatus.CALLED?now:t.calledAt(),requeue?null:status==QueueStatus.SERVING?now:t.servingAt(),status==QueueStatus.COMPLETED?now:t.completedAt(),status==QueueStatus.CANCELLED?now:null,reason,t.createdAt(),now,t.issueRequestId());tickets.put(id,new QueueRow(changed,row.office(),row.studentNumber(),row.studentName()));}
}
