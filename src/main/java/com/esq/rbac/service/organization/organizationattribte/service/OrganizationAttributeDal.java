package com.esq.rbac.service.organization.organizationattribte.service;

import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.organization.organizationattribte.domain.OrganizationAttribute;
import com.esq.rbac.service.organization.embedded.OrganizationAttributeInfo;
import com.esq.rbac.service.organization.embedded.OrganizationAttributeWithTenant;
import com.esq.rbac.service.util.dal.Options;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrganizationAttributeDal extends BaseDal {

    @Transactional(propagation = Propagation.REQUIRED)
    OrganizationAttribute create(OrganizationAttribute organizationAttribute);

    Boolean isAttributeValid(OrganizationAttribute organizationAttribute);

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    boolean isAttributeExists(OrganizationAttribute organizationAttribute);

    @Transactional(propagation = Propagation.REQUIRED)
    OrganizationAttribute update(OrganizationAttribute organizationAttribute);

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    OrganizationAttribute toOrganizationAttribute(OrganizationAttributeInfo organizationAttributeInfo);

    @Transactional(propagation = Propagation.REQUIRED)
    void delete(OrganizationAttribute organizationAttribute);

    @Transactional(propagation = Propagation.REQUIRED)
    void deleteByOrganizationId(Long organizationId);

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    List<OrganizationAttributeInfo> getList(Options options);

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    List<OrganizationAttributeWithTenant> getListForOrganizationAttributeWithTenant(OrganizationAttributeWithTenant organizationAttributeWithTenant);
}
