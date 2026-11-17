package app;

import com.formdev.flatlaf.FlatLightLaf;
import config.AppConfig;
import config.DatabasePool;
import dao.JdbcUnitOfWork;
import java.awt.Font;
import java.awt.Window;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AuthService;
import service.NavigationService;
import service.StudentService;
import service.OfficeService;
import service.UserService;
import service.FineService;
import service.PaymentService;
import service.TermService;
import ui.LoginFrame;
import ui.MainFrame;
import ui.dialog.ChangePasswordDialog;
import ui.util.UiTasks;

/** Desktop composition root. Configuration/pool startup and shutdown run off EDT. */
public final class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private final AtomicReference<DatabasePool> pool = new AtomicReference<>();
    private AuthService auth;
    private NavigationService navigation;
    private StudentService students;
    private OfficeService offices;
    private UserService users;
    private FineService fines;
    private PaymentService payments;
    private TermService terms;
    private service.QueueService queue;
    private service.ClearanceService clearance;private service.ReportService reports;private service.AuditService audit;private service.BackupService backup;
    private LoginFrame login;
    private boolean stopping;

    public static void main(String[] args) {
        Application application = new Application();
        Runtime.getRuntime().addShutdownHook(new Thread(application::closePool, "queue-clearance-shutdown"));
        SwingUtilities.invokeLater(application::start);
    }

    private void start() {
        FlatLightLaf.setup();
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.minimumHeight", 36);
        UIManager.put("Component.arc", 8);
        UiTasks.run(null, () -> {
            AppConfig config = AppConfig.load();
            DatabasePool database = new DatabasePool(config);
            pool.set(database);
            JdbcUnitOfWork transactions = new JdbcUnitOfWork(database.dataSource());
            AuthService authentication = new AuthService(transactions, config.bcryptCost());
            return new RuntimeServices(config, authentication, new StudentService(transactions, authentication),
                    new OfficeService(transactions, authentication), new UserService(transactions, authentication, config.bcryptCost()),
                    new FineService(transactions, authentication), new PaymentService(transactions, authentication), new TermService(transactions, authentication), new service.QueueService(transactions, authentication),new service.ClearanceService(transactions,authentication),
                    new service.ReportService(transactions,authentication,config.outputs()),new service.AuditService(transactions,authentication),new service.BackupService(transactions,authentication,config.outputs()));
        }, services -> {
            UIManager.put("defaultFont", new Font(services.config().fontFamily(), Font.PLAIN, services.config().fontSize()));
            auth = services.auth();
            navigation = new NavigationService(auth);
            students = services.students(); offices = services.offices(); users = services.users();
            fines = services.fines(); payments = services.payments(); terms = services.terms();
            queue = services.queue();clearance=services.clearance();reports=services.reports();audit=services.audit();backup=services.backup();
            showLogin();
        }, failure -> {
            JOptionPane.showMessageDialog(null, failure.getMessage(), "Queue-Clearance | Cannot start", JOptionPane.ERROR_MESSAGE);
            shutdown();
        });
    }

    private void showLogin() {
        login = new LoginFrame(auth, this::signedIn, this::shutdown);
        login.setVisible(true);
    }

    private void signedIn(Session session) {
        Session current = session;
        if (session.user().mustChangePassword()) {
            ChangePasswordDialog dialog = new ChangePasswordDialog(login, auth, session, true);
            dialog.setVisible(true);
            if (dialog.updatedSession().isEmpty()) {
                auth.logout(session);
                login.showMessage("Sign-in cancelled. Your password must be changed to continue.", true);
                return;
            }
            current = dialog.updatedSession().orElseThrow();
        }
        MainFrame main = new MainFrame(auth, navigation, students, offices, users, fines, payments, terms, queue, clearance,reports,audit,backup,current, this::showLogin, this::shutdown);
        login.dispose();
        login = null;
        main.setVisible(true);
    }

    private void shutdown() {
        if (stopping) {
            return;
        }
        stopping = true;
        for (Window window : Window.getWindows()) {
            window.dispose();
        }
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                closePool();
                return null;
            }

            @Override protected void done() {
                System.exit(0);
            }
        }.execute();
    }

    private void closePool() {
        DatabasePool database = pool.getAndSet(null);
        if (database != null) {
            try {
                database.close();
            } catch (RuntimeException ex) {
                LOG.warn("Database pool shutdown failed", ex);
            }
        }
    }

    private record RuntimeServices(AppConfig config, AuthService auth, StudentService students, OfficeService offices, UserService users,
            FineService fines, PaymentService payments, TermService terms, service.QueueService queue,service.ClearanceService clearance,service.ReportService reports,service.AuditService audit,service.BackupService backup) {
    }
}
