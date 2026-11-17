package dao;
import java.util.*;
import model.*;
import service.ServiceException;
public interface ClearanceRepository {
    List<ClearanceRow> rows(long student,long term,Long office) throws ServiceException;
    void save(long student,long office,long term,ClearanceDraft draft,long actor) throws ServiceException;
}
