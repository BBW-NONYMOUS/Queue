package service;

import dao.LoginAccount;
import dao.UnitOfWork;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

public final class UserService {
    private final UnitOfWork transactions;
    private final AuthService auth;
    private final int bcryptCost;

    public UserService(UnitOfWork transactions, AuthService auth, int bcryptCost) {
        if (bcryptCost < 12 || bcryptCost > 16) { throw new IllegalArgumentException("BCrypt cost must be 12 to 16."); }
        this.transactions = transactions; this.auth = auth; this.bcryptCost = bcryptCost;
    }

    public Page<UserAccount> search(Session session, UserFilter input) throws ServiceException {
        if (input == null) { throw new ValidationException("Enter user search filters."); }
        Validation.page(input.page(), input.pageSize());
        UserFilter filter = new UserFilter(Validation.filter(input.query(), "Search", 100), input.role(), input.active(), input.page(), input.pageSize());
        return transactions.read(context -> {
            auth.require(context, session, Role.ADMIN);
            List<UserAccount> rows = context.users().search(filter);
            return new Page<>(rows.subList(0, Math.min(rows.size(), filter.pageSize())), filter.page(), filter.pageSize(), rows.size() > filter.pageSize());
        });
    }

    public List<Organization> offices(Session session) throws ServiceException {
        return transactions.read(context -> { auth.require(context, session, Role.ADMIN); return context.organizations().list(); });
    }

    /** Owns and erases the supplied temporary password, including on failure. */
    public UserAccount save(Session session, UserAccount original, UserDraft input, char[] temporaryPassword) throws ServiceException {
        try {
            UserDraft draft = validate(input);
            boolean reset = temporaryPassword != null && temporaryPassword.length > 0;
            if (original == null && !reset) { throw new ValidationException("Enter an initial password for the new account."); }
            if (reset) { validatePassword(temporaryPassword); }
            transactions.read(context -> auth.require(context, session, Role.ADMIN));
            // Never hold administrator/assignment row locks while hashing.
            String hash = reset ? BCrypt.hashpw(new String(temporaryPassword), BCrypt.gensalt(bcryptCost)) : null;
            return transactions.write(null, context -> {
                auth.require(context, session, Role.ADMIN);
                List<Long> admins = context.users().lockActiveAdministrators();
                User actor = auth.require(context, session, Role.ADMIN);
                context.auditActor(actor.id());
                LoginAccount observed = original == null ? null : context.users().findById(original.user().id()).orElseThrow(() -> new NotFoundException("User"));
                if (observed != null) { Validation.unchanged(original.user().updatedAt(), observed.user().updatedAt()); }
                java.util.TreeSet<Long> officeIds = new java.util.TreeSet<>();
                if (observed != null && observed.user().orgId() != null) { officeIds.add(observed.user().orgId()); }
                if (draft.orgId() != null) { officeIds.add(draft.orgId()); }
                for (long officeId : officeIds) { context.organizations().lockById(officeId).orElseThrow(() -> new NotFoundException("Office")); }
                Long studentId = null;
                if (draft.role() == Role.OFFICER) {
                    Organization office = context.organizations().findById(draft.orgId()).orElseThrow(() -> new NotFoundException("Office"));
                    if (!office.active() && (observed == null || !Objects.equals(observed.user().orgId(), office.id()) || draft.active())) {
                        throw new ValidationException("Choose an active office for an active officer account.");
                    }
                }
                if (draft.role() == Role.STUDENT) {
                    Student observedStudent = context.students().findByNumber(draft.studentNumber()).orElseThrow(() -> new NotFoundException("Student number"));
                    Student student = context.students().lockById(observedStudent.id()).orElseThrow(() -> new NotFoundException("Student"));
                    if (!student.studentNumber().equals(observedStudent.studentNumber())) { throw new RecordChangedException(); }
                    if (!student.active() && (observed == null || !Objects.equals(observed.user().studentId(), student.id()) || draft.active())) {
                        throw new ValidationException("Choose an active student record for an active student account.");
                    }
                    studentId = student.id();
                    LoginAccount linked = context.users().findByStudentId(studentId).orElse(null);
                    if (linked != null && (observed == null || linked.user().id() != observed.user().id())) {
                        throw new ValidationException("That student already has a user account. Edit or reactivate the existing account.");
                    }
                }
                LoginAccount current = null;
                if (original != null) {
                    current = context.users().lockById(original.user().id()).orElseThrow(() -> new NotFoundException("User"));
                    Validation.unchanged(original.user().updatedAt(), current.user().updatedAt());
                    if (current.user().id() == actor.id() && (reset || draft.role() != Role.ADMIN || !draft.active() || draft.mustChangePassword())) {
                        throw new ValidationException("Keep your own administrator account active. Use My account to change its password.");
                    }
                    if (current.user().role() == Role.ADMIN && current.user().active()
                            && (!draft.active() || draft.role() != Role.ADMIN) && admins.size() <= 1) {
                        throw new ValidationException("At least one active administrator must remain.");
                    }
                    if (current.user().role() == Role.OFFICER && (!draft.active() || draft.role() != Role.OFFICER
                            || !Objects.equals(current.user().orgId(), draft.orgId())) && context.users().hasServingTickets(current.user().id())) {
                        throw new ValidationException("Finish or requeue this officer's called or serving tickets before changing the assignment.");
                    }
                }
                LoginAccount duplicate = context.users().findByUsername(draft.username()).orElse(null);
                if (duplicate != null && (current == null || duplicate.user().id() != current.user().id())) {
                    throw new ValidationException("That username is already in use.");
                }
                boolean mustChange = reset || draft.mustChangePassword();
                if (current != null && current.user().mustChangePassword() && !reset) { mustChange = true; }
                UserDraft effective = new UserDraft(draft.username(), draft.displayName(), draft.role(), draft.studentNumber(), draft.orgId(), draft.active(), mustChange);
                long id;
                if (current == null) { id = context.users().insert(effective, studentId, hash); }
                else {
                    id = current.user().id(); context.users().update(id, effective, studentId);
                    if (reset) { context.users().resetPassword(id, hash); }
                }
                LoginAccount saved = context.users().findById(id).orElseThrow(() -> new NotFoundException("User"));
                return new UserAccount(saved.user(), saved.studentNumber(), saved.officeCode());
            });
        } finally {
            if (temporaryPassword != null) { Arrays.fill(temporaryPassword, '\0'); }
        }
    }

