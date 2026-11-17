package ui.util;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;
import service.ServiceException;
/** Three-second background reads. Old generations never deliver results after pause or navigation. */
public final class SwingPoller<T> implements AutoCloseable {
    private final Consumer<Boolean> activity;
    private final Timer timer; private final Callable<T> query; private final Consumer<T> success;
    private final Consumer<ServiceException> failure; private boolean enabled, running; private long generation;
    public SwingPoller(Callable<T> query, Consumer<T> success,Consumer<ServiceException> failure) {
        this(query,success,failure,active->{});
    }
    public SwingPoller(Callable<T> query, Consumer<T> success,Consumer<ServiceException> failure,Consumer<Boolean> activity) {
        this.query=query;this.success=success;this.failure=failure;this.activity=activity;timer=new Timer(3000,e->refresh());
    }
    public void start() { enabled=true;timer.start();refresh(); }
    public void pause() { enabled=false;timer.stop();generation++;activity.accept(false); }
    public void refresh() {
        if(!enabled||running)return;running=true;long expected=generation;activity.accept(true);
        new SwingWorker<T,Void>() {
            @Override protected T doInBackground() throws Exception { return query.call(); }
            @Override protected void done() {
                running=false;if(!enabled||expected!=generation)return;
                try { success.accept(get()); }
                catch(java.util.concurrent.ExecutionException ex) {
                    Throwable cause=ex.getCause();
                    if(cause instanceof ServiceException service)failure.accept(service);
                    else { org.slf4j.LoggerFactory.getLogger(SwingPoller.class).error("Queue refresh failed",cause);failure.accept(new ServiceException("The screen could not be refreshed. Try again.")); }
                } catch(InterruptedException ex) { Thread.currentThread().interrupt();failure.accept(new ServiceException("The refresh was interrupted.")); }
                finally { activity.accept(false); }
            }
        }.execute();
    }
    @Override public void close() { pause(); }
}
