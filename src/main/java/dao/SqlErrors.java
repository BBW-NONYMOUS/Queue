package dao;

import java.sql.SQLException;
import service.ConflictException;
import service.DataAccessException;
import service.TransactionConflictException;

final class SqlErrors {
    private SqlErrors() {
    }

    static DataAccessException translate(SQLException ex) {
        if ("45001".equals(ex.getSQLState())) { return new service.QueueDateChangedException(ex); }
        if (ex.getErrorCode() == 1213 || ex.getErrorCode() == 1205
                || "40001".equals(ex.getSQLState())) {
            return new TransactionConflictException(ex);
        }
        if (ex.getSQLState() != null && ex.getSQLState().startsWith("23")) {
            return new ConflictException(ex);
        }
        return new DataAccessException("Cannot reach the database or complete the request. Contact the administrator.", ex);
    }
}
