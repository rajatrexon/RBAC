package com.esq.rbac.service.organization.organizationlogo.service;

import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.organization.organizationlogo.domain.OrganizationLogo;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface OrganizationLogoDal extends BaseDal {

    @Transactional(propagation = Propagation.REQUIRED)
    void set(long organizationId,int loggedinUserId, OrganizationLogo organizationLogo, String organizationName);

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    OrganizationLogo get(long organizationId);
}
