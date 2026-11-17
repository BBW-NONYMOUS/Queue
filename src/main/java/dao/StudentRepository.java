package dao;

import java.util.List;
import java.util.Optional;
import model.Student;
import model.StudentDraft;
import model.StudentFilter;
import service.ServiceException;

public interface StudentRepository {
    List<Student> search(StudentFilter filter) throws ServiceException;
    Optional<Student> findById(long id) throws ServiceException;
    Optional<Student> findByNumber(String number) throws ServiceException;
    Optional<Student> lockById(long id) throws ServiceException;
    long insert(StudentDraft draft) throws ServiceException;
    void update(long id, StudentDraft draft) throws ServiceException;
    boolean hasActiveTickets(long id) throws ServiceException;
}
