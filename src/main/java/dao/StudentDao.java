package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import model.Student;
import model.StudentDraft;
import model.StudentFilter;
import service.ServiceException;

final class StudentDao extends BaseDao implements StudentRepository {
    private static final String COLUMNS = "id, student_number, first_name, middle_name, last_name, suffix, program, year_level, section, is_active, created_at, updated_at";

    StudentDao(Connection connection, boolean writable) {
        super(connection, writable);
    }

    @Override public List<Student> search(StudentFilter f) throws ServiceException {
        return query("SELECT " + COLUMNS + " FROM students WHERE "
                + "(? = '' OR student_number LIKE ? ESCAPE '!' OR first_name LIKE ? ESCAPE '!' "
                + "OR last_name LIKE ? ESCAPE '!' OR CONCAT_WS(' ', first_name, middle_name, last_name) LIKE ? ESCAPE '!') "
                + "AND (? = '' OR program = ?) AND (? IS NULL OR year_level = ?) "
                + "AND (? = '' OR section = ?) AND (? IS NULL OR is_active = ?) "
                + "ORDER BY last_name, first_name, id LIMIT ? OFFSET ?", s -> {
            s.setString(1, f.query());
            for (int i = 2; i <= 5; i++) { s.setString(i, SearchSql.contains(f.query())); }
            s.setString(6, f.program()); s.setString(7, f.program());
            s.setObject(8, f.yearLevel()); s.setObject(9, f.yearLevel());
            s.setString(10, f.section()); s.setString(11, f.section());
            s.setObject(12, f.active()); s.setObject(13, f.active());
            s.setInt(14, f.pageSize() + 1); s.setInt(15, f.page() * f.pageSize());
        }, StudentDao::map);
    }

    @Override public Optional<Student> findById(long id) throws ServiceException {
        return query("SELECT " + COLUMNS + " FROM students WHERE id = ?", s -> s.setLong(1, id), StudentDao::map).stream().findFirst();
    }

    @Override public Optional<Student> findByNumber(String number) throws ServiceException {
        return query("SELECT " + COLUMNS + " FROM students WHERE student_number = ?", s -> s.setString(1, number), StudentDao::map).stream().findFirst();
    }

    @Override public Optional<Student> lockById(long id) throws ServiceException {
        requireWritable();
        return query("SELECT " + COLUMNS + " FROM students WHERE id = ? FOR UPDATE", s -> s.setLong(1, id), StudentDao::map).stream().findFirst();
    }

    @Override public long insert(StudentDraft draft) throws ServiceException {
        return insert("INSERT INTO students (student_number, first_name, middle_name, last_name, suffix, program, year_level, section, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", s -> bind(s, draft));
    }

    @Override public void update(long id, StudentDraft draft) throws ServiceException {
        update("UPDATE students SET student_number = ?, first_name = ?, middle_name = ?, last_name = ?, suffix = ?, program = ?, year_level = ?, section = ?, is_active = ? WHERE id = ?", s -> {
            bind(s, draft); s.setLong(10, id);
        });
    }

    @Override public boolean hasActiveTickets(long id) throws ServiceException {
        return !query("SELECT id FROM queue_tickets WHERE student_id = ? AND status IN ('waiting','called','serving','deferred') LIMIT 1", s -> s.setLong(1, id), r -> r.getLong(1)).isEmpty();
    }

    private static void bind(PreparedStatement s, StudentDraft d) throws SQLException {
        s.setString(1, d.studentNumber()); s.setString(2, d.firstName()); s.setString(3, d.middleName());
        s.setString(4, d.lastName()); s.setString(5, d.suffix()); s.setString(6, d.program());
        s.setInt(7, d.yearLevel()); s.setString(8, d.section()); s.setBoolean(9, d.active());
    }

    private static Student map(ResultSet r) throws SQLException {
        return new Student(r.getLong("id"), r.getString("student_number"), r.getString("first_name"),
                r.getString("middle_name"), r.getString("last_name"), r.getString("suffix"),
                r.getString("program"), r.getInt("year_level"), r.getString("section"), r.getBoolean("is_active"),
                r.getObject("created_at", LocalDateTime.class), r.getObject("updated_at", LocalDateTime.class));
    }
}
