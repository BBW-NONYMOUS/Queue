package dao;

import service.ServiceException;

/** Valid only for the duration of one UnitOfWork callback; never retain it. */
public interface DaoContext {
    UserRepository users();
    StudentRepository students();
    OrganizationRepository organizations();
    FinanceRepository finance();
    QueueRepository queue();
    ClearanceRepository clearance();
    OversightRepository oversight();
    void auditActor(long userId) throws ServiceException;
}
