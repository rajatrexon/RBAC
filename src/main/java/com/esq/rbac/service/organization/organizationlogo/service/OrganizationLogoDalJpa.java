package com.esq.rbac.service.organization.organizationlogo.service;

import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.organization.organizationlogo.domain.OrganizationLogo;
import com.esq.rbac.service.organization.organizationlogo.repository.OrganizationLogoRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrganizationLogoDalJpa extends BaseDalJpa implements OrganizationLogoDal{


    private OrganizationLogoRepository organizationLogoRepository;

    @Autowired
    public void setOrganizationLogoRepository(OrganizationLogoRepository organizationLogoRepository) {
        this.organizationLogoRepository = organizationLogoRepository;
    }


    @Override
    public void set(long organizationId, int loggedinUserId, OrganizationLogo organizationLogo, String organizationName) {
        organizationLogo.setOrganizationId(organizationId);
        setObjectChangeSet(organizationLogo, organizationName);
        organizationLogo.setUpdatedOn(DateTime.now().toDate());
        organizationLogo.setUpdatedBy(loggedinUserId);
        organizationLogoRepository.save(organizationLogo);
    }

    @Override
    public OrganizationLogo get(long organizationId) {
        return organizationLogoRepository.findById(organizationId).get();
    }

    private void setObjectChangeSet(OrganizationLogo organizationLogo, String organizationName) {
        clearObjectChangeSet();
        putToObjectChangeSet(OBJECTCHANGES_ORGANIZATIONID, organizationLogo.getOrganizationId().toString());
        putToObjectChangeSet(OBJECTNAME, organizationName);
        checkObjectPutToObjectChangeSet(OBJECTCHANGES_ORGANIZATIONLOGO);
    }
}
