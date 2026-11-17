package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import model.OfficeCode;
import model.OfficeDraft;
import model.Organization;
import service.ServiceException;

final class OrganizationDao extends BaseDao implements OrganizationRepository {
    private static final String SELECT = "SELECT id, code, name, queue_open, is_active, created_at, updated_at FROM organizations";

    OrganizationDao(Connection connection, boolean writable) { super(connection, writable); }

    @Override public List<Organization> list() throws ServiceException {
        return query(SELECT + " ORDER BY FIELD(code, 'LSG','PSITS','PHG','LIBRARY','CCSO','CIT','ESO')", s -> {}, OrganizationDao::map);
    }
    @Override public Optional<Organization> findById(long id) throws ServiceException {
        return query(SELECT + " WHERE id = ?", s -> s.setLong(1, id), OrganizationDao::map).stream().findFirst();
    }
    @Override public Optional<Organization> lockById(long id) throws ServiceException {
        requireWritable();
        return query(SELECT + " WHERE id = ? FOR UPDATE", s -> s.setLong(1, id), OrganizationDao::map).stream().findFirst();
    }
    @Override public Optional<Organization> lockSharedById(long id) throws ServiceException {
        requireWritable();return query(SELECT+" WHERE id=? FOR SHARE",s->s.setLong(1,id),OrganizationDao::map).stream().findFirst();
    }
    @Override public void update(long id, OfficeDraft draft) throws ServiceException {
        update("UPDATE organizations SET name = ?, queue_open = ?, is_active = ? WHERE id = ?", s -> {
            s.setString(1, draft.name()); s.setBoolean(2, draft.queueOpen()); s.setBoolean(3, draft.active()); s.setLong(4, id);
        });
    }
    @Override public boolean hasActiveTickets(long id) throws ServiceException {
        return !query("SELECT id FROM queue_tickets WHERE org_id = ? AND status IN ('waiting','called','serving','deferred') LIMIT 1", s -> s.setLong(1, id), r -> r.getLong(1)).isEmpty();
    }
    private static Organization map(ResultSet r) throws SQLException {
        return new Organization(r.getLong("id"), OfficeCode.valueOf(r.getString("code")), r.getString("name"),
                r.getBoolean("queue_open"), r.getBoolean("is_active"), r.getObject("created_at", LocalDateTime.class), r.getObject("updated_at", LocalDateTime.class));
    }
}
