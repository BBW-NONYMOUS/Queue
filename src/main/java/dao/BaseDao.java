package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import service.ServiceException;

/** Shared prepared-statement execution; the transaction owns the connection. */
public abstract class BaseDao {
    private final Connection connection;
    private final boolean writable;

    protected BaseDao(Connection connection, boolean writable) {
        this.connection = connection;
        this.writable = writable;
    }

    protected final <T> List<T> query(String sql, Binder binder, Mapper<T> mapper) throws ServiceException {
        assertBackgroundThread();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(10);
            binder.bind(statement);
            try (ResultSet rows = statement.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rows.next()) {
                    result.add(mapper.map(rows));
                }
                return List.copyOf(result);
            }
        } catch (SQLException ex) {
            throw SqlErrors.translate(ex);
        }
    }

    protected final int update(String sql, Binder binder) throws ServiceException {
        requireWritable();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setQueryTimeout(10);
            binder.bind(statement);
            return statement.executeUpdate();
        } catch (SQLException ex) {
            throw SqlErrors.translate(ex);
        }
    }

    protected final long insert(String sql, Binder binder) throws ServiceException {
        requireWritable();
        try (PreparedStatement statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setQueryTimeout(10);
            binder.bind(statement);
            if (statement.executeUpdate() != 1) {
                throw new service.DataAccessException("The new record could not be saved.", null);
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new service.DataAccessException("The new record identifier could not be read.", null);
                }
                return keys.getLong(1);
            }
        } catch (SQLException ex) {
            throw SqlErrors.translate(ex);
        }
    }

    protected final void requireWritable() {
        assertBackgroundThread();
        if (!writable) {
            throw new IllegalStateException("A write or row lock requires a write transaction.");
        }
    }

    static void assertBackgroundThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Database work must run in a SwingWorker, never on the EDT.");
        }
    }

    @FunctionalInterface
    protected interface Binder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    protected interface Mapper<T> {
        T map(ResultSet row) throws SQLException;
    }
}
