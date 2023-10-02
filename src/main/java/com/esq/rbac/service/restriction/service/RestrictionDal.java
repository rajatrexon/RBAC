package com.esq.rbac.service.restriction.service;

import com.esq.rbac.service.restriction.domain.Restriction;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface RestrictionDal {

    @Transactional(propagation = Propagation.REQUIRED)
    Restriction create(Restriction restriction);

    @Transactional(propagation = Propagation.REQUIRED)
    Restriction update(Restriction restriction);

    @Transactional(propagation = Propagation.SUPPORTS)
    Restriction getById(int restrictionId);

    @Transactional(propagation = Propagation.REQUIRED)
    void deleteById(int restrictionId);
}
