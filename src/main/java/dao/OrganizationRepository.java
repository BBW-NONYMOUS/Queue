package dao;

import java.util.List;
import java.util.Optional;
import model.Organization;
import model.OfficeDraft;
import service.ServiceException;

public interface OrganizationRepository {
    List<Organization> list() throws ServiceException;
    Optional<Organization> findById(long id) throws ServiceException;
    Optional<Organization> lockById(long id) throws ServiceException;
    Optional<Organization> lockSharedById(long id) throws ServiceException;
    void update(long id, OfficeDraft draft) throws ServiceException;
    boolean hasActiveTickets(long id) throws ServiceException;
}
