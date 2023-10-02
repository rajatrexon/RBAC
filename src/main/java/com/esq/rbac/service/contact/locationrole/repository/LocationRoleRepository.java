package com.esq.rbac.service.contact.locationrole.repository;

import com.esq.rbac.service.base.repository.Repository;
import com.esq.rbac.service.contact.locationrole.queries.LocationRoleQueries;
import com.esq.rbac.service.contact.locationrole.domain.LocationRole;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Slf4j
@Service("LocationRoleRepository")
public class LocationRoleRepository extends Repository<LocationRole> {

    public LocationRoleRepository() {
        super(LocationRole.class);
    }

    @Override
    public void deleteById(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocationRole create(LocationRole instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocationRole update(long id, LocationRole instance) {
        throw new UnsupportedOperationException();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<LocationRole> getRoles() {
        TypedQuery<LocationRole> query = entityManager.createQuery(LocationRoleQueries.LIST_LOCATION_ROLE_NAME_ID, LocationRole.class);
        return query.getResultList();
    }
}