    private static UserDraft validate(UserDraft d) throws ValidationException {
        if (d == null || d.role() == null) { throw new ValidationException("Choose the account role."); }
        String username = Validation.required(d.username(), "Username", 64);
        if (!username.matches("[A-Za-z0-9][A-Za-z0-9._-]{2,63}")) { throw new ValidationException("Use 3 to 64 letters, numbers, dots, underscores or hyphens for the username."); }
        String number = Validation.optional(d.studentNumber(), "Student number", 32);
        if (d.role() == Role.STUDENT && (number == null || d.orgId() != null)) { throw new ValidationException("A student account needs a student number and no office assignment."); }
        if (d.role() == Role.OFFICER && (d.orgId() == null || d.orgId() <= 0 || number != null)) { throw new ValidationException("An officer needs one office and no student assignment."); }
        if ((d.role() == Role.ADMIN || d.role() == Role.CASHIER) && (number != null || d.orgId() != null)) { throw new ValidationException("Administrators and cashiers have no student or office assignment."); }
        return new UserDraft(username, Validation.required(d.displayName(), "Display name", 160), d.role(), number, d.orgId(), d.active(), d.mustChangePassword());
    }

    private static void validatePassword(char[] password) throws ValidationException {
        String value = new String(password);
        if (value.isBlank() || password.length < 12 || value.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ValidationException("Use at least 12 characters and at most 72 UTF-8 bytes for the initial password.");
        }
    }
}
