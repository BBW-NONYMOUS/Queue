package dao.support;
import java.util.*;import java.util.concurrent.*;
/** Starts independent client requests together; every result/failure is inspected by the test. */
public final class ConcurrentCalls {
    private ConcurrentCalls() { }
    public record Outcome<T>(T value,Exception error) {public boolean succeeded(){return error==null;}}
    public static <T>List<Outcome<T>> run(List<Callable<T>> requests)throws Exception {
        ExecutorService executor=Executors.newFixedThreadPool(requests.size());CountDownLatch ready=new CountDownLatch(requests.size()),start=new CountDownLatch(1);
        List<Future<Outcome<T>>> futures=new ArrayList<>();
        try {
            for(Callable<T> request:requests)futures.add(executor.submit(()->{ready.countDown();if(!start.await(10,TimeUnit.SECONDS))throw new TimeoutException("Concurrent start timed out");try{return new Outcome<>(request.call(),null);}catch(Exception ex){return new Outcome<T>(null,ex);}}));
            if(!ready.await(10,TimeUnit.SECONDS))throw new TimeoutException("Workers did not become ready");start.countDown();
            List<Outcome<T>> outcomes=new ArrayList<>();for(var future:futures)outcomes.add(future.get(45,TimeUnit.SECONDS));return List.copyOf(outcomes);
        } finally {
            start.countDown();for(var future:futures)if(!future.isDone())future.cancel(true);executor.shutdownNow();
            if(!executor.awaitTermination(10,TimeUnit.SECONDS))throw new IllegalStateException("Integration workers did not terminate");
        }
    }
}
