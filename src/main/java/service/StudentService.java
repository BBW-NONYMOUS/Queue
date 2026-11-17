package service;

import dao.UnitOfWork;
import java.util.List;
import model.*;

public final class StudentService {
    private final UnitOfWork transactions;
    private final AuthService auth;

    public StudentService(UnitOfWork transactions, AuthService auth) { this.transactions = transactions; this.auth = auth; }

    public Page<Student> search(Session session, StudentFilter input) throws ServiceException {
        if (input == null) { throw new ValidationException("Enter student search filters."); }
        Validation.page(input.page(), input.pageSize());
        if (input.yearLevel() != null && (input.yearLevel() < 1 || input.yearLevel() > 6)) { throw new ValidationException("Choose a year level from 1 to 6."); }
        StudentFilter filter = new StudentFilter(Validation.filter(input.query(), "Search", 100),
                Validation.filter(input.program(), "Program", 80), input.yearLevel(),
                Validation.filter(input.section(), "Section", 32), input.active(), input.page(), input.pageSize());
        return transactions.read(context -> {
            auth.require(context, session, Role.ADMIN, Role.OFFICER, Role.CASHIER);
            List<Student> rows = context.students().search(filter);
            return new Page<>(rows.subList(0, Math.min(rows.size(), filter.pageSize())), filter.page(), filter.pageSize(), rows.size() > filter.pageSize());
        });
    }

    public Student save(Session session, Student original, StudentDraft input) throws ServiceException {
        StudentDraft draft = validate(input);
        return transactions.write(null, context -> {
            User actor = auth.require(context, session, Role.ADMIN);
            context.auditActor(actor.id());
            Student current = null;
            if (original != null) {
                current = context.students().lockById(original.id()).orElseThrow(() -> new NotFoundException("Student"));
                Validation.unchanged(original.updatedAt(), current.updatedAt());
                if (current.active() && !draft.active() && context.students().hasActiveTickets(current.id())) {
                    throw new ValidationException("Complete or cancel the student's active tickets before deactivation.");
                }
            }
            Student duplicate = context.students().findByNumber(draft.studentNumber()).orElse(null);
            if (duplicate != null && (current == null || duplicate.id() != current.id())) {
                throw new ValidationException("That student number is already in use.");
            }
            long id;
            if (current == null) { id = context.students().insert(draft); }
            else { id = current.id(); context.students().update(id, draft); }
            return context.students().findById(id).orElseThrow(() -> new NotFoundException("Student"));
        });
    }

    private static StudentDraft validate(StudentDraft d) throws ValidationException {
        if (d == null) { throw new ValidationException("Enter the student details."); }
        if (d.yearLevel() < 1 || d.yearLevel() > 6) { throw new ValidationException("Choose a year level from 1 to 6."); }
        return new StudentDraft(Validation.required(d.studentNumber(), "Student number", 32),
                Validation.required(d.firstName(), "First name", 80), Validation.optional(d.middleName(), "Middle name", 80),
                Validation.required(d.lastName(), "Last name", 80), Validation.optional(d.suffix(), "Suffix", 16),
                Validation.required(d.program(), "Program", 80), d.yearLevel(), Validation.required(d.section(), "Section", 32), d.active());
    }
}
