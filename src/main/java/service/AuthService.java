package service;

import dao.DaoContext;
import dao.LoginAccount;
import dao.UnitOfWork;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import model.Role;
import model.AccountProfile;
import model.Session;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

/** Authentication and live authorization. No JDBC types escape into this layer. */
public final class AuthService {
    private static final String DUMMY_HASH = "$2a$12$Buvx3PK4q4cmVTzSS942Mu6DpQHzxoYF25..WWvHCyu/J2ok12a2.";
    private final UnitOfWork transactions;
    private final int bcryptCost;
    private final Map<UUID, SessionState> sessions = new ConcurrentHashMap<>();

    public AuthService(UnitOfWork transactions, int bcryptCost) {
        this.transactions = Objects.requireNonNull(transactions);
        if (bcryptCost < 12 || bcryptCost > 16) {
            throw new IllegalArgumentException("BCrypt cost must be between 12 and 16.");
        }
        this.bcryptCost = bcryptCost;
    }

    /** Takes ownership of the password array and erases it on every outcome. */
    public Session login(String username, char[] password) throws ServiceException {
        try {
            String name = username == null ? "" : username.strip();
            if (name.isEmpty() || name.length() > 64 || password == null || password.length == 0) {
                throw new ValidationException("Enter your username and password.");
            }
            String candidate = new String(password);
            if (candidate.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new AuthenticationException();
            }
            LoginAccount observed = transactions.read(context ->
                    context.users().findByUsername(name).orElse(null));
            String hash = observed == null ? DUMMY_HASH : observed.passwordHash();
            boolean matches = matches(candidate, hash);
            if (observed == null || !matches || !observed.eligible()) {
                throw new AuthenticationException();
            }
            // BCrypt runs outside the row lock. Recheck credentials/eligibility
            // under the write lock so a concurrent reset/deactivation wins.
            User current = transactions.write(observed.user().id(), context -> {
                lockAssignment(context, observed.user());
                LoginAccount locked = context.users().lockById(observed.user().id())
                        .orElseThrow(AuthenticationException::new);
                if (!locked.eligible() || !sameAssignment(locked.user(), observed.user()) || !locked.passwordHash().equals(observed.passwordHash())) {
                    throw new AuthenticationException();
                }
                context.users().recordLogin(locked.user().id());
                return context.users().findById(locked.user().id())
                        .orElseThrow(AuthenticationException::new).user();
            });
            UUID token = UUID.randomUUID();
            sessions.put(token, new SessionState(current, observed.passwordHash()));
            return new Session(token, current);
        } finally {
            erase(password);
        }
    }

    public Session currentSession(Session session) throws ServiceException {
        return transactions.read(context -> new Session(sessionToken(session),
                require(context, session)));
    }

    public AccountProfile currentProfile(Session session) throws ServiceException {
        return transactions.read(context -> {
            LoginAccount account = requireAccount(context, session, false);
            String assignment = switch (account.user().role()) {
                case ADMIN -> "Campus administration";
                case CASHIER -> "Campus cashier";
                case OFFICER -> account.officeCode() + " - " + account.officeName();
                case STUDENT -> account.studentNumber();
            };
            return new AccountProfile(account.user(), assignment);
        });
    }

    /**
     * Future services call this INSIDE their transaction before any protected
     * read/write. It checks current DB roles, assignments and account state.
     */
    public User require(DaoContext context, Session session, Role... allowedRoles) throws ServiceException {
        User user = requireAccount(context, session, false).user();
        if (allowedRoles.length > 0 && Arrays.stream(allowedRoles).noneMatch(role -> role == user.role())) {
            throw new AuthorizationException("Your account does not have permission to do that.");
        }
        return user;
    }

