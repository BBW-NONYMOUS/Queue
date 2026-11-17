package ui.util;
import java.util.concurrent.*;import java.util.concurrent.atomic.*;import javax.swing.*;
import org.junit.jupiter.api.*;import static org.junit.jupiter.api.Assertions.*;
@Timeout(15)
class SwingPollerTest {
    @Test void backgroundWorkDeliversOnEdtAndDoesNotOverlap()throws Exception {
        CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1),delivered=new CountDownLatch(1);AtomicInteger queries=new AtomicInteger();AtomicBoolean workerWasEdt=new AtomicBoolean(),callbackWasEdt=new AtomicBoolean();AtomicReference<SwingPoller<Integer>> holder=new AtomicReference<>();AtomicReference<Throwable> failure=new AtomicReference<>();
        SwingUtilities.invokeAndWait(()->{var poller=new SwingPoller<Integer>(()->{queries.incrementAndGet();workerWasEdt.set(SwingUtilities.isEventDispatchThread());entered.countDown();if(!release.await(5,TimeUnit.SECONDS))throw new TimeoutException();return 42;},value->{callbackWasEdt.set(SwingUtilities.isEventDispatchThread());delivered.countDown();},ex->{failure.set(ex);delivered.countDown();});holder.set(poller);poller.start();poller.refresh();poller.refresh();});
        try{assertTrue(entered.await(5,TimeUnit.SECONDS));assertEquals(1,queries.get());release.countDown();assertTrue(delivered.await(5,TimeUnit.SECONDS));assertNull(failure.get());assertFalse(workerWasEdt.get());assertTrue(callbackWasEdt.get());}
        finally{release.countDown();SwingUtilities.invokeAndWait(()->holder.get().close());}
    }
    @Test void pauseDiscardsAnInFlightResult()throws Exception {
        CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1),finished=new CountDownLatch(1);AtomicInteger deliveries=new AtomicInteger();AtomicReference<SwingPoller<Integer>> holder=new AtomicReference<>();
        SwingUtilities.invokeAndWait(()->{holder.set(new SwingPoller<>(()->{entered.countDown();try{if(!release.await(5,TimeUnit.SECONDS))throw new TimeoutException();return 1;}finally{finished.countDown();}},v->deliveries.incrementAndGet(),e->deliveries.incrementAndGet()));holder.get().start();});
        try{assertTrue(entered.await(5,TimeUnit.SECONDS));SwingUtilities.invokeAndWait(()->holder.get().pause());release.countDown();assertTrue(finished.await(5,TimeUnit.SECONDS));
            // SwingWorker batches done() onto the EDT; a short Swing timer drains that batch.
            CountDownLatch drained=new CountDownLatch(1);SwingUtilities.invokeAndWait(()->{Timer timer=new Timer(150,e->drained.countDown());timer.setRepeats(false);timer.start();});assertTrue(drained.await(5,TimeUnit.SECONDS));assertEquals(0,deliveries.get());
        }finally{release.countDown();SwingUtilities.invokeAndWait(()->holder.get().close());}
    }
}
