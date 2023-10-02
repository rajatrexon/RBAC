package com.esq.rbac.service.tenant.service;

import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.tenant.domain.Tenant;
import com.esq.rbac.service.util.dal.Options;

import java.util.List;
import java.util.Map;

public interface TenantDal {


    void evictSecondLevelCacheById(Long tenantId);

    Tenant create(Tenant tenant, AuditLogInfo auditLogInfo);

    Tenant update(Tenant tenant, AuditLogInfo auditLogInfo);

    void deleteById(Long tenantId, AuditLogInfo auditLogInfo);

    List<Tenant> list(Options options);

    List<Long> getTenantIds(Options options);

    long count(Options options);

    Tenant getById(Long tenantId);

    boolean checkEntityPermission(long tenantId, Options options);

    List<Tenant> searchList(Options options);

    long getSearchCount(Options options);

    List<Map<String,Object>> getTenantIdNames(Options options);

    List<Map<String,Object>> searchTenantIdNames(Options options);

    long searchTenantIdNamesCount(Options options);

    Tenant getTenantByTenantName(String tenantName);

    Tenant getHostTenant();
}
