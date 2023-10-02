package com.esq.rbac.service.tenantattribute.service;

import com.esq.rbac.service.tenantattribute.domain.TenantAttribute;

import java.util.List;

public interface TenantAttributeDal {
    TenantAttribute create(TenantAttribute tenantAttribute);

    TenantAttribute update(TenantAttribute tenantAttribute);

    List<TenantAttribute> getTenantAttributesByTenantIdAndAppKey(Long tenantId, String appKey);

    List<TenantAttribute> getTenantAttributesByAppKey(String appKey);

    void deleteTenantAttributeByAttributeId(Integer attributeId);

}
