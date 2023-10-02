package com.esq.rbac.service.util;

import com.esq.rbac.service.organization.embedded.OrganizationAttributeInfo;
import com.esq.rbac.service.organization.organizationattribte.domain.OrganizationAttribute;

public class OrganizationAttributeUtil {

    public static OrganizationAttributeInfo fromOrganizationAttribute(OrganizationAttribute organizationAttribute) {

        OrganizationAttributeInfo organizationAttributeInfo = new OrganizationAttributeInfo();

        organizationAttributeInfo.setApplicationId(organizationAttribute.getApplicationId());
        organizationAttributeInfo.setAttributeId(organizationAttribute.getAttributeId());
        organizationAttributeInfo.setAttributeName(organizationAttribute.getAttributeName());
        organizationAttributeInfo.setAttributeValue(organizationAttribute.getAttributeValue());
        organizationAttributeInfo.setOrganizationId(organizationAttribute.getOrganizationId());
        return organizationAttributeInfo;
    }
}