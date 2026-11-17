package dao;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.CommitUncertainException;
import service.DataAccessException;
import service.ServiceException;

/** One pooled connection per service transaction, including safe audit cleanup. */
public final class JdbcUnitOfWork implements UnitOfWork {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcUnitOfWork.class);
    private final HikariDataSource pool;
    private volatile boolean serverVerified;

    public JdbcUnitOfWork(HikariDataSource pool) {
        this.pool = Objects.requireNonNull(pool);
    }

    @Override
    public <T> T read(Work<T> work) throws ServiceException {
        return execute(false, null, work, Connection.TRANSACTION_READ_COMMITTED);
    }

    @Override
    public <T> T write(Long actorUserId, Work<T> work) throws ServiceException {
        return execute(true, actorUserId, work, Connection.TRANSACTION_READ_COMMITTED);
    }

    @Override public <T>T snapshot(Work<T> work) throws ServiceException {
        return execute(false,null,work,Connection.TRANSACTION_REPEATABLE_READ);
    }

    private <T> T execute(boolean writable, Long actor, Work<T> work, int isolation) throws ServiceException {
        BaseDao.assertBackgroundThread();
        Objects.requireNonNull(work);
        Connection connection = null;
        boolean reusable = true;
        try {
            connection = pool.getConnection();
            prepareSession(connection);
            connection.setTransactionIsolation(isolation);
            connection.setAutoCommit(false);
            Context context = new Context(connection, writable);
            setAudit(connection, actor, UUID.randomUUID().toString());
            T result = work.execute(context);
            try {
                connection.commit();
            } catch (SQLException ex) {
                if (writable) {
                    throw new CommitUncertainException(ex);
                }
                throw ex;
            }
            return result;
        } catch (SQLException ex) {
            reusable = rollback(connection, ex);
            throw SqlErrors.translate(ex);
        } catch (ServiceException | RuntimeException | Error ex) {
            reusable = rollback(connection, ex);
            throw ex;
        } finally {
            release(connection, reusable);
        }
    }

    private void prepareSession(Connection connection) throws SQLException, ServiceException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SET SESSION time_zone = ?,
                    sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION'
                """)) {
            statement.setQueryTimeout(10);
            statement.setString(1, "+08:00");
            statement.execute();
        }
        // Check each newly initialized pool once; no assumption about XAMPP's server.
        if (!serverVerified) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT VERSION()")) {
                statement.setQueryTimeout(10);
                try (ResultSet rows = statement.executeQuery()) {
                    if (!rows.next() || !rows.getString(1).matches("8\\.4\\..*")
                            || rows.getString(1).toLowerCase(java.util.Locale.ROOT).contains("mariadb")) {
                        throw new DataAccessException("This application requires MySQL 8.4. Contact the administrator.", null);
                    }
                }
            }
            serverVerified = true;
        }
    }

    private static void setAudit(Connection connection, Long actor, String request) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SET @audit_user_id = ?, @audit_request_id = ?")) {
            statement.setQueryTimeout(10);
            if (actor == null) {
                statement.setNull(1, Types.BIGINT);
            } else {
                statement.setLong(1, actor);
            }
            statement.setString(2, request);
            statement.execute();
        }
    }

    private static boolean rollback(Connection connection, Throwable failure) {
        if (connection == null) {
            return true;
        }
        try {
            connection.rollback();
            return true;
        } catch (SQLException ex) {
            failure.addSuppressed(ex);
            return false;
        }
    }

    private void release(Connection connection, boolean reusable) {
        if (connection == null) {
            return;
        }
        try {
            if (reusable) {
                // User variables survive ROLLBACK and are not reset by HikariCP.
                setAudit(connection, null, null);
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            reusable = false;
            LOG.warn("Discarding a connection after audit context reset failed", ex);
        }
        try {
            if (!reusable) {
                pool.evictConnection(connection);
            }
        } catch (RuntimeException ex) {
            LOG.warn("Connection eviction failed during shutdown", ex);
        } finally {
            try {
                connection.close();
            } catch (SQLException ex) {
                LOG.warn("Connection close failed", ex);
            }
        }
    }

    private static final class Context implements DaoContext {
        private final Connection connection;
        private final boolean writable;
        private final UserRepository users;
        private final StudentRepository students;
        private final OrganizationRepository organizations;
        private final FinanceRepository finance;
        private final QueueRepository queue;
        private final ClearanceRepository clearance;
        private final OversightRepository oversight;

        private Context(Connection connection, boolean writable) {
            this.connection = connection;
            this.writable = writable;
            this.users = new UserDao(connection, writable);
            this.students = new StudentDao(connection, writable);
            this.organizations = new OrganizationDao(connection, writable);
            this.finance = new FinanceDao(connection, writable);
            this.queue = new QueueDao(connection, writable);
            this.clearance = new ClearanceDao(connection,writable);
            this.oversight = new OversightDao(connection,writable);
        }

        @Override
        public UserRepository users() {
            return users;
        }

        @Override public StudentRepository students() { return students; }
        @Override public OrganizationRepository organizations() { return organizations; }
        @Override public FinanceRepository finance() { return finance; }
        @Override public QueueRepository queue() { return queue; }
        @Override public ClearanceRepository clearance(){return clearance;}
        @Override public OversightRepository oversight(){return oversight;}

        @Override
        public void auditActor(long userId) throws ServiceException {
            if (!writable || userId <= 0) {
                throw new IllegalStateException("An audit actor requires a write transaction and a positive user ID.");
            }
            try (PreparedStatement statement = connection.prepareStatement("SET @audit_user_id = ?")) {
                statement.setQueryTimeout(10);
                statement.setLong(1, userId);
                statement.execute();
            } catch (SQLException ex) {
                throw SqlErrors.translate(ex);
            }
        }
    }
}
