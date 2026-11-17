package dao;
import java.util.*;
import model.*;
import service.ServiceException;
public interface OversightRepository {
    List<AuditRow> audit(AuditFilter filter) throws ServiceException;
    Optional<AuditRow> auditEntry(long id) throws ServiceException;
    ReportData report(ReportFilter filter) throws ServiceException;
}
