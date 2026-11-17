package ui.util;

import java.awt.Component;
import java.awt.Cursor;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ServiceException;

/** Disables controls until work really completes; callbacks always run on the EDT. */
public final class UiTasks {
    private static final Logger LOG = LoggerFactory.getLogger(UiTasks.class);

    private UiTasks() {
    }

    public static <T> void run(Component owner, Callable<T> action, Consumer<T> success,
            Consumer<ServiceException> failure, JComponent... controls) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Start UI tasks on the EDT.");
        }
        Map<JComponent, Boolean> enabled = new IdentityHashMap<>();
        for (JComponent control : controls) {
            enabled.put(control, control.isEnabled());
            control.setEnabled(false);
        }
        Cursor previous = owner == null ? null : owner.getCursor();
        if (owner != null) {
            owner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return action.call();
            }

            @Override
            protected void done() {
                enabled.forEach(JComponent::setEnabled);
                if (owner != null) {
                    owner.setCursor(previous);
                }
                T result;
                try {
                    result = get();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    failure.accept(new ServiceException("The operation was interrupted. Please try again.", ex));
                    return;
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof ServiceException expected) {
                        if (expected.getCause() != null) {
                            LOG.warn("Background operation failed", expected);
                        }
                        failure.accept(expected);
                    } else {
                        LOG.error("Unexpected background failure", cause);
                        failure.accept(new ServiceException("The request could not be completed. Contact the administrator.", cause));
                    }
                    return;
                }
                success.accept(result);
            }
        }.execute();
    }
}
