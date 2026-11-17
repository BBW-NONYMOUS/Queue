package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.sql.PreparedStatement;
import model.UserAccount;
import model.UserDraft;
import model.UserFilter;
import model.Role;
import model.User;
import service.DataAccessException;
import service.ServiceException;

final class UserDao extends BaseDao implements UserRepository {
    private static final String USER_COLUMNS = """
            u.id, u.username, u.display_name, u.role,
                u.student_id, u.org_id, u.is_active, u.must_change_password,
                u.last_login_at, u.created_at, u.updated_at,
                COALESCE(s.is_active, FALSE) AS student_active,
                COALESCE(o.is_active, FALSE) AS office_active,
                s.student_number, o.code AS office_code, o.name AS office_name
            """;
    private static final String FROM_ACCOUNT = """
            FROM users u
            LEFT JOIN students s ON s.id = u.student_id
            LEFT JOIN organizations o ON o.id = u.org_id
            """;
    private static final String SELECT_ACCOUNT = "SELECT u.password_hash, " + USER_COLUMNS + FROM_ACCOUNT;
    private static final String SELECT_USERS = "SELECT " + USER_COLUMNS + FROM_ACCOUNT;

    UserDao(Connection connection, boolean writable) {
        super(connection, writable);
    }

    @Override
    public Optional<LoginAccount> findByUsername(String username) throws ServiceException {
        return query(SELECT_ACCOUNT + " WHERE u.username = ?", statement -> statement.setString(1, username),
                UserDao::mapAccount).stream().findFirst();
    }

    @Override
    public Optional<LoginAccount> findById(long id) throws ServiceException {
        return query(SELECT_ACCOUNT + " WHERE u.id = ?", statement -> statement.setLong(1, id),
                UserDao::mapAccount).stream().findFirst();
    }

    @Override
    public Optional<LoginAccount> lockById(long id) throws ServiceException {
        requireWritable();
        if (query("SELECT id FROM users WHERE id = ? FOR UPDATE", s -> s.setLong(1, id), r -> r.getLong(1)).isEmpty()) {
            return Optional.empty();
        }
        return findById(id);
    }

    @Override
    public void recordLogin(long id) throws ServiceException {
        int changed = update("UPDATE users SET last_login_at = CURRENT_TIMESTAMP(6) WHERE id = ?",
                statement -> statement.setLong(1, id));
        requireOne(changed);
    }

    @Override
    public void changePassword(long id, String hash) throws ServiceException {
        int changed = update("UPDATE users SET password_hash = ?, must_change_password = FALSE WHERE id = ?",
                statement -> { statement.setString(1, hash); statement.setLong(2, id); });
        requireOne(changed);
    }

    private static void requireOne(int changed) throws DataAccessException {
        if (changed != 1) {
            throw new DataAccessException("The account changed. Sign in again before continuing.", null);
        }
    }

    private static User mapUser(ResultSet row) throws SQLException {
        return new User(row.getLong("id"), row.getString("username"), row.getString("display_name"),
                Role.valueOf(row.getString("role").toUpperCase(java.util.Locale.ROOT)),
                row.getObject("student_id", Long.class), row.getObject("org_id", Long.class),
                row.getBoolean("is_active"), row.getBoolean("must_change_password"),
                row.getObject("last_login_at", LocalDateTime.class),
                row.getObject("created_at", LocalDateTime.class), row.getObject("updated_at", LocalDateTime.class));
    }

    private static LoginAccount mapAccount(ResultSet row) throws SQLException {
        return new LoginAccount(mapUser(row), row.getString("password_hash"),
                row.getBoolean("student_active"), row.getBoolean("office_active"),
                row.getString("student_number"), row.getString("office_code"), row.getString("office_name"));
    }

    @Override public List<UserAccount> search(UserFilter f) throws ServiceException {
        return query(SELECT_USERS + " WHERE (? = '' OR u.username LIKE ? ESCAPE '!' OR u.display_name LIKE ? ESCAPE '!' "
                + "OR s.student_number LIKE ? ESCAPE '!' OR o.code LIKE ? ESCAPE '!') "
                + "AND (? IS NULL OR u.role = ?) AND (? IS NULL OR u.is_active = ?) ORDER BY u.username, u.id LIMIT ? OFFSET ?", s -> {
            s.setString(1, f.query());
            for (int i = 2; i <= 5; i++) { s.setString(i, SearchSql.contains(f.query())); }
            s.setString(6, f.role() == null ? null : f.role().databaseValue());
            s.setString(7, f.role() == null ? null : f.role().databaseValue());
            s.setObject(8, f.active()); s.setObject(9, f.active());
            s.setInt(10, f.pageSize() + 1); s.setInt(11, f.page() * f.pageSize());
        }, r -> new UserAccount(mapUser(r), r.getString("student_number"), r.getString("office_code")));
    }

    @Override public List<Long> lockActiveAdministrators() throws ServiceException {
        requireWritable();
        return query("SELECT id FROM users WHERE role = 'admin' AND is_active = TRUE ORDER BY id FOR UPDATE", s -> {}, r -> r.getLong(1));
    }

    @Override public long insert(UserDraft draft, Long studentId, String hash) throws ServiceException {
        return insert("INSERT INTO users (username, display_name, role, student_id, org_id, is_active, must_change_password, password_hash) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", s -> {
            bind(s, draft, studentId); s.setString(8, hash);
        });
    }

    @Override public void update(long id, UserDraft draft, Long studentId) throws ServiceException {
        requireOne(update("UPDATE users SET username = ?, display_name = ?, role = ?, student_id = ?, org_id = ?, is_active = ?, must_change_password = ? WHERE id = ?", s -> {
            bind(s, draft, studentId); s.setLong(8, id);
        }));
    }

    @Override public void resetPassword(long id, String hash) throws ServiceException {
        requireOne(update("UPDATE users SET password_hash = ?, must_change_password = TRUE WHERE id = ?", s -> {
            s.setString(1, hash); s.setLong(2, id);
        }));
    }

    @Override public boolean hasServingTickets(long id) throws ServiceException {
        return !query("SELECT id FROM queue_tickets WHERE served_by = ? AND status IN ('called','serving') LIMIT 1", s -> s.setLong(1, id), r -> r.getLong(1)).isEmpty();
    }

    @Override public Optional<LoginAccount> findByStudentId(long studentId) throws ServiceException {
        return query(SELECT_ACCOUNT + " WHERE u.student_id = ?", s -> s.setLong(1, studentId), UserDao::mapAccount).stream().findFirst();
    }

    private static void bind(PreparedStatement s, UserDraft d, Long studentId) throws SQLException {
        s.setString(1, d.username()); s.setString(2, d.displayName()); s.setString(3, d.role().databaseValue());
        s.setObject(4, studentId); s.setObject(5, d.orgId()); s.setBoolean(6, d.active()); s.setBoolean(7, d.mustChangePassword());
    }
}
