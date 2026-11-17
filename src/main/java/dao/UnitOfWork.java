package dao;

import service.ServiceException;

/** Services define transaction work without importing JDBC types. */
public interface UnitOfWork {
    <T> T read(Work<T> work) throws ServiceException;
    default <T> T snapshot(Work<T> work) throws ServiceException { return read(work); }
    <T> T write(Long actorUserId, Work<T> work) throws ServiceException;

    @FunctionalInterface
    interface Work<T> {
        T execute(DaoContext context) throws ServiceException;
    }
}