    public Session changePassword(Session session, char[] currentPassword, char[] newPassword)
            throws ServiceException {
        try {
            validateNewPassword(newPassword);
            if (currentPassword == null || currentPassword.length == 0) {
                throw new ValidationException("Enter your current password.");
            }
            String oldValue = new String(currentPassword);
            String newValue = new String(newPassword);
            if (oldValue.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new AuthenticationException();
            }
            if (oldValue.equals(newValue)) {
                throw new ValidationException("Choose a password different from your current password.");
            }
            LoginAccount observed = transactions.read(context -> requireAccount(context, session, true));
            if (!matches(oldValue, observed.passwordHash())) {
                throw new AuthenticationException();
            }
            String hash = BCrypt.hashpw(newValue, BCrypt.gensalt(bcryptCost));
            long id = registeredUserId(session);
            User changed = transactions.write(id, context -> {
                // Check that logout did not revoke this session during hashing.
                registeredUserId(session);
                lockAssignment(context, observed.user());
                LoginAccount locked = context.users().lockById(id).orElseThrow(AuthenticationException::new);
                if (!locked.eligible() || !sameAssignment(locked.user(), observed.user()) || !locked.passwordHash().equals(observed.passwordHash())) {
                    throw new AuthenticationException();
                }
                context.users().changePassword(id, hash);
                return context.users().findById(id).orElseThrow(AuthenticationException::new).user();
            });
            // Revoke other local sessions for this account after a password reset.
            sessions.computeIfPresent(session.token(), (token, state) -> new SessionState(changed, hash));
            sessions.entrySet().removeIf(entry -> entry.getValue().user().id() == id && !entry.getKey().equals(session.token()));
            return new Session(session.token(), changed);
        } finally {
            erase(currentPassword);
            erase(newPassword);
        }
    }

    /** Logout has no database write; its local token becomes unusable immediately. */
    public void logout(Session session) {
        if (session != null && session.token() != null) {
            sessions.remove(session.token());
        }
    }

    private LoginAccount requireAccount(DaoContext context, Session session, boolean allowPasswordChange)
            throws ServiceException {
        long id = registeredUserId(session);
        LoginAccount account = context.users().findById(id).orElseThrow(AuthenticationException::new);
        SessionState state = sessions.get(session.token());
        if (state == null || !sameAssignment(account.user(), state.user()) || !account.passwordHash().equals(state.passwordHash())) {
            sessions.remove(session.token());
            throw new AuthorizationException("Your password or session changed. Please sign in again.");
        }
        if (!account.eligible()) {
            sessions.remove(session.token());
            throw new AuthorizationException("Your account is no longer active. Please sign out.");
        }
        if (!allowPasswordChange && account.user().mustChangePassword()) {
            throw new AuthorizationException("Sign out and sign in again to complete the required password change.");
        }
        return account;
    }

    private long registeredUserId(Session session) throws AuthorizationException {
        UUID token = sessionToken(session);
        SessionState state = sessions.get(token);
        if (state == null) {
            throw new AuthorizationException("Your session has ended. Please sign in again.");
        }
        return state.user().id();
    }

    private static UUID sessionToken(Session session) throws AuthorizationException {
        if (session == null || session.token() == null) {
            throw new AuthorizationException("Please sign in before continuing.");
        }
        return session.token();
    }

    private static boolean matches(String candidate, String hash) throws AuthenticationException {
        // jBCrypt 0.4 supports $2a$. A malformed imported hash cannot crash the UI.
        if (hash == null || !hash.matches("\\$2a\\$(0[4-9]|1[0-6])\\$[./A-Za-z0-9]{53}")) {
            BCrypt.checkpw(candidate, DUMMY_HASH);
            throw new AuthenticationException();
        }
        try {
            return BCrypt.checkpw(candidate, hash);
        } catch (IllegalArgumentException ex) {
            throw new AuthenticationException();
        }
    }

    private static void validateNewPassword(char[] password) throws ValidationException {
        if (password == null || password.length < 12 || new String(password).isBlank()
                || new String(password).getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ValidationException("Use at least 12 characters and at most 72 UTF-8 bytes.");
        }
    }

    private static void erase(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }

    private static boolean sameAssignment(User first, User second) {
        return first.role() == second.role() && Objects.equals(first.studentId(), second.studentId()) && Objects.equals(first.orgId(), second.orgId());
    }

    private static void lockAssignment(DaoContext context, User user) throws ServiceException {
        if (user.role() == Role.OFFICER && !context.organizations().lockById(user.orgId()).orElseThrow(AuthenticationException::new).active()) {
            throw new AuthenticationException();
        }
        if (user.role() == Role.STUDENT && !context.students().lockById(user.studentId()).orElseThrow(AuthenticationException::new).active()) {
            throw new AuthenticationException();
        }
    }

    private record SessionState(User user, String passwordHash) {
        @Override public String toString() { return "SessionState[userId=" + user.id() + ", passwordHash=REDACTED]"; }
    }
}
