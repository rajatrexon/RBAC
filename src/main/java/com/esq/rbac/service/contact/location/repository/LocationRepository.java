package com.esq.rbac.service.contact.location.repository;

import com.esq.rbac.service.base.repository.Repository;
import com.esq.rbac.service.contact.location.queries.LocationQueries;
import com.esq.rbac.service.contact.location.domain.Location;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service("LocationRepository")
public class LocationRepository extends Repository<Location> {

    private static final String SQL_WILDCARD = "%";
    public LocationRepository() {
        super(Location.class);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Location update(long id, Location location) {
        log.debug("update; {} " + location);
        location.setId(id);
        return super.update(id, location);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Location> getLocations(String q) {
        TypedQuery<Location> query = entityManager.createQuery(
                LocationQueries.LIST_LOCATIONS, Location.class);
        if (q == null) {
            q = "";
        }
        query.setParameter("q", wildcarded(q));
        return query.getResultList();
    }

    private String wildcarded(String q) {
        StringBuilder sb = new StringBuilder();
        if (q.startsWith(SQL_WILDCARD) == false) {
            sb.append(SQL_WILDCARD);
        }
        sb.append(q.toLowerCase());
        if (q.endsWith(SQL_WILDCARD) == false) {
            sb.append(SQL_WILDCARD);
        }
        return sb.toString();
    }
}
