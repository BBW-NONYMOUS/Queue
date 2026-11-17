package service;

import java.util.List;
import model.Session;
import model.User;
import model.AccountProfile;

/** Only implemented destinations are exposed; later phases register real screens. */
public final class NavigationService {
    public enum Destination { WORKSPACE, STUDENTS, OFFICES, USERS, FINES, PAYMENTS, QUEUE, BOARD, CLEARANCE, REPORTS, AUDIT, BACKUP, ACCOUNT }

    public record Entry(Destination destination, String label) {
    }

    public record Workspace(Session session, String title, String description, String assignment, List<Entry> entries) {
        public Workspace {
            entries = List.copyOf(entries);
        }
    }

    private final AuthService auth;

    public NavigationService(AuthService auth) {
        this.auth = auth;
    }

    public Workspace open(Session session, Destination destination) throws ServiceException {
        if (destination == null) {
            throw new ValidationException("Choose a workspace to open.");
        }
        AccountProfile profile = auth.currentProfile(session);
        Session current = new Session(session.token(), profile.user());
        User user = current.user();
        if (!allowed(user.role(), destination)) {
            throw new AuthorizationException("Your account does not have permission to open that screen.");
        }
        String title = switch (user.role()) {
            case ADMIN -> "Administration";
            case OFFICER -> "Office workspace";
            case CASHIER -> "Cashier workspace";
            case STUDENT -> "Student workspace";
        };
        String description = switch (user.role()) {
            case ADMIN -> "Your account manages campus records and system administration.";
            case OFFICER -> "Your account serves students in your assigned clearance office.";
            case CASHIER -> "Your account receives payments and issues student receipts.";
            case STUDENT -> "Your account is linked to your student record for queueing and clearance.";
        };
        java.util.ArrayList<Entry> entries = new java.util.ArrayList<>();
        entries.add(new Entry(Destination.WORKSPACE, title));
        for (Destination target : List.of(Destination.STUDENTS, Destination.OFFICES, Destination.USERS, Destination.FINES, Destination.PAYMENTS, Destination.QUEUE, Destination.BOARD,Destination.CLEARANCE,Destination.REPORTS,Destination.AUDIT,Destination.BACKUP)) {
            if (allowed(user.role(), target)) {
                String label = switch (target) { case STUDENTS -> "Students"; case OFFICES -> "Offices"; case USERS -> "Users"; case FINES -> "Fines"; case PAYMENTS -> "Payments"; case QUEUE -> "Office queue"; case BOARD -> "Now serving";case CLEARANCE -> "Clearance";case REPORTS -> "Reports";case AUDIT -> "Audit log";case BACKUP -> "Backup"; default -> throw new IllegalStateException(); };
                entries.add(new Entry(target, label));
            }
        }
        entries.add(new Entry(Destination.ACCOUNT, "My account"));
        return new Workspace(current, title, description, profile.assignment(), entries);
    }

    private static boolean allowed(model.Role role, Destination destination) {
        return switch (destination) {
            case WORKSPACE, ACCOUNT, CLEARANCE, REPORTS -> true;
            case AUDIT, BACKUP -> role == model.Role.ADMIN;
            case STUDENTS -> role != model.Role.STUDENT;
            case OFFICES -> role == model.Role.ADMIN || role == model.Role.OFFICER;
            case USERS -> role == model.Role.ADMIN;
            case FINES -> role != model.Role.STUDENT;
            case QUEUE -> role == model.Role.ADMIN || role == model.Role.OFFICER;
            case BOARD -> role != model.Role.STUDENT;
            case PAYMENTS -> role == model.Role.ADMIN || role == model.Role.CASHIER;
        };
    }
}
