package dao;
import java.util.*;
import model.*;
import service.ServiceException;
public interface QueueRepository {
    QueueClock clock() throws ServiceException;
    Optional<Term> currentTerm(boolean lock) throws ServiceException;
    Optional<QueueRow> find(long id, boolean lock) throws ServiceException;
    Optional<QueueRow> byRequest(UUID request) throws ServiceException;
    Optional<QueueRow> activeStudent(long office, long student, java.time.LocalDate day) throws ServiceException;
    long nextSequence(long office, java.time.LocalDate day) throws ServiceException;
    long issue(long office, long student, long term, java.time.LocalDate day, long seq, long actor, UUID request) throws ServiceException;
    int expire(long office) throws ServiceException;
    Optional<QueueRow> occupied(long office) throws ServiceException;
    Optional<QueueRow> nextWaiting(long office) throws ServiceException;
    void call(long id, long actor) throws ServiceException;
    void transition(long id, QueueAction action, long actor, String reason) throws ServiceException;
    QueueSnapshot snapshot(long office, String status, int page) throws ServiceException;
    List<QueueRow> studentTickets(long student) throws ServiceException;
    List<BoardRow> board() throws ServiceException;
    List<QueueCallEvent> calls(long student) throws ServiceException;
}
