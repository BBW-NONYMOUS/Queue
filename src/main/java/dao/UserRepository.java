package dao;

import java.util.Optional;
import java.util.List;
import model.UserAccount;
import model.UserDraft;
import model.UserFilter;
import service.ServiceException;

public interface UserRepository {
    Optional<LoginAccount> findByUsername(String username) throws ServiceException;
    Optional<LoginAccount> findById(long id) throws ServiceException;
    Optional<LoginAccount> lockById(long id) throws ServiceException;
    void recordLogin(long id) throws ServiceException;
    void changePassword(long id, String hash) throws ServiceException;
    List<UserAccount> search(UserFilter filter) throws ServiceException;
    List<Long> lockActiveAdministrators() throws ServiceException;
    long insert(UserDraft draft, Long studentId, String hash) throws ServiceException;
    void update(long id, UserDraft draft, Long studentId) throws ServiceException;
    void resetPassword(long id, String hash) throws ServiceException;
    boolean hasServingTickets(long id) throws ServiceException;
    Optional<LoginAccount> findByStudentId(long studentId) throws ServiceException;
}
